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

import androidx.constraintlayout.coremp.motion.key.MotionKeyCycle
import androidx.constraintlayout.coremp.motion.utils.ArcCurveFit
import androidx.constraintlayout.coremp.motion.utils.KeyCache
import androidx.constraintlayout.coremp.motion.utils.TypedValues
import kotlinx.coroutines.Runnable
import kotlin.test.Test
import kotlin.test.assertEquals

class MotionKeyCycleTest {
    inner class Scene {
        var mMW1 = MotionWidget()
        var mMW2 = MotionWidget()
        var mRes = MotionWidget()
        var mCache = KeyCache()
        var mMotion: Motion

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
            for (p in 0..SAMPLES) {
                mMotion.interpolate(mRes, p * 0.1f, (1000000 + (p * 100)).toLong(), mCache)
                r.run()
            }
        }
    }

    fun cycleBuilder(s: Scene, type: Int) {
        val amp = floatArrayOf(0f, 50f, 0f)
        val pos = intArrayOf(0, 50, 100)
        val period = floatArrayOf(0f, 2f, 0f)
        for (i in amp.indices) {
            val cycle = MotionKeyCycle()
            cycle.setValue(type, amp[i])
            cycle.setValue(TypedValues.CycleType.TYPE_WAVE_PERIOD, period[i])
            cycle.setFramePosition(pos[i])
            cycle.dump()
            s.mMotion.addKey(cycle)
        }
    }

    private fun basicRunThrough(type: Int): Scene {
        val s = Scene()
        cycleBuilder(s, type)
        s.setup()
        if (DEBUG) {
            s.sample(Runnable { println(s.mRes.getValueAttributes(type)) })
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        return s
    }

    @Test
    fun keyCycleRotationX() {
        val s = basicRunThrough(TypedValues.CycleType.TYPE_ROTATION_X)
        assertEquals(0.0f, s.mRes.getRotationX(), 0.0001f)
    }

    @Test
    fun keyCycleRotationY() {
        val s = basicRunThrough(TypedValues.CycleType.TYPE_ROTATION_Y)
        assertEquals(0.0f, s.mRes.getRotationY(), 0.0001f)
    }

    @Test
    fun keyCycleRotationZ() {
        val s = basicRunThrough(TypedValues.CycleType.TYPE_ROTATION_Z)
        assertEquals(0.0f, s.mRes.getRotationZ(), 0.0001f)
    }

    @Test
    fun keyCycleTranslationX() {
        val s = basicRunThrough(TypedValues.CycleType.TYPE_TRANSLATION_X)
        assertEquals(0.0f, s.mRes.getTranslationX(), 0.0001f)
    }

    @Test
    fun keyCycleTranslationY() {
        val s = basicRunThrough(TypedValues.CycleType.TYPE_TRANSLATION_Y)
        assertEquals(0.0f, s.mRes.getTranslationY(), 0.0001f)
    }

    @Test
    fun keyCycleTranslationZ() {
        val s = basicRunThrough(TypedValues.CycleType.TYPE_TRANSLATION_Z)
        assertEquals(0.0f, s.mRes.getTranslationZ(), 0.0001f)
    }

    @Test
    fun keyCycleScaleX() {
        val s = basicRunThrough(TypedValues.CycleType.TYPE_SCALE_X)
        assertEquals(0.0f, s.mRes.getScaleX(), 0.0001f)
    }

    @Test
    fun keyCycleScaleY() {
        val s = basicRunThrough(TypedValues.CycleType.TYPE_SCALE_Y)
        assertEquals(0.0f, s.mRes.getScaleY(), 0.0001f)
    }

    companion object {
        private const val DEBUG = true
        private const val SAMPLES = 30
    }
}
