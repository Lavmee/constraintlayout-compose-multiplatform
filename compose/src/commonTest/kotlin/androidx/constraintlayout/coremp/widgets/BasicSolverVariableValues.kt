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
package androidx.constraintlayout.coremp.widgets

import androidx.constraintlayout.coremp.ArrayRow
import androidx.constraintlayout.coremp.ArrayRow.ArrayRowVariables
import androidx.constraintlayout.coremp.Cache
import androidx.constraintlayout.coremp.SolverVariable

class BasicSolverVariableValues internal constructor(
    // our owner
    private val mRow: ArrayRow,
    cache: Cache?,
) : ArrayRowVariables {
    inner class Item {
        var mVariable: SolverVariable? = null
        var mValue = 0f
    }

    private var mList: ArrayList<Item> = ArrayList()

    // LinkedList<Item> list = new LinkedList<>();
    private var mComparator: Comparator<Item> =
        Comparator { a, b -> a.mVariable!!.id - b.mVariable!!.id }

    override val currentSize: Int
        get() = mList.size

    override fun getVariable(i: Int): SolverVariable? {
        return mList.get(i).mVariable
    }

    override fun getVariableValue(i: Int): Float {
        return mList.get(i).mValue
    }

    override fun contains(variable: SolverVariable?): Boolean {
        for (item in mList) {
            if (item.mVariable!!.id == variable!!.id) {
                return true
            }
        }
        return false
    }

    override fun indexOf(variable: SolverVariable?): Int {
        for (i in 0 until currentSize) {
            val item: Item = mList.get(i)
            if (item.mVariable!!.id == variable!!.id) {
                return i
            }
        }
        return -1
    }

    override fun get(variable: SolverVariable?): Float {
        return if (contains(variable)) {
            mList.get(indexOf(variable)).mValue
        } else {
            0f
        }
    }

    override fun display() {
        val count = currentSize
        print("{ ")
        for (i in 0 until count) {
            val v = getVariable(i) ?: continue
            print(v.toString() + " = " + getVariableValue(i) + " ")
        }
        println(" }")
    }

    override fun clear() {
        val count = currentSize
        for (i in 0 until count) {
            val v = getVariable(i)
            v!!.removeFromRow(mRow)
        }
        mList.clear()
    }

    override fun put(variable: SolverVariable?, value: Float) {
        if (value > -S_EPSILON && value < S_EPSILON) {
            remove(variable, true)
            return
        }
        //        System.out.println("Put " + variable + " [" + value + "] in " + mRow);
        // list.add(item);
        if (mList.size == 0) {
            val item: Item = Item()
            item.mVariable = variable
            item.mValue = value
            mList.add(item)
            variable!!.addToRow(mRow)
            variable.usageInRowCount++
        } else {
            if (contains(variable)) {
                val currentItem: Item = mList.get(indexOf(variable))
                currentItem.mValue = value
                return
            } else {
                val item: Item = Item()
                item.mVariable = variable
                item.mValue = value
                mList.add(item)
                variable!!.usageInRowCount++
                variable.addToRow(mRow)
                mList.sortWith(mComparator)
            }
            //            if (false) {
//                int previousItem = -1;
//                int n = 0;
//                for (Item currentItem : list) {
//                    if (currentItem.variable.id == variable.id) {
//                        currentItem.value = value;
//                        return;
//                    }
//                    if (currentItem.variable.id < variable.id) {
//                        previousItem = n;
//                    }
//                    n++;
//                }
//                Item item = new Item();
//                item.variable = variable;
//                item.value = value;
//                list.add(previousItem + 1, item);
//                variable.usageInRowCount++;
//                variable.addToRow(mRow);
//            }
        }
    }

    override fun sizeInBytes(): Int {
        return 0
    }

    override fun remove(v: SolverVariable?, removeFromDefinition: Boolean): Float {
        if (!contains(v)) {
            return 0f
        }
        val index = indexOf(v)
        val value: Float = mList.get(indexOf(v)).mValue
        mList.removeAt(index)
        v!!.usageInRowCount--
        if (removeFromDefinition) {
            v.removeFromRow(mRow)
        }
        return value
    }

    override fun add(v: SolverVariable?, value: Float, removeFromDefinition: Boolean) {
        if (value > -S_EPSILON && value < S_EPSILON) {
            return
        }
        if (!contains(v)) {
            put(v, value)
        } else {
            val item: Item = mList.get(indexOf(v))
            item.mValue += value
            if (item.mValue > -S_EPSILON && item.mValue < S_EPSILON) {
                item.mValue = 0f
                mList.remove(item)
                v!!.usageInRowCount--
                if (removeFromDefinition) {
                    v.removeFromRow(mRow)
                }
            }
        }
    }

    override fun use(definition: ArrayRow?, removeFromDefinition: Boolean): Float {
        return 0f
    }

    override fun invert() {
        for (item in mList) {
            item.mValue *= -1f
        }
    }

    override fun divideByAmount(amount: Float) {
        for (item in mList) {
            item.mValue /= amount
        }
    }

    companion object {
        private const val S_EPSILON = 0.001f
    }
}
