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
import androidx.constraintlayout.coremp.widgets.ConstraintWidgetContainer
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Basic wrap test
 */
class WrapTest {
    @Test
    fun testBasic() {
        val root = ConstraintWidgetContainer(0, 0, 1000, 600)
        val a = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        root.add(a)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("a) root: $root A: $a")
        a.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_SPREAD, 0, 100, 0f)
        a.setVerticalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_SPREAD, 0, 60, 0f)
        root.layout()
        println("b) root: $root A: $a")
    }

    @Test
    fun testBasic2() {
        val root = ConstraintWidgetContainer(0, 0, 1000, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        root.add(a)
        root.add(b)
        root.add(c)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.BOTTOM, c, ConstraintAnchor.Type.TOP)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_SPREAD, 0, 100, 1f)
        b.setVerticalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_SPREAD, 0, 60, 1f)
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.optimizationLevel = androidx.constraintlayout.coremp.widgets.Optimizer.OPTIMIZATION_NONE
        root.layout()
        println("root: $root A: $a B: $b C: $c")
        assertEquals(root.width, 200)
        assertEquals(root.height, 40)
        b.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_SPREAD, 20, 100, 1f)
        b.setVerticalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_SPREAD, 30, 60, 1f)
        root.width = 0
        root.height = 0
        root.layout()
        println("root: $root A: $a B: $b C: $c")
        assertEquals(root.width, 220)
        assertEquals(root.height, 70)
    }

    @Test
    fun testRatioWrap() {
        val root = ConstraintWidgetContainer(0, 0, 100, 600)
        val a = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        root.add(a)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setDimensionRatio("1:1")
        root.height = 0
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.optimizationLevel = androidx.constraintlayout.coremp.widgets.Optimizer.OPTIMIZATION_NONE
        root.layout()
        println("root: $root A: $a")
        assertEquals(root.width, 100)
        assertEquals(root.height, 100)
        root.height = 600
        root.width = 0
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.FIXED)
        root.layout()
        println("root: $root A: $a")
        assertEquals(root.width, 600)
        assertEquals(root.height, 600)
        root.width = 100
        root.height = 600
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("root: $root A: $a")
        assertEquals(root.width, 0)
        assertEquals(root.height, 0)
    }

    @Test
    fun testRatioWrap2() {
        val root = ConstraintWidgetContainer(0, 0, 1000, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        root.add(a)
        root.add(b)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setDimensionRatio("1:1")
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.optimizationLevel = androidx.constraintlayout.coremp.widgets.Optimizer.OPTIMIZATION_NONE
        root.layout()
        println("root: $root A: $a B: $b")
        assertEquals(root.width, 100)
        assertEquals(root.height, 120)
    }

    @Test
    fun testRatioWrap3() {
        val root = ConstraintWidgetContainer(0, 0, 500, 600)
        val a = ConstraintWidget(100, 60)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        root.add(a)
        root.add(b)
        root.add(c)
        a.baselineDistance = 100
        b.baselineDistance = 10
        c.baselineDistance = 10
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        a.verticalBiasPercent = 0f
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.BASELINE, a, ConstraintAnchor.Type.BASELINE)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.BASELINE, b, ConstraintAnchor.Type.BASELINE)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setDimensionRatio("1:1")
        root.optimizationLevel = androidx.constraintlayout.coremp.widgets.Optimizer.OPTIMIZATION_NONE
        root.layout()
        println("root: $root A: $a B: $b C: $c")
        assertEquals(a.width, 300)
        assertEquals(a.height, 300)
        assertEquals(b.left, 300)
        assertEquals(b.top, 90)
        assertEquals(b.width, 100)
        assertEquals(b.height, 20)
        assertEquals(c.left, 400)
        assertEquals(c.top, 90)
        assertEquals(c.width, 100)
        assertEquals(c.height, 20)
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        a.baselineDistance = 10
        root.layout()
        println("root: $root A: $a B: $b C: $c")
        assertEquals(root.width, 220)
        assertEquals(root.height, 20)
    }

    @Test
    fun testGoneChainWrap() {
        val root = ConstraintWidgetContainer(0, 0, 500, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        val d = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        d.debugName = "D"
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(d)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        d.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.BOTTOM, c, ConstraintAnchor.Type.TOP)
        c.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.BOTTOM)
        c.connect(ConstraintAnchor.Type.BOTTOM, d, ConstraintAnchor.Type.TOP)
        d.connect(ConstraintAnchor.Type.TOP, c, ConstraintAnchor.Type.BOTTOM)
        d.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        b.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        d.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.optimizationLevel = androidx.constraintlayout.coremp.widgets.Optimizer.OPTIMIZATION_NONE
        root.layout()
        println("root: $root A: $a B: $b C: $c D: $d")
        assertEquals(root.height, 40)
        a.setVerticalChainStyle(ConstraintWidget.CHAIN_PACKED)
        root.layout()
        println("root: $root A: $a B: $b C: $c D: $d")
        assertEquals(root.height, 40)
    }

    @Test
    fun testWrap() {
        val root = ConstraintWidgetContainer(0, 0, 500, 600)
        val a = ConstraintWidget(100, 0)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        val d = ConstraintWidget(100, 40)
        val e = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        d.debugName = "D"
        e.debugName = "E"
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(d)
        root.add(e)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        d.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, c, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        c.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.BOTTOM)
        d.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        e.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        e.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        e.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.optimizationLevel = androidx.constraintlayout.coremp.widgets.Optimizer.OPTIMIZATION_NONE
        root.layout()
        println(
            "root: " + root + " A: " + a +
                " B: " + b + " C: " + c + " D: " + d + " E: " + e,
        )
        assertEquals(root.height, 80)
        assertEquals(e.top, 30)
    }

    @Test
    fun testWrap2() {
        val root = ConstraintWidgetContainer(0, 0, 500, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        val d = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        d.debugName = "D"
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(d)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        d.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        d.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        a.connect(ConstraintAnchor.Type.TOP, c, ConstraintAnchor.Type.BOTTOM, 30)
        a.connect(ConstraintAnchor.Type.BOTTOM, d, ConstraintAnchor.Type.TOP, 40)
        b.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("root: $root A: $a B: $b C: $c D: $d")
        assertEquals(c.top, 0)
        assertEquals(a.top, c.bottom + 30)
        assertEquals(d.top, a.bottom + 40)
        assertEquals(root.height, 20 + 30 + 20 + 40 + 20)
    }

    @Test
    fun testWrap3() {
        val root = ConstraintWidgetContainer(0, 0, 500, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        root.add(a)
        root.add(b)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 200)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.LEFT, 250)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("root: $root A: $a B: $b")
        assertEquals(root.width, a.width + 200)
        assertEquals(a.left, 0)
        assertEquals(b.left, 250)
        assertEquals(b.width, 100)
        assertEquals(b.right > root.width, true)
    }

    @Test
    fun testWrap4() {
        val root = ConstraintWidgetContainer(0, 0, 500, 600)
        val a = ConstraintWidget(80, 80)
        val b = ConstraintWidget(60, 60)
        val c = ConstraintWidget(50, 100)
        val barrier1 = androidx.constraintlayout.coremp.widgets.Barrier()
        barrier1.setBarrierType(androidx.constraintlayout.coremp.widgets.Barrier.BOTTOM)
        val barrier2 = androidx.constraintlayout.coremp.widgets.Barrier()
        barrier2.setBarrierType(androidx.constraintlayout.coremp.widgets.Barrier.BOTTOM)
        barrier1.add(a)
        barrier1.add(b)
        barrier2.add(c)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        barrier1.debugName = "B1"
        barrier2.debugName = "B2"
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(barrier1)
        root.add(barrier2)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, barrier1, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.BOTTOM, barrier1, ConstraintAnchor.Type.BOTTOM)
        c.connect(ConstraintAnchor.Type.TOP, barrier1, ConstraintAnchor.Type.BOTTOM)
        c.connect(ConstraintAnchor.Type.BOTTOM, barrier2, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("root: $root")
        println("A: $a")
        println("B: $b")
        println("C: $c")
        println("B1: $barrier1")
        println("B2: $barrier2")
        assertEquals(a.top >= 0, true)
        assertEquals(b.top >= 0, true)
        assertEquals(c.top >= 0, true)
        assertEquals(
            root.height.toDouble(),
            kotlin.math.max(a.height.toDouble(), b.height.toDouble()) + c.height,
        )
    }

    @Test
    fun testWrap5() {
        val root = ConstraintWidgetContainer(0, 0, 500, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        val d = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        d.debugName = "D"
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(d)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 8)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 8)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 8)
        b.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 8)
        c.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        c.connect(ConstraintAnchor.Type.BOTTOM, c, ConstraintAnchor.Type.TOP)
        d.horizontalBiasPercent = 0.557f
        d.verticalBiasPercent = 0.8f
        d.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        d.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        d.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        d.connect(ConstraintAnchor.Type.BOTTOM, c, ConstraintAnchor.Type.TOP)
        d.horizontalBiasPercent = 0.557f
        d.verticalBiasPercent = 0.28f
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("root: $root")
        println("A: $a")
        println("B: $b")
        println("C: $c")
        println("D: $d")
    }

    @Test
    fun testWrap6() {
        val root = ConstraintWidgetContainer(0, 0, 500, 600)
        val a = ConstraintWidget(100, 20)
        val guideline = androidx.constraintlayout.coremp.widgets.Guideline()
        guideline.setOrientation(ConstraintWidget.VERTICAL)
        guideline.setGuidePercent(0.5f)
        root.debugName = "root"
        a.debugName = "A"
        guideline.debugName = "guideline"
        root.add(a)
        root.add(guideline)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 8)
        a.connect(ConstraintAnchor.Type.LEFT, guideline, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_WRAP, 0, 0, 0f)
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("root: $root")
        println("A: $a")
        println("guideline: $guideline")
        assertEquals(root.width, a.width * 2)
        assertEquals(root.height, a.height + 8)
        assertEquals(guideline.left.toFloat(), root.width / 2f, 0f)
        assertEquals(a.width, 100)
        assertEquals(a.height, 20)
    }

    @Test
    fun testWrap7() {
        val root = ConstraintWidgetContainer(0, 0, 500, 600)
        val a = ConstraintWidget(100, 20)
        val divider = ConstraintWidget(1, 20)
        val guideline = androidx.constraintlayout.coremp.widgets.Guideline()
        guideline.setOrientation(ConstraintWidget.VERTICAL)
        guideline.setGuidePercent(0.5f)
        root.debugName = "root"
        a.debugName = "A"
        divider.debugName = "divider"
        guideline.debugName = "guideline"
        root.add(a)
        root.add(divider)
        root.add(guideline)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        a.connect(ConstraintAnchor.Type.LEFT, guideline, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_WRAP, 0, 0, 0f)
        divider.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        divider.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        divider.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        divider.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        divider.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("root: $root")
        println("A: $a")
        println("divider: $divider")
        println("guideline: $guideline")
        assertEquals(root.width, a.width * 2)
        assertEquals(root.height, a.height)
        assertEquals(guideline.left.toFloat(), root.width / 2f, 0f)
        assertEquals(a.width, 100)
        assertEquals(a.height, 20)
    }

    @Test
    fun testWrap8() {
        // check_048
        val root = ConstraintWidgetContainer(0, 0, 1080, 1080)
        val button56 = ConstraintWidget(231, 126)
        val button60 = ConstraintWidget(231, 126)
        val button63 = ConstraintWidget(368, 368)
        val button65 = ConstraintWidget(231, 126)
        button56.debugName = "button56"
        button60.debugName = "button60"
        button63.debugName = "button63"
        button65.debugName = "button65"
        root.add(button56)
        root.add(button60)
        root.add(button63)
        root.add(button65)
        button56.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 42)
        button56.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 42)
        // button56.baselineDistance = 77;
        button60.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 42)
        button60.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 79)
        // button60.baselineDistance = 77;
        button63.connect(ConstraintAnchor.Type.LEFT, button56, ConstraintAnchor.Type.RIGHT, 21)
        button63.connect(ConstraintAnchor.Type.RIGHT, button60, ConstraintAnchor.Type.LEFT, 21)
        button63.connect(ConstraintAnchor.Type.TOP, button56, ConstraintAnchor.Type.BOTTOM, 21)
        button63.connect(ConstraintAnchor.Type.BOTTOM, button60, ConstraintAnchor.Type.TOP, 21)
        // button63.baselineDistance = 155;
        button63.verticalBiasPercent = 0.8f
        button65.connect(ConstraintAnchor.Type.LEFT, button56, ConstraintAnchor.Type.RIGHT, 21)
        button65.connect(ConstraintAnchor.Type.RIGHT, button60, ConstraintAnchor.Type.LEFT, 21)
        button65.connect(ConstraintAnchor.Type.TOP, button56, ConstraintAnchor.Type.BOTTOM, 21)
        button65.connect(ConstraintAnchor.Type.BOTTOM, button60, ConstraintAnchor.Type.TOP, 21)
        // button65.baselineDistance = 77;
        button65.verticalBiasPercent = 0.28f
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("root: $root")
        println("button56: $button56")
        println("button60: $button60")
        println("button63: $button63")
        println("button65: $button65")
        assertEquals(root.width, 1080)
        assertEquals(root.height, 783)
        assertEquals(button56.left, 42)
        assertEquals(button56.top, 42)
        assertEquals(button60.left, 807)
        assertEquals(button60.top, 578)
        assertEquals(button63.left, 356)
        assertEquals(button63.top, 189)
        assertEquals(button65.left, 425)
        assertEquals(button65.top, 257)
    }

    @Test
    fun testWrap9() {
        // b/161826272
        val root = ConstraintWidgetContainer(0, 0, 1080, 1080)
        val text = ConstraintWidget(270, 30)
        val view = ConstraintWidget(10, 10)
        root.debugName = "root"
        text.debugName = "text"
        view.debugName = "view"
        root.add(text)
        root.add(view)
        text.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        text.connect(ConstraintAnchor.Type.TOP, view, ConstraintAnchor.Type.TOP)
        view.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        view.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        view.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        view.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        view.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        view.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        view.setVerticalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_PERCENT, 0, 0, 0.2f)
        view.setDimensionRatio("1:1")
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("root: $root")
        println("text: $text")
        println("view: $view")
        assertEquals(view.width, view.height)
        assertEquals(view.height, (0.2 * root.height).toInt())
        assertEquals(root.width.toDouble(), kotlin.math.max(text.width.toDouble(), view.width.toDouble()))
    }

    @Test
    fun testBarrierWrap() {
        // b/165028374
        val root = ConstraintWidgetContainer(0, 0, 1080, 1080)
        val view = ConstraintWidget(200, 200)
        val space = ConstraintWidget(50, 50)
        val button = ConstraintWidget(100, 80)
        val text = ConstraintWidget(90, 30)
        val barrier = androidx.constraintlayout.coremp.widgets.Barrier()
        barrier.setBarrierType(androidx.constraintlayout.coremp.widgets.Barrier.BOTTOM)
        barrier.add(button)
        barrier.add(space)
        root.debugName = "root"
        view.debugName = "view"
        space.debugName = "space"
        button.debugName = "button"
        text.debugName = "text"
        barrier.debugName = "barrier"
        root.add(view)
        root.add(space)
        root.add(button)
        root.add(text)
        root.add(barrier)
        view.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        space.connect(ConstraintAnchor.Type.TOP, view, ConstraintAnchor.Type.BOTTOM)
        button.connect(ConstraintAnchor.Type.TOP, view, ConstraintAnchor.Type.BOTTOM)
        button.connect(ConstraintAnchor.Type.BOTTOM, text, ConstraintAnchor.Type.TOP)
        text.connect(ConstraintAnchor.Type.TOP, barrier, ConstraintAnchor.Type.BOTTOM)
        text.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        button.verticalBiasPercent = 1f
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("root: $root")
        println("view: $view")
        println("space: $space")
        println("button: $button")
        println("barrier: $barrier")
        println("text: $text")
        assertEquals(view.top, 0)
        assertEquals(view.bottom, 200)
        assertEquals(space.top, 200)
        assertEquals(space.bottom, 250)
        assertEquals(button.top, 200)
        assertEquals(button.bottom, 280)
        assertEquals(barrier.top, 280)
        assertEquals(text.top, barrier.top)
    }
}
