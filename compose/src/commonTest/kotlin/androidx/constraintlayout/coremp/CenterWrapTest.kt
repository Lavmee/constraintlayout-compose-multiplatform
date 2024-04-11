/*
 * Copyright (C) 2017 The Android Open Source Project
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
import kotlin.test.Test
import kotlin.test.assertEquals

class CenterWrapTest {
    @Test
    fun testRatioCenter() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        root.debugName = "Root"
        a.debugName = "A"
        b.debugName = "B"
        root.add(a)
        root.add(b)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.setDimensionRatio(0.3f, ConstraintWidget.VERTICAL)
        b.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setDimensionRatio(1f, ConstraintWidget.VERTICAL)
        //        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT);
        root.optimizationLevel = 0
        root.layout()
        println("root: $root A: $a")
    }

    @Test
    fun testSimpleWrap() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        root.debugName = "Root"
        a.debugName = "A"
        root.add(a)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.optimizationLevel = 0
        root.layout()
        println("root: $root A: $a")
        assertEquals(a.width, 100)
        assertEquals(a.height, 20)
        assertEquals(root.width, 100)
        assertEquals(root.height, 20)
    }

    @Test
    fun testSimpleWrap2() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        root.debugName = "Root"
        a.debugName = "A"
        root.add(a)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.optimizationLevel = 0
        root.layout()
        println("root: $root A: $a")
        assertEquals(a.width, 100)
        assertEquals(a.height, 20)
        assertEquals(root.width, 100)
        assertEquals(root.height, 20)
    }

    @Test
    fun testWrap() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        root.debugName = "Root"
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        root.add(a)
        root.add(b)
        root.add(c)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.TOP)
        c.connect(ConstraintAnchor.Type.LEFT, b, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.BOTTOM)
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.optimizationLevel = 0
        root.layout()
        println("root: $root A: $a B: $b C: $c")
        assertEquals(a.width, 100)
        assertEquals(b.width, 100)
        assertEquals(c.width, 100)
        assertEquals(a.width, 100)
        assertEquals(a.height, 20)
        assertEquals(b.height, 20)
        assertEquals(c.height, 20)
    }

    @Test
    fun testWrapHeight() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val tl = ConstraintWidget(100, 20)
        val trl = ConstraintWidget(100, 20)
        val tbl = ConstraintWidget(100, 20)
        val img = ConstraintWidget(100, 100)
        root.debugName = "root"
        tl.debugName = "TL"
        trl.debugName = "TRL"
        tbl.debugName = "TBL"
        img.debugName = "IMG"

        // vertical
        tl.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        tl.connect(ConstraintAnchor.Type.BOTTOM, tbl, ConstraintAnchor.Type.BOTTOM)
        trl.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        // TRL.connect(ConstraintAnchor.Type.BOTTOM, TBL, ConstraintAnchor.Type.TOP);
        tbl.connect(ConstraintAnchor.Type.TOP, trl, ConstraintAnchor.Type.BOTTOM)
        img.connect(ConstraintAnchor.Type.TOP, tbl, ConstraintAnchor.Type.BOTTOM)
        img.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        root.add(tl)
        root.add(trl)
        root.add(tbl)
        root.add(img)
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println(
            "a) root: " + root + " TL: " + tl +
                " TRL: " + trl + " TBL: " + tbl + " IMG: " + img,
        )
        assertEquals(root.height, 140)
    }

    @Test
    fun testComplexLayout() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val img = ConstraintWidget(100, 100)
        val margin = 16
        val button = ConstraintWidget(50, 50)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val c = ConstraintWidget(100, 20)
        img.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        img.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        img.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        img.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        button.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, margin)
        button.connect(ConstraintAnchor.Type.TOP, img, ConstraintAnchor.Type.BOTTOM)
        button.connect(ConstraintAnchor.Type.BOTTOM, img, ConstraintAnchor.Type.BOTTOM)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, margin)
        a.connect(ConstraintAnchor.Type.TOP, button, ConstraintAnchor.Type.BOTTOM, margin)
        b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, margin)
        b.connect(ConstraintAnchor.Type.TOP, button, ConstraintAnchor.Type.BOTTOM, margin)
        c.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, margin)
        c.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, margin)
        c.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM, margin)
        c.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        root.add(img)
        root.add(button)
        root.add(a)
        root.add(b)
        root.add(c)
        root.debugName = "root"
        img.debugName = "IMG"
        button.debugName = "BUTTON"
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        root.layout()
        println(
            "a) root: " + root + " IMG: " + img +
                " BUTTON: " + button + " A: " + a + " B: " + b + " C: " + c,
        )
        assertEquals(root.width, 800)
        assertEquals(root.height, 600)
        assertEquals(img.width, root.width)
        assertEquals(button.width, 50)
        assertEquals(a.width, 100)
        assertEquals(b.width, 100)
        assertEquals(c.width, 100)
        assertEquals(img.height, 100)
        assertEquals(button.height, 50)
        assertEquals(a.height, 20)
        assertEquals(b.height, 20)
        assertEquals(c.height, 20)
        assertEquals(img.left, 0)
        assertEquals(img.right, root.right)
        assertEquals(button.left, 734)
        assertEquals(button.top, img.bottom - button.height / 2)
        assertEquals(a.left, margin)
        assertEquals(a.top, button.bottom + margin)
        assertEquals(b.right, root.right - margin)
        assertEquals(b.top, a.top)
        assertEquals(c.left, 350)
        assertEquals(c.right, 450)
        assertEquals(c.top, 379, 1)
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        root.optimizationLevel = 0
        println(
            "b) root: " + root + " IMG: " + img +
                " BUTTON: " + button + " A: " + a + " B: " + b + " C: " + c,
        )
        assertEquals(root.width, 800)
        assertEquals(root.height, 197)
        assertEquals(img.width, root.width)
        assertEquals(button.width, 50)
        assertEquals(a.width, 100)
        assertEquals(b.width, 100)
        assertEquals(c.width, 100)
        assertEquals(img.height, 100)
        assertEquals(button.height, 50)
        assertEquals(a.height, 20)
        assertEquals(b.height, 20)
        assertEquals(c.height, 20)
        assertEquals(img.left, 0)
        assertEquals(img.right, root.right)
        assertEquals(button.left, 734)
        assertEquals(button.top, img.bottom - button.height / 2)
        assertEquals(a.left, margin)
        assertEquals(a.top, button.bottom + margin)
        assertEquals(b.right, root.right - margin)
        assertEquals(b.top, a.top)
        assertEquals(c.left, 350)
        assertEquals(c.right, 450)
        assertEquals(c.top, a.bottom + margin)
    }

    @Test
    fun testWrapCenter() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val TextBox = ConstraintWidget(100, 50)
        val TextBoxGone = ConstraintWidget(100, 50)
        val ValueBox = ConstraintWidget(20, 20)
        root.debugName = "root"
        TextBox.debugName = "TextBox"
        TextBoxGone.debugName = "TextBoxGone"
        ValueBox.debugName = "ValueBox"

        // vertical
        TextBox.setHorizontalDimensionBehaviour(
            ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT,
        )
        TextBox.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 10)
        TextBox.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 10)
        TextBox.connect(ConstraintAnchor.Type.RIGHT, ValueBox, ConstraintAnchor.Type.LEFT, 10)
        ValueBox.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 10)
        ValueBox.connect(ConstraintAnchor.Type.TOP, TextBox, ConstraintAnchor.Type.TOP)
        ValueBox.connect(ConstraintAnchor.Type.BOTTOM, TextBox, ConstraintAnchor.Type.BOTTOM)
        TextBoxGone.setHorizontalDimensionBehaviour(
            ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT,
        )
        TextBoxGone.connect(ConstraintAnchor.Type.TOP, TextBox, ConstraintAnchor.Type.BOTTOM, 10)
        TextBoxGone.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT, 10)
        TextBoxGone.connect(ConstraintAnchor.Type.RIGHT, TextBox, ConstraintAnchor.Type.RIGHT)
        TextBoxGone.visibility = ConstraintWidget.GONE
        root.add(TextBox)
        root.add(ValueBox)
        root.add(TextBoxGone)
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println(
            "a) root: " + root + " TextBox: " + TextBox +
                " ValueBox: " + ValueBox + " TextBoxGone: " + TextBoxGone,
        )
        assertEquals(
            ValueBox.top,
            TextBox.top + (TextBox.height - ValueBox.height) / 2,
        )
        assertEquals(root.height, 60)
    }
}
