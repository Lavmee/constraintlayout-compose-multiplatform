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
import kotlin.math.max
import kotlin.math.min

class Barrier() : HelperWidget() {
    private var mBarrierType = LEFT

    private var mAllowsGoneWidget = true
    private var mMargin = 0
    private var mResolved = false

    constructor(debugName: String) : this() {
        this.debugName = (debugName)
    }

    override fun allowedInBarrier(): Boolean {
        return true
    }

    fun getBarrierType(): Int {
        return mBarrierType
    }

    fun setBarrierType(barrierType: Int) {
        mBarrierType = barrierType
    }

    fun setAllowsGoneWidget(allowsGoneWidget: Boolean) {
        mAllowsGoneWidget = allowsGoneWidget
    }

    /**
     * Find if this barrier supports gone widgets.
     *
     * @return true if this barrier supports gone widgets, otherwise false
     */
    @Deprecated(
        """This method should be called {@code getAllowsGoneWidget}
      such that {@code allowsGoneWidget}
      can be accessed as a property from Kotlin; {@see https://android.github
     * .io/kotlin-guides/interop.html#property-prefixes}.
      Use {@link #getAllowsGoneWidget()} instead.""",
    )
    fun allowsGoneWidget(): Boolean {
        return mAllowsGoneWidget
    }

    /**
     * Find if this barrier supports gone widgets.
     *
     * @return true if this barrier supports gone widgets, otherwise false
     */
    fun getAllowsGoneWidget(): Boolean {
        return mAllowsGoneWidget
    }

    override val isResolvedHorizontally: Boolean
        get() = mResolved

    override val isResolvedVertically: Boolean
        get() = mResolved

    override fun copy(src: ConstraintWidget, map: HashMap<ConstraintWidget?, ConstraintWidget?>) {
        super.copy(src, map)
        val srcBarrier = src as Barrier
        mBarrierType = srcBarrier.mBarrierType
        mAllowsGoneWidget = srcBarrier.mAllowsGoneWidget
        mMargin = srcBarrier.mMargin
    }

    override fun toString(): String {
        var debug = "[Barrier] " + debugName + " {"
        for (i in 0 until mWidgetsCount) {
            val widget = mWidgets[i]!!
            if (i > 0) {
                debug += ", "
            }
            debug += widget.debugName
        }
        debug += "}"
        return debug
    }

    internal fun markWidgets() {
        for (i in 0 until mWidgetsCount) {
            val widget = mWidgets[i]!!
            if (!mAllowsGoneWidget && !widget.allowedInBarrier()) {
                continue
            }
            if (mBarrierType == LEFT || mBarrierType == RIGHT) {
                widget.setInBarrier(HORIZONTAL, true)
            } else if (mBarrierType == TOP || mBarrierType == BOTTOM) {
                widget.setInBarrier(VERTICAL, true)
            }
        }
    }

    /**
     * Add this widget to the solver
     *
     * @param system   the solver we want to add the widget to
     * @param optimize true if [Optimizer.OPTIMIZATION_GRAPH] is on
     */
    override fun addToSolver(system: LinearSystem, optimize: Boolean) {
        if (LinearSystem.FULL_DEBUG) {
            println("\n----------------------------------------------")
            println("-- adding " + debugName + " to the solver")
            println("----------------------------------------------\n")
        }
        val position: ConstraintAnchor
        mListAnchors[LEFT] = mLeft
        mListAnchors[TOP] = mTop
        mListAnchors[RIGHT] = mRight
        mListAnchors[BOTTOM] = mBottom
        for (i in 0 until mListAnchors.size) {
            mListAnchors[i].mSolverVariable = system.createObjectVariable(mListAnchors[i])
        }
        position = if (mBarrierType in 0..3) {
            mListAnchors[mBarrierType]
        } else {
            return
        }
        if (USE_RESOLUTION) {
            if (!mResolved) {
                allSolved()
            }
            if (mResolved) {
                mResolved = false
                if (mBarrierType == LEFT || mBarrierType == RIGHT) {
                    system.addEquality(mLeft.mSolverVariable!!, mX)
                    system.addEquality(mRight.mSolverVariable!!, mX)
                } else if (mBarrierType == TOP || mBarrierType == BOTTOM) {
                    system.addEquality(mTop.mSolverVariable!!, mY)
                    system.addEquality(mBottom.mSolverVariable!!, mY)
                }
                return
            }
        }

        // We have to handle the case where some of the elements
        //  referenced in the barrier are set as
        // match_constraint; we have to take it in account to set the strength of the barrier.
        var hasMatchConstraintWidgets = false
        for (i in 0 until mWidgetsCount) {
            val widget = mWidgets[i]!!
            if (!mAllowsGoneWidget && !widget.allowedInBarrier()) {
                continue
            }
            if ((
                    (mBarrierType == LEFT || mBarrierType == RIGHT) &&
                        (
                            widget.horizontalDimensionBehaviour
                                == DimensionBehaviour.MATCH_CONSTRAINT
                            ) && widget.mLeft.mTarget != null
                    ) && widget.mRight.mTarget != null
            ) {
                hasMatchConstraintWidgets = true
                break
            } else if ((
                    (mBarrierType == TOP || mBarrierType == BOTTOM) &&
                        (
                            widget.verticalDimensionBehaviour
                                == DimensionBehaviour.MATCH_CONSTRAINT
                            ) && widget.mTop.mTarget != null
                    ) && widget.mBottom.mTarget != null
            ) {
                hasMatchConstraintWidgets = true
                break
            }
        }
        val mHasHorizontalCenteredDependents =
            mLeft.hasCenteredDependents() || mRight.hasCenteredDependents()
        val mHasVerticalCenteredDependents =
            mTop.hasCenteredDependents() || mBottom.hasCenteredDependents()
        val applyEqualityOnReferences = (
            !hasMatchConstraintWidgets &&
                (mBarrierType == LEFT && mHasHorizontalCenteredDependents || mBarrierType == TOP && mHasVerticalCenteredDependents || mBarrierType == RIGHT && mHasHorizontalCenteredDependents || mBarrierType == BOTTOM && mHasVerticalCenteredDependents)
            )
        var equalityOnReferencesStrength = SolverVariable.STRENGTH_EQUALITY
        if (!applyEqualityOnReferences) {
            equalityOnReferencesStrength = SolverVariable.STRENGTH_HIGHEST
        }
        for (i in 0 until mWidgetsCount) {
            val widget = mWidgets[i]!!
            if (!mAllowsGoneWidget && !widget.allowedInBarrier()) {
                continue
            }
            val target = system.createObjectVariable(widget.mListAnchors[mBarrierType])
            widget.mListAnchors[mBarrierType].mSolverVariable = target
            var margin = 0
            if (widget.mListAnchors[mBarrierType].mTarget != null &&
                widget.mListAnchors[mBarrierType].mTarget!!.mOwner == this
            ) {
                margin += widget.mListAnchors[mBarrierType].mMargin
            }
            if (mBarrierType == LEFT || mBarrierType == TOP) {
                system.addLowerBarrier(
                    position.mSolverVariable!!,
                    target!!,
                    mMargin - margin,
                    hasMatchConstraintWidgets,
                )
            } else {
                system.addGreaterBarrier(
                    position.mSolverVariable!!,
                    target!!,
                    mMargin + margin,
                    hasMatchConstraintWidgets,
                )
            }
            if (USE_RELAX_GONE) {
                if (widget.visibility != GONE || widget is Guideline || widget is Barrier) {
                    system.addEquality(
                        position.mSolverVariable!!,
                        target,
                        mMargin + margin,
                        equalityOnReferencesStrength,
                    )
                }
            } else {
                system.addEquality(
                    position.mSolverVariable!!,
                    target,
                    mMargin + margin,
                    equalityOnReferencesStrength,
                )
            }
        }
        val barrierParentStrength = SolverVariable.STRENGTH_HIGHEST
        val barrierParentStrengthOpposite = SolverVariable.STRENGTH_NONE
        when (mBarrierType) {
            LEFT -> {
                system.addEquality(
                    mRight.mSolverVariable!!,
                    mLeft.mSolverVariable!!,
                    0,
                    SolverVariable.STRENGTH_FIXED,
                )
                system.addEquality(
                    mLeft.mSolverVariable!!,
                    parent!!.mRight.mSolverVariable!!,
                    0,
                    barrierParentStrength,
                )
                system.addEquality(
                    mLeft.mSolverVariable!!,
                    parent!!.mLeft.mSolverVariable!!,
                    0,
                    barrierParentStrengthOpposite,
                )
            }
            RIGHT -> {
                system.addEquality(
                    mLeft.mSolverVariable!!,
                    mRight.mSolverVariable!!,
                    0,
                    SolverVariable.STRENGTH_FIXED,
                )
                system.addEquality(
                    mLeft.mSolverVariable!!,
                    parent!!.mLeft.mSolverVariable!!,
                    0,
                    barrierParentStrength,
                )
                system.addEquality(
                    mLeft.mSolverVariable!!,
                    parent!!.mRight.mSolverVariable!!,
                    0,
                    barrierParentStrengthOpposite,
                )
            }
            TOP -> {
                system.addEquality(
                    mBottom.mSolverVariable!!,
                    mTop.mSolverVariable!!,
                    0,
                    SolverVariable.STRENGTH_FIXED,
                )
                system.addEquality(
                    mTop.mSolverVariable!!,
                    parent!!.mBottom.mSolverVariable!!,
                    0,
                    barrierParentStrength,
                )
                system.addEquality(
                    mTop.mSolverVariable!!,
                    parent!!.mTop.mSolverVariable!!,
                    0,
                    barrierParentStrengthOpposite,
                )
            }
            BOTTOM -> {
                system.addEquality(
                    mTop.mSolverVariable!!,
                    mBottom.mSolverVariable!!,
                    0,
                    SolverVariable.STRENGTH_FIXED,
                )
                system.addEquality(
                    mTop.mSolverVariable!!,
                    parent!!.mTop.mSolverVariable!!,
                    0,
                    barrierParentStrength,
                )
                system.addEquality(
                    mTop.mSolverVariable!!,
                    parent!!.mBottom.mSolverVariable!!,
                    0,
                    barrierParentStrengthOpposite,
                )
            }
        }
    }

    fun setMargin(margin: Int) {
        mMargin = margin
    }

    fun getMargin(): Int {
        return mMargin
    }

    // @TODO: add description

    val orientation: Int get() {
        when (mBarrierType) {
            LEFT, RIGHT -> return HORIZONTAL
            TOP, BOTTOM -> return VERTICAL
        }
        return UNKNOWN
    }

    // @TODO: add description
    fun allSolved(): Boolean {
        if (!USE_RESOLUTION) {
            return false
        }
        var hasAllWidgetsResolved = true
        for (i in 0 until mWidgetsCount) {
            val widget = mWidgets[i]!!
            if (!mAllowsGoneWidget && !widget.allowedInBarrier()) {
                continue
            }
            if ((mBarrierType == LEFT || mBarrierType == RIGHT) &&
                !widget.isResolvedHorizontally
            ) {
                hasAllWidgetsResolved = false
            } else if ((mBarrierType == TOP || mBarrierType == BOTTOM) &&
                !widget.isResolvedVertically
            ) {
                hasAllWidgetsResolved = false
            }
        }
        if (hasAllWidgetsResolved && mWidgetsCount > 0) {
            // we're done!
            var barrierPosition = 0
            var initialized = false
            for (i in 0 until mWidgetsCount) {
                val widget = mWidgets[i]!!
                if (!mAllowsGoneWidget && !widget.allowedInBarrier()) {
                    continue
                }
                if (!initialized) {
                    when (mBarrierType) {
                        LEFT -> {
                            barrierPosition =
                                widget.getAnchor(ConstraintAnchor.Type.LEFT)!!.getFinalValue()
                        }
                        RIGHT -> {
                            barrierPosition =
                                widget.getAnchor(ConstraintAnchor.Type.RIGHT)!!.getFinalValue()
                        }
                        TOP -> {
                            barrierPosition =
                                widget.getAnchor(ConstraintAnchor.Type.TOP)!!.getFinalValue()
                        }
                        BOTTOM -> {
                            barrierPosition =
                                widget.getAnchor(ConstraintAnchor.Type.BOTTOM)!!.getFinalValue()
                        }
                    }
                    initialized = true
                }
                when (mBarrierType) {
                    LEFT -> {
                        barrierPosition = min(
                            barrierPosition,
                            widget.getAnchor(ConstraintAnchor.Type.LEFT)!!.getFinalValue(),
                        )
                    }
                    RIGHT -> {
                        barrierPosition = max(
                            barrierPosition,
                            widget.getAnchor(ConstraintAnchor.Type.RIGHT)!!.getFinalValue(),
                        )
                    }
                    TOP -> {
                        barrierPosition = min(
                            barrierPosition,
                            widget.getAnchor(ConstraintAnchor.Type.TOP)!!.getFinalValue(),
                        )
                    }
                    BOTTOM -> {
                        barrierPosition = max(
                            barrierPosition,
                            widget.getAnchor(ConstraintAnchor.Type.BOTTOM)!!.getFinalValue(),
                        )
                    }
                }
            }
            barrierPosition += mMargin
            if (mBarrierType == LEFT || mBarrierType == RIGHT) {
                setFinalHorizontal(barrierPosition, barrierPosition)
            } else {
                setFinalVertical(barrierPosition, barrierPosition)
            }
            if (LinearSystem.FULL_DEBUG) {
                println(
                    "*** BARRIER " + debugName +
                        " SOLVED TO " + barrierPosition + " ***",
                )
            }
            mResolved = true
            return true
        }
        return false
    }

    companion object {
        const val LEFT = 0
        const val RIGHT = 1
        const val TOP = 2
        const val BOTTOM = 3
        private const val USE_RESOLUTION = true
        private const val USE_RELAX_GONE = false
    }
}
