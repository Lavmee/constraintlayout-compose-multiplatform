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
package androidx.constraintlayout.coremp.widgets

import androidx.constraintlayout.coremp.widgets.analyzer.BasicMeasure

open class VirtualLayout : HelperWidget() {

    private var mPaddingTop = 0
    private var mPaddingBottom = 0

    @Suppress("unused")
    private var mPaddingLeft = 0

    @Suppress("unused")
    private var mPaddingRight = 0

    @Suppress("unused")
    private var mPaddingStart = 0
    private var mPaddingEnd = 0
    private var mResolvedPaddingLeft = 0
    private var mResolvedPaddingRight = 0

    private var mNeedsCallFromSolver = false
    private var mMeasuredWidth = 0
    private var mMeasuredHeight = 0

    protected var mMeasure: BasicMeasure.Measure = BasicMeasure.Measure()

    // ///////////////////////////////////////////////////////////////////////////////////////////
    // Accessors
    // ///////////////////////////////////////////////////////////////////////////////////////////

    // ///////////////////////////////////////////////////////////////////////////////////////////
    // Accessors
    // ///////////////////////////////////////////////////////////////////////////////////////////
    // @TODO: add description
    fun setPadding(value: Int) {
        mPaddingLeft = value
        mPaddingTop = value
        mPaddingRight = value
        mPaddingBottom = value
        mPaddingStart = value
        mPaddingEnd = value
    }

    // @TODO: add description
    fun setPaddingStart(value: Int) {
        mPaddingStart = value
        mResolvedPaddingLeft = value
        mResolvedPaddingRight = value
    }

    fun setPaddingEnd(value: Int) {
        mPaddingEnd = value
    }

    // @TODO: add description
    fun setPaddingLeft(value: Int) {
        mPaddingLeft = value
        mResolvedPaddingLeft = value
    }

    // @TODO: add description
    fun applyRtl(isRtl: Boolean) {
        if (mPaddingStart > 0 || mPaddingEnd > 0) {
            if (isRtl) {
                mResolvedPaddingLeft = mPaddingEnd
                mResolvedPaddingRight = mPaddingStart
            } else {
                mResolvedPaddingLeft = mPaddingStart
                mResolvedPaddingRight = mPaddingEnd
            }
        }
    }

    fun setPaddingTop(value: Int) {
        mPaddingTop = value
    }

    // @TODO: add description
    fun setPaddingRight(value: Int) {
        mPaddingRight = value
        mResolvedPaddingRight = value
    }

    fun setPaddingBottom(value: Int) {
        mPaddingBottom = value
    }

    fun getPaddingTop(): Int {
        return mPaddingTop
    }

    fun getPaddingBottom(): Int {
        return mPaddingBottom
    }

    fun getPaddingLeft(): Int {
        return mResolvedPaddingLeft
    }

    fun getPaddingRight(): Int {
        return mResolvedPaddingRight
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////
    // Solver callback
    // ///////////////////////////////////////////////////////////////////////////////////////////

    // ///////////////////////////////////////////////////////////////////////////////////////////
    // Solver callback
    // ///////////////////////////////////////////////////////////////////////////////////////////
    protected fun needsCallbackFromSolver(value: Boolean) {
        mNeedsCallFromSolver = value
    }

    // @TODO: add description
    fun needSolverPass(): Boolean {
        return mNeedsCallFromSolver
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////
    // Measure
    // ///////////////////////////////////////////////////////////////////////////////////////////

    // ///////////////////////////////////////////////////////////////////////////////////////////
    // Measure
    // ///////////////////////////////////////////////////////////////////////////////////////////
    // @TODO: add description
    open fun measure(widthMode: Int, widthSize: Int, heightMode: Int, heightSize: Int) {
        // nothing
    }

    override fun updateConstraints(container: ConstraintWidgetContainer?) {
        captureWidgets()
    }

    // @TODO: add description
    fun captureWidgets() {
        for (i in 0 until mWidgetsCount) {
            val widget: ConstraintWidget? = mWidgets[i]
            if (widget != null) {
                widget.isInVirtualLayout = true
            }
        }
    }

    val measuredWidth: Int get() = mMeasuredWidth
    val measuredHeight: Int get() = mMeasuredHeight

    // @TODO: add description
    fun setMeasure(width: Int, height: Int) {
        mMeasuredWidth = width
        mMeasuredHeight = height
    }

    protected fun measureChildren(): Boolean {
        var measurer: BasicMeasure.Measurer? = null
        if (parent != null) {
            measurer = (parent as ConstraintWidgetContainer).measurer
        }
        if (measurer == null) {
            return false
        }
        for (i in 0 until mWidgetsCount) {
            val widget: ConstraintWidget = mWidgets[i] ?: continue
            if (widget is Guideline) {
                continue
            }
            var widthBehavior = widget.getDimensionBehaviour(HORIZONTAL)
            var heightBehavior = widget.getDimensionBehaviour(VERTICAL)
            val skip =
                widthBehavior == DimensionBehaviour.MATCH_CONSTRAINT && widget.mMatchConstraintDefaultWidth != MATCH_CONSTRAINT_WRAP && heightBehavior == DimensionBehaviour.MATCH_CONSTRAINT && widget.mMatchConstraintDefaultHeight != MATCH_CONSTRAINT_WRAP
            if (skip) {
                // we don't need to measure here as the dimension of the widget
                // will be completely computed by the solver.
                continue
            }
            if (widthBehavior == DimensionBehaviour.MATCH_CONSTRAINT) {
                widthBehavior = DimensionBehaviour.WRAP_CONTENT
            }
            if (heightBehavior == DimensionBehaviour.MATCH_CONSTRAINT) {
                heightBehavior = DimensionBehaviour.WRAP_CONTENT
            }
            mMeasure.horizontalBehavior = widthBehavior!!
            mMeasure.verticalBehavior = heightBehavior!!
            mMeasure.horizontalDimension = widget.width
            mMeasure.verticalDimension = widget.height
            measurer.measure(widget, mMeasure)
            widget.width = mMeasure.measuredWidth
            widget.height = mMeasure.measuredHeight
            widget.baselineDistance = mMeasure.measuredBaseline
        }
        return true
    }

    var mMeasurer: BasicMeasure.Measurer? = null

    protected fun measure(
        widget: ConstraintWidget,
        horizontalBehavior: DimensionBehaviour,
        horizontalDimension: Int,
        verticalBehavior: DimensionBehaviour,
        verticalDimension: Int,
    ) {
        while (mMeasurer == null && parent != null) {
            val parent = parent as ConstraintWidgetContainer
            mMeasurer = parent.measurer
        }
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

    // @TODO: add description
    operator fun contains(widgets: HashSet<ConstraintWidget>): Boolean {
        for (i in 0 until mWidgetsCount) {
            val widget: ConstraintWidget? = mWidgets[i]
            if (widgets.contains(widget)) {
                return true
            }
        }
        return false
    }
}
