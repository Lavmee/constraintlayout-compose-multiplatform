/*
 * Copyright (C) 2021 The Android Open Source Project
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

import androidx.constraintlayout.coremp.widgets.ConstraintAnchor
import androidx.constraintlayout.coremp.widgets.ConstraintWidget
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.DimensionBehaviour
import androidx.constraintlayout.coremp.widgets.ConstraintWidgetContainer
import androidx.constraintlayout.coremp.widgets.Flow
import androidx.constraintlayout.coremp.widgets.Optimizer
import androidx.constraintlayout.coremp.widgets.VirtualLayout
import androidx.constraintlayout.coremp.widgets.analyzer.BasicMeasure
import kotlin.math.max
import kotlin.test.Test
import kotlin.test.assertEquals

class FlowTest {
    @Test
    fun testFlowBaseline() {
        val root = ConstraintWidgetContainer(0, 0, 1080, 1536)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(20, 15)
        val flow = Flow()
        root.measurer = sMeasurer
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        flow.debugName = "Flow"
        flow.setVerticalAlign(Flow.VERTICAL_ALIGN_BASELINE)
        flow.add(a)
        flow.add(b)
        a.baselineDistance = 15
        flow.height = 30
        flow.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        flow.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
        flow.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        flow.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        flow.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        flow.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        root.add(flow)
        root.add(a)
        root.add(b)
        root.measure(
            Optimizer.OPTIMIZATION_NONE,
            0, 0, 0, 0, 0, 0, 0, 0,
        )
        root.layout()
        println("a) root: $root")
        println("flow: $flow")
        println("A: $a")
        println("B: $b")
        assertEquals(flow.width, 1080)
        assertEquals(flow.height, 30)
        assertEquals(flow.top, 753)
        assertEquals(a.left, 320)
        assertEquals(a.top, 758)
        assertEquals(b.left, 740)
        assertEquals(b.top, 761)
    }

    @Test
    fun testComplexChain() {
        val root = ConstraintWidgetContainer(0, 0, 1080, 1536)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        val flow = Flow()
        root.measurer = sMeasurer
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        flow.debugName = "Flow"
        flow.setWrapMode(Flow.WRAP_CHAIN)
        flow.setMaxElementsWrap(2)
        flow.add(a)
        flow.add(b)
        flow.add(c)
        root.add(flow)
        root.add(a)
        root.add(b)
        root.add(c)
        a.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        b.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        c.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        flow.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        flow.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        flow.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        flow.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        flow.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_PARENT)
        flow.setVerticalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        root.measure(
            Optimizer.OPTIMIZATION_NONE,
            0, 0, 0, 0, 0, 0, 0, 0,
        )
        root.layout()
        println("a) root: $root")
        println("flow: $flow")
        println("A: $a")
        println("B: $b")
        println("C: $c")
        assertEquals(a.width, 540)
        assertEquals(b.width, 540)
        assertEquals(c.width, 1080)
        assertEquals(flow.width, root.width)
        assertEquals(
            flow.height,
            (max(a.height.toDouble(), b.height.toDouble()) + c.height).toInt(),
        )
        assertEquals(flow.top, 748)
    }

    @Test
    fun testFlowText() {
        val root = ConstraintWidgetContainer(20, 5)
        val a = ConstraintWidget(7, 1)
        val b = ConstraintWidget(6, 1)
        a.debugName = "A"
        b.debugName = "B"
        val flow = Flow()
        flow.debugName = "flow"
        flow.setHorizontalDimensionBehaviour(DimensionBehaviour.FIXED)
        flow.setVerticalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        flow.width = 20
        flow.height = 2
        flow.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        flow.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        flow.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        flow.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        flow.add(a)
        flow.add(b)
        root.add(flow)
        root.add(a)
        root.add(b)
        root.measurer = object : BasicMeasure.Measurer {
            override fun measure(widget: ConstraintWidget, measure: BasicMeasure.Measure) {
                measure.measuredWidth = widget.width
                measure.measuredHeight = widget.height
            }

            override fun didMeasures() {}
        }
        root.measurer = sMeasurer
        root.measure(
            Optimizer.OPTIMIZATION_NONE,
            0, 0, 0, 0, 0, 0, 0, 0,
        )
        // root.layout();
        println("root: $root")
        println("flow: $flow")
        println("A: $a")
        println("B: $b")
    }

    companion object {
        var sMeasurer: BasicMeasure.Measurer = object : BasicMeasure.Measurer {
            override fun measure(widget: ConstraintWidget, measure: BasicMeasure.Measure) {
                val horizontalBehavior: DimensionBehaviour = measure.horizontalBehavior
                val verticalBehavior: DimensionBehaviour = measure.verticalBehavior
                val horizontalDimension = measure.horizontalDimension
                val verticalDimension = measure.verticalDimension
                if (widget is VirtualLayout) {
                    val layout: VirtualLayout = widget
                    var widthMode: Int = BasicMeasure.UNSPECIFIED
                    var heightMode: Int = BasicMeasure.UNSPECIFIED
                    var widthSize = 0
                    var heightSize = 0
                    if (layout.horizontalDimensionBehaviour
                        === DimensionBehaviour.MATCH_PARENT
                    ) {
                        widthSize = if (layout.parent != null) layout.parent!!.width else 0
                        widthMode = BasicMeasure.EXACTLY
                    } else if (horizontalBehavior
                        === DimensionBehaviour.FIXED
                    ) {
                        widthSize = horizontalDimension
                        widthMode = BasicMeasure.EXACTLY
                    }
                    if (layout.verticalDimensionBehaviour
                        === DimensionBehaviour.MATCH_PARENT
                    ) {
                        heightSize =
                            if (layout.parent != null) layout.parent!!.height else 0
                        heightMode = BasicMeasure.EXACTLY
                    } else if (verticalBehavior === DimensionBehaviour.FIXED) {
                        heightSize = verticalDimension
                        heightMode = BasicMeasure.EXACTLY
                    }
                    layout.measure(widthMode, widthSize, heightMode, heightSize)
                    measure.measuredWidth = layout.measuredWidth
                    measure.measuredHeight = layout.measuredHeight
                } else {
                    if (horizontalBehavior === DimensionBehaviour.FIXED) {
                        measure.measuredWidth = horizontalDimension
                    }
                    if (verticalBehavior === DimensionBehaviour.FIXED) {
                        measure.measuredHeight = verticalDimension
                    }
                }
            }

            override fun didMeasures() {}
        }
    }
}
