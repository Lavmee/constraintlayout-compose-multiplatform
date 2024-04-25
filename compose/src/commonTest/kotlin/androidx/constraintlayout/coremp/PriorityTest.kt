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

class PriorityTest {
    @Test
    fun testPriorityChainHorizontal() {
        val root = ConstraintWidgetContainer(0, 0, 1000, 600)
        val a = ConstraintWidget(400, 20)
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
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.RIGHT, a, ConstraintAnchor.Type.RIGHT)
        b.setHorizontalChainStyle(ConstraintWidget.CHAIN_PACKED)
        root.layout()
        println("a) root: $root A: $a B: $b C: $c")
        assertEquals(a.width, 400)
        assertEquals(b.width, 100)
        assertEquals(c.width, 100)
        assertEquals(a.left, 300)
        assertEquals(b.left, 400)
        assertEquals(c.left, 500)
        b.setHorizontalChainStyle(ConstraintWidget.CHAIN_SPREAD)
        root.layout()
        println("b) root: $root A: $a B: $b C: $c")
        assertEquals(a.width, 400)
        assertEquals(b.width, 100)
        assertEquals(c.width, 100)
        assertEquals(a.left, 300)
        assertEquals(b.left, 367)
        assertEquals(c.left, 533)
        b.setHorizontalChainStyle(ConstraintWidget.CHAIN_SPREAD_INSIDE)
        root.layout()
        println("c) root: $root A: $a B: $b C: $c")
        assertEquals(a.width, 400)
        assertEquals(b.width, 100)
        assertEquals(c.width, 100)
        assertEquals(a.left, 300)
        assertEquals(b.left, 300)
        assertEquals(c.left, 600)
    }

    @Test
    fun testPriorityChainVertical() {
        val root = ConstraintWidgetContainer(0, 0, 1000, 1000)
        val a = ConstraintWidget(400, 400)
        val b = ConstraintWidget(100, 100)
        val c = ConstraintWidget(100, 100)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        root.add(a)
        root.add(b)
        root.add(c)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.BOTTOM, c, ConstraintAnchor.Type.TOP)
        c.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.BOTTOM)
        c.connect(ConstraintAnchor.Type.BOTTOM, a, ConstraintAnchor.Type.BOTTOM)
        b.setVerticalChainStyle(ConstraintWidget.CHAIN_PACKED)
        root.layout()
        println("a) root: $root A: $a B: $b C: $c")
        assertEquals(a.height, 400)
        assertEquals(b.height, 100)
        assertEquals(c.height, 100)
        assertEquals(a.top, 300)
        assertEquals(b.top, 400)
        assertEquals(c.top, 500)
        b.setVerticalChainStyle(ConstraintWidget.CHAIN_SPREAD)
        root.layout()
        println("b) root: $root A: $a B: $b C: $c")
        assertEquals(a.height, 400)
        assertEquals(b.height, 100)
        assertEquals(c.height, 100)
        assertEquals(a.top, 300)
        assertEquals(b.top, 367)
        assertEquals(c.top, 533)
        b.setVerticalChainStyle(ConstraintWidget.CHAIN_SPREAD_INSIDE)
        root.layout()
        println("c) root: $root A: $a B: $b C: $c")
        assertEquals(a.height, 400)
        assertEquals(b.height, 100)
        assertEquals(c.height, 100)
        assertEquals(a.top, 300)
        assertEquals(b.top, 300)
        assertEquals(c.top, 600)
    }
}
