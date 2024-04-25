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

import androidx.constraintlayout.coremp.motion.utils.KeyCache
import androidx.constraintlayout.coremp.motion.utils.TypedValues
import kotlinx.coroutines.Runnable
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalStdlibApi::class)
class MotionCustomAttributesTest {
    @Test
    fun testBasic() {
        assertEquals(2, 1 + 1)
    }

    internal inner class Scene {
        var mMW1 = MotionWidget()
        var mMW2 = MotionWidget()
        var mRes = MotionWidget()
        var mCache = KeyCache()
        var mMotion: Motion = Motion(mMW1)
        var mPos = 0f

        fun setup() {
            mMotion.setStart(mMW1)
            mMotion.setEnd(mMW2)
            mMotion.setup(0, 0, 1f, 1000000)
        }

        fun sample(r: Runnable) {
            for (p in 0..10) {
                mPos = p.toFloat()
                mMotion.interpolate(mRes, p * 0.1f, (1000000 + (p * 100)).toLong(), mCache)
                r.run()
            }
        }
    }

    @Test
    fun customFloat() {
        val s = Scene()
        s.mMW1.setCustomAttribute("bob", TypedValues.Custom.TYPE_FLOAT, 0f)
        s.mMW2.setCustomAttribute("bob", TypedValues.Custom.TYPE_FLOAT, 1f)
        s.setup()
        if (DEBUG) {
            s.sample(
                Runnable {
                    println(
                        s.mRes.getCustomAttribute("bob")!!.getFloatValue(),
                    )
                },
            )
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        assertEquals(0.5f, s.mRes.getCustomAttribute("bob")!!.getFloatValue(), 0.001f)
    }

    @Test
    fun customColor1() {
        val s = Scene()
        s.mMW1.setCustomAttribute("fish", TypedValues.Custom.TYPE_COLOR, -0xff0100)
        s.mMW2.setCustomAttribute("fish", TypedValues.Custom.TYPE_COLOR, -0xff01)
        s.setup()
        s.sample(
            Runnable {
                s.mRes.getCustomAttribute("fish")!!.getColorValue().toHexString()
                println(
                    s.mPos.toString() + " " +
                        s.mRes.getCustomAttribute("fish")!!.getColorValue().toHexString(),
                )
            },
        )
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        assertEquals(-0x454546, s.mRes.getCustomAttribute("fish")!!.getColorValue())
    }

    @Test
    fun customColor2() {
        val s = Scene()
        s.mMW1.setCustomAttribute("fish", TypedValues.Custom.TYPE_COLOR, -0x1000000)
        s.mMW2.setCustomAttribute("fish", TypedValues.Custom.TYPE_COLOR, 0x00880088)
        s.setup()
        if (DEBUG) {
            s.sample(
                Runnable {
                    println(
                        s.mPos.toString() + " " +
                            s.mRes.getCustomAttribute("fish")!!.getColorValue().toHexString(),
                    )
                },
            )
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        assertEquals(0x7f630063, s.mRes.getCustomAttribute("fish")!!.getColorValue())
    }

    companion object {
        private const val DEBUG = true
    }
}
