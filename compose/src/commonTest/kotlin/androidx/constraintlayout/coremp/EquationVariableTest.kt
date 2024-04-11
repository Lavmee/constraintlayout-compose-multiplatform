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

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EquationVariableTest {
    var mLinearSystem: LinearSystem? = null
    var mEV1: EquationVariable? = null
    var mEV2: EquationVariable? = null

    @BeforeTest
    fun setUp() {
        mLinearSystem = LinearSystem()
        mEV1 = EquationVariable(mLinearSystem, 200)
        mEV2 = EquationVariable(mLinearSystem, 200)
    }

    @Test
    fun testEquality() {
        assertEquals(mEV1!!.amount!!, mEV2!!.amount)
    }

    @Test
    fun testAddition() {
        mEV1!!.add(mEV2!!)
        assertEquals(mEV1!!.amount!!.numerator, 400)
    }

    @Test
    fun testSubtraction() {
        mEV1!!.subtract(mEV2!!)
        assertEquals(mEV1!!.amount!!.numerator, 0)
    }

    @Test
    fun testMultiply() {
        mEV1!!.multiply(mEV2!!)
        assertEquals(mEV1!!.amount!!.numerator, 40000)
    }

    @Test
    fun testDivide() {
        mEV1!!.divide(mEV2!!)
        assertEquals(mEV1!!.amount!!.numerator, 1)
    }

    @Test
    fun testCompatible() {
        assertTrue(mEV1!!.isCompatible(mEV2!!))
        mEV2 = EquationVariable(mLinearSystem!!, 200, "TEST", SolverVariable.Type.UNRESTRICTED)
        assertFalse(mEV1!!.isCompatible(mEV2!!))
    }
}
