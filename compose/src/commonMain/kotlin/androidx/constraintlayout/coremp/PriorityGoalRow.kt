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
package androidx.constraintlayout.coremp

import kotlin.math.abs

class PriorityGoalRow(var mCache: Cache) : ArrayRow(mCache) {

    private val mTableSize = 128
    private var mArrayGoals = arrayOfNulls<SolverVariable>(mTableSize)
    private var mSortArray = arrayOfNulls<SolverVariable>(mTableSize)
    private var mNumGoals = 0
    private var mAccessor: GoalVariableAccessor = GoalVariableAccessor(this)

    inner class GoalVariableAccessor(row: PriorityGoalRow) {
        private var mVariable: SolverVariable? = null

        @Suppress("unused")
        private var mRow: PriorityGoalRow? = row

        fun init(variable: SolverVariable?) {
            mVariable = variable
        }

        fun addToGoal(other: SolverVariable, value: Float): Boolean {
            if (mVariable!!.inGoal) {
                var empty = true
                for (i in 0 until SolverVariable.MAX_STRENGTH) {
                    mVariable!!.mGoalStrengthVector[i] += other.mGoalStrengthVector[i] * value
                    val v = mVariable!!.mGoalStrengthVector[i]
                    if (abs(v) < EPSILON) {
                        mVariable!!.mGoalStrengthVector[i] = 0f
                    } else {
                        empty = false
                    }
                }
                if (empty) {
                    removeGoal(mVariable!!)
                }
            } else {
                for (i in 0 until SolverVariable.MAX_STRENGTH) {
                    val strength = other.mGoalStrengthVector[i]
                    if (strength != 0f) {
                        var v = value * strength
                        if (abs(v) < EPSILON) {
                            v = 0f
                        }
                        mVariable!!.mGoalStrengthVector[i] = v
                    } else {
                        mVariable!!.mGoalStrengthVector[i] = 0f
                    }
                }
                return true
            }
            return false
        }

        fun add(other: SolverVariable) {
            for (i in 0 until SolverVariable.MAX_STRENGTH) {
                mVariable!!.mGoalStrengthVector[i] += other.mGoalStrengthVector[i]
                val value = mVariable!!.mGoalStrengthVector[i]
                if (abs(value) < EPSILON) {
                    mVariable!!.mGoalStrengthVector[i] = 0f
                }
            }
        }

        val isNegative: Boolean
            get() {
                for (i in SolverVariable.MAX_STRENGTH - 1 downTo 0) {
                    val value = mVariable!!.mGoalStrengthVector[i]
                    if (value > 0) {
                        return false
                    }
                    if (value < 0) {
                        return true
                    }
                }
                return false
            }

        fun isSmallerThan(other: SolverVariable): Boolean {
            for (i in SolverVariable.MAX_STRENGTH - 1 downTo 0) {
                val comparedValue = other.mGoalStrengthVector[i]
                val value = mVariable!!.mGoalStrengthVector[i]
                if (value == comparedValue) {
                    continue
                }
                return value < comparedValue
            }
            return false
        }

        @Suppress("unused")
        val isNull: Boolean
            get() {
                for (i in 0 until SolverVariable.MAX_STRENGTH) {
                    if (mVariable!!.mGoalStrengthVector[i] != 0f) {
                        return false
                    }
                }
                return true
            }

        fun reset() {
            mVariable!!.mGoalStrengthVector.fill(0f)
        }

        override fun toString(): String {
            var result = "[ "
            if (mVariable != null) {
                for (i in 0 until SolverVariable.MAX_STRENGTH) {
                    result += mVariable!!.mGoalStrengthVector[i].toString() + " "
                }
            }
            result += "] $mVariable"
            return result
        }
    }

    override fun clear() {
        mNumGoals = 0
        mConstantValue = 0f
    }

    override val isEmpty: Boolean get() = mNumGoals == 0

    override fun getPivotCandidate(system: LinearSystem?, avoid: BooleanArray?): SolverVariable? {
        var pivot = NOT_FOUND
        for (i in 0 until mNumGoals) {
            val variable = mArrayGoals[i]!!
            if (avoid!![variable.id]) {
                continue
            }
            mAccessor.init(variable)
            if (pivot == NOT_FOUND) {
                if (mAccessor.isNegative) {
                    pivot = i
                }
            } else if (mAccessor.isSmallerThan(mArrayGoals[pivot]!!)) {
                pivot = i
            }
        }
        return if (pivot == NOT_FOUND) {
            null
        } else {
            mArrayGoals[pivot]
        }
    }

    override fun addError(error: SolverVariable?) {
        mAccessor.init(error)
        mAccessor.reset()
        error!!.mGoalStrengthVector[error.strength] = 1f
        addToGoal(error)
    }

    private fun addToGoal(variable: SolverVariable) {
        if (mNumGoals + 1 > mArrayGoals.size) {
            mArrayGoals = mArrayGoals.copyOf(mArrayGoals.size * 2)
            mSortArray = mArrayGoals.copyOf(mArrayGoals.size * 2)
        }
        mArrayGoals[mNumGoals] = variable
        mNumGoals++
        if (mNumGoals > 1 && mArrayGoals[mNumGoals - 1]!!.id > variable.id) {
            for (i in 0 until mNumGoals) {
                mSortArray[i] = mArrayGoals[i]
            }
            mSortArray.filterNotNull().toTypedArray().sort(fromIndex = 0, toIndex = mNumGoals)
            for (i in 0 until mNumGoals) {
                mArrayGoals[i] = mSortArray[i]
            }
        }
        variable.inGoal = true
        variable.addToRow(this)
    }

    private fun removeGoal(variable: SolverVariable) {
        for (i in 0 until mNumGoals) {
            if (mArrayGoals[i] == variable) {
                for (j in i until mNumGoals - 1) {
                    mArrayGoals[j] = mArrayGoals[j + 1]
                }
                mNumGoals--
                variable.inGoal = false
                return
            }
        }
    }

    override fun updateFromRow(
        system: LinearSystem?,
        definition: ArrayRow?,
        removeFromDefinition: Boolean,
    ) {
        val goalVariable = definition!!.mVariable ?: return
        val rowVariables = definition.variables
        val currentSize = rowVariables!!.currentSize
        for (i in 0 until currentSize) {
            val solverVariable = rowVariables.getVariable(i)
            val value = rowVariables.getVariableValue(i)
            mAccessor.init(solverVariable)
            if (mAccessor.addToGoal(goalVariable, value)) {
                addToGoal(solverVariable!!)
            }
            mConstantValue += definition.mConstantValue * value
        }
        removeGoal(goalVariable)
    }

    override fun toString(): String {
        var result = ""
        result += " goal -> ($mConstantValue) : "
        for (i in 0 until mNumGoals) {
            val v = mArrayGoals[i]!!
            mAccessor.init(v)
            result += "$mAccessor "
        }
        return result
    }

    companion object {
        private const val EPSILON = 0.0001f

        @Suppress("unused")
        private val DEBUG = false

        const val NOT_FOUND = -1
    }
}
