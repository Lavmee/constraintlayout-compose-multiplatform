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
import androidx.constraintlayout.coremp.widgets.ConstraintWidgetContainer
import androidx.constraintlayout.coremp.widgets.Guideline
import androidx.constraintlayout.coremp.widgets.Optimizer
import kotlin.math.max
import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for Barriers
 */
class BarrierTest {
    @Test
    fun barrierConstrainedWidth() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(200, 20)
        val barrier = Barrier()
        val guidelineStart = Guideline()
        val guidelineEnd = Guideline()
        guidelineStart.setOrientation(ConstraintWidget.VERTICAL)
        guidelineEnd.setOrientation(ConstraintWidget.VERTICAL)
        guidelineStart.setGuideBegin(30)
        guidelineEnd.setGuideEnd(20)
        root.setDebugSolverName(root.getSystem(), "root")
        guidelineStart.setDebugSolverName(root.getSystem(), "guidelineStart")
        guidelineEnd.setDebugSolverName(root.getSystem(), "guidelineEnd")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        barrier.setDebugSolverName(root.getSystem(), "Barrier")
        barrier.setBarrierType(Barrier.LEFT)
        barrier.add(a)
        barrier.add(b)
        root.add(a)
        root.add(b)
        root.add(guidelineStart)
        root.add(guidelineEnd)
        root.add(barrier)
        a.connect(ConstraintAnchor.Type.LEFT, guidelineStart, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, guidelineEnd, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.LEFT, guidelineStart, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.RIGHT, guidelineEnd, ConstraintAnchor.Type.RIGHT)
        a.horizontalBiasPercent = 1f
        b.horizontalBiasPercent = 1f
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("root: $root")
        println("guidelineStart: $guidelineStart")
        println("guidelineEnd: $guidelineEnd")
        println("A: $a")
        println("B: $b")
        println("barrier: $barrier")
        assertEquals(root.width, 250)
        assertEquals(guidelineStart.left, 30)
        assertEquals(guidelineEnd.left, 230)
        assertEquals(a.left, 130)
        assertEquals(a.width, 100)
        assertEquals(b.left, 30)
        assertEquals(b.width, 200)
        assertEquals(barrier.left, 30)
    }

    @Test
    fun barrierImage() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(200, 20)
        val c = ConstraintWidget(60, 60)
        val barrier = Barrier()
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        c.setDebugSolverName(root.getSystem(), "C")
        barrier.setDebugSolverName(root.getSystem(), "Barrier")
        barrier.setBarrierType(Barrier.RIGHT)
        barrier.add(a)
        barrier.add(b)
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(barrier)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        a.setVerticalChainStyle(ConstraintWidget.CHAIN_SPREAD_INSIDE)
        c.horizontalBiasPercent = 1f
        c.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        c.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        c.connect(ConstraintAnchor.Type.LEFT, barrier, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        root.optimizationLevel = Optimizer.OPTIMIZATION_STANDARD
        root.layout()
        println("A: $a B: $b C: $c barrier: $barrier")
        assertEquals(a.left, 0)
        assertEquals(a.top, 0)
        assertEquals(b.left, 0)
        assertEquals(b.top, 580)
        assertEquals(c.left, 740)
        assertEquals(c.top, 270)
        assertEquals(barrier.left, 200)
    }

    @Test
    fun barrierTooStrong() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(60, 60)
        val b = ConstraintWidget(100, 200)
        val c = ConstraintWidget(100, 20)
        val barrier = Barrier()
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        c.setDebugSolverName(root.getSystem(), "C")
        barrier.setDebugSolverName(root.getSystem(), "Barrier")
        barrier.setBarrierType(Barrier.BOTTOM)
        barrier.add(b)
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(barrier)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.TOP, c, ConstraintAnchor.Type.BOTTOM)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_PARENT)
        c.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_PARENT)
        c.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        c.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        c.connect(ConstraintAnchor.Type.BOTTOM, a, ConstraintAnchor.Type.BOTTOM)
        root.optimizationLevel = Optimizer.OPTIMIZATION_NONE
        root.layout()
        println("A: $a B: $b C: $c barrier: $barrier")
        assertEquals(a.left, 740)
        assertEquals(a.top, 0)
        assertEquals(b.left, 0)
        assertEquals(b.top, 60)
        assertEquals(b.width, 800)
        assertEquals(b.height, 200)
        assertEquals(c.left, 0)
        assertEquals(c.top, 0)
        assertEquals(c.width, 800)
        assertEquals(c.height, 60)
        assertEquals(barrier.bottom, 260)
    }

    @Test
    fun barrierMax() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(150, 20)
        val barrier = Barrier()
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        barrier.setDebugSolverName(root.getSystem(), "Barrier")
        barrier.add(a)
        root.add(a)
        root.add(barrier)
        root.add(b)
        barrier.setBarrierType(Barrier.RIGHT)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.LEFT, barrier, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        b.horizontalBiasPercent = 0f
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_SPREAD, 0, 150, 1f)
        root.optimizationLevel = Optimizer.OPTIMIZATION_NONE
        root.layout()
        println("A: $a B: $b barrier: $barrier")
        assertEquals(a.left, 0)
        assertEquals(barrier.left, 100)
        assertEquals(b.left, 100)
        assertEquals(b.width, 150)
    }

    @Test
    fun barrierCenter() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val barrier = Barrier()
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        barrier.setDebugSolverName(root.getSystem(), "Barrier")
        barrier.add(a)
        root.add(a)
        root.add(barrier)
        barrier.setBarrierType(Barrier.RIGHT)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 10)
        a.connect(ConstraintAnchor.Type.RIGHT, barrier, ConstraintAnchor.Type.RIGHT, 30)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        root.layout()
        println("A: $a barrier: $barrier")
        assertEquals(a.left, 10)
        assertEquals(barrier.left, 140)
    }

    @Test
    fun barrierCenter2() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val barrier = Barrier()
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        barrier.setDebugSolverName(root.getSystem(), "Barrier")
        barrier.add(a)
        root.add(a)
        root.add(barrier)
        barrier.setBarrierType(Barrier.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 10)
        a.connect(ConstraintAnchor.Type.LEFT, barrier, ConstraintAnchor.Type.LEFT, 30)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        root.layout()
        println("A: $a barrier: $barrier")
        assertEquals(a.right, root.width - 10)
        assertEquals(barrier.left, a.left - 30)
    }

    @Test
    fun barrierCenter3() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val barrier = Barrier()
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        barrier.setDebugSolverName(root.getSystem(), "Barrier")
        barrier.add(a)
        barrier.add(b)
        root.add(a)
        root.add(b)
        root.add(barrier)
        barrier.setBarrierType(Barrier.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        a.width = 100
        b.width = 200
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_WRAP, 0, 0, 0f)
        a.horizontalBiasPercent = 1f
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_WRAP, 0, 0, 0f)
        b.horizontalBiasPercent = 1f
        root.layout()
        println("A: $a B: $b barrier: $barrier")
        assertEquals(a.width, 100)
        assertEquals(b.width, 200)
        assertEquals(barrier.left, b.left)
    }

    @Test
    fun barrierCenter4() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(150, 20)
        val b = ConstraintWidget(100, 20)
        val barrier = Barrier()
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        barrier.setDebugSolverName(root.getSystem(), "Barrier")
        barrier.add(a)
        barrier.add(b)
        root.add(a)
        root.add(b)
        root.add(barrier)
        barrier.setBarrierType(Barrier.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.LEFT, barrier, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.LEFT, barrier, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        a.horizontalBiasPercent = 0f
        b.horizontalBiasPercent = 0f
        root.layout()
        println("A: $a B: $b barrier: $barrier")
        assertEquals(a.right, root.width)
        assertEquals(barrier.left, min(a.left.toDouble(), b.left.toDouble()).toInt())
        assertEquals(a.left, barrier.left)
        assertEquals(b.left, barrier.left)
    }

    @Test
    fun barrierCenter5() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(150, 20)
        val c = ConstraintWidget(200, 20)
        val barrier = Barrier()
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        c.setDebugSolverName(root.getSystem(), "C")
        barrier.setDebugSolverName(root.getSystem(), "Barrier")
        barrier.add(a)
        barrier.add(b)
        barrier.add(c)
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(barrier)
        barrier.setBarrierType(Barrier.RIGHT)
        a.connect(ConstraintAnchor.Type.RIGHT, barrier, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.RIGHT, barrier, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        c.connect(ConstraintAnchor.Type.RIGHT, barrier, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.BOTTOM)
        a.horizontalBiasPercent = 0f
        b.horizontalBiasPercent = 0f
        c.horizontalBiasPercent = 0f
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        c.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_WRAP, 0, 0, 0f)
        b.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_WRAP, 0, 0, 0f)
        c.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_WRAP, 0, 0, 0f)
        root.layout()
        println("A: $a B: $b C: $c barrier: $barrier")
        assertEquals(
            barrier.right,
            max(max(a.right.toDouble(), b.right.toDouble()), c.right.toDouble())
                .toInt(),
        )
        assertEquals(a.width, 100)
        assertEquals(b.width, 150)
        assertEquals(c.width, 200)
    }

    @Test
    fun basic() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(150, 20)
        val barrier = Barrier()
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        barrier.setDebugSolverName(root.getSystem(), "Barrier")
        root.add(a)
        root.add(b)
        root.add(barrier)
        barrier.setBarrierType(Barrier.LEFT)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 50)
        b.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM, 20)
        barrier.add(a)
        barrier.add(b)
        root.optimizationLevel = Optimizer.OPTIMIZATION_NONE
        root.layout()
        println("A: $a B: $b barrier: $barrier")
        assertEquals(barrier.left, b.left)
        barrier.setBarrierType(Barrier.RIGHT)
        root.layout()
        println("A: $a B: $b barrier: $barrier")
        assertEquals(barrier.right, b.right)
        barrier.setBarrierType(Barrier.LEFT)
        b.width = 10
        root.layout()
        println("A: $a B: $b barrier: $barrier")
        assertEquals(barrier.left, a.left)
    }

    @Test
    fun basic2() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(150, 20)
        val barrier = Barrier()
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        barrier.setDebugSolverName(root.getSystem(), "Barrier")
        root.add(a)
        root.add(b)
        root.add(barrier)
        barrier.setBarrierType(Barrier.BOTTOM)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_WRAP, 0, 0, 0f)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.TOP, barrier, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        barrier.add(a)
        root.optimizationLevel = Optimizer.OPTIMIZATION_NONE
        root.layout()
        println("A: $a B: $b barrier: $barrier")
        assertEquals(barrier.top, a.bottom)
        val actual: Float = (
            barrier.bottom +
                (root.bottom - barrier.bottom - b.height) / 2f
            )
        assertEquals(b.top.toFloat(), actual, 1f)
    }

    @Test
    fun basic3() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(150, 20)
        val barrier = Barrier()
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        barrier.setDebugSolverName(root.getSystem(), "Barrier")
        root.add(a)
        root.add(b)
        root.add(barrier)
        barrier.setBarrierType(Barrier.RIGHT)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.LEFT, barrier, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        barrier.add(a)
        root.optimizationLevel = Optimizer.OPTIMIZATION_NONE
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("root: $root A: $a B: $b barrier: $barrier")
        assertEquals(barrier.right, a.right)
        assertEquals(root.width, a.width + b.width)
    }

    @Test
    fun basic4() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        val barrier = Barrier()
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        c.setDebugSolverName(root.getSystem(), "C")
        barrier.setDebugSolverName(root.getSystem(), "Barrier")
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(barrier)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        b.visibility = ConstraintWidget.GONE
        c.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.TOP, barrier, ConstraintAnchor.Type.TOP)
        barrier.add(a)
        barrier.add(b)
        barrier.setBarrierType(Barrier.BOTTOM)
        root.optimizationLevel = Optimizer.OPTIMIZATION_NONE
        root.layout()
        println(
            "root: " + root +
                " A: " + a + " B: " + b + " C: " + c + " barrier: " + barrier,
        )
        assertEquals(b.top, a.bottom)
        assertEquals(barrier.top, b.bottom)
        assertEquals(c.top, barrier.top)
    }

    @Test
    fun growArray() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(150, 20)
        val c = ConstraintWidget(175, 20)
        val d = ConstraintWidget(200, 20)
        val e = ConstraintWidget(125, 20)
        val barrier = Barrier()
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        c.setDebugSolverName(root.getSystem(), "C")
        d.setDebugSolverName(root.getSystem(), "D")
        e.setDebugSolverName(root.getSystem(), "E")
        barrier.setDebugSolverName(root.getSystem(), "Barrier")
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(d)
        root.add(e)
        root.add(barrier)
        barrier.setBarrierType(Barrier.LEFT)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 50)
        b.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM, 20)
        c.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.BOTTOM, 20)
        d.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        d.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        d.connect(ConstraintAnchor.Type.TOP, c, ConstraintAnchor.Type.BOTTOM, 20)
        e.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        e.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        e.connect(ConstraintAnchor.Type.TOP, d, ConstraintAnchor.Type.BOTTOM, 20)
        barrier.add(a)
        barrier.add(b)
        barrier.add(c)
        barrier.add(d)
        barrier.add(e)
        root.layout()
        println(
            "A: " + a +
                " B: " + b + " C: " + c + " D: " + d + " E: " + e + " barrier: " + barrier,
        )
        assertEquals(a.left, (root.width - a.width) / 2, 1)
        assertEquals(b.left, (root.width - b.width) / 2, 1)
        assertEquals(c.left, (root.width - c.width) / 2, 1)
        assertEquals(d.left, (root.width - d.width) / 2, 1)
        assertEquals(e.left, (root.width - e.width) / 2, 1)
        assertEquals(barrier.left, d.left)
    }

    @Test
    fun connection() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(150, 20)
        val c = ConstraintWidget(100, 20)
        val barrier = Barrier()
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        c.setDebugSolverName(root.getSystem(), "C")
        barrier.setDebugSolverName(root.getSystem(), "Barrier")
        root.add(a)
        root.add(b)
        root.add(c)
        root.add(barrier)
        barrier.setBarrierType(Barrier.LEFT)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 50)
        b.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM, 20)
        c.connect(ConstraintAnchor.Type.LEFT, barrier, ConstraintAnchor.Type.LEFT, 0)
        barrier.add(a)
        barrier.add(b)
        root.layout()
        println("A: $a B: $b C: $c barrier: $barrier")
        assertEquals(barrier.left, b.left)
        assertEquals(c.left, barrier.left)
    }

    @Test
    fun withGuideline() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val barrier = Barrier()
        val guideline = Guideline()
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        barrier.setDebugSolverName(root.getSystem(), "Barrier")
        guideline.setDebugSolverName(root.getSystem(), "Guideline")
        guideline.setOrientation(ConstraintWidget.VERTICAL)
        guideline.setGuideBegin(200)
        barrier.setBarrierType(Barrier.RIGHT)
        root.add(a)
        root.add(barrier)
        root.add(guideline)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 10)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 50)
        barrier.add(a)
        barrier.add(guideline)
        root.layout()
        println("A: $a guideline: $guideline barrier: $barrier")
        assertEquals(barrier.left, guideline.left)
    }

    @Test
    fun wrapIssue() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val barrier = Barrier()
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        barrier.setDebugSolverName(root.getSystem(), "Barrier")
        barrier.setBarrierType(Barrier.BOTTOM)
        root.add(a)
        root.add(b)
        root.add(barrier)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 0)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 0)
        barrier.add(a)
        b.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 0)
        b.connect(ConstraintAnchor.Type.TOP, barrier, ConstraintAnchor.Type.BOTTOM, 0)
        b.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 0)
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("1/ root: $root A: $a B: $b barrier: $barrier")
        assertEquals(barrier.top, a.bottom)
        assertEquals(b.top, barrier.bottom)
        assertEquals(root.height, a.height + b.height)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_WRAP, 0, 0, 0f)
        root.layout()
        println("2/ root: $root A: $a B: $b barrier: $barrier")
        assertEquals(barrier.top, a.bottom)
        assertEquals(b.top, barrier.bottom)
        assertEquals(root.height, a.height + b.height)
    }
}
