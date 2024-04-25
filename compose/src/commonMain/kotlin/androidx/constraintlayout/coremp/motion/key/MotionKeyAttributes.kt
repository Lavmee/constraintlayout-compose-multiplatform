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
package androidx.constraintlayout.coremp.motion.key

import androidx.constraintlayout.coremp.motion.CustomVariable
import androidx.constraintlayout.coremp.motion.utils.SplineSet
import androidx.constraintlayout.coremp.motion.utils.TypedValues
import androidx.constraintlayout.coremp.motion.utils.TypedValues.AttributesType

class MotionKeyAttributes : MotionKey() {
    @Suppress("unused")
    private var mTransitionEasing: String? = null
    private var mCurveFit = -1

    @Suppress("unused")
    private var mVisibility = 0
    private var mAlpha = Float.NaN
    private var mElevation = Float.NaN
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
    private var mProgress = Float.NaN

    init {
        mType = KEY_TYPE
        mCustom = HashMap()
    }

    override fun getAttributeNames(attributes: HashSet<String>) {
        if (!mAlpha.isNaN()) {
            attributes.add(AttributesType.S_ALPHA)
        }
        if (!mElevation.isNaN()) {
            attributes.add(AttributesType.S_ELEVATION)
        }
        if (!mRotation.isNaN()) {
            attributes.add(AttributesType.S_ROTATION_Z)
        }
        if (!mRotationX.isNaN()) {
            attributes.add(AttributesType.S_ROTATION_X)
        }
        if (!mRotationY.isNaN()) {
            attributes.add(AttributesType.S_ROTATION_Y)
        }
        if (!mPivotX.isNaN()) {
            attributes.add(AttributesType.S_PIVOT_X)
        }
        if (!mPivotY.isNaN()) {
            attributes.add(AttributesType.S_PIVOT_Y)
        }
        if (!mTranslationX.isNaN()) {
            attributes.add(AttributesType.S_TRANSLATION_X)
        }
        if (!mTranslationY.isNaN()) {
            attributes.add(AttributesType.S_TRANSLATION_Y)
        }
        if (!mTranslationZ.isNaN()) {
            attributes.add(AttributesType.S_TRANSLATION_Z)
        }
        if (!mTransitionPathRotate.isNaN()) {
            attributes.add(AttributesType.S_PATH_ROTATE)
        }
        if (!mScaleX.isNaN()) {
            attributes.add(AttributesType.S_SCALE_X)
        }
        if (!mScaleY.isNaN()) {
            attributes.add(AttributesType.S_SCALE_Y)
        }
        if (!mProgress.isNaN()) {
            attributes.add(AttributesType.S_PROGRESS)
        }
        if (mCustom!!.size > 0) {
            for (s in mCustom!!.keys) {
                attributes.add(TypedValues.S_CUSTOM + "," + s)
            }
        }
    }

    override fun addValues(splines: HashMap<String, SplineSet>) {
        for (s in splines.keys) {
            val splineSet: SplineSet = splines[s] ?: continue
            // TODO support custom
            if (s.startsWith(AttributesType.S_CUSTOM)) {
                val cKey = s.substring(AttributesType.S_CUSTOM.length + 1)
                val cValue: CustomVariable? = mCustom!![cKey]
                if (cValue != null) {
                    (splineSet as SplineSet.CustomSpline).setPoint(mFramePosition, cValue)
                }
                continue
            }
            when (s) {
                AttributesType.S_ALPHA -> if (!mAlpha.isNaN()) {
                    splineSet.setPoint(mFramePosition, mAlpha)
                }

                AttributesType.S_ELEVATION -> if (!mElevation.isNaN()) {
                    splineSet.setPoint(mFramePosition, mElevation)
                }

                AttributesType.S_ROTATION_Z -> if (!mRotation.isNaN()) {
                    splineSet.setPoint(mFramePosition, mRotation)
                }

                AttributesType.S_ROTATION_X -> if (!mRotationX.isNaN()) {
                    splineSet.setPoint(mFramePosition, mRotationX)
                }

                AttributesType.S_ROTATION_Y -> if (!mRotationY.isNaN()) {
                    splineSet.setPoint(mFramePosition, mRotationY)
                }

                AttributesType.S_PIVOT_X -> if (!mRotationX.isNaN()) {
                    splineSet.setPoint(mFramePosition, mPivotX)
                }

                AttributesType.S_PIVOT_Y -> if (!mRotationY.isNaN()) {
                    splineSet.setPoint(mFramePosition, mPivotY)
                }

                AttributesType.S_PATH_ROTATE -> if (!mTransitionPathRotate.isNaN()) {
                    splineSet.setPoint(mFramePosition, mTransitionPathRotate)
                }

                AttributesType.S_SCALE_X -> if (!mScaleX.isNaN()) {
                    splineSet.setPoint(mFramePosition, mScaleX)
                }

                AttributesType.S_SCALE_Y -> if (!mScaleY.isNaN()) {
                    splineSet.setPoint(mFramePosition, mScaleY)
                }

                AttributesType.S_TRANSLATION_X -> if (!mTranslationX.isNaN()) {
                    splineSet.setPoint(mFramePosition, mTranslationX)
                }

                AttributesType.S_TRANSLATION_Y -> if (!mTranslationY.isNaN()) {
                    splineSet.setPoint(mFramePosition, mTranslationY)
                }

                AttributesType.S_TRANSLATION_Z -> if (!mTranslationZ.isNaN()) {
                    splineSet.setPoint(mFramePosition, mTranslationZ)
                }

                AttributesType.S_PROGRESS -> if (!mProgress.isNaN()) {
                    splineSet.setPoint(mFramePosition, mProgress)
                }

                else -> println("not supported by KeyAttributes $s")
            }
        }
    }

    override fun clone(): MotionKey? {
        return null
    }

    // @TODO: add description
    override fun setValue(type: Int, value: Int): Boolean {
        when (type) {
            AttributesType.TYPE_VISIBILITY -> mVisibility = value
            AttributesType.TYPE_CURVE_FIT -> mCurveFit = value
            TypedValues.TYPE_FRAME_POSITION -> mFramePosition = value
            else -> if (!setValue(type, value)) {
                return super.setValue(type, value)
            }
        }
        return true
    }

    // @TODO: add description
    override fun setValue(type: Int, value: Float): Boolean {
        when (type) {
            AttributesType.TYPE_ALPHA -> mAlpha = value
            AttributesType.TYPE_TRANSLATION_X -> mTranslationX = value
            AttributesType.TYPE_TRANSLATION_Y -> mTranslationY = value
            AttributesType.TYPE_TRANSLATION_Z -> mTranslationZ = value
            AttributesType.TYPE_ELEVATION -> mElevation = value
            AttributesType.TYPE_ROTATION_X -> mRotationX = value
            AttributesType.TYPE_ROTATION_Y -> mRotationY = value
            AttributesType.TYPE_ROTATION_Z -> mRotation = value
            AttributesType.TYPE_SCALE_X -> mScaleX = value
            AttributesType.TYPE_SCALE_Y -> mScaleY = value
            AttributesType.TYPE_PIVOT_X -> mPivotX = value
            AttributesType.TYPE_PIVOT_Y -> mPivotY = value
            AttributesType.TYPE_PROGRESS -> mProgress = value
            AttributesType.TYPE_PATH_ROTATE -> mTransitionPathRotate = value
            TypedValues.TYPE_FRAME_POSITION -> mTransitionPathRotate = value
            else -> return super.setValue(type, value)
        }
        return true
    }

    // @TODO: add description
    override fun setInterpolation(interpolation: HashMap<String?, Int?>) {
        if (!mAlpha.isNaN()) {
            interpolation[AttributesType.S_ALPHA] = mCurveFit
        }
        if (!mElevation.isNaN()) {
            interpolation[AttributesType.S_ELEVATION] = mCurveFit
        }
        if (!mRotation.isNaN()) {
            interpolation[AttributesType.S_ROTATION_Z] = mCurveFit
        }
        if (!mRotationX.isNaN()) {
            interpolation[AttributesType.S_ROTATION_X] = mCurveFit
        }
        if (!mRotationY.isNaN()) {
            interpolation[AttributesType.S_ROTATION_Y] = mCurveFit
        }
        if (!mPivotX.isNaN()) {
            interpolation[AttributesType.S_PIVOT_X] = mCurveFit
        }
        if (!mPivotY.isNaN()) {
            interpolation[AttributesType.S_PIVOT_Y] = mCurveFit
        }
        if (!mTranslationX.isNaN()) {
            interpolation[AttributesType.S_TRANSLATION_X] = mCurveFit
        }
        if (!mTranslationY.isNaN()) {
            interpolation[AttributesType.S_TRANSLATION_Y] = mCurveFit
        }
        if (!mTranslationZ.isNaN()) {
            interpolation[AttributesType.S_TRANSLATION_Z] = mCurveFit
        }
        if (!mTransitionPathRotate.isNaN()) {
            interpolation[AttributesType.S_PATH_ROTATE] = mCurveFit
        }
        if (!mScaleX.isNaN()) {
            interpolation[AttributesType.S_SCALE_X] = mCurveFit
        }
        if (!mScaleY.isNaN()) {
            interpolation[AttributesType.S_SCALE_Y] = mCurveFit
        }
        if (!mProgress.isNaN()) {
            interpolation[AttributesType.S_PROGRESS] = mCurveFit
        }
        if (mCustom!!.size > 0) {
            for (s in mCustom!!.keys) {
                interpolation[AttributesType.S_CUSTOM + "," + s] = mCurveFit
            }
        }
    }

    // @TODO: add description
    override fun setValue(type: Int, value: String): Boolean {
        when (type) {
            AttributesType.TYPE_EASING -> mTransitionEasing = value
            TypedValues.TYPE_TARGET -> mTargetString = value
            else -> return super.setValue(type, value)
        }
        return true
    }

    override fun getId(name: String?): Int {
        return AttributesType.getId(name)
    }

    fun getCurveFit(): Int {
        return mCurveFit
    }

    // @TODO: add description
    fun printAttributes() {
        val nameSet = HashSet<String>()
        getAttributeNames(nameSet)
        println(" ------------- $mFramePosition -------------")
        val names = nameSet.toTypedArray()
        for (i in names.indices) {
            val id = AttributesType.getId(names[i])
            println(names[i] + ":" + getFloatValue(id))
        }
    }

    private fun getFloatValue(id: Int): Float {
        return when (id) {
            AttributesType.TYPE_ALPHA -> mAlpha
            AttributesType.TYPE_TRANSLATION_X -> mTranslationX
            AttributesType.TYPE_TRANSLATION_Y -> mTranslationY
            AttributesType.TYPE_TRANSLATION_Z -> mTranslationZ
            AttributesType.TYPE_ELEVATION -> mElevation
            AttributesType.TYPE_ROTATION_X -> mRotationX
            AttributesType.TYPE_ROTATION_Y -> mRotationY
            AttributesType.TYPE_ROTATION_Z -> mRotation
            AttributesType.TYPE_SCALE_X -> mScaleX
            AttributesType.TYPE_SCALE_Y -> mScaleY
            AttributesType.TYPE_PIVOT_X -> mPivotX
            AttributesType.TYPE_PIVOT_Y -> mPivotY
            AttributesType.TYPE_PROGRESS -> mProgress
            AttributesType.TYPE_PATH_ROTATE -> mTransitionPathRotate
            TypedValues.TYPE_FRAME_POSITION -> mFramePosition.toFloat()
            else -> Float.NaN
        }
    }

    companion object {
        const val NAME = "KeyAttribute"
        private const val TAG = "KeyAttributes"

        @Suppress("unused")
        private val DEBUG = false

        const val KEY_TYPE = 1
    }
}
