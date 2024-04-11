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
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.DimensionBehaviour
import androidx.constraintlayout.coremp.widgets.ConstraintWidgetContainer
import kotlin.test.Test
import kotlin.test.assertEquals

class AdvancedChainTest {
    @Test
    fun testComplexChainWeights() {
        val root = ConstraintWidgetContainer(0, 0, 800, 800)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        a.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.LEFT,
            root,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.LEFT,
        )
        a.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.RIGHT,
            root,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.RIGHT,
        )
        b.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.LEFT,
            root,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.LEFT,
        )
        b.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.RIGHT,
            root,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.RIGHT,
        )
        a.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.TOP,
            root,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.TOP,
            0,
        )
        a.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.BOTTOM,
            b,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.TOP,
            0,
        )
        b.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.TOP,
            a,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.BOTTOM,
            0,
        )
        b.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.BOTTOM,
            root,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.BOTTOM,
            0,
        )
        root.add(a)
        root.add(b)
        a.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        b.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        b.setVerticalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        root.optimizationLevel = androidx.constraintlayout.coremp.widgets.Optimizer.OPTIMIZATION_NONE
        root.layout()
        println("root: $root")
        println("A: $a")
        println("B: $b")
        assertEquals(a.width, 800)
        assertEquals(b.width, 800)
        assertEquals(a.height, 400)
        assertEquals(b.height, 400)
        assertEquals(a.top, 0)
        assertEquals(b.top, 400)
        a.setDimensionRatio("16:3")
        root.layout()
        println("root: $root")
        println("A: $a")
        println("B: $b")
        assertEquals(a.width, 800)
        assertEquals(b.width, 800)
        assertEquals(a.height, 150)
        assertEquals(b.height, 150)
        assertEquals(a.top, 167)
        assertEquals(b.top, 483)
        b.setVerticalWeight(1f)
        root.layout()
        println("root: $root")
        println("A: $a")
        println("B: $b")
        assertEquals(a.width, 800)
        assertEquals(b.width, 800)
        assertEquals(a.height, 150)
        assertEquals(b.height, 650)
        assertEquals(a.top, 0)
        assertEquals(b.top, 150)
        a.setVerticalWeight(1f)
        root.layout()
        println("root: $root")
        println("A: $a")
        println("B: $b")
        assertEquals(a.width, 800)
        assertEquals(b.width, 800)
        assertEquals(a.height, 150)
        assertEquals(b.height, 150)
        assertEquals(a.top, 167)
        assertEquals(b.top, 483)
    }

    @Test
    fun testTooSmall() {
        val root = ConstraintWidgetContainer(0, 0, 800, 800)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        c.setDebugSolverName(root.getSystem(), "C")
        root.add(a)
        root.add(b)
        root.add(c)
        a.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.LEFT,
            root,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.LEFT,
        )
        a.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.TOP,
            root,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.TOP,
        )
        a.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.BOTTOM,
            root,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.BOTTOM,
        )
        b.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.LEFT,
            a,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.RIGHT,
            100,
        )
        c.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.LEFT,
            a,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.RIGHT,
            100,
        )
        b.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.TOP,
            a,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.TOP,
        )
        b.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.BOTTOM,
            c,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.TOP,
        )
        c.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.TOP,
            b,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.BOTTOM,
        )
        c.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.BOTTOM,
            a,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.BOTTOM,
        )
        root.optimizationLevel = androidx.constraintlayout.coremp.widgets.Optimizer.OPTIMIZATION_NONE
        root.layout()
        println("A: $a")
        println("B: $b")
        println("C: $c")
        assertEquals(a.top, 390)
        assertEquals(b.top, 380)
        assertEquals(c.top, 400)
    }

    @Test
    fun testChainWeights() {
        val root = ConstraintWidgetContainer(0, 0, 800, 800)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        a.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.LEFT,
            root,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.LEFT,
            0,
        )
        a.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.RIGHT,
            b,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.LEFT,
            0,
        )
        b.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.LEFT,
            a,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.RIGHT,
            0,
        )
        b.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.RIGHT,
            root,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.RIGHT,
            0,
        )
        root.add(a)
        root.add(b)
        a.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        b.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        a.setHorizontalWeight(1f)
        b.setHorizontalWeight(0f)
        root.optimizationLevel = androidx.constraintlayout.coremp.widgets.Optimizer.OPTIMIZATION_NONE
        root.layout()
        println("A: $a")
        println("B: $b")
        assertEquals(a.width, 800)
        assertEquals(b.width, 0)
        assertEquals(a.left, 0)
        assertEquals(b.left, 800)
    }

    @Test
    fun testChain3Weights() {
        val root = ConstraintWidgetContainer(0, 0, 800, 800)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        c.setDebugSolverName(root.getSystem(), "C")
        a.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.LEFT,
            root,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.LEFT,
            0,
        )
        a.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.RIGHT,
            b,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.LEFT,
            0,
        )
        b.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.LEFT,
            a,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.RIGHT,
            0,
        )
        b.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.RIGHT,
            c,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.LEFT,
            0,
        )
        c.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.LEFT,
            b,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.RIGHT,
            0,
        )
        c.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.RIGHT,
            root,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.RIGHT,
            0,
        )
        root.add(a)
        root.add(b)
        root.add(c)
        a.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        b.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        c.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        a.setHorizontalWeight(1f)
        b.setHorizontalWeight(0f)
        c.setHorizontalWeight(1f)
        root.optimizationLevel = androidx.constraintlayout.coremp.widgets.Optimizer.OPTIMIZATION_NONE
        root.layout()
        println("A: $a")
        println("B: $b")
        println("C: $c")
        assertEquals(a.width, 400)
        assertEquals(b.width, 0)
        assertEquals(c.width, 400)
        assertEquals(a.left, 0)
        assertEquals(b.left, 400)
        assertEquals(c.left, 400)
    }

    @Test
    fun testChainLastGone() {
        val root = ConstraintWidgetContainer(0, 0, 800, 800)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        val d = ConstraintWidget(100, 20)
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        c.setDebugSolverName(root.getSystem(), "C")
        d.setDebugSolverName(root.getSystem(), "D")
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(d)
        a.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.LEFT,
            root,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.LEFT,
            0,
        )
        a.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.RIGHT,
            root,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.RIGHT,
            0,
        )
        b.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.LEFT,
            root,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.LEFT,
            0,
        )
        b.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.RIGHT,
            root,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.RIGHT,
            0,
        )
        c.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.LEFT,
            root,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.LEFT,
            0,
        )
        c.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.RIGHT,
            root,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.RIGHT,
            0,
        )
        d.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.LEFT,
            root,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.LEFT,
            0,
        )
        d.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.RIGHT,
            root,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.RIGHT,
            0,
        )
        a.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.TOP,
            root,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.TOP,
            0,
        )
        a.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.BOTTOM,
            b,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.TOP,
            0,
        )
        b.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.TOP,
            a,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.BOTTOM,
            0,
        )
        b.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.BOTTOM,
            c,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.TOP,
            0,
        )
        c.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.TOP,
            b,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.BOTTOM,
            0,
        )
        c.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.BOTTOM,
            d,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.TOP,
            0,
        )
        d.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.TOP,
            c,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.BOTTOM,
            0,
        )
        d.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.BOTTOM,
            root,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.BOTTOM,
            0,
        )
        b.visibility = ConstraintWidget.GONE
        d.visibility = ConstraintWidget.GONE
        root.optimizationLevel = androidx.constraintlayout.coremp.widgets.Optimizer.OPTIMIZATION_NONE
        root.layout()
        println("A: $a")
        println("B: $b")
        println("C: $c")
        println("D: $d")
        assertEquals(a.top, 253)
        assertEquals(c.top, 527)
    }

    @Test
    fun testRatioChainGone() {
        val root = ConstraintWidgetContainer(0, 0, 800, 800)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        val ratio = ConstraintWidget(100, 20)
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        c.setDebugSolverName(root.getSystem(), "C")
        ratio.setDebugSolverName(root.getSystem(), "ratio")
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(ratio)
        a.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.LEFT,
            root,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.LEFT,
            0,
        )
        a.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.RIGHT,
            root,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.RIGHT,
            0,
        )
        b.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.LEFT,
            root,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.LEFT,
            0,
        )
        b.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.RIGHT,
            root,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.RIGHT,
            0,
        )
        c.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.LEFT,
            root,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.LEFT,
            0,
        )
        c.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.RIGHT,
            root,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.RIGHT,
            0,
        )
        ratio.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.TOP,
            root,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.TOP,
            0,
        )
        ratio.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.LEFT,
            root,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.LEFT,
            0,
        )
        ratio.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.RIGHT,
            root,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.RIGHT,
            0,
        )
        a.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.TOP,
            root,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.TOP,
            0,
        )
        a.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.BOTTOM,
            b,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.TOP,
            0,
        )
        b.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.TOP,
            a,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.BOTTOM,
            0,
        )
        b.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.BOTTOM,
            ratio,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.BOTTOM,
            0,
        )
        c.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.TOP,
            b,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.TOP,
            0,
        )
        c.connect(
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.BOTTOM,
            ratio,
            androidx.constraintlayout.coremp.widgets.ConstraintAnchor.Type.BOTTOM,
            0,
        )
        a.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        b.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        c.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        ratio.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        b.setVerticalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        c.setVerticalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        ratio.setVerticalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        ratio.setDimensionRatio("4:3")
        b.visibility = ConstraintWidget.GONE
        c.visibility = ConstraintWidget.GONE
        root.optimizationLevel = androidx.constraintlayout.coremp.widgets.Optimizer.OPTIMIZATION_NONE
        root.layout()
        println("A: $a")
        println("B: $b")
        println("C: $c")
        println("ratio: $ratio")
        assertEquals(a.height, 600)
        root.setVerticalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("A: $a")
        println("B: $b")
        println("C: $c")
        println("ratio: $ratio")
        println("root: $root")
        assertEquals(a.height, 600)
    }

    @Test
    fun testSimpleHorizontalChainPacked() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        val widgets: ArrayList<ConstraintWidget> = ArrayList<ConstraintWidget>()
        widgets.add(a)
        widgets.add(b)
        widgets.add(root)
        root.add(a)
        root.add(b)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 20)
        b.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 20)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 0)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT, 0)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT, 0)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 0)
        a.setHorizontalChainStyle(ConstraintWidget.CHAIN_PACKED)
        root.layout()
        println("a) A: $a B: $b")
        assertEquals(a.left - root.left, root.right - b.right)
        assertEquals(b.left - a.right, 0)
    }

    @Test
    fun testSimpleVerticalTChainPacked() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        val widgets: ArrayList<ConstraintWidget> = ArrayList<ConstraintWidget>()
        widgets.add(a)
        widgets.add(b)
        widgets.add(root)
        root.add(a)
        root.add(b)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 20)
        b.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 20)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 0)
        a.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.TOP, 0)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM, 0)
        b.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 0)
        a.setVerticalChainStyle(ConstraintWidget.CHAIN_PACKED)
        root.layout()
        println("a) A: $a B: $b")
        assertEquals(a.top - root.top, root.bottom - b.bottom)
        assertEquals(b.top - a.bottom, 0)
    }

    @Test
    fun testHorizontalChainStyles() {
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
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT, 0)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT, 0)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT, 0)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT, 0)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 0)
        root.layout()
        println("       spread) root: $root A: $a B: $b C: $c")
        var gap: Int = (root.width - a.width - b.width - c.width) / 4
        val size = 100
        assertEquals(a.width, size)
        assertEquals(b.width, size)
        assertEquals(c.width, size)
        assertEquals(gap, a.left)
        assertEquals(a.right + gap, b.left)
        assertEquals(root.width - gap - c.width, c.left)
        a.setHorizontalChainStyle(ConstraintWidget.CHAIN_SPREAD_INSIDE)
        root.layout()
        println("spread inside) root: $root A: $a B: $b C: $c")
        gap = (root.width - a.width - b.width - c.width) / 2
        assertEquals(a.width, size)
        assertEquals(b.width, size)
        assertEquals(c.width, size)
        assertEquals(a.left, 0)
        assertEquals(a.right + gap, b.left)
        assertEquals(root.width, c.right)
        a.setHorizontalChainStyle(ConstraintWidget.CHAIN_PACKED)
        root.layout()
        println("       packed) root: $root A: $a B: $b C: $c")
        assertEquals(a.width, size)
        assertEquals(b.width, size)
        assertEquals(c.width, size)
        assertEquals(a.left, gap)
        assertEquals(root.width - gap, c.right)
    }

    @Test
    fun testVerticalChainStyles() {
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
        a.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.TOP, 0)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM, 0)
        b.connect(ConstraintAnchor.Type.BOTTOM, c, ConstraintAnchor.Type.TOP, 0)
        c.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.BOTTOM, 0)
        c.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 0)
        root.layout()
        println("       spread) root: $root A: $a B: $b C: $c")
        var gap: Int = (root.height - a.height - b.height - c.height) / 4
        val size = 20
        assertEquals(a.height, size)
        assertEquals(b.height, size)
        assertEquals(c.height, size)
        assertEquals(gap, a.top)
        assertEquals(a.bottom + gap, b.top)
        assertEquals(root.height - gap - c.height, c.top)
        a.setVerticalChainStyle(ConstraintWidget.CHAIN_SPREAD_INSIDE)
        root.layout()
        println("spread inside) root: $root A: $a B: $b C: $c")
        gap = (root.height - a.height - b.height - c.height) / 2
        assertEquals(a.height, size)
        assertEquals(b.height, size)
        assertEquals(c.height, size)
        assertEquals(a.top, 0)
        assertEquals(a.bottom + gap, b.top)
        assertEquals(root.height, c.bottom)
        a.setVerticalChainStyle(ConstraintWidget.CHAIN_PACKED)
        root.layout()
        println("       packed) root: $root A: $a B: $b C: $c")
        assertEquals(a.height, size)
        assertEquals(b.height, size)
        assertEquals(c.height, size)
        assertEquals(a.top, gap)
        assertEquals(root.height - gap, c.bottom)
    }

    @Test
    fun testPacked() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        root.add(a)
        root.add(b)
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 0)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT, 0)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT, 0)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 0)
        val gap: Int = (root.width - a.width - b.width) / 2
        val size = 100
        a.setHorizontalChainStyle(ConstraintWidget.CHAIN_PACKED)
        root.layout()
        root.optimizationLevel = 0
        println("       packed) root: $root A: $a B: $b")
        assertEquals(a.width, size)
        assertEquals(b.width, size)
        assertEquals(a.left, gap)
    }
}
