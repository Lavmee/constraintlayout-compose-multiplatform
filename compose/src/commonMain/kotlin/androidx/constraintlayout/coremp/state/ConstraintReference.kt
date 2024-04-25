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
package androidx.constraintlayout.coremp.state

import androidx.constraintlayout.coremp.motion.utils.TypedBundle
import androidx.constraintlayout.coremp.motion.utils.TypedValues
import androidx.constraintlayout.coremp.state.State.Constraint.BASELINE_TO_BASELINE
import androidx.constraintlayout.coremp.state.State.Constraint.BASELINE_TO_BOTTOM
import androidx.constraintlayout.coremp.state.State.Constraint.BASELINE_TO_TOP
import androidx.constraintlayout.coremp.state.State.Constraint.BOTTOM_TO_BASELINE
import androidx.constraintlayout.coremp.state.State.Constraint.BOTTOM_TO_BOTTOM
import androidx.constraintlayout.coremp.state.State.Constraint.BOTTOM_TO_TOP
import androidx.constraintlayout.coremp.state.State.Constraint.CIRCULAR_CONSTRAINT
import androidx.constraintlayout.coremp.state.State.Constraint.END_TO_END
import androidx.constraintlayout.coremp.state.State.Constraint.END_TO_START
import androidx.constraintlayout.coremp.state.State.Constraint.LEFT_TO_LEFT
import androidx.constraintlayout.coremp.state.State.Constraint.LEFT_TO_RIGHT
import androidx.constraintlayout.coremp.state.State.Constraint.RIGHT_TO_LEFT
import androidx.constraintlayout.coremp.state.State.Constraint.RIGHT_TO_RIGHT
import androidx.constraintlayout.coremp.state.State.Constraint.START_TO_END
import androidx.constraintlayout.coremp.state.State.Constraint.START_TO_START
import androidx.constraintlayout.coremp.state.State.Constraint.TOP_TO_BASELINE
import androidx.constraintlayout.coremp.state.State.Constraint.TOP_TO_BOTTOM
import androidx.constraintlayout.coremp.state.State.Constraint.TOP_TO_TOP
import androidx.constraintlayout.coremp.state.helpers.Facade
import androidx.constraintlayout.coremp.widgets.ConstraintAnchor
import androidx.constraintlayout.coremp.widgets.ConstraintWidget
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.HORIZONTAL
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.UNKNOWN
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.VERTICAL

open class ConstraintReference : Reference {

    private var mKey: Any? = null

    override fun setKey(key: Any?) {
        mKey = key
    }

    override fun getKey(): Any? {
        return mKey
    }

    fun setTag(tag: String) {
        mTag = tag
    }

    fun getTag(): String? {
        return mTag
    }

    interface ConstraintReferenceFactory {
        // @TODO: add description
        fun create(state: State): ConstraintReference
    }

    private var mState: State? = null

    private var mTag: String? = null

    private var mFacade: Facade? = null

    private var mHorizontalChainStyle = ConstraintWidget.CHAIN_SPREAD
    private var mVerticalChainStyle = ConstraintWidget.CHAIN_SPREAD

    private var mHorizontalChainWeight: Float = UNKNOWN.toFloat()
    private var mVerticalChainWeight: Float = UNKNOWN.toFloat()

    protected var mHorizontalBias = 0.5f
    protected var mVerticalBias = 0.5f

    protected var mMarginLeft = 0
    protected var mMarginRight = 0
    protected var mMarginStart = 0
    protected var mMarginEnd = 0
    protected var mMarginTop = 0
    protected var mMarginBottom = 0

    protected var mMarginLeftGone = 0
    protected var mMarginRightGone = 0
    protected var mMarginStartGone = 0
    protected var mMarginEndGone = 0
    protected var mMarginTopGone = 0
    protected var mMarginBottomGone = 0

    private var mMarginBaseline = 0
    private var mMarginBaselineGone = 0

    private var mPivotX = Float.NaN
    private var mPivotY = Float.NaN

    private var mRotationX = Float.NaN
    private var mRotationY = Float.NaN
    private var mRotationZ = Float.NaN

    private var mTranslationX = Float.NaN
    private var mTranslationY = Float.NaN
    private var mTranslationZ = Float.NaN

    private var mAlpha = Float.NaN

    private var mScaleX = Float.NaN
    private var mScaleY = Float.NaN

    private var mVisibility = ConstraintWidget.VISIBLE

    protected var mLeftToLeft: Any? = null
    protected var mLeftToRight: Any? = null
    protected var mRightToLeft: Any? = null
    protected var mRightToRight: Any? = null
    protected var mStartToStart: Any? = null
    protected var mStartToEnd: Any? = null
    protected var mEndToStart: Any? = null
    protected var mEndToEnd: Any? = null
    protected var mTopToTop: Any? = null
    protected var mTopToBottom: Any? = null
    private var mTopToBaseline: Any? = null
    protected var mBottomToTop: Any? = null
    protected var mBottomToBottom: Any? = null
    private var mBottomToBaseline: Any? = null
    private var mBaselineToBaseline: Any? = null
    private var mBaselineToTop: Any? = null
    private var mBaselineToBottom: Any? = null
    private var mCircularConstraint: Any? = null
    private var mCircularAngle = 0f
    private var mCircularDistance = 0f

    private var mLast: State.Constraint? = null

    private var mHorizontalDimension = Dimension.createFixed(Dimension.WRAP_DIMENSION)
    private var mVerticalDimension = Dimension.createFixed(Dimension.WRAP_DIMENSION)

    private var mView: Any? = null
    private var mConstraintWidget: ConstraintWidget? = null

    private val mCustomColors: HashMap<String, Int> = HashMap()
    private var mCustomFloats: HashMap<String, Float>? = HashMap()

    var mMotionProperties: TypedBundle? = null

    // @TODO: add description
    fun setView(view: Any) {
        mView = view
        mConstraintWidget?.setCompanionWidget(mView)
    }

    fun getView(): Any {
        return mView!!
    }

    // @TODO: add description
    fun setFacade(facade: Facade?) {
        mFacade = facade
        if (facade != null) {
            setConstraintWidget(facade.getConstraintWidget())
        }
    }

    override fun getFacade(): Facade? {
        return mFacade
    }

    // @TODO: add description
    override fun setConstraintWidget(widget: ConstraintWidget?) {
        if (widget == null) {
            return
        }
        mConstraintWidget = widget
        mConstraintWidget!!.setCompanionWidget(mView)
    }

    override fun getConstraintWidget(): ConstraintWidget? {
        if (mConstraintWidget == null) {
            mConstraintWidget = createConstraintWidget().also {
                it.setCompanionWidget(mView)
            }
        }
        return mConstraintWidget
    }

    // @TODO: add description
    fun createConstraintWidget(): ConstraintWidget {
        return ConstraintWidget(
            width.getValue(),
            height.getValue(),
        )
    }

    /**
     * Validate the constraints
     */
    @Throws(IncorrectConstraintException::class)
    fun validate() {
        val errors = ArrayList<String>()
        if (mLeftToLeft != null && mLeftToRight != null) {
            errors.add("LeftToLeft and LeftToRight both defined")
        }
        if (mRightToLeft != null && mRightToRight != null) {
            errors.add("RightToLeft and RightToRight both defined")
        }
        if (mStartToStart != null && mStartToEnd != null) {
            errors.add("StartToStart and StartToEnd both defined")
        }
        if (mEndToStart != null && mEndToEnd != null) {
            errors.add("EndToStart and EndToEnd both defined")
        }
        if ((mLeftToLeft != null || mLeftToRight != null || mRightToLeft != null || mRightToRight != null) &&
            (mStartToStart != null || mStartToEnd != null || mEndToStart != null || mEndToEnd != null)
        ) {
            errors.add("Both left/right and start/end constraints defined")
        }
        if (errors.size > 0) {
            throw IncorrectConstraintException(errors)
        }
    }

    private fun get(reference: Any?): Any? {
        if (reference == null) {
            return null
        }
        if (!(reference is ConstraintReference)) {
            return mState!!.reference(reference)
        }
        return reference
    }

    constructor(state: State) {
        mState = state
    }

    fun setHorizontalChainStyle(chainStyle: Int) {
        mHorizontalChainStyle = chainStyle
    }

    fun getHorizontalChainStyle(): Int {
        return mHorizontalChainStyle
    }

    fun setVerticalChainStyle(chainStyle: Int) {
        mVerticalChainStyle = chainStyle
    }

    // @TODO: add description
    fun getVerticalChainStyle(chainStyle: Int): Int {
        return mVerticalChainStyle
    }

    fun getHorizontalChainWeight(): Float {
        return mHorizontalChainWeight
    }

    fun setHorizontalChainWeight(weight: Float) {
        mHorizontalChainWeight = weight
    }

    fun getVerticalChainWeight(): Float {
        return mVerticalChainWeight
    }

    fun setVerticalChainWeight(weight: Float) {
        mVerticalChainWeight = weight
    }

    // @TODO: add description
    fun clearVertical(): ConstraintReference {
        top().clear()
        baseline().clear()
        bottom().clear()
        return this
    }

    // @TODO: add description
    fun clearHorizontal(): ConstraintReference {
        start().clear()
        end().clear()
        left().clear()
        right().clear()
        return this
    }

    fun getTranslationX(): Float {
        return mTranslationX
    }

    fun getTranslationY(): Float {
        return mTranslationY
    }

    fun getTranslationZ(): Float {
        return mTranslationZ
    }

    fun getScaleX(): Float {
        return mScaleX
    }

    fun getScaleY(): Float {
        return mScaleY
    }

    fun getAlpha(): Float {
        return mAlpha
    }

    fun getPivotX(): Float {
        return mPivotX
    }

    fun getPivotY(): Float {
        return mPivotY
    }

    fun getRotationX(): Float {
        return mRotationX
    }

    fun getRotationY(): Float {
        return mRotationY
    }

    fun getRotationZ(): Float {
        return mRotationZ
    }

    // @TODO: add description
    fun pivotX(x: Float): ConstraintReference {
        mPivotX = x
        return this
    }

    // @TODO: add description
    fun pivotY(y: Float): ConstraintReference {
        mPivotY = y
        return this
    }

    // @TODO: add description
    fun rotationX(x: Float): ConstraintReference {
        mRotationX = x
        return this
    }

    // @TODO: add description
    fun rotationY(y: Float): ConstraintReference {
        mRotationY = y
        return this
    }

    // @TODO: add description
    fun rotationZ(z: Float): ConstraintReference {
        mRotationZ = z
        return this
    }

    // @TODO: add description
    fun translationX(x: Float): ConstraintReference {
        mTranslationX = x
        return this
    }

    // @TODO: add description
    fun translationY(y: Float): ConstraintReference {
        mTranslationY = y
        return this
    }

    // @TODO: add description
    fun translationZ(z: Float): ConstraintReference {
        mTranslationZ = z
        return this
    }

    // @TODO: add description
    fun scaleX(x: Float): ConstraintReference {
        mScaleX = x
        return this
    }

    // @TODO: add description
    fun scaleY(y: Float): ConstraintReference {
        mScaleY = y
        return this
    }

    // @TODO: add description
    fun alpha(alpha: Float): ConstraintReference {
        mAlpha = alpha
        return this
    }

    // @TODO: add description
    fun visibility(visibility: Int): ConstraintReference {
        mVisibility = visibility
        return this
    }

    // @TODO: add description
    fun left(): ConstraintReference {
        mLast = if (mLeftToLeft != null) {
            LEFT_TO_LEFT
        } else {
            LEFT_TO_RIGHT
        }
        return this
    }

    // @TODO: add description
    fun right(): ConstraintReference {
        mLast = if (mRightToLeft != null) {
            RIGHT_TO_LEFT
        } else {
            RIGHT_TO_RIGHT
        }
        return this
    }

    // @TODO: add description
    fun start(): ConstraintReference {
        mLast = if (mStartToStart != null) {
            START_TO_START
        } else {
            START_TO_END
        }
        return this
    }

    // @TODO: add description
    fun end(): ConstraintReference {
        mLast = if (mEndToStart != null) {
            END_TO_START
        } else {
            END_TO_END
        }
        return this
    }

    // @TODO: add description
    fun top(): ConstraintReference {
        mLast = if (mTopToTop != null) {
            TOP_TO_TOP
        } else {
            TOP_TO_BOTTOM
        }
        return this
    }

    // @TODO: add description
    fun bottom(): ConstraintReference {
        mLast = if (mBottomToTop != null) {
            BOTTOM_TO_TOP
        } else {
            BOTTOM_TO_BOTTOM
        }
        return this
    }

    // @TODO: add description
    fun baseline(): ConstraintReference {
        mLast = BASELINE_TO_BASELINE
        return this
    }

    // @TODO: add description
    fun addCustomColor(name: String?, color: Int) {
        mCustomColors[name!!] = color
    }

    // @TODO: add description
    fun addCustomFloat(name: String?, value: Float) {
        if (mCustomFloats == null) {
            mCustomFloats = HashMap()
        }
        mCustomFloats!![name!!] = value
    }

    private fun dereference() {
        mLeftToLeft = get(mLeftToLeft)
        mLeftToRight = get(mLeftToRight)
        mRightToLeft = get(mRightToLeft)
        mRightToRight = get(mRightToRight)
        mStartToStart = get(mStartToStart)
        mStartToEnd = get(mStartToEnd)
        mEndToStart = get(mEndToStart)
        mEndToEnd = get(mEndToEnd)
        mTopToTop = get(mTopToTop)
        mTopToBottom = get(mTopToBottom)
        mBottomToTop = get(mBottomToTop)
        mBottomToBottom = get(mBottomToBottom)
        mBaselineToBaseline = get(mBaselineToBaseline)
        mBaselineToTop = get(mBaselineToTop)
        mBaselineToBottom = get(mBaselineToBottom)
    }

    // @TODO: add description
    fun leftToLeft(reference: Any): ConstraintReference {
        mLast = LEFT_TO_LEFT
        mLeftToLeft = reference
        return this
    }

    // @TODO: add description
    fun leftToRight(reference: Any): ConstraintReference {
        mLast = LEFT_TO_RIGHT
        mLeftToRight = reference
        return this
    }

    // @TODO: add description
    fun rightToLeft(reference: Any): ConstraintReference {
        mLast = RIGHT_TO_LEFT
        mRightToLeft = reference
        return this
    }

    // @TODO: add description
    fun rightToRight(reference: Any): ConstraintReference {
        mLast = RIGHT_TO_RIGHT
        mRightToRight = reference
        return this
    }

    // @TODO: add description
    fun startToStart(reference: Any): ConstraintReference {
        mLast = START_TO_START
        mStartToStart = reference
        return this
    }

    // @TODO: add description
    fun startToEnd(reference: Any): ConstraintReference {
        mLast = START_TO_END
        mStartToEnd = reference
        return this
    }

    // @TODO: add description
    fun endToStart(reference: Any): ConstraintReference {
        mLast = END_TO_START
        mEndToStart = reference
        return this
    }

    // @TODO: add description
    fun endToEnd(reference: Any): ConstraintReference {
        mLast = END_TO_END
        mEndToEnd = reference
        return this
    }

    // @TODO: add description
    fun topToTop(reference: Any): ConstraintReference {
        mLast = TOP_TO_TOP
        mTopToTop = reference
        return this
    }

    // @TODO: add description
    fun topToBottom(reference: Any): ConstraintReference {
        mLast = TOP_TO_BOTTOM
        mTopToBottom = reference
        return this
    }

    fun topToBaseline(reference: Any): ConstraintReference {
        mLast = TOP_TO_BASELINE
        mTopToBaseline = reference
        return this
    }

    // @TODO: add description
    fun bottomToTop(reference: Any): ConstraintReference {
        mLast = BOTTOM_TO_TOP
        mBottomToTop = reference
        return this
    }

    // @TODO: add description
    fun bottomToBottom(reference: Any): ConstraintReference {
        mLast = BOTTOM_TO_BOTTOM
        mBottomToBottom = reference
        return this
    }

    fun bottomToBaseline(reference: Any): ConstraintReference {
        mLast = BOTTOM_TO_BASELINE
        mBottomToBaseline = reference
        return this
    }

    // @TODO: add description
    fun baselineToBaseline(reference: Any): ConstraintReference {
        mLast = BASELINE_TO_BASELINE
        mBaselineToBaseline = reference
        return this
    }

    // @TODO: add description
    fun baselineToTop(reference: Any): ConstraintReference {
        mLast = BASELINE_TO_TOP
        mBaselineToTop = reference
        return this
    }

    // @TODO: add description
    fun baselineToBottom(reference: Any): ConstraintReference {
        mLast = BASELINE_TO_BOTTOM
        mBaselineToBottom = reference
        return this
    }

    // @TODO: add description
    fun centerHorizontally(reference: Any?): ConstraintReference {
        val ref = get(reference)
        mStartToStart = ref
        mEndToEnd = ref
        mLast = State.Constraint.CENTER_HORIZONTALLY
        mHorizontalBias = 0.5f
        return this
    }

    // @TODO: add description
    fun centerVertically(reference: Any?): ConstraintReference {
        val ref = get(reference)
        mTopToTop = ref
        mBottomToBottom = ref
        mLast = State.Constraint.CENTER_VERTICALLY
        mVerticalBias = 0.5f
        return this
    }

    // @TODO: add description
    fun circularConstraint(reference: Any?, angle: Float, distance: Float): ConstraintReference {
        val ref = get(reference)
        mCircularConstraint = ref
        mCircularAngle = angle
        mCircularDistance = distance
        mLast = CIRCULAR_CONSTRAINT
        return this
    }

    // @TODO: add description

    fun width(dimension: Dimension): ConstraintReference {
        return setWidth(dimension)
    }

    // @TODO: add description
    fun height(dimension: Dimension): ConstraintReference {
        return setHeight(dimension)
    }

    val width: Dimension get() = mHorizontalDimension

    // @TODO: add description
    fun setWidth(dimension: Dimension): ConstraintReference {
        mHorizontalDimension = dimension
        return this
    }

    val height: Dimension get() = mVerticalDimension

    // @TODO: add description
    fun setHeight(dimension: Dimension): ConstraintReference {
        mVerticalDimension = dimension
        return this
    }

    // @TODO: add description
    open fun margin(marginValue: Any): ConstraintReference {
        return margin(mState!!.convertDimension(marginValue))
    }

    // @TODO: add description
    fun marginGone(marginGoneValue: Any): ConstraintReference {
        return marginGone(mState!!.convertDimension(marginGoneValue))
    }

    // @TODO: add description
    open fun margin(value: Int): ConstraintReference {
        if (mLast != null) {
            when (mLast) {
                LEFT_TO_LEFT, LEFT_TO_RIGHT -> {
                    mMarginLeft = value
                }

                RIGHT_TO_LEFT, RIGHT_TO_RIGHT -> {
                    mMarginRight = value
                }

                START_TO_START, START_TO_END -> {
                    mMarginStart = value
                }

                END_TO_START, END_TO_END -> {
                    mMarginEnd = value
                }

                TOP_TO_TOP, TOP_TO_BOTTOM, TOP_TO_BASELINE -> {
                    mMarginTop = value
                }

                BOTTOM_TO_TOP, BOTTOM_TO_BOTTOM, BOTTOM_TO_BASELINE -> {
                    mMarginBottom = value
                }

                BASELINE_TO_BOTTOM, BASELINE_TO_TOP, BASELINE_TO_BASELINE -> {
                    mMarginBaseline = value
                }

                CIRCULAR_CONSTRAINT -> {
                    mCircularDistance = value.toFloat()
                }

                else -> {}
            }
        } else {
            mMarginLeft = value
            mMarginRight = value
            mMarginStart = value
            mMarginEnd = value
            mMarginTop = value
            mMarginBottom = value
        }
        return this
    }

    // @TODO: add description
    fun marginGone(value: Int): ConstraintReference {
        if (mLast != null) {
            when (mLast) {
                LEFT_TO_LEFT, LEFT_TO_RIGHT -> {
                    mMarginLeftGone = value
                }

                RIGHT_TO_LEFT, RIGHT_TO_RIGHT -> {
                    mMarginRightGone = value
                }

                START_TO_START, START_TO_END -> {
                    mMarginStartGone = value
                }

                END_TO_START, END_TO_END -> {
                    mMarginEndGone = value
                }

                TOP_TO_TOP, TOP_TO_BOTTOM, TOP_TO_BASELINE -> {
                    mMarginTopGone = value
                }

                BOTTOM_TO_TOP, BOTTOM_TO_BOTTOM, BOTTOM_TO_BASELINE -> {
                    mMarginBottomGone = value
                }

                BASELINE_TO_TOP, BASELINE_TO_BOTTOM, BASELINE_TO_BASELINE -> {
                    mMarginBaselineGone = value
                }

                else -> {}
            }
        } else {
            mMarginLeftGone = value
            mMarginRightGone = value
            mMarginStartGone = value
            mMarginEndGone = value
            mMarginTopGone = value
            mMarginBottomGone = value
        }
        return this
    }

    // @TODO: add description
    fun horizontalBias(value: Float): ConstraintReference {
        mHorizontalBias = value
        return this
    }

    // @TODO: add description
    fun verticalBias(value: Float): ConstraintReference {
        mVerticalBias = value
        return this
    }

    // @TODO: add description
    open fun bias(value: Float): ConstraintReference {
        if (mLast == null) {
            return this
        }
        when (mLast) {
            State.Constraint.CENTER_HORIZONTALLY, LEFT_TO_LEFT, LEFT_TO_RIGHT, RIGHT_TO_LEFT, RIGHT_TO_RIGHT, START_TO_START, START_TO_END, END_TO_START, END_TO_END -> {
                mHorizontalBias = value
            }

            State.Constraint.CENTER_VERTICALLY, TOP_TO_TOP, TOP_TO_BOTTOM, TOP_TO_BASELINE, BOTTOM_TO_TOP, BOTTOM_TO_BOTTOM, BOTTOM_TO_BASELINE -> {
                mVerticalBias = value
            }

            else -> {}
        }
        return this
    }

    /**
     * Clears all constraints.
     */
    fun clearAll(): ConstraintReference {
        mLeftToLeft = null
        mLeftToRight = null
        mMarginLeft = 0
        mRightToLeft = null
        mRightToRight = null
        mMarginRight = 0
        mStartToStart = null
        mStartToEnd = null
        mMarginStart = 0
        mEndToStart = null
        mEndToEnd = null
        mMarginEnd = 0
        mTopToTop = null
        mTopToBottom = null
        mMarginTop = 0
        mBottomToTop = null
        mBottomToBottom = null
        mMarginBottom = 0
        mBaselineToBaseline = null
        mCircularConstraint = null
        mHorizontalBias = 0.5f
        mVerticalBias = 0.5f
        mMarginLeftGone = 0
        mMarginRightGone = 0
        mMarginStartGone = 0
        mMarginEndGone = 0
        mMarginTopGone = 0
        mMarginBottomGone = 0
        return this
    }

    // @TODO: add description
    fun clear(): ConstraintReference {
        if (mLast != null) {
            when (mLast) {
                LEFT_TO_LEFT, LEFT_TO_RIGHT -> {
                    mLeftToLeft = null
                    mLeftToRight = null
                    mMarginLeft = 0
                    mMarginLeftGone = 0
                }

                RIGHT_TO_LEFT, RIGHT_TO_RIGHT -> {
                    mRightToLeft = null
                    mRightToRight = null
                    mMarginRight = 0
                    mMarginRightGone = 0
                }

                START_TO_START, START_TO_END -> {
                    mStartToStart = null
                    mStartToEnd = null
                    mMarginStart = 0
                    mMarginStartGone = 0
                }

                END_TO_START, END_TO_END -> {
                    mEndToStart = null
                    mEndToEnd = null
                    mMarginEnd = 0
                    mMarginEndGone = 0
                }

                TOP_TO_TOP, TOP_TO_BOTTOM, TOP_TO_BASELINE -> {
                    mTopToTop = null
                    mTopToBottom = null
                    mTopToBaseline = null
                    mMarginTop = 0
                    mMarginTopGone = 0
                }

                BOTTOM_TO_TOP, BOTTOM_TO_BOTTOM, BOTTOM_TO_BASELINE -> {
                    mBottomToTop = null
                    mBottomToBottom = null
                    mBottomToBaseline = null
                    mMarginBottom = 0
                    mMarginBottomGone = 0
                }

                BASELINE_TO_BASELINE -> {
                    mBaselineToBaseline = null
                }

                CIRCULAR_CONSTRAINT -> {
                    mCircularConstraint = null
                }

                else -> {}
            }
        } else {
            clearAll()
        }
        return this
    }

    private fun getTarget(target: Any?): ConstraintWidget? {
        if (target is Reference) {
            return target.getConstraintWidget()
        }
        return null
    }

    private fun applyConnection(
        widget: ConstraintWidget,
        opaqueTarget: Any?,
        type: State.Constraint,
    ) {
        val target = getTarget(opaqueTarget) ?: return
        when (type) {
            // TODO: apply RTL
            else -> {}
        }
        when (type) {
            START_TO_START -> {
                widget.getAnchor(ConstraintAnchor.Type.LEFT)!!.connect(
                    target.getAnchor(
                        ConstraintAnchor.Type.LEFT,
                    ),
                    mMarginStart,
                    mMarginStartGone,
                    false,
                )
            }

            START_TO_END -> {
                widget.getAnchor(ConstraintAnchor.Type.LEFT)!!.connect(
                    target.getAnchor(
                        ConstraintAnchor.Type.RIGHT,
                    ),
                    mMarginStart,
                    mMarginStartGone,
                    false,
                )
            }

            END_TO_START -> {
                widget.getAnchor(ConstraintAnchor.Type.RIGHT)!!.connect(
                    target.getAnchor(
                        ConstraintAnchor.Type.LEFT,
                    ),
                    mMarginEnd,
                    mMarginEndGone,
                    false,
                )
            }

            END_TO_END -> {
                widget.getAnchor(ConstraintAnchor.Type.RIGHT)!!.connect(
                    target.getAnchor(
                        ConstraintAnchor.Type.RIGHT,
                    ),
                    mMarginEnd,
                    mMarginEndGone,
                    false,
                )
            }

            LEFT_TO_LEFT -> {
                widget.getAnchor(ConstraintAnchor.Type.LEFT)!!.connect(
                    target.getAnchor(
                        ConstraintAnchor.Type.LEFT,
                    ),
                    mMarginLeft,
                    mMarginLeftGone,
                    false,
                )
            }

            LEFT_TO_RIGHT -> {
                widget.getAnchor(ConstraintAnchor.Type.LEFT)!!.connect(
                    target.getAnchor(
                        ConstraintAnchor.Type.RIGHT,
                    ),
                    mMarginLeft,
                    mMarginLeftGone,
                    false,
                )
            }

            RIGHT_TO_LEFT -> {
                widget.getAnchor(ConstraintAnchor.Type.RIGHT)!!.connect(
                    target.getAnchor(
                        ConstraintAnchor.Type.LEFT,
                    ),
                    mMarginRight,
                    mMarginRightGone,
                    false,
                )
            }

            RIGHT_TO_RIGHT -> {
                widget.getAnchor(ConstraintAnchor.Type.RIGHT)!!.connect(
                    target.getAnchor(
                        ConstraintAnchor.Type.RIGHT,
                    ),
                    mMarginRight,
                    mMarginRightGone,
                    false,
                )
            }

            TOP_TO_TOP -> {
                widget.getAnchor(ConstraintAnchor.Type.TOP)!!.connect(
                    target.getAnchor(
                        ConstraintAnchor.Type.TOP,
                    ),
                    mMarginTop,
                    mMarginTopGone,
                    false,
                )
            }

            TOP_TO_BOTTOM -> {
                widget.getAnchor(ConstraintAnchor.Type.TOP)!!.connect(
                    target.getAnchor(
                        ConstraintAnchor.Type.BOTTOM,
                    ),
                    mMarginTop,
                    mMarginTopGone,
                    false,
                )
            }

            TOP_TO_BASELINE -> {
                widget.immediateConnect(
                    ConstraintAnchor.Type.TOP,
                    target,
                    ConstraintAnchor.Type.BASELINE,
                    mMarginTop,
                    mMarginTopGone,
                )
            }

            BOTTOM_TO_TOP -> {
                widget.getAnchor(ConstraintAnchor.Type.BOTTOM)!!.connect(
                    target.getAnchor(
                        ConstraintAnchor.Type.TOP,
                    ),
                    mMarginBottom,
                    mMarginBottomGone,
                    false,
                )
            }

            BOTTOM_TO_BOTTOM -> {
                widget.getAnchor(ConstraintAnchor.Type.BOTTOM)!!.connect(
                    target.getAnchor(
                        ConstraintAnchor.Type.BOTTOM,
                    ),
                    mMarginBottom,
                    mMarginBottomGone,
                    false,
                )
            }

            BOTTOM_TO_BASELINE -> {
                widget.immediateConnect(
                    ConstraintAnchor.Type.BOTTOM,
                    target,
                    ConstraintAnchor.Type.BASELINE,
                    mMarginBottom,
                    mMarginBottomGone,
                )
            }

            BASELINE_TO_BASELINE -> {
                widget.immediateConnect(
                    ConstraintAnchor.Type.BASELINE,
                    target,
                    ConstraintAnchor.Type.BASELINE,
                    mMarginBaseline,
                    mMarginBaselineGone,
                )
            }

            BASELINE_TO_TOP -> {
                widget.immediateConnect(
                    ConstraintAnchor.Type.BASELINE,
                    target,
                    ConstraintAnchor.Type.TOP,
                    mMarginBaseline,
                    mMarginBaselineGone,
                )
            }

            BASELINE_TO_BOTTOM -> {
                widget.immediateConnect(
                    ConstraintAnchor.Type.BASELINE,
                    target,
                    ConstraintAnchor.Type.BOTTOM,
                    mMarginBaseline,
                    mMarginBaselineGone,
                )
            }

            CIRCULAR_CONSTRAINT -> {
                widget.connectCircularConstraint(target, mCircularAngle, mCircularDistance.toInt())
            }

            else -> {}
        }
    }

    /**
     * apply all the constraints attributes of the mConstraintWidget
     */
    fun applyWidgetConstraints() {
        applyConnection(mConstraintWidget!!, mLeftToLeft, LEFT_TO_LEFT)
        applyConnection(mConstraintWidget!!, mLeftToRight, LEFT_TO_RIGHT)
        applyConnection(mConstraintWidget!!, mRightToLeft, RIGHT_TO_LEFT)
        applyConnection(mConstraintWidget!!, mRightToRight, RIGHT_TO_RIGHT)
        applyConnection(mConstraintWidget!!, mStartToStart, START_TO_START)
        applyConnection(mConstraintWidget!!, mStartToEnd, START_TO_END)
        applyConnection(mConstraintWidget!!, mEndToStart, END_TO_START)
        applyConnection(mConstraintWidget!!, mEndToEnd, END_TO_END)
        applyConnection(mConstraintWidget!!, mTopToTop, TOP_TO_TOP)
        applyConnection(mConstraintWidget!!, mTopToBottom, TOP_TO_BOTTOM)
        applyConnection(mConstraintWidget!!, mTopToBaseline, TOP_TO_BASELINE)
        applyConnection(mConstraintWidget!!, mBottomToTop, BOTTOM_TO_TOP)
        applyConnection(mConstraintWidget!!, mBottomToBottom, BOTTOM_TO_BOTTOM)
        applyConnection(
            mConstraintWidget!!,
            mBottomToBaseline,
            BOTTOM_TO_BASELINE,
        )
        applyConnection(
            mConstraintWidget!!,
            mBaselineToBaseline,
            BASELINE_TO_BASELINE,
        )
        applyConnection(mConstraintWidget!!, mBaselineToTop, BASELINE_TO_TOP)
        applyConnection(
            mConstraintWidget!!,
            mBaselineToBottom,
            BASELINE_TO_BOTTOM,
        )
        applyConnection(
            mConstraintWidget!!,
            mCircularConstraint,
            CIRCULAR_CONSTRAINT,
        )
    }

    // @TODO: add description
    override fun apply() {
        if (mConstraintWidget == null) {
            return
        }
        if (mFacade != null) {
            mFacade!!.apply()
        }
        mHorizontalDimension.apply(mState, mConstraintWidget!!, HORIZONTAL)
        mVerticalDimension.apply(mState, mConstraintWidget!!, VERTICAL)
        dereference()
        applyWidgetConstraints()
        if (mHorizontalChainStyle != ConstraintWidget.CHAIN_SPREAD) {
            mConstraintWidget!!.setHorizontalChainStyle(mHorizontalChainStyle)
        }
        if (mVerticalChainStyle != ConstraintWidget.CHAIN_SPREAD) {
            mConstraintWidget!!.setVerticalChainStyle(mVerticalChainStyle)
        }
        if (mHorizontalChainWeight != UNKNOWN.toFloat()) {
            mConstraintWidget!!.setHorizontalWeight(mHorizontalChainWeight)
        }
        if (mVerticalChainWeight != UNKNOWN.toFloat()) {
            mConstraintWidget!!.setVerticalWeight(mVerticalChainWeight)
        }
        mConstraintWidget!!.horizontalBiasPercent = mHorizontalBias
        mConstraintWidget!!.verticalBiasPercent = mVerticalBias
        mConstraintWidget!!.frame.pivotX = mPivotX
        mConstraintWidget!!.frame.pivotY = mPivotY
        mConstraintWidget!!.frame.rotationX = mRotationX
        mConstraintWidget!!.frame.rotationY = mRotationY
        mConstraintWidget!!.frame.rotationZ = mRotationZ
        mConstraintWidget!!.frame.translationX = mTranslationX
        mConstraintWidget!!.frame.translationY = mTranslationY
        mConstraintWidget!!.frame.translationZ = mTranslationZ
        mConstraintWidget!!.frame.scaleX = mScaleX
        mConstraintWidget!!.frame.scaleY = mScaleY
        mConstraintWidget!!.frame.alpha = mAlpha
        mConstraintWidget!!.frame.visibility = mVisibility
        mConstraintWidget!!.visibility = mVisibility
        mConstraintWidget!!.frame.setMotionAttributes(mMotionProperties)
        if (mCustomColors != null) {
            for (key in mCustomColors.keys) {
                val color = mCustomColors[key]!!
                mConstraintWidget!!.frame.setCustomAttribute(
                    key,
                    TypedValues.Custom.TYPE_COLOR,
                    color,
                )
            }
        }
        if (mCustomFloats != null) {
            for (key in mCustomFloats!!.keys) {
                val value = mCustomFloats!![key]!!
                mConstraintWidget!!.frame.setCustomAttribute(
                    key,
                    TypedValues.Custom.TYPE_FLOAT,
                    value,
                )
            }
        }
    }

    companion object {

        internal class IncorrectConstraintException(private val errors: ArrayList<String>) : Exception() {

            override val message: String
                get() = toString()

            override fun toString(): String {
                return "IncorrectConstraintException: $errors"
            }
        }
    }
}
