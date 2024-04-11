/*
 * Copyright (C) 2018 The Android Open Source Project
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
package androidx.constraintlayout.coremp.widgets

import androidx.constraintlayout.coremp.widgets.ConstraintWidget.DimensionBehaviour
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChainHeadTest {
    @Test
    fun basicHorizontalChainHeadTest() {
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
        var chainHead = ChainHead(a, ConstraintWidget.HORIZONTAL, false)
        chainHead.define()
        assertEquals(chainHead.getHead(), a)
        assertEquals(chainHead.getFirst(), a)
        assertEquals(chainHead.getFirstVisibleWidget(), a)
        assertEquals(chainHead.getLast(), c)
        assertEquals(chainHead.getLastVisibleWidget(), c)
        a.visibility = ConstraintWidget.GONE
        chainHead = ChainHead(a, ConstraintWidget.HORIZONTAL, false)
        chainHead.define()
        assertEquals(chainHead.getHead(), a)
        assertEquals(chainHead.getFirst(), a)
        assertEquals(chainHead.getFirstVisibleWidget(), b)
        chainHead = ChainHead(a, ConstraintWidget.HORIZONTAL, true)
        chainHead.define()
        assertEquals(chainHead.getHead(), c)
        assertEquals(chainHead.getFirst(), a)
        assertEquals(chainHead.getFirstVisibleWidget(), b)
    }

    @Test
    fun basicVerticalChainHeadTest() {
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
        a.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.BOTTOM, c, ConstraintAnchor.Type.TOP)
        c.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.BOTTOM)
        c.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        var chainHead = ChainHead(a, ConstraintWidget.VERTICAL, false)
        chainHead.define()
        assertEquals(chainHead.getHead(), a)
        assertEquals(chainHead.getFirst(), a)
        assertEquals(chainHead.getFirstVisibleWidget(), a)
        assertEquals(chainHead.getLast(), c)
        assertEquals(chainHead.getLastVisibleWidget(), c)
        a.visibility = ConstraintWidget.GONE
        chainHead = ChainHead(a, ConstraintWidget.VERTICAL, false)
        chainHead.define()
        assertEquals(chainHead.getHead(), a)
        assertEquals(chainHead.getFirst(), a)
        assertEquals(chainHead.getFirstVisibleWidget(), b)
        chainHead = ChainHead(a, ConstraintWidget.VERTICAL, true)
        chainHead.define()
        assertEquals(chainHead.getHead(), a)
        assertEquals(chainHead.getFirst(), a)
        assertEquals(chainHead.getFirstVisibleWidget(), b)
    }

    @Test
    fun basicMatchConstraintTest() {
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
        a.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        b.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        c.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        a.setHorizontalWeight(1f)
        b.setHorizontalWeight(2f)
        c.setHorizontalWeight(3f)
        var chainHead = ChainHead(a, ConstraintWidget.HORIZONTAL, false)
        chainHead.define()
        assertEquals(chainHead.getFirstMatchConstraintWidget(), a)
        assertEquals(chainHead.getLastMatchConstraintWidget(), c)
        assertEquals(chainHead.getTotalWeight(), 6f, 0f)
        c.visibility = ConstraintWidget.GONE
        chainHead = ChainHead(a, ConstraintWidget.HORIZONTAL, false)
        chainHead.define()
        assertEquals(chainHead.getFirstMatchConstraintWidget(), a)
        assertEquals(chainHead.getLastMatchConstraintWidget(), b)
        assertEquals(chainHead.getTotalWeight(), 3f, 0f)
    }

    @Test
    fun chainOptimizerValuesTest() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        val a = ConstraintWidget(50, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(200, 20)
        root.debugName = "root"
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        root.add(a, b, c)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 5)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT, 5)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT, 1)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.LEFT, 1)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT, 10)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 10)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        c.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        var chainHead = ChainHead(a, ConstraintWidget.HORIZONTAL, false)
        chainHead.define()
        assertEquals(chainHead.mVisibleWidgets, 3)
        assertEquals(chainHead.mTotalSize, 367) // Takes all but first and last margins.
        assertEquals(chainHead.mTotalMargins, 32)
        assertEquals(chainHead.mWidgetsMatchCount, 0)
        assertTrue(chainHead.mOptimizable)
        b.visibility = ConstraintWidget.GONE
        chainHead = ChainHead(a, ConstraintWidget.HORIZONTAL, false)
        chainHead.define()
        assertEquals(chainHead.mVisibleWidgets, 2)
        assertEquals(chainHead.mTotalSize, 265)
        assertEquals(chainHead.mTotalMargins, 30)
        assertEquals(chainHead.mWidgetsMatchCount, 0)
        assertTrue(chainHead.mOptimizable)
        a.visibility = ConstraintWidget.GONE
        b.visibility = ConstraintWidget.VISIBLE
        c.visibility = ConstraintWidget.GONE
        chainHead = ChainHead(a, ConstraintWidget.HORIZONTAL, false)
        chainHead.define()
        assertEquals(chainHead.mVisibleWidgets, 1)
        assertEquals(chainHead.mTotalSize, 100)
        assertEquals(chainHead.mTotalMargins, 2)
        assertEquals(chainHead.mWidgetsMatchCount, 0)
        assertTrue(chainHead.mOptimizable)
        a.visibility = ConstraintWidget.VISIBLE
        b.visibility = ConstraintWidget.VISIBLE
        c.visibility = ConstraintWidget.VISIBLE
        a.setHorizontalDimensionBehaviour(DimensionBehaviour.MATCH_CONSTRAINT)
        a.mMatchConstraintDefaultWidth = ConstraintWidget.MATCH_CONSTRAINT_PERCENT
        chainHead = ChainHead(a, ConstraintWidget.HORIZONTAL, false)
        chainHead.define()
        assertEquals(chainHead.mVisibleWidgets, 3)
        assertEquals(chainHead.mTotalSize, 317)
        assertEquals(chainHead.mTotalMargins, 32)
        assertEquals(chainHead.mWidgetsMatchCount, 1)
        assertFalse(chainHead.mOptimizable)
    }
}
