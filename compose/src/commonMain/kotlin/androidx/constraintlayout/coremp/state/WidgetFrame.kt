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

import androidx.constraintlayout.coremp.motion.CustomAttribute
import androidx.constraintlayout.coremp.motion.CustomVariable
import androidx.constraintlayout.coremp.motion.utils.TypedBundle
import androidx.constraintlayout.coremp.motion.utils.TypedValues
import androidx.constraintlayout.coremp.parser.CLElement
import androidx.constraintlayout.coremp.parser.CLKey
import androidx.constraintlayout.coremp.parser.CLNumber
import androidx.constraintlayout.coremp.parser.CLObject
import androidx.constraintlayout.coremp.parser.CLParsingException
import androidx.constraintlayout.coremp.widgets.ConstraintAnchor
import androidx.constraintlayout.coremp.widgets.ConstraintWidget
import kotlin.math.max

/**
 * Utility class to encapsulate layout of a widget
 */
class WidgetFrame {
    var widget: ConstraintWidget? = null
    var left = 0
    var top = 0
    var right = 0
    var bottom = 0

    // transforms

    // transforms
    var pivotX = Float.NaN
    var pivotY = Float.NaN

    var rotationX = Float.NaN
    var rotationY = Float.NaN
    var rotationZ = Float.NaN

    var translationX = Float.NaN
    var translationY = Float.NaN
    var translationZ = Float.NaN
    var phone_orientation = Float.NaN

    var scaleX = Float.NaN
    var scaleY = Float.NaN

    var alpha = Float.NaN
    var interpolatedPos = Float.NaN

    var visibility = ConstraintWidget.VISIBLE

    private val mCustom: HashMap<String, CustomVariable> = HashMap()

    var name: String? = null

    var mMotionProperties: TypedBundle? = null

    // @TODO: add description
    fun width(): Int {
        return max(0, right - left)
    }

    // @TODO: add description
    fun height(): Int {
        return max(0, bottom - top)
    }

    constructor()

    constructor(widget: ConstraintWidget) {
        this.widget = widget
    }

    constructor(frame: WidgetFrame) {
        widget = frame.widget
        left = frame.left
        top = frame.top
        right = frame.right
        bottom = frame.bottom
        updateAttributes(frame)
    }

    // @TODO: add description
    fun updateAttributes(frame: WidgetFrame?) {
        if (frame == null) {
            return
        }
        pivotX = frame.pivotX
        pivotY = frame.pivotY
        rotationX = frame.rotationX
        rotationY = frame.rotationY
        rotationZ = frame.rotationZ
        translationX = frame.translationX
        translationY = frame.translationY
        translationZ = frame.translationZ
        scaleX = frame.scaleX
        scaleY = frame.scaleY
        alpha = frame.alpha
        visibility = frame.visibility
        setMotionAttributes(frame.mMotionProperties)
        mCustom.clear()
        for (c in frame.mCustom.values) {
            mCustom[c.getName()] = c.copy()
        }
    }

    val isDefaultTransform: Boolean get() = rotationX.isNaN() &&
        rotationY.isNaN() &&
        rotationZ.isNaN() &&
        translationX.isNaN() &&
        translationY.isNaN() &&
        translationZ.isNaN() &&
        scaleX.isNaN() &&
        scaleY.isNaN() &&
        alpha.isNaN()

    // @TODO: add description
    fun centerX(): Float {
        return left + (right - left) / 2f
    }

    // @TODO: add description
    fun centerY(): Float {
        return top + (bottom - top) / 2f
    }

    // @TODO: add description
    fun update(): WidgetFrame {
        widget?.let { widget ->
            left = widget.left
            top = widget.top
            right = widget.right
            bottom = widget.bottom
            val frame: WidgetFrame = widget.frame
            updateAttributes(frame)
        }
        return this
    }

    // @TODO: add description
    fun update(widget: ConstraintWidget?): WidgetFrame {
        if (widget == null) {
            return this
        }
        this.widget = widget
        update()
        return this
    }

    /**
     * Return whether this WidgetFrame contains a custom property of the given name.
     */
    fun containsCustom(name: String): Boolean {
        return mCustom.containsKey(name)
    }

    // @TODO: add description
    fun addCustomColor(name: String, color: Int) {
        setCustomAttribute(name, TypedValues.Custom.TYPE_COLOR, color)
    }

    // @TODO: add description
    fun getCustomColor(name: String?): Int {
        return if (mCustom.containsKey(name)) {
            mCustom[name]!!.getColorValue()
        } else {
            -0x5578
        }
    }

    // @TODO: add description
    fun addCustomFloat(name: String, value: Float) {
        setCustomAttribute(name, TypedValues.Custom.TYPE_FLOAT, value)
    }

    // @TODO: add description
    fun getCustomFloat(name: String?): Float {
        return if (mCustom.containsKey(name)) {
            mCustom[name]!!.getFloatValue()
        } else {
            Float.NaN
        }
    }

    // @TODO: add description
    fun setCustomAttribute(name: String, type: Int, value: Float) {
        if (mCustom.containsKey(name)) {
            mCustom[name]!!.setFloatValue(value)
        } else {
            mCustom[name] = CustomVariable(name, type, value)
        }
    }

    // @TODO: add description
    fun setCustomAttribute(name: String, type: Int, value: Int) {
        if (mCustom.containsKey(name)) {
            mCustom[name]!!.setIntValue(value)
        } else {
            mCustom[name] = CustomVariable(name, type, value)
        }
    }

    // @TODO: add description
    fun setCustomAttribute(name: String, type: Int, value: Boolean) {
        if (mCustom.containsKey(name)) {
            mCustom[name]!!.setBooleanValue(value)
        } else {
            mCustom[name] = CustomVariable(name, type, value)
        }
    }

    // @TODO: add description
    fun setCustomAttribute(name: String?, type: Int, value: String) {
        if (mCustom.containsKey(name)) {
            mCustom[name]!!.setStringValue(value)
        } else {
            mCustom[name!!] = CustomVariable(name, type, value)
        }
    }

    /**
     * Get the custom attribute given Nam
     * @param name Name of the custom attribut
     * @return The customAttribute
     */
    fun getCustomAttribute(name: String): CustomVariable? {
        return mCustom[name]
    }

    /**
     * Get the known custom Attributes names
     * @return set of custom attribute names
     */
    fun getCustomAttributeNames(): Set<String> {
        return mCustom.keys
    }

    // @TODO: add description
    @Throws(CLParsingException::class)
    fun setValue(key: String?, value: CLElement): Boolean {
        when (key) {
            "pivotX" -> pivotX = value.float
            "pivotY" -> pivotY = value.float
            "rotationX" -> rotationX = value.float
            "rotationY" -> rotationY = value.float
            "rotationZ" -> rotationZ = value.float
            "translationX" -> translationX = value.float
            "translationY" -> translationY = value.float
            "translationZ" -> translationZ = value.float
            "scaleX" -> scaleX = value.float
            "scaleY" -> scaleY = value.float
            "alpha" -> alpha = value.float
            "interpolatedPos" -> interpolatedPos = value.float
            "phone_orientation" -> phone_orientation = value.float
            "top" -> top = value.getInt()
            "left" -> left = value.getInt()
            "right" -> right = value.getInt()
            "bottom" -> bottom = value.getInt()
            "custom" -> parseCustom(value)
            else -> return false
        }
        return true
    }

    val id: String get() = if (widget == null) {
        "unknown"
    } else {
        widget!!.stringId!!
    }

    @Throws(CLParsingException::class)
    fun parseCustom(custom: CLElement) {
        val obj = custom as CLObject
        val n = obj.size()
        for (i in 0 until n) {
            val tmp = obj[i]
            val k = tmp as CLKey
            val v = k.value
            val vStr = v.content()
            if (vStr.matches("#[0-9a-fA-F]+".toRegex())) {
                val color = vStr.substring(1).toInt(16)
                setCustomAttribute(name!!, TypedValues.Custom.TYPE_COLOR, color)
            } else if (v is CLNumber) {
                setCustomAttribute(name!!, TypedValues.Custom.TYPE_FLOAT, v.float)
            } else {
                setCustomAttribute(name, TypedValues.Custom.TYPE_STRING, vStr)
            }
        }
    }

    // @TODO: add description
    fun serialize(ret: StringBuilder): StringBuilder {
        return serialize(ret, false)
    }

    /**
     * If true also send the phone orientation
     */
    fun serialize(
        ret: StringBuilder,
        sendPhoneOrientation: Boolean,
    ): StringBuilder {
        val frame = this
        ret.append("{\n")
        add(ret, "left", frame.left)
        add(ret, "top", frame.top)
        add(ret, "right", frame.right)
        add(ret, "bottom", frame.bottom)
        add(ret, "pivotX", frame.pivotX)
        add(ret, "pivotY", frame.pivotY)
        add(ret, "rotationX", frame.rotationX)
        add(ret, "rotationY", frame.rotationY)
        add(ret, "rotationZ", frame.rotationZ)
        add(ret, "translationX", frame.translationX)
        add(ret, "translationY", frame.translationY)
        add(ret, "translationZ", frame.translationZ)
        add(ret, "scaleX", frame.scaleX)
        add(ret, "scaleY", frame.scaleY)
        add(ret, "alpha", frame.alpha)
        add(ret, "visibility", frame.visibility)
        add(ret, "interpolatedPos", frame.interpolatedPos)
        if (widget != null) {
            for (side in ConstraintAnchor.Type.entries) {
                serializeAnchor(ret, side)
            }
        }
        if (sendPhoneOrientation) {
            add(ret, "phone_orientation", phone_orientation)
        }
        if (sendPhoneOrientation) {
            add(ret, "phone_orientation", phone_orientation)
        }
        if (frame.mCustom.size != 0) {
            ret.append("custom : {\n")
            for (s in frame.mCustom.keys) {
                val value: CustomVariable = frame.mCustom[s]!!
                ret.append(s)
                ret.append(": ")
                when (value.getType()) {
                    TypedValues.Custom.TYPE_INT -> {
                        ret.append(value.getIntegerValue())
                        ret.append(",\n")
                    }

                    TypedValues.Custom.TYPE_FLOAT, TypedValues.Custom.TYPE_DIMENSION -> {
                        ret.append(value.getFloatValue())
                        ret.append(",\n")
                    }

                    TypedValues.Custom.TYPE_COLOR -> {
                        ret.append("'")
                        ret.append(CustomVariable.colorString(value.getIntegerValue()))
                        ret.append("',\n")
                    }

                    TypedValues.Custom.TYPE_STRING -> {
                        ret.append("'")
                        ret.append(value.getStringValue())
                        ret.append("',\n")
                    }

                    TypedValues.Custom.TYPE_BOOLEAN -> {
                        ret.append("'")
                        ret.append(value.getBooleanValue())
                        ret.append("',\n")
                    }
                }
            }
            ret.append("}\n")
        }
        ret.append("}\n")
        return ret
    }

    private fun serializeAnchor(ret: StringBuilder, type: ConstraintAnchor.Type) {
        val anchor = widget?.getAnchor(type)
        if (anchor?.mTarget == null) {
            return
        }
        ret.append("Anchor")
        ret.append(type.name)
        ret.append(": ['")
        val str = anchor.mTarget!!.owner.stringId
        ret.append(str ?: "#PARENT")
        ret.append("', '")
        ret.append(anchor.mTarget!!.type.name)
        ret.append("', '")
        ret.append(anchor.mMargin)
        ret.append("'],\n")
    }

    /**
     * For debugging only
     */
    fun printCustomAttributes() {
        var ss = Throwable().stackTraceToString()
        // val s = Throwable().stackTrace[1]
        // var ss = ".(" + s.fileName + ":" + s.lineNumber + ") " + s.methodName
        ss += " " + this.hashCode() % 1000
        ss += if (widget != null) {
            "/" + widget.hashCode() % 1000 + " "
        } else {
            "/NULL "
        }
        if (mCustom != null) {
            for (key in mCustom.keys) {
                println(ss + mCustom[key].toString())
            }
        }
    }

    /**
     * For debugging only
     */
    fun logv(str: String) {
        // val s = Throwable().stackTrace[1]
        // var ss = ".(" + s.fileName + ":" + s.lineNumber + ") " + s.methodName
        var ss = Throwable().stackTraceToString()

        ss += " " + this.hashCode() % 1000
        ss += if (widget != null) {
            "/" + widget.hashCode() % 1000
        } else {
            "/NULL"
        }
        println("$ss $str")
    }

    // @TODO: add description
    fun setCustomValue(valueAt: CustomAttribute?, mTempValues: FloatArray?) {}

    fun setMotionAttributes(motionProperties: TypedBundle?) {
        mMotionProperties = motionProperties
    }

    /**
     * get the property bundle associated with MotionAttributes
     *
     * @return the property bundle associated with MotionAttributes or null
     */
    fun getMotionProperties(): TypedBundle? {
        return mMotionProperties
    }

    companion object {
        // @TODO: add description
        fun interpolate(
            parentWidth: Int,
            parentHeight: Int,
            frame: WidgetFrame,
            start: WidgetFrame,
            end: WidgetFrame,
            transition: Transition,
            progress: Float,
        ) {
            val frameNumber = (progress * 100).toInt()
            var startX = start.left
            var startY = start.top
            var endX = end.left
            var endY = end.top
            var startWidth = start.right - start.left
            var startHeight = start.bottom - start.top
            var endWidth = end.right - end.left
            var endHeight = end.bottom - end.top

            var progressPosition = progress

            var startAlpha = start.alpha
            var endAlpha = end.alpha
            if (start.visibility == ConstraintWidget.GONE) {
                // On visibility gone, keep the same size to do an alpha to zero
                startX -= (endWidth / 2f).toInt()
                startY -= (endHeight / 2f).toInt()
                startWidth = endWidth
                startHeight = endHeight
                if (startAlpha.isNaN()) {
                    // override only if not defined...
                    startAlpha = 0f
                }
            }
            if (end.visibility == ConstraintWidget.GONE) {
                // On visibility gone, keep the same size to do an alpha to zero
                endX -= (startWidth / 2f).toInt()
                endY -= (startHeight / 2f).toInt()
                endWidth = startWidth
                endHeight = startHeight
                if (endAlpha.isNaN()) {
                    // override only if not defined...
                    endAlpha = 0f
                }
            }
            if (startAlpha.isNaN() && !endAlpha.isNaN()) {
                startAlpha = 1f
            }
            if (!startAlpha.isNaN() && endAlpha.isNaN()) {
                endAlpha = 1f
            }
            if (start.visibility == ConstraintWidget.INVISIBLE) {
                startAlpha = 0f
            }
            if (end.visibility == ConstraintWidget.INVISIBLE) {
                endAlpha = 0f
            }
            if (frame.widget != null && transition.hasPositionKeyframes()) {
                val firstPosition: Transition.KeyPosition? =
                    transition.findPreviousPosition(frame.widget!!.stringId!!, frameNumber)
                var lastPosition: Transition.KeyPosition? =
                    transition.findNextPosition(frame.widget!!.stringId!!, frameNumber)
                if (firstPosition == lastPosition) {
                    lastPosition = null
                }
                var interpolateStartFrame = 0
                var interpolateEndFrame = 100
                if (firstPosition != null) {
                    startX = (firstPosition.mX * parentWidth).toInt()
                    startY = (firstPosition.mY * parentHeight).toInt()
                    interpolateStartFrame = firstPosition.mFrame
                }
                if (lastPosition != null) {
                    endX = (lastPosition.mX * parentWidth).toInt()
                    endY = (lastPosition.mY * parentHeight).toInt()
                    interpolateEndFrame = lastPosition.mFrame
                }
                progressPosition = (
                    (progress * 100f - interpolateStartFrame) /
                        (interpolateEndFrame - interpolateStartFrame).toFloat()
                    )
            }
            frame.widget = start.widget
            frame.left = (startX + progressPosition * (endX - startX)).toInt()
            frame.top = (startY + progressPosition * (endY - startY)).toInt()
            val width = ((1 - progress) * startWidth + (progress * endWidth)).toInt()
            val height = ((1 - progress) * startHeight + (progress * endHeight)).toInt()
            frame.right = frame.left + width
            frame.bottom = frame.top + height

            frame.pivotX = interpolate(start.pivotX, end.pivotX, 0.5f, progress)
            frame.pivotY = interpolate(start.pivotY, end.pivotY, 0.5f, progress)

            frame.rotationX = interpolate(start.rotationX, end.rotationX, 0f, progress)
            frame.rotationY = interpolate(start.rotationY, end.rotationY, 0f, progress)
            frame.rotationZ = interpolate(start.rotationZ, end.rotationZ, 0f, progress)

            frame.scaleX = interpolate(start.scaleX, end.scaleX, 1f, progress)
            frame.scaleY = interpolate(start.scaleY, end.scaleY, 1f, progress)

            frame.translationX = interpolate(start.translationX, end.translationX, 0f, progress)
            frame.translationY = interpolate(start.translationY, end.translationY, 0f, progress)
            frame.translationZ = interpolate(start.translationZ, end.translationZ, 0f, progress)

            frame.alpha = interpolate(startAlpha, endAlpha, 1f, progress)
            val keys: Set<String> = end.mCustom.keys
            frame.mCustom.clear()
            for (key in keys) {
                if (start.mCustom.containsKey(key)) {
                    val startVariable: CustomVariable? = start.mCustom[key]
                    val endVariable: CustomVariable? = end.mCustom[key]
                    val interpolated = CustomVariable(startVariable!!)
                    frame.mCustom[key] = interpolated
                    if (startVariable.numberOfInterpolatedValues() == 1) {
                        interpolated.setValue(
                            interpolate(
                                startVariable.getValueToInterpolate(),
                                endVariable!!.getValueToInterpolate(),
                                0f,
                                progress,
                            ),
                        )
                    } else {
                        val n: Int = startVariable.numberOfInterpolatedValues()
                        val startValues = FloatArray(n)
                        val endValues = FloatArray(n)
                        startVariable.getValuesToInterpolate(startValues)
                        endVariable!!.getValuesToInterpolate(endValues)
                        for (i in 0 until n) {
                            startValues[i] = interpolate(startValues[i], endValues[i], 0f, progress)
                            interpolated.setValue(startValues)
                        }
                    }
                }
            }
        }

        private fun interpolate(
            start: Float,
            end: Float,
            defaultValue: Float,
            progress: Float,
        ): Float {
            var start = start
            var end = end
            val isStartUnset = start.isNaN()
            val isEndUnset = end.isNaN()
            if (isStartUnset && isEndUnset) {
                return Float.NaN
            }
            if (isStartUnset) {
                start = defaultValue
            }
            if (isEndUnset) {
                end = defaultValue
            }
            return start + progress * (end - start)
        }
    }

    private fun add(s: StringBuilder, title: String, value: Int) {
        s.append(title)
        s.append(": ")
        s.append(value)
        s.append(",\n")
    }

    private fun add(s: StringBuilder, title: String, value: Float) {
        if (value.isNaN()) {
            return
        }
        s.append(title)
        s.append(": ")
        s.append(value)
        s.append(",\n")
    }
}
