/*
 * Copyright (C) 2015 The Android Open Source Project
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

import androidx.constraintlayout.coremp.platform.System
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

class OptimizationsTest {
    @Test
    fun testGoneMatchConstraint() {
        val root = ConstraintWidgetContainer(0, 0, 600, 800)
        val a = ConstraintWidget("A", 0, 10)
        val b = ConstraintWidget("B", 10, 10)
        root.debugName = "root"
        root.add(a)
        root.add(b)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 8)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 8)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 8)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 8)
        a.verticalBiasPercent = 0.2f
        a.horizontalBiasPercent = 0.2f
        a.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        val metrics = Metrics()
        root.fillMetrics(metrics)
        root.optimizationLevel = Optimizer.OPTIMIZATION_STANDARD
        root.layout()
        println("1) A: $a")
        assertEquals(a.left, 8)
        assertEquals(a.top, 163)
        assertEquals(a.right, 592)
        assertEquals(a.bottom, 173)
        a.visibility = ConstraintWidget.GONE
        root.layout()
        println("2) A: $a")
        assertEquals(a.left, 120)
        assertEquals(a.top, 160)
        assertEquals(a.right, 120)
        assertEquals(a.bottom, 160)
    }

    @Test
    fun test3EltsChain() {
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
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        c.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 40)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 30)
        val metrics = Metrics()
        root.fillMetrics(metrics)
        root.optimizationLevel = Optimizer.OPTIMIZATION_STANDARD
        //        root.setOptimizationLevel(Optimizer.OPTIMIZATION_NONE);
        a.setHorizontalChainStyle(ConstraintWidget.CHAIN_SPREAD_INSIDE)
        root.layout()
        println("1) root: $root A: $a B: $b C: $c")
        println(metrics)
        assertEquals(a.left, 40)
        assertEquals(b.left, 255)
        assertEquals(c.left, 470)
        a.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        b.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        c.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        root.layout()
        println("2) root: $root A: $a B: $b C: $c")
        println(metrics)
        assertEquals(a.left, 40)
        assertEquals(b.left, 217, 1)
        assertEquals(c.left, 393)
        assertEquals(a.width, 177, 1)
        assertEquals(b.width, 176, 1)
        assertEquals(c.width, 177, 1)
        a.setHorizontalChainStyle(ConstraintWidget.CHAIN_SPREAD)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT, 7)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT, 3)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT, 7)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT, 3)
        root.layout()
        println("3) root: $root A: $a B: $b C: $c")
        println(metrics)
        assertEquals(a.left, 40)
        assertEquals(b.left, 220)
        assertEquals(c.left, 400, 1)
        assertEquals(a.width, 170, 1)
        assertEquals(b.width, 170, 1)
        assertEquals(c.width, 170, 1)
        a.setHorizontalChainStyle(ConstraintWidget.CHAIN_SPREAD_INSIDE)
        a.visibility = ConstraintWidget.GONE
        root.layout()
        println("4) root: $root A: $a B: $b C: $c")
        println(metrics)
        assertEquals(a.left, 0)
        assertEquals(b.left, 3)
        assertEquals(c.left, 292, 1)
        assertEquals(a.width, 0)
        assertEquals(b.width, 279, 1)
        assertEquals(c.width, 278, 1)
    }

    @Test
    fun testBasicChain() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        root.add(a)
        root.add(b)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        val metrics = Metrics()
        root.fillMetrics(metrics)
        root.optimizationLevel = Optimizer.OPTIMIZATION_STANDARD
        root.layout()
        println("1) root: $root A: $a B: $b")
        println(metrics)
        assertEquals(a.left, 133)
        assertEquals(b.left, 367, 1)
        val c = ConstraintWidget(100, 20)
        c.debugName = "C"
        root.add(c)
        c.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT)
        root.layout()
        println("2) root: $root A: $a B: $b")
        println(metrics)
        assertEquals(a.left, 133)
        assertEquals(b.left, 367, 1)
        assertEquals(c.left, b.right)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 40)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT, 100)
        a.setHorizontalChainStyle(ConstraintWidget.CHAIN_PACKED)
        root.layout()
        println("3) root: $root A: $a B: $b")
        println(metrics)
        assertEquals(a.left, 170)
        assertEquals(b.left, 370)
        a.horizontalBiasPercent = 0f
        root.layout()
        println("4) root: $root A: $a B: $b")
        println(metrics)
        assertEquals(a.left, 40)
        assertEquals(b.left, 240)
        a.horizontalBiasPercent = 0.5f
        a.visibility = ConstraintWidget.GONE
        //        root.setOptimizationLevel(Optimizer.OPTIMIZATION_NONE);
        root.layout()
        println("5) root: $root A: $a B: $b")
        println(metrics)
        assertEquals(a.left, 250)
        assertEquals(b.left, 250)
    }

    @Test
    fun testBasicChain2() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        root.add(a)
        root.add(b)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        val c = ConstraintWidget(100, 20)
        c.debugName = "C"
        root.add(c)
        c.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 40)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT, 100)
        a.setHorizontalChainStyle(ConstraintWidget.CHAIN_PACKED)
        a.horizontalBiasPercent = 0.5f
        a.visibility = ConstraintWidget.GONE
        //        root.setOptimizationLevel(Optimizer.OPTIMIZATION_NONE);
        root.layout()
        println("5) root: $root A: $a B: $b")
        assertEquals(a.left, 250)
        assertEquals(b.left, 250)
    }

    @Test
    fun testBasicRatio() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        root.add(a)
        root.add(b)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.BOTTOM, a, ConstraintAnchor.Type.BOTTOM)
        a.setVerticalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        a.setDimensionRatio("1:1")
        val metrics = Metrics()
        root.fillMetrics(metrics)
        root.optimizationLevel = Optimizer.OPTIMIZATION_STANDARD
        root.layout()
        println("1) root: $root A: $a B: $b")
        println(metrics)
        assertEquals(a.height, a.width)
        assertEquals(b.top, (a.height - b.height) / 2)
    }

    @Test
    fun testBasicBaseline() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        a.baselineDistance = 8
        b.baselineDistance = 8
        root.add(a)
        root.add(b)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.BASELINE, a, ConstraintAnchor.Type.BASELINE)
        val metrics = Metrics()
        root.fillMetrics(metrics)
        root.optimizationLevel = Optimizer.OPTIMIZATION_STANDARD
        root.layout()
        println("1) root: $root A: $a B: $b")
        println(metrics)
        assertEquals(a.top, 290)
        assertEquals(b.top, a.top)
    }

    @Test
    fun testBasicMatchConstraints() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        val a = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        root.add(a)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        val metrics = Metrics()
        root.fillMetrics(metrics)
        root.optimizationLevel = Optimizer.OPTIMIZATION_STANDARD
        root.layout()
        println("1) root: $root A: $a")
        println(metrics)
        assertEquals(a.left, 0)
        assertEquals(a.top, 0)
        assertEquals(a.right, root.width)
        assertEquals(a.bottom, root.height)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 10)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 20)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 30)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 40)
        root.layout()
        println("2) root: $root A: $a")
        println(metrics)
        assertEquals(a.left, 30)
        assertEquals(a.top, 10)
        assertEquals(a.right, root.width - 40)
        assertEquals(a.bottom, root.height - 20)
    }

    @Test
    fun testBasicCenteringPositioning() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        val a = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        root.add(a)
        var time: Long = System.nanoTime()
        val metrics = Metrics()
        root.fillMetrics(metrics)
        root.optimizationLevel = Optimizer.OPTIMIZATION_STANDARD
        root.layout()
        time = System.nanoTime() - time
        println("A) execution time: $time")
        println("1) root: $root A: $a")
        println(metrics)
        assertEquals(a.left, (root.width - a.width) / 2)
        assertEquals(a.top, (root.height - a.height) / 2)
        a.horizontalBiasPercent = 0.3f
        a.verticalBiasPercent = 0.3f
        root.layout()
        println("2) root: $root A: $a")
        println(metrics)
        assertEquals(a.left, ((root.width - a.width) * 0.3f).toInt())
        assertEquals(a.top, ((root.height - a.height) * 0.3f).toInt())
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 10)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 30)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 50)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 20)
        root.layout()
        println("3) root: $root A: $a")
        println(metrics)
        assertEquals(a.left, ((root.width - a.width - 40) * 0.3f).toInt() + 10)
        assertEquals(a.top, ((root.height - a.height - 70) * 0.3f).toInt() + 50)
    }

    @Test
    fun testBasicVerticalPositioning() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        val margin = 13
        val marginR = 27
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 31)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 27)
        b.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 27)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM, 104)
        root.add(a)
        root.add(b)
        root.optimizationLevel = Optimizer.OPTIMIZATION_STANDARD
        val time: Long = System.nanoTime()
        //        root.layout();
//        time = System.nanoTime() - time;
//        System.out.println("A) execution time: " + time);
//        System.out.println("a - root: " + root + " A: " + A + " B: " + B);
//
//        assertEquals(A.getLeft(), 27);
//        assertEquals(A.getTop(), 31);
//        assertEquals(B.getLeft(), 27);
//        assertEquals(B.getTop(), 155);
        a.visibility = ConstraintWidget.GONE
        val metrics = Metrics()
        root.fillMetrics(metrics)
        root.layout()
        println("b - root: $root A: $a B: $b")
        println(metrics)
        assertEquals(a.left, 0)
        assertEquals(a.top, 0)
        assertEquals(b.left, 27)
        assertEquals(b.top, 104)
        // root: id: root (0, 0) - (600 x 600) wrap: (0 x 0) A: id: A (27, 31) - (100 x 20)
        // wrap: (0 x 0) B: id: B (27, 155) - (100 x 20) wrap: (0 x 0)
    }

    @Test
    fun testBasicVerticalGuidelinePositioning() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        val a = ConstraintWidget(100, 20)
        val guidelineA = Guideline()
        guidelineA.setOrientation(Guideline.HORIZONTAL)
        guidelineA.setGuideEnd(67)
        root.debugName = "root"
        a.debugName = "A"
        guidelineA.debugName = "guideline"
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 31)
        a.connect(ConstraintAnchor.Type.BOTTOM, guidelineA, ConstraintAnchor.Type.TOP, 12)
        root.add(a)
        root.add(guidelineA)
        root.optimizationLevel = Optimizer.OPTIMIZATION_STANDARD
        var time: Long = System.nanoTime()
        root.layout()
        time = System.nanoTime() - time
        println("A) execution time: $time")
        println("root: $root A: $a guide: $guidelineA")
        assertEquals(a.top, 266)
        assertEquals(guidelineA.top, 533)
    }

    @Test
    fun testSimpleCenterPositioning() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        val a = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        val margin = 13
        val marginR = 27
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, margin)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, -margin)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, margin)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, -marginR)
        root.add(a)
        root.optimizationLevel = Optimizer.OPTIMIZATION_STANDARD
        var time: Long = System.nanoTime()
        root.layout()
        time = System.nanoTime() - time
        println("A) execution time: $time")
        println("root: $root A: $a")
        assertEquals(a.left, 270, 1)
        assertEquals(a.top, 303, 1)
    }

    @Test
    fun testSimpleGuideline() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        val guidelineA = Guideline()
        val a = ConstraintWidget(100, 20)
        guidelineA.setOrientation(Guideline.VERTICAL)
        guidelineA.setGuideBegin(100)
        root.debugName = "root"
        a.debugName = "A"
        guidelineA.debugName = "guidelineA"
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 32)
        a.connect(ConstraintAnchor.Type.LEFT, guidelineA, ConstraintAnchor.Type.LEFT, 2)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 7)
        a.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        root.add(guidelineA)
        root.add(a)
        root.optimizationLevel = Optimizer.OPTIMIZATION_STANDARD
        val metrics = Metrics()
        root.fillMetrics(metrics)
        var time: Long = System.nanoTime()
        root.layout()
        assertEquals(a.left, 102)
        assertEquals(a.top, 32)
        assertEquals(a.width, 491)
        assertEquals(a.height, 20)
        assertEquals(guidelineA.left, 100)
        time = System.nanoTime() - time
        println("A) execution time: $time")
        println("root: $root A: $a guideline: $guidelineA")
        println(metrics)
        root.width = 700
        time = System.nanoTime()
        root.layout()
        time = System.nanoTime() - time
        println("B) execution time: $time")
        println("root: $root A: $a guideline: $guidelineA")
        println(metrics)
        assertEquals(a.left, 102)
        assertEquals(a.top, 32)
        assertEquals(a.width, 591)
        assertEquals(a.height, 20)
        assertEquals(guidelineA.left, 100)
    }

    @Test
    fun testSimple() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 10)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 20)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT, 10)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM, 20)
        c.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT, 30)
        c.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.BOTTOM, 20)
        root.add(a)
        root.add(b)
        root.add(c)
        root.optimizationLevel = Optimizer.OPTIMIZATION_STANDARD
        var time: Long = System.nanoTime()
        root.layout()
        time = System.nanoTime() - time
        println("execution time: $time")
        println("root: $root A: $a B: $b C: $c")
        assertEquals(a.left, 10)
        assertEquals(a.top, 20)
        assertEquals(b.left, 120)
        assertEquals(b.top, 60)
        assertEquals(c.left, 140)
        assertEquals(c.top, 100)
    }

    @Test
    fun testGuideline() {
        testVerticalGuideline(Optimizer.OPTIMIZATION_NONE)
        testVerticalGuideline(Optimizer.OPTIMIZATION_STANDARD)
        testHorizontalGuideline(Optimizer.OPTIMIZATION_NONE)
        testHorizontalGuideline(Optimizer.OPTIMIZATION_STANDARD)
    }

    fun testVerticalGuideline(directResolution: Int) {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        root.optimizationLevel = directResolution
        val a = ConstraintWidget(100, 20)
        val guideline = Guideline()
        guideline.setOrientation(Guideline.VERTICAL)
        root.debugName = "root"
        a.debugName = "A"
        guideline.debugName = "guideline"
        root.add(a)
        root.add(guideline)
        a.connect(ConstraintAnchor.Type.LEFT, guideline, ConstraintAnchor.Type.LEFT, 16)
        guideline.setGuideBegin(100)
        root.layout()
        println(
            "res: " + directResolution + " root: " + root +
                " A: " + a + " guideline: " + guideline,
        )
        assertEquals(guideline.left, 100)
        assertEquals(a.left, 116)
        assertEquals(a.width, 100)
        assertEquals(a.height, 20)
        assertEquals(a.top, 0)
        guideline.setGuidePercent(0.5f)
        root.layout()
        println(
            "res: " + directResolution + " root: " + root +
                " A: " + a + " guideline: " + guideline,
        )
        assertEquals(guideline.left, root.width / 2)
        assertEquals(a.left, 316)
        assertEquals(a.width, 100)
        assertEquals(a.height, 20)
        assertEquals(a.top, 0)
        guideline.setGuideEnd(100)
        root.layout()
        println(
            "res: " + directResolution + " root: " + root +
                " A: " + a + " guideline: " + guideline,
        )
        assertEquals(guideline.left, 500)
        assertEquals(a.left, 516)
        assertEquals(a.width, 100)
        assertEquals(a.height, 20)
        assertEquals(a.top, 0)
    }

    fun testHorizontalGuideline(directResolution: Int) {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        root.optimizationLevel = directResolution
        val a = ConstraintWidget(100, 20)
        val guideline = Guideline()
        guideline.setOrientation(Guideline.HORIZONTAL)
        root.debugName = "root"
        a.debugName = "A"
        guideline.debugName = "guideline"
        root.add(a)
        root.add(guideline)
        a.connect(ConstraintAnchor.Type.TOP, guideline, ConstraintAnchor.Type.TOP, 16)
        guideline.setGuideBegin(100)
        root.layout()
        println(
            "res: " + directResolution + " root: " + root +
                " A: " + a + " guideline: " + guideline,
        )
        assertEquals(guideline.top, 100)
        assertEquals(a.top, 116)
        assertEquals(a.width, 100)
        assertEquals(a.height, 20)
        assertEquals(a.left, 0)
        guideline.setGuidePercent(0.5f)
        root.layout()
        println(
            "res: " + directResolution + " root: " + root +
                " A: " + a + " guideline: " + guideline,
        )
        assertEquals(guideline.top, root.height / 2)
        assertEquals(a.top, 316)
        assertEquals(a.width, 100)
        assertEquals(a.height, 20)
        assertEquals(a.left, 0)
        guideline.setGuideEnd(100)
        root.layout()
        println(
            "res: " + directResolution + " root: " + root +
                " A: " + a + " guideline: " + guideline,
        )
        assertEquals(guideline.top, 500)
        assertEquals(a.top, 516)
        assertEquals(a.width, 100)
        assertEquals(a.height, 20)
        assertEquals(a.left, 0)
    }

    @Test
    fun testBasicCentering() {
        testBasicCentering(Optimizer.OPTIMIZATION_NONE)
        testBasicCentering(Optimizer.OPTIMIZATION_STANDARD)
    }

    fun testBasicCentering(directResolution: Int) {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        root.optimizationLevel = directResolution
        val a = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        root.add(a)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 10)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 10)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 10)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 10)
        root.layout()
        println("res: $directResolution root: $root A: $a")
        assertEquals(a.left, 250)
        assertEquals(a.top, 290)
    }

    @Test
    fun testPercent() {
        testPercent(Optimizer.OPTIMIZATION_NONE)
        testPercent(Optimizer.OPTIMIZATION_STANDARD)
    }

    fun testPercent(directResolution: Int) {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        root.optimizationLevel = directResolution
        val a = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        root.add(a)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 10)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 10)
        a.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        a.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_PERCENT, 0, 0, 0.5f)
        a.setVerticalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_PERCENT, 0, 0, 0.5f)
        root.layout()
        println("res: $directResolution root: $root A: $a")
        assertEquals(a.left, 10)
        assertEquals(a.top, 10)
        assertEquals(a.width, 300)
        assertEquals(a.height, 300)
    }

    @Test
    fun testDependency() {
        testDependency(Optimizer.OPTIMIZATION_NONE)
        testDependency(Optimizer.OPTIMIZATION_STANDARD)
    }

    fun testDependency(directResolution: Int) {
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
        a.baselineDistance = 8
        b.baselineDistance = 8
        c.baselineDistance = 8
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 10)
        a.connect(ConstraintAnchor.Type.BASELINE, b, ConstraintAnchor.Type.BASELINE)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT, 16)
        b.connect(ConstraintAnchor.Type.BASELINE, c, ConstraintAnchor.Type.BASELINE)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT, 48)
        c.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 32)
        root.layout()
        println(
            "res: " + directResolution + " root: " + root +
                " A: " + a + " B: " + b + " C: " + c,
        )
        assertEquals(a.left, 10)
        assertEquals(a.top, 32)
        assertEquals(b.left, 126)
        assertEquals(b.top, 32)
        assertEquals(c.left, 274)
        assertEquals(c.top, 32)
    }

    @Test
    fun testDependency2() {
        testDependency2(Optimizer.OPTIMIZATION_NONE)
        testDependency2(Optimizer.OPTIMIZATION_STANDARD)
    }

    fun testDependency2(directResolution: Int) {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        root.optimizationLevel = directResolution
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        root.add(a)
        root.add(b)
        root.add(c)
        a.baselineDistance = 8
        b.baselineDistance = 8
        c.baselineDistance = 8
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        a.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.BOTTOM, a, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.LEFT, c, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.TOP)
        c.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 12)
        root.layout()
        println(
            "res: " + directResolution + " root: " + root +
                " A: " + a + " B: " + b + " C: " + c,
        )
        assertEquals(a.left, 12)
        assertEquals(a.top, 580)
        assertEquals(b.left, 12)
        assertEquals(b.top, 560)
        assertEquals(c.left, 12)
        assertEquals(c.top, 540)
    }

    @Test
    fun testDependency3() {
        testDependency3(Optimizer.OPTIMIZATION_NONE)
        testDependency3(Optimizer.OPTIMIZATION_STANDARD)
    }

    fun testDependency3(directResolution: Int) {
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
        a.baselineDistance = 8
        b.baselineDistance = 8
        c.baselineDistance = 8
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 10)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 20)
        b.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 30)
        b.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 60)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 10)
        c.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.TOP)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT, 20)
        root.layout()
        println(
            "res: " + directResolution + " root: " + root +
                " A: " + a + " B: " + b + " C: " + c,
        )
        assertEquals(a.left, 10)
        assertEquals(a.top, 20)
        assertEquals(b.left, 260)
        assertEquals(b.top, 520)
        assertEquals(c.left, 380)
        assertEquals(c.top, 500)
    }

    @Test
    fun testDependency4() {
        testDependency4(Optimizer.OPTIMIZATION_NONE)
        testDependency4(Optimizer.OPTIMIZATION_STANDARD)
    }

    fun testDependency4(directResolution: Int) {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        root.optimizationLevel = directResolution
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        root.add(a)
        root.add(b)
        a.baselineDistance = 8
        b.baselineDistance = 8
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 10)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 20)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 10)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 20)
        b.connect(ConstraintAnchor.Type.RIGHT, a, ConstraintAnchor.Type.RIGHT, 30)
        b.connect(ConstraintAnchor.Type.BOTTOM, a, ConstraintAnchor.Type.BOTTOM, 60)
        root.layout()
        println(
            "res: " + directResolution + " root: " + root +
                " A: " + a + " B: " + b,
        )
        assertEquals(a.left, 250)
        assertEquals(a.top, 290)
        assertEquals(b.left, 220)
        assertEquals(b.top, 230)
    }

    @Test
    fun testDependency5() {
        testDependency5(Optimizer.OPTIMIZATION_NONE)
        testDependency5(Optimizer.OPTIMIZATION_STANDARD)
    }

    fun testDependency5(directResolution: Int) {
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
        a.baselineDistance = 8
        b.baselineDistance = 8
        c.baselineDistance = 8
        d.baselineDistance = 8
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 10)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 10)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 20)
        a.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 10)
        c.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.BOTTOM)
        c.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.RIGHT, 20)
        d.connect(ConstraintAnchor.Type.TOP, c, ConstraintAnchor.Type.BOTTOM)
        d.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.RIGHT, 20)
        root.layout()
        println(
            "res: " + directResolution + " root: " + root +
                " A: " + a + " B: " + b + " C: " + c + " D: " + d,
        )
        assertEquals(a.left, 250)
        assertEquals(a.top, 197)
        assertEquals(b.left, 250)
        assertEquals(b.top, 393)
        assertEquals(c.left, 230)
        assertEquals(c.top, 413)
        assertEquals(d.left, 210)
        assertEquals(d.top, 433)
    }

    @Test
    fun testUnconstrainedDependency() {
        testUnconstrainedDependency(Optimizer.OPTIMIZATION_NONE)
        testUnconstrainedDependency(Optimizer.OPTIMIZATION_STANDARD)
    }

    fun testUnconstrainedDependency(directResolution: Int) {
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
        a.baselineDistance = 8
        b.baselineDistance = 8
        c.baselineDistance = 8
        a.setFrame(142, 96, 242, 130)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT, 10)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.TOP, 100)
        c.connect(ConstraintAnchor.Type.RIGHT, a, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.BASELINE, a, ConstraintAnchor.Type.BASELINE)
        root.layout()
        println(
            "res: " + directResolution + " root: " + root +
                " A: " + a + " B: " + b + " C: " + c,
        )
        assertEquals(a.left, 142)
        assertEquals(a.top, 96)
        assertEquals(a.width, 100)
        assertEquals(a.height, 34)
        assertEquals(b.left, 252)
        assertEquals(b.top, 196)
        assertEquals(c.left, 42)
        assertEquals(c.top, 96)
    }

    @Test
    fun testFullLayout() {
        testFullLayout(Optimizer.OPTIMIZATION_NONE)
        testFullLayout(Optimizer.OPTIMIZATION_STANDARD)
    }

    fun testFullLayout(directResolution: Int) {
        // Horizontal :
        // r <- A
        // r <- B <- C <- D
        //      B <- E
        // r <- F
        // r <- G
        // Vertical:
        // r <- A <- B <- C <- D <- E
        // r <- F <- G
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        root.optimizationLevel = directResolution
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        val d = ConstraintWidget(100, 20)
        val e = ConstraintWidget(100, 20)
        val f = ConstraintWidget(100, 20)
        val g = ConstraintWidget(100, 20)
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        d.debugName = "D"
        e.debugName = "E"
        f.debugName = "F"
        g.debugName = "G"
        root.add(g)
        root.add(a)
        root.add(b)
        root.add(e)
        root.add(c)
        root.add(d)
        root.add(f)
        a.baselineDistance = 8
        b.baselineDistance = 8
        c.baselineDistance = 8
        d.baselineDistance = 8
        e.baselineDistance = 8
        f.baselineDistance = 8
        g.baselineDistance = 8
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 20)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM, 40)
        b.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 16)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT, 16)
        c.connect(ConstraintAnchor.Type.BASELINE, b, ConstraintAnchor.Type.BASELINE)
        d.connect(ConstraintAnchor.Type.TOP, c, ConstraintAnchor.Type.BOTTOM)
        d.connect(ConstraintAnchor.Type.LEFT, c, ConstraintAnchor.Type.LEFT)
        e.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.RIGHT)
        e.connect(ConstraintAnchor.Type.BASELINE, d, ConstraintAnchor.Type.BASELINE)
        f.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        f.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        g.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 16)
        g.connect(ConstraintAnchor.Type.BASELINE, f, ConstraintAnchor.Type.BASELINE)
        root.layout()
        println(
            " direct: " + directResolution + " -> A: " + a + " B: " + b +
                " C: " + c + " D: " + d + " E: " + e + " F: " + f + " G: " + g,
        )
        assertEquals(a.left, 250)
        assertEquals(a.top, 20)
        assertEquals(b.left, 16)
        assertEquals(b.top, 80)
        assertEquals(c.left, 132)
        assertEquals(c.top, 80)
        assertEquals(d.left, 132)
        assertEquals(d.top, 100)
        assertEquals(e.left, 16)
        assertEquals(e.top, 100)
        assertEquals(f.left, 500)
        assertEquals(f.top, 580)
        assertEquals(g.left, 16)
        assertEquals(g.top, 580)
    }

    @Test
    fun testComplexLayout() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        root.optimizationLevel = Optimizer.OPTIMIZATION_GROUPING
        val a = ConstraintWidget(100, 100)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        val d = ConstraintWidget(30, 30)
        val e = ConstraintWidget(30, 30)
        val f = ConstraintWidget(30, 30)
        val g = ConstraintWidget(100, 20)
        val h = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        d.debugName = "D"
        e.debugName = "E"
        f.debugName = "F"
        g.debugName = "G"
        h.debugName = "H"
        root.add(g)
        root.add(a)
        root.add(b)
        root.add(e)
        root.add(c)
        root.add(d)
        root.add(f)
        root.add(h)
        b.baselineDistance = 8
        c.baselineDistance = 8
        d.baselineDistance = 8
        e.baselineDistance = 8
        f.baselineDistance = 8
        g.baselineDistance = 8
        h.baselineDistance = 8
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 16)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 16)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 16)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT, 16)
        c.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        c.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT, 16)
        c.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        d.connect(ConstraintAnchor.Type.BOTTOM, a, ConstraintAnchor.Type.BOTTOM)
        d.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT, 16)
        e.connect(ConstraintAnchor.Type.BOTTOM, d, ConstraintAnchor.Type.BOTTOM)
        e.connect(ConstraintAnchor.Type.LEFT, d, ConstraintAnchor.Type.RIGHT, 16)
        f.connect(ConstraintAnchor.Type.BOTTOM, e, ConstraintAnchor.Type.BOTTOM)
        f.connect(ConstraintAnchor.Type.LEFT, e, ConstraintAnchor.Type.RIGHT, 16)
        g.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        g.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 16)
        g.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        h.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 16)
        h.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 16)
        root.measurer = sMeasurer
        root.layout()
        println(
            " direct: -> A: " + a + " B: " + b + " C: " + c +
                " D: " + d + " E: " + e + " F: " + f + " G: " + g + " H: " + h,
        )
        assertEquals(a.left, 16)
        assertEquals(a.top, 250)
        assertEquals(b.left, 132)
        assertEquals(b.top, 250)
        assertEquals(c.left, 132)
        assertEquals(c.top, 290)
        assertEquals(d.left, 132)
        assertEquals(d.top, 320)
        assertEquals(e.left, 178)
        assertEquals(e.top, 320)
        assertEquals(f.left, 224)
        assertEquals(f.top, 320)
        assertEquals(g.left, 484)
        assertEquals(g.top, 290)
        assertEquals(h.left, 484)
        assertEquals(h.top, 564)
    }

    @Test
    fun testComplexLayoutWrap() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        root.optimizationLevel = Optimizer.OPTIMIZATION_DIRECT
        val a = ConstraintWidget(100, 100)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        val d = ConstraintWidget(30, 30)
        val e = ConstraintWidget(30, 30)
        val f = ConstraintWidget(30, 30)
        val g = ConstraintWidget(100, 20)
        val h = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        d.debugName = "D"
        e.debugName = "E"
        f.debugName = "F"
        g.debugName = "G"
        h.debugName = "H"
        root.add(g)
        root.add(a)
        root.add(b)
        root.add(e)
        root.add(c)
        root.add(d)
        root.add(f)
        root.add(h)
        b.baselineDistance = 8
        c.baselineDistance = 8
        d.baselineDistance = 8
        e.baselineDistance = 8
        f.baselineDistance = 8
        g.baselineDistance = 8
        h.baselineDistance = 8
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 16)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 16)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 16)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT, 16)
        c.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        c.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT, 16)
        c.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        d.connect(ConstraintAnchor.Type.BOTTOM, a, ConstraintAnchor.Type.BOTTOM)
        d.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT, 16)
        e.connect(ConstraintAnchor.Type.BOTTOM, d, ConstraintAnchor.Type.BOTTOM)
        e.connect(ConstraintAnchor.Type.LEFT, d, ConstraintAnchor.Type.RIGHT, 16)
        f.connect(ConstraintAnchor.Type.BOTTOM, e, ConstraintAnchor.Type.BOTTOM)
        f.connect(ConstraintAnchor.Type.LEFT, e, ConstraintAnchor.Type.RIGHT, 16)
        g.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        g.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 16)
        g.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        h.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 16)
        h.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 16)
        root.measurer = sMeasurer
        root.setVerticalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println(
            " direct: -> A: " + a + " B: " + b + " C: " + c +
                " D: " + d + " E: " + e + " F: " + f + " G: " + g + " H: " + h,
        )
        assertEquals(a.left, 16)
        assertEquals(a.top, 16)
        assertEquals(b.left, 132)
        assertEquals(b.top, 16)
        assertEquals(c.left, 132)
        assertEquals(c.top, 56)
        assertEquals(d.left, 132)
        assertEquals(d.top, 86)
        assertEquals(e.left, 178)
        assertEquals(e.top, 86)
        assertEquals(f.left, 224)
        assertEquals(f.top, 86)
        assertEquals(g.left, 484)
        assertEquals(g.top, 56)
        assertEquals(h.left, 484)
        assertEquals(h.top, 96)
    }

    @Test
    fun testChainLayoutWrap() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        root.optimizationLevel = Optimizer.OPTIMIZATION_GROUPING
        val a = ConstraintWidget(100, 100)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        root.add(a)
        root.add(b)
        root.add(c)
        a.baselineDistance = 28
        b.baselineDistance = 8
        c.baselineDistance = 8
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 16)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 16)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 16)
        b.connect(ConstraintAnchor.Type.BASELINE, a, ConstraintAnchor.Type.BASELINE)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.BASELINE, b, ConstraintAnchor.Type.BASELINE)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 16)
        root.measurer = sMeasurer
        // root.setWidth(332);
        root.setHorizontalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        // root.setVerticalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT);
        root.layout()
        println(" direct: -> A: $a B: $b C: $c")
        assertEquals(a.left, 16)
        assertEquals(a.top, 250)
        assertEquals(b.left, 116)
        assertEquals(b.top, 270)
        assertEquals(c.left, 216)
        assertEquals(c.top, 270)
    }

    @Test
    fun testChainLayoutWrap2() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        root.optimizationLevel = Optimizer.OPTIMIZATION_GROUPING
        val a = ConstraintWidget(100, 100)
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
        a.baselineDistance = 28
        b.baselineDistance = 8
        c.baselineDistance = 8
        d.baselineDistance = 8
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 16)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 16)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 16)
        b.connect(ConstraintAnchor.Type.BASELINE, a, ConstraintAnchor.Type.BASELINE)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.BASELINE, b, ConstraintAnchor.Type.BASELINE)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.RIGHT, d, ConstraintAnchor.Type.LEFT, 16)
        d.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        d.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        root.measurer = sMeasurer
        // root.setWidth(332);
        root.setHorizontalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        // root.setVerticalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT);
        root.layout()
        println(" direct: -> A: $a B: $b C: $c D: $d")
        assertEquals(a.left, 16)
        assertEquals(a.top, 250)
        assertEquals(b.left, 116)
        assertEquals(b.top, 270)
        assertEquals(c.left, 216)
        assertEquals(c.top, 270)
        assertEquals(d.left, 332)
        assertEquals(d.top, 580)
    }

    @Test
    fun testChainLayoutWrapGuideline() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        root.optimizationLevel = Optimizer.OPTIMIZATION_GROUPING
        val a = ConstraintWidget(100, 20)
        val guideline = Guideline()
        guideline.setOrientation(Guideline.VERTICAL)
        guideline.setGuideEnd(100)
        root.debugName = "root"
        a.debugName = "A"
        guideline.debugName = "guideline"
        root.add(a)
        root.add(guideline)
        a.baselineDistance = 28
        a.connect(ConstraintAnchor.Type.LEFT, guideline, ConstraintAnchor.Type.LEFT, 16)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 16)
        root.measurer = sMeasurer
        // root.setHorizontalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT);
        root.setVerticalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println(" direct: -> A: $a guideline: $guideline")
        assertEquals(a.left, 516)
        assertEquals(a.top, 0)
        assertEquals(guideline.left, 500)
    }

    @Test
    fun testChainLayoutWrapGuidelineChain() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        root.optimizationLevel = Optimizer.OPTIMIZATION_GROUPING
        val a = ConstraintWidget(20, 20)
        val b = ConstraintWidget(20, 20)
        val c = ConstraintWidget(20, 20)
        val d = ConstraintWidget(20, 20)
        val a2 = ConstraintWidget(20, 20)
        val b2 = ConstraintWidget(20, 20)
        val c2 = ConstraintWidget(20, 20)
        val d2 = ConstraintWidget(20, 20)
        val guidelineStart = Guideline()
        val guidelineEnd = Guideline()
        guidelineStart.setOrientation(Guideline.VERTICAL)
        guidelineEnd.setOrientation(Guideline.VERTICAL)
        guidelineStart.setGuideBegin(30)
        guidelineEnd.setGuideEnd(30)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        d.debugName = "D"
        a2.debugName = "A2"
        b2.debugName = "B2"
        c2.debugName = "C2"
        d2.debugName = "D2"
        guidelineStart.debugName = "guidelineStart"
        guidelineEnd.debugName = "guidelineEnd"
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(d)
        root.add(a2)
        root.add(b2)
        root.add(c2)
        root.add(d2)
        root.add(guidelineStart)
        root.add(guidelineEnd)
        c.visibility = ConstraintWidget.GONE
        chainConnect(
            ConstraintAnchor.Type.LEFT,
            guidelineStart,
            ConstraintAnchor.Type.RIGHT,
            guidelineEnd,
            a,
            b,
            c,
            d,
        )
        chainConnect(
            ConstraintAnchor.Type.LEFT,
            root,
            ConstraintAnchor.Type.RIGHT,
            root,
            a2,
            b2,
            c2,
            d2,
        )
        root.measurer = sMeasurer
        root.setHorizontalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        // root.setVerticalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT);
        root.layout()
        println(
            " direct: -> A: " + a + " guideline: " + guidelineStart +
                " ebnd " + guidelineEnd + " B: " + b + " C: " + c + " D: " + d,
        )
        println(" direct: -> A2: $a2 B2: $b2 C2: $c2 D2: $d2")
        assertEquals(a.left, 30)
        assertEquals(b.left, 50)
        assertEquals(c.left, 70)
        assertEquals(d.left, 70)
        assertEquals(guidelineStart.left, 30)
        assertEquals(guidelineEnd.left, 90)
        assertEquals(a2.left, 8)
        assertEquals(b2.left, 36)
        assertEquals(c2.left, 64)
        assertEquals(d2.left, 92)
    }

    private fun chainConnect(
        start: ConstraintAnchor.Type,
        startTarget: ConstraintWidget,
        end: ConstraintAnchor.Type,
        endTarget: ConstraintWidget,
        vararg widgets: ConstraintWidget,
    ) {
        widgets[0].connect(start, startTarget, start)
        var previousWidget: ConstraintWidget? = null
        for (i in widgets.indices) {
            if (previousWidget != null) {
                widgets[i].connect(start, previousWidget, end)
            }
            if (i < widgets.size - 1) {
                widgets[i].connect(end, widgets[i + 1], start)
            }
            previousWidget = widgets[i]
        }
        if (previousWidget != null) {
            previousWidget.connect(end, endTarget, end)
        }
    }

    @Test
    fun testChainLayoutWrapGuidelineChainVertical() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        root.optimizationLevel = Optimizer.OPTIMIZATION_GROUPING
        val a = ConstraintWidget(20, 20)
        val b = ConstraintWidget(20, 20)
        val c = ConstraintWidget(20, 20)
        val d = ConstraintWidget(20, 20)
        val a2 = ConstraintWidget(20, 20)
        val b2 = ConstraintWidget(20, 20)
        val c2 = ConstraintWidget(20, 20)
        val d2 = ConstraintWidget(20, 20)
        val guidelineStart = Guideline()
        val guidelineEnd = Guideline()
        guidelineStart.setOrientation(Guideline.HORIZONTAL)
        guidelineEnd.setOrientation(Guideline.HORIZONTAL)
        guidelineStart.setGuideBegin(30)
        guidelineEnd.setGuideEnd(30)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        d.debugName = "D"
        a2.debugName = "A2"
        b2.debugName = "B2"
        c2.debugName = "C2"
        d2.debugName = "D2"
        guidelineStart.debugName = "guidelineStart"
        guidelineEnd.debugName = "guidelineEnd"
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(d)
        root.add(a2)
        root.add(b2)
        root.add(c2)
        root.add(d2)
        root.add(guidelineStart)
        root.add(guidelineEnd)
        c.visibility = ConstraintWidget.GONE
        chainConnect(
            ConstraintAnchor.Type.TOP,
            guidelineStart,
            ConstraintAnchor.Type.BOTTOM,
            guidelineEnd,
            a,
            b,
            c,
            d,
        )
        chainConnect(
            ConstraintAnchor.Type.TOP,
            root,
            ConstraintAnchor.Type.BOTTOM,
            root,
            a2,
            b2,
            c2,
            d2,
        )
        root.measurer = sMeasurer
        // root.setHorizontalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT);
        root.setVerticalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println(
            " direct: -> A: " + a + " guideline: " + guidelineStart +
                " ebnd " + guidelineEnd + " B: " + b + " C: " + c + " D: " + d,
        )
        println(" direct: -> A2: $a2 B2: $b2 C2: $c2 D2: $d2")
        assertEquals(a.top, 30)
        assertEquals(b.top, 50)
        assertEquals(c.top, 70)
        assertEquals(d.top, 70)
        assertEquals(guidelineStart.top, 30)
        assertEquals(guidelineEnd.top, 90)
        assertEquals(a2.top, 8)
        assertEquals(b2.top, 36)
        assertEquals(c2.top, 64)
        assertEquals(d2.top, 92)
        assertEquals(a.left, 0)
        assertEquals(b.left, 0)
        assertEquals(c.left, 0)
        assertEquals(d.left, 0)
        assertEquals(a2.left, 0)
        assertEquals(b2.left, 0)
        assertEquals(c2.left, 0)
        assertEquals(d2.left, 0)
    }

    @Test
    fun testChainLayoutWrapRatioChain() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        root.optimizationLevel = Optimizer.OPTIMIZATION_GROUPING
        val a = ConstraintWidget(20, 20)
        val b = ConstraintWidget(20, 20)
        val c = ConstraintWidget(20, 20)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        root.add(a)
        root.add(b)
        root.add(c)
        chainConnect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.BOTTOM, root, a, b, c)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.setVerticalChainStyle(ConstraintWidget.CHAIN_SPREAD_INSIDE)
        b.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        b.setVerticalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        b.setDimensionRatio("1:1")
        root.measurer = sMeasurer
        // root.setHorizontalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT);
//        root.layout();
//
//        System.out.println(" direct: -> A: " + A + " B: " + B +  " C: "  + C);
//
//        assertEquals(A.getTop(), 0);
//        assertEquals(B.getTop(), 20);
//        assertEquals(C.getTop(), 580);
//        assertEquals(A.getLeft(), 290);
//        assertEquals(B.getLeft(), 20);
//        assertEquals(C.getLeft(), 290);
//        assertEquals(B.getWidth(), 560);
//        assertEquals(B.getHeight(), B.getWidth());
//
//        //root.setHorizontalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT);
//        root.setVerticalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT);
//        root.layout();
        root.setHorizontalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        root.setVerticalDimensionBehaviour(DimensionBehaviour.FIXED)
        root.height = 600
        root.layout()
        println(" direct: -> A: $a B: $b C: $c")
        assertEquals(a.top, 0)
        assertEquals(b.top, 290)
        assertEquals(c.top, 580)
        assertEquals(a.left, 0)
        assertEquals(b.left, 0)
        assertEquals(c.left, 0)
        assertEquals(b.width, 20)
        assertEquals(b.height, b.width)
    }

    @Test
    fun testLayoutWrapBarrier() {
        val root = ConstraintWidgetContainer("root", 600, 600)
        root.optimizationLevel = Optimizer.OPTIMIZATION_GROUPING
        val a = ConstraintWidget("A", 20, 20)
        val b = ConstraintWidget("B", 20, 20)
        val c = ConstraintWidget("C", 20, 20)
        val barrier = Barrier("Barrier")
        barrier.setBarrierType(Barrier.BOTTOM)
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(barrier)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        b.visibility = ConstraintWidget.GONE
        c.connect(ConstraintAnchor.Type.TOP, barrier, ConstraintAnchor.Type.TOP)
        barrier.add(a)
        barrier.add(b)
        root.measurer = sMeasurer
        root.setVerticalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println(
            " direct: -> root: " + root + " A: " + a + " B: " + b +
                " C: " + c + " Barrier: " + barrier.top,
        )
        assertEquals(a.left, 0)
        assertEquals(a.top, 0)
        assertEquals(b.left, 0)
        assertEquals(b.top, 20)
        assertEquals(c.left, 0)
        assertEquals(c.top, 20)
        assertEquals(barrier.top, 20)
        assertEquals(root.height, 40)
    }

    @Test
    fun testLayoutWrapGuidelinesMatch() {
        val root = ConstraintWidgetContainer("root", 600, 600)
        root.optimizationLevel = Optimizer.OPTIMIZATION_GROUPING
        // root.setOptimizationLevel(Optimizer.OPTIMIZATION_NONE);
        val a = ConstraintWidget("A", 20, 20)
        val left = Guideline()
        left.setOrientation(Guideline.VERTICAL)
        left.setGuideBegin(30)
        left.debugName = "L"
        val right = Guideline()
        right.setOrientation(Guideline.VERTICAL)
        right.setGuideEnd(30)
        right.debugName = "R"
        val top = Guideline()
        top.setOrientation(Guideline.HORIZONTAL)
        top.setGuideBegin(30)
        top.debugName = "T"
        val bottom = Guideline()
        bottom.setOrientation(Guideline.HORIZONTAL)
        bottom.setGuideEnd(30)
        bottom.debugName = "B"
        root.add(a)
        root.add(left)
        root.add(right)
        root.add(top)
        root.add(bottom)
        a.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        a.connect(ConstraintAnchor.Type.LEFT, left, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, right, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.TOP, top, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, bottom, ConstraintAnchor.Type.BOTTOM)
        root.measurer = sMeasurer
        root.setVerticalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println(
            " direct: -> root: " + root + " A: " + a + " L: " + left + " R: " + right +
                " T: " + top + " B: " + bottom,
        )
        assertEquals(root.height, 60)
        assertEquals(a.left, 30)
        assertEquals(a.top, 30)
        assertEquals(a.width, 540)
        assertEquals(a.height, 0)
    }

    @Test
    fun testLayoutWrapMatch() {
        val root = ConstraintWidgetContainer("root", 600, 600)
        root.optimizationLevel = Optimizer.OPTIMIZATION_GROUPING
        //        root.setOptimizationLevel(Optimizer.OPTIMIZATION_NONE);
        val a = ConstraintWidget("A", 50, 20)
        val b = ConstraintWidget("B", 50, 30)
        val c = ConstraintWidget("C", 50, 20)
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
        b.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        b.setVerticalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        root.measurer = sMeasurer
        root.setVerticalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println(" direct: -> root: $root A: $a B: $b C: $c")
        assertEquals(b.top, 20)
        assertEquals(b.bottom, 50)
        assertEquals(b.left, 50)
        assertEquals(b.right, 550)
        assertEquals(root.height, 70)
    }

    @Test
    fun testLayoutWrapBarrier2() {
        val root = ConstraintWidgetContainer("root", 600, 600)
        root.optimizationLevel = Optimizer.OPTIMIZATION_GROUPING
        // root.setOptimizationLevel(Optimizer.OPTIMIZATION_NONE);
        val a = ConstraintWidget("A", 50, 20)
        val b = ConstraintWidget("B", 50, 30)
        val c = ConstraintWidget("C", 50, 20)
        val guideline = Guideline()
        guideline.debugName = "end"
        guideline.setGuideEnd(40)
        guideline.setOrientation(ConstraintWidget.VERTICAL)
        val barrier = Barrier()
        barrier.setBarrierType(Barrier.LEFT)
        barrier.debugName = "barrier"
        barrier.add(b)
        barrier.add(c)
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(barrier)
        root.add(guideline)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, barrier, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.RIGHT, guideline, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        root.measurer = sMeasurer
        root.setHorizontalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println(" direct: -> root: $root A: $a B: $b C: $c")
        assertEquals(root.width, 140)
    }

    @Test
    fun testLayoutWrapBarrier3() {
        val root = ConstraintWidgetContainer("root", 600, 600)
        root.optimizationLevel = Optimizer.OPTIMIZATION_GROUPING
        root.optimizationLevel = Optimizer.OPTIMIZATION_NONE
        val a = ConstraintWidget("A", 50, 20)
        val b = ConstraintWidget("B", 50, 30)
        val c = ConstraintWidget("C", 50, 20)
        val guideline = Guideline()
        guideline.debugName = "end"
        guideline.setGuideEnd(40)
        guideline.setOrientation(ConstraintWidget.VERTICAL)
        val barrier = Barrier()
        barrier.setBarrierType(Barrier.LEFT)
        barrier.debugName = "barrier"
        barrier.add(b)
        barrier.add(c)
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(barrier)
        root.add(guideline)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, barrier, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.RIGHT, guideline, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        root.measurer = sMeasurer
        root.setHorizontalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println(" direct: -> root: $root A: $a B: $b C: $c")
        assertEquals(root.width, 140)
    }

    @Test
    fun testSimpleGuideline2() {
        val root = ConstraintWidgetContainer("root", 600, 600)
        val guidelineStart = Guideline()
        guidelineStart.debugName = "start"
        guidelineStart.setGuidePercent(0.1f)
        guidelineStart.setOrientation(ConstraintWidget.VERTICAL)
        val guidelineEnd = Guideline()
        guidelineEnd.debugName = "end"
        guidelineEnd.setGuideEnd(40)
        guidelineEnd.setOrientation(ConstraintWidget.VERTICAL)
        val a = ConstraintWidget("A", 50, 20)
        root.add(a)
        root.add(guidelineStart)
        root.add(guidelineEnd)
        a.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        a.connect(ConstraintAnchor.Type.LEFT, guidelineStart, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, guidelineEnd, ConstraintAnchor.Type.RIGHT)
        root.measurer = sMeasurer
        // root.setHorizontalDimensionBehaviour(DimensionBehaviour.WRAP_CONTENT);
        root.layout()
        println(" root: $root")
        println("guideline start: $guidelineStart")
        println("guideline end: $guidelineEnd")
    }

    companion object {
        var sMeasurer: BasicMeasure.Measurer = object : BasicMeasure.Measurer {
            override fun measure(widget: ConstraintWidget, measure: BasicMeasure.Measure) {
                val horizontalBehavior: DimensionBehaviour = measure.horizontalBehavior
                val verticalBehavior: DimensionBehaviour = measure.verticalBehavior
                val horizontalDimension = measure.horizontalDimension
                val verticalDimension = measure.verticalDimension
                println("*** MEASURE $widget ***")
                if (horizontalBehavior === DimensionBehaviour.FIXED) {
                    measure.measuredWidth = horizontalDimension
                } else if (horizontalBehavior === DimensionBehaviour.MATCH_CONSTRAINT) {
                    measure.measuredWidth = horizontalDimension
                }
                if (verticalBehavior === DimensionBehaviour.FIXED) {
                    measure.measuredHeight = verticalDimension
                    measure.measuredBaseline = 8
                } else {
                    measure.measuredHeight = verticalDimension
                    measure.measuredBaseline = 8
                }
            }

            override fun didMeasures() {}
        }
    }
}
