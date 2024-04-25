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

import androidx.constraintlayout.coremp.test.assertEquals
import androidx.constraintlayout.coremp.widgets.ConstraintAnchor
import androidx.constraintlayout.coremp.widgets.ConstraintWidget
import androidx.constraintlayout.coremp.widgets.ConstraintWidgetContainer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RatioTest {
    @Test
    fun testWrapRatio() {
        val root = ConstraintWidgetContainer(0, 0, 700, 1920)
        val a = ConstraintWidget(231, 126)
        val b = ConstraintWidget(231, 126)
        val c = ConstraintWidget(231, 126)
        root.debugName = "root"
        root.add(a)
        root.add(b)
        root.add(c)
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setDimensionRatio("1:1")
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.TOP)
        a.setHorizontalChainStyle(ConstraintWidget.CHAIN_PACKED)
        a.horizontalBiasPercent = 0.3f
        a.setVerticalChainStyle(ConstraintWidget.CHAIN_SPREAD_INSIDE)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.LEFT, 171)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.BOTTOM, c, ConstraintAnchor.Type.TOP)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.BOTTOM)
        c.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("root: $root")
        println("A: $a")
        println("B: $b")
        println("C: $c")
        assertEquals(a.left >= 0, true)
        assertEquals(a.width, a.height)
        assertEquals(a.width, 402)
        assertEquals(root.width, 402)
        assertEquals(root.height, 654)
        assertEquals(a.left, 0)
        assertEquals(b.top, 402)
        assertEquals(b.left, 171)
        assertEquals(c.top, 528)
        assertEquals(c.left, 171)
    }

    @Test
    fun testGuidelineRatioChainWrap() {
        val root = ConstraintWidgetContainer(0, 0, 700, 1920)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        val guideline = androidx.constraintlayout.coremp.widgets.Guideline()
        guideline.setOrientation(androidx.constraintlayout.coremp.widgets.Guideline.HORIZONTAL)
        guideline.setGuideBegin(100)
        root.debugName = "root"
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(guideline)
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setDimensionRatio("1:1")
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, guideline, ConstraintAnchor.Type.TOP)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setDimensionRatio("1:1")
        b.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.BOTTOM, c, ConstraintAnchor.Type.TOP)
        c.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        c.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        c.setDimensionRatio("1:1")
        c.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.BOTTOM)
        c.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        root.height = 0
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("root: $root")
        println("A: $a")
        println("B: $b")
        println("C: $c")
        assertEquals(root.height, 1500)
        assertEquals(a.width, 100)
        assertEquals(a.height, 100)
        assertEquals(b.width, 700)
        assertEquals(b.height, 700)
        assertEquals(c.width, 700)
        assertEquals(c.height, 700)
        assertEquals(a.top, 0)
        assertEquals(b.top, a.bottom)
        assertEquals(c.top, b.bottom)
        assertEquals(a.left, 300)
        assertEquals(b.left, 0)
        assertEquals(c.left, 0)
        root.width = 0
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("root: $root")
        println("A: $a")
        println("B: $b")
        println("C: $c")
        assertEquals(root.width, 100)
        assertEquals(root.height, 300)
        assertEquals(a.width, 100)
        assertEquals(a.height, 100)
        assertEquals(b.width, 100)
        assertEquals(b.height, 100)
        assertEquals(c.width, 100)
        assertEquals(c.height, 100)
    }

    @Test
    fun testComplexRatioChainWrap() {
        val root = ConstraintWidgetContainer(0, 0, 700, 1920)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        val d = ConstraintWidget(100, 40)
        val x = ConstraintWidget(100, 20)
        val y = ConstraintWidget(100, 20)
        val z = ConstraintWidget(100, 40)
        root.debugName = "root"
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(d)
        root.add(x)
        root.add(y)
        root.add(z)
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        d.debugName = "D"
        x.debugName = "X"
        y.debugName = "Y"
        z.debugName = "Z"
        x.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        x.connect(ConstraintAnchor.Type.BOTTOM, y, ConstraintAnchor.Type.TOP)
        x.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        x.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        x.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        x.height = 40
        y.connect(ConstraintAnchor.Type.TOP, x, ConstraintAnchor.Type.BOTTOM)
        y.connect(ConstraintAnchor.Type.BOTTOM, z, ConstraintAnchor.Type.TOP)
        y.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        y.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        y.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        y.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        y.setDimensionRatio("1:1")
        z.connect(ConstraintAnchor.Type.TOP, y, ConstraintAnchor.Type.BOTTOM)
        z.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        z.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        z.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        z.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        z.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        z.setDimensionRatio("1:1")
        root.width = 700
        root.height = 0
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("root: $root")
        println("X: $x")
        println("Y: $y")
        println("Z: $z")
        assertEquals(root.width, 700)
        assertEquals(root.height, 1440)
        assertEquals(x.left, 0)
        assertEquals(x.top, 0)
        assertEquals(x.width, 700)
        assertEquals(x.height, 40)
        assertEquals(y.left, 0)
        assertEquals(y.top, 40)
        assertEquals(y.width, 700)
        assertEquals(y.height, 700)
        assertEquals(z.left, 0)
        assertEquals(z.top, 740)
        assertEquals(z.width, 700)
        assertEquals(z.height, 700)
        a.connect(ConstraintAnchor.Type.TOP, x, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.LEFT, x, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setDimensionRatio("1:1")
        b.connect(ConstraintAnchor.Type.TOP, x, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setDimensionRatio("1:1")
        c.connect(ConstraintAnchor.Type.TOP, x, ConstraintAnchor.Type.TOP)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.RIGHT, d, ConstraintAnchor.Type.LEFT)
        c.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        c.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        c.setDimensionRatio("1:1")
        d.connect(ConstraintAnchor.Type.TOP, x, ConstraintAnchor.Type.TOP)
        d.connect(ConstraintAnchor.Type.LEFT, c, ConstraintAnchor.Type.RIGHT)
        d.connect(ConstraintAnchor.Type.RIGHT, x, ConstraintAnchor.Type.RIGHT)
        d.connect(ConstraintAnchor.Type.BOTTOM, x, ConstraintAnchor.Type.BOTTOM)
        d.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        d.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        d.setDimensionRatio("1:1")
        root.height = 0
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("root: $root")
        println("X: $x")
        println("Y: $y")
        println("Z: $z")
        assertEquals(root.width, 700)
        assertEquals(root.height, 1440)
        assertEquals(x.left, 0)
        assertEquals(x.top, 0)
        assertEquals(x.width, 700)
        assertEquals(x.height, 40)
        assertEquals(y.left, 0)
        assertEquals(y.top, 40)
        assertEquals(y.width, 700)
        assertEquals(y.height, 700)
        assertEquals(z.left, 0)
        assertEquals(z.top, 740)
        assertEquals(z.width, 700)
        assertEquals(z.height, 700)
    }

    @Test
    fun testRatioChainWrap() {
        val root = ConstraintWidgetContainer(0, 0, 1000, 1000)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        val d = ConstraintWidget(100, 40)
        root.debugName = "root"
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(d)
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        d.debugName = "D"
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        c.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        c.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        d.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        d.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.FIXED)
        d.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        d.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        d.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.LEFT, d, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.TOP, d, ConstraintAnchor.Type.TOP)
        a.setDimensionRatio("1:1")
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.TOP, d, ConstraintAnchor.Type.TOP)
        b.setDimensionRatio("1:1")
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.RIGHT, d, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.TOP, d, ConstraintAnchor.Type.TOP)
        c.connect(ConstraintAnchor.Type.BOTTOM, d, ConstraintAnchor.Type.BOTTOM)
        c.setDimensionRatio("1:1")

//        root.layout();
//        System.out.println("a) root: " + root + " D: " + D + " A: " + A
//          + " B: " + B + " C: " + C);
//
//        root.setWidth(0);
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("b) root: $root D: $d A: $a B: $b C: $c")
        assertEquals(root.width, 120)
        assertEquals(d.width, 120)
        assertEquals(a.width, 40)
        assertEquals(a.height, 40)
        assertEquals(b.width, 40)
        assertEquals(b.height, 40)
        assertEquals(c.width, 40)
        assertEquals(c.height, 40)
    }

    @Test
    fun testRatioChainWrap2() {
        val root = ConstraintWidgetContainer(0, 0, 1000, 1536)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        val d = ConstraintWidget(100, 40)
        val e = ConstraintWidget(100, 40)
        val f = ConstraintWidget(100, 40)
        root.debugName = "root"
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(d)
        root.add(e)
        root.add(f)
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        d.debugName = "D"
        e.debugName = "E"
        f.debugName = "F"
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        c.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        c.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        d.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        d.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.FIXED)
        e.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        e.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        f.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        f.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        d.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        d.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        d.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        d.connect(ConstraintAnchor.Type.BOTTOM, e, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.LEFT, d, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.TOP, d, ConstraintAnchor.Type.TOP)
        a.setDimensionRatio("1:1")
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.TOP, d, ConstraintAnchor.Type.TOP)
        b.setDimensionRatio("1:1")
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.RIGHT, d, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.TOP, d, ConstraintAnchor.Type.TOP)
        c.connect(ConstraintAnchor.Type.BOTTOM, d, ConstraintAnchor.Type.BOTTOM)
        c.setDimensionRatio("1:1")
        e.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        e.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        e.connect(ConstraintAnchor.Type.TOP, d, ConstraintAnchor.Type.BOTTOM)
        e.connect(ConstraintAnchor.Type.BOTTOM, f, ConstraintAnchor.Type.TOP)
        e.setDimensionRatio("1:1")
        f.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        f.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        f.connect(ConstraintAnchor.Type.TOP, e, ConstraintAnchor.Type.BOTTOM)
        f.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        f.setDimensionRatio("1:1")
        root.layout()
        println(
            "a) root: " + root + " D: " + d + " A: " + a + " B: " + b +
                " C: " + c + " D: " + d + " E: " + e + " F: " + f,
        )
        root.width = 0
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println(
            "b) root: " + root + " D: " + d + " A: " + a + " B: " + b +
                " C: " + c + " D: " + d + " E: " + e + " F: " + f,
        )

        // assertEquals(root.getWidth(), 748);
        assertEquals(d.width, root.width)
        assertEquals(a.width, d.height)
        assertEquals(a.height, d.height)
        assertEquals(b.width, d.height)
        assertEquals(b.height, d.height)
        assertEquals(c.width, d.height)
        assertEquals(c.height, d.height)
    }

    @Test
    fun testRatioMax() {
        val root = ConstraintWidgetContainer(0, 0, 1000, 1000)
        val a = ConstraintWidget(100, 100)
        root.debugName = "root"
        root.add(a)
        a.debugName = "A"
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_RATIO, 0, 150, 0f)
        a.setDimensionRatio("W,16:9")
        root.optimizationLevel = androidx.constraintlayout.coremp.widgets.Optimizer.OPTIMIZATION_NONE
        root.layout()
        println("a) root: $root A: $a")
        assertEquals(a.width, 267)
        assertEquals(a.height, 150)
        assertEquals(a.top, 425)
    }

    @Test
    fun testRatioMax2() {
        val root = ConstraintWidgetContainer(0, 0, 1000, 1000)
        val a = ConstraintWidget(100, 100)
        root.debugName = "root"
        root.add(a)
        a.debugName = "A"
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_RATIO, 0, 150, 0f)
        a.setDimensionRatio("16:9")
        root.optimizationLevel = androidx.constraintlayout.coremp.widgets.Optimizer.OPTIMIZATION_NONE
        root.layout()
        println("a) root: $root A: $a")
        assertEquals(a.width, 267, 1)
        assertEquals(a.height, 150)
        assertEquals(a.top, 425)
    }

    @Test
    fun testRatioSingleTarget() {
        val root = ConstraintWidgetContainer(0, 0, 1000, 1000)
        val a = ConstraintWidget(100, 100)
        val b = ConstraintWidget(100, 20)
        root.debugName = "root"
        root.add(a)
        root.add(b)
        a.debugName = "A"
        b.debugName = "B"
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.BOTTOM, a, ConstraintAnchor.Type.BOTTOM)
        b.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setDimensionRatio("2:3")
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.LEFT, 50)
        root.optimizationLevel = androidx.constraintlayout.coremp.widgets.Optimizer.OPTIMIZATION_NONE
        root.layout()
        println("a) root: $root A: $a B: $b")
        assertEquals(b.height, 150)
        assertEquals(b.top, a.bottom - b.height / 2)
    }

    @Test
    fun testSimpleWrapRatio() {
        val root = ConstraintWidgetContainer(0, 0, 1000, 1000)
        val a = ConstraintWidget(100, 20)
        root.debugName = "root"
        root.add(a)
        a.debugName = "A"
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setDimensionRatio("1:1")
        root.optimizationLevel = androidx.constraintlayout.coremp.widgets.Optimizer.OPTIMIZATION_NONE
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("a) root: $root A: $a")
        assertEquals(root.width, 1000)
        assertEquals(root.height, 1000)
        assertEquals(a.width, 1000)
        assertEquals(a.height, 1000)
    }

    @Test
    fun testSimpleWrapRatio2() {
        val root = ConstraintWidgetContainer(0, 0, 1000, 1000)
        val a = ConstraintWidget(100, 20)
        root.debugName = "root"
        root.add(a)
        a.debugName = "A"
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setDimensionRatio("1:1")
        root.optimizationLevel = androidx.constraintlayout.coremp.widgets.Optimizer.OPTIMIZATION_NONE
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("a) root: $root A: $a")
        assertEquals(root.width, 1000)
        assertEquals(root.height, 1000)
        assertEquals(a.width, 1000)
        assertEquals(a.height, 1000)
    }

    @Test
    fun testNestedRatio() {
        val root = ConstraintWidgetContainer(0, 0, 1000, 1000)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        root.add(a)
        root.add(b)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.RIGHT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setDimensionRatio("1:1")
        b.setDimensionRatio("1:1")
        root.optimizationLevel = androidx.constraintlayout.coremp.widgets.Optimizer.OPTIMIZATION_NONE
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("a) root: $root A: $a B: $b")
        assertEquals(root.width, 500)
        assertEquals(a.width, 500)
        assertEquals(b.width, 500)
        assertEquals(root.height, 1000)
        assertEquals(a.height, 500)
        assertEquals(b.height, 500)
    }

    @Test
    fun testNestedRatio2() {
        val root = ConstraintWidgetContainer(0, 0, 700, 1200)
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
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.RIGHT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.BOTTOM, a, ConstraintAnchor.Type.BOTTOM)
        c.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.RIGHT, a, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.TOP)
        c.connect(ConstraintAnchor.Type.BOTTOM, a, ConstraintAnchor.Type.BOTTOM)
        d.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.LEFT)
        d.connect(ConstraintAnchor.Type.RIGHT, a, ConstraintAnchor.Type.RIGHT)
        d.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.TOP)
        d.connect(ConstraintAnchor.Type.BOTTOM, a, ConstraintAnchor.Type.BOTTOM)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.verticalBiasPercent = 0f
        c.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        c.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        c.verticalBiasPercent = 0.5f
        d.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        d.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        d.verticalBiasPercent = 1f
        a.setDimensionRatio("1:1")
        b.setDimensionRatio("4:1")
        c.setDimensionRatio("4:1")
        d.setDimensionRatio("4:1")
        root.layout()
        println("a) root: $root A: $a B: $b C: $c D: $d")
        assertEquals(a.width, 700)
        assertEquals(a.height, 700)
        assertEquals(b.width, a.width)
        assertEquals(b.height, b.width / 4)
        assertEquals(b.top, a.top)
        assertEquals(c.width, a.width)
        assertEquals(c.height, c.width / 4)
        assertEquals(c.top, (root.height - c.height) / 2, 1)
        assertEquals(d.width, a.width)
        assertEquals(d.height, d.width / 4)
        assertEquals(d.top, a.bottom - d.height)
        root.width = 300
        root.layout()
        println("b) root: $root A: $a B: $b C: $c D: $d")
        assertEquals(a.width, root.width)
        assertEquals(a.height, root.width)
        assertEquals(b.width, a.width)
        assertEquals(b.height, b.width / 4)
        assertEquals(b.top, a.top)
        assertEquals(c.width, a.width)
        assertEquals(c.height, c.width / 4)
        assertEquals(c.top, (root.height - c.height) / 2, 1)
        assertEquals(d.width, a.width)
        assertEquals(d.height, d.width / 4)
        assertEquals(d.top, a.bottom - d.height)
        root.width = 0
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("c) root: $root A: $a B: $b C: $c D: $d")
        assertTrue(root.width > 0, "root width should be bigger than zero")
        assertEquals(a.width, root.width)
        assertEquals(a.height, root.width)
        assertEquals(b.width, a.width)
        assertEquals(b.height, b.width / 4)
        assertEquals(b.top, a.top)
        assertEquals(c.width, a.width)
        assertEquals(c.height, c.width / 4)
        assertEquals(c.top, (root.height - c.height) / 2, 1)
        assertEquals(d.width, a.width)
        assertEquals(d.height, d.width / 4)
        assertEquals(d.top, a.bottom - d.height)
        root.width = 700
        root.height = 0
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.FIXED)
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("d) root: $root A: $a B: $b C: $c D: $d")
        assertTrue(root.height > 0, "root width should be bigger than zero")
        assertEquals(a.width, root.width)
        assertEquals(a.height, root.width)
        assertEquals(b.width, a.width)
        assertEquals(b.height, b.width / 4)
        assertEquals(b.top, a.top)
        assertEquals(c.width, a.width)
        assertEquals(c.height, c.width / 4, 1)
        assertEquals(c.top, (root.height - c.height) / 2, 1)
        assertEquals(d.width, a.width)
        assertEquals(d.height, d.width / 4)
        assertEquals(d.top, a.bottom - d.height)
    }

    @Test
    fun testNestedRatio3() {
        val root = ConstraintWidgetContainer(0, 0, 1080, 1536)
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
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setDimensionRatio("1:1")
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setDimensionRatio("3.5:1")
        c.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        c.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        c.setDimensionRatio("5:2")
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.RIGHT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.BOTTOM, a, ConstraintAnchor.Type.BOTTOM)
        b.verticalBiasPercent = 0.9f
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.TOP)
        c.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.BOTTOM)
        c.verticalBiasPercent = 0.9f

//        root.layout();
//        System.out.println("A: " + A);
//        System.out.println("B: " + B);
//        System.out.println("C: " + C);
//
//        assertEquals((float)A.getWidth() / A.getHeight(), 1f, .1f);
//        assertEquals((float)B.getWidth() / B.getHeight(), 3.5f, .1f);
//        assertEquals((float)C.getWidth() / C.getHeight(), 2.5f, .1f);
//        assertEquals(B.getTop() >= A.getTop(), true);
//        assertEquals(B.getTop() <= A.getBottom(), true);
//        assertEquals(C.getTop() >= B.getTop(), true);
//        assertEquals(C.getBottom() <= B.getBottom(), true);
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("\nA: $a")
        println("B: $b")
        println("C: $c")
        assertEquals(a.width.toFloat() / a.height, 1f, .1f)
        assertEquals(b.width.toFloat() / b.height, 3.5f, .1f)
        assertEquals(c.width.toFloat() / c.height, 2.5f, .1f)
    }

    @Test
    fun testNestedRatio4() {
        val root = ConstraintWidgetContainer(0, 0, 1000, 600)
        val a = ConstraintWidget(264, 144)
        val b = ConstraintWidget(264, 144)
        val verticalGuideline = androidx.constraintlayout.coremp.widgets.Guideline()
        verticalGuideline.setGuidePercent(0.34f)
        verticalGuideline.setOrientation(androidx.constraintlayout.coremp.widgets.Guideline.VERTICAL)
        val horizontalGuideline = androidx.constraintlayout.coremp.widgets.Guideline()
        horizontalGuideline.setGuidePercent(0.66f)
        horizontalGuideline.setOrientation(androidx.constraintlayout.coremp.widgets.Guideline.HORIZONTAL)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        horizontalGuideline.debugName = "hGuideline"
        verticalGuideline.debugName = "vGuideline"
        root.add(a)
        root.add(b)
        root.add(verticalGuideline)
        root.add(horizontalGuideline)
        a.width = 200
        a.height = 200
        a.connect(ConstraintAnchor.Type.BOTTOM, horizontalGuideline, ConstraintAnchor.Type.BOTTOM)
        a.connect(ConstraintAnchor.Type.LEFT, verticalGuideline, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, verticalGuideline, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.TOP, horizontalGuideline, ConstraintAnchor.Type.TOP)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setDimensionRatio("H,1:1")
        b.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_PERCENT, 0, 0, 0.3f)
        b.connect(ConstraintAnchor.Type.BOTTOM, horizontalGuideline, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.LEFT, verticalGuideline, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.RIGHT, verticalGuideline, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.TOP, horizontalGuideline, ConstraintAnchor.Type.TOP)
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("\nroot: $root")
        println("A: $a")
        println("B: $b")
        println("hG: $horizontalGuideline")
        println("vG: $verticalGuideline")
        assertEquals(
            verticalGuideline.left,
            0.34f * root.width,
            1f,
        )
        assertEquals(
            horizontalGuideline.top,
            0.66f * root.height,
            1f,
        )
        assertTrue(a.left >= 0)
        assertTrue(b.left >= 0)
        assertEquals(a.left, verticalGuideline.left - a.width / 2)
        assertEquals(a.top, horizontalGuideline.top - a.height / 2)
        assertEquals(b.left, verticalGuideline.left - b.width / 2)
        assertEquals(b.top, horizontalGuideline.top - b.height / 2)
    }

    @Test
    fun testBasicCenter() {
        val root = ConstraintWidgetContainer(0, 0, 1000, 600)
        val a = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        root.add(a)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        root.optimizationLevel = androidx.constraintlayout.coremp.widgets.Optimizer.OPTIMIZATION_NONE
        root.layout()
        println("a) root: $root A: $a")
        assertEquals(a.left, 450)
        assertEquals(a.top, 290)
        assertEquals(a.width, 100)
        assertEquals(a.height, 20)
        root.optimizationLevel =
            androidx.constraintlayout.coremp.widgets.Optimizer.OPTIMIZATION_STANDARD
        root.layout()
        println("b) root: $root A: $a")
        assertEquals(a.left, 450)
        assertEquals(a.top, 290)
        assertEquals(a.width, 100)
        assertEquals(a.height, 20)
    }

    @Test
    fun testBasicCenter2() {
        val root = ConstraintWidgetContainer(0, 0, 1000, 600)
        val a = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        root.add(a)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_RATIO, 0, 150, 0f)
        a.setDimensionRatio("W,16:9")
        root.layout()
        println("a) root: $root A: $a")
        assertEquals(a.left, 0)
        assertEquals(a.width.toFloat() / a.height, 16f / 9f, .1f)
        assertEquals(a.height, 150)
        assertEquals(a.top.toFloat(), (root.height - a.height) / 2f, 0f)
    }

    @Test
    fun testBasicRatio() {
        val root = ConstraintWidgetContainer(0, 0, 600, 1000)
        val a = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        root.add(a)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        a.verticalBiasPercent = 0f
        a.horizontalBiasPercent = 0f
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setDimensionRatio("1:1")
        root.optimizationLevel = androidx.constraintlayout.coremp.widgets.Optimizer.OPTIMIZATION_NONE
        root.layout()
        println("a) root: $root A: $a")
        assertEquals(a.left, 0)
        assertEquals(a.top, 0)
        assertEquals(a.width, 600)
        assertEquals(a.height, 600)
        a.verticalBiasPercent = 1f
        root.layout()
        println("b) root: $root A: $a")
        assertEquals(a.left, 0)
        assertEquals(a.top, 400)
        assertEquals(a.width, 600)
        assertEquals(a.height, 600)
        a.verticalBiasPercent = 0f
        root.optimizationLevel =
            androidx.constraintlayout.coremp.widgets.Optimizer.OPTIMIZATION_STANDARD
        root.layout()
        println("c) root: $root A: $a")
        assertEquals(a.left, 0)
        assertEquals(a.top, 0)
        assertEquals(a.width, 600)
        assertEquals(a.height, 600)
    }

    @Test
    fun testBasicRatio2() {
        val root = ConstraintWidgetContainer(0, 0, 1000, 600)
        val a = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        root.add(a)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setDimensionRatio("1:1")
        root.optimizationLevel = androidx.constraintlayout.coremp.widgets.Optimizer.OPTIMIZATION_NONE
        root.layout()
        println("a) root: $root A: $a")
        assertEquals(a.left, 450)
        assertEquals(a.top, 250)
        assertEquals(a.width, 100)
        assertEquals(a.height, 100)
        root.optimizationLevel =
            androidx.constraintlayout.coremp.widgets.Optimizer.OPTIMIZATION_STANDARD
        root.layout()
        println("b) root: $root A: $a")
        assertEquals(a.left, 450)
        assertEquals(a.top, 250)
        assertEquals(a.width, 100)
        assertEquals(a.height, 100)
    }

    @Test
    fun testSimpleRatio() {
        val root = ConstraintWidgetContainer(0, 0, 200, 600)
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
        a.setDimensionRatio("3:2")
        root.layout()
        println("a) root: $root A: $a")
        assertEquals(a.width.toFloat() / a.height, 3f / 2f, .1f)
        assertTrue(a.top >= 0, "A.top > 0")
        assertTrue(a.left >= 0, "A.left > 0")
        assertEquals(a.top, root.height - a.bottom, "A vertically centered")
        assertEquals(a.left, root.right - a.right, "A horizontally centered")
        a.setDimensionRatio("1:2")
        root.layout()
        println("b) root: $root A: $a")
        assertEquals(a.width.toFloat() / a.height, 1f / 2f, .1f)
        assertTrue(a.top >= 0, "A.top > 0")
        assertTrue(a.left >= 0, "A.left > 0")
        assertEquals(a.top, root.height - a.bottom, "A vertically centered")
        assertEquals(a.left, root.right - a.right, "A horizontally centered")
    }

    @Test
    fun testRatioGuideline() {
        val root = ConstraintWidgetContainer(0, 0, 400, 600)
        val a = ConstraintWidget(100, 20)
        val guideline = androidx.constraintlayout.coremp.widgets.Guideline()
        guideline.setOrientation(ConstraintWidget.VERTICAL)
        guideline.setGuideBegin(200)
        root.debugName = "root"
        a.debugName = "A"
        root.add(a)
        root.add(guideline)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, guideline, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setDimensionRatio("3:2")
        root.layout()
        println("a) root: $root guideline: $guideline A: $a")
        assertEquals(a.width / a.height, 3 / 2)
        assertTrue(a.top >= 0, "A.top > 0")
        assertTrue(a.left >= 0, "A.left > 0")
        assertEquals(a.top, root.height - a.bottom, "A vertically centered")
        assertEquals(a.left, guideline.left - a.right, "A horizontally centered")
        a.setDimensionRatio("1:2")
        root.layout()
        println("b) root: $root guideline: $guideline A: $a")
        assertEquals(a.width / a.height, 1 / 2)
        assertTrue(a.top >= 0, "A.top > 0")
        assertTrue(a.left >= 0, "A.left > 0")
        assertEquals(a.top, root.height - a.bottom, "A vertically centered")
        assertEquals(a.left, guideline.left - a.right, "A horizontally centered")
    }

    @Test
    fun testRatioWithMinimum() {
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
        a.setDimensionRatio("16:9")
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.optimizationLevel = androidx.constraintlayout.coremp.widgets.Optimizer.OPTIMIZATION_NONE
        root.width = 0
        root.height = 0
        root.layout()
        println("a) root: $root A: $a")
        assertEquals(root.width, 0)
        assertEquals(root.height, 0)
        a.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_SPREAD, 100, 0, 0f)
        root.width = 0
        root.height = 0
        root.layout()
        println("b) root: $root A: $a")
        assertEquals(root.width, 100)
        assertEquals(root.height, 56)
        a.setVerticalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_SPREAD, 100, 0, 0f)
        root.width = 0
        root.height = 0
        root.layout()
        println("c) root: $root A: $a")
        assertEquals(root.width, 178)
        assertEquals(root.height, 100)
    }

    @Test
    fun testRatioWithPercent() {
        val root = ConstraintWidgetContainer(0, 0, 600, 1000)
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
        a.setDimensionRatio("1:1")
        a.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_PERCENT, 0, 0, 0.7f)
        root.optimizationLevel = androidx.constraintlayout.coremp.widgets.Optimizer.OPTIMIZATION_NONE
        root.layout()
        println("a) root: $root A: $a")
        val w: Int = (0.7 * root.width).toInt()
        assertEquals(a.width, w)
        assertEquals(a.height, w)
        assertEquals(a.left, (root.width - w) / 2)
        assertEquals(a.top, (root.height - w) / 2)
        root.optimizationLevel =
            androidx.constraintlayout.coremp.widgets.Optimizer.OPTIMIZATION_STANDARD
        root.layout()
        println("b) root: $root A: $a")
        assertEquals(a.width, w)
        assertEquals(a.height, w)
        assertEquals(a.left, (root.width - w) / 2)
        assertEquals(a.top, (root.height - w) / 2)
    }

    @Test
    fun testRatio() {
        val root = ConstraintWidgetContainer(0, 0, 1000, 600)
        val a = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        root.add(a)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setDimensionRatio("16:9")
        root.layout()
        println("a) root: $root A: $a")
        assertEquals(a.width, 1067)
        assertEquals(a.height, 600)
    }

    @Test
    fun testRatio2() {
        val root = ConstraintWidgetContainer(0, 0, 1080, 1920)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        root.add(a)
        root.add(b)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.verticalBiasPercent = 0.9f
        a.setDimensionRatio("3.5:1")
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.RIGHT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.BOTTOM, a, ConstraintAnchor.Type.BOTTOM)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.horizontalBiasPercent = 0.5f
        b.verticalBiasPercent = 0.9f
        b.setDimensionRatio("4:2")
        root.layout()
        println("a) root: $root A: $a B: $b")
        // A: id: A (0, 414) - (600 x 172) B: (129, 414) - (342 x 172)
        assertEquals(a.width / a.height.toFloat(), 3.5f, 0.1f)
        assertEquals(b.width / b.height.toFloat(), 2f, 0.1f)
        assertEquals(a.width, 1080, 1)
        assertEquals(a.height, 309, 1)
        assertEquals(b.width, 618, 1)
        assertEquals(b.height, 309, 1)
        assertEquals(a.left, 0)
        assertEquals(a.top, 1450)
        assertEquals(b.left, 231)
        assertEquals(b.top, a.top)
    }

    @Test
    fun testRatio3() {
        val root = ConstraintWidgetContainer(0, 0, 1080, 1920)
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
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.verticalBiasPercent = 0.5f
        a.setDimensionRatio("1:1")
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.RIGHT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.BOTTOM, a, ConstraintAnchor.Type.BOTTOM)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.horizontalBiasPercent = 0.5f
        b.verticalBiasPercent = 0.9f
        b.setDimensionRatio("3.5:1")
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.TOP)
        c.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.BOTTOM)
        c.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        c.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        c.horizontalBiasPercent = 0.5f
        c.verticalBiasPercent = 0.9f
        c.setDimensionRatio("5:2")
        root.layout()
        println("a) root: $root A: $a B: $b C: $c")
        // A: id: A (0, 414) - (600 x 172) B: (129, 414) - (342 x 172)
        assertEquals(a.width / a.height.toFloat(), 1.0f, 0.1f)
        assertEquals(b.width / b.height.toFloat(), 3.5f, 0.1f)
        assertEquals(c.width / c.height.toFloat(), 2.5f, 0.1f)
        assertEquals(a.width, 1080, 1)
        assertEquals(a.height, 1080, 1)
        assertEquals(b.width, 1080, 1)
        assertEquals(b.height, 309, 1)
        assertEquals(c.width, 772, 1)
        assertEquals(c.height, 309, 1)
        assertEquals(a.left, 0)
        assertEquals(a.top, 420)
        assertEquals(b.top, 1114)
        assertEquals(c.left, 154)
        assertEquals(c.top, b.top)
    }

    @Test
    fun testDanglingRatio() {
        val root = ConstraintWidgetContainer(0, 0, 1000, 600)
        val a = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        root.add(a)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setDimensionRatio("1:1")
        //        root.layout();
        println("a) root: $root A: $a")
        //        assertEquals(A.getWidth(), 1000);
//        assertEquals(A.getHeight(), 1000);
        a.width = 100
        a.height = 20
        a.setDimensionRatio("W,1:1")
        root.layout()
        println("b) root: $root A: $a")
        assertEquals(a.width, 1000)
        assertEquals(a.height, 1000)
    }

    @Test
    fun testDanglingRatio2() {
        val root = ConstraintWidgetContainer(0, 0, 1000, 600)
        val a = ConstraintWidget(300, 200)
        val b = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        root.add(a)
        b.debugName = "B"
        root.add(b)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 20)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 100)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.BOTTOM, a, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT, 15)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setDimensionRatio("1:1")
        root.layout()
        println("a) root: $root A: $a B: $b")
        assertEquals(b.left, 335)
        assertEquals(b.top, 100)
        assertEquals(b.width, 200)
        assertEquals(b.height, 200)
    }

    @Test
    fun testDanglingRatio3() {
        val root = ConstraintWidgetContainer(0, 0, 1000, 600)
        val a = ConstraintWidget(300, 200)
        val b = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        root.add(a)
        b.debugName = "B"
        root.add(b)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 20)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 100)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setDimensionRatio("h,1:1")
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.BOTTOM, a, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT, 15)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setDimensionRatio("w,1:1")
        root.layout()
        println("a) root: $root A: $a B: $b")
        assertEquals(a.left, 20)
        assertEquals(a.top, 100)
        assertEquals(a.width, 300)
        assertEquals(a.height, 300)
        assertEquals(b.left, 335)
        assertEquals(b.top, 100)
        assertEquals(b.width, 300)
        assertEquals(b.height, 300)
    }

    @Test
    fun testChainRatio() {
        val root = ConstraintWidgetContainer(0, 0, 1000, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(300, 20)
        val c = ConstraintWidget(300, 20)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        root.add(a)
        root.add(b)
        root.add(c)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setDimensionRatio("1:1")
        root.layout()
        println("a) root: $root A: $a B: $b C: $c")
        assertEquals(a.left, 0)
        assertEquals(a.top, 100)
        assertEquals(a.width, 400)
        assertEquals(a.height, 400)
        assertEquals(b.left, 400)
        assertEquals(b.top, 0)
        assertEquals(b.width, 300)
        assertEquals(b.height, 20)
        assertEquals(c.left, 700)
        assertEquals(c.top, 0)
        assertEquals(c.width, 300)
        assertEquals(c.height, 20)
    }

    @Test
    fun testChainRatio2() {
        val root = ConstraintWidgetContainer(0, 0, 600, 1000)
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
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setDimensionRatio("1:1")
        root.layout()
        println("a) root: $root A: $a B: $b C: $c")
        assertEquals(a.left, 0)
        assertEquals(a.top, 300)
        assertEquals(a.width, 400)
        assertEquals(a.height, 400)
        assertEquals(b.left, 400)
        assertEquals(b.top, 0)
        assertEquals(b.width, 100)
        assertEquals(b.height, 20)
        assertEquals(c.left, 500)
        assertEquals(c.top, 0)
        assertEquals(c.width, 100)
        assertEquals(c.height, 20)
    }

    @Test
    fun testChainRatio3() {
        val root = ConstraintWidgetContainer(0, 0, 600, 1000)
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
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.BOTTOM, c, ConstraintAnchor.Type.TOP)
        c.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.BOTTOM)
        c.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setDimensionRatio("1:1")
        root.layout()
        println("a) root: $root A: $a B: $b C: $c")
        assertEquals(a.left, 0)
        assertEquals(a.top, 90)
        assertEquals(a.width, 600)
        assertEquals(a.height, 600)
        assertEquals(b.left, 0)
        assertEquals(b.top, 780)
        assertEquals(b.width, 100)
        assertEquals(b.height, 20)
        assertEquals(c.left, 0)
        assertEquals(c.top, 890)
        assertEquals(c.width, 100)
        assertEquals(c.height, 20)
    }

    @Test
    fun testChainRatio4() {
        val root = ConstraintWidgetContainer(0, 0, 1000, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        root.add(a)
        root.add(b)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setDimensionRatio("4:3")
        root.layout()
        println("a) root: $root A: $a B: $b")
        assertEquals(a.left, 0)
        assertEquals(a.top, 113, 1)
        assertEquals(a.width, 500)
        assertEquals(a.height, 375)
        assertEquals(b.left, 500)
        assertEquals(b.top, 113, 1)
        assertEquals(b.width, 500)
        assertEquals(b.height, 375)
    }

    @Test
    fun testChainRatio5() {
        val root = ConstraintWidgetContainer(0, 0, 700, 1200)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        root.add(b)
        root.add(a)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setDimensionRatio("1:1")
        a.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_RATIO, 60, 0, 0f)
        root.layout()
        println("a) root: $root A: $a B: $b")
        assertEquals(a.left, 0)
        assertEquals(a.top, 300)
        assertEquals(a.width, 600)
        assertEquals(a.height, 600)
        assertEquals(b.left, 600)
        assertEquals(b.top, 590)
        assertEquals(b.width, 100)
        assertEquals(b.height, 20)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_WRAP, 0, 0, 0f)
        root.layout()
        println("b) root: $root A: $a B: $b")
        assertEquals(a.left, 0)
        assertEquals(a.top, 300)
        assertEquals(a.width, 600)
        assertEquals(a.height, 600)
        assertEquals(b.left, 600)
        assertEquals(b.top, 590)
        assertEquals(b.width, 100)
        assertEquals(b.height, 20)
        root.width = 1080
        root.height = 1536
        a.width = 180
        a.height = 180
        b.width = 900
        b.height = 106
        a.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_RATIO, 180, 0, 0f)
        root.layout()
        println("c) root: $root A: $a B: $b")
    }

    @Test
    fun testChainRatio6() {
        val root = ConstraintWidgetContainer(0, 0, 1000, 600)
        val a = ConstraintWidget(264, 144)
        val b = ConstraintWidget(264, 144)
        val c = ConstraintWidget(264, 144)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        root.add(a)
        root.add(b)
        root.add(c)
        a.setVerticalChainStyle(ConstraintWidget.CHAIN_SPREAD_INSIDE)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.BOTTOM, c, ConstraintAnchor.Type.TOP)
        c.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.BOTTOM)
        c.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        b.horizontalBiasPercent = 0.501f
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setDimensionRatio("1:1")
        a.baselineDistance = 88
        c.baselineDistance = 88
        root.width = 1080
        root.height = 2220
        //        root.setHorizontalDimensionBehaviour(
//          ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT);
//        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT);
//        root.layout();
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("a) root: $root")
        println(" A: $a")
        println(" B: $b")
        println(" C: $c")
        assertEquals(a.width, b.width)
        assertEquals(b.width, b.height)
        assertEquals(root.width, c.width)
        assertEquals(root.height, a.height + b.height + c.height)
    }
}
