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
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test nested layout
 */
class NestedLayout {
    @Ignore
    @Test
    fun testNestedLayout() {
        val root = ConstraintWidgetContainer(20, 20, 1000, 1000)
        val container = ConstraintWidgetContainer(0, 0, 100, 100)
        root.debugName = "root"
        container.debugName = "container"
        container.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        container.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        root.add(container)
        root.layout()
        println("container: $container")
        assertEquals(container.left, 450)
        assertEquals(container.width, 100)
        val a = ConstraintWidget(0, 0, 100, 20)
        val b = ConstraintWidget(0, 0, 50, 20)
        container.add(a)
        container.add(b)
        container.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        a.connect(ConstraintAnchor.Type.LEFT, container, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.RIGHT, container, ConstraintAnchor.Type.RIGHT)
        root.layout()
        println("container: $container")
        println("A: $a")
        println("B: $b")
        assertEquals(container.width, 150)
        assertEquals(container.left, 425)
        assertEquals(a.left, 425)
        assertEquals(b.left, 525)
        assertEquals(a.width, 100)
        assertEquals(b.width, 50)
    }
}
