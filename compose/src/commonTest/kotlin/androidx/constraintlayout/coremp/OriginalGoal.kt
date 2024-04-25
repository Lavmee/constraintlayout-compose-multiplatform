/*
 * Copyright (C) 2015 The Android Open Source Project
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

/**
 * Represents a goal to minimize
 */
open class OriginalGoal {
    inner class GoalElement {
        var mStrengths = FloatArray(sMax)
        var mVariable: SolverVariable? = null
        fun clearStrengths() {
            for (i in 0 until sMax) {
                mStrengths[i] = 0f
            }
        }

        override fun toString(): String {
            var representation = mVariable.toString() + "["
            for (j in mStrengths.indices) {
                representation += mStrengths[j]
                representation += if (j < mStrengths.size - 1) {
                    ", "
                } else {
                    "] "
                }
            }
            return representation
        }
    }

    var mVariables: ArrayList<GoalElement> = ArrayList<GoalElement>()
    open val pivotCandidate: SolverVariable?
        get() {
            val count: Int = mVariables.size
            var candidate: SolverVariable? = null
            var strength = 0
            for (i in 0 until count) {
                val element: GoalElement = mVariables.get(i)
                //            System.out.println("get pivot, looking at " + element);
                for (k in sMax - 1 downTo 0) {
                    val value = element.mStrengths[k]
                    if (candidate == null && value < 0 && k >= strength) {
                        strength = k
                        candidate = element.mVariable
                        //                    System.out.println("-> k: " + k + " strength: "
//                      + strength + " v: " + value + " candidate " + candidate);
                    }
                    if (value > 0 && k > strength) {
//                    System.out.println("-> reset, k: " + k + " strength: "
//                      + strength + " v: " + value + " candidate " + candidate);
                        strength = k
                        candidate = null
                    }
                }
            }
            return candidate
        }

    open fun updateFromSystemErrors(system: LinearSystem) {
        for (i in 1 until system.mNumColumns) {
            val variable = system.mCache.mIndexedVariables[i]!!
            if (variable.mType !== SolverVariable.Type.ERROR) {
                continue
            }
            val element: GoalElement = GoalElement()
            element.mVariable = variable
            element.mStrengths[variable.strength] = 1f
            mVariables.add(element)
        }
    }

    open fun updateFromSystem(system: LinearSystem) {
        mVariables.clear()
        updateFromSystemErrors(system)
        val count: Int = mVariables.size
        for (i in 0 until count) {
            val element: GoalElement = mVariables.get(i)
            if (element.mVariable!!.mDefinitionId != -1) {
                val definition = system.getRow(element.mVariable!!.mDefinitionId)
                val variables = definition!!.variables as ArrayLinkedVariables?
                val size = variables!!.mCurrentSize
                for (j in 0 until size) {
                    val `var` = variables.getVariable(j)
                    val value = variables.getVariableValue(j)
                    add(element, `var`, value)
                }
                element.clearStrengths()
            }
        }
    }

    fun getElement(variable: SolverVariable?): GoalElement {
        val count: Int = mVariables.size
        for (i in 0 until count) {
            val element: GoalElement = mVariables.get(i)
            if (element.mVariable == variable) {
                return element
            }
        }
        val element: GoalElement = GoalElement()
        element.mVariable = variable
        element.mStrengths[variable!!.strength] = 1f
        mVariables.add(element)
        return element
    }

    fun add(element: GoalElement, variable: SolverVariable?, value: Float) {
        val addition = getElement(variable)
        for (i in 0 until sMax) {
            addition.mStrengths[i] += element.mStrengths[i] * value
        }
    }

    override fun toString(): String {
        var representation = "OriginalGoal: "
        val count: Int = mVariables.size
        for (i in 0 until count) {
            val element: GoalElement = mVariables.get(i)
            representation += element.mVariable.toString() + "["
            for (j in element.mStrengths.indices) {
                representation += element.mStrengths[j]
                representation += if (j < element.mStrengths.size - 1) {
                    ", "
                } else {
                    "], "
                }
            }
        }
        return representation
    }

    companion object {
        var sMax = 6
    }
}
