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

import androidx.constraintlayout.coremp.motion.key.MotionKeyAttributes
import androidx.constraintlayout.coremp.motion.utils.ArcCurveFit
import androidx.constraintlayout.coremp.motion.utils.KeyCache
import androidx.constraintlayout.coremp.motion.utils.TypedValues
import kotlinx.coroutines.Runnable
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals

class MotionCustomKeyAttributesTest {
    private fun Double.format(digits: Int = 1): String {
        return ((this * (10.0).pow(digits)).roundToInt() / 100.0).toString()
    }

    internal inner class Scene {
        var mMW1 = MotionWidget()
        var mMW2 = MotionWidget()
        var mRes = MotionWidget()
        var mCache = KeyCache()
        var mMotion: Motion
        var mPos = 0f

        init {
            mMotion = Motion(mMW1)
            mMW1.setBounds(0, 0, 30, 40)
            mMW2.setBounds(400, 400, 430, 440)
            mMotion.setPathMotionArc(ArcCurveFit.ARC_START_VERTICAL)
        }

        fun setup() {
            mMotion.setStart(mMW1)
            mMotion.setEnd(mMW2)
            mMotion.setup(1000, 1000, 1f, 1000000)
        }

        fun sample(r: Runnable) {
            for (p in 0..10) {
                mPos = p * 0.1f
                mMotion.interpolate(mRes, mPos, (1000000 + (p * 100)).toLong(), mCache)
                r.run()
            }
        }
    }

    @Test
    fun customFloat() {
        val s = Scene()
        s.mMW1.setCustomAttribute("bob", TypedValues.Custom.TYPE_FLOAT, 0f)
        s.mMW2.setCustomAttribute("bob", TypedValues.Custom.TYPE_FLOAT, 1f)
        val mka = MotionKeyAttributes()
        mka.setFramePosition(50)
        mka.setCustomAttribute("bob", TypedValues.Custom.TYPE_FLOAT, 2f)
        s.mMotion.addKey(mka)
        s.setup()
        if (DEBUG) {
            s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
            s.sample(
                Runnable {
                    println(
                        s.mPos.toDouble().format() + " " + s.mRes.getCustomAttribute(
                            "bob",
                        ),
                    )
                },
            )
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        assertEquals(
            2.0f,
            s.mRes.getCustomAttribute("bob")!!.getFloatValue(),
            0.001f,
        )
    }

    @Test
    fun customColor1() {
        val s = Scene()
        s.mMW1.setCustomAttribute("fish", TypedValues.Custom.TYPE_COLOR, -0x1000000)
        s.mMW2.setCustomAttribute("fish", TypedValues.Custom.TYPE_COLOR, -0x1)
        val mka = MotionKeyAttributes()
        mka.setFramePosition(50)
        mka.setCustomAttribute("fish", TypedValues.Custom.TYPE_COLOR, -0x1000000)
        s.mMotion.addKey(mka)
        s.setup()
        if (DEBUG) {
            s.sample(
                Runnable {
                    println(
                        s.mPos.toDouble().format() + "\t" + s.mRes.getCustomAttribute(
                            "fish",
                        ),
                    )
                },
            )
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        assertEquals(-0x1000000, s.mRes.getCustomAttribute("fish")!!.getColorValue())
    }

    @Test
    fun customColor2() {
        val s = Scene()
        s.mMW1.setCustomAttribute("fish", TypedValues.Custom.TYPE_COLOR, -0x10000)
        s.mMW2.setCustomAttribute("fish", TypedValues.Custom.TYPE_COLOR, -0xffff01)
        val mka = MotionKeyAttributes()
        mka.setFramePosition(50)
        mka.setCustomAttribute("fish", TypedValues.Custom.TYPE_COLOR, -0xff0100)
        s.mMotion.addKey(mka)
        s.setup()
        if (DEBUG) {
            s.sample(
                Runnable {
                    println(
                        s.mPos.toDouble().format() + " " + s.mRes.getCustomAttribute(
                            "fish",
                        ),
                    )
                },
            )
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        assertEquals(-0xff0100, s.mRes.getCustomAttribute("fish")!!.getColorValue())
    }

    companion object {
        private const val DEBUG = true
    }
}
