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
import androidx.constraintlayout.coremp.motion.utils.TypedValues.MotionType.Companion.TYPE_QUANTIZE_INTERPOLATOR_TYPE
import androidx.constraintlayout.coremp.motion.utils.TypedValues.MotionType.Companion.TYPE_QUANTIZE_MOTIONSTEPS
import androidx.constraintlayout.coremp.motion.utils.TypedValues.MotionType.Companion.TYPE_QUANTIZE_MOTION_PHASE
import androidx.constraintlayout.coremp.parser.CLArray
import androidx.constraintlayout.coremp.parser.CLElement
import androidx.constraintlayout.coremp.parser.CLKey
import androidx.constraintlayout.coremp.parser.CLNumber
import androidx.constraintlayout.coremp.parser.CLObject
import androidx.constraintlayout.coremp.parser.CLParser
import androidx.constraintlayout.coremp.parser.CLParsingException
import androidx.constraintlayout.coremp.parser.CLString
import androidx.constraintlayout.coremp.state.helpers.BarrierReference
import androidx.constraintlayout.coremp.state.helpers.ChainReference
import androidx.constraintlayout.coremp.state.helpers.FlowReference
import androidx.constraintlayout.coremp.state.helpers.GridReference
import androidx.constraintlayout.coremp.state.helpers.GuidelineReference
import androidx.constraintlayout.coremp.widgets.ConstraintWidget
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.HORIZONTAL
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.VERTICAL
import androidx.constraintlayout.coremp.widgets.Flow

class ConstraintSetParser {

    interface GeneratedValue {
        fun value(): Float
    }

    // ==================== end store variables =========================
    // ==================== MotionScene =========================
    enum class MotionLayoutDebugFlags {
        NONE,
        SHOW_ALL,
        UNKNOWN,
    }

    class LayoutVariables {
        private var mMargins = HashMap<String, Int>()
        private var mGenerators: HashMap<String, GeneratedValue> = HashMap()
        private var mArrayIds = HashMap<String, ArrayList<String>>()

        fun put(elementName: String?, element: Int) {
            mMargins[elementName!!] = element
        }

        fun put(elementName: String?, start: Float, incrementBy: Float) {
            if (mGenerators.containsKey(elementName)) {
                if (mGenerators[elementName] is OverrideValue) {
                    return
                }
            }
            mGenerators[elementName!!] = Generator(start, incrementBy)
        }

        fun put(
            elementName: String,
            from: Float,
            to: Float,
            step: Float,
            prefix: String?,
            postfix: String?,
        ) {
            if (mGenerators.containsKey(elementName)) {
                if (mGenerators[elementName] is OverrideValue) {
                    return
                }
            }
            val generator = FiniteGenerator(from, to, step, prefix, postfix)
            mGenerators[elementName] = generator
            mArrayIds[elementName] = generator.array()
        }

        /**
         * insert an override variable
         *
         * @param elementName the name
         * @param value       the value a float
         */
        fun putOverride(elementName: String, value: Float) {
            val generator: GeneratedValue = OverrideValue(value)
            mGenerators[elementName] = generator
        }

        operator fun get(elementName: Any): Float {
            if (elementName is CLString) {
                val stringValue = elementName.content()
                if (mGenerators.containsKey(stringValue)) {
                    return mGenerators[stringValue]!!.value()
                }
                if (mMargins.containsKey(stringValue)) {
                    return mMargins[stringValue]!!.toFloat()
                }
            } else if (elementName is CLNumber) {
                return elementName.float
            }
            return 0f
        }

        fun getList(elementName: String): ArrayList<String>? {
            return if (mArrayIds.containsKey(elementName)) {
                mArrayIds[elementName]
            } else {
                null
            }
        }

        fun put(elementName: String, elements: ArrayList<String>) {
            mArrayIds[elementName] = elements
        }
    }

    class DesignElement(val id: String, val type: String, val params: HashMap<String, String>)

    class Generator(start: Float, incrementBy: Float) : GeneratedValue {
        var mStart = start
        private var mIncrementBy = incrementBy
        private var mCurrent = start
        private var mStop = false

        override fun value(): Float {
            if (!mStop) {
                mCurrent += mIncrementBy
            }
            return mCurrent
        }
    }

    companion object {
        private const val PARSER_DEBUG = false

        internal class FiniteGenerator(
            from: Float,
            to: Float,
            step: Float,
            prefix: String?,
            postfix: String?,
        ) : GeneratedValue {
            private var mFrom = 0f
            private var mTo = 0f
            private var mStep = 0f
            private var mStop = false
            private var mPrefix: String
            private var mPostfix: String
            private var mCurrent = 0f
            private var mInitial: Float
            private var mMax: Float

            init {
                mFrom = from
                mTo = to
                mStep = step
                mPrefix = prefix ?: ""
                mPostfix = postfix ?: ""
                mMax = to
                mInitial = from
            }

            override fun value(): Float {
                if (mCurrent >= mMax) {
                    mStop = true
                }
                if (!mStop) {
                    mCurrent += mStep
                }
                return mCurrent
            }

            fun array(): ArrayList<String> {
                val array = ArrayList<String>()
                var value = mInitial.toInt()
                val maxInt = mMax.toInt()
                for (i in value..maxInt) {
                    array.add(mPrefix + value + mPostfix)
                    value += mStep.toInt()
                }
                return array
            }
        }

        internal class OverrideValue(value: Float) : GeneratedValue {
            private var mValue: Float = value
            override fun value(): Float {
                return mValue
            }
        }

        // ==================== end Motion Scene =========================

        // ==================== end Motion Scene =========================
        /**
         * Parse and populate a transition
         *
         * @param content    JSON string to parse
         * @param transition The Transition to be populated
         * @param state
         */
        fun parseJSON(content: String?, transition: Transition, state: Int) {
            try {
                val json = CLParser.parse(content!!)
                val elements = json.names()
                for (elementName in elements) {
                    val base_element = json[elementName]
                    if (base_element is CLObject) {
                        val customProperties = base_element.getObjectOrNull("custom")
                        if (customProperties != null) {
                            val properties = customProperties.names()
                            for (property in properties) {
                                val value = customProperties[property]
                                if (value is CLNumber) {
                                    transition.addCustomFloat(
                                        state,
                                        elementName,
                                        property,
                                        value.float,
                                    )
                                } else if (value is CLString) {
                                    val color: Long = parseColorString(value.content())
                                    if (color != -1L) {
                                        transition.addCustomColor(
                                            state,
                                            elementName,
                                            property,
                                            color.toInt(),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: CLParsingException) {
                println("Error parsing JSON $e")
            }
        }

        /**
         * Parse and build a motionScene
         *
         * this should be in a MotionScene / MotionSceneParser
         */
        fun parseMotionSceneJSON(scene: CoreMotionScene, content: String?) {
            try {
                val json = CLParser.parse(content!!)
                val elements = json.names()
                for (elementName in elements) {
                    val element = json[elementName]
                    if (element is CLObject) {
                        val clObject = element
                        when (elementName) {
                            "ConstraintSets" -> parseConstraintSets(scene, clObject)
                            "Transitions" -> parseTransitions(scene, clObject)
                            "Header" -> parseHeader(scene, clObject)
                        }
                    }
                }
            } catch (e: CLParsingException) {
                println("Error parsing JSON $e")
            }
        }

        /**
         * Parse ConstraintSets and populate MotionScene
         */
        @Throws(CLParsingException::class)
        fun parseConstraintSets(
            scene: CoreMotionScene,
            json: CLObject,
        ) {
            val constraintSetNames = json.names()
            for (csName in constraintSetNames) {
                val constraintSet = json.getObject(csName)
                var added = false
                val ext = constraintSet.getStringOrNull("Extends")
                if (ext != null && !ext.isEmpty()) {
                    val base = scene.getConstraintSet(ext) ?: continue
                    val baseJson = CLParser.parse(base)
                    val widgetsOverride = constraintSet.names()
                    for (widgetOverrideName in widgetsOverride) {
                        val value = constraintSet[widgetOverrideName]
                        if (value is CLObject) {
                            override(baseJson, widgetOverrideName, value)
                        }
                    }
                    scene.setConstraintSetContent(csName, baseJson.toJSON())
                    added = true
                }
                if (!added) {
                    scene.setConstraintSetContent(csName, constraintSet.toJSON())
                }
            }
        }

        @Throws(CLParsingException::class)
        fun override(
            baseJson: CLObject,
            name: String?,
            overrideValue: CLObject,
        ) {
            if (!baseJson.has(name)) {
                baseJson.put(name!!, overrideValue)
            } else {
                val base = baseJson.getObject(name!!)
                val keys = overrideValue.names()
                for (key in keys) {
                    if (key != "clear") {
                        base.put(key, overrideValue[key])
                        continue
                    }
                    val toClear = overrideValue.getArray("clear")
                    for (i in 0 until toClear.size()) {
                        val clearedKey = toClear.getStringOrNull(i) ?: continue
                        when (clearedKey) {
                            "dimensions" -> {
                                base.remove("width")
                                base.remove("height")
                            }

                            "constraints" -> {
                                base.remove("start")
                                base.remove("end")
                                base.remove("top")
                                base.remove("bottom")
                                base.remove("baseline")
                                base.remove("center")
                                base.remove("centerHorizontally")
                                base.remove("centerVertically")
                            }

                            "transforms" -> {
                                base.remove("visibility")
                                base.remove("alpha")
                                base.remove("pivotX")
                                base.remove("pivotY")
                                base.remove("rotationX")
                                base.remove("rotationY")
                                base.remove("rotationZ")
                                base.remove("scaleX")
                                base.remove("scaleY")
                                base.remove("translationX")
                                base.remove("translationY")
                            }

                            else -> base.remove(clearedKey)
                        }
                    }
                }
            }
        }

        /**
         * Parse the Transition
         */
        @Throws(CLParsingException::class)
        fun parseTransitions(scene: CoreMotionScene, json: CLObject) {
            val elements = json.names()
            for (elementName in elements) {
                scene.setTransitionContent(elementName, json.getObject(elementName).toJSON())
            }
        }

        /**
         * Used to parse for "export"
         */
        fun parseHeader(scene: CoreMotionScene, json: CLObject) {
            val name = json.getStringOrNull("export")
            if (name != null) {
                scene.setDebugName(name)
            }
        }

        /**
         * Top leve parsing of the json ConstraintSet supporting
         * "Variables", "Helpers", "Generate", guidelines, and barriers
         *
         * @param content         the JSON string
         * @param state           the state to populate
         * @param layoutVariables the variables to override
         */
        @Throws(CLParsingException::class)
        fun parseJSON(
            content: String,
            state: State,
            layoutVariables: LayoutVariables,
        ) {
            try {
                val json = CLParser.parse(content)
                populateState(json, state, layoutVariables)
            } catch (e: CLParsingException) {
                println("Error parsing JSON $e")
            }
        }

        /**
         * Populates the given [State] with the parameters from [CLObject]. Where the
         * object represents a parsed JSONObject of a ConstraintSet.
         *
         * @param parsedJson CLObject of the parsed ConstraintSet
         * @param state the state to populate
         * @param layoutVariables the variables to override
         * @throws CLParsingException when parsing fails
         */
        // @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        @Throws(CLParsingException::class)
        fun populateState(
            parsedJson: CLObject,
            state: State,
            layoutVariables: LayoutVariables,
        ) {
            val elements: ArrayList<String> = parsedJson.names()
            for (elementName: String in elements) {
                val element = parsedJson[elementName]
                if (PARSER_DEBUG) {
                    println(
                        "[" + elementName + "] = " + element +
                            " > " + element.getContainer(),
                    )
                }
                when (elementName) {
                    "Variables" -> if (element is CLObject) {
                        parseVariables(state, layoutVariables, element)
                    }

                    "Helpers" -> if (element is CLArray) {
                        parseHelpers(state, layoutVariables, element)
                    }

                    "Generate" -> if (element is CLObject) {
                        parseGenerate(state, layoutVariables, element)
                    }

                    else -> if (element is CLObject) {
                        val type: String? = lookForType(element)
                        if (type != null) {
                            when (type) {
                                "hGuideline" -> parseGuidelineParams(
                                    HORIZONTAL,
                                    state,
                                    elementName,
                                    element,
                                )

                                "vGuideline" -> parseGuidelineParams(
                                    VERTICAL,
                                    state,
                                    elementName,
                                    element,
                                )

                                "barrier" -> parseBarrier(state, elementName, element)
                                "vChain", "hChain" -> parseChainType(
                                    type,
                                    state,
                                    elementName,
                                    layoutVariables,
                                    element,
                                )

                                "vFlow", "hFlow" -> parseFlowType(
                                    type,
                                    state,
                                    elementName,
                                    layoutVariables,
                                    element,
                                )

                                "grid", "row", "column" -> parseGridType(
                                    type,
                                    state,
                                    elementName,
                                    layoutVariables,
                                    element,
                                )
                            }
                        } else {
                            parseWidget(
                                state,
                                layoutVariables,
                                elementName,
                                element,
                            )
                        }
                    } else if (element is CLNumber) {
                        layoutVariables.put(elementName, element.getInt())
                    }
                }
            }
        }

        @Throws(CLParsingException::class)
        private fun parseVariables(
            state: State,
            layoutVariables: LayoutVariables,
            json: CLObject,
        ) {
            val elements = json.names()
            for (elementName in elements) {
                val element = json[elementName]
                if (element is CLNumber) {
                    layoutVariables.put(elementName, element.getInt())
                } else if (element is CLObject) {
                    val obj = element
                    var arrayIds: ArrayList<String>?
                    if (obj.has("from") && obj.has("to")) {
                        val from = layoutVariables[obj["from"]]
                        val to = layoutVariables[obj["to"]]
                        val prefix = obj.getStringOrNull("prefix")
                        val postfix = obj.getStringOrNull("postfix")
                        layoutVariables.put(elementName, from, to, 1f, prefix, postfix)
                    } else if (obj.has("from") && obj.has("step")) {
                        val start = layoutVariables[obj["from"]]
                        val increment = layoutVariables[obj["step"]]
                        layoutVariables.put(elementName, start, increment)
                    } else if (obj.has("ids")) {
                        val ids = obj.getArray("ids")
                        arrayIds = ArrayList()
                        for (i in 0 until ids.size()) {
                            arrayIds.add(ids.getString(i))
                        }
                        layoutVariables.put(elementName, arrayIds)
                    } else if (obj.has("tag")) {
                        arrayIds = state.getIdsForTag(obj.getString("tag"))
                        layoutVariables.put(elementName, arrayIds!!)
                    }
                }
            }
        }

        /**
         * parse the Design time elements.
         *
         * @param content the json
         * @param list    output the list of design elements
         */
        @Throws(CLParsingException::class)
        fun parseDesignElementsJSON(
            content: String?,
            list: ArrayList<DesignElement>,
        ) {
            val json: CLObject? = content?.let { CLParser.parse(it) }
            var elements: ArrayList<String> = json?.names() ?: return
            for (i in elements.indices) {
                val elementName = elements[i]
                val element = json[elementName]
                if (PARSER_DEBUG) {
                    println("[" + element + "] " + element::class.simpleName)
                }
                when (elementName) {
                    "Design" -> {
                        if (element !is CLObject) {
                            return
                        }
                        elements = element.names()
                        var j = 0
                        while (j < elements.size) {
                            val designElementName: String = elements[j]
                            val designElement = element[designElementName] as CLObject
                            println("element found $designElementName")
                            val type = designElement.getStringOrNull("type")
                            if (type != null) {
                                val parameters = HashMap<String, String>()
                                val size = designElement.size()
                                var k = 0
                                while (k < size) {
                                    val key = designElement[j] as CLKey
                                    val paramName = key.content()
                                    val paramValue = key.value.content()
                                    parameters[paramName] = paramValue
                                    k++
                                }
                                list.add(DesignElement(elementName, type, parameters))
                            }
                            j++
                        }
                    }
                }
                break
            }
        }

        @Throws(CLParsingException::class)
        fun parseHelpers(
            state: State,
            layoutVariables: LayoutVariables,
            element: CLArray,
        ) {
            for (i in 0 until element.size()) {
                val helper = element[i]
                if (helper is CLArray) {
                    val array = helper
                    if (array.size() > 1) {
                        when (array.getString(0)) {
                            "hChain" -> parseChain(
                                HORIZONTAL,
                                state,
                                layoutVariables,
                                array,
                            )

                            "vChain" -> parseChain(
                                VERTICAL,
                                state,
                                layoutVariables,
                                array,
                            )

                            "hGuideline" -> parseGuideline(
                                HORIZONTAL,
                                state,
                                array,
                            )

                            "vGuideline" -> parseGuideline(VERTICAL, state, array)
                        }
                    }
                }
            }
        }

        @Throws(CLParsingException::class)
        fun parseGenerate(
            state: State,
            layoutVariables: LayoutVariables,
            json: CLObject,
        ) {
            val elements = json.names()
            for (elementName in elements) {
                val element = json[elementName]
                val arrayIds = layoutVariables.getList(elementName)
                if (arrayIds != null && element is CLObject) {
                    for (id in arrayIds) {
                        parseWidget(state, layoutVariables, id, element)
                    }
                }
            }
        }

        @Throws(CLParsingException::class)
        fun parseChain(
            orientation: Int,
            state: State,
            margins: LayoutVariables,
            helper: CLArray,
        ) {
            val chain: ChainReference =
                if (orientation == HORIZONTAL) state.horizontalChain() else state.verticalChain()
            val refs = helper[1]
            if (refs !is CLArray || refs.size() < 1) {
                return
            }
            for (i in 0 until refs.size()) {
                chain.add(refs.getString(i))
            }
            if (helper.size() > 2) { // we have additional parameters
                val params = helper[2] as? CLObject ?: return
                val constraints = params.names()
                for (constraintName in constraints) {
                    when (constraintName) {
                        "style" -> {
                            val styleObject = params[constraintName]
                            var styleValue: String
                            if (styleObject is CLArray && styleObject.size() > 1) {
                                styleValue = styleObject.getString(0)
                                val biasValue = styleObject.getFloat(1)
                                chain.bias(biasValue)
                            } else {
                                styleValue = styleObject.content()
                            }
                            when (styleValue) {
                                "packed" -> chain.style(State.Chain.PACKED)
                                "spread_inside" -> chain.style(State.Chain.SPREAD_INSIDE)
                                else -> chain.style(State.Chain.SPREAD)
                            }
                        }

                        else -> parseConstraint(
                            state,
                            margins,
                            params,
                            chain as ConstraintReference,
                            constraintName,
                        )
                    }
                }
            }
        }

        private fun toPix(state: State, dp: Float): Float {
            return state.getDpToPixel()!!.toPixels(dp)
        }

        /**
         * Support parsing Chain in the following manner
         * chainId : {
         * type:'hChain'  // or vChain
         * contains: ['id1', 'id2', 'id3' ]
         * contains: [['id', weight, marginL ,marginR], 'id2', 'id3' ]
         * start: ['parent', 'start',0],
         * end: ['parent', 'end',0],
         * top: ['parent', 'top',0],
         * bottom: ['parent', 'bottom',0],
         * style: 'spread'
         * }
         *
         * @throws CLParsingException
         */
        @Throws(CLParsingException::class)
        private fun parseChainType(
            orientation: String,
            state: State,
            chainName: String,
            margins: LayoutVariables,
            `object`: CLObject,
        ) {
            val chain: ChainReference =
                if (orientation[0] == 'h') state.horizontalChain() else state.verticalChain()
            chain.setKey(chainName)
            for (params: String in `object`.names()) {
                when (params) {
                    "contains" -> {
                        val refs = `object`[params]
                        if (refs !is CLArray || refs.size() < 1) {
                            println(
                                chainName + " contains should be an array \"" + refs.content() +
                                    "\"",
                            )
                            return
                        }
                        var i = 0
                        while (i < refs.size()) {
                            val chainElement = refs[i]
                            if (chainElement is CLArray) {
                                val array = chainElement
                                if (array.size() > 0) {
                                    val id = array[0].content()
                                    var weight = Float.NaN
                                    var preMargin = Float.NaN
                                    var postMargin = Float.NaN
                                    var preGoneMargin = Float.NaN
                                    var postGoneMargin = Float.NaN
                                    when (array.size()) {
                                        2 -> weight = array.getFloat(1)
                                        3 -> {
                                            weight = array.getFloat(1)
                                            run {
                                                preMargin = toPix(state, array.getFloat(2))
                                                postMargin = preMargin
                                            }
                                        }

                                        4 -> {
                                            weight = array.getFloat(1)
                                            preMargin = toPix(state, array.getFloat(2))
                                            postMargin = toPix(state, array.getFloat(3))
                                        }

                                        6 -> {
                                            // postGoneMargin
                                            weight = array.getFloat(1)
                                            preMargin = toPix(state, array.getFloat(2))
                                            postMargin = toPix(state, array.getFloat(3))
                                            preGoneMargin = toPix(state, array.getFloat(4))
                                            postGoneMargin = toPix(state, array.getFloat(5))
                                        }
                                    }
                                    chain.addChainElement(
                                        id,
                                        weight,
                                        preMargin,
                                        postMargin,
                                        preGoneMargin,
                                        postGoneMargin,
                                    )
                                }
                            } else {
                                chain.add(chainElement.content())
                            }
                            i++
                        }
                    }

                    "start", "end", "top", "bottom", "left", "right" -> parseConstraint(
                        state,
                        margins,
                        `object`,
                        chain,
                        params,
                    )

                    "style" -> {
                        val styleObject = `object`[params]
                        var styleValue: String
                        if (styleObject is CLArray && styleObject.size() > 1) {
                            styleValue = styleObject.getString(0)
                            val biasValue = styleObject.getFloat(1)
                            chain.bias(biasValue)
                        } else {
                            styleValue = styleObject.content()
                        }
                        when (styleValue) {
                            "packed" -> chain.style(State.Chain.PACKED)
                            "spread_inside" -> chain.style(State.Chain.SPREAD_INSIDE)
                            else -> chain.style(State.Chain.SPREAD)
                        }
                    }
                }
            }
        }

        /**
         * Support parsing Grid in the following manner
         * chainId : {
         * height: "parent",
         * width: "parent",
         * type: "Grid",
         * vGap: 10,
         * hGap: 10,
         * orientation: 0,
         * rows: 0,
         * columns: 1,
         * columnWeights: "",
         * rowWeights: "",
         * contains: ["btn1", "btn2", "btn3", "btn4"],
         * top: ["parent", "top", 10],
         * bottom: ["parent", "bottom", 20],
         * right: ["parent", "right", 30],
         * left: ["parent", "left", 40],
         * }
         *
         * @param gridType type of the Grid helper could be "Grid"|"Row"|"Column"
         * @param state ConstraintLayout State
         * @param name the name of the Grid Helper
         * @param layoutVariables layout margins
         * @param element the element to be parsed
         * @throws CLParsingException
         */
        @Throws(CLParsingException::class)
        private fun parseGridType(
            gridType: String,
            state: State,
            name: String,
            layoutVariables: LayoutVariables,
            element: CLObject,
        ) {
            val grid: GridReference = state.getGrid(name, gridType)!!
            for (param in element.names()) {
                when (param) {
                    "contains" -> {
                        val list = element.getArrayOrNull(param)
                        if (list != null) {
                            var j = 0
                            while (j < list.size()) {
                                val elementNameReference = list[j].content()
                                val elementReference = state.constraints(elementNameReference)
                                grid.add(elementReference!!)
                                j++
                            }
                        }
                    }

                    "orientation" -> {
                        val orientation = element[param].getInt()
                        grid.setOrientation(orientation)
                    }

                    "rows" -> {
                        val rows = element[param].getInt()
                        if (rows > 0) {
                            grid.setRowsSet(rows)
                        }
                    }

                    "columns" -> {
                        val columns = element[param].getInt()
                        if (columns > 0) {
                            grid.setColumnsSet(columns)
                        }
                    }

                    "hGap" -> {
                        val hGap = element[param].float
                        grid.setHorizontalGaps(toPix(state, hGap))
                    }

                    "vGap" -> {
                        val vGap = element[param].float
                        grid.setVerticalGaps(toPix(state, vGap))
                    }

                    "spans" -> {
                        val spans = element[param].content()
                        if (spans != null && spans.contains(":")) {
                            grid.setSpans(spans)
                        }
                    }

                    "skips" -> {
                        val skips = element[param].content()
                        if (skips != null && skips.contains(":")) {
                            grid.setSkips(skips)
                        }
                    }

                    "rowWeights" -> {
                        val rowWeights = element[param].content()
                        if (rowWeights != null && rowWeights.contains(",")) {
                            grid.setRowWeights(rowWeights)
                        }
                    }

                    "columnWeights" -> {
                        val columnWeights = element[param].content()
                        if (columnWeights != null && columnWeights.contains(",")) {
                            grid.setColumnWeights(columnWeights)
                        }
                    }

                    "padding" -> {
                        val paddingObject = element[param]
                        var paddingStart = 0
                        var paddingTop = 0
                        var paddingEnd = 0
                        var paddingBottom = 0
                        if (paddingObject is CLArray && paddingObject.size() > 1) {
                            paddingStart = paddingObject.getInt(0)
                            paddingEnd = paddingStart
                            paddingTop = paddingObject.getInt(1)
                            paddingBottom = paddingTop
                            if (paddingObject.size() > 2) {
                                paddingEnd = paddingObject.getInt(2)
                                paddingBottom = try {
                                    paddingObject.getInt(3)
                                } catch (e: IndexOutOfBoundsException) {
                                    0
                                }
                            }
                        } else {
                            paddingStart = paddingObject.getInt()
                            paddingTop = paddingStart
                            paddingEnd = paddingStart
                            paddingBottom = paddingStart
                        }
                        grid.setPaddingStart(paddingStart)
                        grid.setPaddingTop(paddingTop)
                        grid.setPaddingEnd(paddingEnd)
                        grid.setPaddingBottom(paddingBottom)
                    }

                    "flags" -> {
                        var flags: String? = element[param].content()
                        if (flags != null && flags.length > 0) {
                            grid.setFlags(flags)
                        } else {
                            val flagArray = element.getArrayOrNull(param)
                            flags = ""
                            if (flagArray != null) {
                                var i = 0
                                while (i < flagArray.size()) {
                                    val flag = flagArray[i].content()
                                    flags += flag
                                    if (i != flagArray.size() - 1) {
                                        flags += "|"
                                    }
                                    i++
                                }
                                grid.setFlags(flags)
                            }
                        }
                    }

                    else -> {
                        val reference = state.constraints(name)
                        applyAttribute(state, layoutVariables, reference!!, element, param)
                    }
                }
            }
        }

        /**
         * It's used to parse the Flow type of Helper with the following format:
         * flowID: {
         * type: 'hFlow'|'vFlow’
         * wrap: 'chain'|'none'|'aligned',
         * contains: ['id1', 'id2', 'id3' ] |
         * [['id1', weight, preMargin , postMargin], 'id2', 'id3'],
         * vStyle: 'spread'|'spread_inside'|'packed' | ['first', 'middle', 'last'],
         * hStyle: 'spread'|'spread_inside'|'packed' | ['first', 'middle', 'last'],
         * vAlign: 'top'|'bottom'|'baseline'|'center',
         * hAlign: 'start'|'end'|'center',
         * vGap: 32,
         * hGap: 23,
         * padding: 32,
         * maxElement: 5,
         * vBias: 0.3 | [0.0, 0.5, 0.5],
         * hBias: 0.4 | [0.0, 0.5, 0.5],
         * start: ['parent', 'start', 0],
         * end: ['parent', 'end', 0],
         * top: ['parent', 'top', 0],
         * bottom: ['parent', 'bottom', 0],
         * }
         *
         * @param flowType orientation of the Flow Helper
         * @param state ConstraintLayout State
         * @param flowName the name of the Flow Helper
         * @param layoutVariables layout margins
         * @param element the element to be parsed
         * @throws CLParsingException
         */
        @Throws(CLParsingException::class)
        private fun parseFlowType(
            flowType: String,
            state: State,
            flowName: String,
            layoutVariables: LayoutVariables,
            element: CLObject,
        ) {
            val isVertical = flowType[0] == 'v'
            val flow: FlowReference? = state.getFlow(flowName, isVertical)
            for (param: String in element.names()) {
                when (param) {
                    "contains" -> {
                        val refs = element[param]
                        if (refs !is CLArray || refs.size() < 1) {
                            println(
                                flowName + " contains should be an array \"" + refs.content() +
                                    "\"",
                            )
                            return
                        }
                        var i = 0
                        while (i < refs.size()) {
                            val chainElement = refs[i]
                            if (chainElement is CLArray) {
                                val array = chainElement
                                if (array.size() > 0) {
                                    val id = array[0].content()
                                    var weight = Float.NaN
                                    var preMargin = Float.NaN
                                    var postMargin = Float.NaN
                                    when (array.size()) {
                                        2 -> weight = array.getFloat(1)
                                        3 -> {
                                            weight = array.getFloat(1)
                                            run {
                                                preMargin = toPix(state, array.getFloat(2))
                                                postMargin = preMargin
                                            }
                                        }

                                        4 -> {
                                            weight = array.getFloat(1)
                                            preMargin = toPix(state, array.getFloat(2))
                                            postMargin = toPix(state, array.getFloat(3))
                                        }
                                    }
                                    flow!!.addFlowElement(id, weight, preMargin, postMargin)
                                }
                            } else {
                                flow!!.add(chainElement.content())
                            }
                            i++
                        }
                    }

                    "type" -> if ((element[param].content() == "hFlow")) {
                        flow!!.setOrientation(HORIZONTAL)
                    } else {
                        flow!!.setOrientation(VERTICAL)
                    }

                    "wrap" -> {
                        val wrapValue = element[param].content()
                        flow!!.setWrapMode(State.Wrap.getValueByString(wrapValue))
                    }

                    "vGap" -> {
                        val vGapValue = element[param].getInt()
                        flow!!.setVerticalGap(vGapValue)
                    }

                    "hGap" -> {
                        val hGapValue = element[param].getInt()
                        flow!!.setHorizontalGap(hGapValue)
                    }

                    "maxElement" -> {
                        val maxElementValue = element[param].getInt()
                        flow!!.setMaxElementsWrap(maxElementValue)
                    }

                    "padding" -> {
                        val paddingObject = element[param]
                        var paddingLeft = 0
                        var paddingTop = 0
                        var paddingRight = 0
                        var paddingBottom = 0
                        if (paddingObject is CLArray && paddingObject.size() > 1) {
                            paddingLeft = paddingObject.getInt(0)
                            paddingRight = paddingLeft
                            paddingTop = paddingObject.getInt(1)
                            paddingBottom = paddingTop
                            if (paddingObject.size() > 2) {
                                paddingRight = paddingObject.getInt(2)
                                try {
                                    paddingBottom = paddingObject.getInt(3)
                                } catch (e: IndexOutOfBoundsException) {
                                    paddingBottom = 0
                                }
                            }
                        } else {
                            paddingLeft = paddingObject.getInt()
                            paddingTop = paddingLeft
                            paddingRight = paddingLeft
                            paddingBottom = paddingLeft
                        }
                        flow!!.setPaddingLeft(paddingLeft)
                        flow.setPaddingTop(paddingTop)
                        flow.setPaddingRight(paddingRight)
                        flow.setPaddingBottom(paddingBottom)
                    }

                    "vAlign" -> {
                        val vAlignValue = element[param].content()
                        when (vAlignValue) {
                            "top" -> flow!!.setVerticalAlign(Flow.VERTICAL_ALIGN_TOP)
                            "bottom" -> flow!!.setVerticalAlign(Flow.VERTICAL_ALIGN_BOTTOM)
                            "baseline" -> flow!!.setVerticalAlign(Flow.VERTICAL_ALIGN_BASELINE)
                            else -> flow!!.setVerticalAlign(Flow.VERTICAL_ALIGN_CENTER)
                        }
                    }

                    "hAlign" -> {
                        val hAlignValue = element[param].content()
                        when (hAlignValue) {
                            "start" -> flow!!.setHorizontalAlign(Flow.HORIZONTAL_ALIGN_START)
                            "end" -> flow!!.setHorizontalAlign(Flow.HORIZONTAL_ALIGN_END)
                            else -> flow!!.setHorizontalAlign(Flow.HORIZONTAL_ALIGN_CENTER)
                        }
                    }

                    "vFlowBias" -> {
                        val vBiasObject = element[param]
                        var vBiasValue: Float = 0.5f
                        var vFirstBiasValue = 0.5f
                        var vLastBiasValue = 0.5f
                        if (vBiasObject is CLArray && vBiasObject.size() > 1) {
                            vFirstBiasValue = vBiasObject.getFloat(0)
                            vBiasValue = vBiasObject.getFloat(1)
                            if (vBiasObject.size() > 2) {
                                vLastBiasValue = vBiasObject.getFloat(2)
                            }
                        } else {
                            vBiasValue = vBiasObject.float
                        }
                        try {
                            flow!!.verticalBias(vBiasValue)
                            if (vFirstBiasValue != 0.5f) {
                                flow.setFirstVerticalBias(vFirstBiasValue)
                            }
                            if (vLastBiasValue != 0.5f) {
                                flow.setLastVerticalBias(vLastBiasValue)
                            }
                        } catch (e: NumberFormatException) {
                        }
                    }

                    "hFlowBias" -> {
                        val hBiasObject = element[param]
                        var hBiasValue: Float = 0.5f
                        var hFirstBiasValue = 0.5f
                        var hLastBiasValue = 0.5f
                        if (hBiasObject is CLArray && hBiasObject.size() > 1) {
                            hFirstBiasValue = hBiasObject.getFloat(0)
                            hBiasValue = hBiasObject.getFloat(1)
                            if (hBiasObject.size() > 2) {
                                hLastBiasValue = hBiasObject.getFloat(2)
                            }
                        } else {
                            hBiasValue = hBiasObject.float
                        }
                        try {
                            flow!!.horizontalBias(hBiasValue)
                            if (hFirstBiasValue != 0.5f) {
                                flow.setFirstHorizontalBias(hFirstBiasValue)
                            }
                            if (hLastBiasValue != 0.5f) {
                                flow.setLastHorizontalBias(hLastBiasValue)
                            }
                        } catch (e: NumberFormatException) {
                        }
                    }

                    "vStyle" -> {
                        val vStyleObject = element[param]
                        var vStyleValueStr = ""
                        var vFirstStyleValueStr = ""
                        var vLastStyleValueStr = ""
                        if (vStyleObject is CLArray && vStyleObject.size() > 1) {
                            vFirstStyleValueStr = vStyleObject.getString(0)
                            vStyleValueStr = vStyleObject.getString(1)
                            if (vStyleObject.size() > 2) {
                                vLastStyleValueStr = vStyleObject.getString(2)
                            }
                        } else {
                            vStyleValueStr = vStyleObject.content()
                        }
                        if (vStyleValueStr != "") {
                            flow!!.setVerticalStyle(State.Chain.getValueByString(vStyleValueStr))
                        }
                        if (vFirstStyleValueStr != "") {
                            flow!!.setFirstVerticalStyle(
                                State.Chain.getValueByString(vFirstStyleValueStr),
                            )
                        }
                        if (vLastStyleValueStr != "") {
                            flow!!.setLastVerticalStyle(
                                State.Chain.getValueByString(
                                    vLastStyleValueStr,
                                ),
                            )
                        }
                    }

                    "hStyle" -> {
                        val hStyleObject = element[param]
                        var hStyleValueStr = ""
                        var hFirstStyleValueStr = ""
                        var hLastStyleValueStr = ""
                        if (hStyleObject is CLArray && hStyleObject.size() > 1) {
                            hFirstStyleValueStr = hStyleObject.getString(0)
                            hStyleValueStr = hStyleObject.getString(1)
                            if (hStyleObject.size() > 2) {
                                hLastStyleValueStr = hStyleObject.getString(2)
                            }
                        } else {
                            hStyleValueStr = hStyleObject.content()
                        }
                        if (hStyleValueStr != "") {
                            flow!!.setHorizontalStyle(State.Chain.getValueByString(hStyleValueStr))
                        }
                        if (hFirstStyleValueStr != "") {
                            flow!!.setFirstHorizontalStyle(
                                State.Chain.getValueByString(hFirstStyleValueStr),
                            )
                        }
                        if (hLastStyleValueStr != "") {
                            flow!!.setLastHorizontalStyle(
                                State.Chain.getValueByString(hLastStyleValueStr),
                            )
                        }
                    }

                    else -> {
                        // Get the underlying reference for the flow, apply the constraints
                        // attributes to it
                        val reference = state.constraints(flowName)
                        applyAttribute(state, layoutVariables, reference!!, element, param)
                    }
                }
            }
        }

        @Throws(CLParsingException::class)
        fun parseGuideline(
            orientation: Int,
            state: State,
            helper: CLArray,
        ) {
            val params = helper[1] as? CLObject ?: return
            val guidelineId = params.getStringOrNull("id") ?: return
            parseGuidelineParams(orientation, state, guidelineId, params)
        }

        @Throws(CLParsingException::class)
        fun parseGuidelineParams(
            orientation: Int,
            state: State,
            guidelineId: String?,
            params: CLObject,
        ) {
            val constraints = params.names()
            val reference = state.constraints(guidelineId)
            if (orientation == HORIZONTAL) {
                state.horizontalGuideline(guidelineId)
            } else {
                state.verticalGuideline(guidelineId)
            }

            // Layout direction may be ignored for Horizontal guidelines (placed along the Y axis),
            // since `start` & `end` represent the `top` and `bottom` distances respectively.
            val isLtr = !state.isRtl || orientation == HORIZONTAL
            val guidelineReference: GuidelineReference =
                reference!!.getFacade()!! as GuidelineReference

            // Whether the guideline is based on percentage or distance
            var isPercent = false

            // Percent or distance value of the guideline
            var value = 0f

            // Indicates if the value is considered from the "start" position,
            // meaning "left" anchor for vertical guidelines and "top" anchor for
            // horizontal guidelines
            var fromStart = true
            for (constraintName in constraints) {
                when (constraintName) {
                    "left" -> {
                        value = toPix(state, params.getFloat(constraintName))
                        fromStart = true
                    }

                    "right" -> {
                        value = toPix(state, params.getFloat(constraintName))
                        fromStart = false
                    }

                    "start" -> {
                        value = toPix(state, params.getFloat(constraintName))
                        fromStart = isLtr
                    }

                    "end" -> {
                        value = toPix(state, params.getFloat(constraintName))
                        fromStart = !isLtr
                    }

                    "percent" -> {
                        isPercent = true
                        val percentParams = params.getArrayOrNull(constraintName)
                        if (percentParams == null) {
                            fromStart = true
                            value = params.getFloat(constraintName)
                        } else if (percentParams.size() > 1) {
                            val origin = percentParams.getString(0)
                            value = percentParams.getFloat(1)
                            when (origin) {
                                "left" -> fromStart = true
                                "right" -> fromStart = false
                                "start" -> fromStart = isLtr
                                "end" -> fromStart = !isLtr
                            }
                        }
                    }
                }
            }

            // Populate the guideline based on the resolved properties
            if (isPercent) {
                if (fromStart) {
                    guidelineReference.percent(value)
                } else {
                    guidelineReference.percent(1f - value)
                }
            } else {
                if (fromStart) {
                    guidelineReference.start(value)
                } else {
                    guidelineReference.end(value)
                }
            }
        }

        @Throws(CLParsingException::class)
        fun parseBarrier(
            state: State,
            elementName: String?,
            element: CLObject,
        ) {
            val isLtr = !state.isRtl
            val reference: BarrierReference? = state.barrier(elementName, State.Direction.END)
            val constraints = element.names()
            for (constraintName in constraints) {
                when (constraintName) {
                    "direction" -> {
                        when (element.getString(constraintName)) {
                            "start" -> if (isLtr) {
                                reference!!.setBarrierDirection(State.Direction.LEFT)
                            } else {
                                reference!!.setBarrierDirection(State.Direction.RIGHT)
                            }

                            "end" -> if (isLtr) {
                                reference!!.setBarrierDirection(State.Direction.RIGHT)
                            } else {
                                reference!!.setBarrierDirection(State.Direction.LEFT)
                            }

                            "left" -> reference!!.setBarrierDirection(State.Direction.LEFT)
                            "right" -> reference!!.setBarrierDirection(State.Direction.RIGHT)
                            "top" -> reference!!.setBarrierDirection(State.Direction.TOP)
                            "bottom" -> reference!!.setBarrierDirection(State.Direction.BOTTOM)
                        }
                    }

                    "margin" -> {
                        val margin = element.getFloatOrNaN(constraintName)
                        if (!margin.isNaN()) {
                            reference!!.margin(toPix(state, margin))
                        }
                    }

                    "contains" -> {
                        val list = element.getArrayOrNull(constraintName)
                        if (list != null) {
                            var j = 0
                            while (j < list.size()) {
                                val elementNameReference = list[j].content()
                                val elementReference = state.constraints(elementNameReference)
                                if (PARSER_DEBUG) {
                                    println(
                                        "Add REFERENCE " +
                                            "(\$elementNameReference = \$elementReference) " +
                                            "TO BARRIER ",
                                    )
                                }
                                reference!!.add(elementReference!!)
                                j++
                            }
                        }
                    }
                }
            }
        }

        @Throws(CLParsingException::class)
        fun parseWidget(
            state: State,
            layoutVariables: LayoutVariables?,
            elementName: String?,
            element: CLObject,
        ) {
            val reference = state.constraints(elementName)!!
            parseWidget(state, layoutVariables, reference, element)
        }

        /**
         * Set/apply attribute to a widget/helper reference
         *
         * @param state Constraint State
         * @param layoutVariables layout variables
         * @param reference widget/helper reference
         * @param element the parsed CLObject
         * @param attributeName Name of the attribute to be set/applied
         * @throws CLParsingException
         */
        @Throws(CLParsingException::class)
        fun applyAttribute(
            state: State,
            layoutVariables: LayoutVariables,
            reference: ConstraintReference,
            element: CLObject,
            attributeName: String?,
        ) {
            var value: Float
            when (attributeName) {
                "width" -> reference.setWidth(
                    parseDimension(
                        element,
                        attributeName,
                        state,
                        state.getDpToPixel()!!,
                    ),
                )

                "height" -> reference.setHeight(
                    parseDimension(
                        element,
                        attributeName,
                        state,
                        state.getDpToPixel()!!,
                    ),
                )

                "center" -> {
                    val target = element.getString(attributeName)
                    val targetReference: ConstraintReference?
                    targetReference = if (target == "parent") {
                        state.constraints(State.PARENT)
                    } else {
                        state.constraints(target)
                    }
                    reference.startToStart(targetReference!!)
                    reference.endToEnd(targetReference)
                    reference.topToTop(targetReference)
                    reference.bottomToBottom(targetReference)
                }

                "centerHorizontally" -> {
                    val target = element.getString(attributeName)
                    val targetReference =
                        if (target == "parent") {
                            state.constraints(State.PARENT)!!
                        } else {
                            state.constraints(
                                target,
                            )!!
                        }
                    reference.startToStart(targetReference)
                    reference.endToEnd(targetReference)
                }

                "centerVertically" -> {
                    val target = element.getString(attributeName)
                    val targetReference =
                        if (target == "parent") {
                            state.constraints(State.PARENT)!!
                        } else {
                            state.constraints(
                                target,
                            )!!
                        }
                    reference.topToTop(targetReference)
                    reference.bottomToBottom(targetReference)
                }

                "alpha" -> {
                    value = layoutVariables[element[attributeName]]
                    reference.alpha(value)
                }

                "scaleX" -> {
                    value = layoutVariables[element[attributeName]]
                    reference.scaleX(value)
                }

                "scaleY" -> {
                    value = layoutVariables[element[attributeName]]
                    reference.scaleY(value)
                }

                "translationX" -> {
                    value = layoutVariables[element[attributeName]]
                    reference.translationX(toPix(state, value))
                }

                "translationY" -> {
                    value = layoutVariables[element[attributeName]]
                    reference.translationY(toPix(state, value))
                }

                "translationZ" -> {
                    value = layoutVariables[element[attributeName]]
                    reference.translationZ(toPix(state, value))
                }

                "pivotX" -> {
                    value = layoutVariables[element[attributeName]]
                    reference.pivotX(value)
                }

                "pivotY" -> {
                    value = layoutVariables[element[attributeName]]
                    reference.pivotY(value)
                }

                "rotationX" -> {
                    value = layoutVariables[element[attributeName]]
                    reference.rotationX(value)
                }

                "rotationY" -> {
                    value = layoutVariables[element[attributeName]]
                    reference.rotationY(value)
                }

                "rotationZ" -> {
                    value = layoutVariables[element[attributeName]]
                    reference.rotationZ(value)
                }

                "visibility" -> when (element.getString(attributeName)) {
                    "visible" -> reference.visibility(ConstraintWidget.VISIBLE)
                    "invisible" -> {
                        reference.visibility(ConstraintWidget.INVISIBLE)
                        reference.alpha(0f)
                    }

                    "gone" -> reference.visibility(ConstraintWidget.GONE)
                }

                "vBias" -> {
                    value = layoutVariables[element[attributeName]]
                    reference.verticalBias(value)
                }

                "hRtlBias" -> {
                    // TODO: This is a temporary solution to support bias with start/end constraints,
                    //  where the bias needs to be reversed in RTL, we probably want a better or more
                    //  intuitive way to do this
                    value = layoutVariables[element[attributeName]]
                    if (state.isRtl) {
                        value = 1f - value
                    }
                    reference.horizontalBias(value)
                }

                "hBias" -> {
                    value = layoutVariables[element[attributeName]]
                    reference.horizontalBias(value)
                }

                "vWeight" -> {
                    value = layoutVariables[element[attributeName]]
                    reference.setVerticalChainWeight(value)
                }

                "hWeight" -> {
                    value = layoutVariables[element[attributeName]]
                    reference.setHorizontalChainWeight(value)
                }

                "custom" -> parseCustomProperties(element, reference, attributeName)
                "motion" -> parseMotionProperties(element[attributeName], reference)
                else -> parseConstraint(state, layoutVariables, element, reference, attributeName)
            }
        }

        @Throws(CLParsingException::class)
        fun parseWidget(
            state: State?,
            layoutVariables: LayoutVariables?,
            reference: ConstraintReference,
            element: CLObject,
        ) {
            if (reference.width == null) {
                // Default to Wrap when the Dimension has not been assigned
                reference.setWidth(Dimension.createWrap())
            }
            if (reference.height == null) {
                // Default to Wrap when the Dimension has not been assigned
                reference.setHeight(Dimension.createWrap())
            }
            val constraints = element.names()
            for (constraintName in constraints) {
                applyAttribute(state!!, layoutVariables!!, reference, element, constraintName)
            }
        }

        @Throws(CLParsingException::class)
        fun parseCustomProperties(
            element: CLObject,
            reference: ConstraintReference,
            constraintName: String?,
        ) {
            val json = element.getObjectOrNull(constraintName) ?: return
            val properties = json.names()
            for (property in properties) {
                val value = json[property]
                if (value is CLNumber) {
                    reference.addCustomFloat(property, value.float)
                } else if (value is CLString) {
                    val it: Long = parseColorString(value.content())
                    if (it != -1L) {
                        reference.addCustomColor(property, it.toInt())
                    }
                }
            }
        }

        private fun indexOf(`val`: String, vararg types: String): Int {
            for (i in types.indices) {
                if (types[i] == `val`) {
                    return i
                }
            }
            return -1
        }

        /**
         * parse the motion section of a constraint
         * <pre>
         * csetName: {
         * idToConstrain : {
         * motion: {
         * pathArc : 'startVertical'
         * relativeTo: 'id'
         * easing: 'curve'
         * stagger: '2'
         * quantize: steps or [steps, 'interpolator' phase ]
         * }
         * }
         * }
         </pre> *
         */
        @Throws(CLParsingException::class)
        private fun parseMotionProperties(
            element: CLElement,
            reference: ConstraintReference,
        ) {
            if (element !is CLObject) {
                return
            }
            val obj = element
            val bundle = TypedBundle()
            val constraints = obj.names()
            for (constraintName in constraints) {
                when (constraintName) {
                    "pathArc" -> {
                        val `val` = obj.getString(constraintName)
                        val ord = indexOf(
                            `val`,
                            "none",
                            "startVertical",
                            "startHorizontal",
                            "flip",
                            "below",
                            "above",
                        )
                        if (ord == -1) {
                            println(
                                obj.getLine().toString() + " pathArc = '" + `val` + "'",
                            )
                            break
                        }
                        bundle.add(TypedValues.MotionType.TYPE_PATHMOTION_ARC, ord)
                    }

                    "relativeTo" -> bundle.add(
                        TypedValues.MotionType.TYPE_ANIMATE_RELATIVE_TO,
                        obj.getString(constraintName),
                    )

                    "easing" -> bundle.add(
                        TypedValues.MotionType.TYPE_EASING,
                        obj.getString(constraintName),
                    )

                    "stagger" -> bundle.add(
                        TypedValues.MotionType.TYPE_STAGGER,
                        obj.getFloat(constraintName),
                    )

                    "quantize" -> {
                        val quant = obj[constraintName]
                        if (quant is CLArray) {
                            val array = quant
                            val len = array.size()
                            if (len > 0) {
                                bundle.add(TYPE_QUANTIZE_MOTIONSTEPS, array.getInt(0))
                                if (len > 1) {
                                    bundle.add(TYPE_QUANTIZE_INTERPOLATOR_TYPE, array.getString(1))
                                    if (len > 2) {
                                        bundle.add(TYPE_QUANTIZE_MOTION_PHASE, array.getFloat(2))
                                    }
                                }
                            }
                        } else {
                            bundle.add(TYPE_QUANTIZE_MOTIONSTEPS, obj.getInt(constraintName))
                        }
                    }
                }
            }
            reference.mMotionProperties = bundle
        }

        @Throws(CLParsingException::class)
        fun parseConstraint(
            state: State,
            layoutVariables: LayoutVariables,
            element: CLObject,
            reference: ConstraintReference,
            constraintName: String?,
        ) {
            val isLtr = !state.isRtl
            val constraint = element.getArrayOrNull(constraintName)
            if (constraint != null && constraint.size() > 1) {
                // params: target, anchor
                val target = constraint.getString(0)
                val anchor = constraint.getStringOrNull(1)
                var margin = 0f
                var marginGone = 0f
                if (constraint.size() > 2) {
                    // params: target, anchor, margin
                    val arg2 = constraint.getOrNull(2)
                    margin = layoutVariables[arg2!!]
                    margin = toPix(state, margin)
                }
                if (constraint.size() > 3) {
                    // params: target, anchor, margin, marginGone
                    val arg2 = constraint.getOrNull(3)
                    marginGone = layoutVariables[arg2!!]
                    marginGone = toPix(state, marginGone)
                }
                val targetReference =
                    if (target == "parent") {
                        state.constraints(State.PARENT)
                    } else {
                        state.constraints(
                            target,
                        )
                    }

                // For simplicity, we'll apply horizontal constraints separately
                var isHorizontalConstraint = false
                var isHorOriginLeft = true
                var isHorTargetLeft = true
                when (constraintName) {
                    "circular" -> {
                        val angle = layoutVariables[constraint[1]]
                        var distance = 0f
                        if (constraint.size() > 2) {
                            val distanceArg = constraint.getOrNull(2)
                            distance = layoutVariables[distanceArg!!]
                            distance = toPix(state, distance)
                        }
                        reference.circularConstraint(targetReference, angle, distance)
                    }

                    "top" -> when (anchor) {
                        "top" -> reference.topToTop(targetReference!!)
                        "bottom" -> reference.topToBottom(targetReference!!)
                        "baseline" -> {
                            state.baselineNeededFor(targetReference!!.getKey()!!)
                            reference.topToBaseline(targetReference)
                        }
                    }

                    "bottom" -> when (anchor) {
                        "top" -> reference.bottomToTop(targetReference!!)
                        "bottom" -> reference.bottomToBottom(targetReference!!)
                        "baseline" -> {
                            state.baselineNeededFor(targetReference!!.getKey()!!)
                            reference.bottomToBaseline(targetReference)
                        }
                    }

                    "baseline" -> when (anchor) {
                        "baseline" -> {
                            state.baselineNeededFor(reference.getKey()!!)
                            state.baselineNeededFor(targetReference!!.getKey()!!)
                            reference.baselineToBaseline(targetReference)
                        }

                        "top" -> {
                            state.baselineNeededFor(reference.getKey()!!)
                            reference.baselineToTop(targetReference!!)
                        }

                        "bottom" -> {
                            state.baselineNeededFor(reference.getKey()!!)
                            reference.baselineToBottom(targetReference!!)
                        }
                    }

                    "left" -> {
                        isHorizontalConstraint = true
                        isHorOriginLeft = true
                    }

                    "right" -> {
                        isHorizontalConstraint = true
                        isHorOriginLeft = false
                    }

                    "start" -> {
                        isHorizontalConstraint = true
                        isHorOriginLeft = isLtr
                    }

                    "end" -> {
                        isHorizontalConstraint = true
                        isHorOriginLeft = !isLtr
                    }
                }
                if (isHorizontalConstraint) {
                    // Resolve horizontal target anchor
                    when (anchor) {
                        "left" -> isHorTargetLeft = true
                        "right" -> isHorTargetLeft = false
                        "start" -> isHorTargetLeft = isLtr
                        "end" -> isHorTargetLeft = !isLtr
                    }

                    // Resolved anchors, apply corresponding constraint
                    if (isHorOriginLeft) {
                        if (isHorTargetLeft) {
                            reference.leftToLeft(targetReference!!)
                        } else {
                            reference.leftToRight(targetReference!!)
                        }
                    } else {
                        if (isHorTargetLeft) {
                            reference.rightToLeft(targetReference!!)
                        } else {
                            reference.rightToRight(targetReference!!)
                        }
                    }
                }
                reference.margin(margin).marginGone(marginGone)
            } else {
                val target = element.getStringOrNull(constraintName)
                if (target != null) {
                    val targetReference =
                        if (target == "parent") {
                            state.constraints(State.PARENT)
                        } else {
                            state.constraints(
                                target,
                            )
                        }
                    when (constraintName) {
                        "start" -> if (isLtr) {
                            reference.leftToLeft(targetReference!!)
                        } else {
                            reference.rightToRight(targetReference!!)
                        }

                        "end" -> if (isLtr) {
                            reference.rightToRight(targetReference!!)
                        } else {
                            reference.leftToLeft(targetReference!!)
                        }

                        "top" -> reference.topToTop(targetReference!!)
                        "bottom" -> reference.bottomToBottom(targetReference!!)
                        "baseline" -> {
                            state.baselineNeededFor(reference.getKey()!!)
                            state.baselineNeededFor(targetReference!!.getKey()!!)
                            reference.baselineToBaseline(targetReference)
                        }
                    }
                }
            }
        }

        fun parseDimensionMode(dimensionString: String): Dimension {
            var dimension = Dimension.createFixed(0)
            when (dimensionString) {
                "wrap" -> dimension = Dimension.createWrap()
                "preferWrap" -> dimension = Dimension.createSuggested(Dimension.WRAP_DIMENSION)
                "spread" -> dimension = Dimension.createSuggested(Dimension.SPREAD_DIMENSION)
                "parent" -> dimension = Dimension.createParent()
                else -> {
                    if (dimensionString.endsWith("%")) {
                        // parent percent
                        val percentString =
                            dimensionString.substring(0, dimensionString.indexOf('%'))
                        val percentValue = percentString.toFloat() / 100f
                        dimension = Dimension.createPercent(0, percentValue).suggested(0)
                    } else if (dimensionString.contains(":")) {
                        dimension = Dimension.createRatio(dimensionString)
                            .suggested(Dimension.SPREAD_DIMENSION)
                    }
                }
            }
            return dimension
        }

        @Throws(CLParsingException::class)
        fun parseDimension(
            element: CLObject,
            constraintName: String?,
            state: State,
            dpToPixels: CorePixelDp,
        ): Dimension {
            val dimensionElement = element[constraintName!!]
            var dimension: Dimension = Dimension.createFixed(0)
            if (dimensionElement is CLString) {
                dimension = parseDimensionMode(dimensionElement.content())
            } else if (dimensionElement is CLNumber) {
                dimension = Dimension.createFixed(
                    state.convertDimension(dpToPixels.toPixels(element.getFloat(constraintName))),
                )
            } else if (dimensionElement is CLObject) {
                val obj = dimensionElement
                val mode = obj.getStringOrNull("value")
                if (mode != null) {
                    dimension = parseDimensionMode(mode)
                }
                val minEl = obj.getOrNull("min")
                if (minEl != null) {
                    if (minEl is CLNumber) {
                        val min = minEl.float
                        dimension.min(state.convertDimension(dpToPixels.toPixels(min)))
                    } else if (minEl is CLString) {
                        dimension.min(Dimension.WRAP_DIMENSION)
                    }
                }
                val maxEl = obj.getOrNull("max")
                if (maxEl != null) {
                    if (maxEl is CLNumber) {
                        val max = maxEl.float
                        dimension.max(state.convertDimension(dpToPixels.toPixels(max)))
                    } else if (maxEl is CLString) {
                        dimension.max(Dimension.WRAP_DIMENSION)
                    }
                }
            }
            return dimension
        }

        /**
         * parse a color string
         *
         * @return -1 if it cannot parse unsigned long
         */
        fun parseColorString(value: String): Long {
            var str = value
            return if (str.startsWith("#")) {
                str = str.substring(1)
                if (str.length == 6) {
                    str = "FF$str"
                }
                str.toLong(16)
            } else {
                -1L
            }
        }

        @Throws(CLParsingException::class)
        fun lookForType(element: CLObject): String? {
            val constraints = element.names()
            for (constraintName in constraints) {
                if (constraintName == "type") {
                    return element.getString("type")
                }
            }
            return null
        }
    }
}
