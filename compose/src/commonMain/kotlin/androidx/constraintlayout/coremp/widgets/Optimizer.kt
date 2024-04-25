/*
 * Copyright (C) 2020 The Android Open Source Project
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

import androidx.constraintlayout.coremp.LinearSystem
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.DIMENSION_HORIZONTAL
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.DIMENSION_VERTICAL
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.UNKNOWN

class Optimizer {

    companion object {
        // Optimization levels (mask)
        const val OPTIMIZATION_NONE = 0
        const val OPTIMIZATION_DIRECT = 1
        const val OPTIMIZATION_BARRIER = 1 shl 1
        const val OPTIMIZATION_CHAIN = 1 shl 2
        const val OPTIMIZATION_DIMENSIONS = 1 shl 3
        const val OPTIMIZATION_RATIO = 1 shl 4
        const val OPTIMIZATION_GROUPS = 1 shl 5
        const val OPTIMIZATION_GRAPH = 1 shl 6
        const val OPTIMIZATION_GRAPH_WRAP = 1 shl 7
        const val OPTIMIZATION_CACHE_MEASURES = 1 shl 8
        const val OPTIMIZATION_DEPENDENCY_ORDERING = 1 shl 9
        const val OPTIMIZATION_GROUPING = 1 shl 10
        const val OPTIMIZATION_STANDARD = (OPTIMIZATION_DIRECT or OPTIMIZATION_CACHE_MEASURES)

        // Internal use.
        var sFlags = BooleanArray(3)
        const val FLAG_USE_OPTIMIZE = 0 // simple enough to use optimizer

        const val FLAG_CHAIN_DANGLING = 1
        const val FLAG_RECOMPUTE_BOUNDS = 2

        /**
         * Looks at optimizing match_parent
         */
        fun checkMatchParent(
            container: ConstraintWidgetContainer,
            system: LinearSystem,
            widget: ConstraintWidget,
        ) {
            widget.mHorizontalResolution = UNKNOWN
            widget.mVerticalResolution = UNKNOWN
            if (container.mListDimensionBehaviors[DIMENSION_HORIZONTAL]
                != ConstraintWidget.DimensionBehaviour.WRAP_CONTENT &&
                widget.mListDimensionBehaviors[DIMENSION_HORIZONTAL]
                == ConstraintWidget.DimensionBehaviour.MATCH_PARENT
            ) {
                val left = widget.mLeft.mMargin
                val right = container.width - widget.mRight.mMargin
                widget.mLeft.mSolverVariable = system.createObjectVariable(widget.mLeft)
                widget.mRight.mSolverVariable = system.createObjectVariable(widget.mRight)
                system.addEquality(widget.mLeft.mSolverVariable!!, left)
                system.addEquality(widget.mRight.mSolverVariable!!, right)
                widget.mHorizontalResolution = ConstraintWidget.DIRECT
                widget.setHorizontalDimension(left, right)
            }
            if (container.mListDimensionBehaviors[DIMENSION_VERTICAL]
                != ConstraintWidget.DimensionBehaviour.WRAP_CONTENT &&
                widget.mListDimensionBehaviors[DIMENSION_VERTICAL]
                == ConstraintWidget.DimensionBehaviour.MATCH_PARENT
            ) {
                val top = widget.mTop.mMargin
                val bottom = container.height - widget.mBottom.mMargin
                widget.mTop.mSolverVariable = system.createObjectVariable(widget.mTop)
                widget.mBottom.mSolverVariable = system.createObjectVariable(widget.mBottom)
                system.addEquality(widget.mTop.mSolverVariable!!, top)
                system.addEquality(widget.mBottom.mSolverVariable!!, bottom)
                if (widget.baselineDistance > 0 || widget.visibility == ConstraintWidget.GONE) {
                    widget.mBaseline.mSolverVariable = system.createObjectVariable(widget.mBaseline)
                    system.addEquality(
                        widget.mBaseline.mSolverVariable!!,
                        top + widget.baselineDistance,
                    )
                }
                widget.mVerticalResolution = ConstraintWidget.DIRECT
                widget.setVerticalDimension(top, bottom)
            }
        }

        // @TODO: add description
        fun enabled(optimizationLevel: Int, optimization: Int): Boolean {
            return optimizationLevel and optimization == optimization
        }
    }
}
