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
 * Store a set of variables and their values in an array.
 */
internal class ArrayBackedVariables(arrayRow: ArrayRow?, cache: Cache?) {
    private var mVariables: Array<SolverVariable?>? = null
    private var mValues: FloatArray? = null
    private var mIndexes: IntArray? = null
    private val mThreshold = 16
    private var mMaxSize = 4
    private var mCurrentSize = 0
    private var mCurrentWriteSize = 0
    private var mCandidate: SolverVariable? = null

    init {
        mVariables = arrayOfNulls(mMaxSize)
        mValues = FloatArray(mMaxSize)
        mIndexes = IntArray(mMaxSize)
    }

    val pivotCandidate: SolverVariable?
        get() {
            if (mCandidate == null) {
                for (i in 0 until mCurrentSize) {
                    val idx = mIndexes!![i]
                    if (mValues!![idx] < 0) {
                        mCandidate = mVariables!![idx]
                        break
                    }
                }
            }
            return mCandidate
        }

    fun increaseSize() {
        mMaxSize *= 2
        mVariables = mVariables!!.copyOf(mMaxSize)
        mValues = mValues!!.copyOf(mMaxSize)
        mIndexes = mIndexes!!.copyOf(mMaxSize)
    }

    fun size(): Int {
        return mCurrentSize
    }

    fun getVariable(index: Int): SolverVariable? {
        return mVariables!![mIndexes!![index]]
    }

    fun getVariableValue(index: Int): Float {
        return mValues!![mIndexes!![index]]
    }

    fun updateArray(target: ArrayBackedVariables, amount: Float) {
        if (amount == 0f) {
            return
        }
        for (i in 0 until mCurrentSize) {
            val idx = mIndexes!![i]
            val v = mVariables!![idx]
            val value = mValues!![idx]
            target.add(v, value * amount)
        }
    }

    fun setVariable(index: Int, value: Float) {
        val idx = mIndexes!![index]
        mValues!![idx] = value
        if (value < 0) {
            mCandidate = mVariables!![idx]
        }
    }

    operator fun get(v: SolverVariable): Float {
        if (mCurrentSize < mThreshold) {
            for (i in 0 until mCurrentSize) {
                val idx = mIndexes!![i]
                if (mVariables!![idx] == v) {
                    return mValues!![idx]
                }
            }
        } else {
            var start = 0
            var end = mCurrentSize - 1
            while (start <= end) {
                val index = start + (end - start) / 2
                val idx = mIndexes!![index]
                val current = mVariables!![idx]
                if (current == v) {
                    return mValues!![idx]
                } else if (current!!.id < v.id) {
                    start = index + 1
                } else {
                    end = index - 1
                }
            }
        }
        return 0f
    }

    fun put(variable: SolverVariable, value: Float) {
        if (value == 0f) {
            remove(variable)
            return
        }
        while (true) {
            var firstEmptyIndex = -1
            for (i in 0 until mCurrentWriteSize) {
                if (mVariables!![i] == variable) {
                    mValues!![i] = value
                    if (value < 0) {
                        mCandidate = variable
                    }
                    return
                }
                if (firstEmptyIndex == -1 && mVariables!![i] == null) {
                    firstEmptyIndex = i
                }
            }
            if (firstEmptyIndex == -1 && mCurrentWriteSize < mMaxSize) {
                firstEmptyIndex = mCurrentWriteSize
            }
            if (firstEmptyIndex != -1) {
                mVariables!![firstEmptyIndex] = variable
                mValues!![firstEmptyIndex] = value
                // insert the position...
                var inserted = false
                for (j in 0 until mCurrentSize) {
                    val index = mIndexes!![j]
                    if (mVariables!![index]!!.id > variable.id) {
                        // this is our insertion point
                        mIndexes!!.copyInto(mIndexes!!, startIndex = j, destinationOffset = j + 1, endIndex = mCurrentSize)
                        mIndexes!![j] = firstEmptyIndex
                        inserted = true
                        break
                    }
                }
                if (!inserted) {
                    mIndexes!![mCurrentSize] = firstEmptyIndex
                }
                mCurrentSize++
                if (firstEmptyIndex + 1 > mCurrentWriteSize) {
                    mCurrentWriteSize = firstEmptyIndex + 1
                }
                if (value < 0) {
                    mCandidate = variable
                }
                return
            } else {
                increaseSize()
            }
        }
    }

    fun add(variable: SolverVariable?, value: Float) {
        if (value == 0f) {
            return
        }
        while (true) {
            var firstEmptyIndex = -1
            for (i in 0 until mCurrentWriteSize) {
                if (mVariables!![i] == variable) {
                    mValues!![i] += value
                    if (value < 0) {
                        mCandidate = variable
                    }
                    if (mValues!![i] == 0f) {
                        remove(variable)
                    }
                    return
                }
                if (firstEmptyIndex == -1 && mVariables!![i] == null) {
                    firstEmptyIndex = i
                }
            }
            if (firstEmptyIndex == -1 && mCurrentWriteSize < mMaxSize) {
                firstEmptyIndex = mCurrentWriteSize
            }
            if (firstEmptyIndex != -1) {
                mVariables!![firstEmptyIndex] = variable
                mValues!![firstEmptyIndex] = value
                // insert the position...
                var inserted = false
                for (j in 0 until mCurrentSize) {
                    val index = mIndexes!![j]
                    if (mVariables!![index]!!.id > variable!!.id) {
                        // this is our insertion point
                        mIndexes!!.copyInto(mIndexes!!, startIndex = j, destinationOffset = j + 1, endIndex = mCurrentSize)
                        mIndexes!![j] = firstEmptyIndex
                        inserted = true
                        break
                    }
                }
                if (!inserted) {
                    mIndexes!![mCurrentSize] = firstEmptyIndex
                }
                mCurrentSize++
                if (firstEmptyIndex + 1 > mCurrentWriteSize) {
                    mCurrentWriteSize = firstEmptyIndex + 1
                }
                if (value < 0) {
                    mCandidate = variable
                }
                return
            } else {
                increaseSize()
            }
        }
    }

    fun clear() {
        var i = 0
        val length = mVariables!!.size
        while (i < length) {
            mVariables!![i] = null
            i++
        }
        mCurrentSize = 0
        mCurrentWriteSize = 0
    }

    fun containsKey(variable: SolverVariable): Boolean {
        if (mCurrentSize < 8) {
            for (i in 0 until mCurrentSize) {
                if (mVariables!![mIndexes!![i]] == variable) {
                    return true
                }
            }
        } else {
            var start = 0
            var end = mCurrentSize - 1
            while (start <= end) {
                val index = start + (end - start) / 2
                val current = mVariables!![mIndexes!![index]]
                if (current == variable) {
                    return true
                } else if (current!!.id < variable.id) {
                    start = index + 1
                } else {
                    end = index - 1
                }
            }
        }
        return false
    }

    fun remove(variable: SolverVariable?): Float {
        if (DEBUG) {
            print("BEFORE REMOVE $variable -> ")
            display()
        }
        if (mCandidate == variable) {
            mCandidate = null
        }
        for (i in 0 until mCurrentWriteSize) {
            val idx = mIndexes!![i]
            if (mVariables!![idx] == variable) {
                val amount = mValues!![idx]
                mVariables!![idx] = null
                mIndexes!!.copyInto(mIndexes!!, startIndex = i + 1, destinationOffset = i, endIndex = mCurrentWriteSize - 1)
                mCurrentSize--
                if (DEBUG) {
                    print("AFTER REMOVE ")
                    display()
                }
                return amount
            }
        }
        return 0f
    }

    fun sizeInBytes(): Int {
        var size = 0
        size += mMaxSize * 4
        size += mMaxSize * 4
        size += mMaxSize * 4
        size += 4 + 4 + 4 + 4
        return size
    }

    fun display() {
        val count = size()
        print("{ ")
        for (i in 0 until count) {
            print(getVariable(i).toString() + " = " + getVariableValue(i) + " ")
        }
        println(" }")
    }

    private val internalArrays: String
        private get() {
            var str = ""
            val count = size()
            str += "idx { "
            for (i in 0 until count) {
                str += mIndexes!![i].toString() + " "
            }
            str += "}\n"
            str += "obj { "
            for (i in 0 until count) {
                str += mVariables!![i].toString() + ":" + mValues!![i] + " "
            }
            str += "}\n"
            return str
        }

    fun displayInternalArrays() {
        val count = size()
        print("idx { ")
        for (i in 0 until count) {
            print(mIndexes!![i].toString() + " ")
        }
        println("}")
        print("obj { ")
        for (i in 0 until count) {
            print(mVariables!![i].toString() + ":" + mValues!![i] + " ")
        }
        println("}")
    }

    fun updateFromRow(arrayRow: ArrayRow?, definition: ArrayRow?) {
        // TODO -- only used when ArrayRow.USE_LINKED_VARIABLES is set to true
    }

    fun pickPivotCandidate(): SolverVariable? {
        // TODO -- only used when ArrayRow.USE_LINKED_VARIABLES is set to true
        return null
    }

    fun updateFromSystem(goal: ArrayRow?, mRows: Array<ArrayRow?>?) {
        // TODO -- only used when ArrayRow.USE_LINKED_VARIABLES is set to true
    }

    fun divideByAmount(amount: Float) {
        // TODO -- only used when ArrayRow.USE_LINKED_VARIABLES is set to true
    }

    fun updateClientEquations(arrayRow: ArrayRow?) {
        // TODO -- only used when ArrayRow.USE_LINKED_VARIABLES is set to true
    }

    fun hasAtLeastOnePositiveVariable(): Boolean {
        // TODO -- only used when ArrayRow.USE_LINKED_VARIABLES is set to true
        return false
    }

    fun invert() {
        // TODO -- only used when ArrayRow.USE_LINKED_VARIABLES is set to true
    }

    companion object {
        private const val DEBUG = false
    }
}
