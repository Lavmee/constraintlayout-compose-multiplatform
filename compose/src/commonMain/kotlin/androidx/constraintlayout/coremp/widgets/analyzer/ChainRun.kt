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

import androidx.constraintlayout.coremp.widgets.ConstraintWidget
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.GONE
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.HORIZONTAL
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.MATCH_CONSTRAINT_WRAP
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.VERTICAL
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT
import androidx.constraintlayout.coremp.widgets.ConstraintWidgetContainer
import kotlin.math.max
import kotlin.math.min

class ChainRun(widget: ConstraintWidget, orientation: Int) : WidgetRun(widget) {

    var mWidgets = ArrayList<WidgetRun>()
    private var mChainStyle = 0

    init {
        this.orientation = orientation
        build()
    }

    override fun toString(): String {
        val log = StringBuilder("ChainRun ")
        log.append(if (orientation == HORIZONTAL) "horizontal : " else "vertical : ")
        for (run in mWidgets) {
            log.append("<")
            log.append(run)
            log.append("> ")
        }
        return log.toString()
    }

    override fun supportsWrapComputation(): Boolean {
        val count = mWidgets.size
        for (i in 0 until count) {
            val run = mWidgets[i]
            if (!run.supportsWrapComputation()) {
                return false
            }
        }
        return true
    }

    // @TODO: add description
    override fun getWrapDimension(): Long {
        val count = mWidgets.size
        var wrapDimension: Long = 0
        for (i in 0 until count) {
            val run = mWidgets[i]
            wrapDimension += run.start.mMargin.toLong()
            wrapDimension += run.getWrapDimension()
            wrapDimension += run.end.mMargin.toLong()
        }
        return wrapDimension
    }

    private fun build() {
        var current = mWidget!!
        var previous = current.getPreviousChainMember(orientation)
        while (previous != null) {
            current = previous
            previous = current.getPreviousChainMember(orientation)
        }
        mWidget = current // first element of the chain
        mWidgets.add(current.getRun(orientation)!!)
        var next = current.getNextChainMember(orientation)
        while (next != null) {
            current = next
            mWidgets.add(current.getRun(orientation)!!)
            next = current.getNextChainMember(orientation)
        }
        for (run in mWidgets) {
            if (orientation == HORIZONTAL) {
                run.mWidget!!.horizontalChainRun = this
            } else if (orientation == VERTICAL) {
                run.mWidget!!.verticalChainRun = this
            }
        }
        val isInRtl = (
            orientation == HORIZONTAL &&
                (mWidget!!.parent as ConstraintWidgetContainer?)!!.isRtl()
            )
        if (isInRtl && mWidgets.size > 1) {
            mWidget = mWidgets[mWidgets.size - 1].mWidget
        }
        mChainStyle =
            if (orientation == HORIZONTAL) mWidget!!.getHorizontalChainStyle() else mWidget!!.getVerticalChainStyle()
    }

    override fun clear() {
        mRunGroup = null
        for (run in mWidgets) {
            run.clear()
        }
    }

    override fun reset() {
        start.resolved = false
        end.resolved = false
    }

    override fun update(node: Dependency) {
        if (!(start.resolved && end.resolved)) {
            return
        }
        val parent = mWidget!!.parent
        var isInRtl = false
        if (parent is ConstraintWidgetContainer) {
            isInRtl = parent.isRtl()
        }
        val distance = end.value - start.value
        var size = 0
        var numMatchConstraints = 0
        var weights = 0f
        var numVisibleWidgets = 0
        val count = mWidgets.size
        // let's find the first visible widget...
        var firstVisibleWidget = -1
        for (i in 0 until count) {
            val run = mWidgets[i]
            if (run.mWidget!!.visibility == GONE) {
                continue
            }
            firstVisibleWidget = i
            break
        }
        // now the last visible widget...
        var lastVisibleWidget = -1
        for (i in count - 1 downTo 0) {
            val run = mWidgets[i]
            if (run.mWidget!!.visibility == GONE) {
                continue
            }
            lastVisibleWidget = i
            break
        }
        for (j in 0..1) {
            for (i in 0 until count) {
                val run = mWidgets[i]
                if (run.mWidget!!.visibility == GONE) {
                    continue
                }
                numVisibleWidgets++
                if (i > 0 && i >= firstVisibleWidget) {
                    size += run.start.mMargin
                }
                var dimension = run.mDimension.value
                var treatAsFixed = run.mDimensionBehavior != MATCH_CONSTRAINT
                if (treatAsFixed) {
                    if (orientation == HORIZONTAL &&
                        !run.mWidget!!.mHorizontalRun!!.mDimension.resolved
                    ) {
                        return
                    }
                    if (orientation == VERTICAL && !run.mWidget!!.mVerticalRun!!.mDimension.resolved) {
                        return
                    }
                } else if (run.matchConstraintsType == MATCH_CONSTRAINT_WRAP && j == 0) {
                    treatAsFixed = true
                    dimension = run.mDimension.wrapValue
                    numMatchConstraints++
                } else if (run.mDimension.resolved) {
                    treatAsFixed = true
                }
                if (!treatAsFixed) { // only for the first pass
                    numMatchConstraints++
                    val weight = run.mWidget!!.mWeight[orientation]
                    if (weight >= 0) {
                        weights += weight
                    }
                } else {
                    size += dimension
                }
                if (i < count - 1 && i < lastVisibleWidget) {
                    size += -run.end.mMargin
                }
            }
            if (size < distance || numMatchConstraints == 0) {
                break // we are good to go!
            }
            // otherwise, let's do another pass with using match_constraints
            numVisibleWidgets = 0
            numMatchConstraints = 0
            size = 0
            weights = 0f
        }
        var position = start.value
        if (isInRtl) {
            position = end.value
        }
        if (size > distance) {
            if (isInRtl) {
                position += (0.5f + (size - distance) / 2f).toInt()
            } else {
                position -= (0.5f + (size - distance) / 2f).toInt()
            }
        }
        val matchConstraintsDimension: Int
        if (numMatchConstraints > 0) {
            matchConstraintsDimension =
                (0.5f + (distance - size) / numMatchConstraints.toFloat()).toInt()
            var appliedLimits = 0
            for (i in 0 until count) {
                val run = mWidgets[i]
                if (run.mWidget!!.visibility == GONE) {
                    continue
                }
                if (run.mDimensionBehavior == MATCH_CONSTRAINT && !run.mDimension.resolved) {
                    var dimension = matchConstraintsDimension
                    if (weights > 0) {
                        val weight = run.mWidget!!.mWeight[orientation]
                        dimension = (0.5f + weight * (distance - size) / weights).toInt()
                    }
                    var max: Int
                    var min: Int
                    var value = dimension
                    if (orientation == HORIZONTAL) {
                        max = run.mWidget!!.mMatchConstraintMaxWidth
                        min = run.mWidget!!.mMatchConstraintMinWidth
                    } else {
                        max = run.mWidget!!.mMatchConstraintMaxHeight
                        min = run.mWidget!!.mMatchConstraintMinHeight
                    }
                    if (run.matchConstraintsType == MATCH_CONSTRAINT_WRAP) {
                        value = min(value, run.mDimension.wrapValue)
                    }
                    value = max(min, value)
                    if (max > 0) {
                        value = min(max, value)
                    }
                    if (value != dimension) {
                        appliedLimits++
                        dimension = value
                    }
                    run.mDimension.resolve(dimension)
                }
            }
            if (appliedLimits > 0) {
                numMatchConstraints -= appliedLimits
                // we have to recompute the sizes
                size = 0
                for (i in 0 until count) {
                    val run = mWidgets[i]
                    if (run.mWidget!!.visibility == GONE) {
                        continue
                    }
                    if (i > 0 && i >= firstVisibleWidget) {
                        size += run.start.mMargin
                    }
                    size += run.mDimension.value
                    if (i < count - 1 && i < lastVisibleWidget) {
                        size += -run.end.mMargin
                    }
                }
            }
            if (mChainStyle == ConstraintWidget.CHAIN_PACKED && appliedLimits == 0) {
                mChainStyle = ConstraintWidget.CHAIN_SPREAD
            }
        }
        if (size > distance) {
            mChainStyle = ConstraintWidget.CHAIN_PACKED
        }
        if (numVisibleWidgets > 0 && numMatchConstraints == 0 && firstVisibleWidget == lastVisibleWidget) {
            // only one widget of fixed size to display...
            mChainStyle = ConstraintWidget.CHAIN_PACKED
        }
        if (mChainStyle == ConstraintWidget.CHAIN_SPREAD_INSIDE) {
            var gap = 0
            if (numVisibleWidgets > 1) {
                gap = (distance - size) / (numVisibleWidgets - 1)
            } else if (numVisibleWidgets == 1) {
                gap = (distance - size) / 2
            }
            if (numMatchConstraints > 0) {
                gap = 0
            }
            for (i in 0 until count) {
                var index = i
                if (isInRtl) {
                    index = count - (i + 1)
                }
                val run = mWidgets[index]
                if (run.mWidget!!.visibility == GONE) {
                    run.start.resolve(position)
                    run.end.resolve(position)
                    continue
                }
                if (i > 0) {
                    if (isInRtl) {
                        position -= gap
                    } else {
                        position += gap
                    }
                }
                if (i > 0 && i >= firstVisibleWidget) {
                    if (isInRtl) {
                        position -= run.start.mMargin
                    } else {
                        position += run.start.mMargin
                    }
                }
                if (isInRtl) {
                    run.end.resolve(position)
                } else {
                    run.start.resolve(position)
                }
                var dimension = run.mDimension.value
                if (run.mDimensionBehavior == MATCH_CONSTRAINT &&
                    run.matchConstraintsType == MATCH_CONSTRAINT_WRAP
                ) {
                    dimension = run.mDimension.wrapValue
                }
                if (isInRtl) {
                    position -= dimension
                } else {
                    position += dimension
                }
                if (isInRtl) {
                    run.start.resolve(position)
                } else {
                    run.end.resolve(position)
                }
                run.mResolved = true
                if (i < count - 1 && i < lastVisibleWidget) {
                    if (isInRtl) {
                        position -= -run.end.mMargin
                    } else {
                        position += -run.end.mMargin
                    }
                }
            }
        } else if (mChainStyle == ConstraintWidget.CHAIN_SPREAD) {
            var gap = (distance - size) / (numVisibleWidgets + 1)
            if (numMatchConstraints > 0) {
                gap = 0
            }
            for (i in 0 until count) {
                var index = i
                if (isInRtl) {
                    index = count - (i + 1)
                }
                val run = mWidgets[index]
                if (run.mWidget!!.visibility == GONE) {
                    run.start.resolve(position)
                    run.end.resolve(position)
                    continue
                }
                if (isInRtl) {
                    position -= gap
                } else {
                    position += gap
                }
                if (i > 0 && i >= firstVisibleWidget) {
                    if (isInRtl) {
                        position -= run.start.mMargin
                    } else {
                        position += run.start.mMargin
                    }
                }
                if (isInRtl) {
                    run.end.resolve(position)
                } else {
                    run.start.resolve(position)
                }
                var dimension = run.mDimension.value
                if (run.mDimensionBehavior == MATCH_CONSTRAINT &&
                    run.matchConstraintsType == MATCH_CONSTRAINT_WRAP
                ) {
                    dimension = min(dimension, run.mDimension.wrapValue)
                }
                if (isInRtl) {
                    position -= dimension
                } else {
                    position += dimension
                }
                if (isInRtl) {
                    run.start.resolve(position)
                } else {
                    run.end.resolve(position)
                }
                if (i < count - 1 && i < lastVisibleWidget) {
                    if (isInRtl) {
                        position -= -run.end.mMargin
                    } else {
                        position += -run.end.mMargin
                    }
                }
            }
        } else if (mChainStyle == ConstraintWidget.CHAIN_PACKED) {
            var bias =
                if (orientation == HORIZONTAL) mWidget!!.horizontalBiasPercent else mWidget!!.verticalBiasPercent
            if (isInRtl) {
                bias = 1 - bias
            }
            var gap = (0.5f + (distance - size) * bias).toInt()
            if (gap < 0 || numMatchConstraints > 0) {
                gap = 0
            }
            if (isInRtl) {
                position -= gap
            } else {
                position += gap
            }
            for (i in 0 until count) {
                var index = i
                if (isInRtl) {
                    index = count - (i + 1)
                }
                val run = mWidgets[index]
                if (run.mWidget!!.visibility == GONE) {
                    run.start.resolve(position)
                    run.end.resolve(position)
                    continue
                }
                if (i > 0 && i >= firstVisibleWidget) {
                    if (isInRtl) {
                        position -= run.start.mMargin
                    } else {
                        position += run.start.mMargin
                    }
                }
                if (isInRtl) {
                    run.end.resolve(position)
                } else {
                    run.start.resolve(position)
                }
                var dimension = run.mDimension.value
                if (run.mDimensionBehavior == MATCH_CONSTRAINT &&
                    run.matchConstraintsType == MATCH_CONSTRAINT_WRAP
                ) {
                    dimension = run.mDimension.wrapValue
                }
                if (isInRtl) {
                    position -= dimension
                } else {
                    position += dimension
                }
                if (isInRtl) {
                    run.start.resolve(position)
                } else {
                    run.end.resolve(position)
                }
                if (i < count - 1 && i < lastVisibleWidget) {
                    if (isInRtl) {
                        position -= -run.end.mMargin
                    } else {
                        position += -run.end.mMargin
                    }
                }
            }
        }
    }

    // @TODO: add description
    override fun applyToWidget() {
        for (i in mWidgets.indices) {
            val run = mWidgets[i]
            run.applyToWidget()
        }
    }

    private fun getFirstVisibleWidget(): ConstraintWidget? {
        for (i in mWidgets.indices) {
            val run = mWidgets[i]
            if (run.mWidget!!.visibility != GONE) {
                return run.mWidget
            }
        }
        return null
    }

    private fun getLastVisibleWidget(): ConstraintWidget? {
        for (i in mWidgets.indices.reversed()) {
            val run = mWidgets[i]
            if (run.mWidget!!.visibility != GONE) {
                return run.mWidget
            }
        }
        return null
    }

    override fun apply() {
        for (run in mWidgets) {
            run.apply()
        }
        val count = mWidgets.size
        if (count < 1) {
            return
        }

        // get the first and last element of the chain
        val firstWidget = mWidgets[0].mWidget
        val lastWidget = mWidgets[count - 1].mWidget
        if (orientation == HORIZONTAL) {
            val startAnchor = firstWidget!!.mLeft
            val endAnchor = lastWidget!!.mRight
            val startTarget = getTarget(startAnchor, HORIZONTAL)
            var startMargin = startAnchor.margin
            val firstVisibleWidget = getFirstVisibleWidget()
            if (firstVisibleWidget != null) {
                startMargin = firstVisibleWidget.mLeft.margin
            }
            startTarget?.let { addTarget(start, it, startMargin) }
            val endTarget = getTarget(endAnchor, HORIZONTAL)
            var endMargin = endAnchor.margin
            val lastVisibleWidget = getLastVisibleWidget()
            if (lastVisibleWidget != null) {
                endMargin = lastVisibleWidget.mRight.margin
            }
            if (endTarget != null) {
                addTarget(end, endTarget, -endMargin)
            }
        } else {
            val startAnchor = firstWidget!!.mTop
            val endAnchor = lastWidget!!.mBottom
            val startTarget = getTarget(startAnchor, VERTICAL)
            var startMargin = startAnchor.margin
            val firstVisibleWidget = getFirstVisibleWidget()
            if (firstVisibleWidget != null) {
                startMargin = firstVisibleWidget.mTop.margin
            }
            startTarget?.let { addTarget(start, it, startMargin) }
            val endTarget = getTarget(endAnchor, VERTICAL)
            var endMargin = endAnchor.margin
            val lastVisibleWidget = getLastVisibleWidget()
            if (lastVisibleWidget != null) {
                endMargin = lastVisibleWidget.mBottom.margin
            }
            if (endTarget != null) {
                addTarget(end, endTarget, -endMargin)
            }
        }
        start.updateDelegate = this
        end.updateDelegate = this
    }
}
