/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.constraintlayout.coremp.widgets.analyzer

import androidx.constraintlayout.coremp.widgets.Barrier
import androidx.constraintlayout.coremp.widgets.ConstraintWidget
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.GONE
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.HORIZONTAL
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.MATCH_CONSTRAINT_PERCENT
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.MATCH_CONSTRAINT_RATIO
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.MATCH_CONSTRAINT_WRAP
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.UNKNOWN
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.VERTICAL
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.DimensionBehaviour
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.DimensionBehaviour.FIXED
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.DimensionBehaviour.MATCH_PARENT
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.DimensionBehaviour.WRAP_CONTENT
import androidx.constraintlayout.coremp.widgets.ConstraintWidgetContainer
import androidx.constraintlayout.coremp.widgets.Guideline
import androidx.constraintlayout.coremp.widgets.HelperWidget
import kotlin.math.max

class DependencyGraph(container: ConstraintWidgetContainer) {

    private val mWidgetcontainer: ConstraintWidgetContainer = container
    private var mNeedBuildGraph = true
    private var mNeedRedoMeasures = true
    private val mContainer: ConstraintWidgetContainer = container
    private val mRuns = ArrayList<WidgetRun>()

    // TODO: Unused, should we delete?
    @Suppress("unused")
    private val mRunGroups = ArrayList<RunGroup>()

    private var mMeasurer: BasicMeasure.Measurer? = null
    private val mMeasure = BasicMeasure.Measure()

    fun setMeasurer(measurer: BasicMeasure.Measurer?) {
        mMeasurer = measurer
    }

    private fun computeWrap(container: ConstraintWidgetContainer, orientation: Int): Int {
        val count: Int = mGroups.size
        var wrapSize: Long = 0
        for (i in 0 until count) {
            val run: RunGroup = mGroups[i]
            val size = run.computeWrapSize(container, orientation)
            wrapSize = max(wrapSize, size)
        }
        return wrapSize.toInt()
    }

    /**
     * Find and mark terminal widgets (trailing widgets) -- they are the only
     * ones we need to care for wrap_content checks
     */
    fun defineTerminalWidgets(
        horizontalBehavior: DimensionBehaviour,
        verticalBehavior: DimensionBehaviour,
    ) {
        if (mNeedBuildGraph) {
            buildGraph()
            if (USE_GROUPS) {
                var hasBarrier = false
                for (widget in mWidgetcontainer.mChildren) {
                    widget.isTerminalWidget[HORIZONTAL] = true
                    widget.isTerminalWidget[VERTICAL] = true
                    if (widget is Barrier) {
                        hasBarrier = true
                    }
                }
                if (!hasBarrier) {
                    for (group in mGroups) {
                        group.defineTerminalWidgets(
                            horizontalBehavior == WRAP_CONTENT,
                            verticalBehavior == WRAP_CONTENT,
                        )
                    }
                }
            }
        }
    }

    /**
     * Try to measure the layout by solving the graph of constraints directly
     *
     * @param optimizeWrap use the wrap_content optimizer
     * @return true if all widgets have been resolved
     */
    fun directMeasure(optimizeWrap: Boolean): Boolean {
        var optimizeWrap = optimizeWrap
        optimizeWrap = optimizeWrap and USE_GROUPS
        if (mNeedBuildGraph || mNeedRedoMeasures) {
            for (widget in mWidgetcontainer.mChildren) {
                widget.ensureWidgetRuns()
                widget.measured = false
                widget.mHorizontalRun!!.reset()
                widget.mVerticalRun!!.reset()
            }
            mWidgetcontainer.ensureWidgetRuns()
            mWidgetcontainer.measured = false
            mWidgetcontainer.mHorizontalRun!!.reset()
            mWidgetcontainer.mVerticalRun!!.reset()
            mNeedRedoMeasures = false
        }
        val avoid: Boolean = basicMeasureWidgets(mContainer)
        if (avoid) {
            return false
        }
        mWidgetcontainer.setX(0)
        mWidgetcontainer.setY(0)
        val originalHorizontalDimension = mWidgetcontainer.getDimensionBehaviour(HORIZONTAL)
        val originalVerticalDimension = mWidgetcontainer.getDimensionBehaviour(VERTICAL)
        if (mNeedBuildGraph) {
            buildGraph()
        }
        val x1 = mWidgetcontainer.x
        val y1 = mWidgetcontainer.y
        mWidgetcontainer.mHorizontalRun!!.start.resolve(x1)
        mWidgetcontainer.mVerticalRun!!.start.resolve(y1)

        // Let's do the easy steps first -- anything that can be immediately measured
        // Whatever is left for the dimension will be match_constraints.
        measureWidgets()

        // If we have to support wrap, let's see if we can compute it directly
        if (originalHorizontalDimension == WRAP_CONTENT ||
            originalVerticalDimension == WRAP_CONTENT
        ) {
            if (optimizeWrap) {
                for (run in mRuns) {
                    if (!run.supportsWrapComputation()) {
                        optimizeWrap = false
                        break
                    }
                }
            }
            if (optimizeWrap && originalHorizontalDimension == WRAP_CONTENT) {
                mWidgetcontainer.setHorizontalDimensionBehaviour(FIXED)
                mWidgetcontainer.width = (computeWrap(mWidgetcontainer, HORIZONTAL))
                mWidgetcontainer.mHorizontalRun!!.mDimension.resolve(mWidgetcontainer.width)
            }
            if (optimizeWrap && originalVerticalDimension == WRAP_CONTENT) {
                mWidgetcontainer.setVerticalDimensionBehaviour(FIXED)
                mWidgetcontainer.height = (computeWrap(mWidgetcontainer, VERTICAL))
                mWidgetcontainer.mVerticalRun!!.mDimension.resolve(mWidgetcontainer.height)
            }
        }
        var checkRoot = false

        // Now, depending on our own dimension behavior, we may want to solve
        // one dimension before the other
        if (mWidgetcontainer.mListDimensionBehaviors[HORIZONTAL]
            == FIXED ||
            mWidgetcontainer.mListDimensionBehaviors[HORIZONTAL]
            == MATCH_PARENT
        ) {
            // solve horizontal dimension
            val x2 = x1 + mWidgetcontainer.width
            mWidgetcontainer.mHorizontalRun!!.end.resolve(x2)
            mWidgetcontainer.mHorizontalRun!!.mDimension.resolve(x2 - x1)
            measureWidgets()
            if (mWidgetcontainer.mListDimensionBehaviors[VERTICAL] == FIXED ||
                mWidgetcontainer.mListDimensionBehaviors[VERTICAL] == MATCH_PARENT
            ) {
                val y2 = y1 + mWidgetcontainer.height
                mWidgetcontainer.mVerticalRun!!.end.resolve(y2)
                mWidgetcontainer.mVerticalRun!!.mDimension.resolve(y2 - y1)
            }
            measureWidgets()
            checkRoot = true
        } else {
            // we'll bail out to the solver...
        }

        // Let's apply what we did resolve
        for (run in mRuns) {
            if (run.mWidget == mWidgetcontainer && !run.mResolved) {
                continue
            }
            run.applyToWidget()
        }
        var allResolved = true
        for (run in mRuns) {
            if (!checkRoot && run.mWidget == mWidgetcontainer) {
                continue
            }
            if (!run.start.resolved) {
                allResolved = false
                break
            }
            if (!run.end.resolved && run !is GuidelineReference) {
                allResolved = false
                break
            }
            if (!run.mDimension.resolved &&
                run !is ChainRun && run !is GuidelineReference
            ) {
                allResolved = false
                break
            }
        }
        mWidgetcontainer.setHorizontalDimensionBehaviour(originalHorizontalDimension!!)
        mWidgetcontainer.setVerticalDimensionBehaviour(originalVerticalDimension!!)
        return allResolved
    }

    // @TODO: add description
    @Suppress("UNUSED_PARAMETER")
    fun directMeasureSetup(optimizeWrap: Boolean): Boolean {
        if (mNeedBuildGraph) {
            for (widget in mWidgetcontainer.mChildren) {
                widget.ensureWidgetRuns()
                widget.measured = false
                widget.mHorizontalRun!!.mDimension.resolved = false
                widget.mHorizontalRun!!.mResolved = false
                widget.mHorizontalRun!!.reset()
                widget.mVerticalRun!!.mDimension.resolved = false
                widget.mVerticalRun!!.mResolved = false
                widget.mVerticalRun!!.reset()
            }
            mWidgetcontainer.ensureWidgetRuns()
            mWidgetcontainer.measured = false
            mWidgetcontainer.mHorizontalRun!!.mDimension.resolved = false
            mWidgetcontainer.mHorizontalRun!!.mResolved = false
            mWidgetcontainer.mHorizontalRun!!.reset()
            mWidgetcontainer.mVerticalRun!!.mDimension.resolved = false
            mWidgetcontainer.mVerticalRun!!.mResolved = false
            mWidgetcontainer.mVerticalRun!!.reset()
            buildGraph()
        }
        val avoid: Boolean = basicMeasureWidgets(mContainer)
        if (avoid) {
            return false
        }
        mWidgetcontainer.setX(0)
        mWidgetcontainer.setY(0)
        mWidgetcontainer.mHorizontalRun!!.start.resolve(0)
        mWidgetcontainer.mVerticalRun!!.start.resolve(0)
        return true
    }

    // @TODO: add description
    fun directMeasureWithOrientation(optimizeWrap: Boolean, orientation: Int): Boolean {
        var optimizeWrap = optimizeWrap
        optimizeWrap = optimizeWrap and USE_GROUPS
        val originalHorizontalDimension = mWidgetcontainer.getDimensionBehaviour(HORIZONTAL)
        val originalVerticalDimension = mWidgetcontainer.getDimensionBehaviour(VERTICAL)
        val x1 = mWidgetcontainer.x
        val y1 = mWidgetcontainer.y

        // If we have to support wrap, let's see if we can compute it directly
        if (optimizeWrap && (
                originalHorizontalDimension == WRAP_CONTENT ||
                    originalVerticalDimension == WRAP_CONTENT
                )
        ) {
            for (run in mRuns) {
                if (run.orientation == orientation &&
                    !run.supportsWrapComputation()
                ) {
                    optimizeWrap = false
                    break
                }
            }
            if (orientation == HORIZONTAL) {
                if (optimizeWrap && originalHorizontalDimension == WRAP_CONTENT) {
                    mWidgetcontainer.setHorizontalDimensionBehaviour(FIXED)
                    mWidgetcontainer.width = (computeWrap(mWidgetcontainer, HORIZONTAL))
                    mWidgetcontainer.mHorizontalRun!!.mDimension.resolve(mWidgetcontainer.width)
                }
            } else {
                if (optimizeWrap && originalVerticalDimension == WRAP_CONTENT) {
                    mWidgetcontainer.setVerticalDimensionBehaviour(FIXED)
                    mWidgetcontainer.height = (computeWrap(mWidgetcontainer, VERTICAL))
                    mWidgetcontainer.mVerticalRun!!.mDimension.resolve(mWidgetcontainer.height)
                }
            }
        }
        var checkRoot = false

        // Now, depending on our own dimension behavior, we may want to solve
        // one dimension before the other
        if (orientation == HORIZONTAL) {
            if (mWidgetcontainer.mListDimensionBehaviors[HORIZONTAL] == FIXED ||
                mWidgetcontainer.mListDimensionBehaviors[HORIZONTAL] == MATCH_PARENT
            ) {
                val x2 = x1 + mWidgetcontainer.width
                mWidgetcontainer.mHorizontalRun!!.end.resolve(x2)
                mWidgetcontainer.mHorizontalRun!!.mDimension.resolve(x2 - x1)
                checkRoot = true
            }
        } else {
            if (mWidgetcontainer.mListDimensionBehaviors[VERTICAL] == FIXED ||
                mWidgetcontainer.mListDimensionBehaviors[VERTICAL] == MATCH_PARENT
            ) {
                val y2 = y1 + mWidgetcontainer.height
                mWidgetcontainer.mVerticalRun!!.end.resolve(y2)
                mWidgetcontainer.mVerticalRun!!.mDimension.resolve(y2 - y1)
                checkRoot = true
            }
        }
        measureWidgets()

        // Let's apply what we did resolve
        for (run in mRuns) {
            if (run.orientation != orientation) {
                continue
            }
            if (run.mWidget == mWidgetcontainer && !run.mResolved) {
                continue
            }
            run.applyToWidget()
        }
        var allResolved = true
        for (run in mRuns) {
            if (run.orientation != orientation) {
                continue
            }
            if (!checkRoot && run.mWidget == mWidgetcontainer) {
                continue
            }
            if (!run.start.resolved) {
                allResolved = false
                break
            }
            if (!run.end.resolved) {
                allResolved = false
                break
            }
            if (run !is ChainRun && !run.mDimension.resolved) {
                allResolved = false
                break
            }
        }
        mWidgetcontainer.setHorizontalDimensionBehaviour(originalHorizontalDimension!!)
        mWidgetcontainer.setVerticalDimensionBehaviour(originalVerticalDimension!!)
        return allResolved
    }

    /**
     * Convenience function to fill in the measure spec
     *
     * @param widget the widget to measure
     */
    private fun measure(
        widget: ConstraintWidget,
        horizontalBehavior: DimensionBehaviour,
        horizontalDimension: Int,
        verticalBehavior: DimensionBehaviour,
        verticalDimension: Int,
    ) {
        mMeasure.horizontalBehavior = horizontalBehavior
        mMeasure.verticalBehavior = verticalBehavior
        mMeasure.horizontalDimension = horizontalDimension
        mMeasure.verticalDimension = verticalDimension
        mMeasurer!!.measure(widget, mMeasure)
        widget.width = mMeasure.measuredWidth
        widget.height = mMeasure.measuredHeight
        widget.hasBaseline = mMeasure.measuredHasBaseline
        widget.baselineDistance = mMeasure.measuredBaseline
    }

    private fun basicMeasureWidgets(constraintWidgetContainer: ConstraintWidgetContainer): Boolean {
        for (widget in constraintWidgetContainer.mChildren) {
            var horizontal = widget.mListDimensionBehaviors[HORIZONTAL]
            var vertical = widget.mListDimensionBehaviors[VERTICAL]

            if (widget.visibility == GONE) {
                widget.measured = true
                continue
            }

            // Basic validation
            // TODO: might move this earlier in the process
            if (widget.mMatchConstraintPercentWidth < 1 && horizontal == MATCH_CONSTRAINT) {
                widget.mMatchConstraintDefaultWidth = MATCH_CONSTRAINT_PERCENT
            }
            if (widget.mMatchConstraintPercentHeight < 1 && vertical == MATCH_CONSTRAINT) {
                widget.mMatchConstraintDefaultHeight = MATCH_CONSTRAINT_PERCENT
            }
            if (widget.getDimensionRatio() > 0) {
                if (horizontal == MATCH_CONSTRAINT && (vertical == WRAP_CONTENT || vertical == FIXED)) {
                    widget.mMatchConstraintDefaultWidth = MATCH_CONSTRAINT_RATIO
                } else if (vertical == MATCH_CONSTRAINT && (horizontal == WRAP_CONTENT || horizontal == FIXED)) {
                    widget.mMatchConstraintDefaultHeight = MATCH_CONSTRAINT_RATIO
                } else if (horizontal == MATCH_CONSTRAINT && vertical == MATCH_CONSTRAINT) {
                    if (widget.mMatchConstraintDefaultWidth == MATCH_CONSTRAINT_SPREAD) {
                        widget.mMatchConstraintDefaultWidth = MATCH_CONSTRAINT_RATIO
                    }
                    if (widget.mMatchConstraintDefaultHeight == MATCH_CONSTRAINT_SPREAD) {
                        widget.mMatchConstraintDefaultHeight = MATCH_CONSTRAINT_RATIO
                    }
                }
            }
            if (horizontal == MATCH_CONSTRAINT && widget.mMatchConstraintDefaultWidth == MATCH_CONSTRAINT_WRAP) {
                if (widget.mLeft.mTarget == null || widget.mRight.mTarget == null) {
                    horizontal = WRAP_CONTENT
                }
            }
            if (vertical == MATCH_CONSTRAINT && widget.mMatchConstraintDefaultHeight == MATCH_CONSTRAINT_WRAP) {
                if (widget.mTop.mTarget == null || widget.mBottom.mTarget == null) {
                    vertical = WRAP_CONTENT
                }
            }
            widget.mHorizontalRun!!.mDimensionBehavior = horizontal
            widget.mHorizontalRun!!.matchConstraintsType = widget.mMatchConstraintDefaultWidth
            widget.mVerticalRun!!.mDimensionBehavior = vertical
            widget.mVerticalRun!!.matchConstraintsType = widget.mMatchConstraintDefaultHeight
            if ((horizontal == MATCH_PARENT || horizontal == FIXED || horizontal == WRAP_CONTENT) &&
                (vertical == MATCH_PARENT || vertical == FIXED || vertical == WRAP_CONTENT)
            ) {
                var width = widget.width
                if (horizontal == MATCH_PARENT) {
                    width = (constraintWidgetContainer.width - widget.mLeft.mMargin - widget.mRight.mMargin)
                    horizontal = FIXED
                }
                var height = widget.height
                if (vertical == MATCH_PARENT) {
                    height = (constraintWidgetContainer.height - widget.mTop.mMargin - widget.mBottom.mMargin)
                    vertical = FIXED
                }
                measure(widget, horizontal, width, vertical, height)
                widget.mHorizontalRun!!.mDimension.resolve(widget.width)
                widget.mVerticalRun!!.mDimension.resolve(widget.height)
                widget.measured = true
                continue
            }
            if (horizontal == MATCH_CONSTRAINT && (vertical == WRAP_CONTENT || vertical == FIXED)) {
                if (widget.mMatchConstraintDefaultWidth == MATCH_CONSTRAINT_RATIO) {
                    if (vertical == WRAP_CONTENT) {
                        measure(widget, WRAP_CONTENT, 0, WRAP_CONTENT, 0)
                    }
                    val height = widget.height
                    val width = (height * widget.mDimensionRatio + 0.5f).toInt()
                    measure(widget, FIXED, width, FIXED, height)
                    widget.mHorizontalRun!!.mDimension.resolve(widget.width)
                    widget.mVerticalRun!!.mDimension.resolve(widget.height)
                    widget.measured = true
                    continue
                } else if (widget.mMatchConstraintDefaultWidth == MATCH_CONSTRAINT_WRAP) {
                    measure(widget, WRAP_CONTENT, 0, vertical, 0)
                    widget.mHorizontalRun!!.mDimension.wrapValue = widget.width
                    continue
                } else if (widget.mMatchConstraintDefaultWidth == MATCH_CONSTRAINT_PERCENT) {
                    if (constraintWidgetContainer.mListDimensionBehaviors[HORIZONTAL] == FIXED ||
                        constraintWidgetContainer.mListDimensionBehaviors[HORIZONTAL] == MATCH_PARENT
                    ) {
                        val percent = widget.mMatchConstraintPercentWidth
                        val width = (0.5f + percent * constraintWidgetContainer.width).toInt()
                        val height = widget.height
                        measure(widget, FIXED, width, vertical, height)
                        widget.mHorizontalRun!!.mDimension.resolve(widget.width)
                        widget.mVerticalRun!!.mDimension.resolve(widget.height)
                        widget.measured = true
                        continue
                    }
                } else {
                    // let's verify we have both constraints
                    if (widget.mListAnchors[ConstraintWidget.ANCHOR_LEFT].mTarget == null ||
                        widget.mListAnchors[ConstraintWidget.ANCHOR_RIGHT].mTarget == null
                    ) {
                        measure(widget, WRAP_CONTENT, 0, vertical, 0)
                        widget.mHorizontalRun!!.mDimension.resolve(widget.width)
                        widget.mVerticalRun!!.mDimension.resolve(widget.height)
                        widget.measured = true
                        continue
                    }
                }
            }
            if (vertical == MATCH_CONSTRAINT && (horizontal == WRAP_CONTENT || horizontal == FIXED)) {
                if (widget.mMatchConstraintDefaultHeight == MATCH_CONSTRAINT_RATIO) {
                    if (horizontal == WRAP_CONTENT) {
                        measure(widget, WRAP_CONTENT, 0, WRAP_CONTENT, 0)
                    }
                    val width = widget.width
                    var ratio = widget.mDimensionRatio
                    if (widget.dimensionRatioSide == UNKNOWN) {
                        ratio = 1f / ratio
                    }
                    val height = (width * ratio + 0.5f).toInt()
                    measure(widget, FIXED, width, FIXED, height)
                    widget.mHorizontalRun!!.mDimension.resolve(widget.width)
                    widget.mVerticalRun!!.mDimension.resolve(widget.height)
                    widget.measured = true
                    continue
                } else if (widget.mMatchConstraintDefaultHeight == MATCH_CONSTRAINT_WRAP) {
                    measure(widget, horizontal, 0, WRAP_CONTENT, 0)
                    widget.mVerticalRun!!.mDimension.wrapValue = widget.height
                    continue
                } else if (widget.mMatchConstraintDefaultHeight == MATCH_CONSTRAINT_PERCENT) {
                    if (constraintWidgetContainer.mListDimensionBehaviors[VERTICAL] == FIXED ||
                        constraintWidgetContainer.mListDimensionBehaviors[VERTICAL] == MATCH_PARENT
                    ) {
                        val percent = widget.mMatchConstraintPercentHeight
                        val width = widget.width
                        val height =
                            (0.5f + percent * constraintWidgetContainer.height).toInt()
                        measure(widget, horizontal, width, FIXED, height)
                        widget.mHorizontalRun!!.mDimension.resolve(widget.width)
                        widget.mVerticalRun!!.mDimension.resolve(widget.height)
                        widget.measured = true
                        continue
                    }
                } else {
                    // let's verify we have both constraints
                    if (widget.mListAnchors[ConstraintWidget.ANCHOR_TOP].mTarget == null ||
                        widget.mListAnchors[ConstraintWidget.ANCHOR_BOTTOM].mTarget == null
                    ) {
                        measure(widget, WRAP_CONTENT, 0, vertical, 0)
                        widget.mHorizontalRun!!.mDimension.resolve(widget.width)
                        widget.mVerticalRun!!.mDimension.resolve(widget.height)
                        widget.measured = true
                        continue
                    }
                }
            }
            if (horizontal == MATCH_CONSTRAINT && vertical == MATCH_CONSTRAINT) {
                if (widget.mMatchConstraintDefaultWidth == MATCH_CONSTRAINT_WRAP ||
                    widget.mMatchConstraintDefaultHeight == MATCH_CONSTRAINT_WRAP
                ) {
                    measure(widget, WRAP_CONTENT, 0, WRAP_CONTENT, 0)
                    widget.mHorizontalRun!!.mDimension.wrapValue = widget.width
                    widget.mVerticalRun!!.mDimension.wrapValue = widget.height
                } else if ((widget.mMatchConstraintDefaultHeight == MATCH_CONSTRAINT_PERCENT) &&
                    (widget.mMatchConstraintDefaultWidth == MATCH_CONSTRAINT_PERCENT) &&
                    constraintWidgetContainer.mListDimensionBehaviors[HORIZONTAL] == FIXED &&
                    constraintWidgetContainer.mListDimensionBehaviors[VERTICAL] == FIXED
                ) {
                    val horizPercent = widget.mMatchConstraintPercentWidth
                    val vertPercent = widget.mMatchConstraintPercentHeight
                    val width = (0.5f + horizPercent * constraintWidgetContainer.width).toInt()
                    val height =
                        (0.5f + vertPercent * constraintWidgetContainer.height).toInt()
                    measure(widget, FIXED, width, FIXED, height)
                    widget.mHorizontalRun!!.mDimension.resolve(widget.width)
                    widget.mVerticalRun!!.mDimension.resolve(widget.height)
                    widget.measured = true
                }
            }
        }
        return false
    }

    // @TODO: add description
    fun measureWidgets() {
        for (widget in mWidgetcontainer.mChildren) {
            if (widget.measured) {
                continue
            }
            val horiz = widget.mListDimensionBehaviors[HORIZONTAL]
            val vert = widget.mListDimensionBehaviors[VERTICAL]
            val horizMatchConstraintsType = widget.mMatchConstraintDefaultWidth
            val vertMatchConstraintsType = widget.mMatchConstraintDefaultHeight
            val horizWrap = (
                horiz == WRAP_CONTENT ||
                    (
                        horiz == MATCH_CONSTRAINT &&
                            horizMatchConstraintsType == MATCH_CONSTRAINT_WRAP
                        )
                )
            val vertWrap = (
                vert == WRAP_CONTENT ||
                    (
                        vert == MATCH_CONSTRAINT &&
                            vertMatchConstraintsType == MATCH_CONSTRAINT_WRAP
                        )
                )
            val horizResolved = widget.mHorizontalRun!!.mDimension.resolved
            val vertResolved = widget.mVerticalRun!!.mDimension.resolved
            if (horizResolved && vertResolved) {
                measure(
                    widget,
                    FIXED,
                    widget.mHorizontalRun!!.mDimension.value,
                    FIXED,
                    widget.mVerticalRun!!.mDimension.value,
                )
                widget.measured = true
            } else if (horizResolved && vertWrap) {
                measure(
                    widget,
                    FIXED,
                    widget.mHorizontalRun!!.mDimension.value,
                    WRAP_CONTENT,
                    widget.mVerticalRun!!.mDimension.value,
                )
                if (vert == MATCH_CONSTRAINT) {
                    widget.mVerticalRun!!.mDimension.wrapValue = widget.height
                } else {
                    widget.mVerticalRun!!.mDimension.resolve(widget.height)
                    widget.measured = true
                }
            } else if (vertResolved && horizWrap) {
                measure(
                    widget,
                    WRAP_CONTENT,
                    widget.mHorizontalRun!!.mDimension.value,
                    FIXED,
                    widget.mVerticalRun!!.mDimension.value,
                )
                if (horiz == MATCH_CONSTRAINT) {
                    widget.mHorizontalRun!!.mDimension.wrapValue = widget.width
                } else {
                    widget.mHorizontalRun!!.mDimension.resolve(widget.width)
                    widget.measured = true
                }
            }
            if (widget.measured && widget.mVerticalRun!!.mBaselineDimension != null) {
                widget.mVerticalRun!!.mBaselineDimension!!.resolve(widget.baselineDistance)
            }
        }
    }

    /**
     * Invalidate the graph of constraints
     */
    fun invalidateGraph() {
        mNeedBuildGraph = true
    }

    /**
     * Mark the widgets as needing to be remeasured
     */
    fun invalidateMeasures() {
        mNeedRedoMeasures = true
    }

    var mGroups = ArrayList<RunGroup>()

    // @TODO: add description
    fun buildGraph() {
        // First, let's identify the overall dependency graph
        buildGraph(mRuns)
        if (USE_GROUPS) {
            mGroups.clear()
            // Then get the horizontal and vertical groups
            RunGroup.index = 0
            findGroup(mWidgetcontainer.mHorizontalRun!!, HORIZONTAL, mGroups)
            findGroup(mWidgetcontainer.mVerticalRun!!, VERTICAL, mGroups)
        }
        mNeedBuildGraph = false
    }

    // @TODO: add description
    fun buildGraph(runs: ArrayList<WidgetRun>) {
        runs.clear()
        mContainer.mHorizontalRun!!.clear()
        mContainer.mVerticalRun!!.clear()
        runs.add(mContainer.mHorizontalRun!!)
        runs.add(mContainer.mVerticalRun!!)
        var chainRuns: HashSet<ChainRun>? = null
        for (widget in mContainer.mChildren) {
            if (widget is Guideline) {
                runs.add(GuidelineReference(widget))
                continue
            }
            if (widget.isInHorizontalChain()) {
                if (widget.horizontalChainRun == null) {
                    // build the horizontal chain
                    widget.horizontalChainRun = ChainRun(widget, HORIZONTAL)
                }
                if (chainRuns == null) {
                    chainRuns = HashSet()
                }
                chainRuns.add(widget.horizontalChainRun!!)
            } else {
                runs.add(widget.mHorizontalRun!!)
            }
            if (widget.isInVerticalChain()) {
                if (widget.verticalChainRun == null) {
                    // build the vertical chain
                    widget.verticalChainRun = ChainRun(widget, VERTICAL)
                }
                if (chainRuns == null) {
                    chainRuns = HashSet()
                }
                chainRuns.add(widget.verticalChainRun!!)
            } else {
                runs.add(widget.mVerticalRun!!)
            }
            if (widget is HelperWidget) {
                runs.add(HelperReferences(widget))
            }
        }
        if (chainRuns != null) {
            runs.addAll(chainRuns)
        }
        for (run in runs) {
            run.clear()
        }
        for (run in runs) {
            if (run.mWidget == mContainer) {
                continue
            }
            run.apply()
        }
        if (DEBUG) {
            displayGraph()
        }
    }

    private fun displayGraph() {
        var content = "digraph {\n"
        for (run in mRuns) {
            content = generateDisplayGraph(run, content)
        }
        content += "\n}\n"
        println("content:<<\n$content\n>>")
    }

    private fun applyGroup(
        node: DependencyNode,
        orientation: Int,
        direction: Int,
        end: DependencyNode?,
        groups: ArrayList<RunGroup>,
        group: RunGroup?,
    ) {
        var group: RunGroup? = group
        val run = node.mRun
        if (run.mRunGroup != null || run == mWidgetcontainer.mHorizontalRun || run == mWidgetcontainer.mVerticalRun) {
            return
        }
        if (group == null) {
            group = RunGroup(run, direction)
            groups.add(group)
        }
        run.mRunGroup = group
        group.add(run)
        for (dependent in run.start.mDependencies) {
            if (dependent is DependencyNode) {
                applyGroup(
                    dependent,
                    orientation,
                    RunGroup.START,
                    end,
                    groups,
                    group,
                )
            }
        }
        for (dependent in run.end.mDependencies) {
            if (dependent is DependencyNode) {
                applyGroup(
                    dependent,
                    orientation,
                    RunGroup.END,
                    end,
                    groups,
                    group,
                )
            }
        }
        if (orientation == VERTICAL && run is VerticalWidgetRun) {
            for (dependent in run.baseline.mDependencies) {
                if (dependent is DependencyNode) {
                    applyGroup(
                        dependent,
                        orientation,
                        RunGroup.BASELINE,
                        end,
                        groups,
                        group,
                    )
                }
            }
        }
        for (target in run.start.mTargets) {
            if (target == end) {
                group.dual = true
            }
            applyGroup(target, orientation, RunGroup.START, end, groups, group)
        }
        for (target in run.end.mTargets) {
            if (target == end) {
                group.dual = true
            }
            applyGroup(target, orientation, RunGroup.END, end, groups, group)
        }
        if (orientation == VERTICAL && run is VerticalWidgetRun) {
            for (target in run.baseline.mTargets) {
                applyGroup(target, orientation, RunGroup.BASELINE, end, groups, group)
            }
        }
    }

    private fun findGroup(run: WidgetRun, orientation: Int, groups: ArrayList<RunGroup>) {
        for (dependent in run.start.mDependencies) {
            if (dependent is DependencyNode) {
                applyGroup(dependent, orientation, RunGroup.START, run.end, groups, null)
            } else if (dependent is WidgetRun) {
                applyGroup(dependent.start, orientation, RunGroup.START, run.end, groups, null)
            }
        }
        for (dependent in run.end.mDependencies) {
            if (dependent is DependencyNode) {
                applyGroup(dependent, orientation, RunGroup.END, run.start, groups, null)
            } else if (dependent is WidgetRun) {
                applyGroup(dependent.end, orientation, RunGroup.END, run.start, groups, null)
            }
        }
        if (orientation == VERTICAL) {
            for (dependent in (run as VerticalWidgetRun).baseline.mDependencies) {
                if (dependent is DependencyNode) {
                    applyGroup(dependent, orientation, RunGroup.BASELINE, null, groups, null)
                }
            }
        }
    }

    private fun generateDisplayNode(
        node: DependencyNode,
        centeredConnection: Boolean,
        content: String,
    ): String {
        var content: String = content
        val contentBuilder = StringBuilder(content)
        for (target in node.mTargets) {
            var constraint = """
            
            ${node.name()}
            """.trimIndent()
            constraint += " -> " + target.name()
            if (node.mMargin > 0 || centeredConnection || node.mRun is HelperReferences) {
                constraint += "["
                if (node.mMargin > 0) {
                    constraint += "label=\"" + node.mMargin + "\""
                    if (centeredConnection) {
                        constraint += ","
                    }
                }
                if (centeredConnection) {
                    constraint += " style=dashed "
                }
                if (node.mRun is HelperReferences) {
                    constraint += " style=bold,color=gray "
                }
                constraint += "]"
            }
            constraint += "\n"
            contentBuilder.append(constraint)
        }
        content = contentBuilder.toString()
        //        for (DependencyNode dependency : node.dependencies) {
//            content = generateDisplayNode(dependency, content);
//        }
        return content
    }

    private fun nodeDefinition(run: WidgetRun): String {
        val orientation = if (run is VerticalWidgetRun) VERTICAL else HORIZONTAL
        val name = run.mWidget!!.debugName!!
        val definition = StringBuilder(name)
        val behaviour =
            if (orientation == HORIZONTAL) run.mWidget!!.horizontalDimensionBehaviour else run.mWidget!!.verticalDimensionBehaviour
        val runGroup = run.mRunGroup
        if (orientation == HORIZONTAL) {
            definition.append("_HORIZONTAL")
        } else {
            definition.append("_VERTICAL")
        }
        definition.append(" [shape=none, label=<")
        definition.append("<TABLE BORDER=\"0\" CELLSPACING=\"0\" CELLPADDING=\"2\">")
        definition.append("  <TR>")
        if (orientation == HORIZONTAL) {
            definition.append("    <TD ")
            if (run.start.resolved) {
                definition.append(" BGCOLOR=\"green\"")
            }
            definition.append(" PORT=\"LEFT\" BORDER=\"1\">L</TD>")
        } else {
            definition.append("    <TD ")
            if (run.start.resolved) {
                definition.append(" BGCOLOR=\"green\"")
            }
            definition.append(" PORT=\"TOP\" BORDER=\"1\">T</TD>")
        }
        definition.append("    <TD BORDER=\"1\" ")
        if (run.mDimension.resolved && !run.mWidget!!.measured) {
            definition.append(" BGCOLOR=\"green\" ")
        } else if (run.mDimension.resolved) {
            definition.append(" BGCOLOR=\"lightgray\" ")
        } else if (run.mWidget!!.measured) {
            definition.append(" BGCOLOR=\"yellow\" ")
        }
        if (behaviour == MATCH_CONSTRAINT) {
            definition.append("style=\"dashed\"")
        }
        definition.append(">")
        definition.append(name)
        if (runGroup != null) {
            definition.append(" [")
            definition.append(runGroup.mGroupIndex + 1)
            definition.append("/")
            definition.append(RunGroup.index)
            definition.append("]")
        }
        definition.append(" </TD>")
        if (orientation == HORIZONTAL) {
            definition.append("    <TD ")
            if (run.end.resolved) {
                definition.append(" BGCOLOR=\"green\"")
            }
            definition.append(" PORT=\"RIGHT\" BORDER=\"1\">R</TD>")
        } else {
            definition.append("    <TD ")
            if ((run as VerticalWidgetRun).baseline.resolved) {
                definition.append(" BGCOLOR=\"green\"")
            }
            definition.append(" PORT=\"BASELINE\" BORDER=\"1\">b</TD>")
            definition.append("    <TD ")
            if (run.end.resolved) {
                definition.append(" BGCOLOR=\"green\"")
            }
            definition.append(" PORT=\"BOTTOM\" BORDER=\"1\">B</TD>")
        }
        definition.append("  </TR></TABLE>")
        definition.append(">];\n")
        return definition.toString()
    }

    private fun generateChainDisplayGraph(chain: ChainRun, content: String): String {
        val orientation = chain.orientation
        val subgroup = StringBuilder("subgraph ")
        subgroup.append("cluster_")
        subgroup.append(chain.mWidget!!.debugName)
        if (orientation == HORIZONTAL) {
            subgroup.append("_h")
        } else {
            subgroup.append("_v")
        }
        subgroup.append(" {\n")
        var definitions = ""
        for (run in chain.mWidgets) {
            subgroup.append(run.mWidget!!.debugName)
            if (orientation == HORIZONTAL) {
                subgroup.append("_HORIZONTAL")
            } else {
                subgroup.append("_VERTICAL")
            }
            subgroup.append(";\n")
            definitions = generateDisplayGraph(run, definitions)
        }
        subgroup.append("}\n")
        return content + definitions + subgroup
    }

    private fun isCenteredConnection(start: DependencyNode, end: DependencyNode): Boolean {
        var startTargets = 0
        var endTargets = 0
        for (s in start.mTargets) {
            if (s != end) {
                startTargets++
            }
        }
        for (e in end.mTargets) {
            if (e != start) {
                endTargets++
            }
        }
        return startTargets > 0 && endTargets > 0
    }

    private fun generateDisplayGraph(root: WidgetRun, content: String): String {
        var content: String = content
        val start = root.start
        val end = root.end
        val sb = StringBuilder(content)
        if (root !is HelperReferences && start.mDependencies.isEmpty() &&
            end.mDependencies.isEmpty() && start.mTargets.isEmpty() &&
            end.mTargets.isEmpty()
        ) {
            return content
        }
        sb.append(nodeDefinition(root))
        val centeredConnection = isCenteredConnection(start, end)
        content = generateDisplayNode(start, centeredConnection, content)
        content = generateDisplayNode(end, centeredConnection, content)
        if (root is VerticalWidgetRun) {
            val baseline = root.baseline
            content = generateDisplayNode(baseline, centeredConnection, content)
        }
        if (root is HorizontalWidgetRun || root is ChainRun && root.orientation == HORIZONTAL) {
            val behaviour = root.mWidget!!.horizontalDimensionBehaviour
            if (behaviour == FIXED ||
                behaviour == WRAP_CONTENT
            ) {
                if (!start.mTargets.isEmpty() && end.mTargets.isEmpty()) {
                    sb.append("\n")
                    sb.append(end.name())
                    sb.append(" -> ")
                    sb.append(start.name())
                    sb.append("\n")
                } else if (start.mTargets.isEmpty() && !end.mTargets.isEmpty()) {
                    sb.append("\n")
                    sb.append(start.name())
                    sb.append(" -> ")
                    sb.append(end.name())
                    sb.append("\n")
                }
            } else {
                if (behaviour == MATCH_CONSTRAINT && root.mWidget!!.getDimensionRatio() > 0) {
                    sb.append("\n")
                    sb.append(root.mWidget!!.debugName)
                    sb.append("_HORIZONTAL -> ")
                    sb.append(root.mWidget!!.debugName)
                    sb.append("_VERTICAL;\n")
                }
            }
        } else if (root is VerticalWidgetRun || root is ChainRun && root.orientation == VERTICAL) {
            val behaviour = root.mWidget!!.verticalDimensionBehaviour
            if (behaviour == FIXED ||
                behaviour == WRAP_CONTENT
            ) {
                if (!start.mTargets.isEmpty() && end.mTargets.isEmpty()) {
                    sb.append("\n")
                    sb.append(end.name())
                    sb.append(" -> ")
                    sb.append(start.name())
                    sb.append("\n")
                } else if (start.mTargets.isEmpty() && !end.mTargets.isEmpty()) {
                    sb.append("\n")
                    sb.append(start.name())
                    sb.append(" -> ")
                    sb.append(end.name())
                    sb.append("\n")
                }
            } else {
                if (behaviour == MATCH_CONSTRAINT && root.mWidget!!.getDimensionRatio() > 0) {
                    sb.append("\n")
                    sb.append(root.mWidget!!.debugName)
                    sb.append("_VERTICAL -> ")
                    sb.append(root.mWidget!!.debugName)
                    sb.append("_HORIZONTAL;\n")
                }
            }
        }
        return if (root is ChainRun) {
            generateChainDisplayGraph(root, content)
        } else {
            sb.toString()
        }
    }

    companion object {
        private const val USE_GROUPS = true
        private const val DEBUG = false
    }
}
