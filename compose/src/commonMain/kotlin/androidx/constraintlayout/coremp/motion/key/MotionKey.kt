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

abstract class MotionKey : TypedValues {

    var mFramePosition = UNSET
    var mTargetId = UNSET
    var mTargetString: String? = null
    var mType = 0
    var mCustom: HashMap<String, CustomVariable>? = null

    // @TODO: add description
    abstract fun getAttributeNames(attributes: HashSet<String>)

    open fun matches(constraintTag: String?): Boolean {
        return if (mTargetString == null || constraintTag == null) {
            false
        } else {
            constraintTag.matches(
                mTargetString!!.toRegex(),
            )
        }
    }

    /**
     * Defines method to add a a view to splines derived form this key frame.
     * The values are written to the spline
     *
     * @param splines splines to write values to
     */
    abstract fun addValues(splines: HashMap<String, SplineSet>)

    /**
     * Return the float given a value. If the value is a "Float" object it is casted
     *
     */
    open fun toFloat(value: Any): Float {
        return if (value is Float) value else value.toString().toFloat()
    }

    /**
     * Return the int version of an object if the value is an Integer object it is casted.
     *
     *
     */
    open fun toInt(value: Any): Int {
        return if (value is Int) value else value.toString().toInt()
    }

    /**
     * Return the boolean version this object if the object is a Boolean it is casted.
     *
     *
     */
    open fun toBoolean(value: Any): Boolean {
        return if (value is Boolean) value else value.toString().toBooleanStrict()
    }

    /**
     * Key frame can specify the type of interpolation it wants on various attributes
     * For each string it set it to -1, CurveFit.LINEAR or  CurveFit.SPLINE
     */
    open fun setInterpolation(interpolation: HashMap<String?, Int?>) {}

    // @TODO: add description
    open fun copy(src: MotionKey): MotionKey {
        mFramePosition = src.mFramePosition
        mTargetId = src.mTargetId
        mTargetString = src.mTargetString
        mType = src.mType
        return this
    }

    // @TODO: add description
    abstract fun clone(): MotionKey?

    // @TODO: add description
    open fun setViewId(id: Int): MotionKey {
        mTargetId = id
        return this
    }

    /**
     * sets the frame position
     */
    open fun setFramePosition(pos: Int) {
        mFramePosition = pos
    }

    /**
     * Gets the current frame position
     */
    open fun getFramePosition(): Int {
        return mFramePosition
    }

    // @TODO: add description
    override fun setValue(type: Int, value: Int): Boolean {
        when (type) {
            TypedValues.TYPE_FRAME_POSITION -> {
                mFramePosition = value
                return true
            }
        }
        return false
    }

    // @TODO: add description
    override fun setValue(type: Int, value: Float): Boolean {
        return false
    }

    // @TODO: add description
    override fun setValue(type: Int, value: String): Boolean {
        when (type) {
            TypedValues.TYPE_TARGET -> {
                mTargetString = value
                return true
            }
        }
        return false
    }

    // @TODO: add description
    override fun setValue(type: Int, value: Boolean): Boolean {
        return false
    }

    // @TODO: add description
    open fun setCustomAttribute(name: String?, type: Int, value: Float) {
        mCustom!![name!!] = CustomVariable(name, type, value)
    }

    // @TODO: add description
    open fun setCustomAttribute(name: String?, type: Int, value: Int) {
        mCustom!![name!!] = CustomVariable(name, type, value)
    }

    // @TODO: add description
    open fun setCustomAttribute(name: String?, type: Int, value: Boolean) {
        mCustom!![name!!] = CustomVariable(name, type, value)
    }

    // @TODO: add description
    open fun setCustomAttribute(name: String?, type: Int, value: String) {
        mCustom!![name!!] = CustomVariable(name, type, value)
    }

    companion object {
        const val UNSET = -1

        const val ALPHA = "alpha"
        const val ELEVATION = "elevation"
        const val ROTATION = "rotationZ"
        const val ROTATION_X = "rotationX"

        const val TRANSITION_PATH_ROTATE = "transitionPathRotate"
        const val SCALE_X = "scaleX"
        const val SCALE_Y = "scaleY"

        const val TRANSLATION_X = "translationX"
        const val TRANSLATION_Y = "translationY"

        const val CUSTOM = "CUSTOM"

        const val VISIBILITY = "visibility"
    }
}
