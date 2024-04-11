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
package androidx.constraintlayout.coremp.state

import androidx.constraintlayout.coremp.widgets.ConstraintWidget
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.MATCH_CONSTRAINT_PERCENT
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.MATCH_CONSTRAINT_WRAP

class Dimension {

    private val mWrapContent = -2

    var mMin = 0
    var mMax = Int.MAX_VALUE
    var mPercent = 1f
    var mValue = 0
    var mRatioString: String? = null
    var mInitialValue: Any? = WRAP_DIMENSION
    var mIsSuggested = false

    /**
     * Returns true if the dimension is a fixed dimension of
     * the same given value
     */
    fun equalsFixedValue(value: Int): Boolean {
        return (
            mInitialValue == null &&
                mValue == value
            )
    }

    enum class Type {
        FIXED,
        WRAP,
        MATCH_PARENT,
        MATCH_CONSTRAINT,
    }

    constructor()

    constructor(type: Any) {
        mInitialValue = type
    }

    // @TODO: add description
    fun percent(key: Any?, value: Float): Dimension {
        mPercent = value
        return this
    }

    // @TODO: add description
    fun min(value: Int): Dimension {
        if (value >= 0) {
            mMin = value
        }
        return this
    }

    // @TODO: add description
    fun min(value: Any): Dimension {
        if (value == WRAP_DIMENSION) {
            mMin = mWrapContent
        }
        return this
    }

    // @TODO: add description
    fun max(value: Int): Dimension {
        if (mMax >= 0) {
            mMax = value
        }
        return this
    }

    // @TODO: add description
    fun max(value: Any): Dimension {
        if (value == WRAP_DIMENSION && mIsSuggested) {
            mInitialValue = WRAP_DIMENSION
            mMax = Int.MAX_VALUE
        }
        return this
    }

    // @TODO: add description
    fun suggested(value: Int): Dimension {
        mIsSuggested = true
        if (value >= 0) {
            mMax = value
        }
        return this
    }

    // @TODO: add description
    fun suggested(value: Any): Dimension {
        mInitialValue = value
        mIsSuggested = true
        return this
    }

    // @TODO: add description
    fun fixed(value: Any): Dimension {
        mInitialValue = value
        if (value is Int) {
            mValue = value
            mInitialValue = null
        }
        return this
    }

    // @TODO: add description
    fun fixed(value: Int): Dimension {
        mInitialValue = null
        mValue = value
        return this
    }

    // @TODO: add description
    fun ratio(ratio: String): Dimension { // WxH ratio
        mRatioString = ratio
        return this
    }

    fun setValue(value: Int) {
        mIsSuggested = false // fixed value
        mInitialValue = null
        mValue = value
    }

    fun getValue(): Int {
        return mValue
    }

    /**
     * Apply the dimension to the given constraint widget
     */
    fun apply(state: State?, constraintWidget: ConstraintWidget, orientation: Int) {
        if (mRatioString != null) {
            constraintWidget.setDimensionRatio(mRatioString)
        }
        if (orientation == ConstraintWidget.HORIZONTAL) {
            if (mIsSuggested) {
                constraintWidget.setHorizontalDimensionBehaviour(
                    ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT,
                )
                var type: Int = MATCH_CONSTRAINT_SPREAD
                if (mInitialValue == WRAP_DIMENSION) {
                    type = MATCH_CONSTRAINT_WRAP
                } else if (mInitialValue == PERCENT_DIMENSION) {
                    type = MATCH_CONSTRAINT_PERCENT
                }
                constraintWidget.setHorizontalMatchStyle(type, mMin, mMax, mPercent)
            } else { // fixed
                if (mMin > 0) {
                    constraintWidget.setMinWidth(mMin)
                }
                if (mMax < Int.MAX_VALUE) {
                    constraintWidget.maxWidth = mMax
                }
                when (mInitialValue) {
                    WRAP_DIMENSION -> {
                        constraintWidget.setHorizontalDimensionBehaviour(
                            ConstraintWidget.DimensionBehaviour.WRAP_CONTENT,
                        )
                    }
                    PARENT_DIMENSION -> {
                        constraintWidget.setHorizontalDimensionBehaviour(
                            ConstraintWidget.DimensionBehaviour.MATCH_PARENT,
                        )
                    }
                    null -> {
                        constraintWidget.setHorizontalDimensionBehaviour(
                            ConstraintWidget.DimensionBehaviour.FIXED,
                        )
                        constraintWidget.width = (mValue)
                    }
                }
            }
        } else {
            if (mIsSuggested) {
                constraintWidget.setVerticalDimensionBehaviour(
                    ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT,
                )
                var type: Int = MATCH_CONSTRAINT_SPREAD
                if (mInitialValue == WRAP_DIMENSION) {
                    type = MATCH_CONSTRAINT_WRAP
                } else if (mInitialValue == PERCENT_DIMENSION) {
                    type = MATCH_CONSTRAINT_PERCENT
                }
                constraintWidget.setVerticalMatchStyle(type, mMin, mMax, mPercent)
            } else { // fixed
                if (mMin > 0) {
                    constraintWidget.setMinHeight(mMin)
                }
                if (mMax < Int.MAX_VALUE) {
                    constraintWidget.maxHeight = mMax
                }
                when (mInitialValue) {
                    WRAP_DIMENSION -> {
                        constraintWidget.setVerticalDimensionBehaviour(
                            ConstraintWidget.DimensionBehaviour.WRAP_CONTENT,
                        )
                    }
                    PARENT_DIMENSION -> {
                        constraintWidget.setVerticalDimensionBehaviour(
                            ConstraintWidget.DimensionBehaviour.MATCH_PARENT,
                        )
                    }
                    null -> {
                        constraintWidget.setVerticalDimensionBehaviour(
                            ConstraintWidget.DimensionBehaviour.FIXED,
                        )
                        constraintWidget.height = (mValue)
                    }
                }
            }
        }
    }

    companion object {
        const val FIXED_DIMENSION: String = "FIXED_DIMENSION"
        const val WRAP_DIMENSION: String = "WRAP_DIMENSION"
        const val SPREAD_DIMENSION: String = "SPREAD_DIMENSION"
        const val PARENT_DIMENSION: String = "PARENT_DIMENSION"
        const val PERCENT_DIMENSION: String = "PERCENT_DIMENSION"
        const val RATIO_DIMENSION: String = "RATIO_DIMENSION"

        // @TODO: add description
        fun createSuggested(value: Int): Dimension {
            val dimension = Dimension()
            dimension.suggested(value)
            return dimension
        }

        // @TODO: add description
        fun createSuggested(startValue: Any): Dimension {
            val dimension = Dimension()
            dimension.suggested(startValue)
            return dimension
        }

        // @TODO: add description
        fun createFixed(value: Int): Dimension {
            val dimension = Dimension(FIXED_DIMENSION)
            dimension.fixed(value)
            return dimension
        }

        // @TODO: add description
        fun createFixed(value: Any): Dimension {
            val dimension = Dimension(FIXED_DIMENSION)
            dimension.fixed(value)
            return dimension
        }

        // @TODO: add description
        fun createPercent(key: Any?, value: Float): Dimension {
            val dimension = Dimension(PERCENT_DIMENSION)
            dimension.percent(key, value)
            return dimension
        }

        // @TODO: add description
        fun createParent(): Dimension {
            return Dimension(PARENT_DIMENSION)
        }

        // @TODO: add description
        fun createWrap(): Dimension {
            return Dimension(WRAP_DIMENSION)
        }

        // @TODO: add description
        fun createSpread(): Dimension {
            return Dimension(SPREAD_DIMENSION)
        }

        // @TODO: add description
        fun createRatio(ratio: String): Dimension {
            val dimension = Dimension(RATIO_DIMENSION)
            dimension.ratio(ratio)
            return dimension
        }
    }
}
