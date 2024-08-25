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
package tech.annexflow.constraintlayout.core

import tech.annexflow.constraintlayout.core.widgets.ConstraintAnchor
import tech.annexflow.constraintlayout.core.widgets.ConstraintWidget
import tech.annexflow.constraintlayout.core.widgets.ConstraintWidgetContainer
import kotlin.test.Test
import kotlin.test.assertEquals

class GuidelineTest {
    @Test
    fun testWrapGuideline() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val guidelineRight = tech.annexflow.constraintlayout.core.widgets.Guideline()
        guidelineRight.setOrientation(tech.annexflow.constraintlayout.core.widgets.Guideline.VERTICAL)
        val guidelineBottom = tech.annexflow.constraintlayout.core.widgets.Guideline()
        guidelineBottom.setOrientation(tech.annexflow.constraintlayout.core.widgets.Guideline.HORIZONTAL)
        guidelineRight.setGuidePercent(0.64f)
        guidelineBottom.setGuideEnd(60)
        root.debugName = "Root"
        a.debugName = "A"
        guidelineRight.debugName = "GuidelineRight"
        guidelineBottom.debugName = "GuidelineBottom"
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, guidelineRight, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.BOTTOM, guidelineBottom, ConstraintAnchor.Type.TOP)
        root.add(a)
        root.add(guidelineRight)
        root.add(guidelineBottom)
        root.layout()
        println(
            "a) root: " + root + " guideline right: " + guidelineRight +
                " guideline bottom: " + guidelineBottom + " A: " + a,
        )
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println(
            "b) root: " + root + " guideline right: " + guidelineRight +
                " guideline bottom: " + guidelineBottom + " A: " + a,
        )
        assertEquals(root.height, 80)
    }

    @Test
    fun testWrapGuideline2() {
        val root = ConstraintWidgetContainer(0, 0, 800, 600)
        val a = ConstraintWidget(100, 20)
        val guideline = tech.annexflow.constraintlayout.core.widgets.Guideline()
        guideline.setOrientation(tech.annexflow.constraintlayout.core.widgets.Guideline.VERTICAL)
        guideline.setGuideBegin(60)
        root.debugName = "Root"
        a.debugName = "A"
        guideline.debugName = "Guideline"
        a.connect(ConstraintAnchor.Type.LEFT, guideline, ConstraintAnchor.Type.LEFT, 5)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT, 5)
        a.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        root.add(a)
        root.add(guideline)
        //        root.layout();
        println("a) root: $root guideline: $guideline A: $a")
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("b) root: $root guideline: $guideline A: $a")
        assertEquals(root.width, 70)
    }
}
