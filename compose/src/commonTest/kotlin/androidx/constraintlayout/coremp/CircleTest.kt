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

import androidx.constraintlayout.coremp.widgets.ConstraintAnchor
import androidx.constraintlayout.coremp.widgets.ConstraintWidget
import androidx.constraintlayout.coremp.widgets.ConstraintWidgetContainer
import kotlin.test.Test
import kotlin.test.assertEquals

class CircleTest {
    @Test
    fun basic() {
        val root = ConstraintWidgetContainer(0, 0, 1000, 600)
        val a = ConstraintWidget(100, 20)
        val w1 = ConstraintWidget(10, 10)
        val w2 = ConstraintWidget(10, 10)
        val w3 = ConstraintWidget(10, 10)
        val w4 = ConstraintWidget(10, 10)
        val w5 = ConstraintWidget(10, 10)
        val w6 = ConstraintWidget(10, 10)
        val w7 = ConstraintWidget(10, 10)
        val w8 = ConstraintWidget(10, 10)
        val w9 = ConstraintWidget(10, 10)
        val w10 = ConstraintWidget(10, 10)
        val w11 = ConstraintWidget(10, 10)
        val w12 = ConstraintWidget(10, 10)
        root.debugName = "root"
        a.debugName = "A"
        w1.debugName = "w1"
        w2.debugName = "w2"
        w3.debugName = "w3"
        w4.debugName = "w4"
        w5.debugName = "w5"
        w6.debugName = "w6"
        w7.debugName = "w7"
        w8.debugName = "w8"
        w9.debugName = "w9"
        w10.debugName = "w10"
        w11.debugName = "w11"
        w12.debugName = "w12"
        root.add(a)
        root.add(w1)
        root.add(w2)
        root.add(w3)
        root.add(w4)
        root.add(w5)
        root.add(w6)
        root.add(w7)
        root.add(w8)
        root.add(w9)
        root.add(w10)
        root.add(w11)
        root.add(w12)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        w1.connectCircularConstraint(a, 30f, 50)
        w2.connectCircularConstraint(a, 60f, 50)
        w3.connectCircularConstraint(a, 90f, 50)
        w4.connectCircularConstraint(a, 120f, 50)
        w5.connectCircularConstraint(a, 150f, 50)
        w6.connectCircularConstraint(a, 180f, 50)
        w7.connectCircularConstraint(a, 210f, 50)
        w8.connectCircularConstraint(a, 240f, 50)
        w9.connectCircularConstraint(a, 270f, 50)
        w10.connectCircularConstraint(a, 300f, 50)
        w11.connectCircularConstraint(a, 330f, 50)
        w12.connectCircularConstraint(a, 360f, 50)
        root.layout()
        println("w1: $w1")
        println("w2: $w2")
        println("w3: $w3")
        println("w4: $w4")
        println("w5: $w5")
        println("w6: $w6")
        println("w7: $w7")
        println("w8: $w8")
        println("w9: $w9")
        println("w10: $w10")
        println("w11: $w11")
        println("w12: $w12")
        assertEquals(w1.left, 520)
        assertEquals(w1.top, 252)
        assertEquals(w2.left, 538)
        assertEquals(w2.top, 270)
        assertEquals(w3.left, 545)
        assertEquals(w3.top, 295)
        assertEquals(w4.left, 538)
        assertEquals(w4.top, 320)
        assertEquals(w5.left, 520)
        assertEquals(w5.top, 338)
        assertEquals(w6.left, 495)
        assertEquals(w6.top, 345)
        assertEquals(w7.left, 470)
        assertEquals(w7.top, 338)
        assertEquals(w8.left, 452)
        assertEquals(w8.top, 320)
        assertEquals(w9.left, 445)
        assertEquals(w9.top, 295)
        assertEquals(w10.left, 452)
        assertEquals(w10.top, 270)
        assertEquals(w11.left, 470)
        assertEquals(w11.top, 252)
        assertEquals(w12.left, 495)
        assertEquals(w12.top, 245)
    }
}
