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

import androidx.constraintlayout.coremp.motion.key.MotionKeyPosition
import androidx.constraintlayout.coremp.motion.utils.ArcCurveFit
import androidx.constraintlayout.coremp.motion.utils.KeyCache
import androidx.constraintlayout.coremp.motion.utils.TypedValues
import androidx.constraintlayout.coremp.motion.utils.Utils
import androidx.constraintlayout.coremp.test.assertEquals
import kotlinx.coroutines.Runnable
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals

class MotionKeyPositionTest {
    @Test
    fun testBasic() {
        assertEquals(2, 1 + 1)
    }

    @Test
    fun keyPosition1() {
        val mw1 = MotionWidget()
        val mw2 = MotionWidget()
        val res = MotionWidget()
        val cache = KeyCache()
        mw1.setBounds(0, 0, 30, 40)
        mw2.setBounds(400, 400, 430, 440)
        // mw1.motion.mPathMotionArc = MotionWidget.A
        val motion = Motion(mw1)
        motion.setPathMotionArc(ArcCurveFit.ARC_START_VERTICAL)
        motion.setStart(mw1)
        motion.setEnd(mw2)
        motion.setup(1000, 1000, 1f, 1000000)
        if (DEBUG) {
            var p = 0f
            while (p <= 1) {
                motion.interpolate(res, p, (1000000 + (p * 100).toInt()).toLong(), cache)
                println(res)
                p += 0.1.toFloat()
            }
        }
        motion.interpolate(res, 0.5f, (1000000 + 1000).toLong(), cache)
        val left = (0.5 + 400 * (1 - sqrt(0.5))).toInt()
        val top = (0.5 + 400 * sqrt(0.5)).toInt()
        assertEquals(left, res.getLeft())
        assertEquals(147, res.getRight())
        assertEquals(top, res.getTop(), 0.01)
        assertEquals(top + 40, res.getBottom())
    }

    @Test
    fun keyPosition2() {
        val mw1 = MotionWidget()
        val mw2 = MotionWidget()
        val res = MotionWidget()
        val cache = KeyCache()
        mw1.setBounds(0, 0, 30, 40)
        mw2.setBounds(400, 400, 430, 440)
        // mw1.motion.mPathMotionArc = MotionWidget.A
        val motion = Motion(mw1)
        motion.setPathMotionArc(ArcCurveFit.ARC_START_HORIZONTAL)
        motion.setStart(mw1)
        motion.setEnd(mw2)
        motion.setup(1000, 1000, 2f, 1000000)
        motion.interpolate(res, 0.5f, (1000000 + (0.5 * 100).toInt()).toLong(), cache)
        println("0.5 $res")
        if (DEBUG) {
            var p = 0f
            while (p <= 1) {
                motion.interpolate(res, p, (1000000 + (p * 100).toInt()).toLong(), cache)
                println("$res ,     $p")
                p += 0.01.toFloat()
            }
        }
        motion.interpolate(res, 0.5f, (1000000 + 1000).toLong(), cache)
        assertEquals(283, res.getLeft())
        assertEquals(313, res.getRight())
        assertEquals(117, res.getTop())
        assertEquals(157, res.getBottom())
    }

    @Test
    fun keyPosition3() {
        val mw1 = MotionWidget()
        val mw2 = MotionWidget()
        val res = MotionWidget()
        val cache = KeyCache()
        mw1.setBounds(0, 0, 30, 40)
        mw2.setBounds(400, 400, 460, 480)
        val keyPosition = MotionKeyPosition()
        keyPosition.setFramePosition(30)
        keyPosition.setValue(TypedValues.PositionType.TYPE_PERCENT_X, 0.3f)
        keyPosition.setValue(TypedValues.PositionType.TYPE_PERCENT_Y, 0.3f)
        val keyPosition2 = MotionKeyPosition()
        keyPosition2.setFramePosition(88)
        keyPosition2.setValue(TypedValues.PositionType.TYPE_PERCENT_X, .9f)
        keyPosition2.setValue(TypedValues.PositionType.TYPE_PERCENT_Y, 0.5f)

        // mw1.motion.mPathMotionArc = MotionWidget.A
        val motion = Motion(mw1)
        //  motion.setPathMotionArc(ArcCurveFit.ARC_START_HORIZONTAL);
        motion.setStart(mw1)
        motion.setEnd(mw2)
        motion.addKey(keyPosition)
        motion.addKey(keyPosition2)
        motion.setup(1000, 1000, 2f, 1000000)
        motion.interpolate(res, 0.5f, (1000000 + (0.5 * 100).toInt()).toLong(), cache)
        println("0.5 $res")
        if (DEBUG) {
            var str = ""
            var p = 0f
            while (p <= 1) {
                motion.interpolate(res, p, (1000000 + (p * 100).toInt()).toLong(), cache)
                str += res.toString() + "\n"
                p += 0.01.toFloat()
            }
            Utils.socketSend(str)
        }
        motion.interpolate(res, 0f, (1000000 + 1000).toLong(), cache)
        assertEquals("0, 0, 30, 40", res.toString())
        motion.interpolate(res, 0.2f, (1000000 + 1000).toLong(), cache)
        assertEquals("80, 86, 116, 134", res.toString())
        motion.interpolate(res, 0.3f, (1000000 + 1000).toLong(), cache)
        assertEquals("120, 120, 159, 172", res.toString())
        motion.interpolate(res, 0.5f, (1000000 + 1000).toLong(), cache)
        assertEquals("204, 120, 249, 180", res.toString())
        motion.interpolate(res, 0.7f, (1000000 + 1000).toLong(), cache)
        assertEquals("289, 106, 339, 174", res.toString())
        motion.interpolate(res, 0.9f, (1000000 + 1000).toLong(), cache)
        assertEquals("367, 215, 424, 291", res.toString())
        motion.interpolate(res, 1f, (1000000 + 1000).toLong(), cache)
        assertEquals("400, 400, 460, 480", res.toString())
    }

    @Test
    fun keyPosition4() {
        val mw1 = MotionWidget()
        val mw2 = MotionWidget()
        val res = MotionWidget()
        val cache = KeyCache()
        val keyPosition = MotionKeyPosition()
        mw1.setBounds(0, 0, 30, 40)
        mw2.setBounds(400, 400, 460, 480)
        keyPosition.setFramePosition(20)
        keyPosition.setValue(TypedValues.PositionType.TYPE_PERCENT_X, 1f)
        keyPosition.setValue(TypedValues.PositionType.TYPE_PERCENT_Y, 0.5f)
        keyPosition.setValue(TypedValues.PositionType.TYPE_PERCENT_HEIGHT, 0.2f)
        keyPosition.setValue(TypedValues.PositionType.TYPE_PERCENT_WIDTH, 1f)
        // mw1.motion.mPathMotionArc = MotionWidget.A
        val motion = Motion(mw1)
        //  motion.setPathMotionArc(ArcCurveFit.ARC_START_HORIZONTAL);
        motion.setStart(mw1)
        motion.setEnd(mw2)
        motion.addKey(keyPosition)
        motion.setup(1000, 1000, 2f, 1000000)
        motion.interpolate(res, 0.5f, (1000000 + (0.5 * 100).toInt()).toLong(), cache)
        println("0.5 $res")
        if (DEBUG) {
            var p = 0f
            while (p <= 1) {
                motion.interpolate(res, p, (1000000 + (p * 100).toInt()).toLong(), cache)
                println("$res ,     $p")
                p += 0.01.toFloat()
            }
        }
        motion.interpolate(res, 0.5f, (1000000 + 1000).toLong(), cache)
        assertEquals("400, 325, 460, 385", res.toString())
    }

    internal inner class Scene {
        var mMW1 = MotionWidget()
        var mMW2 = MotionWidget()
        var mRes = MotionWidget()
        var mCache = KeyCache()
        var mMotion: Motion
        var mProgress = 0f

        init {
            mMotion = Motion(mMW1)
            mMW1.setBounds(0, 0, 30, 40)
            mMW2.setBounds(400, 400, 430, 440)
        }

        fun setup() {
            mMotion.setStart(mMW1)
            mMotion.setEnd(mMW2)
            mMotion.setup(1000, 1000, 1f, 1000000)
        }

        fun sample(r: Runnable) {
            for (p in 0..10) {
                mProgress = p * 0.1f
                mMotion.interpolate(mRes, mProgress, (1000000 + (p * 100)).toLong(), mCache)
                r.run()
            }
        }
    }

    @Test
    fun keyPosition3x() {
        val s: Scene = Scene()
        val cache = KeyCache()
        val frames = intArrayOf(25, 50, 75)
        val percentX = floatArrayOf(0.1f, 0.8f, 0.1f)
        val percentY = floatArrayOf(0.4f, 0.8f, 0.0f)
        for (i in frames.indices) {
            val keyPosition = MotionKeyPosition()
            keyPosition.setFramePosition(frames[i])
            keyPosition.setValue(TypedValues.PositionType.TYPE_PERCENT_X, percentX[i])
            keyPosition.setValue(TypedValues.PositionType.TYPE_PERCENT_Y, percentY[i])
            s.mMotion.addKey(keyPosition)
        }
        s.setup()
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + (0.5 * 100).toInt()).toLong(), cache)
        println("0.5 " + s.mRes)
        if (DEBUG) {
            s.sample(Runnable { println(s.mProgress.toString() + " ,     " + s.mRes) })
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + (0.5 * 100).toInt()).toLong(), cache)
        println("0.5 " + s.mRes)
        assertEquals("320, 320, 350, 360", s.mRes.toString())
    }

    companion object {
        private const val DEBUG = false
    }
}
