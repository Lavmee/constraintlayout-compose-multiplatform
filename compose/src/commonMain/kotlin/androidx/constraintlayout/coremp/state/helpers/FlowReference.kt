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
package androidx.constraintlayout.coremp.state.helpers

import androidx.constraintlayout.coremp.state.HelperReference
import androidx.constraintlayout.coremp.state.State
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.HORIZONTAL
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.UNKNOWN
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.VERTICAL
import androidx.constraintlayout.coremp.widgets.Flow
import androidx.constraintlayout.coremp.widgets.Flow.Companion.HORIZONTAL_ALIGN_CENTER
import androidx.constraintlayout.coremp.widgets.Flow.Companion.VERTICAL_ALIGN_CENTER
import androidx.constraintlayout.coremp.widgets.Flow.Companion.WRAP_NONE
import androidx.constraintlayout.coremp.widgets.HelperWidget

class FlowReference(state: State, type: State.Helper) : HelperReference(state, type) {
    protected var mFlow: Flow? = null

    protected var mMapWeights: HashMap<String, Float>? = null
    protected var mMapPreMargin: HashMap<String, Float>? = null
    protected var mMapPostMargin: HashMap<String, Float>? = null

    protected var mWrapMode: Int = WRAP_NONE

    protected var mVerticalStyle: Int = UNKNOWN
    protected var mFirstVerticalStyle: Int = UNKNOWN
    protected var mLastVerticalStyle: Int = UNKNOWN
    protected var mHorizontalStyle: Int = UNKNOWN
    protected var mFirstHorizontalStyle: Int = UNKNOWN
    protected var mLastHorizontalStyle: Int = UNKNOWN

    protected var mVerticalAlign: Int = HORIZONTAL_ALIGN_CENTER
    protected var mHorizontalAlign: Int = VERTICAL_ALIGN_CENTER

    protected var mVerticalGap = 0
    protected var mHorizontalGap = 0

    protected var mPaddingLeft = 0
    protected var mPaddingRight = 0
    protected var mPaddingTop = 0
    protected var mPaddingBottom = 0

    protected var mMaxElementsWrap: Int = UNKNOWN

    protected var mOrientation: Int = HORIZONTAL

    protected var mFirstVerticalBias = 0.5f
    protected var mLastVerticalBias = 0.5f
    protected var mFirstHorizontalBias = 0.5f
    protected var mLastHorizontalBias = 0.5f

    init {
        if (type == State.Helper.VERTICAL_FLOW) {
            mOrientation = VERTICAL
        }
    }

    /**
     * Relate widgets to the FlowReference
     *
     * @param id id of a widget
     * @param weight weight of a widget
     * @param preMargin preMargin of a widget
     * @param postMargin postMargin of a widget
     */
    fun addFlowElement(id: String?, weight: Float, preMargin: Float, postMargin: Float) {
        super.add(id!!)
        if (!weight.isNaN()) {
            if (mMapWeights == null) {
                mMapWeights = HashMap()
            }
            mMapWeights!![id] = weight
        }
        if (!preMargin.isNaN()) {
            if (mMapPreMargin == null) {
                mMapPreMargin = HashMap()
            }
            mMapPreMargin!![id] = preMargin
        }
        if (!postMargin.isNaN()) {
            if (mMapPostMargin == null) {
                mMapPostMargin = HashMap()
            }
            mMapPostMargin!![id] = postMargin
        }
    }

    /**
     * Get the weight of a widget
     *
     * @param id id of a widget
     * @return the weight of a widget
     */
    protected fun getWeight(id: String?): Float {
        if (mMapWeights == null) {
            return UNKNOWN.toFloat()
        }
        return if (mMapWeights!!.containsKey(id)) {
            mMapWeights!![id]!!
        } else {
            UNKNOWN.toFloat()
        }
    }

    /**
     * Get the post margin of a widget
     *
     * @param id id id of a widget
     * @return the post margin of a widget
     */
    protected fun getPostMargin(id: String?): Float {
        return if (mMapPreMargin != null && mMapPreMargin!!.containsKey(id)) {
            mMapPreMargin!![id]!!
        } else {
            0f
        }
    }

    /**
     * Get the pre margin of a widget
     *
     * @param id id id of a widget
     * @return the pre margin of a widget
     */
    protected fun getPreMargin(id: String?): Float {
        return if (mMapPostMargin != null && mMapPostMargin!!.containsKey(id)) {
            mMapPostMargin!![id]!!
        } else {
            0f
        }
    }

    /**
     * Get wrap mode
     *
     * @return wrap mode
     */
    fun getWrapMode(): Int {
        return mWrapMode
    }

    /**
     * Set wrap Mode
     *
     * @param wrap wrap Mode
     */
    fun setWrapMode(wrap: Int) {
        mWrapMode = wrap
    }

    /**
     * Get paddingLeft
     *
     * @return paddingLeft value
     */
    fun getPaddingLeft(): Int {
        return mPaddingLeft
    }

    /**
     * Set paddingLeft
     *
     * @param padding paddingLeft value
     */
    fun setPaddingLeft(padding: Int) {
        mPaddingLeft = padding
    }

    /**
     * Get paddingTop
     *
     * @return paddingTop value
     */
    fun getPaddingTop(): Int {
        return mPaddingTop
    }

    /**
     * Set paddingTop
     *
     * @param padding paddingTop value
     */
    fun setPaddingTop(padding: Int) {
        mPaddingTop = padding
    }

    /**
     * Get paddingRight
     *
     * @return paddingRight value
     */
    fun getPaddingRight(): Int {
        return mPaddingRight
    }

    /**
     * Set paddingRight
     *
     * @param padding paddingRight value
     */
    fun setPaddingRight(padding: Int) {
        mPaddingRight = padding
    }

    /**
     * Get paddingBottom
     *
     * @return paddingBottom value
     */
    fun getPaddingBottom(): Int {
        return mPaddingBottom
    }

    /**
     * Set padding
     *
     * @param padding paddingBottom value
     */
    fun setPaddingBottom(padding: Int) {
        mPaddingBottom = padding
    }

    /**
     * Get vertical style
     *
     * @return vertical style
     */
    fun getVerticalStyle(): Int {
        return mVerticalStyle
    }

    /**
     * set vertical style
     *
     * @param verticalStyle Flow vertical style
     */
    fun setVerticalStyle(verticalStyle: Int) {
        mVerticalStyle = verticalStyle
    }

    /**
     * Get first vertical style
     *
     * @return first vertical style
     */
    fun getFirstVerticalStyle(): Int {
        return mFirstVerticalStyle
    }

    /**
     * Set first vertical style
     *
     * @param firstVerticalStyle Flow first vertical style
     */
    fun setFirstVerticalStyle(firstVerticalStyle: Int) {
        mFirstVerticalStyle = firstVerticalStyle
    }

    /**
     * Get last vertical style
     *
     * @return last vertical style
     */
    fun getLastVerticalStyle(): Int {
        return mLastVerticalStyle
    }

    /**
     * Set last vertical style
     *
     * @param lastVerticalStyle Flow last vertical style
     */
    fun setLastVerticalStyle(lastVerticalStyle: Int) {
        mLastVerticalStyle = lastVerticalStyle
    }

    /**
     * Get horizontal style
     *
     * @return horizontal style
     */
    fun getHorizontalStyle(): Int {
        return mHorizontalStyle
    }

    /**
     * Set horizontal style
     *
     * @param horizontalStyle Flow horizontal style
     */
    fun setHorizontalStyle(horizontalStyle: Int) {
        mHorizontalStyle = horizontalStyle
    }

    /**
     * Get first horizontal style
     *
     * @return first horizontal style
     */
    fun getFirstHorizontalStyle(): Int {
        return mFirstHorizontalStyle
    }

    /**
     * Set first horizontal style
     *
     * @param firstHorizontalStyle Flow first horizontal style
     */
    fun setFirstHorizontalStyle(firstHorizontalStyle: Int) {
        mFirstHorizontalStyle = firstHorizontalStyle
    }

    /**
     * Get last horizontal style
     *
     * @return last horizontal style
     */
    fun getLastHorizontalStyle(): Int {
        return mLastHorizontalStyle
    }

    /**
     * Set last horizontal style
     *
     * @param lastHorizontalStyle Flow last horizontal style
     */
    fun setLastHorizontalStyle(lastHorizontalStyle: Int) {
        mLastHorizontalStyle = lastHorizontalStyle
    }

    /**
     * Get vertical align
     * @return vertical align value
     */
    fun getVerticalAlign(): Int {
        return mVerticalAlign
    }

    /**
     * Set vertical align
     *
     * @param verticalAlign vertical align value
     */
    fun setVerticalAlign(verticalAlign: Int) {
        mVerticalAlign = verticalAlign
    }

    /**
     * Get horizontal align
     *
     * @return horizontal align value
     */
    fun getHorizontalAlign(): Int {
        return mHorizontalAlign
    }

    /**
     * Set horizontal align
     *
     * @param horizontalAlign horizontal align value
     */
    fun setHorizontalAlign(horizontalAlign: Int) {
        mHorizontalAlign = horizontalAlign
    }

    /**
     * Get vertical gap
     *
     * @return vertical gap value
     */
    fun getVerticalGap(): Int {
        return mVerticalGap
    }

    /**
     * Set vertical gap
     *
     * @param verticalGap vertical gap value
     */
    fun setVerticalGap(verticalGap: Int) {
        mVerticalGap = verticalGap
    }

    /**
     * Get horizontal gap
     *
     * @return horizontal gap value
     */
    fun getHorizontalGap(): Int {
        return mHorizontalGap
    }

    /**
     * Set horizontal gap
     *
     * @param horizontalGap horizontal gap value
     */
    fun setHorizontalGap(horizontalGap: Int) {
        mHorizontalGap = horizontalGap
    }

    /**
     * Get max element wrap
     *
     * @return max element wrap value
     */
    fun getMaxElementsWrap(): Int {
        return mMaxElementsWrap
    }

    /**
     * Set max element wrap
     *
     * @param maxElementsWrap max element wrap value
     */
    fun setMaxElementsWrap(maxElementsWrap: Int) {
        mMaxElementsWrap = maxElementsWrap
    }

    /**
     * Get the orientation of a Flow
     *
     * @return orientation value
     */
    fun getOrientation(): Int {
        return mOrientation
    }

    /**
     * Set the orientation of a Flow
     *
     * @param mOrientation orientation value
     */
    fun setOrientation(mOrientation: Int) {
        this.mOrientation = mOrientation
    }

    /**
     * Get vertical bias
     *
     * @return vertical bias value
     */
    fun getVerticalBias(): Float {
        return mVerticalBias
    }

    /**
     * Get first vertical bias
     *
     * @return first vertical bias value
     */
    fun getFirstVerticalBias(): Float {
        return mFirstVerticalBias
    }

    /**
     * Set first vertical bias
     *
     * @param firstVerticalBias first vertical bias value
     */
    fun setFirstVerticalBias(firstVerticalBias: Float) {
        mFirstVerticalBias = firstVerticalBias
    }

    /**
     * Get last vertical bias
     *
     * @return last vertical bias
     */
    fun getLastVerticalBias(): Float {
        return mLastVerticalBias
    }

    /**
     * Set last vertical bias
     *
     * @param lastVerticalBias last vertical bias value
     */
    fun setLastVerticalBias(lastVerticalBias: Float) {
        mLastVerticalBias = lastVerticalBias
    }

    /**
     * Get horizontal bias
     * @return horizontal bias value
     */
    fun getHorizontalBias(): Float {
        return mHorizontalBias
    }

    /**
     * Get first horizontal bias
     *
     * @return first horizontal bias
     */
    fun getFirstHorizontalBias(): Float {
        return mFirstHorizontalBias
    }

    /**
     * Set first horizontal bias
     *
     * @param firstHorizontalBias first horizontal bias value
     */
    fun setFirstHorizontalBias(firstHorizontalBias: Float) {
        mFirstHorizontalBias = firstHorizontalBias
    }

    /**
     * Get last horizontal bias
     *
     * @return last horizontal bias value
     */
    fun getLastHorizontalBias(): Float {
        return mLastHorizontalBias
    }

    /**
     * Set last horizontal bias
     *
     * @param lastHorizontalBias last horizontal bias value
     */
    fun setLastHorizontalBias(lastHorizontalBias: Float) {
        mLastHorizontalBias = lastHorizontalBias
    }

    override val helperWidget: HelperWidget
        get() {
            if (mFlow == null) {
                mFlow = Flow()
            }
            return mFlow!!
        }

    override fun setHelperWidget(widget: HelperWidget) {
        mFlow = if (widget is Flow) {
            widget
        } else {
            null
        }
    }

    override fun apply() {
        helperWidget
        setConstraintWidget(mFlow)
        mFlow!!.setOrientation(mOrientation)
        mFlow!!.setWrapMode(mWrapMode)
        if (mMaxElementsWrap != UNKNOWN) {
            mFlow!!.setMaxElementsWrap(mMaxElementsWrap)
        }

        // Padding
        if (mPaddingLeft != 0) {
            mFlow!!.setPaddingLeft(mPaddingLeft)
        }
        if (mPaddingTop != 0) {
            mFlow!!.setPaddingTop(mPaddingTop)
        }
        if (mPaddingRight != 0) {
            mFlow!!.setPaddingRight(mPaddingRight)
        }
        if (mPaddingBottom != 0) {
            mFlow!!.setPaddingBottom(mPaddingBottom)
        }

        // Gap
        if (mHorizontalGap != 0) {
            mFlow!!.setHorizontalGap(mHorizontalGap)
        }
        if (mVerticalGap != 0) {
            mFlow!!.setVerticalGap(mVerticalGap)
        }

        // Bias
        if (mHorizontalBias != 0.5f) {
            mFlow!!.setHorizontalBias(mHorizontalBias)
        }
        if (mFirstHorizontalBias != 0.5f) {
            mFlow!!.setFirstHorizontalBias(mFirstHorizontalBias)
        }
        if (mLastHorizontalBias != 0.5f) {
            mFlow!!.setLastHorizontalBias(mLastHorizontalBias)
        }
        if (mVerticalBias != 0.5f) {
            mFlow!!.setVerticalBias(mVerticalBias)
        }
        if (mFirstVerticalBias != 0.5f) {
            mFlow!!.setFirstVerticalBias(mFirstVerticalBias)
        }
        if (mLastVerticalBias != 0.5f) {
            mFlow!!.setLastVerticalBias(mLastVerticalBias)
        }

        // Align
        if (mHorizontalAlign != HORIZONTAL_ALIGN_CENTER) {
            mFlow!!.setHorizontalAlign(mHorizontalAlign)
        }
        if (mVerticalAlign != VERTICAL_ALIGN_CENTER) {
            mFlow!!.setVerticalAlign(mVerticalAlign)
        }

        // Style
        if (mVerticalStyle != UNKNOWN) {
            mFlow!!.setVerticalStyle(mVerticalStyle)
        }
        if (mFirstVerticalStyle != UNKNOWN) {
            mFlow!!.setFirstVerticalStyle(mFirstVerticalStyle)
        }
        if (mLastVerticalStyle != UNKNOWN) {
            mFlow!!.setLastVerticalStyle(mLastVerticalStyle)
        }
        if (mHorizontalStyle != UNKNOWN) {
            mFlow!!.setHorizontalStyle(mHorizontalStyle)
        }
        if (mFirstHorizontalStyle != UNKNOWN) {
            mFlow!!.setFirstHorizontalStyle(mFirstHorizontalStyle)
        }
        if (mLastHorizontalStyle != UNKNOWN) {
            mFlow!!.setLastHorizontalStyle(mLastHorizontalStyle)
        }

        // General attributes of a widget
        applyBase()
    }
}
