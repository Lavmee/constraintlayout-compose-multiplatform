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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MatchConstraintTest {
    @Test
    fun testSimpleMinMatch() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_SPREAD, 150, 200, 1f)
        root.add(a)
        root.add(b)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("a) root: $root A: $a B: $b")
        assertEquals(a.width, 150)
        assertEquals(b.width, 100)
        assertEquals(root.width, 150)
        b.width = 200
        root.width = 0
        root.layout()
        println("b) root: $root A: $a B: $b")
        assertEquals(a.width, 200)
        assertEquals(b.width, 200)
        assertEquals(root.width, 200)
        b.width = 300
        root.width = 0
        root.layout()
        println("c) root: $root A: $a B: $b")
        assertEquals(a.width, 200)
        assertEquals(b.width, 300)
        assertEquals(root.width, 300)
    }

    @Test
    fun testMinMaxMatch() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val guidelineA = androidx.constraintlayout.coremp.widgets.Guideline()
        guidelineA.setOrientation(androidx.constraintlayout.coremp.widgets.Guideline.VERTICAL)
        guidelineA.setGuideBegin(100)
        val guidelineB = androidx.constraintlayout.coremp.widgets.Guideline()
        guidelineB.setOrientation(androidx.constraintlayout.coremp.widgets.Guideline.VERTICAL)
        guidelineB.setGuideEnd(100)
        root.add(guidelineA)
        root.add(guidelineB)
        val a = ConstraintWidget(100, 20)
        a.connect(ConstraintAnchor.Type.LEFT, guidelineA, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, guidelineB, ConstraintAnchor.Type.RIGHT)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_SPREAD, 150, 200, 1f)
        root.add(a)
        root.debugName = "root"
        guidelineA.debugName = "guideline A"
        guidelineB.debugName = "guideline B"
        a.debugName = "A"
        root.layout()
        println(
            "a) root: " + root + " guideA: " + guidelineA +
                " A: " + a + " guideB: " + guidelineB,
        )
        assertEquals(root.width, 800)
        assertEquals(a.width, 200)
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        a.width = 100
        root.layout()
        println(
            "b) root: " + root + " guideA: " + guidelineA +
                " A: " + a + " guideB: " + guidelineB,
        )
        assertEquals(root.width, 350)
        assertEquals(a.width, 150)
        a.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_WRAP, 150, 200, 1f)
        root.layout()
        println(
            "c) root: " + root + " guideA: " + guidelineA +
                " A: " + a + " guideB: " + guidelineB,
        )
        assertEquals(root.width, 350)
        assertEquals(a.width, 150)
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.FIXED)
        root.width = 800
        root.layout()
        println(
            "d) root: " + root + " guideA: " + guidelineA +
                " A: " + a + " guideB: " + guidelineB,
        )
        assertEquals(root.width, 800)
        assertEquals(a.width, 150) // because it's wrap
        a.width = 250
        root.layout()
        println(
            "e) root: " + root + " guideA: " + guidelineA +
                " A: " + a + " guideB: " + guidelineB,
        )
        assertEquals(root.width, 800)
        assertEquals(a.width, 200)
        a.width = 700
        a.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_SPREAD, 150, 0, 1f)
        root.layout()
        println(
            "f) root: " + root + " guideA: " + guidelineA +
                " A: " + a + " guideB: " + guidelineB,
        )
        assertEquals(root.width, 800)
        assertEquals(a.width, 600)
        a.width = 700
        a.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_WRAP, 150, 0, 1f)
        root.layout()
        println(
            "g) root: " + root + " guideA: " + guidelineA +
                " A: " + a + " guideB: " + guidelineB,
        )
        assertEquals(root.width, 800)
        assertEquals(a.width, 600)
        a.width = 700
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.width = 0
        root.layout()
        println(
            "h) root: " + root + " guideA: " + guidelineA +
                " A: " + a + " guideB: " + guidelineB,
        )
        assertEquals(root.width, 900)
        assertEquals(a.width, 700)
        a.width = 700
        a.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_SPREAD, 150, 0, 1f)
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        assertEquals(root.width, 350)
        assertEquals(a.width, 150)
        println(
            "i) root: " + root + " guideA: " + guidelineA +
                " A: " + a + " guideB: " + guidelineB,
        )
    }

    @Test
    fun testSimpleHorizontalMatch() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        c.setDebugSolverName(root.getSystem(), "C")
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 0)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 0)
        c.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT, 0)
        c.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT, 0)
        root.add(a)
        root.add(b)
        root.add(c)
        root.layout()
        println("a) A: $a B: $b C: $c")
        assertEquals(a.width, 100)
        assertEquals(b.width, 100)
        assertEquals(c.width, 100)
        assertTrue(c.left >= a.right)
        assertTrue(c.right <= b.left)
        assertEquals(c.left - a.right, b.left - c.right)
        c.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        root.layout()
        println("b) A: $a B: $b C: $c")
        assertEquals(a.width, 100)
        assertEquals(b.width, 100)
        assertEquals(c.width, 600)
        assertTrue(c.left >= a.right)
        assertTrue(c.right <= b.left)
        assertEquals(c.left - a.right, b.left - c.right)
        c.width = 144
        c.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_WRAP, 0, 0, 0f)
        root.layout()
        println("c) A: $a B: $b C: $c")
        assertEquals(a.width, 100)
        assertEquals(b.width, 100)
        assertEquals(c.width, 144)
        assertTrue(c.left >= a.right)
        assertTrue(c.right <= b.left)
        assertEquals(c.left - a.right, b.left - c.right)
        c.width = 1000
        c.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_WRAP, 0, 0, 0f)
        root.layout()
        println("d) A: $a B: $b C: $c")
        assertEquals(a.width, 100)
        assertEquals(b.width, 100)
        assertEquals(c.width, 600)
        assertTrue(c.left >= a.right)
        assertTrue(c.right <= b.left)
        assertEquals(c.left - a.right, b.left - c.right)
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
        a.setHorizontalMatchStyle(ConstraintWidget.MATCH_CONSTRAINT_WRAP, 0, 0, 0f)
        root.layout()
        println("a) root: $root A: $a")
    }
}
