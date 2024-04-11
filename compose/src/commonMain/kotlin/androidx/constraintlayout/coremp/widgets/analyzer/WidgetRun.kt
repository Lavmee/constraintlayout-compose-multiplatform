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

import androidx.constraintlayout.coremp.widgets.ConstraintAnchor
import androidx.constraintlayout.coremp.widgets.ConstraintWidget
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.HORIZONTAL
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.MATCH_CONSTRAINT_PERCENT
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.MATCH_CONSTRAINT_RATIO
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.MATCH_CONSTRAINT_WRAP
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.VERTICAL
import kotlin.math.max
import kotlin.math.min

abstract class WidgetRun(widget: ConstraintWidget?) : Dependency {
    var matchConstraintsType = 0
    var mWidget: ConstraintWidget? = widget
    var mRunGroup: RunGroup? = null
    var mDimensionBehavior: ConstraintWidget.DimensionBehaviour? = null
    var mDimension = DimensionDependency(this)

    var orientation: Int = HORIZONTAL
    var mResolved = false
    var start = DependencyNode(this)
    var end = DependencyNode(this)

    internal var mRunType: RunType = RunType.NONE

    abstract fun clear()

    abstract fun apply()

    abstract fun applyToWidget()

    abstract fun reset()

    abstract fun supportsWrapComputation(): Boolean

    @Suppress("unused")
    open fun isDimensionResolved(): Boolean {
        return mDimension.resolved
    }

    // @TODO: add description
    open fun isCenterConnection(): Boolean {
        var connections = 0
        var count = start.mTargets.size
        for (i in 0 until count) {
            val dependency = start.mTargets[i]
            if (dependency.mRun != this) {
                connections++
            }
        }
        count = end.mTargets.size
        for (i in 0 until count) {
            val dependency = end.mTargets[i]
            if (dependency.mRun != this) {
                connections++
            }
        }
        return connections >= 2
    }

    // @TODO: add description
    @Suppress("unused")
    open fun wrapSize(direction: Int): Long {
        if (mDimension.resolved) {
            var size = mDimension.value.toLong()
            if (isCenterConnection()) { // start.targets.size() > 0 && end.targets.size() > 0) {
                size += (start.mMargin - end.mMargin).toLong()
            } else {
                if (direction == RunGroup.START) {
                    size += start.mMargin.toLong()
                } else {
                    size -= end.mMargin.toLong()
                }
            }
            return size
        }
        return 0
    }

    protected fun getTarget(anchor: ConstraintAnchor): DependencyNode? {
        if (anchor.mTarget == null) {
            return null
        }
        var target: DependencyNode? = null
        val targetWidget = anchor.mTarget!!.mOwner
        val targetType = anchor.mTarget!!.mType
        when (targetType) {
            ConstraintAnchor.Type.LEFT -> {
                val run: HorizontalWidgetRun = targetWidget.mHorizontalRun!!
                target = run.start
            }

            ConstraintAnchor.Type.RIGHT -> {
                val run: HorizontalWidgetRun = targetWidget.mHorizontalRun!!
                target = run.end
            }

            ConstraintAnchor.Type.TOP -> {
                val run: VerticalWidgetRun = targetWidget.mVerticalRun!!
                target = run.start
            }

            ConstraintAnchor.Type.BASELINE -> {
                val run: VerticalWidgetRun = targetWidget.mVerticalRun!!
                target = run.baseline
            }

            ConstraintAnchor.Type.BOTTOM -> {
                val run: VerticalWidgetRun = targetWidget.mVerticalRun!!
                target = run.end
            }

            else -> {}
        }
        return target
    }

    protected open fun updateRunCenter(
        dependency: Dependency?,
        startAnchor: ConstraintAnchor,
        endAnchor: ConstraintAnchor,
        orientation: Int,
    ) {
        val startTarget = getTarget(startAnchor)
        val endTarget = getTarget(endAnchor)
        if (!(startTarget!!.resolved && endTarget!!.resolved)) {
            return
        }
        var startPos = startTarget.value + startAnchor.margin
        var endPos = endTarget.value - endAnchor.margin
        val distance = endPos - startPos
        if (!mDimension.resolved &&
            mDimensionBehavior == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT
        ) {
            resolveDimension(orientation, distance)
        }
        if (!mDimension.resolved) {
            return
        }
        if (mDimension.value == distance) {
            start.resolve(startPos)
            end.resolve(endPos)
            return
        }

        // Otherwise, we have to center
        var bias =
            if (orientation == HORIZONTAL) mWidget!!.horizontalBiasPercent else mWidget!!.verticalBiasPercent
        if (startTarget == endTarget) {
            startPos = startTarget.value
            endPos = endTarget.value
            // TODO: taking advantage of bias here would be a nice feature to support,
            // but for now let's stay compatible with 1.1
            bias = 0.5f
        }
        val availableDistance = endPos - startPos - mDimension.value
        start.resolve((0.5f + startPos + availableDistance * bias).toInt())
        end.resolve(start.value + mDimension.value)
    }

    private fun resolveDimension(orientation: Int, distance: Int) {
        when (matchConstraintsType) {
            MATCH_CONSTRAINT_SPREAD -> {
                mDimension.resolve(getLimitedDimension(distance, orientation))
            }

            MATCH_CONSTRAINT_PERCENT -> {
                val parent = mWidget!!.parent
                if (parent != null) {
                    val run: WidgetRun =
                        if (orientation == HORIZONTAL) parent.mHorizontalRun!! else parent.mVerticalRun!!
                    if (run.mDimension.resolved) {
                        val percent =
                            if (orientation == HORIZONTAL) mWidget!!.mMatchConstraintPercentWidth else mWidget!!.mMatchConstraintPercentHeight
                        val targetDimensionValue = run.mDimension.value
                        val size = (0.5f + targetDimensionValue * percent).toInt()
                        mDimension.resolve(getLimitedDimension(size, orientation))
                    }
                }
            }

            MATCH_CONSTRAINT_WRAP -> {
                val wrapValue: Int = getLimitedDimension(mDimension.wrapValue, orientation)
                mDimension.resolve(min(wrapValue, distance))
            }

            MATCH_CONSTRAINT_RATIO -> {
                if (mWidget!!.mHorizontalRun!!.mDimensionBehavior
                    == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT &&
                    mWidget!!.mHorizontalRun!!.matchConstraintsType == MATCH_CONSTRAINT_RATIO &&
                    mWidget!!.mVerticalRun!!.mDimensionBehavior == ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT &&
                    mWidget!!.mVerticalRun!!.matchConstraintsType == MATCH_CONSTRAINT_RATIO
                ) {
                    // pof
                } else {
                    val run: WidgetRun =
                        if (orientation == HORIZONTAL) mWidget!!.mVerticalRun!! else mWidget!!.mHorizontalRun!!
                    if (run.mDimension.resolved) {
                        val ratio = mWidget!!.getDimensionRatio()
                        val value: Int = if (orientation == VERTICAL) {
                            (0.5f + run.mDimension.value / ratio).toInt()
                        } else {
                            (0.5f + ratio * run.mDimension.value).toInt()
                        }
                        mDimension.resolve(value)
                    }
                }
            }

            else -> {}
        }
    }

    protected open fun updateRunStart(dependency: Dependency?) {}

    protected open fun updateRunEnd(dependency: Dependency?) {}

    // @TODO: add description
    override fun update(node: Dependency) {}

    protected fun getLimitedDimension(dimension: Int, orientation: Int): Int {
        var dimension = dimension
        if (orientation == HORIZONTAL) {
            val max = mWidget!!.mMatchConstraintMaxWidth
            val min = mWidget!!.mMatchConstraintMinWidth
            var value = max(min, dimension)
            if (max > 0) {
                value = min(max, dimension)
            }
            if (value != dimension) {
                dimension = value
            }
        } else {
            val max = mWidget!!.mMatchConstraintMaxHeight
            val min = mWidget!!.mMatchConstraintMinHeight
            var value = max(min, dimension)
            if (max > 0) {
                value = min(max, dimension)
            }
            if (value != dimension) {
                dimension = value
            }
        }
        return dimension
    }

    protected fun getTarget(anchor: ConstraintAnchor, orientation: Int): DependencyNode? {
        if (anchor.mTarget == null) {
            return null
        }
        var target: DependencyNode? = null
        val targetWidget = anchor.mTarget!!.mOwner
        val run: WidgetRun =
            if (orientation == HORIZONTAL) targetWidget.mHorizontalRun!! else targetWidget.mVerticalRun!!
        val targetType = anchor.mTarget!!.mType
        when (targetType) {
            ConstraintAnchor.Type.TOP, ConstraintAnchor.Type.LEFT -> {
                target = run.start
            }

            ConstraintAnchor.Type.BOTTOM, ConstraintAnchor.Type.RIGHT -> {
                target = run.end
            }

            else -> {}
        }
        return target
    }

    protected fun addTarget(
        node: DependencyNode,
        target: DependencyNode,
        margin: Int,
    ) {
        node.mTargets.add(target)
        node.mMargin = margin
        target.mDependencies.add(node)
    }

    protected fun addTarget(
        node: DependencyNode,
        target: DependencyNode,
        marginFactor: Int,
        dimensionDependency: DimensionDependency,
    ) {
        node.mTargets.add(target)
        node.mTargets.add(mDimension)
        node.mMarginFactor = marginFactor
        node.mMarginDependency = dimensionDependency
        target.mDependencies.add(node)
        dimensionDependency.mDependencies.add(node)
    }

    // @TODO: add description
    open fun getWrapDimension(): Long {
        return if (mDimension.resolved) {
            mDimension.value.toLong()
        } else {
            0
        }
    }

    open fun isResolved(): Boolean {
        return mResolved
    }

    internal enum class RunType {
        NONE,
        START,
        END,
        CENTER,
    }
}
