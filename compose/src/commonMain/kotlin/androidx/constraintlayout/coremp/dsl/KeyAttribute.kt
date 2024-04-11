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
@Suppress("UNUSED")
open class KeyAttribute(frame: Int, target: String) : Keys() {
    protected var TYPE = "KeyAttributes"
    private var mTarget: String = target
    private val mFrame = frame
    private var mTransitionEasing: String? = null
    private var mCurveFit: Fit? = null
    private var mVisibility: Visibility? = null
    private var mAlpha = Float.NaN
    private var mRotation = Float.NaN
    private var mRotationX = Float.NaN
    private var mRotationY = Float.NaN
    private var mPivotX = Float.NaN
    private var mPivotY = Float.NaN
    private var mTransitionPathRotate = Float.NaN
    private var mScaleX = Float.NaN
    private var mScaleY = Float.NaN
    private var mTranslationX = Float.NaN
    private var mTranslationY = Float.NaN
    private var mTranslationZ = Float.NaN

    enum class Fit {
        SPLINE,
        LINEAR,
    }

    enum class Visibility {
        VISIBLE,
        INVISIBLE,
        GONE,
    }

    fun getTarget(): String {
        return mTarget
    }

    fun setTarget(target: String) {
        mTarget = target
    }

    fun getTransitionEasing(): String? {
        return mTransitionEasing
    }

    fun setTransitionEasing(transitionEasing: String) {
        mTransitionEasing = transitionEasing
    }

    fun getCurveFit(): Fit? {
        return mCurveFit
    }

    fun setCurveFit(curveFit: Fit) {
        mCurveFit = curveFit
    }

    fun getVisibility(): Visibility? {
        return mVisibility
    }

    fun setVisibility(visibility: Visibility) {
        mVisibility = visibility
    }

    fun getAlpha(): Float {
        return mAlpha
    }

    fun setAlpha(alpha: Float) {
        mAlpha = alpha
    }

    fun getRotation(): Float {
        return mRotation
    }

    fun setRotation(rotation: Float) {
        mRotation = rotation
    }

    fun getRotationX(): Float {
        return mRotationX
    }

    fun setRotationX(rotationX: Float) {
        mRotationX = rotationX
    }

    fun getRotationY(): Float {
        return mRotationY
    }

    fun setRotationY(rotationY: Float) {
        mRotationY = rotationY
    }

    fun getPivotX(): Float {
        return mPivotX
    }

    fun setPivotX(pivotX: Float) {
        mPivotX = pivotX
    }

    fun getPivotY(): Float {
        return mPivotY
    }

    fun setPivotY(pivotY: Float) {
        mPivotY = pivotY
    }

    fun getTransitionPathRotate(): Float {
        return mTransitionPathRotate
    }

    fun setTransitionPathRotate(transitionPathRotate: Float) {
        mTransitionPathRotate = transitionPathRotate
    }

    fun getScaleX(): Float {
        return mScaleX
    }

    fun setScaleX(scaleX: Float) {
        mScaleX = scaleX
    }

    fun getScaleY(): Float {
        return mScaleY
    }

    fun setScaleY(scaleY: Float) {
        mScaleY = scaleY
    }

    fun getTranslationX(): Float {
        return mTranslationX
    }

    fun setTranslationX(translationX: Float) {
        mTranslationX = translationX
    }

    fun getTranslationY(): Float {
        return mTranslationY
    }

    fun setTranslationY(translationY: Float) {
        mTranslationY = translationY
    }

    fun getTranslationZ(): Float {
        return mTranslationZ
    }

    fun setTranslationZ(translationZ: Float) {
        mTranslationZ = translationZ
    }

    override fun toString(): String {
        val ret = StringBuilder()
        ret.append(TYPE)
        ret.append(":{\n")
        attributesToString(ret)
        ret.append("},\n")
        return ret.toString()
    }

    protected open fun attributesToString(builder: StringBuilder) {
        append(builder, "target", mTarget)
        builder.append("frame:").append(mFrame).append(",\n")
        append(builder, "easing", mTransitionEasing)
        if (mCurveFit != null) {
            builder.append("fit:'").append(mCurveFit).append("',\n")
        }
        if (mVisibility != null) {
            builder.append("visibility:'").append(mVisibility).append("',\n")
        }
        append(builder, "alpha", mAlpha)
        append(builder, "rotationX", mRotationX)
        append(builder, "rotationY", mRotationY)
        append(builder, "rotationZ", mRotation)
        append(builder, "pivotX", mPivotX)
        append(builder, "pivotY", mPivotY)
        append(builder, "pathRotate", mTransitionPathRotate)
        append(builder, "scaleX", mScaleX)
        append(builder, "scaleY", mScaleY)
        append(builder, "translationX", mTranslationX)
        append(builder, "translationY", mTranslationY)
        append(builder, "translationZ", mTranslationZ)
    }
}
