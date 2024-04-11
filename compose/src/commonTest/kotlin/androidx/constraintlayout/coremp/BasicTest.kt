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

import androidx.constraintlayout.coremp.test.assertEquals
import androidx.constraintlayout.coremp.widgets.Barrier
import androidx.constraintlayout.coremp.widgets.ConstraintAnchor
import androidx.constraintlayout.coremp.widgets.ConstraintWidget
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.DimensionBehaviour
import androidx.constraintlayout.coremp.widgets.ConstraintWidgetContainer
import androidx.constraintlayout.coremp.widgets.Guideline
import androidx.constraintlayout.coremp.widgets.Optimizer
import androidx.constraintlayout.coremp.widgets.analyzer.BasicMeasure
import kotlin.test.Test
import kotlin.test.assertEquals

class BasicTest {
    @Test
    fun testWrapPercent() {
        val root = ConstraintWidgetContainer(0, 0, 600, 800)
        val a = ConstraintWidget(100, 30)
        root.debugName = "root"
        a.debugName = "A"
        a.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        a.setHorizontalMatchStyle(
            ConstraintWidget.MATCH_CONSTRAINT_PERCENT,
            BasicMeasure.WRAP_CONTENT,
            0,
            0.5f,
        )
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        root.add(a)
        root.setHorizontalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("root: $root")
        println("A: $a")
        assertEquals(a.width, 100)
        assertEquals(root.width, a.width * 2)
    }

    @Test
    fun testMiddleSplit() {
        val root = ConstraintWidgetContainer(0, 0, 600, 800)
        root.measurer = sMeasurer
        val a = ConstraintWidget(400, 30)
        val b = ConstraintWidget(400, 60)
        val guideline = Guideline()
        val divider = ConstraintWidget(100, 30)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        guideline.debugName = "guideline"
        divider.debugName = "divider"
        root.add(a)
        root.add(b)
        root.add(guideline)
        root.add(divider)
        guideline.setOrientation(Guideline.VERTICAL)
        guideline.setGuidePercent(0.5f)
        a.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        b.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        a.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_WRAP, 0, 0, 0f)
        b.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_WRAP, 0, 0, 0f)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, guideline, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.LEFT, guideline, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        divider.width = 1
        divider.setVerticalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        divider.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        divider.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        root.setVerticalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        //        root.layout();
        root.updateHierarchy()
        root.measure(
            Optimizer.OPTIMIZATION_NONE,
            BasicMeasure.EXACTLY,
            600,
            BasicMeasure.EXACTLY,
            800,
            0,
            0,
            0,
            0,
        )
        println("root: $root")
        println("A: $a")
        println("B: $b")
        println("guideline: $guideline")
        println("divider: $divider")
        assertEquals(a.width, 300)
        assertEquals(b.width, 300)
        assertEquals(a.left, 0)
        assertEquals(b.left, 300)
        assertEquals(divider.height, 60)
        assertEquals(root.width, 600)
        assertEquals(root.height, 60)
    }

    @Test
    fun testSimpleConstraint() {
        val root = ConstraintWidgetContainer(0, 0, 600, 800)
        root.measurer = sMeasurer
        val a = ConstraintWidget(100, 30)
        root.debugName = "root"
        a.debugName = "A"
        root.add(a)
        a.setHorizontalDimensionBehaviour(DimensionBehaviour.FIXED)
        a.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 8)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 8)
        root.optimizationLevel = Optimizer.OPTIMIZATION_GRAPH
        root.measure(Optimizer.OPTIMIZATION_GRAPH, 0, 0, 0, 0, 0, 0, 0, 0)
        //        root.layout();
        println("1) A: $a")
    }

    @Test
    fun testSimpleWrapConstraint9() {
        val root = ConstraintWidgetContainer(0, 0, 600, 800)
        root.measurer = sMeasurer
        val a = ConstraintWidget(100, 30)
        root.debugName = "root"
        a.debugName = "A"
        root.add(a)
        a.setHorizontalDimensionBehaviour(DimensionBehaviour.FIXED)
        a.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
        val margin = 8
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, margin)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, margin)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, margin)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, margin)
        root.optimizationLevel = Optimizer.OPTIMIZATION_GRAPH_WRAP
        root.measure(Optimizer.OPTIMIZATION_GRAPH_WRAP, 0, 0, 0, 0, 0, 0, 0, 0)
        //        root.layout();
        println("root: $root")
        println("1) A: $a")
        root.setHorizontalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        root.setVerticalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        root.measure(Optimizer.OPTIMIZATION_GRAPH_WRAP, 0, 0, 0, 0, 0, 0, 0, 0)
        println("root: $root")
        println("1) A: $a")
        assertEquals(root.width, 116)
        assertEquals(root.height, 46)
    }

    @Test
    fun testSimpleWrapConstraint10() {
        val root = ConstraintWidgetContainer(0, 0, 600, 800)
        root.measurer = sMeasurer
        val a = ConstraintWidget(100, 30)
        root.debugName = "root"
        a.debugName = "A"
        root.add(a)
        a.setHorizontalDimensionBehaviour(DimensionBehaviour.FIXED)
        a.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
        val margin = 8
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, margin)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, margin)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, margin)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, margin)

        // root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT);
        root.measure(Optimizer.OPTIMIZATION_NONE, 0, 0, 0, 0, 0, 0, 0, 0)
        root.layout()
        println("root: $root")
        println("1) A: $a")
        root.setHorizontalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        root.measure(
            Optimizer.OPTIMIZATION_GRAPH,
            BasicMeasure.WRAP_CONTENT, 0, BasicMeasure.EXACTLY, 800, 0, 0, 0, 0,
        )
        println("root: $root")
        println("1) A: $a")
        assertEquals(root.width, 116)
        assertEquals(root.height, 800)
        assertEquals(a.left, 8)
        assertEquals(a.top, 385)
        assertEquals(a.width, 100)
        assertEquals(a.height, 30)
    }

    @Test
    fun testSimpleWrapConstraint11() {
        val root = ConstraintWidgetContainer(0, 0, 600, 800)
        root.measurer = sMeasurer
        val a = ConstraintWidget(10, 30)
        val b = ConstraintWidget(800, 30)
        val c = ConstraintWidget(10, 30)
        val d = ConstraintWidget(800, 30)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        d.debugName = "D"
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(d)
        b.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        b.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
        b.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_WRAP, 0, 0, 0f)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        c.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        d.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.BOTTOM)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        d.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        d.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT)
        root.layout()
        println("root: $root")
        println("1) A: $a")
        println("1) B: $b")
        println("1) C: $c")
        println("1) D: $d")
        assertEquals(a.left, 0)
        assertEquals(a.width, 10)
        assertEquals(c.width, 10)
        assertEquals(b.left, a.right)
        assertEquals(b.width, root.width - a.width - c.width)
        assertEquals(c.left, root.width - c.width)
        assertEquals(d.width, 800)
        assertEquals(d.left, -99)
    }

    @Test
    fun testSimpleWrapConstraint() {
        val root = ConstraintWidgetContainer(0, 0, 600, 800)
        root.measurer = sMeasurer
        val a = ConstraintWidget(100, 30)
        val b = ConstraintWidget(100, 60)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        root.add(a)
        root.add(b)
        a.setHorizontalDimensionBehaviour(DimensionBehaviour.FIXED)
        a.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
        b.setHorizontalDimensionBehaviour(DimensionBehaviour.FIXED)
        b.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 8)
        b.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 8)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 8)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT, 8)
        root.setHorizontalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        root.setVerticalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        root.measure(
            Optimizer.OPTIMIZATION_STANDARD,
            BasicMeasure.WRAP_CONTENT, 0, BasicMeasure.WRAP_CONTENT, 0, 0, 0, 0, 0,
        )
        println("0) root: $root")
        println("1) A: $a")
        println("2) B: $b")
        assertEquals(root.width, 216)
        assertEquals(root.height, 68)
        assertEquals(a.left, 8)
        assertEquals(a.top, 8)
        assertEquals(a.width, 100)
        assertEquals(a.height, 30)
        assertEquals(b.left, 116)
        assertEquals(b.top, 0)
        assertEquals(b.width, 100)
        assertEquals(b.height, 60)
        root.measure(
            Optimizer.OPTIMIZATION_GRAPH,
            BasicMeasure.WRAP_CONTENT, 0, BasicMeasure.WRAP_CONTENT, 0, 0, 0, 0, 0,
        )
        println("0) root: $root")
        println("1) A: $a")
        println("2) B: $b")
        assertEquals(root.width, 216)
        assertEquals(root.height, 68)
        assertEquals(a.left, 8)
        assertEquals(a.top, 8)
        assertEquals(a.width, 100)
        assertEquals(a.height, 30)
        assertEquals(b.left, 116)
        assertEquals(b.top, 0)
        assertEquals(b.width, 100)
        assertEquals(b.height, 60)
    }

    @Test
    fun testSimpleWrapConstraint2() {
        val root = ConstraintWidgetContainer(0, 0, 600, 800)
        root.measurer = sMeasurer
        val a = ConstraintWidget(100, 30)
        val b = ConstraintWidget(120, 60)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        root.add(a)
        root.add(b)
        a.setHorizontalDimensionBehaviour(DimensionBehaviour.FIXED)
        a.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
        b.setHorizontalDimensionBehaviour(DimensionBehaviour.FIXED)
        b.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 8)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM, 8)
        b.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 8)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 8)
        b.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 8)
        root.setHorizontalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        root.setVerticalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        root.measure(
            Optimizer.OPTIMIZATION_STANDARD,
            BasicMeasure.WRAP_CONTENT, 0, BasicMeasure.WRAP_CONTENT, 0, 0, 0, 0, 0,
        )
        //        root.layout();
        println("0) root: $root")
        println("1) A: $a")
        println("2) B: $b")
        assertEquals(root.width, 128)
        assertEquals(root.height, 114)
        assertEquals(a.left, 8)
        assertEquals(a.top, 8)
        assertEquals(a.width, 100)
        assertEquals(a.height, 30)
        assertEquals(b.left, 8)
        assertEquals(b.top, 46)
        assertEquals(b.width, 120)
        assertEquals(b.height, 60)
        root.measure(
            Optimizer.OPTIMIZATION_GRAPH,
            BasicMeasure.WRAP_CONTENT, 0, BasicMeasure.WRAP_CONTENT, 0, 0, 0, 0, 0,
        )
        println("0) root: $root")
        println("1) A: $a")
        println("2) B: $b")
        assertEquals(root.width, 128)
        assertEquals(root.height, 114)
        assertEquals(a.left, 8)
        assertEquals(a.top, 8)
        assertEquals(a.width, 100)
        assertEquals(a.height, 30)
        assertEquals(b.left, 8)
        assertEquals(b.top, 46)
        assertEquals(b.width, 120)
        assertEquals(b.height, 60)
    }

    @Test
    fun testSimpleWrapConstraint3() {
        val root = ConstraintWidgetContainer(0, 0, 600, 800)
        root.measurer = sMeasurer
        val a = ConstraintWidget(100, 30)
        root.debugName = "root"
        a.debugName = "A"
        root.add(a)
        a.setHorizontalDimensionBehaviour(DimensionBehaviour.FIXED)
        a.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 8)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 8)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 8)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 8)
        root.setHorizontalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        root.setVerticalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        root.measure(
            Optimizer.OPTIMIZATION_STANDARD,
            BasicMeasure.WRAP_CONTENT, 0, BasicMeasure.WRAP_CONTENT, 0, 0, 0, 0, 0,
        )
        println("0) root: $root")
        println("1) A: $a")
        assertEquals(root.width, 116)
        assertEquals(root.height, 46)
        assertEquals(a.left, 8)
        assertEquals(a.top, 8)
        assertEquals(a.width, 100)
        assertEquals(a.height, 30)
        root.measure(
            Optimizer.OPTIMIZATION_GRAPH,
            BasicMeasure.WRAP_CONTENT, 0, BasicMeasure.WRAP_CONTENT, 0, 0, 0, 0, 0,
        )
        println("0) root: $root")
        println("1) A: $a")
        assertEquals(root.width, 116)
        assertEquals(root.height, 46)
        assertEquals(a.left, 8)
        assertEquals(a.top, 8)
        assertEquals(a.width, 100)
        assertEquals(a.height, 30)
    }

    @Test
    fun testSimpleWrapConstraint4() {
        val root = ConstraintWidgetContainer(0, 0, 600, 800)
        root.measurer = sMeasurer
        val a = ConstraintWidget(100, 30)
        val b = ConstraintWidget(100, 30)
        val c = ConstraintWidget(100, 30)
        val d = ConstraintWidget(100, 30)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        d.debugName = "D"
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(d)
        a.setHorizontalDimensionBehaviour(DimensionBehaviour.FIXED)
        a.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
        b.setHorizontalDimensionBehaviour(DimensionBehaviour.FIXED)
        b.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
        c.setHorizontalDimensionBehaviour(DimensionBehaviour.FIXED)
        c.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
        d.setHorizontalDimensionBehaviour(DimensionBehaviour.FIXED)
        d.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 8)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 8)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM, 8)
        b.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 8)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 8)
        c.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.TOP, 8)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT, 8)
        d.connect(ConstraintAnchor.Type.BOTTOM, c, ConstraintAnchor.Type.TOP, 8)
        d.connect(ConstraintAnchor.Type.LEFT, c, ConstraintAnchor.Type.RIGHT, 8)
        root.setHorizontalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        root.setVerticalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        root.measure(
            Optimizer.OPTIMIZATION_STANDARD,
            BasicMeasure.WRAP_CONTENT, 0, BasicMeasure.WRAP_CONTENT, 0, 0, 0, 0, 0,
        )
        println("0) root: $root")
        println("1) A: $a")
        println("2) B: $b")
        println("3) C: $c")
        println("4) D: $d")
        assertEquals(root.width, 532)
        assertEquals(root.height, 76)
        assertEquals(a.left, 8)
        assertEquals(a.top, 8)
        assertEquals(a.width, 100)
        assertEquals(a.height, 30)
        assertEquals(b.left, 216)
        assertEquals(b.top, 46)
        assertEquals(b.width, 100)
        assertEquals(b.height, 30)
        assertEquals(c.left, 324)
        assertEquals(c.top, 8)
        assertEquals(c.width, 100)
        assertEquals(c.height, 30)
        assertEquals(d.left, 432)
        assertEquals(d.top, -28, 2)
        assertEquals(d.width, 100)
        assertEquals(d.height, 30)
        root.measure(
            Optimizer.OPTIMIZATION_GRAPH,
            BasicMeasure.WRAP_CONTENT, 0, BasicMeasure.WRAP_CONTENT, 0, 0, 0, 0, 0,
        )
        println("0) root: $root")
        println("1) A: $a")
        println("2) B: $b")
        println("3) C: $c")
        println("4) D: $d")
        assertEquals(root.width, 532)
        assertEquals(root.height, 76)
        assertEquals(a.left, 8)
        assertEquals(a.top, 8)
        assertEquals(a.width, 100)
        assertEquals(a.height, 30)
        assertEquals(b.left, 216)
        assertEquals(b.top, 46)
        assertEquals(b.width, 100)
        assertEquals(b.height, 30)
        assertEquals(c.left, 324)
        assertEquals(c.top, 8)
        assertEquals(c.width, 100)
        assertEquals(c.height, 30)
        assertEquals(d.left, 432)
        assertEquals(d.top, -28, 2)
        assertEquals(d.width, 100)
        assertEquals(d.height, 30)
    }

    @Test
    fun testSimpleWrapConstraint5() {
        val root = ConstraintWidgetContainer(0, 0, 600, 800)
        root.measurer = sMeasurer
        val a = ConstraintWidget(100, 30)
        val b = ConstraintWidget(100, 30)
        val c = ConstraintWidget(100, 30)
        val d = ConstraintWidget(100, 30)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        d.debugName = "D"
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(d)
        a.setHorizontalDimensionBehaviour(DimensionBehaviour.FIXED)
        a.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
        b.setHorizontalDimensionBehaviour(DimensionBehaviour.FIXED)
        b.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
        c.setHorizontalDimensionBehaviour(DimensionBehaviour.FIXED)
        c.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
        d.setHorizontalDimensionBehaviour(DimensionBehaviour.FIXED)
        d.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 8)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 8)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM, 8)
        b.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 8)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 8)
        b.horizontalBiasPercent = 0.2f
        c.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.TOP, 8)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT, 8)
        d.connect(ConstraintAnchor.Type.BOTTOM, c, ConstraintAnchor.Type.TOP, 8)
        d.connect(ConstraintAnchor.Type.LEFT, c, ConstraintAnchor.Type.RIGHT, 8)
        root.setHorizontalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        root.setVerticalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        root.measure(
            Optimizer.OPTIMIZATION_STANDARD,
            BasicMeasure.WRAP_CONTENT, 0, BasicMeasure.WRAP_CONTENT, 0, 0, 0, 0, 0,
        )
        println("0) root: $root")
        println("1) A: $a")
        println("2) B: $b")
        println("3) C: $c")
        println("4) D: $d")
        assertEquals(root.width, 376)
        assertEquals(root.height, 76)
        assertEquals(a.left, 8)
        assertEquals(a.top, 8)
        assertEquals(a.width, 100)
        assertEquals(a.height, 30)
        assertEquals(b.left, 60)
        assertEquals(b.top, 46)
        assertEquals(b.width, 100)
        assertEquals(b.height, 30)
        assertEquals(c.left, 168)
        assertEquals(c.top, 8)
        assertEquals(c.width, 100)
        assertEquals(c.height, 30)
        assertEquals(d.left, 276)
        assertEquals(d.top, -28, 2)
        assertEquals(d.width, 100)
        assertEquals(d.height, 30)
        root.measure(
            Optimizer.OPTIMIZATION_GRAPH,
            BasicMeasure.WRAP_CONTENT, 0, BasicMeasure.WRAP_CONTENT, 0, 0, 0, 0, 0,
        )
        println("0) root: $root")
        println("1) A: $a")
        println("2) B: $b")
        println("3) C: $c")
        println("4) D: $d")
        assertEquals(root.width, 376)
        assertEquals(root.height, 76)
        assertEquals(a.left, 8)
        assertEquals(a.top, 8)
        assertEquals(a.width, 100)
        assertEquals(a.height, 30)
        assertEquals(b.left, 60)
        assertEquals(b.top, 46)
        assertEquals(b.width, 100)
        assertEquals(b.height, 30)
        assertEquals(c.left, 168)
        assertEquals(c.top, 8)
        assertEquals(c.width, 100)
        assertEquals(c.height, 30)
        assertEquals(d.left, 276)
        assertEquals(d.top, -28, 2)
        assertEquals(d.width, 100)
        assertEquals(d.height, 30)
    }

    @Test
    fun testSimpleWrapConstraint6() {
        val root = ConstraintWidgetContainer(0, 0, 600, 800)
        root.measurer = sMeasurer
        val a = ConstraintWidget(100, 30)
        val b = ConstraintWidget(100, 30)
        val c = ConstraintWidget(100, 30)
        val d = ConstraintWidget(100, 30)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        d.debugName = "D"
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(d)
        a.setHorizontalDimensionBehaviour(DimensionBehaviour.FIXED)
        a.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
        b.setHorizontalDimensionBehaviour(DimensionBehaviour.FIXED)
        b.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
        c.setHorizontalDimensionBehaviour(DimensionBehaviour.FIXED)
        c.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
        d.setHorizontalDimensionBehaviour(DimensionBehaviour.FIXED)
        d.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 8)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 8)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM, 8)
        b.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 33)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 16)
        b.horizontalBiasPercent = 0.15f
        c.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.TOP, 8)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT, 12)
        d.connect(ConstraintAnchor.Type.BOTTOM, c, ConstraintAnchor.Type.TOP, 8)
        d.connect(ConstraintAnchor.Type.LEFT, c, ConstraintAnchor.Type.RIGHT, 8)
        root.setHorizontalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        root.setVerticalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        root.measure(
            Optimizer.OPTIMIZATION_STANDARD,
            BasicMeasure.WRAP_CONTENT, 0, BasicMeasure.WRAP_CONTENT, 0, 0, 0, 0, 0,
        )
        println("0) root: $root")
        println("1) A: $a")
        println("2) B: $b")
        println("3) C: $c")
        println("4) D: $d")
        assertEquals(root.width, 389)
        assertEquals(root.height, 76)
        assertEquals(a.left, 8)
        assertEquals(a.top, 8)
        assertEquals(a.width, 100)
        assertEquals(a.height, 30)
        assertEquals(b.left, 69)
        assertEquals(b.top, 46)
        assertEquals(b.width, 100)
        assertEquals(b.height, 30)
        assertEquals(c.left, 181)
        assertEquals(c.top, 8)
        assertEquals(c.width, 100)
        assertEquals(c.height, 30)
        assertEquals(d.left, 289)
        assertEquals(d.top, -28, 2)
        assertEquals(d.width, 100)
        assertEquals(d.height, 30)
        root.measure(
            Optimizer.OPTIMIZATION_GRAPH,
            BasicMeasure.WRAP_CONTENT, 0, BasicMeasure.WRAP_CONTENT, 0, 0, 0, 0, 0,
        )
        println("0) root: $root")
        println("1) A: $a")
        println("2) B: $b")
        println("3) C: $c")
        println("4) D: $d")
        assertEquals(root.width, 389)
        assertEquals(root.height, 76)
        assertEquals(a.left, 8)
        assertEquals(a.top, 8)
        assertEquals(a.width, 100)
        assertEquals(a.height, 30)
        assertEquals(b.left, 69)
        assertEquals(b.top, 46)
        assertEquals(b.width, 100)
        assertEquals(b.height, 30)
        assertEquals(c.left, 181)
        assertEquals(c.top, 8)
        assertEquals(c.width, 100)
        assertEquals(c.height, 30)
        assertEquals(d.left, 289)
        assertEquals(d.top, -28, 2)
        assertEquals(d.width, 100)
        assertEquals(d.height, 30)
    }

    @Test
    fun testSimpleWrapConstraint7() {
        val root = ConstraintWidgetContainer(0, 0, 600, 800)
        root.measurer = sMeasurer
        val a = ConstraintWidget(100, 30)
        root.debugName = "root"
        a.debugName = "A"
        root.add(a)
        a.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 8)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 8)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 8)
        root.setHorizontalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        root.setVerticalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        root.measure(
            Optimizer.OPTIMIZATION_STANDARD,
            BasicMeasure.WRAP_CONTENT, 0, BasicMeasure.WRAP_CONTENT, 0, 0, 0, 0, 0,
        )
        println("0) root: $root")
        println("1) A: $a")
        assertEquals(root.width, 16)
        assertEquals(root.height, 38)
        assertEquals(a.left, 8)
        assertEquals(a.top, 8)
        assertEquals(a.width, 0)
        assertEquals(a.height, 30)
        root.measure(
            Optimizer.OPTIMIZATION_GRAPH,
            BasicMeasure.WRAP_CONTENT, 0, BasicMeasure.WRAP_CONTENT, 0, 0, 0, 0, 0,
        )
        println("0) root: $root")
        println("1) A: $a")
        assertEquals(root.width, 16)
        assertEquals(root.height, 38)
        assertEquals(a.left, 8)
        assertEquals(a.top, 8)
        assertEquals(a.width, 0)
        assertEquals(a.height, 30)
    }

    @Test
    fun testSimpleWrapConstraint8() {
        val root = ConstraintWidgetContainer(0, 0, 600, 800)
        root.measurer = sMeasurer
        val a = ConstraintWidget(100, 30)
        val b = ConstraintWidget(10, 30)
        val c = ConstraintWidget(10, 30)
        val d = ConstraintWidget(100, 30)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        d.debugName = "D"
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(d)
        a.setHorizontalDimensionBehaviour(DimensionBehaviour.FIXED)
        b.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        c.setHorizontalDimensionBehaviour(DimensionBehaviour.FIXED)
        d.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
        b.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
        c.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
        d.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
        applyChain(ConstraintWidget.HORIZONTAL, a, b, c, d)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        d.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        root.setHorizontalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        root.setVerticalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        root.measure(
            Optimizer.OPTIMIZATION_STANDARD,
            BasicMeasure.WRAP_CONTENT, 0, BasicMeasure.WRAP_CONTENT, 0, 0, 0, 0, 0,
        )
        println("0) root: $root")
        println("1) A: $a")
        println("2) B: $b")
        println("3) C: $c")
        println("4) D: $d")
        assertEquals(root.width, 110)
        assertEquals(root.height, 30)
        root.measure(
            Optimizer.OPTIMIZATION_GRAPH,
            BasicMeasure.WRAP_CONTENT, 0, BasicMeasure.WRAP_CONTENT, 0, 0, 0, 0, 0,
        )
        println("0) root: $root")
        println("1) A: $a")
        println("2) B: $b")
        println("3) C: $c")
        println("4) D: $d")
        assertEquals(root.width, 110)
        assertEquals(root.height, 30)
    }

    @Test
    fun testSimpleCircleConstraint() {
        val root = ConstraintWidgetContainer(0, 0, 600, 800)
        root.measurer = sMeasurer
        val a = ConstraintWidget(100, 30)
        val b = ConstraintWidget(100, 30)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        root.add(a)
        root.add(b)
        a.setHorizontalDimensionBehaviour(DimensionBehaviour.FIXED)
        a.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
        b.setHorizontalDimensionBehaviour(DimensionBehaviour.FIXED)
        b.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 8)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 8)
        b.connectCircularConstraint(a, 30f, 50)
        root.optimizationLevel = Optimizer.OPTIMIZATION_GRAPH
        root.measure(
            Optimizer.OPTIMIZATION_GRAPH,
            BasicMeasure.EXACTLY,
            600,
            BasicMeasure.EXACTLY,
            800,
            0,
            0,
            0,
            0,
        )
        //        root.layout();
        println("1) A: $a")
        println("2) B: $b")
    }

    fun applyChain(widgets: ArrayList<ConstraintWidget>, direction: Int) {
        var previous: ConstraintWidget = widgets.get(0)
        for (i in 1 until widgets.size) {
            val widget: ConstraintWidget = widgets.get(i)
            if (direction == 0) { // horizontal
                widget.connect(ConstraintAnchor.Type.LEFT, previous, ConstraintAnchor.Type.RIGHT)
                previous.connect(ConstraintAnchor.Type.RIGHT, widget, ConstraintAnchor.Type.LEFT)
            } else {
                widget.connect(ConstraintAnchor.Type.TOP, previous, ConstraintAnchor.Type.BOTTOM)
                previous.connect(ConstraintAnchor.Type.BOTTOM, widget, ConstraintAnchor.Type.TOP)
            }
            previous = widget
        }
    }

    fun applyChain(direction: Int, vararg widgets: ConstraintWidget) {
        var previous: ConstraintWidget = widgets[0]
        for (i in 1 until widgets.size) {
            val widget: ConstraintWidget = widgets[i]
            if (direction == ConstraintWidget.HORIZONTAL) {
                widget.connect(ConstraintAnchor.Type.LEFT, previous, ConstraintAnchor.Type.RIGHT)
                previous.connect(ConstraintAnchor.Type.RIGHT, widget, ConstraintAnchor.Type.LEFT)
            } else {
                widget.connect(ConstraintAnchor.Type.TOP, previous, ConstraintAnchor.Type.BOTTOM)
                previous.connect(ConstraintAnchor.Type.BOTTOM, widget, ConstraintAnchor.Type.TOP)
            }
            previous = widget
        }
    }

    @Test
    fun testRatioChainConstraint() {
        val root = ConstraintWidgetContainer(0, 0, 600, 800)
        root.measurer = sMeasurer
        val a = ConstraintWidget(100, 30)
        val b = ConstraintWidget(0, 30)
        val c = ConstraintWidget(0, 30)
        val d = ConstraintWidget(100, 30)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(d)
        a.setHorizontalDimensionBehaviour(DimensionBehaviour.FIXED)
        b.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        c.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        d.setHorizontalDimensionBehaviour(DimensionBehaviour.FIXED)
        b.setDimensionRatio("w,1:1")
        a.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
        b.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
        c.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
        d.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
        applyChain(ConstraintWidget.HORIZONTAL, a, b, c, d)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        d.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        root.optimizationLevel = Optimizer.OPTIMIZATION_GRAPH
        root.measure(
            Optimizer.OPTIMIZATION_GRAPH,
            BasicMeasure.EXACTLY,
            600,
            BasicMeasure.EXACTLY,
            800,
            0,
            0,
            0,
            0,
        )
        //        root.layout();
        println("1) A: $a")
        println("2) B: $b")
        println("3) C: $c")
        println("4) D: $d")
    }

    @Test
    fun testCycleConstraints() {
        val root = ConstraintWidgetContainer(0, 0, 600, 800)
        root.measurer = sMeasurer
        val a = ConstraintWidget(100, 30)
        val b = ConstraintWidget(40, 20)
        val c = ConstraintWidget(40, 20)
        val d = ConstraintWidget(30, 30)
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
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.LEFT, c, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.BOTTOM)
        c.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        c.connect(ConstraintAnchor.Type.LEFT, d, ConstraintAnchor.Type.RIGHT)
        d.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.TOP)
        d.connect(ConstraintAnchor.Type.BOTTOM, c, ConstraintAnchor.Type.BOTTOM)
        d.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        root.setVerticalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        root.measure(
            Optimizer.OPTIMIZATION_NONE,
            BasicMeasure.EXACTLY,
            600,
            BasicMeasure.EXACTLY,
            800,
            0,
            0,
            0,
            0,
        )
        println("1) A: $a")
        println("2) B: $b")
        println("3) C: $c")
        println("4) D: $d")
        assertEquals(a.top, 0)
        assertEquals(b.top, 30)
        assertEquals(c.top, 50)
        assertEquals(d.top, 35)
    }

    @Test
    fun testGoneChain() {
        val root = ConstraintWidgetContainer(0, 0, 600, 800)
        root.measurer = sMeasurer
        val a = ConstraintWidget(100, 30)
        val b = ConstraintWidget(100, 30)
        val c = ConstraintWidget(100, 30)
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
        b.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        a.visibility = ConstraintWidget.GONE
        c.visibility = ConstraintWidget.GONE
        root.measure(
            Optimizer.OPTIMIZATION_NONE,
            BasicMeasure.EXACTLY,
            600,
            BasicMeasure.EXACTLY,
            800,
            0,
            0,
            0,
            0,
        )
        println("0) root: $root")
        println("1) A: $a")
        println("2) B: $b")
        println("3) C: $c")
        assertEquals(b.width, root.width)
    }

    @Test
    fun testGoneChainWithCenterWidget() {
        val root = ConstraintWidgetContainer(0, 0, 600, 800)
        root.measurer = sMeasurer
        val a = ConstraintWidget(100, 30)
        val b = ConstraintWidget(100, 30)
        val c = ConstraintWidget(100, 30)
        val d = ConstraintWidget(100, 30)
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
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        b.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        a.visibility = ConstraintWidget.GONE
        c.visibility = ConstraintWidget.GONE
        d.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.LEFT)
        d.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.RIGHT)
        d.visibility = ConstraintWidget.GONE
        root.measure(
            Optimizer.OPTIMIZATION_NONE,
            BasicMeasure.EXACTLY,
            600,
            BasicMeasure.EXACTLY,
            800,
            0,
            0,
            0,
            0,
        )
        println("0) root: $root")
        println("1) A: $a")
        println("2) B: $b")
        println("3) C: $c")
        println("4) D: $d")
        assertEquals(b.width, root.width)
    }

    @Test
    fun testBarrier() {
        val root = ConstraintWidgetContainer(0, 0, 600, 800)
        root.measurer = sMeasurer
        val a = ConstraintWidget(100, 30)
        val b = ConstraintWidget(100, 30)
        val c = ConstraintWidget(100, 30)
        val d = ConstraintWidget(100, 30)
        val barrier1 = Barrier()
        // Barrier barrier2 = new Barrier();
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        d.debugName = "D"
        barrier1.debugName = "barrier1"
        // barrier2.setDebugName("barrier2");
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(d)
        root.add(barrier1)
        // root.add(barrier2);
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        barrier1.add(a)
        barrier1.setBarrierType(Barrier.BOTTOM)
        b.connect(ConstraintAnchor.Type.TOP, barrier1, ConstraintAnchor.Type.BOTTOM)
        // barrier2.add(B);
        // barrier2.setBarrierType(Barrier.TOP);
        c.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.BOTTOM)
        d.connect(ConstraintAnchor.Type.TOP, c, ConstraintAnchor.Type.BOTTOM)
        d.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        root.measure(
            Optimizer.OPTIMIZATION_NONE,
            BasicMeasure.EXACTLY,
            600,
            BasicMeasure.EXACTLY,
            800,
            0,
            0,
            0,
            0,
        )
        println("0) root: $root")
        println("1) A: $a")
        println("2) barrier1: $barrier1")
        println("3) B: $b")
        // System.out.println("4) barrier2: " + barrier2);
        println("5) C: $c")
        println("6) D: $d")
        assertEquals(a.top, 0)
        assertEquals(b.top, a.bottom)
        assertEquals(barrier1.top, a.bottom)
        assertEquals(c.top, b.bottom)
        assertEquals(d.top, 430)
        //        assertEquals(barrier2.getTop(), B.getTop());
    }

    @Test
    fun testDirectCentering() {
        val root = ConstraintWidgetContainer(0, 0, 192, 168)
        root.measurer = sMeasurer
        val a = ConstraintWidget(43, 43)
        val b = ConstraintWidget(59, 59)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        root.add(b)
        root.add(a)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.RIGHT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.BOTTOM, a, ConstraintAnchor.Type.BOTTOM)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        root.optimizationLevel = Optimizer.OPTIMIZATION_NONE
        root.measure(
            Optimizer.OPTIMIZATION_NONE,
            BasicMeasure.EXACTLY,
            100,
            BasicMeasure.EXACTLY,
            100,
            0,
            0,
            0,
            0,
        )
        println("0) root: $root")
        println("1) A: $a")
        println("2) B: $b")
        assertEquals(a.top, 63)
        assertEquals(a.left, 75)
        assertEquals(b.top, 55)
        assertEquals(b.left, 67)
        root.optimizationLevel = Optimizer.OPTIMIZATION_STANDARD
        root.measure(
            Optimizer.OPTIMIZATION_STANDARD,
            BasicMeasure.EXACTLY,
            100,
            BasicMeasure.EXACTLY,
            100,
            0,
            0,
            0,
            0,
        )
        println("0) root: $root")
        println("1) A: $a")
        println("2) B: $b")
        assertEquals(63, a.top)
        assertEquals(75, a.left)
        assertEquals(55, b.top)
        assertEquals(67, b.left)
    }

    companion object {
        var sMeasurer: BasicMeasure.Measurer = object : BasicMeasure.Measurer {
            override fun measure(widget: ConstraintWidget, measure: BasicMeasure.Measure) {
                val horizontalBehavior: DimensionBehaviour = measure.horizontalBehavior
                val verticalBehavior: DimensionBehaviour = measure.verticalBehavior
                val horizontalDimension = measure.horizontalDimension
                val verticalDimension = measure.verticalDimension
                if (horizontalBehavior === DimensionBehaviour.FIXED) {
                    measure.measuredWidth = horizontalDimension
                } else if (horizontalBehavior === DimensionBehaviour.MATCH_CONSTRAINT) {
                    measure.measuredWidth = horizontalDimension
                }
                if (verticalBehavior === DimensionBehaviour.FIXED) {
                    measure.measuredHeight = verticalDimension
                }
                widget.isMeasureRequested = false
            }

            override fun didMeasures() {}
        }
    }
}
