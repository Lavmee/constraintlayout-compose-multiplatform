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
import androidx.constraintlayout.coremp.SolverVariable
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.DimensionBehaviour.WRAP_CONTENT

class Guideline : ConstraintWidget {

    protected var mRelativePercent = -1f
    protected var mRelativeBegin = -1
    protected var mRelativeEnd = -1
    protected var mGuidelineUseRtl = true

    private var mAnchor = mTop
    private var mOrientation = ConstraintWidget.HORIZONTAL
    private var mMinimumPosition = 0
    private var mResolved = false

    constructor() {
        mAnchors.clear()
        mAnchors.add(mAnchor)
        val count = mListAnchors.size
        for (i in 0 until count) {
            mListAnchors[i] = mAnchor
        }
    }

    override fun copy(src: ConstraintWidget, map: HashMap<ConstraintWidget?, ConstraintWidget?>) {
        super.copy(src, map)
        val srcGuideline = src as Guideline
        mRelativePercent = srcGuideline.mRelativePercent
        mRelativeBegin = srcGuideline.mRelativeBegin
        mRelativeEnd = srcGuideline.mRelativeEnd
        mGuidelineUseRtl = srcGuideline.mGuidelineUseRtl
        setOrientation(srcGuideline.mOrientation)
    }

    override fun allowedInBarrier(): Boolean {
        return true
    }

    // @TODO: add description
    fun getRelativeBehaviour(): Int {
        if (mRelativePercent != -1f) {
            return RELATIVE_PERCENT
        }
        if (mRelativeBegin != -1) {
            return RELATIVE_BEGIN
        }
        return if (mRelativeEnd != -1) {
            RELATIVE_END
        } else {
            RELATIVE_UNKNOWN
        }
    }

    // @TODO: add description
    fun setOrientation(orientation: Int) {
        if (mOrientation == orientation) {
            return
        }
        mOrientation = orientation
        mAnchors.clear()
        mAnchor = if (mOrientation == ConstraintWidget.VERTICAL) {
            mLeft
        } else {
            mTop
        }
        mAnchors.add(mAnchor)
        val count: Int = mListAnchors.size
        for (i in 0 until count) {
            mListAnchors[i] = mAnchor
        }
    }

    fun getAnchor(): ConstraintAnchor {
        return mAnchor
    }

    /**
     * Specify the xml type for the container
     */

    override var type: String? = "Guideline"

    /**
     * get the orientation VERTICAL or HORIZONTAL
     * @return orientation
     */

    val orientation: Int get() = mOrientation

    /**
     * set the minimum position
     * @param minimum
     */
    fun setMinimumPosition(minimum: Int) {
        mMinimumPosition = minimum
    }

    /**
     * Get the Minimum Position
     * @return the Minimum Position
     */
    fun getMinimumPosition(): Int {
        return mMinimumPosition
    }

    override fun getAnchor(anchorType: ConstraintAnchor.Type): ConstraintAnchor? {
        when (anchorType) {
            ConstraintAnchor.Type.LEFT, ConstraintAnchor.Type.RIGHT -> {
                if (mOrientation == ConstraintWidget.VERTICAL) {
                    return mAnchor
                }
            }

            ConstraintAnchor.Type.TOP, ConstraintAnchor.Type.BOTTOM -> {
                if (mOrientation == ConstraintWidget.HORIZONTAL) {
                    return mAnchor
                }
            }

            ConstraintAnchor.Type.BASELINE, ConstraintAnchor.Type.CENTER, ConstraintAnchor.Type.CENTER_X, ConstraintAnchor.Type.CENTER_Y, ConstraintAnchor.Type.NONE -> return null
        }
        return null
    }

    // @TODO: add description
    fun setGuidePercent(value: Int) {
        setGuidePercent(value / 100f)
    }

    // @TODO: add description
    fun setGuidePercent(value: Float) {
        if (value > -1) {
            mRelativePercent = value
            mRelativeBegin = -1
            mRelativeEnd = -1
        }
    }

    // @TODO: add description
    fun setGuideBegin(value: Int) {
        if (value > -1) {
            mRelativePercent = -1f
            mRelativeBegin = value
            mRelativeEnd = -1
        }
    }

    // @TODO: add description
    fun setGuideEnd(value: Int) {
        if (value > -1) {
            mRelativePercent = -1f
            mRelativeBegin = -1
            mRelativeEnd = value
        }
    }

    fun getRelativePercent(): Float {
        return mRelativePercent
    }

    fun getRelativeBegin(): Int {
        return mRelativeBegin
    }

    fun getRelativeEnd(): Int {
        return mRelativeEnd
    }

    // @TODO: add description
    fun setFinalValue(position: Int) {
        if (LinearSystem.FULL_DEBUG) {
            println(
                "*** SET FINAL GUIDELINE VALUE " +
                    position + " FOR " + debugName,
            )
        }
        mAnchor.setFinalValue(position)
        mResolved = true
    }

    override val isResolvedHorizontally: Boolean get() = mResolved

    override val isResolvedVertically: Boolean get() = mResolved

    override fun addToSolver(system: LinearSystem, optimize: Boolean) {
        if (LinearSystem.FULL_DEBUG) {
            println("\n----------------------------------------------")
            println("-- adding $debugName to the solver")
            println("----------------------------------------------\n")
        }
        val parent = this.parent as ConstraintWidgetContainer? ?: return
        var begin = parent.getAnchor(ConstraintAnchor.Type.LEFT)
        var end = parent.getAnchor(ConstraintAnchor.Type.RIGHT)
        var parentWrapContent =
            if (this.parent != null) this.parent!!.mListDimensionBehaviors[DIMENSION_HORIZONTAL] == WRAP_CONTENT else false
        if (mOrientation == ConstraintWidget.HORIZONTAL) {
            begin = parent.getAnchor(ConstraintAnchor.Type.TOP)
            end = parent.getAnchor(ConstraintAnchor.Type.BOTTOM)
            parentWrapContent =
                if (this.parent != null) this.parent!!.mListDimensionBehaviors[DIMENSION_VERTICAL] == WRAP_CONTENT else false
        }
        if (mResolved && mAnchor.hasFinalValue()) {
            val guide = system.createObjectVariable(mAnchor)
            if (LinearSystem.FULL_DEBUG) {
                println(
                    "*** SET FINAL POSITION FOR GUIDELINE " +
                        debugName + " TO " + mAnchor.getFinalValue(),
                )
            }
            system.addEquality((guide)!!, mAnchor.getFinalValue())
            if (mRelativeBegin != -1) {
                if (parentWrapContent) {
                    system.addGreaterThan(
                        (system.createObjectVariable(end))!!,
                        (guide),
                        0,
                        SolverVariable.STRENGTH_EQUALITY,
                    )
                }
            } else if (mRelativeEnd != -1) {
                if (parentWrapContent) {
                    val parentRight = system.createObjectVariable(end)
                    system.addGreaterThan(
                        (guide),
                        (system.createObjectVariable(begin))!!,
                        0,
                        SolverVariable.STRENGTH_EQUALITY,
                    )
                    system.addGreaterThan(
                        (parentRight)!!,
                        (guide),
                        0,
                        SolverVariable.STRENGTH_EQUALITY,
                    )
                }
            }
            mResolved = false
            return
        }
        if (mRelativeBegin != -1) {
            val guide = system.createObjectVariable(mAnchor)
            val parentLeft = system.createObjectVariable(begin)
            system.addEquality(
                (guide)!!,
                (parentLeft)!!,
                mRelativeBegin,
                SolverVariable.STRENGTH_FIXED,
            )
            if (parentWrapContent) {
                system.addGreaterThan(
                    (system.createObjectVariable(end))!!,
                    (guide),
                    0,
                    SolverVariable.STRENGTH_EQUALITY,
                )
            }
        } else if (mRelativeEnd != -1) {
            val guide = system.createObjectVariable(mAnchor)
            val parentRight = system.createObjectVariable(end)
            system.addEquality(
                (guide)!!,
                (parentRight)!!,
                -mRelativeEnd,
                SolverVariable.STRENGTH_FIXED,
            )
            if (parentWrapContent) {
                system.addGreaterThan(
                    (guide),
                    (system.createObjectVariable(begin))!!,
                    0,
                    SolverVariable.STRENGTH_EQUALITY,
                )
                system.addGreaterThan(
                    (parentRight),
                    (guide),
                    0,
                    SolverVariable.STRENGTH_EQUALITY,
                )
            }
        } else if (mRelativePercent != -1f) {
            val guide = system.createObjectVariable(mAnchor)
            val parentRight = system.createObjectVariable(end)
            system.addConstraint(
                LinearSystem
                    .createRowDimensionPercent(
                        system,
                        (guide)!!,
                        (parentRight)!!,
                        mRelativePercent,
                    ),
            )
        }
    }

    override fun updateFromSolver(system: LinearSystem, optimize: Boolean) {
        if (parent == null) {
            return
        }
        val value = system.getObjectVariableValue(mAnchor)
        if (mOrientation == ConstraintWidget.VERTICAL) {
            setX(value)
            setY(0)
            height = parent!!.height
            width = 0
        } else {
            setX(0)
            setY(value)
            width = parent!!.width
            height = 0
        }
    }

    fun inferRelativePercentPosition() {
        var percent = x / parent!!.width.toFloat()
        if (mOrientation == ConstraintWidget.HORIZONTAL) {
            percent = y / parent!!.height.toFloat()
        }
        setGuidePercent(percent)
    }

    fun inferRelativeBeginPosition() {
        var position = x
        if (mOrientation == ConstraintWidget.HORIZONTAL) {
            position = y
        }
        setGuideBegin(position)
    }

    fun inferRelativeEndPosition() {
        var position = parent!!.width - x
        if (mOrientation == ConstraintWidget.HORIZONTAL) {
            position = parent!!.height - y
        }
        setGuideEnd(position)
    }

    // @TODO: add description
    fun cyclePosition() {
        if (mRelativeBegin != -1) {
            // cycle to percent-based position
            inferRelativePercentPosition()
        } else if (mRelativePercent != -1f) {
            // cycle to end-based position
            inferRelativeEndPosition()
        } else if (mRelativeEnd != -1) {
            // cycle to begin-based position
            inferRelativeBeginPosition()
        }
    }

    fun isPercent(): Boolean {
        return mRelativePercent != -1f && mRelativeBegin == -1 && mRelativeEnd == -1
    }

    companion object {
        const val HORIZONTAL = 0
        const val VERTICAL = 1

        const val RELATIVE_PERCENT = 0
        const val RELATIVE_BEGIN = 1
        const val RELATIVE_END = 2
        const val RELATIVE_UNKNOWN = -1
    }
}
