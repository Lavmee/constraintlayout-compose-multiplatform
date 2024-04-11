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

import androidx.constraintlayout.coremp.state.ConstraintReference
import androidx.constraintlayout.coremp.state.Dimension
import androidx.constraintlayout.coremp.state.State
import androidx.constraintlayout.coremp.widgets.ConstraintWidget
import androidx.constraintlayout.coremp.widgets.ConstraintWidgetContainer
import androidx.constraintlayout.coremp.widgets.Optimizer
import androidx.constraintlayout.coremp.widgets.analyzer.BasicMeasure
import kotlin.test.Test
import kotlin.test.assertEquals

class ComposeLayoutsTest {
    @Test
    fun dividerMatchTextHeight_inWrapConstraintLayout_longText() {
        val state = State()
        val parent: ConstraintReference? = state.constraints(State.PARENT)
        state.verticalGuideline("guideline")!!.percent(0.5f)
        state.constraints("box")!!
            .centerHorizontally(parent)
            .centerVertically(parent)
            .startToEnd("guideline")
            .width(Dimension.createSuggested(Dimension.WRAP_DIMENSION))
            .height(Dimension.createWrap())
            .setView("box")
        state.constraints("divider")!!
            .centerHorizontally(parent)
            .centerVertically(parent)
            .width(Dimension.createFixed(1))
            .height(Dimension.createPercent(0, 0.8f).suggested(0))
            .setView("divider")
        val root = ConstraintWidgetContainer(0, 0, 1080, 1977)
        state.apply(root)
        root.width = 1080
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.FIXED)
        val box: ConstraintWidget? = state.constraints("box")!!.getConstraintWidget()
        val guideline: ConstraintWidget = state.guideline(
            "guideline",
            ConstraintWidget.VERTICAL,
        )!!.getConstraintWidget()
        val divider: ConstraintWidget? = state.constraints("divider")!!.getConstraintWidget()
        root.debugName = "root"
        box!!.debugName = "box"
        guideline.debugName = "guideline"
        divider!!.debugName = "divider"
        root.measurer = sMeasurer
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.optimizationLevel = Optimizer.OPTIMIZATION_STANDARD
        // root.setOptimizationLevel(Optimizer.OPTIMIZATION_NONE);
        root.measure(root.optimizationLevel, 0, 0, 0, 0, 0, 0, 0, 0)
        println("root: $root")
        println("box: $box")
        println("guideline: $guideline")
        println("divider: $divider")
        assertEquals(root.width / 2, box.width)
        assertEquals(root.width / 2 / 2, box.height)
        assertEquals(1, divider.width)
        assertEquals((box.height * 0.8).toInt(), divider.height)
    }

    companion object {
        var sMeasurer: BasicMeasure.Measurer = object : BasicMeasure.Measurer {
            override fun measure(widget: ConstraintWidget, measure: BasicMeasure.Measure) {
                val horizontalBehavior: ConstraintWidget.DimensionBehaviour = measure.horizontalBehavior
                val verticalBehavior: ConstraintWidget.DimensionBehaviour = measure.verticalBehavior
                val horizontalDimension = measure.horizontalDimension
                val verticalDimension = measure.verticalDimension
                println(
                    "Measure (strategy : " + measure.measureStrategy + ") : " +
                        widget.companionWidget +
                        " " + horizontalBehavior + " (" + horizontalDimension + ") x " +
                        verticalBehavior + " (" + verticalDimension + ")",
                )
                if (horizontalBehavior === ConstraintWidget.DimensionBehaviour.FIXED) {
                    measure.measuredWidth = horizontalDimension
                } else if (horizontalBehavior === ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT) {
                    measure.measuredWidth = horizontalDimension
                    if (widget.companionWidget == "box" && measure.measureStrategy == BasicMeasure.Measure.SELF_DIMENSIONS) {
                        measure.measuredWidth = 1080
                    }
                } else if (horizontalBehavior === ConstraintWidget.DimensionBehaviour.WRAP_CONTENT) {
                    if (widget.companionWidget == "box") {
                        measure.measuredWidth = 1080
                    }
                }
                if (verticalBehavior === ConstraintWidget.DimensionBehaviour.FIXED) {
                    measure.measuredHeight = verticalDimension
                } else if (verticalBehavior === ConstraintWidget.DimensionBehaviour.WRAP_CONTENT) {
                    if (widget.companionWidget == "box") {
                        measure.measuredHeight = measure.measuredWidth / 2
                    }
                }
                println(
                    "Measure widget " + widget.companionWidget +
                        " => " + measure.measuredWidth + " x " + measure.measuredHeight,
                )
            }

            override fun didMeasures() {}
        }
    }
}
