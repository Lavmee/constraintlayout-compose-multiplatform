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
package androidx.constraintlayout.coremp.motion

import androidx.constraintlayout.coremp.motion.utils.TypedValues
import androidx.constraintlayout.coremp.motion.utils.TypedValues.AttributesType
import androidx.constraintlayout.coremp.motion.utils.TypedValues.MotionType
import androidx.constraintlayout.coremp.state.WidgetFrame
import androidx.constraintlayout.coremp.widgets.ConstraintWidget

class MotionWidget : TypedValues {

    var mWidgetFrame = WidgetFrame()
    var mMotion: Motion = Motion()
    var mPropertySet: PropertySet = PropertySet()
    private var mProgress = 0f
    var mTransitionPathRotate = 0f

    class Motion {
        var mAnimateRelativeTo: String? = null
        var mAnimateCircleAngleTo = 0
        var mTransitionEasing: String? = null
        var mPathMotionArc = UNSET
        var mDrawPath = 0
        var mMotionStagger = Float.NaN
        var mPolarRelativeTo = UNSET
        var mPathRotate = Float.NaN
        var mQuantizeMotionPhase = Float.NaN
        var mQuantizeMotionSteps = UNSET
        var mQuantizeInterpolatorString: String? = null
        var mQuantizeInterpolatorType: Int = INTERPOLATOR_UNDEFINED // undefined
        var mQuantizeInterpolatorID = -1

        companion object {
            @Suppress("unused")
            private val INTERPOLATOR_REFERENCE_ID = -2

            @Suppress("unused")
            private val SPLINE_STRING = -1
            private const val INTERPOLATOR_UNDEFINED = -3
        }
    }

    class PropertySet {
        var visibility: Int = VISIBLE
        var mVisibilityMode = VISIBILITY_MODE_NORMAL
        var alpha = 1f
        var mProgress = Float.NaN
    }

    constructor()

    fun getParent(): MotionWidget? {
        return null
    }

    // @TODO: add description
    fun findViewById(mTransformPivotTarget: Int): MotionWidget? {
        return null
    }

    fun setVisibility(visibility: Int) {
        mPropertySet.visibility = visibility
    }

    fun getName(): String? {
        return mWidgetFrame.id
    }

    // @TODO: add description
    fun layout(l: Int, t: Int, r: Int, b: Int) {
        setBounds(l, t, r, b)
    }

    // @TODO: add description
    override fun toString(): String {
        return (
            mWidgetFrame.left.toString() + ", " + mWidgetFrame.top + ", " +
                mWidgetFrame.right + ", " + mWidgetFrame.bottom
            )
    }

    // @TODO: add description
    fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        mWidgetFrame.top = top
        mWidgetFrame.left = left
        mWidgetFrame.right = right
        mWidgetFrame.bottom = bottom
    }

    constructor(f: WidgetFrame) {
        mWidgetFrame = f
    }

    /**
     * This populates the motion attributes from widgetFrame to the MotionWidget
     */
    fun updateMotion(toUpdate: TypedValues) {
        if (mWidgetFrame.getMotionProperties() != null) {
            mWidgetFrame.getMotionProperties()!!.applyDelta(toUpdate)
        }
    }

    override fun setValue(id: Int, value: Int): Boolean {
        val set: Boolean = setValueAttributes(id, value.toFloat())
        return if (set) {
            true
        } else {
            setValueMotion(id, value)
        }
    }

    override fun setValue(id: Int, value: Float): Boolean {
        val set: Boolean = setValueAttributes(id, value)
        return if (set) {
            true
        } else {
            setValueMotion(id, value)
        }
    }

    override fun setValue(id: Int, value: String): Boolean {
        if (id == MotionType.TYPE_ANIMATE_RELATIVE_TO) {
            mMotion.mAnimateRelativeTo = value
            return true
        }
        return setValueMotion(id, value)
    }

    override fun setValue(id: Int, value: Boolean): Boolean {
        return false
    }

    // @TODO: add description
    fun setValueMotion(id: Int, value: Int): Boolean {
        when (id) {
            MotionType.TYPE_ANIMATE_CIRCLEANGLE_TO -> mMotion.mAnimateCircleAngleTo = value
            MotionType.TYPE_PATHMOTION_ARC -> mMotion.mPathMotionArc = value
            MotionType.TYPE_DRAW_PATH -> mMotion.mDrawPath = value
            MotionType.TYPE_POLAR_RELATIVETO -> mMotion.mPolarRelativeTo = value
            MotionType.TYPE_QUANTIZE_MOTIONSTEPS -> mMotion.mQuantizeMotionSteps = value
            MotionType.TYPE_QUANTIZE_INTERPOLATOR_TYPE -> mMotion.mQuantizeInterpolatorType = value
            MotionType.TYPE_QUANTIZE_INTERPOLATOR_ID -> mMotion.mQuantizeInterpolatorID = value
            else -> return false
        }
        return true
    }

    // @TODO: add description
    fun setValueMotion(id: Int, value: String?): Boolean {
        when (id) {
            MotionType.TYPE_EASING -> mMotion.mTransitionEasing = value
            MotionType.TYPE_QUANTIZE_INTERPOLATOR -> mMotion.mQuantizeInterpolatorString = value
            else -> return false
        }
        return true
    }

    // @TODO: add description
    fun setValueMotion(id: Int, value: Float): Boolean {
        when (id) {
            MotionType.TYPE_STAGGER -> mMotion.mMotionStagger = value
            MotionType.TYPE_PATH_ROTATE -> mMotion.mPathRotate = value
            MotionType.TYPE_QUANTIZE_MOTION_PHASE -> mMotion.mQuantizeMotionPhase = value
            else -> return false
        }
        return true
    }

    /**
     * Sets the attributes
     */
    fun setValueAttributes(id: Int, value: Float): Boolean {
        when (id) {
            AttributesType.TYPE_ALPHA -> mWidgetFrame.alpha = value
            AttributesType.TYPE_TRANSLATION_X -> mWidgetFrame.translationX = value
            AttributesType.TYPE_TRANSLATION_Y -> mWidgetFrame.translationY = value
            AttributesType.TYPE_TRANSLATION_Z -> mWidgetFrame.translationZ = value
            AttributesType.TYPE_ROTATION_X -> mWidgetFrame.rotationX = value
            AttributesType.TYPE_ROTATION_Y -> mWidgetFrame.rotationY = value
            AttributesType.TYPE_ROTATION_Z -> mWidgetFrame.rotationZ = value
            AttributesType.TYPE_SCALE_X -> mWidgetFrame.scaleX = value
            AttributesType.TYPE_SCALE_Y -> mWidgetFrame.scaleY = value
            AttributesType.TYPE_PIVOT_X -> mWidgetFrame.pivotX = value
            AttributesType.TYPE_PIVOT_Y -> mWidgetFrame.pivotY = value
            AttributesType.TYPE_PROGRESS -> mProgress = value
            AttributesType.TYPE_PATH_ROTATE -> mTransitionPathRotate = value
            else -> return false
        }
        return true
    }

    /**
     * Sets the attributes
     */
    fun getValueAttributes(id: Int): Float {
        return when (id) {
            AttributesType.TYPE_ALPHA -> mWidgetFrame.alpha
            AttributesType.TYPE_TRANSLATION_X -> mWidgetFrame.translationX
            AttributesType.TYPE_TRANSLATION_Y -> mWidgetFrame.translationY
            AttributesType.TYPE_TRANSLATION_Z -> mWidgetFrame.translationZ
            AttributesType.TYPE_ROTATION_X -> mWidgetFrame.rotationX
            AttributesType.TYPE_ROTATION_Y -> mWidgetFrame.rotationY
            AttributesType.TYPE_ROTATION_Z -> mWidgetFrame.rotationZ
            AttributesType.TYPE_SCALE_X -> mWidgetFrame.scaleX
            AttributesType.TYPE_SCALE_Y -> mWidgetFrame.scaleY
            AttributesType.TYPE_PIVOT_X -> mWidgetFrame.pivotX
            AttributesType.TYPE_PIVOT_Y -> mWidgetFrame.pivotY
            AttributesType.TYPE_PROGRESS -> mProgress
            AttributesType.TYPE_PATH_ROTATE -> mTransitionPathRotate
            else -> Float.NaN
        }
    }

    override fun getId(name: String?): Int {
        val ret = AttributesType.getId(name)
        return if (ret != -1) {
            ret
        } else {
            MotionType.getId(name)
        }
    }

    fun getTop(): Int {
        return mWidgetFrame.top
    }

    fun getLeft(): Int {
        return mWidgetFrame.left
    }

    fun getBottom(): Int {
        return mWidgetFrame.bottom
    }

    fun getRight(): Int {
        return mWidgetFrame.right
    }

    fun setPivotX(px: Float) {
        mWidgetFrame.pivotX = px
    }

    fun setPivotY(py: Float) {
        mWidgetFrame.pivotY = py
    }

    fun getRotationX(): Float {
        return mWidgetFrame.rotationX
    }

    fun setRotationX(rotationX: Float) {
        mWidgetFrame.rotationX = rotationX
    }

    fun getRotationY(): Float {
        return mWidgetFrame.rotationY
    }

    fun setRotationY(rotationY: Float) {
        mWidgetFrame.rotationY = rotationY
    }

    fun getRotationZ(): Float {
        return mWidgetFrame.rotationZ
    }

    fun setRotationZ(rotationZ: Float) {
        mWidgetFrame.rotationZ = rotationZ
    }

    fun getTranslationX(): Float {
        return mWidgetFrame.translationX
    }

    fun setTranslationX(translationX: Float) {
        mWidgetFrame.translationX = translationX
    }

    fun getTranslationY(): Float {
        return mWidgetFrame.translationY
    }

    fun setTranslationY(translationY: Float) {
        mWidgetFrame.translationY = translationY
    }

    fun setTranslationZ(tz: Float) {
        mWidgetFrame.translationZ = tz
    }

    fun getTranslationZ(): Float {
        return mWidgetFrame.translationZ
    }

    fun getScaleX(): Float {
        return mWidgetFrame.scaleX
    }

    fun setScaleX(scaleX: Float) {
        mWidgetFrame.scaleX = scaleX
    }

    fun getScaleY(): Float {
        return mWidgetFrame.scaleY
    }

    fun setScaleY(scaleY: Float) {
        mWidgetFrame.scaleY = scaleY
    }

    fun getVisibility(): Int {
        return mPropertySet.visibility
    }

    fun getPivotX(): Float {
        return mWidgetFrame.pivotX
    }

    fun getPivotY(): Float {
        return mWidgetFrame.pivotY
    }

    fun getAlpha(): Float {
        return mWidgetFrame.alpha
    }

    fun getX(): Int {
        return mWidgetFrame.left
    }

    fun getY(): Int {
        return mWidgetFrame.top
    }

    val width: Int get() = mWidgetFrame.right - mWidgetFrame.left

    val height: Int get() = mWidgetFrame.bottom - mWidgetFrame.top

    fun getWidgetFrame(): WidgetFrame {
        return mWidgetFrame
    }

    fun getCustomAttributeNames(): Set<String> {
        return mWidgetFrame.getCustomAttributeNames()
    }

    // @TODO: add description
    fun setCustomAttribute(name: String, type: Int, value: Float) {
        mWidgetFrame.setCustomAttribute(name, type, value)
    }

    // @TODO: add description
    fun setCustomAttribute(name: String, type: Int, value: Int) {
        mWidgetFrame.setCustomAttribute(name, type, value)
    }

    // @TODO: add description
    fun setCustomAttribute(name: String, type: Int, value: Boolean) {
        mWidgetFrame.setCustomAttribute(name, type, value)
    }

    // @TODO: add description
    fun setCustomAttribute(name: String, type: Int, value: String) {
        mWidgetFrame.setCustomAttribute(name, type, value)
    }

    // @TODO: add description
    fun getCustomAttribute(name: String): CustomVariable? {
        return mWidgetFrame.getCustomAttribute(name)
    }

    // @TODO: add description
    fun setInterpolatedValue(attribute: CustomAttribute, mCache: FloatArray) {
        mWidgetFrame.setCustomAttribute(attribute.mName, TypedValues.Custom.TYPE_FLOAT, mCache[0])
    }

    companion object {
        const val VISIBILITY_MODE_NORMAL = 0
        const val VISIBILITY_MODE_IGNORE = 1

        @Suppress("unused")
        private val INTERNAL_MATCH_PARENT = -1

        @Suppress("unused")
        private val INTERNAL_WRAP_CONTENT = -2
        const val INVISIBLE = 0
        const val VISIBLE = 4

        @Suppress("unused")
        private val INTERNAL_MATCH_CONSTRAINT = -3

        @Suppress("unused")
        private val INTERNAL_WRAP_CONTENT_CONSTRAINED = -4

        const val ROTATE_NONE = 0
        const val ROTATE_PORTRATE_OF_RIGHT = 1
        const val ROTATE_PORTRATE_OF_LEFT = 2
        const val ROTATE_RIGHT_OF_PORTRATE = 3
        const val ROTATE_LEFT_OF_PORTRATE = 4
        const val UNSET = -1
        const val MATCH_CONSTRAINT = 0
        const val PARENT_ID = 0
        const val FILL_PARENT = -1
        const val MATCH_PARENT = -1
        const val WRAP_CONTENT = -2
        const val GONE_UNSET = Int.MIN_VALUE
        const val MATCH_CONSTRAINT_WRAP = ConstraintWidget.MATCH_CONSTRAINT_WRAP
    }
}
