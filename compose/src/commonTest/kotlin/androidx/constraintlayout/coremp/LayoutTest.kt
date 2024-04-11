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

import androidx.constraintlayout.coremp.platform.System
import androidx.constraintlayout.coremp.widgets.ConstraintAnchor
import androidx.constraintlayout.coremp.widgets.ConstraintWidget
import androidx.constraintlayout.coremp.widgets.ConstraintWidgetContainer
import kotlin.test.Test

class LayoutTest {
    @Test
    fun testPositions() {
        val root = ConstraintWidgetContainer(1000, 1000)
        root.debugName = "root"
        val button3 = ConstraintWidget(200, 20)
        button3.debugName = "button3"
        val button4 = ConstraintWidget(200, 20)
        button4.debugName = "button4"
        val button5 = ConstraintWidget(200, 20)
        button5.debugName = "button5"
        val editText = ConstraintWidget(200, 20)
        editText.debugName = "editText"
        val button6 = ConstraintWidget(200, 20)
        button6.debugName = "button6"
        val button7 = ConstraintWidget(200, 20)
        button7.debugName = "button7"
        val button8 = ConstraintWidget(200, 20)
        button8.debugName = "button8"
        val button9 = ConstraintWidget(200, 20)
        button9.debugName = "button9"
        val editText2 = ConstraintWidget(200, 20)
        editText2.debugName = "editText2"
        val toggleButton = ConstraintWidget(200, 20)
        toggleButton.debugName = "toggleButton"
        val toggleButton2 = ConstraintWidget(200, 20)
        toggleButton2.debugName = "toggleButton2"
        val toggleButton3 = ConstraintWidget(200, 20)
        toggleButton3.debugName = "toggleButton3"
        val toggleButton4 = ConstraintWidget(200, 20)
        toggleButton4.debugName = "toggleButton4"
        val textView = ConstraintWidget(200, 20)
        textView.debugName = "textView"
        val textView2 = ConstraintWidget(200, 20)
        textView2.debugName = "textView2"
        val back = ConstraintWidget(200, 20)
        back.debugName = "back"
        button3.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        button3.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        button4.connect(ConstraintAnchor.Type.LEFT, button3, ConstraintAnchor.Type.LEFT)
        button4.connect(ConstraintAnchor.Type.TOP, button3, ConstraintAnchor.Type.BOTTOM)
        button4.connect(ConstraintAnchor.Type.RIGHT, button3, ConstraintAnchor.Type.RIGHT)
        button5.connect(ConstraintAnchor.Type.LEFT, button4, ConstraintAnchor.Type.LEFT)
        button5.connect(ConstraintAnchor.Type.TOP, button4, ConstraintAnchor.Type.BOTTOM)
        button5.connect(ConstraintAnchor.Type.RIGHT, button4, ConstraintAnchor.Type.RIGHT)
        editText.connect(ConstraintAnchor.Type.LEFT, button5, ConstraintAnchor.Type.LEFT)
        editText.connect(ConstraintAnchor.Type.TOP, button5, ConstraintAnchor.Type.BOTTOM)
        button6.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        button6.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        button7.connect(ConstraintAnchor.Type.LEFT, button6, ConstraintAnchor.Type.LEFT)
        button7.connect(ConstraintAnchor.Type.TOP, button6, ConstraintAnchor.Type.BOTTOM)
        button7.connect(ConstraintAnchor.Type.RIGHT, button6, ConstraintAnchor.Type.RIGHT)
        button8.connect(ConstraintAnchor.Type.LEFT, button5, ConstraintAnchor.Type.LEFT)
        button8.connect(ConstraintAnchor.Type.TOP, button5, ConstraintAnchor.Type.BOTTOM)
        button8.connect(ConstraintAnchor.Type.RIGHT, button5, ConstraintAnchor.Type.RIGHT)
        button9.connect(ConstraintAnchor.Type.LEFT, toggleButton2, ConstraintAnchor.Type.LEFT)
        button9.connect(ConstraintAnchor.Type.RIGHT, toggleButton2, ConstraintAnchor.Type.RIGHT)
        button9.connect(ConstraintAnchor.Type.BASELINE, button8, ConstraintAnchor.Type.BASELINE)
        editText2.connect(ConstraintAnchor.Type.LEFT, editText, ConstraintAnchor.Type.LEFT)
        editText2.connect(ConstraintAnchor.Type.TOP, editText, ConstraintAnchor.Type.BOTTOM)
        editText2.connect(ConstraintAnchor.Type.RIGHT, editText, ConstraintAnchor.Type.RIGHT)
        toggleButton.connect(ConstraintAnchor.Type.LEFT, button8, ConstraintAnchor.Type.LEFT)
        toggleButton.connect(ConstraintAnchor.Type.TOP, button8, ConstraintAnchor.Type.BOTTOM)
        toggleButton.connect(ConstraintAnchor.Type.RIGHT, button8, ConstraintAnchor.Type.RIGHT)
        toggleButton2.connect(
            ConstraintAnchor.Type.LEFT,
            toggleButton,
            ConstraintAnchor.Type.RIGHT,
        )
        toggleButton2.connect(ConstraintAnchor.Type.TOP, button9, ConstraintAnchor.Type.BOTTOM)
        toggleButton3.connect(
            ConstraintAnchor.Type.LEFT,
            toggleButton2,
            ConstraintAnchor.Type.RIGHT,
        )
        toggleButton3.connect(ConstraintAnchor.Type.TOP, toggleButton2, ConstraintAnchor.Type.TOP)
        toggleButton4.connect(
            ConstraintAnchor.Type.LEFT,
            toggleButton3,
            ConstraintAnchor.Type.RIGHT,
        )
        toggleButton4.connect(
            ConstraintAnchor.Type.BASELINE,
            toggleButton3,
            ConstraintAnchor.Type.BASELINE,
        )
        textView.connect(ConstraintAnchor.Type.LEFT, textView2, ConstraintAnchor.Type.LEFT)
        textView.connect(ConstraintAnchor.Type.TOP, textView2, ConstraintAnchor.Type.BOTTOM)
        textView2.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        textView2.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        back.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        back.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        root.add(button3)
        root.add(button4)
        root.add(button5)
        root.add(editText)
        root.add(button6)
        root.add(button7)
        root.add(button8)
        root.add(button9)
        root.add(editText2)
        root.add(toggleButton)
        root.add(toggleButton2)
        root.add(toggleButton3)
        root.add(toggleButton4)
        root.add(textView)
        root.add(textView2)
        root.add(button3)
        root.add(back)
        while (true) {
            val time: Long = System.nanoTime()
            for (i in 0..0) {
                root.layout()
            }
            val time2: Long = System.nanoTime() - time
            println("Time spent: " + time2 / 1E6 + " ms")
            root.getSystem().displaySystemInformation()
            return
        }
    }
}
