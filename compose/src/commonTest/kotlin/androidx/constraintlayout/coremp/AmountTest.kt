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

import kotlin.test.Test
import kotlin.test.assertEquals

class AmountTest {
    private var mA1 = Amount(2, 3)
    private var mA2 = Amount(3, 5)

    fun setUp() {
        mA1[2] = 3
        mA2[3] = 5
    }

    @Test
    fun testAdd() {
        mA1.add(mA2)
        assertEquals(mA1.numerator, 19)
        assertEquals(mA1.denominator, 15)
    }

    @Test
    fun testSubtract() {
        mA1.subtract(mA2)
        assertEquals(mA1.numerator, 1)
        assertEquals(mA1.denominator, 15)
    }

    @Test
    fun testMultiply() {
        mA1.multiply(mA2)
        assertEquals(mA1.numerator, 2)
        assertEquals(mA1.denominator, 5)
    }

    @Test
    fun testDivide() {
        mA1.divide(mA2)
        assertEquals(mA1.numerator, 10)
        assertEquals(mA1.denominator, 9)
    }

    @Test
    fun testSimplify() {
        mA1[20] = 30
        assertEquals(mA1.numerator, 2)
        assertEquals(mA1.denominator, 3)
        mA1[77] = 88
        assertEquals(mA1.numerator, 7)
        assertEquals(mA1.denominator, 8)
    }

    @Test
    fun testEquality() {
        mA2[mA1.numerator] = mA1.denominator
        assertEquals(mA1, mA2)
    }
}
