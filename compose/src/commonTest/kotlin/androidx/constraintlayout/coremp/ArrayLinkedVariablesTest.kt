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
package androidx.constraintlayout.coremp

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Test nested layout
 */
class ArrayLinkedVariablesTest {
    @Test
    fun testNestedLayout() {
        val cache = Cache()
        val row = ArrayRow(cache)
        val variables = ArrayLinkedVariables(row, cache)
        val v = arrayOfNulls<SolverVariable>(9)
        for (i in v.indices) {
            val p = i xor 3
            v[i] = SolverVariable(
                "dog$p($i)$p",
                SolverVariable.Type.UNRESTRICTED,
            )
            cache.mIndexedVariables[i] = v[i]
            v[i]!!.id = i
            variables.add(v[i], 20f, true)
            if (i % 2 == 1) {
                variables.remove(v[i / 2], true)
            }
            variables.display()
            println()
        }
        for (i in v.indices) {
            if (i % 2 == 1) {
                variables.display()
                variables.add(v[i / 2], 24f, true)
            }
        }
        assertTrue(true)
    }
}
