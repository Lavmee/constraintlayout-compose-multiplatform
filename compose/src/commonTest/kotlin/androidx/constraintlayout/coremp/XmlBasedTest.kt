/*
 * Copyright (C) 2016 The Android Open Source Project
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
package androidx.constraintlayout.coremp

// import androidx.constraintlayout.core.widgets.ConstraintAnchor
// import androidx.constraintlayout.core.widgets.Guideline
// import androidx.constraintlayout.core.widgets.Optimizer
// import kotlin.math.abs
//
// /**
// * This test the ConstraintWidget system buy loading XML that contain tags with there positions.
// * the xml files can be designed in android studio.studio
// */
// @RunWith(Parameterized::class)
// class XmlBasedTest(var mFile: String) {
//    var mWidgetMap: java.util.HashMap<String, ConstraintWidget>? = null
//    var mBoundsMap: java.util.HashMap<ConstraintWidget, String>? = null
//    var mContainer: ConstraintWidgetContainer? = null
//    var mConnectionList: java.util.ArrayList<Connection>? = null
//
//    internal class Connection {
//        var mFromWidget: ConstraintWidget? = null
//        var mFromType: ConstraintAnchor.Type? = null
//        var mToType: ConstraintAnchor.Type? = null
//        var mToName: String? = null
//        var mMargin = 0
//        var mGonMargin = Int.MIN_VALUE
//    }
//
//    @Test
//    fun testAccessToResources() {
//        val dirName = dir
//        assertTrue(" could not find dir $dirName", java.io.File(dirName).exists())
//        val names = genListOfName()
//        assertTrue(" Could not get Path $dirName", names.size > 1)
//    }
//
//    fun dim(w: ConstraintWidget): String {
//        if (w is Guideline) {
//            return w.left.toString() + "," + w.top + "," + 0 + "," + 0
//        }
//        return if (w.getVisibility() == ConstraintWidget.GONE) {
//            0.toString() + "," + 0 + "," + 0 + "," + 0
//        } else w.left.toString() + "," + w.top + "," + w.width + "," + w.height
//    }
//
//    @Test
//    fun testSolverXML() {
//        parseXML(mFile)
//        mContainer.optimizationLevel = Optimizer.OPTIMIZATION_NONE
//        val perm = IntArray(mBoundsMap.size)
//        for (i in perm.indices) {
//            perm[i] = i
//        }
//        val total = fact(perm.size)
//        val skip = 1 + total / 1000
//        populateContainer(perm)
//        makeConnections()
//        layout()
//        validate()
//        var k = 0
//        while (nextPermutation(perm)) {
//            k++
//            if (k % skip != 0) continue
//            populateContainer(perm)
//            makeConnections()
//            layout()
//            validate()
//        }
//    }
//
//    @Test
//    fun testDirectResolutionXML() {
//        parseXML(mFile)
//        mContainer.optimizationLevel = Optimizer.OPTIMIZATION_STANDARD
//        val perm = IntArray(mBoundsMap.size)
//        for (i in perm.indices) {
//            perm[i] = i
//        }
//        val total = fact(perm.size)
//        val skip = 1 + total / 1000
//        populateContainer(perm)
//        makeConnections()
//        layout()
//        validate()
//        var k = 0
//        while (nextPermutation(perm)) {
//            k++
//            if (k % skip != 0) continue
//            populateContainer(perm)
//            makeConnections()
//            layout()
//            validate()
//        }
//    }
//
//    /**
//     * Compare two string containing comer separated integers
//     */
//    private fun isSame(a: String?, b: String?): Boolean {
//        if (a == null || b == null) {
//            return false
//        }
//        val a_split = a.split(",".toRegex()).dropLastWhile { it.isEmpty() }
//            .toTypedArray()
//        val b_split = b.split(",".toRegex()).dropLastWhile { it.isEmpty() }
//            .toTypedArray()
//        if (a_split.size != b_split.size) {
//            return false
//        }
//        for (i in a_split.indices) {
//            if (a_split[i].length == 0) {
//                return false
//            }
//            var error = ALLOWED_POSITION_ERROR
//            if (b_split[i].startsWith("+")) {
//                error += 10
//            }
//            val a_value = a_split[i].toInt()
//            val b_value = b_split[i].toInt()
//            if (abs((a_value - b_value).toDouble()) > error) {
//                return false
//            }
//        }
//        return true
//    }
//
//    /**
//     * parse the XML file
//     */
//    private fun parseXML(fileName: String) {
//        java.lang.System.err.println(fileName)
//        mContainer = ConstraintWidgetContainer(0, 0, 1080, 1920)
//        mContainer.debugName = "parent"
//        mWidgetMap = java.util.HashMap<String, ConstraintWidget>()
//        mBoundsMap = java.util.HashMap<ConstraintWidget, String>()
//        mConnectionList = java.util.ArrayList<Connection>()
//        val handler: org.xml.sax.helpers.DefaultHandler =
//            object : org.xml.sax.helpers.DefaultHandler() {
//                var mParentId: String? = null
//                @Throws(org.xml.sax.SAXException::class)
//                override fun startDocument() {
//                }
//
//                @Throws(org.xml.sax.SAXException::class)
//                override fun endDocument() {
//                }
//
//                @Throws(org.xml.sax.SAXException::class)
//                override fun startElement(
//                    namespaceURI: String,
//                    localName: String,
//                    qName: String,
//                    attributes: org.xml.sax.Attributes
//                ) {
//                    if (qName != null) {
//                        val androidAttrs: MutableMap<String, String> =
//                            java.util.HashMap<String, String>()
//                        val appAttrs: MutableMap<String, String> =
//                            java.util.HashMap<String, String>()
//                        val widgetConstraints: MutableMap<String, String> =
//                            java.util.HashMap<String, String>()
//                        val widgetGoneMargins: MutableMap<String, String> =
//                            java.util.HashMap<String, String>()
//                        val widgetMargins: MutableMap<String, String> =
//                            java.util.HashMap<String, String>()
//                        for (i in 0 until attributes.getLength()) {
//                            val attrName: String = attributes.getLocalName(i)
//                            val attrValue: String = attributes.getValue(i)
//                            if (!attrName.contains(":")) {
//                                continue
//                            }
//                            if (attrValue.trim { it <= ' ' }.isEmpty()) {
//                                continue
//                            }
//                            val parts = attrName.split(":".toRegex()).dropLastWhile { it.isEmpty() }
//                                .toTypedArray()
//                            val scheme = parts[0]
//                            val attr = parts[1]
//                            if (scheme == "android") {
//                                androidAttrs[attr] = attrValue
//                                if (attr.startsWith("layout_margin")) {
//                                    widgetMargins[attr] = attrValue
//                                }
//                            } else if (scheme == "app") {
//                                appAttrs[attr] = attrValue
//                                if (attr == "layout_constraintDimensionRatio") {
//                                    // do nothing
//                                } else if (attr == "layout_constraintGuide_begin") {
//                                    // do nothing
//                                } else if (attr == "layout_constraintGuide_percent") {
//                                    // do nothing
//                                } else if (attr == "layout_constraintGuide_end") {
//                                    // do nothing
//                                } else if (attr == "layout_constraintHorizontal_bias") {
//                                    // do nothing
//                                } else if (attr == "layout_constraintVertical_bias") {
//                                    // do nothing
//                                } else if (attr.startsWith("layout_constraint")) {
//                                    widgetConstraints[attr] = attrValue
//                                }
//                                if (attr.startsWith("layout_goneMargin")) {
//                                    widgetGoneMargins[attr] = attrValue
//                                }
//                            }
//                        }
//                        val id = androidAttrs["id"]
//                        val tag = androidAttrs["tag"]
//                        val layoutWidth = parseDim(
//                            androidAttrs["layout_width"]
//                        )
//                        val layoutHeight = parseDim(
//                            androidAttrs["layout_height"]
//                        )
//                        val text = androidAttrs["text"]
//                        val visibility = androidAttrs["visibility"]
//                        val orientation = androidAttrs["orientation"]
//                        if (qName.endsWith("ConstraintLayout")) {
//                            if (id != null) {
//                                mContainer.debugName = id
//                            }
//                            mWidgetMap.put(mContainer.debugName, mContainer)
//                            mWidgetMap.put("parent", mContainer)
//                        } else if (qName.endsWith("Guideline")) {
//                            val guideline = Guideline()
//                            if (id != null) {
//                                guideline.debugName = id
//                            }
//                            mWidgetMap.put(guideline.debugName, guideline)
//                            mBoundsMap.put(guideline, tag)
//                            val horizontal = "horizontal" == orientation
//                            println(
//                                "Guideline " + id + " "
//                                        + if (horizontal) "HORIZONTAL" else "VERTICAL"
//                            )
//                            guideline.setOrientation(if (horizontal) Guideline.HORIZONTAL else Guideline.VERTICAL)
//                            val constraintGuideBegin = appAttrs["layout_constraintGuide_begin"]
//                            val constraintGuidePercent = appAttrs["layout_constraintGuide_percent"]
//                            val constraintGuideEnd = appAttrs["layout_constraintGuide_end"]
//                            if (constraintGuideBegin != null) {
//                                guideline.setGuideBegin(parseDim(constraintGuideBegin))
//                                println(
//                                    "Guideline " + id
//                                            + " setGuideBegin " + parseDim(constraintGuideBegin)
//                                )
//                            } else if (constraintGuidePercent != null) {
//                                guideline.setGuidePercent(constraintGuidePercent.toFloat())
//                                println(
//                                    "Guideline " + id + " setGuidePercent "
//                                            + constraintGuidePercent.toFloat()
//                                )
//                            } else if (constraintGuideEnd != null) {
//                                guideline.setGuideEnd(parseDim(constraintGuideEnd))
//                                println(
//                                    "Guideline " + id
//                                            + " setGuideBegin " + parseDim(constraintGuideEnd)
//                                )
//                            }
//                            println(">>>>>>>>>>>>  $guideline")
//                        } else {
//                            val widget = ConstraintWidget(200, 51)
//                            widget.setBaselineDistance(28)
//                            val connect = arrayOfNulls<Connection>(5)
//                            val widgetLayoutConstraintDimensionRatio =
//                                appAttrs["layout_constraintDimensionRatio"]
//                            val widgetLayoutConstraintHorizontalBias =
//                                appAttrs["layout_constraintHorizontal_bias"]
//                            val widgetLayoutConstraintVerticalBias =
//                                appAttrs["layout_constraintVertical_bias"]
//                            if (id != null) {
//                                widget.debugName = id
//                            } else {
//                                widget.debugName = "widget" + (mWidgetMap.size + 1)
//                            }
//                            if (tag != null) {
//                                mBoundsMap.put(widget, tag)
//                            }
//                            var hBehaviour: DimensionBehaviour =
//                                ConstraintWidget.DimensionBehaviour.FIXED
//                            if (layoutWidth == 0) {
//                                hBehaviour = ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT
//                                widget.setDimension(layoutWidth, widget.height)
//                            } else if (layoutWidth == -1) {
//                                hBehaviour = ConstraintWidget.DimensionBehaviour.WRAP_CONTENT
//                            } else {
//                                widget.setDimension(layoutWidth, widget.height)
//                            }
//                            widget.setHorizontalDimensionBehaviour(hBehaviour)
//                            var vBehaviour: DimensionBehaviour =
//                                ConstraintWidget.DimensionBehaviour.FIXED
//                            if (layoutHeight == 0) {
//                                vBehaviour = ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT
//                                widget.setDimension(widget.width, layoutHeight)
//                            } else if (layoutHeight == -1) {
//                                vBehaviour = ConstraintWidget.DimensionBehaviour.WRAP_CONTENT
//                            } else {
//                                widget.setDimension(widget.width, layoutHeight)
//                            }
//                            widget.setVerticalDimensionBehaviour(vBehaviour)
//                            if (text != null) {
//                                print("text = \"$text\"")
//                                val wmap: Map<String, Int> =
//                                    if (qName == "Button") sButtonWidthMap else sStringWidthMap
//                                val hmap: Map<String, Int> =
//                                    if (qName == "Button") sButtonHeightMap else sStringHeightMap
//                                if (wmap.containsKey(text) && widget.horizontalDimensionBehaviour
//                                    === ConstraintWidget.DimensionBehaviour.WRAP_CONTENT
//                                ) {
//                                    widget.width = wmap[text]
//                                }
//                                if (hmap.containsKey(text) && widget.verticalDimensionBehaviour
//                                    === ConstraintWidget.DimensionBehaviour.WRAP_CONTENT
//                                ) {
//                                    widget.height = hmap[text]
//                                }
//                            }
//                            if (visibility != null) {
//                                widget.setVisibility(sVisibilityMap.get(visibility))
//                            }
//                            if (widgetLayoutConstraintDimensionRatio != null) {
//                                widget.setDimensionRatio(widgetLayoutConstraintDimensionRatio)
//                            }
//                            if (widgetLayoutConstraintHorizontalBias != null) {
//                                println(
//                                    "widgetLayoutConstraintHorizontalBias "
//                                            + widgetLayoutConstraintHorizontalBias
//                                )
//                                widget.setHorizontalBiasPercent(widgetLayoutConstraintHorizontalBias.toFloat())
//                            }
//                            if (widgetLayoutConstraintVerticalBias != null) {
//                                println(
//                                    "widgetLayoutConstraintVerticalBias "
//                                            + widgetLayoutConstraintVerticalBias
//                                )
//                                widget.setVerticalBiasPercent(widgetLayoutConstraintVerticalBias.toFloat())
//                            }
//                            val constraintKeySet: Set<String> = widgetConstraints.keys
//                            val constraintKeys = constraintKeySet.toTypedArray<String>()
//                            for (i in constraintKeys.indices) {
//                                val attrName = constraintKeys[i]
//                                val attrValue = widgetConstraints[attrName]
//                                val sp = attrName.substring("layout_constraint".length)
//                                    .split("_to".toRegex()).dropLastWhile { it.isEmpty() }
//                                    .toTypedArray()
//                                val fromString = rtl(
//                                    sp[0].uppercase(java.util.Locale.getDefault())
//                                )
//                                val from: ConstraintAnchor.Type =
//                                    ConstraintAnchor.Type.valueOf(fromString)
//                                val toString = rtl(
//                                    sp[1].substring(
//                                        0,
//                                        sp[1].length - 2
//                                    ).uppercase(java.util.Locale.getDefault())
//                                )
//                                val to: ConstraintAnchor.Type =
//                                    ConstraintAnchor.Type.valueOf(toString)
//                                val side = from.ordinal - 1
//                                if (connect[side] == null) {
//                                    connect[side] = Connection()
//                                }
//                                connect[side].mFromWidget = widget
//                                connect[side].mFromType = from
//                                connect[side].mToType = to
//                                connect[side].mToName = attrValue
//                            }
//                            val goneMarginSet: Set<String> = widgetGoneMargins.keys
//                            val goneMargins = goneMarginSet.toTypedArray<String>()
//                            for (i in goneMargins.indices) {
//                                val attrName = goneMargins[i]
//                                val attrValue = widgetGoneMargins[attrName]
//                                val marginSide = rtl(
//                                    attrName.substring("layout_goneMargin".length)
//                                        .uppercase(java.util.Locale.getDefault())
//                                )
//                                val marginType: ConstraintAnchor.Type =
//                                    ConstraintAnchor.Type.valueOf(marginSide)
//                                val side = marginType.ordinal - 1
//                                if (connect[side] == null) {
//                                    connect[side] = Connection()
//                                }
//                                connect[side].mGonMargin = 3 *
//                                        attrValue.substring(0, attrValue.length - 2)
//                                            .toInt()
//                            }
//                            val marginSet: Set<String> = widgetMargins.keys
//                            val margins = marginSet.toTypedArray<String>()
//                            for (i in margins.indices) {
//                                val attrName = margins[i]
//                                val attrValue = widgetMargins[attrName]
//                                // System.out.println("margin [" + attrName + "]
//                                //    by [" + attrValue +"]");
//                                val marginSide = rtl(
//                                    attrName.substring("layout_margin".length)
//                                        .uppercase(java.util.Locale.getDefault())
//                                )
//                                val marginType: ConstraintAnchor.Type =
//                                    ConstraintAnchor.Type.valueOf(marginSide)
//                                val side = marginType.ordinal - 1
//                                if (connect[side] == null) {
//                                    connect[side] = Connection()
//                                }
//                                connect[side].mMargin = 3 *
//                                        attrValue.substring(0, attrValue.length - 2).toInt()
//                            }
//                            mWidgetMap.put(widget.debugName, widget)
//                            for (i in connect.indices) {
//                                if (connect[i] != null) {
//                                    mConnectionList.add(connect[i])
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        val file: java.io.File = java.io.File(fileName)
//        val spf: javax.xml.parsers.SAXParserFactory =
//            javax.xml.parsers.SAXParserFactory.newInstance()
//        try {
//            val saxParser: javax.xml.parsers.SAXParser = spf.newSAXParser()
//            val xmlReader: org.xml.sax.XMLReader = saxParser.getXMLReader()
//            xmlReader.setContentHandler(handler)
//            xmlReader.parse(file.toURI().toString())
//        } catch (e: java.lang.Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    private fun populateContainer(order: IntArray) {
//        println(order.contentToString())
//        val widgetSet: Array<ConstraintWidget> = mBoundsMap.keys.toTypedArray<ConstraintWidget>()
//        for (i in widgetSet.indices) {
//            val widget: ConstraintWidget = widgetSet[order[i]]
//            if (widget.debugName == "parent") {
//                continue
//            }
//            val hBehaviour: DimensionBehaviour = widget.horizontalDimensionBehaviour
//            val vBehaviour: DimensionBehaviour = widget.verticalDimensionBehaviour
//            if (widget is Guideline) {
//                val copy = Guideline()
//                copy.copy(widget, java.util.HashMap<Any, Any>())
//                mContainer.remove(widget)
//                widget.copy(copy, java.util.HashMap<Any, Any>())
//            } else {
//                val copy = ConstraintWidget()
//                copy.copy(widget, java.util.HashMap<Any, Any>())
//                mContainer.remove(widget)
//                widget.copy(copy, java.util.HashMap<Any, Any>())
//            }
//            widget.setHorizontalDimensionBehaviour(hBehaviour)
//            widget.setVerticalDimensionBehaviour(vBehaviour)
//            mContainer.add(widget)
//        }
//    }
//
//    private fun makeConnections() {
//        for (connection in mConnectionList) {
//            var toConnect: ConstraintWidget
//            toConnect = if (connection.mToName.equals(
//                    "parent",
//                    ignoreCase = true
//                ) || connection.mToName == mContainer.debugName
//            ) {
//                mContainer
//            } else {
//                mWidgetMap.get(connection.mToName)
//            }
//            if (toConnect == null) {
//                java.lang.System.err.println("   " + connection.mToName)
//            } else {
//                connection.mFromWidget.connect(
//                    connection.mFromType,
//                    toConnect, connection.mToType, connection.mMargin
//                )
//                connection.mFromWidget.setGoneMargin(connection.mFromType, connection.mGonMargin)
//            }
//        }
//    }
//
//    private fun layout() {
//        mContainer.layout()
//    }
//
//    private fun validate() {
//        val root: ConstraintWidgetContainer =
//            mWidgetMap.remove("parent") as ConstraintWidgetContainer
//        val keys: Array<String> = mWidgetMap.keys.toTypedArray<String>()
//        var ok = true
//        val layout: java.lang.StringBuilder = java.lang.StringBuilder("\n")
//        for (key in keys) {
//            if (key.contains("activity_main")) {
//                continue
//            }
//            val widget: ConstraintWidget = mWidgetMap.get(key)
//            val bounds: String = mBoundsMap.get(widget)
//            val dim = dim(widget)
//            val same = isSame(dim, bounds)
//            val compare = rightPad(key, 17) + rightPad(dim, 15) + "   " + bounds
//            ok = ok and same
//            layout.append(compare).append("\n")
//        }
//        assertTrue(layout.toString(), ok)
//    }
//
//    @Test
//    fun simpleTest() {
//        val root = ConstraintWidgetContainer(0, 0, 1080, 1920)
//        val a = ConstraintWidget(0, 0, 200, 51)
//        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
//        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
//        a.debugName = "A"
//        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 0)
//        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 0)
//        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 0)
//        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 0)
//        root.add(a)
//        root.layout()
//        println("f) A: " + a + " " + a.width + "," + a.height)
//    }
//
//    @Test
//    fun guideLineTest() {
//        val root = ConstraintWidgetContainer(0, 0, 1080, 1920)
//        val a = ConstraintWidget(0, 0, 200, 51)
//        val guideline = Guideline()
//        root.add(guideline)
//        guideline.setGuidePercent(0.50f)
//        guideline.setOrientation(Guideline.VERTICAL)
//        guideline.debugName = "guideline"
//        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
//        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
//        a.debugName = "A"
//        a.connect(ConstraintAnchor.Type.LEFT, guideline, ConstraintAnchor.Type.LEFT, 0)
//        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 0)
//        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 0)
//        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 0)
//        root.add(a)
//        root.layout()
//        println("f) A: " + a + " " + a.width + "," + a.height)
//        println(
//            "f) A: " + guideline + " "
//                    + guideline.width + "," + guideline.height
//        )
//    }
//
//    companion object {
//        private const val ALLOWED_POSITION_ERROR = 1
//        private val sVisibilityMap: java.util.HashMap<String, Int> =
//            java.util.HashMap<String, Int>()
//        private val sStringWidthMap: MutableMap<String, Int> = java.util.HashMap<String, Int>()
//        private val sStringHeightMap: MutableMap<String, Int> = java.util.HashMap<String, Int>()
//        private val sButtonWidthMap: MutableMap<String, Int> = java.util.HashMap<String, Int>()
//        private val sButtonHeightMap: MutableMap<String, Int> = java.util.HashMap<String, Int>()
//
//        init {
//            sVisibilityMap.put("gone", ConstraintWidget.GONE)
//            sVisibilityMap.put("visible", ConstraintWidget.VISIBLE)
//            sVisibilityMap.put("invisible", ConstraintWidget.INVISIBLE)
//            sStringWidthMap["TextView"] = 171
//            sStringWidthMap["Button"] = 107
//            sStringWidthMap["Hello World!"] = 200
//            sStringHeightMap["TextView"] = 57
//            sStringHeightMap["Button"] = 51
//            sStringHeightMap["Hello World!"] = 51
//            val s = ("12345678 12345678 12345678 12345678 12345678 12345678 12345678 "
//                    + "12345678 12345678 12345678 12345678 12345678 12345678 12345678")
//            sStringWidthMap[s] = 984
//            sStringHeightMap[s] = 204
//            sButtonWidthMap["Button"] = 264
//            sButtonHeightMap["Button"] = 144
//        }
//
//        private fun rtl(v: String): String {
//            if (v == "START") return "LEFT"
//            return if (v == "END") "RIGHT" else v
//        }
//
//        private val dir: String
//            private get() =//        String dirName = System.getProperty("user.dir")
// //          + File.separator+".."+File.separator+".."+File.separator+".."
// //          +File.separator+"constraintLayout"+File.separator+"core"+File.separator
// //          +"src"+File.separator+"test"+File.separator+"resources"+File.separator;
//                java.lang.System.getProperty("user.dir") + "/src/test/resources/"
//
//        @Parameterized.Parameters
//        fun genListOfName(): Array<Array<Any?>> {
//            val dirName = dir
//            assertTrue(java.io.File(dirName).exists())
//            val f: Array<java.io.File> = java.io.File(dirName)
//                .listFiles(java.io.FileFilter { pathname: java.io.File ->
//                    pathname.getName().startsWith("check")
//                })
//            assertNotNull(f)
//            java.util.Arrays.sort<java.io.File>(
//                f,
//                java.util.Comparator<java.io.File> { o1: java.io.File, o2: java.io.File ->
//                    o1.getName().compareTo(o2.getName())
//                })
//            val ret = Array(f.size) { arrayOfNulls<Any>(1) }
//            for (i in ret.indices) {
//                ret[i][0] = f[i].getAbsolutePath()
//            }
//            return ret
//        }
//
//        /**
//         * Calculate the Factorial of n
//         *
//         * @param n input number
//         * @return Factorial of n
//         */
//        fun fact(n: Int): Int {
//            var n = n
//            var ret = 1
//            while (n > 0) {
//                ret *= n--
//            }
//            return ret
//        }
//
//        /**
//         * Simple dimension parser
//         * Multiply dp units by 3 because we simulate a screen with 3 pixels per dp
//         */
//        fun parseDim(dim: String?): Int {
//            if (dim.endsWith("dp")) {
//                return 3 * dim.substring(0, dim.length - 2).toInt()
//            }
//            return if (dim == "wrap_content") {
//                -1
//            } else -2
//        }
//
//        private fun rightPad(s: String, n: Int): String {
//            var s = s
//            s = s + String(ByteArray(n)).replace('\u0000', ' ')
//            return s.substring(0, n)
//        }
//
//        private fun r(s: String): String {
//            var s = s
//            s = "             $s"
//            return s.substring(s.length - 13)
//        }
//
//        /**
//         * Ordered array (1,2,3...) will be cycled till the order is reversed (9,8,7...)
//         *
//         * @param array to be carried
//         * @return false when the order is reversed
//         */
//        private fun nextPermutation(array: IntArray): Boolean {
//            var i = array.size - 1
//            while (i > 0 && array[i - 1] >= array[i]) {
//                i--
//            }
//            if (i <= 0) {
//                return false
//            }
//            var j = array.size - 1
//            while (array[j] <= array[i - 1]) {
//                j--
//            }
//            var temp = array[i - 1]
//            array[i - 1] = array[j]
//            array[j] = temp
//            j = array.size - 1
//            while (i < j) {
//                temp = array[i]
//                array[i] = array[j]
//                array[j] = temp
//                i++
//                j--
//            }
//            return true
//        }
//    }
// }
