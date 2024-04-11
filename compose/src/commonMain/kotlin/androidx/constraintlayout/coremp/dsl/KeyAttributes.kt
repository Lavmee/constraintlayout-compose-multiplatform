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

// the default is evenly spaced  1 at 50, 2 at 33 & 66, 3 at 25,50,75
@Suppress("UNUSED")
open class KeyAttributes(numOfFrames: Int, vararg targets: String) : Keys() {
    protected var TYPE = "KeyAttributes"
    private var mTarget: Array<out String> = targets
    private var mTransitionEasing: String? = null
    private var mCurveFit: Fit? = null
    private var mFrames: IntArray

    private var mVisibility: Array<out Visibility>? = null
    private var mAlpha: FloatArray? = null
    private var mRotation: FloatArray? = null
    private var mRotationX: FloatArray? = null
    private var mRotationY: FloatArray? = null
    private var mPivotX: FloatArray? = null
    private var mPivotY: FloatArray? = null
    private var mTransitionPathRotate: FloatArray? = null
    private var mScaleX: FloatArray? = null
    private var mScaleY: FloatArray? = null
    private var mTranslationX: FloatArray? = null
    private var mTranslationY: FloatArray? = null
    private var mTranslationZ: FloatArray? = null

    enum class Fit {
        SPLINE,
        LINEAR,
    }

    enum class Visibility {
        VISIBLE,
        INVISIBLE,
        GONE,
    }

    init {
        mFrames = IntArray(numOfFrames)
        val gap: Float = 100f / (mFrames.size + 1)
        for (i in mFrames.indices) {
            mFrames[i] = (i * gap + gap).toInt()
        }
    }

    fun getTarget(): Array<out String> {
        return mTarget
    }

    fun setTarget(target: Array<out String>) {
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

    fun getVisibility(): Array<out Visibility>? {
        return mVisibility
    }

    fun setVisibility(vararg visibility: Visibility) {
        mVisibility = visibility
    }

    fun getAlpha(): FloatArray? {
        return mAlpha
    }

    fun setAlpha(vararg alpha: Float) {
        mAlpha = alpha
    }

    fun getRotation(): FloatArray? {
        return mRotation
    }

    fun setRotation(vararg rotation: Float) {
        mRotation = rotation
    }

    fun getRotationX(): FloatArray? {
        return mRotationX
    }

    fun setRotationX(vararg rotationX: Float) {
        mRotationX = rotationX
    }

    fun getRotationY(): FloatArray? {
        return mRotationY
    }

    fun setRotationY(vararg rotationY: Float) {
        mRotationY = rotationY
    }

    fun getPivotX(): FloatArray? {
        return mPivotX
    }

    fun setPivotX(vararg pivotX: Float) {
        mPivotX = pivotX
    }

    fun getPivotY(): FloatArray? {
        return mPivotY
    }

    fun setPivotY(vararg pivotY: Float) {
        mPivotY = pivotY
    }

    fun getTransitionPathRotate(): FloatArray? {
        return mTransitionPathRotate
    }

    fun setTransitionPathRotate(vararg transitionPathRotate: Float) {
        mTransitionPathRotate = transitionPathRotate
    }

    fun getScaleX(): FloatArray? {
        return mScaleX
    }

    fun setScaleX(scaleX: FloatArray) {
        mScaleX = scaleX
    }

    fun getScaleY(): FloatArray? {
        return mScaleY
    }

    fun setScaleY(scaleY: FloatArray) {
        mScaleY = scaleY
    }

    fun getTranslationX(): FloatArray? {
        return mTranslationX
    }

    fun setTranslationX(translationX: FloatArray) {
        mTranslationX = translationX
    }

    fun getTranslationY(): FloatArray? {
        return mTranslationY
    }

    fun setTranslationY(translationY: FloatArray) {
        mTranslationY = translationY
    }

    fun getTranslationZ(): FloatArray? {
        return mTranslationZ
    }

    fun setTranslationZ(translationZ: FloatArray) {
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
        builder.append("frame:").append(mFrames.contentToString()).append(",\n")
        append(builder, "easing", mTransitionEasing)
        if (mCurveFit != null) {
            builder.append("fit:'").append(mCurveFit).append("',\n")
        }
        if (mVisibility != null) {
            builder.append("visibility:'").append(mVisibility.contentToString()).append("',\n")
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
