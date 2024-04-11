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

import androidx.constraintlayout.coremp.LinearSystem
import androidx.constraintlayout.coremp.platform.System
import androidx.constraintlayout.coremp.widgets.Barrier
import androidx.constraintlayout.coremp.widgets.ChainHead
import androidx.constraintlayout.coremp.widgets.ConstraintAnchor
import androidx.constraintlayout.coremp.widgets.ConstraintWidget
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.GONE
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.HORIZONTAL
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.MATCH_CONSTRAINT_WRAP
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.VERTICAL
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.DimensionBehaviour
import androidx.constraintlayout.coremp.widgets.ConstraintWidgetContainer
import androidx.constraintlayout.coremp.widgets.Guideline
import kotlin.math.max
import kotlin.math.min

class Direct {

    companion object {
        private const val DEBUG = LinearSystem.FULL_DEBUG
        private const val APPLY_MATCH_PARENT = false
        private val sMeasure = BasicMeasure.Measure()
        private const val EARLY_TERMINATION = true // feature flag -- remove after release.

        private var sHcount = 0
        private var sVcount = 0

        /**
         * Walk the dependency graph and solves it.
         *
         * @param layout   the container we want to optimize
         * @param measurer the measurer used to measure the widget
         */
        fun solvingPass(
            layout: ConstraintWidgetContainer,
            measurer: BasicMeasure.Measurer?,
        ) {
            val horizontal = layout.horizontalDimensionBehaviour
            val vertical = layout.verticalDimensionBehaviour
            sHcount = 0
            sVcount = 0
            var time: Long = 0
            if (DEBUG) {
                time = System.nanoTime()
//                time = System.nanoTime()
                println(
                    "#### SOLVING PASS (horiz " + horizontal +
                        ", vert " + vertical + ") ####",
                )
            }
            layout.resetFinalResolution()
            val children = layout.children
            val count = children.size
            if (DEBUG) {
                println("#### SOLVING PASS on $count widgeets ####")
            }
            for (i in 0 until count) {
                val child = children[i]
                child.resetFinalResolution()
            }
            val isRtl = layout.isRtl()

            // First, let's solve the horizontal dependencies, as it's a lot more common to have
            // a container with a fixed horizontal dimension (e.g. match_parent) than the opposite.

            // If we know our size, we can fully set the entire dimension, but if not we can
            // still solve what we can starting from the left.
            if (horizontal == DimensionBehaviour.FIXED) {
                layout.setFinalHorizontal(0, layout.width)
            } else {
                layout.setFinalLeft(0)
            }
            if (DEBUG) {
                println("\n### Let's solve horizontal dependencies ###\n")
            }

            // Then let's first try to solve horizontal guidelines,
            // as they only depends on the container
            var hasGuideline = false
            var hasBarrier = false
            for (i in 0 until count) {
                val child = children[i]
                if (child is Guideline) {
                    val guideline = child
                    if (guideline.orientation == Guideline.VERTICAL) {
                        if (guideline.getRelativeBegin() != -1) {
                            guideline.setFinalValue(guideline.getRelativeBegin())
                        } else if ((
                            guideline.getRelativeEnd() != -1 &&
                                layout.isResolvedHorizontally
                            )
                        ) {
                            guideline.setFinalValue(layout.width - guideline.getRelativeEnd())
                        } else if (layout.isResolvedHorizontally) {
                            val position =
                                (0.5f + guideline.getRelativePercent() * layout.width).toInt()
                            guideline.setFinalValue(position)
                        }
                        hasGuideline = true
                    }
                } else if (child is Barrier) {
                    if (child.orientation == HORIZONTAL) {
                        hasBarrier = true
                    }
                }
            }
            if (hasGuideline) {
                if (DEBUG) {
                    println("\n#### VERTICAL GUIDELINES CHECKS ####")
                }
                for (i in 0 until count) {
                    val child = children[i]
                    if (child is Guideline) {
                        val guideline = child
                        if (guideline.orientation == Guideline.VERTICAL) {
                            horizontalSolvingPass(0, guideline, measurer, isRtl)
                        }
                    }
                }
                if (DEBUG) {
                    println("### Done solving guidelines.")
                }
            }
            if (DEBUG) {
                println("\n#### HORIZONTAL SOLVING PASS ####")
            }

            // Now let's resolve what we can in the dependencies of the container
            horizontalSolvingPass(0, layout, measurer, isRtl)

            // Finally, let's go through barriers, as they depends on widgets that may have been solved.
            if (hasBarrier) {
                if (DEBUG) {
                    println("\n#### HORIZONTAL BARRIER CHECKS ####")
                }
                for (i in 0 until count) {
                    val child = children[i]
                    if (child is Barrier) {
                        val barrier = child
                        if (barrier.orientation == HORIZONTAL) {
                            solveBarrier(0, barrier, measurer, HORIZONTAL, isRtl)
                        }
                    }
                }
                if (DEBUG) {
                    println("#### DONE HORIZONTAL BARRIER CHECKS ####")
                }
            }
            if (DEBUG) {
                println("\n### Let's solve vertical dependencies now ###\n")
            }

            // Now we are done with the horizontal axis, let's see what we can do vertically
            if (vertical == DimensionBehaviour.FIXED) {
                layout.setFinalVertical(0, layout.height)
            } else {
                layout.setFinalTop(0)
            }

            // Same thing as above -- let's start with guidelines...
            hasGuideline = false
            hasBarrier = false
            for (i in 0 until count) {
                val child = children[i]
                if (child is Guideline) {
                    val guideline = child
                    if (guideline.orientation == Guideline.HORIZONTAL) {
                        if (guideline.getRelativeBegin() != -1) {
                            guideline.setFinalValue(guideline.getRelativeBegin())
                        } else if (guideline.getRelativeEnd() != -1 && layout.isResolvedVertically) {
                            guideline.setFinalValue(layout.height - guideline.getRelativeEnd())
                        } else if (layout.isResolvedVertically) {
                            val position =
                                (0.5f + guideline.getRelativePercent() * layout.height).toInt()
                            guideline.setFinalValue(position)
                        }
                        hasGuideline = true
                    }
                } else if (child is Barrier) {
                    if (child.orientation == VERTICAL) {
                        hasBarrier = true
                    }
                }
            }
            if (hasGuideline) {
                if (DEBUG) {
                    println("\n#### HORIZONTAL GUIDELINES CHECKS ####")
                }
                for (i in 0 until count) {
                    val child = children[i]
                    if (child is Guideline) {
                        val guideline = child
                        if (guideline.orientation == Guideline.HORIZONTAL) {
                            verticalSolvingPass(1, guideline, measurer)
                        }
                    }
                }
                if (DEBUG) {
                    println("\n### Done solving guidelines.")
                }
            }
            if (DEBUG) {
                println("\n#### VERTICAL SOLVING PASS ####")
            }

            // ...then solve the vertical dependencies...
            verticalSolvingPass(0, layout, measurer)

            // ...then deal with any barriers left.
            if (hasBarrier) {
                if (DEBUG) {
                    println("#### VERTICAL BARRIER CHECKS ####")
                }
                for (i in 0 until count) {
                    val child = children[i]
                    if (child is Barrier) {
                        val barrier = child
                        if (barrier.orientation == VERTICAL) {
                            solveBarrier(0, barrier, measurer, VERTICAL, isRtl)
                        }
                    }
                }
            }
            if (DEBUG) {
                println("\n#### LAST PASS ####")
            }
            // We can do a last pass to see any widget that could still be measured
            for (i in 0 until count) {
                val child = children[i]
                if (child.isMeasureRequested && canMeasure(0, child)) {
                    ConstraintWidgetContainer.measure(
                        0,
                        child,
                        measurer,
                        sMeasure,
                        BasicMeasure.Measure.SELF_DIMENSIONS,
                    )
                    if (child is Guideline) {
                        if (child.orientation == Guideline.HORIZONTAL) {
                            verticalSolvingPass(0, child, measurer)
                        } else {
                            horizontalSolvingPass(0, child, measurer, isRtl)
                        }
                    } else {
                        horizontalSolvingPass(0, child, measurer, isRtl)
                        verticalSolvingPass(0, child, measurer)
                    }
                }
            }
            if (DEBUG) {
                time = System.nanoTime() - time
                // time = System.nanoTime() - time
                println("\n*** THROUGH WITH DIRECT PASS in $time ns ***\n")
                println("hcount: $sHcount vcount: $sVcount")
            }
        }

        /**
         * Ask the barrier if it's resolved, and if so do a solving pass
         */
        private fun solveBarrier(
            level: Int,
            barrier: Barrier,
            measurer: BasicMeasure.Measurer?,
            orientation: Int,
            isRtl: Boolean,
        ) {
            if (barrier.allSolved()) {
                if (orientation == HORIZONTAL) {
                    horizontalSolvingPass(level + 1, barrier, measurer, isRtl)
                } else {
                    verticalSolvingPass(level + 1, barrier, measurer)
                }
            }
        }

        /**
         * Small utility function to indent logs depending on the level
         *
         * @return a formatted string for the indentation
         */
        fun ls(level: Int): String {
            val builder = StringBuilder()
            for (i in 0 until level) {
                builder.append("  ")
            }
            builder.append("+-($level) ")
            return builder.toString()
        }

        /**
         * Does an horizontal solving pass for the given widget. This will walk through the widget's
         * horizontal dependencies and if they can be resolved directly, do so.
         *
         * @param layout   the widget we want to solve the dependencies
         * @param measurer the measurer object to measure the widgets.
         */
        private fun horizontalSolvingPass(
            level: Int,
            layout: ConstraintWidget,
            measurer: BasicMeasure.Measurer?,
            isRtl: Boolean,
        ) {
            if (EARLY_TERMINATION && layout.isHorizontalSolvingPassDone()) {
                if (DEBUG) {
                    println(
                        ls(level) + "HORIZONTAL SOLVING PASS ON " +
                            layout.debugName + " ALREADY CALLED",
                    )
                }
                return
            }
            sHcount++
            if (DEBUG) {
                println(ls(level) + "HORIZONTAL SOLVING PASS ON " + layout.debugName)
            }
            if ((
                layout !is ConstraintWidgetContainer && layout.isMeasureRequested &&
                    canMeasure(level + 1, layout)
                )
            ) {
                val measure = BasicMeasure.Measure()
                ConstraintWidgetContainer.measure(
                    level + 1,
                    layout,
                    measurer,
                    measure,
                    BasicMeasure.Measure.SELF_DIMENSIONS,
                )
            }
            val left = layout.getAnchor(ConstraintAnchor.Type.LEFT)
            val right = layout.getAnchor(ConstraintAnchor.Type.RIGHT)
            val l = left!!.getFinalValue()
            val r = right!!.getFinalValue()
            if (left.getDependents() != null && left.hasFinalValue()) {
                for (first: ConstraintAnchor in left.getDependents()!!) {
                    val widget = first.mOwner
                    var x1: Int
                    var x2: Int
                    val canMeasure: Boolean = canMeasure(level + 1, widget)
                    if (widget.isMeasureRequested && canMeasure) {
                        val measure = BasicMeasure.Measure()
                        ConstraintWidgetContainer.measure(
                            level + 1,
                            widget,
                            measurer,
                            measure,
                            BasicMeasure.Measure.SELF_DIMENSIONS,
                        )
                    }
                    val bothConnected = (
                        (
                            (first == widget.mLeft) && (
                                widget.mRight.mTarget != null
                                ) && widget.mRight.mTarget!!.hasFinalValue()
                            ) ||
                            (
                                (first == widget.mRight) && (
                                    widget.mLeft.mTarget != null
                                    ) && widget.mLeft.mTarget!!.hasFinalValue()
                                )
                        )
                    if (widget.horizontalDimensionBehaviour
                        != DimensionBehaviour.MATCH_CONSTRAINT || canMeasure
                    ) {
                        if (widget.isMeasureRequested) {
                            // Widget needs to be measured
                            if (DEBUG) {
                                println(
                                    (
                                        ls(level + 1) + "(L) We didn't measure " +
                                            widget.debugName + ", let's bail"
                                        ),
                                )
                            }
                            continue
                        }
                        if (first == widget.mLeft && widget.mRight.mTarget == null) {
                            x1 = l + widget.mLeft.margin
                            x2 = x1 + widget.width
                            widget.setFinalHorizontal(x1, x2)
                            horizontalSolvingPass(level + 1, widget, measurer, isRtl)
                        } else if (first == widget.mRight && widget.mLeft.mTarget == null) {
                            x2 = l - widget.mRight.margin
                            x1 = x2 - widget.width
                            widget.setFinalHorizontal(x1, x2)
                            horizontalSolvingPass(level + 1, widget, measurer, isRtl)
                        } else if (bothConnected && !widget.isInHorizontalChain()) {
                            solveHorizontalCenterConstraints(level + 1, measurer, widget, isRtl)
                        } else if (APPLY_MATCH_PARENT && widget.horizontalDimensionBehaviour
                            == DimensionBehaviour.MATCH_PARENT
                        ) {
                            widget.setFinalHorizontal(0, widget.width)
                            horizontalSolvingPass(level + 1, widget, measurer, isRtl)
                        }
                    } else if ((
                        (
                            (
                                widget.horizontalDimensionBehaviour
                                    == DimensionBehaviour.MATCH_CONSTRAINT
                                )
                            ) && (
                            widget.mMatchConstraintMaxWidth >= 0
                            ) && (
                            widget.mMatchConstraintMinWidth >= 0
                            ) && (
                            (
                                widget.visibility == GONE ||
                                    (
                                        (
                                            widget.mMatchConstraintDefaultWidth
                                                == ConstraintWidget.MATCH_CONSTRAINT_SPREAD
                                            ) &&
                                            widget.getDimensionRatio() == 0f
                                        )
                                )
                            ) &&
                            !widget.isInHorizontalChain() && !widget.isInVirtualLayout
                        )
                    ) {
                        if (bothConnected && !widget.isInHorizontalChain()) {
                            solveHorizontalMatchConstraint(
                                level + 1,
                                layout,
                                measurer,
                                widget,
                                isRtl,
                            )
                        }
                    }
                }
            }
            if (layout is Guideline) {
                return
            }
            if (right.getDependents() != null && right.hasFinalValue()) {
                for (first: ConstraintAnchor in right.getDependents()!!) {
                    val widget = first.mOwner
                    val canMeasure: Boolean = canMeasure(level + 1, widget)
                    if (widget.isMeasureRequested && canMeasure) {
                        val measure = BasicMeasure.Measure()
                        ConstraintWidgetContainer.measure(
                            level + 1,
                            widget,
                            measurer,
                            measure,
                            BasicMeasure.Measure.SELF_DIMENSIONS,
                        )
                    }
                    var x1: Int
                    var x2: Int
                    val bothConnected = (
                        (
                            (first == widget.mLeft) && (
                                widget.mRight.mTarget != null
                                ) && widget.mRight.mTarget!!.hasFinalValue()
                            ) ||
                            (
                                (first == widget.mRight) && (
                                    widget.mLeft.mTarget != null
                                    ) && widget.mLeft.mTarget!!.hasFinalValue()
                                )
                        )
                    if (widget.horizontalDimensionBehaviour
                        != DimensionBehaviour.MATCH_CONSTRAINT || canMeasure
                    ) {
                        if (widget.isMeasureRequested) {
                            // Widget needs to be measured
                            if (DEBUG) {
                                println(
                                    (
                                        ls(level + 1) + "(R) We didn't measure " +
                                            widget.debugName + ", le'ts bail"
                                        ),
                                )
                            }
                            continue
                        }
                        if (first == widget.mLeft && widget.mRight.mTarget == null) {
                            x1 = r + widget.mLeft.margin
                            x2 = x1 + widget.width
                            widget.setFinalHorizontal(x1, x2)
                            horizontalSolvingPass(level + 1, widget, measurer, isRtl)
                        } else if (first == widget.mRight && widget.mLeft.mTarget == null) {
                            x2 = r - widget.mRight.margin
                            x1 = x2 - widget.width
                            widget.setFinalHorizontal(x1, x2)
                            horizontalSolvingPass(level + 1, widget, measurer, isRtl)
                        } else if (bothConnected && !widget.isInHorizontalChain()) {
                            solveHorizontalCenterConstraints(level + 1, measurer, widget, isRtl)
                        }
                    } else if ((
                        (
                            (
                                widget.horizontalDimensionBehaviour
                                    == DimensionBehaviour.MATCH_CONSTRAINT
                                )
                            ) && (
                            widget.mMatchConstraintMaxWidth >= 0
                            ) && (
                            widget.mMatchConstraintMinWidth >= 0
                            ) && (
                            (
                                widget.visibility == GONE ||
                                    (
                                        (
                                            widget.mMatchConstraintDefaultWidth
                                                == ConstraintWidget.MATCH_CONSTRAINT_SPREAD
                                            ) &&
                                            widget.getDimensionRatio() == 0f
                                        )
                                )
                            ) &&
                            !widget.isInHorizontalChain() && !widget.isInVirtualLayout
                        )
                    ) {
                        if (bothConnected && !widget.isInHorizontalChain()) {
                            solveHorizontalMatchConstraint(
                                level + 1,
                                layout,
                                measurer,
                                widget,
                                isRtl,
                            )
                        }
                    }
                }
            }
            layout.markHorizontalSolvingPassDone()
        }

        /**
         * Does an vertical solving pass for the given widget. This will walk through the widget's
         * vertical dependencies and if they can be resolved directly, do so.
         *
         * @param layout   the widget we want to solve the dependencies
         * @param measurer the measurer object to measure the widgets.
         */
        private fun verticalSolvingPass(
            level: Int,
            layout: ConstraintWidget,
            measurer: BasicMeasure.Measurer?,
        ) {
            if (EARLY_TERMINATION && layout.isVerticalSolvingPassDone()) {
                if (DEBUG) {
                    println(
                        ls(level) + "VERTICAL SOLVING PASS ON " +
                            layout.debugName + " ALREADY CALLED",
                    )
                }
                return
            }
            sVcount++
            if (DEBUG) {
                println(ls(level) + "VERTICAL SOLVING PASS ON " + layout.debugName)
            }
            if ((
                layout !is ConstraintWidgetContainer &&
                    layout.isMeasureRequested && canMeasure(level + 1, layout)
                )
            ) {
                val measure = BasicMeasure.Measure()
                ConstraintWidgetContainer.measure(
                    level + 1,
                    layout,
                    measurer,
                    measure,
                    BasicMeasure.Measure.SELF_DIMENSIONS,
                )
            }
            val top = layout.getAnchor(ConstraintAnchor.Type.TOP)
            val bottom = layout.getAnchor(ConstraintAnchor.Type.BOTTOM)
            val t = top!!.getFinalValue()
            val b = bottom!!.getFinalValue()
            if (top.getDependents() != null && top.hasFinalValue()) {
                for (first: ConstraintAnchor in top.getDependents()!!) {
                    val widget = first.mOwner
                    var y1: Int
                    var y2: Int
                    val canMeasure: Boolean = canMeasure(level + 1, widget)
                    if (widget.isMeasureRequested && canMeasure) {
                        val measure = BasicMeasure.Measure()
                        ConstraintWidgetContainer.measure(
                            level + 1,
                            widget,
                            measurer,
                            measure,
                            BasicMeasure.Measure.SELF_DIMENSIONS,
                        )
                    }
                    val bothConnected = (
                        (
                            (first == widget.mTop) && (
                                widget.mBottom.mTarget != null
                                ) && widget.mBottom.mTarget!!.hasFinalValue()
                            ) ||
                            (
                                (first == widget.mBottom) && (
                                    widget.mTop.mTarget != null
                                    ) && widget.mTop.mTarget!!.hasFinalValue()
                                )
                        )
                    if ((
                        widget.verticalDimensionBehaviour
                            != DimensionBehaviour.MATCH_CONSTRAINT ||
                            canMeasure
                        )
                    ) {
                        if (widget.isMeasureRequested) {
                            // Widget needs to be measured
                            if (DEBUG) {
                                println(
                                    (
                                        ls(level + 1) + "(T) We didn't measure " +
                                            widget.debugName + ", le'ts bail"
                                        ),
                                )
                            }
                            continue
                        }
                        if (first == widget.mTop && widget.mBottom.mTarget == null) {
                            y1 = t + widget.mTop.margin
                            y2 = y1 + widget.height
                            widget.setFinalVertical(y1, y2)
                            verticalSolvingPass(level + 1, widget, measurer)
                        } else if (first == widget.mBottom && widget.mTop.mTarget == null) {
                            y2 = t - widget.mBottom.margin
                            y1 = y2 - widget.height
                            widget.setFinalVertical(y1, y2)
                            verticalSolvingPass(level + 1, widget, measurer)
                        } else if (bothConnected && !widget.isInVerticalChain()) {
                            solveVerticalCenterConstraints(level + 1, measurer, widget)
                        } else if (APPLY_MATCH_PARENT && widget.verticalDimensionBehaviour
                            == DimensionBehaviour.MATCH_PARENT
                        ) {
                            widget.setFinalVertical(0, widget.height)
                            verticalSolvingPass(level + 1, widget, measurer)
                        }
                    } else if ((
                        (
                            (
                                widget.verticalDimensionBehaviour
                                    == DimensionBehaviour.MATCH_CONSTRAINT
                                )
                            ) && (
                            widget.mMatchConstraintMaxHeight >= 0
                            ) && (
                            widget.mMatchConstraintMinHeight >= 0
                            ) && (
                            (
                                widget.visibility == GONE ||
                                    (
                                        (
                                            widget.mMatchConstraintDefaultHeight
                                                == ConstraintWidget.MATCH_CONSTRAINT_SPREAD
                                            ) &&
                                            widget.getDimensionRatio() == 0f
                                        )
                                )
                            ) &&
                            !widget.isInVerticalChain() && !widget.isInVirtualLayout
                        )
                    ) {
                        if (bothConnected && !widget.isInVerticalChain()) {
                            solveVerticalMatchConstraint(level + 1, layout, measurer, widget)
                        }
                    }
                }
            }
            if (layout is Guideline) {
                return
            }
            if (bottom.getDependents() != null && bottom.hasFinalValue()) {
                for (first: ConstraintAnchor in bottom.getDependents()!!) {
                    val widget = first.mOwner
                    val canMeasure: Boolean = canMeasure(level + 1, widget)
                    if (widget.isMeasureRequested && canMeasure) {
                        val measure = BasicMeasure.Measure()
                        ConstraintWidgetContainer.measure(
                            level + 1,
                            widget,
                            measurer,
                            measure,
                            BasicMeasure.Measure.SELF_DIMENSIONS,
                        )
                    }
                    var y1: Int
                    var y2: Int
                    val bothConnected = (
                        (
                            (first == widget.mTop) && (
                                widget.mBottom.mTarget != null
                                ) && widget.mBottom.mTarget!!.hasFinalValue()
                            ) ||
                            (
                                (first == widget.mBottom) && (
                                    widget.mTop.mTarget != null
                                    ) && widget.mTop.mTarget!!.hasFinalValue()
                                )
                        )
                    if (widget.verticalDimensionBehaviour
                        != DimensionBehaviour.MATCH_CONSTRAINT || canMeasure
                    ) {
                        if (widget.isMeasureRequested) {
                            // Widget needs to be measured
                            if (DEBUG) {
                                println(
                                    (
                                        ls(level + 1) + "(B) We didn't measure " +
                                            widget.debugName + ", le'ts bail"
                                        ),
                                )
                            }
                            continue
                        }
                        if (first == widget.mTop && widget.mBottom.mTarget == null) {
                            y1 = b + widget.mTop.margin
                            y2 = y1 + widget.height
                            widget.setFinalVertical(y1, y2)
                            verticalSolvingPass(level + 1, widget, measurer)
                        } else if (first == widget.mBottom && widget.mTop.mTarget == null) {
                            y2 = b - widget.mBottom.margin
                            y1 = y2 - widget.height
                            widget.setFinalVertical(y1, y2)
                            verticalSolvingPass(level + 1, widget, measurer)
                        } else if (bothConnected && !widget.isInVerticalChain()) {
                            solveVerticalCenterConstraints(level + 1, measurer, widget)
                        }
                    } else if ((
                        (
                            (
                                widget.verticalDimensionBehaviour
                                    == DimensionBehaviour.MATCH_CONSTRAINT
                                )
                            ) && (
                            widget.mMatchConstraintMaxHeight >= 0
                            ) && (
                            widget.mMatchConstraintMinHeight >= 0
                            ) && (
                            (
                                widget.visibility == GONE ||
                                    (
                                        (
                                            widget.mMatchConstraintDefaultHeight
                                                == ConstraintWidget.MATCH_CONSTRAINT_SPREAD
                                            ) &&
                                            widget.getDimensionRatio() == 0f
                                        )
                                )
                            ) &&
                            !widget.isInVerticalChain() && !widget.isInVirtualLayout
                        )
                    ) {
                        if (bothConnected && !widget.isInVerticalChain()) {
                            solveVerticalMatchConstraint(level + 1, layout, measurer, widget)
                        }
                    }
                }
            }
            val baseline = layout.getAnchor(ConstraintAnchor.Type.BASELINE)
            if (baseline!!.getDependents() != null && baseline.hasFinalValue()) {
                val baselineValue = baseline.getFinalValue()
                for (first: ConstraintAnchor in baseline.getDependents()!!) {
                    val widget = first.mOwner
                    val canMeasure: Boolean = canMeasure(level + 1, widget)
                    if (widget.isMeasureRequested && canMeasure) {
                        val measure = BasicMeasure.Measure()
                        ConstraintWidgetContainer.measure(
                            level + 1,
                            widget,
                            measurer,
                            measure,
                            BasicMeasure.Measure.SELF_DIMENSIONS,
                        )
                    }
                    if (widget.verticalDimensionBehaviour
                        != DimensionBehaviour.MATCH_CONSTRAINT || canMeasure
                    ) {
                        if (widget.isMeasureRequested) {
                            // Widget needs to be measured
                            if (DEBUG) {
                                println(
                                    (
                                        ls(level + 1) + "(B) We didn't measure " +
                                            widget.debugName + ", le'ts bail"
                                        ),
                                )
                            }
                            continue
                        }
                        if (first == widget.mBaseline) {
                            widget.setFinalBaseline(baselineValue + first.margin)
                            verticalSolvingPass(level + 1, widget, measurer)
                        }
                    }
                }
            }
            layout.markVerticalSolvingPassDone()
        }

        /**
         * Solve horizontal centering constraints
         */
        private fun solveHorizontalCenterConstraints(
            level: Int,
            measurer: BasicMeasure.Measurer?,
            widget: ConstraintWidget,
            isRtl: Boolean,
        ) {
            // TODO: Handle match constraints here or before calling this
            var x1: Int
            var x2: Int
            var bias = widget.horizontalBiasPercent
            val start = widget.mLeft.mTarget!!.getFinalValue()
            val end = widget.mRight.mTarget!!.getFinalValue()
            var s1 = start + widget.mLeft.margin
            var s2 = end - widget.mRight.margin
            if (start == end) {
                bias = 0.5f
                s1 = start
                s2 = end
            }
            val width = widget.width
            var distance = s2 - s1 - width
            if (s1 > s2) {
                distance = s1 - s2 - width
            }
            val d1: Int
            d1 = if (distance > 0) {
                (0.5f + bias * distance).toInt()
            } else {
                (bias * distance).toInt()
            }
            x1 = s1 + d1
            x2 = x1 + width
            if (s1 > s2) {
                x1 = s1 + d1
                x2 = x1 - width
            }
            widget.setFinalHorizontal(x1, x2)
            horizontalSolvingPass(level + 1, widget, measurer, isRtl)
        }

        /**
         * Solve vertical centering constraints
         */
        private fun solveVerticalCenterConstraints(
            level: Int,
            measurer: BasicMeasure.Measurer?,
            widget: ConstraintWidget,
        ) {
            // TODO: Handle match constraints here or before calling this
            var y1: Int
            var y2: Int
            var bias = widget.verticalBiasPercent
            val start = widget.mTop.mTarget!!.getFinalValue()
            val end = widget.mBottom.mTarget!!.getFinalValue()
            var s1 = start + widget.mTop.margin
            var s2 = end - widget.mBottom.margin
            if (start == end) {
                bias = 0.5f
                s1 = start
                s2 = end
            }
            val height = widget.height
            var distance = s2 - s1 - height
            if (s1 > s2) {
                distance = s1 - s2 - height
            }
            val d1: Int
            d1 = if (distance > 0) {
                (0.5f + bias * distance).toInt()
            } else {
                (bias * distance).toInt()
            }
            y1 = s1 + d1
            y2 = y1 + height
            if (s1 > s2) {
                y1 = s1 - d1
                y2 = y1 - height
            }
            widget.setFinalVertical(y1, y2)
            verticalSolvingPass(level + 1, widget, measurer)
        }

        /**
         * Solve horizontal match constraints
         */
        private fun solveHorizontalMatchConstraint(
            level: Int,
            layout: ConstraintWidget,
            measurer: BasicMeasure.Measurer?,
            widget: ConstraintWidget,
            isRtl: Boolean,
        ) {
            val x1: Int
            val x2: Int
            val bias = widget.horizontalBiasPercent
            val s1 = widget.mLeft.mTarget!!.getFinalValue() + widget.mLeft.margin
            val s2 = widget.mRight.mTarget!!.getFinalValue() - widget.mRight.margin
            if (s2 >= s1) {
                var width = widget.width
                if (widget.visibility != GONE) {
                    if (widget.mMatchConstraintDefaultWidth
                        == ConstraintWidget.MATCH_CONSTRAINT_PERCENT
                    ) {
                        val parentWidth: Int = (layout as? ConstraintWidgetContainer)?.width
                            ?: layout.parent!!.width
                        width = (0.5f * widget.horizontalBiasPercent * parentWidth).toInt()
                    } else if (widget.mMatchConstraintDefaultWidth
                        == ConstraintWidget.MATCH_CONSTRAINT_SPREAD
                    ) {
                        width = s2 - s1
                    }
                    width = max(widget.mMatchConstraintMinWidth, width)
                    if (widget.mMatchConstraintMaxWidth > 0) {
                        width = min(widget.mMatchConstraintMaxWidth, width)
                    }
                }
                val distance = s2 - s1 - width
                val d1 = (0.5f + bias * distance).toInt()
                x1 = s1 + d1
                x2 = x1 + width
                widget.setFinalHorizontal(x1, x2)
                horizontalSolvingPass(level + 1, widget, measurer, isRtl)
            }
        }

        /**
         * Solve vertical match constraints
         */
        private fun solveVerticalMatchConstraint(
            level: Int,
            layout: ConstraintWidget,
            measurer: BasicMeasure.Measurer?,
            widget: ConstraintWidget,
        ) {
            val y1: Int
            val y2: Int
            val bias = widget.verticalBiasPercent
            val s1 = widget.mTop.mTarget!!.getFinalValue() + widget.mTop.margin
            val s2 = widget.mBottom.mTarget!!.getFinalValue() - widget.mBottom.margin
            if (s2 >= s1) {
                var height = widget.height
                if (widget.visibility != GONE) {
                    if (widget.mMatchConstraintDefaultHeight
                        == ConstraintWidget.MATCH_CONSTRAINT_PERCENT
                    ) {
                        val parentHeight: Int = (layout as? ConstraintWidgetContainer)?.height
                            ?: layout.parent!!.height
                        height = (0.5f * bias * parentHeight).toInt()
                    } else if (widget.mMatchConstraintDefaultHeight
                        == ConstraintWidget.MATCH_CONSTRAINT_SPREAD
                    ) {
                        height = s2 - s1
                    }
                    height = max(widget.mMatchConstraintMinHeight, height)
                    if (widget.mMatchConstraintMaxHeight > 0) {
                        height = min(widget.mMatchConstraintMaxHeight, height)
                    }
                }
                val distance = s2 - s1 - height
                val d1 = (0.5f + bias * distance).toInt()
                y1 = s1 + d1
                y2 = y1 + height
                widget.setFinalVertical(y1, y2)
                verticalSolvingPass(level + 1, widget, measurer)
            }
        }

        /**
         * Returns true if the dimensions of the given widget are computable directly
         *
         * @param layout the widget to check
         * @return true if both dimensions are knowable by a single measure pass
         */
        private fun canMeasure(level: Int, layout: ConstraintWidget): Boolean {
            val horizontalBehaviour = layout.horizontalDimensionBehaviour
            val verticalBehaviour = layout.verticalDimensionBehaviour
            val parent =
                if (layout.parent != null) layout.parent as ConstraintWidgetContainer? else null
            val isParentHorizontalFixed = parent != null && parent.horizontalDimensionBehaviour == DimensionBehaviour.FIXED
            val isParentVerticalFixed = parent != null && parent.verticalDimensionBehaviour == DimensionBehaviour.FIXED
            val isHorizontalFixed =
                horizontalBehaviour == DimensionBehaviour.FIXED || layout.isResolvedHorizontally || APPLY_MATCH_PARENT && (
                    horizontalBehaviour
                        == DimensionBehaviour.MATCH_PARENT
                    ) && isParentHorizontalFixed || horizontalBehaviour == DimensionBehaviour.WRAP_CONTENT || horizontalBehaviour == DimensionBehaviour.MATCH_CONSTRAINT && (
                    layout.mMatchConstraintDefaultWidth
                        == ConstraintWidget.MATCH_CONSTRAINT_SPREAD
                    ) && layout.mDimensionRatio == 0f && layout.hasDanglingDimension(
                    HORIZONTAL,
                ) || horizontalBehaviour == DimensionBehaviour.MATCH_CONSTRAINT && layout.mMatchConstraintDefaultWidth == MATCH_CONSTRAINT_WRAP && layout.hasResolvedTargets(
                    HORIZONTAL,
                    layout.width,
                )
            val isVerticalFixed =
                verticalBehaviour == DimensionBehaviour.FIXED || layout.isResolvedVertically || APPLY_MATCH_PARENT && (
                    verticalBehaviour
                        == DimensionBehaviour.MATCH_PARENT
                    ) && isParentVerticalFixed || verticalBehaviour == DimensionBehaviour.WRAP_CONTENT || verticalBehaviour == DimensionBehaviour.MATCH_CONSTRAINT && (
                    layout.mMatchConstraintDefaultHeight
                        == ConstraintWidget.MATCH_CONSTRAINT_SPREAD
                    ) && layout.mDimensionRatio == 0f && layout.hasDanglingDimension(
                    VERTICAL,
                ) || verticalBehaviour == DimensionBehaviour.MATCH_CONSTRAINT && layout.mMatchConstraintDefaultHeight == MATCH_CONSTRAINT_WRAP && layout.hasResolvedTargets(
                    VERTICAL,
                    layout.height,
                )
            if (layout.mDimensionRatio > 0 && (isHorizontalFixed || isVerticalFixed)) {
                return true
            }
            if (DEBUG) {
                println(
                    ls(level) + "can measure " + layout.debugName + " ? " +
                        (isHorizontalFixed && isVerticalFixed) + "  [ " +
                        isHorizontalFixed + " (horiz " + horizontalBehaviour + ") & " +
                        isVerticalFixed + " (vert " + verticalBehaviour + ") ]",
                )
            }
            return isHorizontalFixed && isVerticalFixed
        }

        /**
         * Try to directly resolve the chain
         *
         * @return true if fully resolved
         */
        fun solveChain(
            container: ConstraintWidgetContainer,
            system: LinearSystem?,
            orientation: Int,
            offset: Int,
            chainHead: ChainHead,
            isChainSpread: Boolean,
            isChainSpreadInside: Boolean,
            isChainPacked: Boolean,
        ): Boolean {
            if (LinearSystem.FULL_DEBUG) {
                println("\n### SOLVE CHAIN ###")
            }
            if (isChainPacked) {
                return false
            }
            if (orientation == HORIZONTAL) {
                if (!container.isResolvedHorizontally) {
                    return false
                }
            } else {
                if (!container.isResolvedVertically) {
                    return false
                }
            }
            val level = 0 // nested level (used for debugging)
            val isRtl = container.isRtl()
            val first = chainHead.getFirst()
            val last = chainHead.getLast()
            val firstVisibleWidget = chainHead.getFirstVisibleWidget()
            val lastVisibleWidget = chainHead.getLastVisibleWidget()
            val head = chainHead.getHead()
            var widget = first
            var next: ConstraintWidget?
            var done = false
            val begin = first!!.mListAnchors[offset]
            val end = last!!.mListAnchors[offset + 1]
            if (begin.mTarget == null || end.mTarget == null) {
                return false
            }
            if (!begin.mTarget!!.hasFinalValue() || !end.mTarget!!.hasFinalValue()) {
                return false
            }
            if (firstVisibleWidget == null || lastVisibleWidget == null) {
                return false
            }
            val startPoint = (
                begin.mTarget!!.getFinalValue() +
                    firstVisibleWidget.mListAnchors[offset].margin
                )
            val endPoint = (
                end.mTarget!!.getFinalValue() -
                    lastVisibleWidget.mListAnchors[offset + 1].margin
                )
            val distance = endPoint - startPoint
            if (distance <= 0) {
                return false
            }
            var totalSize = 0
            val measure = BasicMeasure.Measure()
            var numWidgets = 0
            var numVisibleWidgets = 0
            while (!done) {
                val canMeasure = canMeasure(level + 1, widget!!)
                if (!canMeasure) {
                    return false
                }
                if (widget.mListDimensionBehaviors[orientation]
                    == DimensionBehaviour.MATCH_CONSTRAINT
                ) {
                    return false
                }
                if (widget.isMeasureRequested) {
                    ConstraintWidgetContainer.measure(
                        level + 1,
                        widget,
                        container.measurer,
                        measure,
                        BasicMeasure.Measure.SELF_DIMENSIONS,
                    )
                }
                totalSize += widget.mListAnchors[offset].margin
                totalSize += if (orientation == HORIZONTAL) {
                    +widget.width
                } else {
                    widget.height
                }
                totalSize += widget.mListAnchors[offset + 1].margin
                numWidgets++
                if (widget.visibility != GONE) {
                    numVisibleWidgets++
                }

                // go to the next widget
                val nextAnchor = widget.mListAnchors[offset + 1].mTarget
                if (nextAnchor != null) {
                    next = nextAnchor.mOwner
                    if (next.mListAnchors[offset].mTarget == null ||
                        next.mListAnchors[offset].mTarget!!.mOwner != widget
                    ) {
                        next = null
                    }
                } else {
                    next = null
                }
                if (next != null) {
                    widget = next
                } else {
                    done = true
                }
            }
            if (numVisibleWidgets == 0) {
                return false
            }
            if (numVisibleWidgets != numWidgets) {
                return false
            }
            if (distance < totalSize) {
                return false
            }
            var gap = distance - totalSize
            if (isChainSpread) {
                gap /= (numVisibleWidgets + 1)
            } else if (isChainSpreadInside) {
                if (numVisibleWidgets > 2) {
                    gap = gap / numVisibleWidgets - 1
                }
            }
            if (numVisibleWidgets == 1) {
                val bias: Float = if (orientation == HORIZONTAL) {
                    head!!.horizontalBiasPercent
                } else {
                    head!!.verticalBiasPercent
                }
                val p1 = (0.5f + startPoint + gap * bias).toInt()
                if (orientation == HORIZONTAL) {
                    firstVisibleWidget.setFinalHorizontal(p1, p1 + firstVisibleWidget.width)
                } else {
                    firstVisibleWidget.setFinalVertical(p1, p1 + firstVisibleWidget.height)
                }
                horizontalSolvingPass(
                    level + 1,
                    firstVisibleWidget,
                    container.measurer!!,
                    isRtl,
                )
                return true
            }
            if (isChainSpread) {
                done = false
                var current = startPoint + gap
                widget = first
                while (!done) {
                    if (widget!!.visibility == GONE) {
                        if (orientation == HORIZONTAL) {
                            widget.setFinalHorizontal(current, current)
                            horizontalSolvingPass(
                                level + 1,
                                widget,
                                container.measurer!!,
                                isRtl,
                            )
                        } else {
                            widget.setFinalVertical(current, current)
                            verticalSolvingPass(level + 1, widget, container.measurer!!)
                        }
                    } else {
                        current += widget.mListAnchors[offset].margin
                        current += if (orientation == HORIZONTAL) {
                            widget.setFinalHorizontal(current, current + widget.width)
                            horizontalSolvingPass(
                                level + 1,
                                widget,
                                container.measurer!!,
                                isRtl,
                            )
                            widget.width
                        } else {
                            widget.setFinalVertical(current, current + widget.height)
                            verticalSolvingPass(level + 1, widget, container.measurer!!)
                            widget.height
                        }
                        current += widget.mListAnchors[offset + 1].margin
                        current += gap
                    }
                    widget.addToSolver(system!!, false)

                    // go to the next widget
                    val nextAnchor = widget.mListAnchors[offset + 1].mTarget
                    if (nextAnchor != null) {
                        next = nextAnchor.mOwner
                        if (next.mListAnchors[offset].mTarget == null ||
                            next.mListAnchors[offset].mTarget!!.mOwner != widget
                        ) {
                            next = null
                        }
                    } else {
                        next = null
                    }
                    if (next != null) {
                        widget = next
                    } else {
                        done = true
                    }
                }
            } else if (isChainSpreadInside) {
                if (numVisibleWidgets == 2) {
                    if (orientation == HORIZONTAL) {
                        firstVisibleWidget.setFinalHorizontal(
                            startPoint,
                            startPoint + firstVisibleWidget.width,
                        )
                        lastVisibleWidget.setFinalHorizontal(
                            endPoint - lastVisibleWidget.width,
                            endPoint,
                        )
                        horizontalSolvingPass(
                            level + 1,
                            firstVisibleWidget,
                            container.measurer!!,
                            isRtl,
                        )
                        horizontalSolvingPass(
                            level + 1,
                            lastVisibleWidget,
                            container.measurer!!,
                            isRtl,
                        )
                    } else {
                        firstVisibleWidget.setFinalVertical(
                            startPoint,
                            startPoint + firstVisibleWidget.height,
                        )
                        lastVisibleWidget.setFinalVertical(
                            endPoint - lastVisibleWidget.height,
                            endPoint,
                        )
                        verticalSolvingPass(
                            level + 1,
                            firstVisibleWidget,
                            container.measurer!!,
                        )
                        verticalSolvingPass(
                            level + 1,
                            lastVisibleWidget,
                            container.measurer!!,
                        )
                    }
                    return true
                }
                return false
            }
            return true
        }
    }
}
