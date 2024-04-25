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

import androidx.constraintlayout.coremp.motion.key.MotionKeyTimeCycle
import androidx.constraintlayout.coremp.motion.utils.ArcCurveFit
import androidx.constraintlayout.coremp.motion.utils.KeyCache
import androidx.constraintlayout.coremp.motion.utils.TypedValues
import androidx.constraintlayout.coremp.test.assertEquals
import kotlinx.coroutines.Runnable
import kotlin.test.Test
import kotlin.test.assertEquals

class MotionKeyTimeCycleTest {
    fun cycleBuilder(s: Scene, type: Int) {
        val amp = floatArrayOf(0f, 50f, 0f)
        val pos = intArrayOf(0, 50, 100)
        val period = floatArrayOf(0f, 2f, 0f)
        for (i in amp.indices) {
            val cycle = MotionKeyTimeCycle()
            cycle.setValue(type, amp[i])
            cycle.setValue(TypedValues.CycleType.TYPE_WAVE_PERIOD, period[i])
            cycle.setFramePosition(pos[i])
            s.mMotion.addKey(cycle)
        }
    }

    fun basicRunThrough(type: Int): Scene {
        val s: Scene = Scene()
        cycleBuilder(s, type)
        s.setup()
        if (DEBUG) {
            s.sample(Runnable { println(s.mRes.getValueAttributes(type)) })
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        return s
    }

    @Test
    fun disabled() {
        if (DISABLE) {
            println(" all test in MotionKeyTimeCycle DISABLE!")
        }
        assertEquals(DISABLE, true)
    }

    @Test
    fun keyCycleRotationX() {
        if (DISABLE) {
            return
        }
        val s = basicRunThrough(TypedValues.CycleType.TYPE_ROTATION_X)
        assertEquals(0, s.mRes.getRotationX(), 0.0001f)
    }

    @Test
    fun keyCycleRotationY() {
        if (DISABLE) {
            return
        }
        val s = basicRunThrough(TypedValues.CycleType.TYPE_ROTATION_Y)
        assertEquals(0, s.mRes.getRotationY(), 0.0001f)
    }

    @Test
    fun keyCycleRotationZ() {
        if (DISABLE) {
            return
        }
        val s = basicRunThrough(TypedValues.CycleType.TYPE_ROTATION_Z)
        assertEquals(0, s.mRes.getRotationZ(), 0.0001f)
    }

    @Test
    fun keyCycleTranslationX() {
        if (DISABLE) {
            return
        }
        val s = basicRunThrough(TypedValues.CycleType.TYPE_TRANSLATION_X)
        assertEquals(0, s.mRes.getTranslationX(), 0.0001f)
    }

    @Test
    fun keyCycleTranslationY() {
        if (DISABLE) {
            return
        }
        val s = basicRunThrough(TypedValues.CycleType.TYPE_TRANSLATION_Y)
        assertEquals(0, s.mRes.getTranslationY(), 0.0001f)
    }

    @Test
    fun keyCycleTranslationZ() {
        if (DISABLE) {
            return
        }
        val s = basicRunThrough(TypedValues.CycleType.TYPE_TRANSLATION_Z)
        assertEquals(0, s.mRes.getTranslationZ(), 0.0001f)
    }

    @Test
    fun keyCycleScaleX() {
        if (DISABLE) {
            return
        }
        val s = basicRunThrough(TypedValues.CycleType.TYPE_SCALE_X)
        assertEquals(0, s.mRes.getScaleX(), 0.0001f)
    }

    @Test
    fun keyCycleScaleY() {
        if (DISABLE) {
            return
        }
        val s = basicRunThrough(TypedValues.CycleType.TYPE_SCALE_Y)
        assertEquals(0, s.mRes.getScaleY(), 0.0001f)
    }

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

    companion object {
        private const val DEBUG = true
        private const val SAMPLES = 30
        private const val DISABLE = true
    }
}
