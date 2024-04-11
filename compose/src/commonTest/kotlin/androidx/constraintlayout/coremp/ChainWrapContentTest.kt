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

import androidx.constraintlayout.coremp.widgets.ConstraintAnchor
import androidx.constraintlayout.coremp.widgets.ConstraintWidget
import androidx.constraintlayout.coremp.widgets.ConstraintWidgetContainer
import androidx.constraintlayout.coremp.widgets.Optimizer
import kotlin.test.Test
import kotlin.test.assertEquals

class ChainWrapContentTest {
    @Test
    fun testVerticalWrapContentChain() {
        testVerticalWrapContentChain(Optimizer.OPTIMIZATION_NONE)
        testVerticalWrapContentChain(Optimizer.OPTIMIZATION_STANDARD)
    }

    fun testVerticalWrapContentChain(directResolution: Int) {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        root.optimizationLevel = directResolution
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
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        b.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 10)
        a.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.BOTTOM, c, ConstraintAnchor.Type.TOP)
        c.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.BOTTOM)
        c.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 32)
        root.layout()
        println(
            "res: " + directResolution + " root: " + root +
                " A: " + a + " B: " + b + " C: " + c,
        )
        assertEquals(a.top, 10)
        assertEquals(b.top, 30)
        assertEquals(c.top, 30)
        assertEquals(root.height, 82)
    }

    @Test
    fun testHorizontalWrapContentChain() {
        testHorizontalWrapContentChain(Optimizer.OPTIMIZATION_NONE)
        testHorizontalWrapContentChain(Optimizer.OPTIMIZATION_STANDARD)
    }

    fun testHorizontalWrapContentChain(directResolution: Int) {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        root.optimizationLevel = directResolution
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
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 10)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 32)
        root.layout()
        println(
            "1/ res: " + directResolution + " root: " + root +
                " A: " + a + " B: " + b + " C: " + c,
        )
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println(
            "2/ res: " + directResolution + " root: " + root +
                " A: " + a + " B: " + b + " C: " + c,
        )
        assertEquals(a.left, 10)
        assertEquals(b.left, 110)
        assertEquals(c.left, 110)
        assertEquals(root.width, 242)
        root.setMinWidth(400)
        root.layout()
        println(
            "3/ res: " + directResolution + " root: " + root +
                " A: " + a + " B: " + b + " C: " + c,
        )
        assertEquals(a.left, 10)
        assertEquals(b.left, 110)
        assertEquals(c.left, 268)
        assertEquals(root.width, 400)
    }

    @Test
    fun testVerticalWrapContentChain3Elts() {
        testVerticalWrapContentChain3Elts(Optimizer.OPTIMIZATION_NONE)
        testVerticalWrapContentChain3Elts(Optimizer.OPTIMIZATION_STANDARD)
    }

    fun testVerticalWrapContentChain3Elts(directResolution: Int) {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        root.optimizationLevel = directResolution
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
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        b.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        c.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 10)
        a.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.BOTTOM, c, ConstraintAnchor.Type.TOP)
        c.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.BOTTOM)
        c.connect(ConstraintAnchor.Type.BOTTOM, d, ConstraintAnchor.Type.TOP)
        d.connect(ConstraintAnchor.Type.TOP, c, ConstraintAnchor.Type.BOTTOM)
        d.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 32)
        root.layout()
        println(
            "res: " + directResolution + " root: " + root +
                " A: " + a + " B: " + b + " C: " + c + " D: " + d,
        )
        assertEquals(a.top, 10)
        assertEquals(b.top, 30)
        assertEquals(c.top, 30)
        assertEquals(d.top, 30)
        assertEquals(root.height, 82)
        root.setMinHeight(300)
        root.layout()
        println(
            "res: " + directResolution + " root: " + root +
                " A: " + a + " B: " + b + " C: " + c + " D: " + d,
        )
        assertEquals(a.top, 10)
        assertEquals(b.top, 30)
        assertEquals(c.top, 139)
        assertEquals(d.top, 248)
        assertEquals(root.height, 300)
        root.height = 600
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.FIXED)
        root.layout()
        println(
            "res: " + directResolution + " root: " + root +
                " A: " + a + " B: " + b + " C: " + c + " D: " + d,
        )
        assertEquals(a.top, 10)
        assertEquals(b.top, 30)
        assertEquals(c.top, 289)
        assertEquals(d.top, 548)
        assertEquals(root.height, 600)
    }

    @Test
    fun testHorizontalWrapContentChain3Elts() {
        testHorizontalWrapContentChain3Elts(Optimizer.OPTIMIZATION_NONE)
        testHorizontalWrapContentChain3Elts(Optimizer.OPTIMIZATION_STANDARD)
    }

    fun testHorizontalWrapContentChain3Elts(directResolution: Int) {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        root.optimizationLevel = directResolution
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
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        c.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 10)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.RIGHT, d, ConstraintAnchor.Type.LEFT)
        d.connect(ConstraintAnchor.Type.LEFT, c, ConstraintAnchor.Type.RIGHT)
        d.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 32)
        root.layout()
        println(
            "res: " + directResolution + " root: " + root +
                " A: " + a + " B: " + b + " C: " + c + " D: " + d,
        )
        assertEquals(a.left, 10)
        assertEquals(b.left, 110)
        assertEquals(c.left, 110)
        assertEquals(d.left, 110)
        assertEquals(root.width, 242)
        root.setMinWidth(300)
        root.layout()
        println(
            "res: " + directResolution + " root: " + root +
                " A: " + a + " B: " + b + " C: " + c + " D: " + d,
        )
        assertEquals(a.left, 10)
        assertEquals(b.left, 110)
        assertEquals(c.left, 139)
        assertEquals(d.left, 168)
        assertEquals(root.width, 300)
        root.width = 600
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.FIXED)
        root.layout()
        println(
            "res: " + directResolution + " root: " + root +
                " A: " + a + " B: " + b + " C: " + c + " D: " + d,
        )
        assertEquals(a.left, 10)
        assertEquals(b.left, 110)
        assertEquals(c.left, 289)
        assertEquals(d.left, 468)
        assertEquals(root.width, 600)
    }

    @Test
    fun testHorizontalWrapChain() {
        val root = ConstraintWidgetContainer(0, 0, 600, 1000)
        val a = ConstraintWidget(20, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(20, 20)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        root.add(a)
        root.add(b)
        root.add(c)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_WRAP, 0, 0, 0f)
        b.width = 600
        root.layout()
        println("a) A: $a B: $b C: $c")
        assertEquals(a.left, 0)
        assertEquals(b.left, 20)
        assertEquals(c.left, 580)
        a.setHorizontalChainStyle(ConstraintWidget.CHAIN_PACKED)
        b.width = 600
        root.layout()
        println("b) A: $a B: $b C: $c")
        assertEquals(a.left, 0)
        assertEquals(b.left, 20)
        assertEquals(c.left, 580) // doesn't expand beyond
        b.width = 100
        root.layout()
        println("c) A: $a B: $b C: $c")
        assertEquals(a.left, 230)
        assertEquals(b.left, 250)
        assertEquals(c.left, 350)
        b.width = 600
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        c.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        root.layout()
        println("d) root: $root A: $a B: $b C: $c")
        assertEquals(root.height, 20)
        assertEquals(a.left, 0)
        assertEquals(b.left, 20)
        assertEquals(c.left, 580)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.FIXED)
        b.width = 600
        root.width = 0
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("e) root: $root A: $a B: $b C: $c")
        assertEquals(root.height, 20)
        assertEquals(a.left, 0)
        assertEquals(b.left, 20)
        assertEquals(c.left, 620)
    }

    @Test
    fun testWrapChain() {
        val root = ConstraintWidgetContainer(0, 0, 1440, 1944)
        val a = ConstraintWidget(308, 168)
        val b = ConstraintWidget(308, 168)
        val c = ConstraintWidget(308, 168)
        val d = ConstraintWidget(308, 168)
        val e = ConstraintWidget(308, 168)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        d.debugName = "D"
        e.debugName = "E"
        root.add(e)
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(d)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.RIGHT, d, ConstraintAnchor.Type.LEFT)
        d.connect(ConstraintAnchor.Type.LEFT, c, ConstraintAnchor.Type.RIGHT)
        d.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        e.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        e.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        e.connect(ConstraintAnchor.Type.TOP, c, ConstraintAnchor.Type.BOTTOM)
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.FIXED)
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println(
            "a) root: " + root + " A: " + a +
                " B: " + b + " C: " + c + " D: " + d + " E: " + e,
        )
        assertEquals(root.width, 1440)
        assertEquals(root.height, 336)
    }

    @Test
    fun testWrapDanglingChain() {
        val root = ConstraintWidgetContainer(0, 0, 1440, 1944)
        val a = ConstraintWidget(308, 168)
        val b = ConstraintWidget(308, 168)
        val c = ConstraintWidget(308, 168)
        val d = ConstraintWidget(308, 168)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        root.add(a)
        root.add(b)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("a) root: $root A: $a B: $b")
        assertEquals(root.width, 616)
        assertEquals(root.height, 168)
        assertEquals(a.left, 0)
        assertEquals(b.left, 308)
        assertEquals(a.width, 308)
        assertEquals(b.width, 308)
    }
}
