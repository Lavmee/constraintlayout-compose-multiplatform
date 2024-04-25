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

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class SolverVariableValuesTest {
    @Test
    fun testOperations() {
        val mCache = Cache()
        val variable5 = SolverVariable("v5", SolverVariable.Type.SLACK)
        val variable1 = SolverVariable("v1", SolverVariable.Type.SLACK)
        val variable3 = SolverVariable("v3", SolverVariable.Type.SLACK)
        val variable7 = SolverVariable("v7", SolverVariable.Type.SLACK)
        val variable11 = SolverVariable("v11", SolverVariable.Type.SLACK)
        val variable12 = SolverVariable("v12", SolverVariable.Type.SLACK)
        variable5.id = 5
        variable1.id = 1
        variable3.id = 3
        variable7.id = 7
        variable11.id = 11
        variable12.id = 12
        mCache.mIndexedVariables[variable5.id] = variable5
        mCache.mIndexedVariables[variable1.id] = variable1
        mCache.mIndexedVariables[variable3.id] = variable3
        mCache.mIndexedVariables[variable7.id] = variable7
        mCache.mIndexedVariables[variable11.id] = variable11
        mCache.mIndexedVariables[variable12.id] = variable12
        val values = SolverVariableValues(null, mCache)
        values.put(variable5, 1f)
        println(values)
        values.put(variable1, -1f)
        println(values)
        values.put(variable3, -1f)
        println(values)
        values.put(variable7, 1f)
        println(values)
        values.put(variable11, 1f)
        println(values)
        values.put(variable12, -1f)
        println(values)
        values.remove(variable1, true)
        println(values)
        values.remove(variable3, true)
        println(values)
        values.remove(variable7, true)
        println(values)
        values.add(variable5, 1f, true)
        println(values)
        val currentSize = values.currentSize
        for (i in 0 until currentSize) {
            val variable = values.getVariable(i)
        }
    }

    @Test
    fun testBasic() {
        val mCache = Cache()
        val variable1 = SolverVariable("A", SolverVariable.Type.SLACK)
        val variable2 = SolverVariable("B", SolverVariable.Type.SLACK)
        val variable3 = SolverVariable("C", SolverVariable.Type.SLACK)
        variable1.id = 0
        variable2.id = 1
        variable3.id = 2
        mCache.mIndexedVariables[variable1.id] = variable1
        mCache.mIndexedVariables[variable2.id] = variable2
        mCache.mIndexedVariables[variable3.id] = variable3
        val values = SolverVariableValues(null, mCache)
        variable1.id = 10
        variable2.id = 100
        variable3.id = 1000
        values.put(variable1, 1f)
        values.put(variable2, 2f)
        values.put(variable3, 3f)
        val v1 = values[variable1]
        val v2 = values[variable2]
        val v3 = values[variable3]
        assertEquals(v1, 1f, 0f)
        assertEquals(v2, 2f, 0f)
        assertEquals(v3, 3f, 0f)
    }

    @Test
    fun testBasic2() {
        val mCache = Cache()
        val values = SolverVariableValues(null, mCache)
        val variable1 = SolverVariable("A", SolverVariable.Type.SLACK)
        val variable2 = SolverVariable("B", SolverVariable.Type.SLACK)
        val variable3 = SolverVariable("C", SolverVariable.Type.SLACK)
        variable1.id = 32
        variable2.id = 32 * 2
        variable3.id = 32 * 3
        values.put(variable1, 1f)
        values.put(variable2, 2f)
        values.put(variable3, 3f)
        val v1 = values[variable1]
        val v2 = values[variable2]
        val v3 = values[variable3]
        assertEquals(v1, 1f, 0f)
        assertEquals(v2, 2f, 0f)
        assertEquals(v3, 3f, 0f)
    }

    @Test
    fun testBasic3() {
        val mCache = Cache()
        val values = SolverVariableValues(null, mCache)
        val variables: ArrayList<SolverVariable> = ArrayList<SolverVariable>()
        for (i in 0..9999) {
            val variable = SolverVariable("A$i", SolverVariable.Type.SLACK)
            variable.id = i * 32
            values.put(variable, i.toFloat())
            variables.add(variable)
        }
        var i = 0
        for (variable in variables) {
            val value = i.toFloat()
            assertEquals(value, values[variable], 0f)
            i++
        }
        //        System.out.println("array size: count: " + values.count
//          + " keys: " + values.keys.length + " values: " + values.values.length);
//        values.maxDepth();
    }

    @Test
    fun testBasic4() {
        val mCache = Cache()
        val values = SolverVariableValues(null, mCache)
        val variables: ArrayList<SolverVariable> = ArrayList<SolverVariable>()
        for (i in 0..9999) {
            val variable = SolverVariable("A$i", SolverVariable.Type.SLACK)
            variable.id = i
            values.put(variable, i.toFloat())
            variables.add(variable)
        }
        var i = 0
        for (variable in variables) {
            val value = i.toFloat()
            assertEquals(value, values[variable], 0f)
            i++
        }
        //        System.out.println("array size: count: " + values.count
//          + " keys: " + values.keys.length + " values: " + values.values.length);
//        values.maxDepth();
    }

    @Test
    fun testBasic5() {
        val mCache = Cache()
        val values = SolverVariableValues(null, mCache)
        val variables: ArrayList<SolverVariable> = ArrayList<SolverVariable>()
        for (i in 0..9999) {
            val variable = SolverVariable("A$i", SolverVariable.Type.SLACK)
            variable.id = i
            values.put(variable, i.toFloat())
            variables.add(variable)
        }
        var i = 0
        for (variable in variables) {
            if (i % 2 == 0) {
                values.remove(variable, false)
            }
            i++
        }
        i = 0
        for (variable in variables) {
            val value = i.toFloat()
            if (i % 2 != 0) {
                assertEquals(value, values[variable], 0f)
            }
            i++
        }
        //        System.out.println("array size: count: " + values.count
//          + " keys: " + values.keys.length + " values: " + values.values.length);
//        values.maxDepth();
    }

    @Test
    fun testBasic6() {
        val mCache = Cache()
        val values = SolverVariableValues(null, mCache)
        val variables: ArrayList<SolverVariable> = ArrayList<SolverVariable>()
        val results: HashMap<SolverVariable, Float> =
            HashMap<SolverVariable, Float>()
        for (i in 0..99) {
            val variable = SolverVariable("A$i", SolverVariable.Type.SLACK)
            variable.id = i
            values.put(variable, i.toFloat())
            results.put(variable, i.toFloat())
            variables.add(variable)
        }
        val toRemove: ArrayList<SolverVariable> = ArrayList<SolverVariable>()
        val random: Random = Random(1234)
        for (variable in variables) {
            if (random.nextFloat() > 0.3f) {
                toRemove.add(variable)
            }
        }
        variables.removeAll(toRemove)
        for (i in 0..99) {
            val variable = SolverVariable("B$i", SolverVariable.Type.SLACK)
            variable.id = 100 + i
            values.put(variable, i.toFloat())
            results.put(variable, i.toFloat())
            variables.add(variable)
        }
        for (variable in variables) {
            val value: Float = results.get(variable)!!
            assertEquals(value, values[variable], 0f)
        }
        //        System.out.println("array size: count: " + values.count
//          + " keys: " + values.keys.length + " values: " + values.values.length);
//        values.maxDepth();
    }
}
