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
package androidx.constraintlayout.coremp.dsl

open class Constraint(id: String) {
    private val mId: String = id

    open inner class Anchor(side: Side) {
        val mSide: Side = side
        var mConnection: Anchor? = null
        var mMargin = 0
        var mGoneMargin = Int.MIN_VALUE

        fun getId(): String {
            return mId
        }

        @Suppress("UNUSED")
        fun getParent(): Constraint {
            return this@Constraint
        }

        fun build(builder: StringBuilder) {
            if (mConnection != null) {
                builder.append(mSide.toString().lowercase())
                    .append(":").append(this).append(",\n")
            }
        }

        override fun toString(): String {
            val ret = StringBuilder("[")
            if (mConnection != null) {
                ret.append("'").append(mConnection!!.getId()).append("',")
                    .append("'")
                    .append(mConnection!!.mSide.toString().lowercase())
                    .append("'")
            }
            if (mMargin != 0) {
                ret.append(",").append(mMargin)
            }
            if (mGoneMargin != Int.MIN_VALUE) {
                if (mMargin == 0) {
                    ret.append(",0,").append(mGoneMargin)
                } else {
                    ret.append(",").append(mGoneMargin)
                }
            }
            ret.append("]")
            return ret.toString()
        }
    }
    inner class VAnchor internal constructor(side: VSide) : Anchor(Side.valueOf(side.name))
    inner class HAnchor internal constructor(side: HSide) : Anchor(Side.valueOf(side.name))

    @Suppress("UNUSED")
    enum class Behaviour {
        SPREAD,
        WRAP,
        PERCENT,
        RATIO,
        RESOLVED,
    }
    enum class ChainMode {
        SPREAD,
        SPREAD_INSIDE,
        PACKED,
    }
    enum class VSide {
        TOP,
        BOTTOM,
        BASELINE,
    }
    enum class HSide {
        LEFT,
        RIGHT,
        START,
        END,
    }
    enum class Side {
        LEFT,
        RIGHT,
        TOP,
        BOTTOM,
        START,
        END,
        BASELINE,
    }

    @Suppress("UNUSED")
    var helperType: String? = null

    @Suppress("UNUSED")
    var helperJason: String? = null

    private val mLeft = HAnchor(HSide.LEFT)
    private val mRight = HAnchor(HSide.RIGHT)
    private val mTop = VAnchor(VSide.TOP)
    private val mBottom = VAnchor(VSide.BOTTOM)
    private val mStart = HAnchor(HSide.START)
    private val mEnd = HAnchor(HSide.END)
    private val mBaseline = VAnchor(VSide.BASELINE)
    private var mWidth = UNSET
    private var mHeight = UNSET
    private var mHorizontalBias = Float.NaN
    private var mVerticalBias = Float.NaN
    private var mDimensionRatio: String? = null
    private var mCircleConstraint: String? = null
    private var mCircleRadius = Int.MIN_VALUE
    private var mCircleAngle = Float.NaN
    private var mEditorAbsoluteX = Int.MIN_VALUE
    private var mEditorAbsoluteY = Int.MIN_VALUE
    private var mVerticalWeight = Float.NaN
    private var mHorizontalWeight = Float.NaN
    private var mHorizontalChainStyle: ChainMode? = null
    private var mVerticalChainStyle: ChainMode? = null
    private var mWidthDefault: Behaviour? = null
    private var mHeightDefault: Behaviour? = null
    private var mWidthMax = UNSET
    private var mHeightMax = UNSET
    private var mWidthMin = UNSET
    private var mHeightMin = UNSET
    private var mWidthPercent = Float.NaN
    private var mHeightPercent = Float.NaN
    private var mReferenceIds: Array<String>? = null
    private var mConstrainedWidth = false
    private var mConstrainedHeight = false

    /**
     * get left anchor
     *
     * @return left anchor
     */
    fun getLeft(): HAnchor {
        return mLeft
    }

    /**
     * get right anchor
     *
     * @return right anchor
     */
    fun getRight(): HAnchor {
        return mRight
    }

    /**
     * get top anchor
     *
     * @return top anchor
     */
    fun getTop(): VAnchor {
        return mTop
    }

    /**
     * get bottom anchor
     *
     * @return bottom anchor
     */
    fun getBottom(): VAnchor {
        return mBottom
    }

    /**
     * get start anchor
     *
     * @return start anchor
     */
    fun getStart(): HAnchor {
        return mStart
    }

    /**
     * get end anchor
     *
     * @return end anchor
     */
    fun getEnd(): HAnchor {
        return mEnd
    }

    /**
     * get baseline anchor
     *
     * @return baseline anchor
     */
    fun getBaseline(): VAnchor {
        return mBaseline
    }

    /**
     * get horizontalBias
     *
     * @return horizontalBias
     */
    @Suppress("UNUSED")
    fun getHorizontalBias(): Float {
        return mHorizontalBias
    }

    /**
     * set horizontalBias
     *
     * @param horizontalBias
     */
    fun setHorizontalBias(horizontalBias: Float) {
        mHorizontalBias = horizontalBias
    }

    /**
     * get verticalBias
     *
     * @return verticalBias
     */
    @Suppress("UNUSED")
    fun getVerticalBias(): Float {
        return mVerticalBias
    }

    /**
     * set verticalBias
     *
     * @param verticalBias
     */
    fun setVerticalBias(verticalBias: Float) {
        mVerticalBias = verticalBias
    }

    /**
     * get dimensionRatio
     *
     * @return dimensionRatio
     */
    @Suppress("UNUSED")
    fun getDimensionRatio(): String? {
        return mDimensionRatio
    }

    /**
     * set dimensionRatio
     *
     * @param dimensionRatio
     */
    fun setDimensionRatio(dimensionRatio: String?) {
        mDimensionRatio = dimensionRatio
    }

    /**
     * get circleConstraint
     *
     * @return circleConstraint
     */
    @Suppress("UNUSED")
    fun getCircleConstraint(): String? {
        return mCircleConstraint
    }

    /**
     * set circleConstraint
     *
     * @param circleConstraint
     */
    fun setCircleConstraint(circleConstraint: String?) {
        mCircleConstraint = circleConstraint
    }

    /**
     * get circleRadius
     *
     * @return circleRadius
     */
    @Suppress("UNUSED")
    fun getCircleRadius(): Int {
        return mCircleRadius
    }

    /**
     * set circleRadius
     *
     * @param circleRadius
     */
    fun setCircleRadius(circleRadius: Int) {
        mCircleRadius = circleRadius
    }

    /**
     * get circleAngle
     *
     * @return circleAngle
     */
    @Suppress("UNUSED")
    fun getCircleAngle(): Float {
        return mCircleAngle
    }

    /**
     * set circleAngle
     *
     * @param circleAngle
     */
    fun setCircleAngle(circleAngle: Float) {
        mCircleAngle = circleAngle
    }

    /**
     * get editorAbsoluteX
     * @return editorAbsoluteX
     */
    @Suppress("UNUSED")
    fun getEditorAbsoluteX(): Int {
        return mEditorAbsoluteX
    }

    /**
     * set editorAbsoluteX
     * @param editorAbsoluteX
     */
    @Suppress("UNUSED")
    fun setEditorAbsoluteX(editorAbsoluteX: Int) {
        mEditorAbsoluteX = editorAbsoluteX
    }

    /**
     * get editorAbsoluteY
     * @return editorAbsoluteY
     */
    @Suppress("UNUSED")
    fun getEditorAbsoluteY(): Int {
        return mEditorAbsoluteY
    }

    /**
     * set editorAbsoluteY
     * @param editorAbsoluteY
     */
    @Suppress("UNUSED")
    fun setEditorAbsoluteY(editorAbsoluteY: Int) {
        mEditorAbsoluteY = editorAbsoluteY
    }

    /**
     * get verticalWeight
     *
     * @return verticalWeight
     */
    @Suppress("UNUSED")
    fun getVerticalWeight(): Float {
        return mVerticalWeight
    }

    /**
     * set verticalWeight
     *
     * @param verticalWeight
     */
    fun setVerticalWeight(verticalWeight: Float) {
        mVerticalWeight = verticalWeight
    }

    /**
     * get horizontalWeight
     *
     * @return horizontalWeight
     */
    @Suppress("UNUSED")
    fun getHorizontalWeight(): Float {
        return mHorizontalWeight
    }

    /**
     * set horizontalWeight
     *
     * @param horizontalWeight
     */
    fun setHorizontalWeight(horizontalWeight: Float) {
        mHorizontalWeight = horizontalWeight
    }

    /**
     * get horizontalChainStyle
     *
     * @return horizontalChainStyle
     */
    @Suppress("UNUSED")
    fun getHorizontalChainStyle(): ChainMode? {
        return mHorizontalChainStyle
    }

    /**
     * set horizontalChainStyle
     *
     * @param horizontalChainStyle
     */
    fun setHorizontalChainStyle(
        horizontalChainStyle: ChainMode?,
    ) {
        mHorizontalChainStyle = horizontalChainStyle
    }

    /**
     * get verticalChainStyle
     *
     * @return verticalChainStyle
     */
    @Suppress("UNUSED")
    fun getVerticalChainStyle(): ChainMode? {
        return mVerticalChainStyle
    }

    /**
     * set verticalChainStyle
     *
     * @param verticalChainStyle
     */
    fun setVerticalChainStyle(
        verticalChainStyle: ChainMode?,
    ) {
        mVerticalChainStyle = verticalChainStyle
    }

    /**
     * get widthDefault
     *
     * @return widthDefault
     */
    @Suppress("UNUSED")
    fun getWidthDefault(): Behaviour? {
        return mWidthDefault
    }

    /**
     * set widthDefault
     *
     * @param widthDefault
     */
    fun setWidthDefault(widthDefault: Behaviour?) {
        mWidthDefault = widthDefault
    }

    /**
     * get heightDefault
     *
     * @return heightDefault
     */
    @Suppress("UNUSED")
    fun getHeightDefault(): Behaviour? {
        return mHeightDefault
    }

    /**
     * set heightDefault
     *
     * @param heightDefault
     */
    fun setHeightDefault(heightDefault: Behaviour?) {
        mHeightDefault = heightDefault
    }

    /**
     * get widthMax
     *
     * @return widthMax
     */
    @Suppress("UNUSED")
    fun getWidthMax(): Int {
        return mWidthMax
    }

    /**
     * set widthMax
     *
     * @param widthMax
     */
    fun setWidthMax(widthMax: Int) {
        mWidthMax = widthMax
    }

    /**
     * get heightMax
     *
     * @return heightMax
     */
    @Suppress("UNUSED")
    fun getHeightMax(): Int {
        return mHeightMax
    }

    /**
     * set heightMax
     *
     * @param heightMax
     */
    fun setHeightMax(heightMax: Int) {
        mHeightMax = heightMax
    }

    /**
     * get widthMin
     *
     * @return widthMin
     */
    @Suppress("UNUSED")
    fun getWidthMin(): Int {
        return mWidthMin
    }

    /**
     * set widthMin
     *
     * @param widthMin
     */
    @Suppress("UNUSED")
    fun setWidthMin(widthMin: Int) {
        mWidthMin = widthMin
    }

    /**
     * get heightMin
     *
     * @return heightMin
     */
    @Suppress("UNUSED")
    fun getHeightMin(): Int {
        return mHeightMin
    }

    /**
     * set heightMin
     *
     * @param heightMin
     */
    @Suppress("UNUSED")
    fun setHeightMin(heightMin: Int) {
        mHeightMin = heightMin
    }

    /**
     * get widthPercent
     *
     * @return
     */
    @Suppress("UNUSED")
    fun getWidthPercent(): Float {
        return mWidthPercent
    }

    /**
     * set widthPercent
     *
     * @param widthPercent
     */
    fun setWidthPercent(widthPercent: Float) {
        mWidthPercent = widthPercent
    }

    /**
     * get heightPercent
     *
     * @return heightPercent
     */
    @Suppress("UNUSED")
    fun getHeightPercent(): Float {
        return mHeightPercent
    }

    /**
     * set heightPercent
     *
     * @param heightPercent
     */
    fun setHeightPercent(heightPercent: Float) {
        mHeightPercent = heightPercent
    }

    /**
     * get referenceIds
     *
     * @return referenceIds
     */
    @Suppress("UNUSED")
    fun getReferenceIds(): Array<String>? {
        return mReferenceIds
    }

    /**
     * set referenceIds
     *
     * @param referenceIds
     */
    fun setReferenceIds(referenceIds: Array<String>) {
        mReferenceIds = referenceIds
    }

    /**
     * is constrainedWidth
     *
     * @return true if width constrained
     */
    @Suppress("UNUSED")
    fun isConstrainedWidth(): Boolean {
        return mConstrainedWidth
    }

    /**
     * set constrainedWidth
     *
     * @param constrainedWidth
     */
    fun setConstrainedWidth(constrainedWidth: Boolean) {
        mConstrainedWidth = constrainedWidth
    }

    /**
     * is constrainedHeight
     *
     * @return true if height constrained
     */
    @Suppress("UNUSED")
    fun isConstrainedHeight(): Boolean {
        return mConstrainedHeight
    }

    /**
     * set constrainedHeight
     *
     * @param constrainedHeight
     */
    fun setConstrainedHeight(constrainedHeight: Boolean) {
        mConstrainedHeight = constrainedHeight
    }

    /**
     * get width
     * @return width
     */
    @Suppress("UNUSED")
    fun getWidth(): Int {
        return mWidth
    }

    /**
     * set width
     *
     * @param width
     */
    fun setWidth(width: Int) {
        mWidth = width
    }

    /**
     * get height
     * @return height
     */
    @Suppress("UNUSED")
    fun getHeight(): Int {
        return mHeight
    }

    /**
     * set height
     *
     * @param height
     */
    fun setHeight(height: Int) {
        mHeight = height
    }

    /**
     * Connect anchor to Top
     *
     * @param anchor anchor to be connected
     */
    @Suppress("UNUSED")
    fun linkToTop(anchor: VAnchor?) {
        linkToTop(anchor, 0)
    }

    /**
     * Connect anchor to Left
     *
     * @param anchor anchor to be connected
     */
    fun linkToLeft(anchor: HAnchor?) {
        linkToLeft(anchor, 0)
    }

    /**
     * Connect anchor to Right
     *
     * @param anchor anchor to be connected
     */
    @Suppress("UNUSED")
    fun linkToRight(anchor: HAnchor?) {
        linkToRight(anchor, 0)
    }

    /**
     * Connect anchor to Start
     *
     * @param anchor anchor to be connected
     */
    @Suppress("UNUSED")
    fun linkToStart(anchor: HAnchor?) {
        linkToStart(anchor, 0)
    }

    /**
     * Connect anchor to End
     *
     * @param anchor anchor to be connected
     */
    @Suppress("UNUSED")
    fun linkToEnd(anchor: HAnchor?) {
        linkToEnd(anchor, 0)
    }

    /**
     * Connect anchor to Bottom
     *
     * @param anchor anchor to be connected
     */
    fun linkToBottom(anchor: VAnchor?) {
        linkToBottom(anchor, 0)
    }

    /**
     * Connect anchor to Baseline
     *
     * @param anchor anchor to be connected
     */
    fun linkToBaseline(anchor: VAnchor?) {
        linkToBaseline(anchor, 0)
    }

    /**
     * Connect anchor to Top
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    fun linkToTop(anchor: VAnchor?, margin: Int) {
        linkToTop(anchor, margin, Int.MIN_VALUE)
    }

    /**
     * Connect anchor to Left
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    fun linkToLeft(anchor: HAnchor?, margin: Int) {
        linkToLeft(anchor, margin, Int.MIN_VALUE)
    }

    /**
     * Connect anchor to Right
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    fun linkToRight(anchor: HAnchor?, margin: Int) {
        linkToRight(anchor, margin, Int.MIN_VALUE)
    }

    /**
     * Connect anchor to Start
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    fun linkToStart(anchor: HAnchor?, margin: Int) {
        linkToStart(anchor, margin, Int.MIN_VALUE)
    }

    /**
     * Connect anchor to End
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    fun linkToEnd(anchor: HAnchor?, margin: Int) {
        linkToEnd(anchor, margin, Int.MIN_VALUE)
    }

    /**
     * Connect anchor to Bottom
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    fun linkToBottom(anchor: VAnchor?, margin: Int) {
        linkToBottom(anchor, margin, Int.MIN_VALUE)
    }

    /**
     * Connect anchor to Baseline
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    fun linkToBaseline(anchor: VAnchor?, margin: Int) {
        linkToBaseline(anchor, margin, Int.MIN_VALUE)
    }

    /**
     * Connect anchor to Top
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    fun linkToTop(anchor: VAnchor?, margin: Int, goneMargin: Int) {
        mTop.mConnection = anchor
        mTop.mMargin = margin
        mTop.mGoneMargin = goneMargin
    }

    /**
     * Connect anchor to Left
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    fun linkToLeft(anchor: HAnchor?, margin: Int, goneMargin: Int) {
        mLeft.mConnection = anchor
        mLeft.mMargin = margin
        mLeft.mGoneMargin = goneMargin
    }

    /**
     * Connect anchor to Right
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    fun linkToRight(anchor: HAnchor?, margin: Int, goneMargin: Int) {
        mRight.mConnection = anchor
        mRight.mMargin = margin
        mRight.mGoneMargin = goneMargin
    }

    /**
     * Connect anchor to Start
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    fun linkToStart(anchor: HAnchor?, margin: Int, goneMargin: Int) {
        mStart.mConnection = anchor
        mStart.mMargin = margin
        mStart.mGoneMargin = goneMargin
    }

    /**
     * Connect anchor to End
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    fun linkToEnd(anchor: HAnchor?, margin: Int, goneMargin: Int) {
        mEnd.mConnection = anchor
        mEnd.mMargin = margin
        mEnd.mGoneMargin = goneMargin
    }

    /**
     * Connect anchor to Bottom
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    fun linkToBottom(anchor: VAnchor?, margin: Int, goneMargin: Int) {
        mBottom.mConnection = anchor
        mBottom.mMargin = margin
        mBottom.mGoneMargin = goneMargin
    }

    /**
     * Connect anchor to Baseline
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    fun linkToBaseline(anchor: VAnchor?, margin: Int, goneMargin: Int) {
        mBaseline.mConnection = anchor
        mBaseline.mMargin = margin
        mBaseline.mGoneMargin = goneMargin
    }

    /**
     * convert a String array into a String representation
     *
     * @param str String array to be converted
     * @return a String representation of the input array.
     */
    fun convertStringArrayToString(str: Array<String>): String {
        val ret = StringBuilder("[")
        for (i in str.indices) {
            ret.append(if (i == 0) "'" else ",'")
            ret.append(str[i])
            ret.append("'")
        }
        ret.append("]")
        return ret.toString()
    }

    protected fun append(builder: StringBuilder, name: String?, value: Float) {
        if (value.isNaN()) {
            return
        }
        builder.append(name)
        builder.append(":").append(value).append(",\n")
    }

    override fun toString(): String {
        val ret = StringBuilder("$mId:{\n")
        mLeft.build(ret)
        mRight.build(ret)
        mTop.build(ret)
        mBottom.build(ret)
        mStart.build(ret)
        mEnd.build(ret)
        mBaseline.build(ret)
        if (mWidth != UNSET) {
            ret.append("width:").append(mWidth).append(",\n")
        }
        if (mHeight != UNSET) {
            ret.append("height:").append(mHeight).append(",\n")
        }
        append(ret, "horizontalBias", mHorizontalBias)
        append(ret, "verticalBias", mVerticalBias)
        if (mDimensionRatio != null) {
            ret.append("dimensionRatio:'").append(mDimensionRatio).append("',\n")
        }
        if (mCircleConstraint != null) {
            if (!mCircleAngle.isNaN() || mCircleRadius != Int.MIN_VALUE) {
                ret.append("circular:['").append(mCircleConstraint).append("'")
                if (!mCircleAngle.isNaN()) {
                    ret.append(",").append(mCircleAngle)
                }
                if (mCircleRadius != Int.MIN_VALUE) {
                    if (mCircleAngle.isNaN()) {
                        ret.append(",0,").append(mCircleRadius)
                    } else {
                        ret.append(",").append(mCircleRadius)
                    }
                }
                ret.append("],\n")
            }
        }
        append(ret, "verticalWeight", mVerticalWeight)
        append(ret, "horizontalWeight", mHorizontalWeight)
        if (mHorizontalChainStyle != null) {
            ret.append("horizontalChainStyle:'").append(chainModeMap[mHorizontalChainStyle])
                .append("',\n")
        }
        if (mVerticalChainStyle != null) {
            ret.append("verticalChainStyle:'").append(chainModeMap[mVerticalChainStyle])
                .append("',\n")
        }
        if (mWidthDefault != null) {
            if (mWidthMax == UNSET && mWidthMin == UNSET) {
                ret.append("width:'")
                    .append(mWidthDefault.toString().lowercase())
                    .append("',\n")
            } else {
                ret.append("width:{value:'")
                    .append(mWidthDefault.toString().lowercase())
                    .append("'")
                if (mWidthMax != UNSET) {
                    ret.append(",max:").append(mWidthMax)
                }
                if (mWidthMin != UNSET) {
                    ret.append(",min:").append(mWidthMin)
                }
                ret.append("},\n")
            }
        }
        if (mHeightDefault != null) {
            if (mHeightMax == UNSET && mHeightMin == UNSET) {
                ret.append("height:'")
                    .append(mHeightDefault.toString().lowercase())
                    .append("',\n")
            } else {
                ret.append("height:{value:'")
                    .append(mHeightDefault.toString().lowercase())
                    .append("'")
                if (mHeightMax != UNSET) {
                    ret.append(",max:").append(mHeightMax)
                }
                if (mHeightMin != UNSET) {
                    ret.append(",min:").append(mHeightMin)
                }
                ret.append("},\n")
            }
        }
        if (!mWidthPercent.toDouble().isNaN()) {
            ret.append("width:'").append(mWidthPercent.toInt()).append("%',\n")
        }
        if (!mHeightPercent.toDouble().isNaN()) {
            ret.append("height:'").append(mHeightPercent.toInt()).append("%',\n")
        }
        if (mReferenceIds != null) {
            ret.append("referenceIds:")
                .append(convertStringArrayToString(mReferenceIds!!))
                .append(",\n")
        }
        if (mConstrainedWidth) {
            ret.append("constrainedWidth:").append(mConstrainedWidth).append(",\n")
        }
        if (mConstrainedHeight) {
            ret.append("constrainedHeight:").append(mConstrainedHeight).append(",\n")
        }
        ret.append("},\n")
        return ret.toString()
    }

    companion object {
        val PARENT = Constraint("parent")
        const val UNSET = Int.MIN_VALUE
        val chainModeMap: MutableMap<ChainMode, String> = hashMapOf(
            ChainMode.SPREAD to "spread",
            ChainMode.SPREAD_INSIDE to "spread_inside",
            ChainMode.PACKED to "packed",
        )
    }
}
