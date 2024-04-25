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
import androidx.constraintlayout.coremp.widgets.Guideline
import androidx.constraintlayout.coremp.widgets.Optimizer
import kotlin.math.max
import kotlin.test.Test
import kotlin.test.assertEquals

class ChainTest() {
    @Test
    fun testCenteringElementsWithSpreadChain() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        val d = ConstraintWidget(100, 20)
        val e = ConstraintWidget(600, 20)
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
        a.connect(ConstraintAnchor.Type.LEFT, e, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, e, ConstraintAnchor.Type.RIGHT)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        c.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.RIGHT, a, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        d.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.LEFT)
        d.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.RIGHT)
        d.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.BOTTOM)
        root.layout()
        println("A: $a B: $b C: $c D: $d E: $e")
        assertEquals(a.width, 300)
        assertEquals(b.width, a.width)
    }

    @Test
    fun testBasicChainMatch() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
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
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        c.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.setHorizontalChainStyle(ConstraintWidget.CHAIN_SPREAD)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        c.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.baselineDistance = 8
        b.baselineDistance = 8
        c.baselineDistance = 8
        root.optimizationLevel = Optimizer.OPTIMIZATION_STANDARD or Optimizer.OPTIMIZATION_CHAIN
        root.layout()
        println("A: $a B: $b C: $c")
        assertEquals(a.left, 0)
        assertEquals(a.right, 200)
        assertEquals(b.left, 200)
        assertEquals(b.right, 400)
        assertEquals(c.left, 400)
        assertEquals(c.right, 600)
    }

    @Test
    fun testSpreadChainGone() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
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
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.setHorizontalChainStyle(ConstraintWidget.CHAIN_SPREAD)
        a.visibility = ConstraintWidget.GONE
        root.layout()
        println("A: $a B: $b C: $c")
        assertEquals(a.left, 0)
        assertEquals(a.right, 0)
        assertEquals(b.left, 133)
        assertEquals(b.right, 233)
        assertEquals(c.left, 367)
        assertEquals(c.right, 467)
    }

    @Test
    fun testPackChainGone() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
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
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 100)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 20)
        a.setHorizontalChainStyle(ConstraintWidget.CHAIN_PACKED)
        b.setGoneMargin(ConstraintAnchor.Type.RIGHT, 100)
        c.visibility = ConstraintWidget.GONE
        root.layout()
        println("A: $a B: $b C: $c")
        assertEquals(a.left, 200)
        assertEquals(b.left, 300)
        assertEquals(c.left, 500)
        assertEquals(a.width, 100)
        assertEquals(b.width, 100)
        assertEquals(c.width, 0)
    }

    @Test
    fun testSpreadInsideChain2() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
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
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT, 25)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.setHorizontalChainStyle(ConstraintWidget.CHAIN_SPREAD_INSIDE)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        root.layout()
        println("A: $a B: $b C: $c")
        assertEquals(a.left, 0)
        assertEquals(a.right, 100)
        assertEquals(b.left, 100)
        assertEquals(b.right, 475)
        assertEquals(c.left, 500)
        assertEquals(c.right, 600)
    }

    @Test
    fun testPackChain2() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        root.add(a)
        root.add(b)
        root.optimizationLevel = Optimizer.OPTIMIZATION_NONE
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.setHorizontalChainStyle(ConstraintWidget.CHAIN_PACKED)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_WRAP, 0, 0, 1f)
        root.layout()
        println("e) A: $a B: $b")
        assertEquals(a.width, 100)
        assertEquals(b.width, 100)
        assertEquals(a.left, root.width - b.right)
        assertEquals(b.left, a.left + a.width)
        // e) A: id: A (200, 0) - (100 x 20) B: id: B (300, 0) - (100 x 20) - pass
        // e) A: id: A (0, 0) - (100 x 20) B: id: B (100, 0) - (100 x 20)
    }

    @Test
    fun testPackChain() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        root.add(a)
        root.add(b)
        root.optimizationLevel = Optimizer.OPTIMIZATION_NONE
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.setHorizontalChainStyle(ConstraintWidget.CHAIN_PACKED)
        root.layout()
        println("a) A: $a B: $b")
        assertEquals(a.width, 100)
        assertEquals(b.width, 100)
        assertEquals(a.left, root.width - b.right)
        assertEquals(b.left, a.left + a.width)
        a.visibility = ConstraintWidget.GONE
        root.layout()
        println("b) A: $a B: $b")
        assertEquals(a.width, 0)
        assertEquals(b.width, 100)
        assertEquals(a.left, root.width - b.right)
        assertEquals(b.left, a.left + a.width)
        b.visibility = ConstraintWidget.GONE
        root.layout()
        println("c) A: $a B: $b")
        assertEquals(a.width, 0)
        assertEquals(b.width, 0)
        assertEquals(a.left, 0)
        assertEquals(b.left, a.left + a.width)
        a.visibility = ConstraintWidget.VISIBLE
        a.width = 100
        root.layout()
        println("d) A: $a B: $b")
        assertEquals(a.width, 100)
        assertEquals(b.width, 0)
        assertEquals(a.left, root.width - b.right)
        assertEquals(b.left, a.left + a.width)
        a.visibility = ConstraintWidget.VISIBLE
        a.width = 100
        a.height = 20
        b.visibility = ConstraintWidget.VISIBLE
        b.width = 100
        b.height = 20
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_WRAP, 0, 0, 1f)
        root.layout()
        println("e) A: $a B: $b")
        assertEquals(a.width, 100)
        assertEquals(b.width, 100)
        assertEquals(a.left, root.width - b.right)
        assertEquals(b.left, a.left + a.width)
        b.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_SPREAD, 0, 0, 1f)
        root.layout()
        println("f) A: $a B: $b")
        assertEquals(a.width, 100)
        assertEquals(b.width, 500)
        assertEquals(a.left, 0)
        assertEquals(b.left, 100)
        b.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_SPREAD, 0, 50, 1f)
        root.layout()
        println("g) A: $a B: $b")
        assertEquals(a.width, 100)
        assertEquals(b.width, 50)
        assertEquals(a.left, root.width - b.right)
        assertEquals(b.left, a.left + a.width)
        b.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_PERCENT, 0, 0, 0.3f)
        root.layout()
        println("h) A: $a B: $b")
        assertEquals(a.width, 100)
        assertEquals(b.width, (0.3f * 600).toInt())
        assertEquals(a.left, root.width - b.right)
        assertEquals(b.left, a.left + a.width)
        b.setDimensionRatio("16:9")
        b.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_RATIO, 0, 0, 1f)
        root.layout()
        println("i) A: $a B: $b")
        assertEquals(a.width, 100)
        assertEquals(b.width, (16f / 9f * 20).toInt(), 1)
        assertEquals(a.left, root.width - b.right, 1)
        assertEquals(b.left, a.left + a.width)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_SPREAD, 0, 0, 1f)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_SPREAD, 0, 0, 1f)
        b.setDimensionRatio(0f, 0)
        a.visibility = ConstraintWidget.VISIBLE
        a.width = 100
        a.height = 20
        b.visibility = ConstraintWidget.VISIBLE
        b.width = 100
        b.height = 20
        root.layout()
        println("j) A: $a B: $b")
        assertEquals(a.width, b.width)
        assertEquals(a.width + b.width, root.width)
        a.setHorizontalWeight(1f)
        b.setHorizontalWeight(3f)
        root.layout()
        println("k) A: $a B: $b")
        assertEquals(a.width * 3, b.width)
        assertEquals(a.width + b.width, root.width)
    }

    /**
     * testPackChain with current Chain Optimizations.
     */
    @Test
    fun testPackChainOpt() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        root.add(a)
        root.add(b)
        root.optimizationLevel = (
            Optimizer.OPTIMIZATION_DIRECT or Optimizer.OPTIMIZATION_BARRIER
                or Optimizer.OPTIMIZATION_CHAIN
            )
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.setHorizontalChainStyle(ConstraintWidget.CHAIN_PACKED)
        root.layout()
        println("a) A: $a B: $b")
        assertEquals(a.width, 100)
        assertEquals(b.width, 100)
        assertEquals(a.left, root.width - b.right)
        assertEquals(b.left, a.left + a.width)
        a.visibility = ConstraintWidget.GONE
        root.layout()
        println("b) A: $a B: $b")
        assertEquals(a.width, 0)
        assertEquals(b.width, 100)
        assertEquals(a.left, root.width - b.right)
        assertEquals(b.left, a.left + a.width)
        b.visibility = ConstraintWidget.GONE
        root.layout()
        println("c) A: $a B: $b")
        assertEquals(a.width, 0)
        assertEquals(b.width, 0)
        assertEquals(a.left, 0)
        assertEquals(b.left, a.left + a.width)
        a.visibility = ConstraintWidget.VISIBLE
        a.width = 100
        root.layout()
        println("d) A: $a B: $b")
        assertEquals(a.width, 100)
        assertEquals(b.width, 0)
        assertEquals(a.left, root.width - b.right)
        assertEquals(b.left, a.left + a.width)
        a.visibility = ConstraintWidget.VISIBLE
        a.width = 100
        a.height = 20
        b.visibility = ConstraintWidget.VISIBLE
        b.width = 100
        b.height = 20
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_WRAP, 0, 0, 1f)
        root.layout()
        println("e) A: $a B: $b")
        assertEquals(a.width, 100)
        assertEquals(b.width, 100)
        assertEquals(a.left, root.width - b.right)
        assertEquals(b.left, a.left + a.width)
        b.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_SPREAD, 0, 0, 1f)
        root.layout()
        println("f) A: $a B: $b")
        assertEquals(a.width, 100)
        assertEquals(b.width, 500)
        assertEquals(a.left, 0)
        assertEquals(b.left, 100)
        b.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_SPREAD, 0, 50, 1f)
        root.layout()
        println("g) A: $a B: $b")
        assertEquals(a.width, 100)
        assertEquals(b.width, 50)
        assertEquals(a.left, root.width - b.right)
        assertEquals(b.left, a.left + a.width)
        b.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_PERCENT, 0, 0, 0.3f)
        root.layout()
        println("h) A: $a B: $b")
        assertEquals(a.width, 100)
        assertEquals(b.width, (0.3f * 600).toInt())
        assertEquals(a.left, root.width - b.right)
        assertEquals(b.left, a.left + a.width)
        b.setDimensionRatio("16:9")
        b.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_RATIO, 0, 0, 1f)
        root.layout()
        println("i) A: $a B: $b")
        assertEquals(a.width, 100)
        assertEquals(b.width, (16f / 9f * 20).toInt(), 1)
        assertEquals(a.left, root.width - b.right, 1)
        assertEquals(b.left, a.left + a.width)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_SPREAD, 0, 0, 1f)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_SPREAD, 0, 0, 1f)
        b.setDimensionRatio(0f, 0)
        a.visibility = ConstraintWidget.VISIBLE
        a.width = 100
        a.height = 20
        b.visibility = ConstraintWidget.VISIBLE
        b.width = 100
        b.height = 20
        root.layout()
        println("j) A: $a B: $b")
        assertEquals(a.width, b.width)
        assertEquals(a.width + b.width, root.width)
        a.setHorizontalWeight(1f)
        b.setHorizontalWeight(3f)
        root.layout()
        println("k) A: $a B: $b")
        assertEquals(a.width * 3, b.width)
        assertEquals(a.width + b.width, root.width)
    }

    @Test
    fun testSpreadChain() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        root.add(a)
        root.add(b)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.setHorizontalChainStyle(ConstraintWidget.CHAIN_SPREAD)
        root.layout()
        println("a) A: $a B: $b")
        assertEquals(a.width, 100)
        assertEquals(b.width, 100)
        assertEquals(a.left, b.left - a.right, 1)
        assertEquals(b.left - a.right, root.width - b.right, 1)
        b.visibility = ConstraintWidget.GONE
        root.layout()
        println("b) A: $a B: $b")
    }

    @Test
    fun testSpreadInsideChain() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
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
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.setHorizontalChainStyle(ConstraintWidget.CHAIN_SPREAD_INSIDE)
        root.layout()
        println("a) A: $a B: $b")
        assertEquals(a.width, 100)
        assertEquals(b.width, 100)
        assertEquals(b.right, root.width)
        b.reset()
        root.add(b)
        b.debugName = "B"
        b.width = 100
        b.height = 20
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        root.layout()
        println("b) A: $a B: $b C: $c")
        assertEquals(a.width, 100)
        assertEquals(b.width, 100)
        assertEquals(c.width, 100)
        assertEquals(b.left - a.right, c.left - b.right)
        val gap: Int = (root.width - a.width - b.width - c.width) / 2
        assertEquals(b.left, a.right + gap)
    }

    @Test
    fun testBasicChain() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        val widgets: ArrayList<ConstraintWidget> = ArrayList<ConstraintWidget>()
        widgets.add(a)
        widgets.add(b)
        widgets.add(root)
        root.add(a)
        root.add(b)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        root.layout()
        println("a) A: $a B: $b")
        assertEquals(a.width, b.width, 1)
        assertEquals(
            a.left - root.left,
            root.right - b.right,
            1,
        )
        assertEquals(a.left - root.left, b.left - a.right, 1)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        root.layout()
        println("b) A: $a B: $b")
        assertEquals(a.width, root.width - b.width)
        assertEquals(b.width, 100)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.FIXED)
        a.width = 100
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        root.layout()
        println("a) A: $a B: $b")
        assertEquals(b.width, root.width - a.width)
        assertEquals(a.width, 100)
    }

    @Test
    fun testBasicVerticalChain() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        a.debugName = "A"
        b.debugName = "B"
        val widgets: ArrayList<ConstraintWidget> = ArrayList<ConstraintWidget>()
        widgets.add(a)
        widgets.add(b)
        widgets.add(root)
        root.add(a)
        root.add(b)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        root.layout()
        println("a) A: $a B: $b")
        assertEquals(a.height, b.height, 1)
        assertEquals(
            a.top - root.top,
            root.bottom - b.bottom,
            1,
        )
        assertEquals(a.top - root.top, b.top - a.bottom, 1)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        root.layout()
        println("b) A: $a B: $b")
        assertEquals(a.height, root.height - b.height)
        assertEquals(b.height, 20)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.FIXED)
        a.height = 20
        b.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        root.layout()
        println("c) A: $a B: $b")
        assertEquals(b.height, root.height - a.height)
        assertEquals(a.height, 20)
    }

    @Test
    fun testBasicChainThreeElements1() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        val marginL = 7
        val marginR = 27
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        c.setDebugSolverName(root.getSystem(), "C")
        val widgets: ArrayList<ConstraintWidget> = ArrayList<ConstraintWidget>()
        widgets.add(a)
        widgets.add(b)
        widgets.add(c)
        widgets.add(root)
        root.add(a)
        root.add(b)
        root.add(c)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 0)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT, 0)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT, 0)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT, 0)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT, 0)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 0)
        root.layout()
        println("a) A: $a B: $b C: $c")
        // all elements spread equally
        assertEquals(a.width, b.width, 1)
        assertEquals(b.width, c.width, 1)
        assertEquals(
            a.left - root.left,
            root.right - c.right,
            1,
        )
        assertEquals(a.left - root.left, b.left - a.right, 1)
        assertEquals(b.left - a.right, c.left - b.right, 1)
        // a) A: id: A (125, 0) - (100 x 20) B: id: B (350, 0) - (100 x 20)
        // C: id: C (575, 0) - (100 x 20)
        // a) A: id: A (0, 0) - (100 x 20) B: id: B (100, 0) - (100 x 20)
        // C: id: C (450, 0) - (100 x 20)
    }

    @Test
    fun testBasicChainThreeElements() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        val marginL = 7
        val marginR = 27
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        c.setDebugSolverName(root.getSystem(), "C")
        val widgets: ArrayList<ConstraintWidget> = ArrayList<ConstraintWidget>()
        widgets.add(a)
        widgets.add(b)
        widgets.add(c)
        widgets.add(root)
        root.add(a)
        root.add(b)
        root.add(c)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 0)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT, 0)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT, 0)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT, 0)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT, 0)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 0)
        root.layout()
        println("a) A: $a B: $b C: $c")
        // all elements spread equally
        assertEquals(a.width, b.width, 1)
        assertEquals(b.width, c.width, 1)
        assertEquals(a.left - root.left, root.right - c.right, 1)
        assertEquals(a.left - root.left, b.left - a.right, 1)
        assertEquals(b.left - a.right, c.left - b.right, 1)
        // A marked as 0dp, B == C, A takes the rest
        a.getAnchor(ConstraintAnchor.Type.LEFT)!!.setMargin(marginL)
        a.getAnchor(ConstraintAnchor.Type.RIGHT)!!.setMargin(marginR)
        b.getAnchor(ConstraintAnchor.Type.LEFT)!!.setMargin(marginL)
        b.getAnchor(ConstraintAnchor.Type.RIGHT)!!.setMargin(marginR)
        c.getAnchor(ConstraintAnchor.Type.LEFT)!!.setMargin(marginL)
        c.getAnchor(ConstraintAnchor.Type.RIGHT)!!.setMargin(marginR)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        root.layout()
        println("b) A: $a B: $b C: $c")
        assertEquals(
            a.left - root.left - marginL,
            root.right - c.right - marginR,
        )
        assertEquals(c.left - b.right, b.left - a.right)
        val matchWidth: Int = (
            root.width - b.width - c.width -
                marginL - marginR - (4 * (b.left - a.right))
            )
        assertEquals(a.width, 498)
        assertEquals(b.width, c.width)
        assertEquals(b.width, 100)
        checkPositions(a, b, c)
        // B marked as 0dp, A == C, B takes the rest
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.FIXED)
        a.width = 100
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        root.layout()
        println("c) A: $a B: $b C: $c")
        assertEquals(b.width, 498)
        assertEquals(a.width, c.width)
        assertEquals(a.width, 100)
        checkPositions(a, b, c)
        // C marked as 0dp, A == B, C takes the rest
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.FIXED)
        b.width = 100
        c.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        root.layout()
        println("d) A: $a B: $b C: $c")
        assertEquals(c.width, 498)
        assertEquals(a.width, b.width)
        assertEquals(a.width, 100)
        checkPositions(a, b, c)
        // A & B marked as 0dp, C == 100
        c.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.FIXED)
        c.width = 100
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        root.layout()
        println("e) A: $a B: $b C: $c")
        assertEquals(c.width, 100)
        assertEquals(a.width, b.width) // L
        assertEquals(a.width, 299)
        checkPositions(a, b, c)
        // A & C marked as 0dp, B == 100
        c.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.FIXED)
        b.width = 100
        root.layout()
        println("f) A: $a B: $b C: $c")
        assertEquals(b.width, 100)
        assertEquals(a.width, c.width)
        assertEquals(a.width, 299)
        checkPositions(a, b, c)
        // B & C marked as 0dp, A == 100
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.FIXED)
        a.width = 100
        root.layout()
        println("g) A: $a B: $b C: $c")
        assertEquals(a.width, 100)
        assertEquals(b.width, c.width)
        assertEquals(b.width, 299)
        checkPositions(a, b, c)
        // A == 0dp, B & C == 100, C is gone
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.width = 100
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.FIXED)
        b.width = 100
        c.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.FIXED)
        c.width = 100
        c.visibility = ConstraintWidget.GONE
        root.layout()
        println("h) A: $a B: $b C: $c")
        assertEquals(a.width, 632)
        assertEquals(b.width, 100)
        assertEquals(c.width, 0)
        checkPositions(a, b, c)
    }

    private fun checkPositions(a: ConstraintWidget, b: ConstraintWidget, c: ConstraintWidget) {
        assertEquals(a.left <= a.right, true)
        assertEquals(a.right <= b.left, true)
        assertEquals(b.left <= b.right, true)
        assertEquals(b.right <= c.left, true)
        assertEquals(c.left <= c.right, true)
    }

    @Test
    fun testBasicVerticalChainThreeElements() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        val marginT = 7
        val marginB = 27
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        c.setDebugSolverName(root.getSystem(), "C")
        val widgets: ArrayList<ConstraintWidget> = ArrayList<ConstraintWidget>()
        widgets.add(a)
        widgets.add(b)
        widgets.add(c)
        widgets.add(root)
        root.add(a)
        root.add(b)
        root.add(c)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 0)
        a.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.TOP, 0)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM, 0)
        b.connect(ConstraintAnchor.Type.BOTTOM, c, ConstraintAnchor.Type.TOP, 0)
        c.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.BOTTOM, 0)
        c.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 0)
        root.layout()
        println("a) A: $a B: $b C: $c")
        // all elements spread equally
        assertEquals(a.height, b.height, 1)
        assertEquals(b.height, c.height, 1)
        assertEquals(
            a.top - root.top,
            root.bottom - c.bottom,
            1,
        )
        assertEquals(a.top - root.top, b.top - a.bottom, 1)
        assertEquals(b.top - a.bottom, c.top - b.bottom, 1)
        // A marked as 0dp, B == C, A takes the rest
        a.getAnchor(ConstraintAnchor.Type.TOP)!!.setMargin(marginT)
        a.getAnchor(ConstraintAnchor.Type.BOTTOM)!!.setMargin(marginB)
        b.getAnchor(ConstraintAnchor.Type.TOP)!!.setMargin(marginT)
        b.getAnchor(ConstraintAnchor.Type.BOTTOM)!!.setMargin(marginB)
        c.getAnchor(ConstraintAnchor.Type.TOP)!!.setMargin(marginT)
        c.getAnchor(ConstraintAnchor.Type.BOTTOM)!!.setMargin(marginB)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        root.layout()
        println("b) A: $a B: $b C: $c")
        assertEquals(a.top, 7)
        assertEquals(c.bottom, 573)
        assertEquals(b.bottom, 519)
        assertEquals(a.height, 458)
        assertEquals(b.height, c.height)
        assertEquals(b.height, 20)
        checkVerticalPositions(a, b, c)
        // B marked as 0dp, A == C, B takes the rest
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.FIXED)
        a.height = 20
        b.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        root.layout()
        println("c) A: $a B: $b C: $c")
        assertEquals(b.height, 458)
        assertEquals(a.height, c.height)
        assertEquals(a.height, 20)
        checkVerticalPositions(a, b, c)
        // C marked as 0dp, A == B, C takes the rest
        b.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.FIXED)
        b.height = 20
        c.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        root.layout()
        println("d) A: $a B: $b C: $c")
        assertEquals(c.height, 458)
        assertEquals(a.height, b.height)
        assertEquals(a.height, 20)
        checkVerticalPositions(a, b, c)
        // A & B marked as 0dp, C == 20
        c.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.FIXED)
        c.height = 20
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        root.layout()
        println("e) A: $a B: $b C: $c")
        assertEquals(c.height, 20)
        assertEquals(a.height, b.height) // L
        assertEquals(a.height, 239)
        checkVerticalPositions(a, b, c)
        // A & C marked as 0dp, B == 20
        c.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.FIXED)
        b.height = 20
        root.layout()
        println("f) A: $a B: $b C: $c")
        assertEquals(b.height, 20)
        assertEquals(a.height, c.height)
        assertEquals(a.height, 239)
        checkVerticalPositions(a, b, c)
        // B & C marked as 0dp, A == 20
        b.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.FIXED)
        a.height = 20
        root.layout()
        println("g) A: $a B: $b C: $c")
        assertEquals(a.height, 20)
        assertEquals(b.height, c.height)
        assertEquals(b.height, 239)
        checkVerticalPositions(a, b, c)
        // A == 0dp, B & C == 20, C is gone
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.height = 20
        b.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.FIXED)
        b.height = 20
        c.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.FIXED)
        c.height = 20
        c.visibility = ConstraintWidget.GONE
        root.layout()
        println("h) A: $a B: $b C: $c")
        assertEquals(a.height, 512)
        assertEquals(b.height, 20)
        assertEquals(c.height, 0)
        checkVerticalPositions(a, b, c)
    }

    private fun checkVerticalPositions(
        a: ConstraintWidget,
        b: ConstraintWidget,
        c: ConstraintWidget,
    ) {
        assertEquals(a.top <= a.bottom, true)
        assertEquals(a.bottom <= b.top, true)
        assertEquals(b.top <= b.bottom, true)
        assertEquals(b.bottom <= c.top, true)
        assertEquals(c.top <= c.bottom, true)
    }

    @Test
    fun testHorizontalChainWeights() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        val marginL = 7
        val marginR = 27
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        c.setDebugSolverName(root.getSystem(), "C")
        val widgets: ArrayList<ConstraintWidget> = ArrayList<ConstraintWidget>()
        widgets.add(a)
        widgets.add(b)
        widgets.add(c)
        widgets.add(root)
        root.add(a)
        root.add(b)
        root.add(c)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, marginL)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT, marginR)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT, marginL)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT, marginR)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT, marginL)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, marginR)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        c.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setHorizontalWeight(1f)
        b.setHorizontalWeight(1f)
        c.setHorizontalWeight(1f)
        root.layout()
        println("a) A: $a B: $b C: $c")
        assertEquals(a.width, b.width, 1)
        assertEquals(b.width, c.width, 1)
        a.setHorizontalWeight(1f)
        b.setHorizontalWeight(2f)
        c.setHorizontalWeight(1f)
        root.layout()
        println("b) A: $a B: $b C: $c")
        assertEquals(2 * a.width, b.width, 1)
        assertEquals(a.width, c.width, 1)
    }

    @Test
    fun testVerticalChainWeights() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        val marginT = 7
        val marginB = 27
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        c.setDebugSolverName(root.getSystem(), "C")
        val widgets: ArrayList<ConstraintWidget> = ArrayList<ConstraintWidget>()
        widgets.add(a)
        widgets.add(b)
        widgets.add(c)
        widgets.add(root)
        root.add(a)
        root.add(b)
        root.add(c)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, marginT)
        a.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.TOP, marginB)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM, marginT)
        b.connect(ConstraintAnchor.Type.BOTTOM, c, ConstraintAnchor.Type.TOP, marginB)
        c.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.BOTTOM, marginT)
        c.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, marginB)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        c.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalWeight(1f)
        b.setVerticalWeight(1f)
        c.setVerticalWeight(1f)
        root.layout()
        println("a) A: $a B: $b C: $c")
        assertEquals(a.height, b.height, 1)
        assertEquals(b.height, c.height, 1)
        a.setVerticalWeight(1f)
        b.setVerticalWeight(2f)
        c.setVerticalWeight(1f)
        root.layout()
        println("b) A: $a B: $b C: $c")
        assertEquals(2 * a.height, b.height, 1)
        assertEquals(a.height, c.height, 1)
    }

    @Test
    fun testHorizontalChainPacked() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        val marginL = 7
        val marginR = 27
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        c.setDebugSolverName(root.getSystem(), "C")
        val widgets: ArrayList<ConstraintWidget> = ArrayList<ConstraintWidget>()
        widgets.add(a)
        widgets.add(b)
        widgets.add(c)
        widgets.add(root)
        root.add(a)
        root.add(b)
        root.add(c)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, marginL)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT, marginR)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT, marginL)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT, marginR)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT, marginL)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, marginR)
        a.setHorizontalChainStyle(ConstraintWidget.CHAIN_PACKED)
        root.layout()
        println("a) A: $a B: $b C: $c")
        assertEquals(
            a.left - root.left - marginL,
            root.right - marginR - c.right,
            1,
        )
    }

    @Test
    fun testVerticalChainPacked() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        val marginT = 7
        val marginB = 27
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        c.setDebugSolverName(root.getSystem(), "C")
        val widgets: ArrayList<ConstraintWidget> = ArrayList<ConstraintWidget>()
        widgets.add(a)
        widgets.add(b)
        widgets.add(c)
        widgets.add(root)
        root.add(a)
        root.add(b)
        root.add(c)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, marginT)
        a.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.TOP, marginB)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM, marginT)
        b.connect(ConstraintAnchor.Type.BOTTOM, c, ConstraintAnchor.Type.TOP, marginB)
        c.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.BOTTOM, marginT)
        c.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, marginB)
        a.setVerticalChainStyle(ConstraintWidget.CHAIN_PACKED)
        root.layout()
        println("a) A: $a B: $b C: $c")
        assertEquals(
            a.top - root.top - marginT,
            root.bottom - marginB - c.bottom,
            1,
        )
    }

    @Test
    fun testHorizontalChainComplex() {
        val root = ConstraintWidgetContainer(0, 0, 1000, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        val d = ConstraintWidget(50, 20)
        val e = ConstraintWidget(50, 20)
        val f = ConstraintWidget(50, 20)
        val marginL = 7
        val marginR = 19
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        c.setDebugSolverName(root.getSystem(), "C")
        d.setDebugSolverName(root.getSystem(), "D")
        e.setDebugSolverName(root.getSystem(), "E")
        f.setDebugSolverName(root.getSystem(), "F")
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(d)
        root.add(e)
        root.add(f)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, marginL)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT, marginR)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT, marginL)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT, marginR)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT, marginL)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, marginR)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        c.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        d.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.LEFT, 0)
        d.connect(ConstraintAnchor.Type.RIGHT, a, ConstraintAnchor.Type.RIGHT, 0)
        e.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.LEFT, 0)
        e.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.RIGHT, 0)
        f.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.LEFT, 0)
        f.connect(ConstraintAnchor.Type.RIGHT, a, ConstraintAnchor.Type.RIGHT, 0)
        root.layout()
        println("a) A: $a B: $b C: $c")
        println("a) D: $d E: $e F: $f")
        assertEquals(a.width, b.width, 1)
        assertEquals(b.width, c.width, 1)
        assertEquals(a.width, 307, 1)
    }

    @Test
    fun testVerticalChainComplex() {
        val root = ConstraintWidgetContainer(0, 0, 1000, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        val d = ConstraintWidget(50, 20)
        val e = ConstraintWidget(50, 20)
        val f = ConstraintWidget(50, 20)
        val marginT = 7
        val marginB = 19
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        c.setDebugSolverName(root.getSystem(), "C")
        d.setDebugSolverName(root.getSystem(), "D")
        e.setDebugSolverName(root.getSystem(), "E")
        f.setDebugSolverName(root.getSystem(), "F")
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(d)
        root.add(e)
        root.add(f)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, marginT)
        a.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.TOP, marginB)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM, marginT)
        b.connect(ConstraintAnchor.Type.BOTTOM, c, ConstraintAnchor.Type.TOP, marginB)
        c.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.BOTTOM, marginT)
        c.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, marginB)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        c.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        d.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.TOP, 0)
        d.connect(ConstraintAnchor.Type.BOTTOM, a, ConstraintAnchor.Type.BOTTOM, 0)
        e.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.TOP, 0)
        e.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.BOTTOM, 0)
        f.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.TOP, 0)
        f.connect(ConstraintAnchor.Type.BOTTOM, a, ConstraintAnchor.Type.BOTTOM, 0)
        root.layout()
        println("a) A: $a B: $b C: $c")
        println("a) D: $d E: $e F: $f")
        assertEquals(a.height, b.height, 1)
        assertEquals(b.height, c.height, 1)
        assertEquals(a.height, 174, 1)
    }

    @Test
    fun testHorizontalChainComplex2() {
        val root = ConstraintWidgetContainer(0, 0, 379, 591)
        val a = ConstraintWidget(100, 185)
        val b = ConstraintWidget(100, 185)
        val c = ConstraintWidget(100, 185)
        val d = ConstraintWidget(53, 17)
        val e = ConstraintWidget(42, 17)
        val f = ConstraintWidget(47, 17)
        val marginL = 0
        val marginR = 0
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        c.setDebugSolverName(root.getSystem(), "C")
        d.setDebugSolverName(root.getSystem(), "D")
        e.setDebugSolverName(root.getSystem(), "E")
        f.setDebugSolverName(root.getSystem(), "F")
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(d)
        root.add(e)
        root.add(f)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 16)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 16)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, marginL)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT, marginR)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT, marginL)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT, marginR)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.TOP, 0)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT, marginL)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, marginR)
        c.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.TOP, 0)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        c.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        d.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.LEFT, 0)
        d.connect(ConstraintAnchor.Type.RIGHT, a, ConstraintAnchor.Type.RIGHT, 0)
        d.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM, 0)
        e.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.LEFT, 0)
        e.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.RIGHT, 0)
        e.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM, 0)
        f.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.LEFT, 0)
        f.connect(ConstraintAnchor.Type.RIGHT, a, ConstraintAnchor.Type.RIGHT, 0)
        f.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM, 0)
        root.layout()
        println("a) A: $a B: $b C: $c")
        println("a) D: $d E: $e F: $f")
        assertEquals(a.width, b.width, 1)
        assertEquals(b.width, c.width, 1)
        assertEquals(a.width, 126)
    }

    @Test
    fun testVerticalChainBaseline() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        root.add(a)
        root.add(b)
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        c.setDebugSolverName(root.getSystem(), "C")
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 0)
        a.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.TOP, 0)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM, 0)
        b.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 0)
        root.layout()
        println("a) root: $root A: $a B: $b")
        val Ay: Int = a.top
        val By: Int = b.top
        assertEquals(
            a.top - root.top,
            root.bottom - b.bottom,
            1,
        )
        assertEquals(b.top - a.bottom, a.top - root.top, 1)
        root.add(c)
        a.baselineDistance = 7
        c.baselineDistance = 7
        c.connect(ConstraintAnchor.Type.BASELINE, a, ConstraintAnchor.Type.BASELINE, 0)
        root.layout()
        println("b) root: $root A: $a B: $b C: $c")
        assertEquals(Ay, c.top, 1)
        a.setVerticalChainStyle(ConstraintWidget.CHAIN_PACKED)
        root.layout()
        println("c) root: $root A: $a B: $b C: $c")
    }

    @Test
    fun testWrapHorizontalChain() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        root.add(a)
        root.add(b)
        root.add(c)
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        c.setDebugSolverName(root.getSystem(), "C")
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 0)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 0)
        b.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 0)
        b.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 0)
        c.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 0)
        c.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 0)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 0)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT, 0)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT, 0)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT, 0)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT, 0)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 0)
        root.layout()
        println("a) root: $root A: $a B: $b C: $c")
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("b) root: $root A: $a B: $b C: $c")
        assertEquals(root.height, a.height)
        assertEquals(root.height, b.height)
        assertEquals(root.height, c.height)
        assertEquals(root.width, a.width + b.width + c.width)
    }

    @Test
    fun testWrapVerticalChain() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        root.add(a)
        root.add(b)
        root.add(c)
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        c.setDebugSolverName(root.getSystem(), "C")
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 0)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 0)
        b.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 0)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 0)
        c.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 0)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 0)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 0)
        a.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.TOP, 0)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM, 0)
        b.connect(ConstraintAnchor.Type.BOTTOM, c, ConstraintAnchor.Type.TOP, 0)
        c.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.BOTTOM, 0)
        c.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 0)
        root.layout()
        println("a) root: $root A: $a B: $b")
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("b) root: $root A: $a B: $b")
        assertEquals(root.width, a.width)
        assertEquals(root.width, b.width)
        assertEquals(root.width, c.width)
        assertEquals(root.height, a.height + b.height + c.height)
    }

    @Test
    fun testPackWithBaseline() {
        val root = ConstraintWidgetContainer(0, 0, 411, 603)
        val a = ConstraintWidget(118, 93, 88, 48)
        val b = ConstraintWidget(206, 93, 88, 48)
        val c = ConstraintWidget(69, 314, 88, 48)
        val d = ConstraintWidget(83, 458, 88, 48)
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(d)
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        c.setDebugSolverName(root.getSystem(), "C")
        d.setDebugSolverName(root.getSystem(), "D")
        a.baselineDistance = 29
        b.baselineDistance = 29
        c.baselineDistance = 29
        d.baselineDistance = 29
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 100)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.BASELINE, a, ConstraintAnchor.Type.BASELINE)
        c.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        d.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        d.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        c.connect(ConstraintAnchor.Type.BOTTOM, d, ConstraintAnchor.Type.TOP)
        d.connect(ConstraintAnchor.Type.TOP, c, ConstraintAnchor.Type.BOTTOM)
        d.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        a.setHorizontalChainStyle(ConstraintWidget.CHAIN_PACKED)
        c.setVerticalChainStyle(ConstraintWidget.CHAIN_PACKED)
        root.layout()
        println("a) root: $root A: $a B: $b")
        println("a) root: $root C: $c D: $d")
        c.getAnchor(ConstraintAnchor.Type.TOP)!!.reset()
        root.layout()
        c.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        root.layout()
        println("a) root: $root A: $a B: $b")
        println("a) root: $root C: $c D: $d")
        assertEquals(c.bottom, d.top)
    }

    @Test
    fun testBasicGoneChain() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
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
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.RIGHT, d, ConstraintAnchor.Type.LEFT)
        d.connect(ConstraintAnchor.Type.LEFT, c, ConstraintAnchor.Type.RIGHT)
        d.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.setHorizontalChainStyle(ConstraintWidget.CHAIN_SPREAD_INSIDE)
        b.visibility = ConstraintWidget.GONE
        root.layout()
        println("a) A: $a B: $b C: $c D: $d")
        assertEquals(a.left, 0)
        assertEquals(c.left, 250)
        assertEquals(d.left, 500)
        b.visibility = ConstraintWidget.VISIBLE
        d.visibility = ConstraintWidget.GONE
        root.layout()
        println("b) A: $a B: $b C: $c D: $d")
    }

    @Test
    fun testGonePackChain() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val guideline = Guideline()
        val d = ConstraintWidget(100, 20)
        guideline.setOrientation(Guideline.VERTICAL)
        guideline.setGuideBegin(200)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        guideline.debugName = "guideline"
        d.debugName = "D"
        root.add(a)
        root.add(b)
        root.add(guideline)
        root.add(d)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, guideline, ConstraintAnchor.Type.LEFT)
        d.connect(ConstraintAnchor.Type.LEFT, guideline, ConstraintAnchor.Type.RIGHT)
        d.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.setHorizontalChainStyle(ConstraintWidget.CHAIN_PACKED)
        a.visibility = ConstraintWidget.GONE
        b.visibility = ConstraintWidget.GONE
        root.layout()
        println("a) A: $a B: $b guideline: $guideline D: $d")
        assertEquals(a.width, 0)
        assertEquals(b.width, 0)
        assertEquals(guideline.left, 200)
        assertEquals(d.left, 350)
        a.setHorizontalChainStyle(ConstraintWidget.CHAIN_SPREAD)
        root.layout()
        println("b) A: $a B: $b guideline: $guideline D: $d")
        assertEquals(a.width, 0)
        assertEquals(b.width, 0)
        assertEquals(guideline.left, 200)
        assertEquals(d.left, 350)
        a.setHorizontalChainStyle(ConstraintWidget.CHAIN_SPREAD_INSIDE)
        root.layout()
        println("c) A: $a B: $b guideline: $guideline D: $d")
        assertEquals(a.width, 0)
        assertEquals(b.width, 0)
        assertEquals(guideline.left, 200)
        assertEquals(d.left, 350)
    }

    @Test
    fun testVerticalGonePackChain() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val guideline = Guideline()
        val d = ConstraintWidget(100, 20)
        guideline.setOrientation(Guideline.HORIZONTAL)
        guideline.setGuideBegin(200)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        guideline.debugName = "guideline"
        d.debugName = "D"
        root.add(a)
        root.add(b)
        root.add(guideline)
        root.add(d)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.BOTTOM, guideline, ConstraintAnchor.Type.TOP)
        d.connect(ConstraintAnchor.Type.TOP, guideline, ConstraintAnchor.Type.BOTTOM)
        d.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        a.setVerticalChainStyle(ConstraintWidget.CHAIN_PACKED)
        a.visibility = ConstraintWidget.GONE
        b.visibility = ConstraintWidget.GONE
        root.layout()
        println("a) A: $a B: $b guideline: $guideline D: $d")
        assertEquals(a.height, 0)
        assertEquals(b.height, 0)
        assertEquals(guideline.top, 200)
        assertEquals(d.top, 390)
        a.setVerticalChainStyle(ConstraintWidget.CHAIN_SPREAD)
        root.layout()
        println("b) A: $a B: $b guideline: $guideline D: $d")
        assertEquals(a.height, 0)
        assertEquals(b.height, 0)
        assertEquals(guideline.top, 200)
        assertEquals(d.top, 390)
        a.setVerticalChainStyle(ConstraintWidget.CHAIN_SPREAD_INSIDE)
        root.layout()
        println("c) A: $a B: $b guideline: $guideline D: $d")
        assertEquals(a.height, 0)
        assertEquals(b.height, 0)
        assertEquals(guideline.top, 200)
        assertEquals(d.top, 390)
    }

    @Test
    fun testVerticalDanglingChain() {
        val root = ConstraintWidgetContainer(0, 0, 600, 1000)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        root.add(a)
        root.add(b)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.TOP, 7)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM, 9)
        root.layout()
        println("a) A: $a B: $b")
        assertEquals(a.top, 0)
        assertEquals(b.top.toDouble(), a.height + max(7.0, 9.0))
    }

    @Test
    fun testHorizontalWeightChain() {
        val root = ConstraintWidgetContainer(0, 0, 600, 1000)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        val guidelineLeft = Guideline()
        val guidelineRight = Guideline()
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        guidelineLeft.debugName = "guidelineLeft"
        guidelineRight.debugName = "guidelineRight"
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(guidelineLeft)
        root.add(guidelineRight)
        guidelineLeft.setOrientation(Guideline.VERTICAL)
        guidelineRight.setOrientation(Guideline.VERTICAL)
        guidelineLeft.setGuideBegin(20)
        guidelineRight.setGuideEnd(20)
        a.connect(ConstraintAnchor.Type.LEFT, guidelineLeft, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.RIGHT, guidelineRight, ConstraintAnchor.Type.RIGHT)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        c.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setHorizontalWeight(1f)
        b.setHorizontalWeight(1f)
        c.setHorizontalWeight(1f)
        root.layout()
        println("a) A: $a B: $b C: $c")
        assertEquals(a.left, 20)
        assertEquals(b.left, 207)
        assertEquals(c.left, 393)
        assertEquals(a.width, 187)
        assertEquals(b.width, 186)
        assertEquals(c.width, 187)
        c.visibility = ConstraintWidget.GONE
        root.layout()
        println("b) A: $a B: $b C: $c")
        assertEquals(a.left, 20)
        assertEquals(b.left, 300)
        assertEquals(c.left, 580)
        assertEquals(a.width, 280)
        assertEquals(b.width, 280)
        assertEquals(c.width, 0)
    }

    @Test
    fun testVerticalGoneChain() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        val widgets: ArrayList<ConstraintWidget> = ArrayList<ConstraintWidget>()
        widgets.add(a)
        widgets.add(b)
        widgets.add(root)
        root.add(a)
        root.add(b)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 16)
        a.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.TOP)
        a.getAnchor(ConstraintAnchor.Type.BOTTOM)!!.setGoneMargin(16)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 16)
        a.setVerticalChainStyle(ConstraintWidget.CHAIN_PACKED)
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("a) root: $root A: $a B: $b")
        assertEquals(a.height, b.height, 1)
        assertEquals(
            a.top - root.top,
            root.bottom - b.bottom,
            1,
        )
        assertEquals(a.bottom, b.top)
        b.visibility = ConstraintWidget.GONE
        root.layout()
        println("b) root: $root A: $a B: $b")
        assertEquals(a.top - root.top, root.bottom - a.bottom)
        assertEquals(root.height, 52)
    }

    @Test
    fun testVerticalGoneChain2() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
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
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 16)
        a.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.BOTTOM, c, ConstraintAnchor.Type.TOP)
        b.getAnchor(ConstraintAnchor.Type.TOP)!!.setGoneMargin(16)
        b.getAnchor(ConstraintAnchor.Type.BOTTOM)!!.setGoneMargin(16)
        c.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.BOTTOM)
        c.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 16)
        a.setVerticalChainStyle(ConstraintWidget.CHAIN_PACKED)
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("a) root: $root A: $a B: $b C: $c")
        assertEquals(
            a.top - root.top,
            root.bottom - c.bottom,
            1,
        )
        assertEquals(a.bottom, b.top)
        a.visibility = ConstraintWidget.GONE
        c.visibility = ConstraintWidget.GONE
        root.layout()
        println("b) root: $root A: $a B: $b C: $c")
        assertEquals(b.top - root.top, root.bottom - b.bottom)
        assertEquals(root.height, 52)
    }

    @Test
    fun testVerticalSpreadInsideChain() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
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
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 16)
        a.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.BOTTOM, c, ConstraintAnchor.Type.TOP)
        c.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.BOTTOM)
        c.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 16)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        c.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalChainStyle(ConstraintWidget.CHAIN_SPREAD_INSIDE)
        root.layout()
        println("a) root: $root A: $a B: $b C: $c")
        assertEquals(a.height, b.height, 1)
        assertEquals(b.height, c.height, 1)
        assertEquals(a.height, (root.height - 32) / 3, 1)
    }

    @Test
    fun testHorizontalSpreadMaxChain() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
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
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        c.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalChainStyle(ConstraintWidget.CHAIN_SPREAD_INSIDE)
        root.layout()
        println("a) root: $root A: $a B: $b C: $c")
        assertEquals(a.width, b.width, 1)
        assertEquals(b.width, c.width, 1)
        assertEquals(a.width, 200, 1)
        a.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_SPREAD, 0, 50, 1f)
        b.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_SPREAD, 0, 50, 1f)
        c.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_SPREAD, 0, 50, 1f)
        root.layout()
        println("b) root: $root A: $a B: $b C: $c")
        assertEquals(a.width, b.width, 1)
        assertEquals(b.width, c.width, 1)
        assertEquals(a.width, 50, 1)
    }

    @Test
    fun testPackCenterChain() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
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
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 16)
        b.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 16)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 16)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        c.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        c.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        a.setVerticalChainStyle(ConstraintWidget.CHAIN_PACKED)
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.setMinHeight(300)
        root.layout()
        println("a) root: $root A: $a B: $b C: $c")
        assertEquals(root.height, 300)
        assertEquals(c.top, (root.height - c.height) / 2)
        assertEquals(a.top, (root.height - a.height - b.height) / 2)
    }

    @Test
    fun testPackCenterChainGone() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
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
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 16)
        b.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 16)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 16)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.BOTTOM, c, ConstraintAnchor.Type.TOP)
        c.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.BOTTOM)
        c.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        a.setVerticalChainStyle(ConstraintWidget.CHAIN_PACKED)
        root.layout()
        println("a) root: $root A: $a B: $b C: $c")
        assertEquals(600, root.height)
        assertEquals(20, a.height)
        assertEquals(20, b.height)
        assertEquals(20, c.height)
        assertEquals(270, a.top)
        assertEquals(290, b.top)
        assertEquals(310, c.top)
        a.visibility = ConstraintWidget.GONE
        root.layout()
        println("a) root: $root A: $a B: $b C: $c")
        assertEquals(600, root.height)
        assertEquals(0, a.height)
        assertEquals(20, b.height)
        assertEquals(20, c.height) // todo not done
        assertEquals(a.top, b.top)
        assertEquals((600 - 40) / 2, b.top)
        assertEquals(b.top + b.height, c.top)
    }

    @Test
    fun testSpreadInsideChainWithMargins() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
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
        var marginOut = 0
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, marginOut)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, marginOut)
        a.setHorizontalChainStyle(ConstraintWidget.CHAIN_SPREAD_INSIDE)
        root.layout()
        println("a) root: $root A: $a B: $b C: $c")
        assertEquals(a.left, marginOut)
        assertEquals(c.right, root.width - marginOut)
        assertEquals(b.left, a.right + (c.left - a.right - b.width) / 2)
        marginOut = 20
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, marginOut)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, marginOut)
        root.layout()
        println("b) root: $root A: $a B: $b C: $c")
        assertEquals(a.left, marginOut)
        assertEquals(c.right, root.width - marginOut)
        assertEquals(b.left, a.right + (c.left - a.right - b.width) / 2)
    }
}
