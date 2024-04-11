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

import androidx.constraintlayout.coremp.ext.Math
import androidx.constraintlayout.coremp.ext.roundToIntOrZero
import androidx.constraintlayout.coremp.motion.utils.TypedValues
import kotlin.math.pow

class CustomVariable {

    var mName: String
    private var mType = 0
    private var mIntegerValue = Int.MIN_VALUE
    private var mFloatValue = Float.NaN
    private var mStringValue: String? = null
    var mBooleanValue: Boolean = false

    // @TODO: add description
    fun copy(): CustomVariable {
        return CustomVariable(this)
    }

    constructor(c: CustomVariable) {
        mName = c.mName
        mType = c.mType
        mIntegerValue = c.mIntegerValue
        mFloatValue = c.mFloatValue
        mStringValue = c.mStringValue
        mBooleanValue = c.mBooleanValue
    }

    constructor(name: String, type: Int, value: String) {
        mName = name
        mType = type
        mStringValue = value
    }

    constructor(name: String, type: Int, value: Int) {
        mName = name
        mType = type
        if (type == TypedValues.Custom.TYPE_FLOAT) { // catch int ment for float
            mFloatValue = value.toFloat()
        } else {
            mIntegerValue = value
        }
    }

    constructor(name: String, type: Int, value: Float) {
        mName = name
        mType = type
        mFloatValue = value
    }

    constructor(name: String, type: Int, value: Boolean) {
        mName = name
        mType = type
        mBooleanValue = value
    }

    override fun toString(): String {
        val str = "$mName:"
        when (mType) {
            TypedValues.Custom.TYPE_INT -> return str + mIntegerValue
            TypedValues.Custom.TYPE_FLOAT -> return str + mFloatValue
            TypedValues.Custom.TYPE_COLOR -> return str + colorString(
                mIntegerValue,
            )
            TypedValues.Custom.TYPE_STRING -> return str + mStringValue
            TypedValues.Custom.TYPE_BOOLEAN -> return str + mBooleanValue
            TypedValues.Custom.TYPE_DIMENSION -> return str + mFloatValue
        }
        return "$str????"
    }

    fun getType(): Int {
        return mType
    }

    fun getBooleanValue(): Boolean {
        return mBooleanValue
    }

    fun getFloatValue(): Float {
        return mFloatValue
    }

    fun getColorValue(): Int {
        return mIntegerValue
    }

    fun getIntegerValue(): Int {
        return mIntegerValue
    }

    fun getStringValue(): String? {
        return mStringValue
    }

    /**
     * Continuous types are interpolated they are fired only at
     */
    fun isContinuous(): Boolean {
        return when (mType) {
            TypedValues.Custom.TYPE_REFERENCE, TypedValues.Custom.TYPE_BOOLEAN, TypedValues.Custom.TYPE_STRING -> false
            else -> true
        }
    }

    fun setFloatValue(value: Float) {
        mFloatValue = value
    }

    fun setBooleanValue(value: Boolean) {
        mBooleanValue = value
    }

    fun setIntValue(value: Int) {
        mIntegerValue = value
    }

    fun setStringValue(value: String) {
        mStringValue = value
    }

    /**
     * The number of interpolation values that need to be interpolated
     * Typically 1 but 3 for colors.
     *
     * @return Typically 1 but 3 for colors.
     */
    fun numberOfInterpolatedValues(): Int {
        return when (mType) {
            TypedValues.Custom.TYPE_COLOR -> 4
            else -> 1
        }
    }

    /**
     * Transforms value to a float for the purpose of interpolation
     *
     * @return interpolation value
     */
    fun getValueToInterpolate(): Float {
        when (mType) {
            TypedValues.Custom.TYPE_INT -> return mIntegerValue.toFloat()
            TypedValues.Custom.TYPE_FLOAT -> return mFloatValue
            TypedValues.Custom.TYPE_COLOR -> throw RuntimeException("Color does not have a single color to interpolate")
            TypedValues.Custom.TYPE_STRING -> throw RuntimeException("Cannot interpolate String")
            TypedValues.Custom.TYPE_BOOLEAN -> return if (mBooleanValue) 1f else 0f
            TypedValues.Custom.TYPE_DIMENSION -> return mFloatValue
        }
        return Float.NaN
    }

    // @TODO: add description
    fun getValuesToInterpolate(ret: FloatArray) {
        when (mType) {
            TypedValues.Custom.TYPE_INT -> ret[0] = mIntegerValue.toFloat()
            TypedValues.Custom.TYPE_FLOAT -> ret[0] = mFloatValue
            TypedValues.Custom.TYPE_COLOR -> {
                val a = 0xFF and (mIntegerValue shr 24)
                val r = 0xFF and (mIntegerValue shr 16)
                val g = 0xFF and (mIntegerValue shr 8)
                val b = 0xFF and mIntegerValue

                val f_r = Math.pow((r / 255.0f).toDouble(), 2.2).toFloat()
                val f_g = Math.pow((g / 255.0f).toDouble(), 2.2).toFloat()
                val f_b = Math.pow((b / 255.0f).toDouble(), 2.2).toFloat()
                ret[0] = f_r
                ret[1] = f_g
                ret[2] = f_b
                ret[3] = a / 255f
            }

            TypedValues.Custom.TYPE_STRING -> throw RuntimeException("Cannot interpolate String")
            TypedValues.Custom.TYPE_BOOLEAN -> ret[0] = (if (mBooleanValue) 1 else 0).toFloat()
            TypedValues.Custom.TYPE_DIMENSION -> ret[0] = mFloatValue
        }
    }

    // @TODO: add description
    fun setValue(value: FloatArray) {
        when (mType) {
            TypedValues.Custom.TYPE_REFERENCE, TypedValues.Custom.TYPE_INT ->
                mIntegerValue =
                    value[0].toInt()

            TypedValues.Custom.TYPE_FLOAT, TypedValues.Custom.TYPE_DIMENSION ->
                mFloatValue =
                    value[0]

            TypedValues.Custom.TYPE_COLOR -> {
                val f_r = value[0]
                val f_g = value[1]
                val f_b = value[2]
                val r = 0xFF and (f_r.toDouble().pow(1.0 / 2.0).toFloat() * 255.0f).roundToIntOrZero()
                val g = 0xFF and (f_g.toDouble().pow(1.0 / 2.0).toFloat() * 255.0f).roundToIntOrZero()
                val b = 0xFF and (f_b.toDouble().pow(1.0 / 2.0).toFloat() * 255.0f).roundToIntOrZero()
                val a = 0xFF and (value[3] * 255.0f).roundToIntOrZero()
                mIntegerValue = a shl 24 or (r shl 16) or (g shl 8) or b
            }

            TypedValues.Custom.TYPE_STRING -> throw RuntimeException("Cannot interpolate String")
            TypedValues.Custom.TYPE_BOOLEAN -> mBooleanValue = value[0] > 0.5
        }
    }

    /**
     * test if the two attributes are different
     */
    fun diff(customAttribute: CustomVariable?): Boolean {
        if (customAttribute == null || mType != customAttribute.mType) {
            return false
        }
        when (mType) {
            TypedValues.Custom.TYPE_INT, TypedValues.Custom.TYPE_REFERENCE -> return mIntegerValue == customAttribute.mIntegerValue
            TypedValues.Custom.TYPE_FLOAT -> return mFloatValue == customAttribute.mFloatValue
            TypedValues.Custom.TYPE_COLOR -> return mIntegerValue == customAttribute.mIntegerValue
            TypedValues.Custom.TYPE_STRING -> return mIntegerValue == customAttribute.mIntegerValue
            TypedValues.Custom.TYPE_BOOLEAN -> return mBooleanValue == customAttribute.mBooleanValue
            TypedValues.Custom.TYPE_DIMENSION -> return mFloatValue == customAttribute.mFloatValue
        }
        return false
    }

    constructor(name: String, attributeType: Int) {
        mName = name
        mType = attributeType
    }

    constructor(name: String, attributeType: Int, value: Any) {
        mName = name
        mType = attributeType
        setValue(value)
    }

    constructor(source: CustomVariable, value: Any) {
        mName = source.mName
        mType = source.mType
        setValue(value)
    }

    // @TODO: add description
    fun setValue(value: Any) {
        when (mType) {
            TypedValues.Custom.TYPE_REFERENCE, TypedValues.Custom.TYPE_INT ->
                mIntegerValue =
                    value as Int

            TypedValues.Custom.TYPE_FLOAT -> mFloatValue = value as Float
            TypedValues.Custom.TYPE_COLOR -> mIntegerValue = value as Int
            TypedValues.Custom.TYPE_STRING -> mStringValue = value as String
            TypedValues.Custom.TYPE_BOOLEAN -> mBooleanValue = value as Boolean
            TypedValues.Custom.TYPE_DIMENSION -> mFloatValue = value as Float
        }
    }

    // @TODO: add description
    fun getInterpolatedColor(value: FloatArray): Int {
        val r = clamp(
            (value[0].toDouble().pow(1.0 / 2.2).toFloat() * 255.0f).toInt(),
        )
        val g = clamp(
            (value[1].toDouble().pow(1.0 / 2.2).toFloat() * 255.0f).toInt(),
        )
        val b = clamp(
            (value[2].toDouble().pow(1.0 / 2.2).toFloat() * 255.0f).toInt(),
        )
        val a = clamp((value[3] * 255.0f).toInt())
        return a shl 24 or (r shl 16) or (g shl 8) or b
    }

    // @TODO: add description
    fun setInterpolatedValue(view: MotionWidget, value: FloatArray) {
        when (mType) {
            TypedValues.Custom.TYPE_INT -> view.setCustomAttribute(mName, mType, value[0].toInt())
            TypedValues.Custom.TYPE_COLOR -> {
                val r = clamp(
                    (
                        Math.pow(
                            value[0].toDouble(),
                            1.0 / 2.2,
                        ).toFloat() * 255.0f
                        ).toInt(),
                )
                val g = clamp(
                    (
                        Math.pow(
                            value[1].toDouble(),
                            1.0 / 2.2,
                        ).toFloat() * 255.0f
                        ).toInt(),
                )
                val b = clamp(
                    (
                        Math.pow(
                            value[2].toDouble(),
                            1.0 / 2.2,
                        ).toFloat() * 255.0f
                        ).toInt(),
                )
                val a =
                    clamp((value[3] * 255.0f).toInt())
                val color = a shl 24 or (r shl 16) or (g shl 8) or b
                view.setCustomAttribute(mName, mType, color)
            }

            TypedValues.Custom.TYPE_REFERENCE, TypedValues.Custom.TYPE_STRING -> throw RuntimeException(
                "unable to interpolate $mName",
            )

            TypedValues.Custom.TYPE_BOOLEAN -> view.setCustomAttribute(
                mName,
                mType,
                value[0] > 0.5f,
            )

            TypedValues.Custom.TYPE_DIMENSION, TypedValues.Custom.TYPE_FLOAT -> view.setCustomAttribute(
                mName,
                mType,
                value[0],
            )
        }
    }

    // @TODO: add description
    fun applyToWidget(view: MotionWidget) {
        when (mType) {
            TypedValues.Custom.TYPE_INT, TypedValues.Custom.TYPE_COLOR, TypedValues.Custom.TYPE_REFERENCE -> view.setCustomAttribute(
                mName,
                mType,
                mIntegerValue,
            )

            TypedValues.Custom.TYPE_STRING -> view.setCustomAttribute(mName, mType, mStringValue.toString())
            TypedValues.Custom.TYPE_BOOLEAN -> view.setCustomAttribute(mName, mType, mBooleanValue)
            TypedValues.Custom.TYPE_DIMENSION, TypedValues.Custom.TYPE_FLOAT -> view.setCustomAttribute(
                mName,
                mType,
                mFloatValue,
            )
        }
    }

    fun getName(): String {
        return mName
    }

    companion object {
        private const val TAG = "TransitionLayout"

        // @TODO: add description
        @OptIn(ExperimentalStdlibApi::class)
        fun colorString(v: Int): String {
            val str = "00000000" + v.toHexString()
            return "#" + str.substring(str.length - 8)
        }

        // @TODO: add description
        fun hsvToRgb(hue: Float, saturation: Float, value: Float): Int {
            val h = (hue * 6).toInt()
            val f = hue * 6 - h
            val p = (0.5f + 255 * value * (1 - saturation)).toInt()
            val q = (0.5f + 255 * value * (1 - f * saturation)).toInt()
            val t = (0.5f + 255 * value * (1 - (1 - f) * saturation)).toInt()
            val v = (0.5f + 255 * value).toInt()
            when (h) {
                0 -> return -0x1000000 or (v shl 16) + (t shl 8) + p
                1 -> return -0x1000000 or (q shl 16) + (v shl 8) + p
                2 -> return -0x1000000 or (p shl 16) + (v shl 8) + t
                3 -> return -0x1000000 or (p shl 16) + (q shl 8) + v
                4 -> return -0x1000000 or (t shl 16) + (p shl 8) + v
                5 -> return -0x1000000 or (v shl 16) + (p shl 8) + q
            }
            return 0
        }

        private fun clamp(c: Int): Int {
            var c = c
            val n = 255
            c = c and (c shr 31).inv()
            c -= n
            c = c and (c shr 31)
            c += n
            return c
        }

        // @TODO: add description
        fun rgbaTocColor(
            r: Float,
            g: Float,
            b: Float,
            a: Float,
        ): Int {
            val ir = clamp((r * 255f).toInt())
            val ig = clamp((g * 255f).toInt())
            val ib = clamp((b * 255f).toInt())
            val ia = clamp((a * 255f).toInt())
            return ia shl 24 or (ir shl 16) or (ig shl 8) or ib
        }
    }
}
