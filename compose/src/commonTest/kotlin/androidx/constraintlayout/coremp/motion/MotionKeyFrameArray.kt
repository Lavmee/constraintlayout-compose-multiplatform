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
package androidx.constraintlayout.coremp.motion

import androidx.constraintlayout.coremp.motion.utils.KeyFrameArray.CustomArray
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class MotionKeyFrameArray {
    @Test
    fun arcTest1() {
        val array = CustomArray()
        val random: Random = Random.Default
        for (i in 0..31) {
            assertEquals(i, array.size())
            array.append(i, null)
        }
        array.dump()
        for (i in 0 until array.size()) {
            val k: Int = array.keyAt(i)
            val `val`: CustomAttribute? = array.valueAt(i)
            assertEquals(null, `val`)
        }
        array.clear()
        for (i in 0..31) {
            val k: Int = random.nextInt(100)
            println(k)
            array.append(k, CustomAttribute("foo", CustomAttribute.AttributeType.INT_TYPE))
            array.dump()
        }
    }
}
