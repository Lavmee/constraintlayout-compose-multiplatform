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

/**
 * Basic visibility behavior test in the solver
 */
class VisibilityTest {
    @Test
    fun testGoneSingleConnection() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        val margin = 175
        val goneMargin = 42
        root.add(a)
        root.add(b)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, margin)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, margin)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT, margin)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM, margin)
        root.layout()
        println("a) A: $a B: $b")
        assertEquals(root.width, 800)
        assertEquals(root.height, 600)
        assertEquals(a.width, 100)
        assertEquals(a.height, 20)
        assertEquals(b.width, 100)
        assertEquals(b.height, 20)
        assertEquals(a.left, root.left + margin)
        assertEquals(a.top, root.top + margin)
        assertEquals(b.left, a.right + margin)
        assertEquals(b.top, a.bottom + margin)
        a.visibility = ConstraintWidget.GONE
        root.layout()
        println("b) A: $a B: $b")
        assertEquals(root.width, 800)
        assertEquals(root.height, 600)
        assertEquals(a.width, 0)
        assertEquals(a.height, 0)
        assertEquals(b.width, 100)
        assertEquals(b.height, 20)
        assertEquals(a.left, root.left)
        assertEquals(a.top, root.top)
        assertEquals(b.left, a.right + margin)
        assertEquals(b.top, a.bottom + margin)
        b.setGoneMargin(ConstraintAnchor.Type.LEFT, goneMargin)
        b.setGoneMargin(ConstraintAnchor.Type.TOP, goneMargin)
        root.layout()
        println("c) A: $a B: $b")
        assertEquals(root.width, 800)
        assertEquals(root.height, 600)
        assertEquals(a.width, 0)
        assertEquals(a.height, 0)
        assertEquals(b.width, 100)
        assertEquals(b.height, 20)
        assertEquals(a.left, root.left)
        assertEquals(a.top, root.top)
        assertEquals(b.left, a.right + goneMargin)
        assertEquals(b.top, a.bottom + goneMargin)
    }

    @Test
    fun testGoneDualConnection() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val guideline = androidx.constraintlayout.coremp.widgets.Guideline()
        guideline.setGuidePercent(0.5f)
        guideline.setOrientation(ConstraintWidget.HORIZONTAL)
        root.setDebugSolverName(root.getSystem(), "root")
        a.setDebugSolverName(root.getSystem(), "A")
        b.setDebugSolverName(root.getSystem(), "B")
        root.add(a)
        root.add(b)
        root.add(guideline)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, guideline, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        root.layout()
        println("a) A: $a B: $b guideline $guideline")
        assertEquals(root.width, 800)
        assertEquals(root.height, 600)
        assertEquals(a.left, root.left)
        assertEquals(a.right, root.right)
        assertEquals(b.left, root.left)
        assertEquals(b.right, root.right)
        assertEquals(guideline.top, root.height / 2)
        assertEquals(a.top, root.top)
        assertEquals(a.bottom, guideline.top)
        assertEquals(b.top, a.bottom)
        assertEquals(b.bottom, root.bottom)
        a.visibility = ConstraintWidget.GONE
        root.layout()
        println("b) A: $a B: $b guideline $guideline")
        assertEquals(root.width, 800)
        assertEquals(root.height, 600)
        assertEquals(a.width, 0)
        assertEquals(a.height, 0)
        assertEquals(a.left, 400)
        assertEquals(a.right, 400)
        assertEquals(b.left, root.left)
        assertEquals(b.right, root.right)
        assertEquals(guideline.top, root.height / 2)
        assertEquals(a.top, 150)
        assertEquals(a.bottom, 150)
        assertEquals(b.top, 150)
        assertEquals(b.bottom, root.bottom)
    }
}
