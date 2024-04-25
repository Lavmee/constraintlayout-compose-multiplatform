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
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals

class MotionControlTest {
    @Test
    fun testBasic() {
        assertEquals(2, 1 + 1)
    }

    @Test
    fun simpleLinear() {
        val mw1 = MotionWidget()
        val mw2 = MotionWidget()
        val res = MotionWidget()
        val cache = KeyCache()
        mw1.setBounds(0, 0, 30, 40)
        mw2.setBounds(500, 600, 530, 640)
        val motion = Motion(mw1)
        motion.setStart(mw1)
        motion.setEnd(mw2)
        motion.setup(1000, 1000, 1f, 1000000)
        println("-------------------------------------------")
        var p = 0f
        while (p <= 1) {
            motion.interpolate(res, p, (1000000 + (p * 100).toInt()).toLong(), cache)
            println(res)
            p += 0.1.toFloat()
        }
        motion.interpolate(res, 0.5f, (1000000 + 1000).toLong(), cache)
        assertEquals((0.5 + (mw1.getLeft() + mw2.getLeft()) / 2).toInt(), res.getLeft())
        assertEquals((0.5 + (mw1.getRight() + mw2.getRight()) / 2).toInt(), res.getRight())
        assertEquals((0.5 + (mw1.getTop() + mw2.getTop()) / 2).toInt(), res.getTop())
        assertEquals((0.5 + (mw1.getBottom() + mw2.getBottom()) / 2).toInt(), res.getBottom())
    }

    @Test
    fun archMode1() {
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
    fun archMode2() {
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
    fun archMode3() {
        val mw1 = MotionWidget()
        val mw2 = MotionWidget()
        val res = MotionWidget()
        val cache = KeyCache()
        val keyPosition = MotionKeyPosition()
        mw1.setBounds(0, 0, 3, 4)
        mw2.setBounds(400, 400, 460, 480)
        keyPosition.setFramePosition(50)
        keyPosition.setValue(TypedValues.PositionType.TYPE_PERCENT_X, 1.0f)
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
            var str = ""
            var p = 0f
            while (p <= 1) {
                motion.interpolate(res, p, (1000000 + (p * 100).toInt()).toLong(), cache)
                str += res.toString() + "\n"
                p += 0.01.toFloat()
            }
            Utils.socketSend(str)
        }
        motion.interpolate(res, 0.7f, (1000000 + 1000).toLong(), cache)
        val str = res.toString()
        println(str)
        assertEquals("400, 288, 460, 328", str)
    }

    companion object {
        private const val DEBUG = false
    }
}
