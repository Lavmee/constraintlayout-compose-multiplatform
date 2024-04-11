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

import androidx.constraintlayout.coremp.motion.CustomVariable
import androidx.constraintlayout.coremp.motion.utils.TypedBundle
import androidx.constraintlayout.coremp.motion.utils.TypedValues
import androidx.constraintlayout.coremp.parser.CLArray
import androidx.constraintlayout.coremp.parser.CLContainer
import androidx.constraintlayout.coremp.parser.CLKey
import androidx.constraintlayout.coremp.parser.CLNumber
import androidx.constraintlayout.coremp.parser.CLObject
import androidx.constraintlayout.coremp.parser.CLParsingException
import androidx.constraintlayout.coremp.state.ConstraintSetParser.Companion.parseColorString

class TransitionParser {

    companion object {

        @Deprecated("")
        @Throws(CLParsingException::class)
        fun parse(json: CLObject, transition: Transition, dpToPixel: CorePixelDp?) {
            parse(json, transition)
        }

        /**
         * Parse a JSON string of a Transition and insert it into the Transition object
         *
         * @param json       Transition Object to parse.
         * @param transition Transition Object to write transition to
         */
        // @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        @Throws(CLParsingException::class)
        fun parse(json: CLObject, transition: Transition) {
            transition.resetProperties()
            val pathMotionArc = json.getStringOrNull("pathMotionArc")
            val bundle = TypedBundle()
            var setBundle = false
            if (pathMotionArc != null) {
                setBundle = true
                when (pathMotionArc) {
                    "none" -> bundle.add(TypedValues.PositionType.TYPE_PATH_MOTION_ARC, 0)
                    "startVertical" -> bundle.add(TypedValues.PositionType.TYPE_PATH_MOTION_ARC, 1)
                    "startHorizontal" -> bundle.add(
                        TypedValues.PositionType.TYPE_PATH_MOTION_ARC,
                        2,
                    )

                    "flip" -> bundle.add(TypedValues.PositionType.TYPE_PATH_MOTION_ARC, 3)
                    "below" -> bundle.add(TypedValues.PositionType.TYPE_PATH_MOTION_ARC, 4)
                    "above" -> bundle.add(TypedValues.PositionType.TYPE_PATH_MOTION_ARC, 5)
                }
            }
            // TODO: Add duration
            val interpolator = json.getStringOrNull("interpolator")
            if (interpolator != null) {
                setBundle = true
                bundle.add(TypedValues.TransitionType.TYPE_INTERPOLATOR, interpolator)
            }
            val staggered = json.getFloatOrNaN("staggered")
            if (!staggered.isNaN()) {
                setBundle = true
                bundle.add(TypedValues.TransitionType.TYPE_STAGGERED, staggered)
            }
            if (setBundle) {
                transition.setTransitionProperties(bundle)
            }
            val onSwipe: CLContainer? = json.getObjectOrNull("onSwipe")
            onSwipe?.let { parseOnSwipe(it, transition) }
            parseKeyFrames(json, transition)
        }

        private fun parseOnSwipe(onSwipe: CLContainer, transition: Transition) {
            val anchor = onSwipe.getStringOrNull("anchor")
            val side: Int = map(onSwipe.getStringOrNull("side"), *Transition.OnSwipe.SIDES)
            val direction: Int = map(
                onSwipe.getStringOrNull("direction"),
                *Transition.OnSwipe.DIRECTIONS,
            )
            val scale = onSwipe.getFloatOrNaN("scale")
            val threshold = onSwipe.getFloatOrNaN("threshold")
            val maxVelocity = onSwipe.getFloatOrNaN("maxVelocity")
            val maxAccel = onSwipe.getFloatOrNaN("maxAccel")
            val limitBounds = onSwipe.getStringOrNull("limitBounds")
            val autoCompleteMode: Int =
                map(onSwipe.getStringOrNull("mode"), *Transition.OnSwipe.MODE)
            val touchUp: Int = map(onSwipe.getStringOrNull("touchUp"), *Transition.OnSwipe.TOUCH_UP)
            val springMass = onSwipe.getFloatOrNaN("springMass")
            val springStiffness = onSwipe.getFloatOrNaN("springStiffness")
            val springDamping = onSwipe.getFloatOrNaN("springDamping")
            val stopThreshold = onSwipe.getFloatOrNaN("stopThreshold")
            val springBoundary: Int = map(
                onSwipe.getStringOrNull("springBoundary"),
                *Transition.OnSwipe.BOUNDARY,
            )
            val around = onSwipe.getStringOrNull("around")
            val swipe: Transition.OnSwipe = transition.createOnSwipe()
            swipe.setAnchorId(anchor)
            swipe.setAnchorSide(side)
            swipe.setDragDirection(direction)
            swipe.setDragScale(scale)
            swipe.setDragThreshold(threshold)
            swipe.setMaxVelocity(maxVelocity)
            swipe.setMaxAcceleration(maxAccel)
            swipe.setLimitBoundsTo(limitBounds)
            swipe.setAutoCompleteMode(autoCompleteMode)
            swipe.setOnTouchUp(touchUp)
            swipe.setSpringMass(springMass)
            swipe.setSpringStiffness(springStiffness)
            swipe.setSpringDamping(springDamping)
            swipe.setSpringStopThreshold(stopThreshold)
            swipe.setSpringBoundary(springBoundary)
            swipe.setRotationCenterId(around)
        }

        private fun map(value: String?, vararg types: String): Int {
            for (i in types.indices) {
                if (types[i] == value) {
                    return i
                }
            }
            return 0
        }

        private fun map(bundle: TypedBundle, type: Int, value: String, vararg types: String) {
            for (i in types.indices) {
                if (types[i] == value) {
                    bundle.add(type, i)
                }
            }
        }

        /**
         * Parses `KeyFrames` attributes from the [CLObject] into [Transition].
         *
         * @param transitionCLObject the CLObject for the root transition json
         * @param transition         core object that holds the state of the Transition
         */
        @Throws(CLParsingException::class)
        fun parseKeyFrames(transitionCLObject: CLObject, transition: Transition) {
            val keyframes = transitionCLObject.getObjectOrNull("KeyFrames") ?: return
            val keyPositions = keyframes.getArrayOrNull("KeyPositions")
            if (keyPositions != null) {
                for (i in 0 until keyPositions.size()) {
                    val keyPosition = keyPositions[i]
                    if (keyPosition is CLObject) {
                        parseKeyPosition(keyPosition, transition)
                    }
                }
            }
            val keyAttributes = keyframes.getArrayOrNull("KeyAttributes")
            if (keyAttributes != null) {
                for (i in 0 until keyAttributes.size()) {
                    val keyAttribute = keyAttributes[i]
                    if (keyAttribute is CLObject) {
                        parseKeyAttribute(keyAttribute, transition)
                    }
                }
            }
            val keyCycles = keyframes.getArrayOrNull("KeyCycles")
            if (keyCycles != null) {
                for (i in 0 until keyCycles.size()) {
                    val keyCycle = keyCycles[i]
                    if (keyCycle is CLObject) {
                        parseKeyCycle(keyCycle, transition)
                    }
                }
            }
        }

        @Throws(CLParsingException::class)
        private fun parseKeyPosition(
            keyPosition: CLObject,
            transition: Transition,
        ) {
            val bundle = TypedBundle()
            val targets = keyPosition.getArray("target")
            val frames = keyPosition.getArray("frames")
            val percentX = keyPosition.getArrayOrNull("percentX")
            val percentY = keyPosition.getArrayOrNull("percentY")
            val percentWidth = keyPosition.getArrayOrNull("percentWidth")
            val percentHeight = keyPosition.getArrayOrNull("percentHeight")
            val pathMotionArc = keyPosition.getStringOrNull("pathMotionArc")
            val transitionEasing = keyPosition.getStringOrNull("transitionEasing")
            val curveFit = keyPosition.getStringOrNull("curveFit")
            var type = keyPosition.getStringOrNull("type")
            if (type == null) {
                type = "parentRelative"
            }
            if (percentX != null && frames.size() != percentX.size()) {
                return
            }
            if (percentY != null && frames.size() != percentY.size()) {
                return
            }
            for (i in 0 until targets.size()) {
                val target = targets.getString(i)
                val pos_type = map(type, "deltaRelative", "pathRelative", "parentRelative")
                bundle.clear()
                bundle.add(TypedValues.PositionType.TYPE_POSITION_TYPE, pos_type)
                if (curveFit != null) {
                    map(
                        bundle,
                        TypedValues.PositionType.TYPE_CURVE_FIT,
                        curveFit,
                        "spline",
                        "linear",
                    )
                }
                bundle.addIfNotNull(
                    TypedValues.PositionType.TYPE_TRANSITION_EASING,
                    transitionEasing,
                )
                if (pathMotionArc != null) {
                    map(
                        bundle, TypedValues.PositionType.TYPE_PATH_MOTION_ARC, pathMotionArc,
                        "none", "startVertical", "startHorizontal", "flip", "below", "above",
                    )
                }
                for (j in 0 until frames.size()) {
                    val frame = frames.getInt(j)
                    bundle.add(TypedValues.TYPE_FRAME_POSITION, frame)
                    set(bundle, TypedValues.PositionType.TYPE_PERCENT_X, percentX, j)
                    set(bundle, TypedValues.PositionType.TYPE_PERCENT_Y, percentY, j)
                    set(bundle, TypedValues.PositionType.TYPE_PERCENT_WIDTH, percentWidth, j)
                    set(bundle, TypedValues.PositionType.TYPE_PERCENT_HEIGHT, percentHeight, j)
                    transition.addKeyPosition(target, bundle)
                }
            }
        }

        @Throws(CLParsingException::class)
        private operator fun set(
            bundle: TypedBundle,
            type: Int,
            array: CLArray?,
            index: Int,
        ) {
            if (array != null) {
                bundle.add(type, array.getFloat(index))
            }
        }

        @Throws(CLParsingException::class)
        private fun parseKeyAttribute(
            keyAttribute: CLObject,
            transition: Transition,
        ) {
            val targets = keyAttribute.getArrayOrNull("target") ?: return
            val frames = keyAttribute.getArrayOrNull("frames") ?: return
            val transitionEasing = keyAttribute.getStringOrNull("transitionEasing")
            // These present an ordered list of attributes that might be used in a keyCycle
            val attrNames = arrayOf(
                TypedValues.AttributesType.S_SCALE_X,
                TypedValues.AttributesType.S_SCALE_Y,
                TypedValues.AttributesType.S_TRANSLATION_X,
                TypedValues.AttributesType.S_TRANSLATION_Y,
                TypedValues.AttributesType.S_TRANSLATION_Z,
                TypedValues.AttributesType.S_ROTATION_X,
                TypedValues.AttributesType.S_ROTATION_Y,
                TypedValues.AttributesType.S_ROTATION_Z,
                TypedValues.AttributesType.S_ALPHA,
            )
            val attrIds = intArrayOf(
                TypedValues.AttributesType.TYPE_SCALE_X,
                TypedValues.AttributesType.TYPE_SCALE_Y,
                TypedValues.AttributesType.TYPE_TRANSLATION_X,
                TypedValues.AttributesType.TYPE_TRANSLATION_Y,
                TypedValues.AttributesType.TYPE_TRANSLATION_Z,
                TypedValues.AttributesType.TYPE_ROTATION_X,
                TypedValues.AttributesType.TYPE_ROTATION_Y,
                TypedValues.AttributesType.TYPE_ROTATION_Z,
                TypedValues.AttributesType.TYPE_ALPHA,
            )
            // if true scale the values from pixels to dp
            val scaleTypes = booleanArrayOf(
                false,
                false,
                true,
                true,
                true,
                false,
                false,
                false,
                false,
            )
            val bundles = arrayOfNulls<TypedBundle>(frames.size())
            var customVars: Array<Array<CustomVariable?>>? = null
            for (i in 0 until frames.size()) {
                bundles[i] = TypedBundle()
            }
            for (k in attrNames.indices) {
                val attrName = attrNames[k]
                val attrId = attrIds[k]
                val scale = scaleTypes[k]
                val arrayValues = keyAttribute.getArrayOrNull(attrName)
                // array must contain one per frame
                if (arrayValues != null && arrayValues.size() != bundles.size) {
                    throw CLParsingException(
                        "incorrect size for " + attrName + " array, " +
                            "not matching targets array!",
                        keyAttribute,
                    )
                }
                if (arrayValues != null) {
                    for (i in bundles.indices) {
                        var value = arrayValues.getFloat(i)
                        if (scale) {
                            value = transition.mToPixel.toPixels(value)
                        }
                        bundles[i]!!.add(attrId, value)
                    }
                } else {
                    var value = keyAttribute.getFloatOrNaN(attrName)
                    if (!value.isNaN()) {
                        if (scale) {
                            value = transition.mToPixel.toPixels(value)
                        }
                        for (i in bundles.indices) {
                            bundles[i]!!.add(attrId, value)
                        }
                    }
                }
            }
            // Support for custom attributes in KeyAttributes
            val customElement = keyAttribute.getOrNull("custom")
            if (customElement != null && customElement is CLObject) {
                val customObj = customElement
                val n = customObj.size()
                customVars = Array(frames.size()) { arrayOfNulls(n) }
                for (i in 0 until n) {
                    val key = customObj[i] as CLKey
                    val customName = key.content()
                    if (key.value is CLArray) {
                        val arrayValues = key.value as CLArray?
                        val vSize = arrayValues!!.size()
                        if (vSize == bundles.size && vSize > 0) {
                            if (arrayValues[0] is CLNumber) {
                                for (j in bundles.indices) {
                                    customVars[j][i] = CustomVariable(
                                        customName,
                                        TypedValues.Custom.TYPE_FLOAT,
                                        arrayValues[j].float,
                                    )
                                }
                            } else { // since it is not a number switching to custom color parsing
                                for (j in bundles.indices) {
                                    val color: Long = parseColorString(arrayValues[j].content())
                                    if (color != -1L) {
                                        customVars[j][i] = CustomVariable(
                                            customName,
                                            TypedValues.Custom.TYPE_COLOR,
                                            color.toInt(),
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        val value = key.value
                        if (value is CLNumber) {
                            val fValue = value.float
                            for (j in bundles.indices) {
                                customVars[j][i] = CustomVariable(
                                    customName,
                                    TypedValues.Custom.TYPE_FLOAT,
                                    fValue,
                                )
                            }
                        } else {
                            val cValue: Long = parseColorString(value.content())
                            if (cValue != -1L) {
                                for (j in bundles.indices) {
                                    customVars[j][i] = CustomVariable(
                                        customName,
                                        TypedValues.Custom.TYPE_COLOR,
                                        cValue.toInt(),
                                    )
                                }
                            }
                        }
                    }
                }
            }
            val curveFit = keyAttribute.getStringOrNull("curveFit")
            for (i in 0 until targets.size()) {
                for (j in bundles.indices) {
                    val target = targets.getString(i)
                    val bundle = bundles[j]
                    if (curveFit != null) {
                        bundle!!.add(
                            TypedValues.PositionType.TYPE_CURVE_FIT,
                            map(curveFit, "spline", "linear"),
                        )
                    }
                    bundle!!.addIfNotNull(
                        TypedValues.PositionType.TYPE_TRANSITION_EASING,
                        transitionEasing,
                    )
                    val frame = frames.getInt(j)
                    bundle.add(TypedValues.TYPE_FRAME_POSITION, frame)
                    transition.addKeyAttribute(target, bundle, customVars?.get(j))
                }
            }
        }

        @Throws(CLParsingException::class)
        private fun parseKeyCycle(
            keyCycleData: CLObject,
            transition: Transition,
        ) {
            val targets = keyCycleData.getArray("target")
            val frames = keyCycleData.getArray("frames")
            val transitionEasing = keyCycleData.getStringOrNull("transitionEasing")
            // These present an ordered list of attributes that might be used in a keyCycle
            val attrNames = arrayOf(
                TypedValues.CycleType.S_SCALE_X,
                TypedValues.CycleType.S_SCALE_Y,
                TypedValues.CycleType.S_TRANSLATION_X,
                TypedValues.CycleType.S_TRANSLATION_Y,
                TypedValues.CycleType.S_TRANSLATION_Z,
                TypedValues.CycleType.S_ROTATION_X,
                TypedValues.CycleType.S_ROTATION_Y,
                TypedValues.CycleType.S_ROTATION_Z,
                TypedValues.CycleType.S_ALPHA,
                TypedValues.CycleType.S_WAVE_PERIOD,
                TypedValues.CycleType.S_WAVE_OFFSET,
                TypedValues.CycleType.S_WAVE_PHASE,
            )
            val attrIds = intArrayOf(
                TypedValues.CycleType.TYPE_SCALE_X,
                TypedValues.CycleType.TYPE_SCALE_Y,
                TypedValues.CycleType.TYPE_TRANSLATION_X,
                TypedValues.CycleType.TYPE_TRANSLATION_Y,
                TypedValues.CycleType.TYPE_TRANSLATION_Z,
                TypedValues.CycleType.TYPE_ROTATION_X,
                TypedValues.CycleType.TYPE_ROTATION_Y,
                TypedValues.CycleType.TYPE_ROTATION_Z,
                TypedValues.CycleType.TYPE_ALPHA,
                TypedValues.CycleType.TYPE_WAVE_PERIOD,
                TypedValues.CycleType.TYPE_WAVE_OFFSET,
                TypedValues.CycleType.TYPE_WAVE_PHASE,
            )
            // type 0 the values are used as.
            // type 1 the value is scaled from dp to pixels.
            // type 2 are scaled if the system has another type 1.
            val scaleTypes = intArrayOf(
                0,
                0,
                1,
                1,
                1,
                0,
                0,
                0,
                0,
                0,
                2,
                0,
            )

            //  TODO S_WAVE_SHAPE S_CUSTOM_WAVE_SHAPE
            val bundles = arrayOfNulls<TypedBundle>(frames.size())
            for (i in bundles.indices) {
                bundles[i] = TypedBundle()
            }
            var scaleOffset = false
            for (k in attrNames.indices) {
                if (keyCycleData.has(attrNames[k]) && scaleTypes[k] == 1) {
                    scaleOffset = true
                }
            }
            for (k in attrNames.indices) {
                val attrName = attrNames[k]
                val attrId = attrIds[k]
                val scale = scaleTypes[k]
                val arrayValues = keyCycleData.getArrayOrNull(attrName)
                // array must contain one per frame
                if (arrayValues != null && arrayValues.size() != bundles.size) {
                    throw CLParsingException(
                        "incorrect size for \$attrName array, " +
                            "not matching targets array!",
                        keyCycleData,
                    )
                }
                if (arrayValues != null) {
                    for (i in bundles.indices) {
                        var value = arrayValues.getFloat(i)
                        if (scale == 1) {
                            value = transition.mToPixel.toPixels(value)
                        } else if (scale == 2 && scaleOffset) {
                            value = transition.mToPixel.toPixels(value)
                        }
                        bundles[i]!!.add(attrId, value)
                    }
                } else {
                    var value = keyCycleData.getFloatOrNaN(attrName)
                    if (!value.isNaN()) {
                        if (scale == 1) {
                            value = transition.mToPixel.toPixels(value)
                        } else if (scale == 2 && scaleOffset) {
                            value = transition.mToPixel.toPixels(value)
                        }
                        for (i in bundles.indices) {
                            bundles[i]!!.add(attrId, value)
                        }
                    }
                }
            }
            val curveFit = keyCycleData.getStringOrNull(TypedValues.CycleType.S_CURVE_FIT)
            val easing = keyCycleData.getStringOrNull(TypedValues.CycleType.S_EASING)
            val waveShape = keyCycleData.getStringOrNull(TypedValues.CycleType.S_WAVE_SHAPE)
            val customWave = keyCycleData.getStringOrNull(TypedValues.CycleType.S_CUSTOM_WAVE_SHAPE)
            for (i in 0 until targets.size()) {
                for (j in bundles.indices) {
                    val target = targets.getString(i)
                    val bundle = bundles[j]
                    if (curveFit != null) {
                        when (curveFit) {
                            "spline" -> bundle!!.add(TypedValues.CycleType.TYPE_CURVE_FIT, 0)
                            "linear" -> bundle!!.add(TypedValues.CycleType.TYPE_CURVE_FIT, 1)
                        }
                    }
                    bundle!!.addIfNotNull(
                        TypedValues.PositionType.TYPE_TRANSITION_EASING,
                        transitionEasing,
                    )
                    if (easing != null) {
                        bundle.add(TypedValues.CycleType.TYPE_EASING, easing)
                    }
                    if (waveShape != null) {
                        bundle.add(TypedValues.CycleType.TYPE_WAVE_SHAPE, waveShape)
                    }
                    if (customWave != null) {
                        bundle.add(TypedValues.CycleType.TYPE_CUSTOM_WAVE_SHAPE, customWave)
                    }
                    val frame = frames.getInt(j)
                    bundle.add(TypedValues.TYPE_FRAME_POSITION, frame)
                    transition.addKeyCycle(target, bundle)
                }
            }
        }
    }
}
