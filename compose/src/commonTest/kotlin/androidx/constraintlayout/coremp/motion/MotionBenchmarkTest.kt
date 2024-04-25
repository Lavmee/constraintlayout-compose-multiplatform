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
import kotlin.test.Test
import kotlin.test.assertEquals

class MotionBenchmarkTest {
    fun setUpMotionController(): Int {
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
        val motion = Motion(mw1)
        motion.setStart(mw1)
        motion.setEnd(mw2)
        motion.addKey(keyPosition)
        motion.addKey(keyPosition2)
        motion.setup(1000, 1000, 2f, 1000000)
        motion.interpolate(res, 0.1f, (1000000 + (0.5 * 100).toInt()).toLong(), cache)
        return res.getLeft()
    }

    fun setUpMotionArcController(): Int {
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
        val motion = Motion(mw1)
        motion.setPathMotionArc(ArcCurveFit.ARC_START_HORIZONTAL)
        motion.setStart(mw1)
        motion.setEnd(mw2)
        motion.addKey(keyPosition)
        motion.addKey(keyPosition2)
        motion.setup(1000, 1000, 2f, 1000000)
        motion.interpolate(res, 0.1f, (1000000 + (0.5 * 100).toInt()).toLong(), cache)
        return res.getLeft()
    }

    @Test
    fun motionController1000xSetup() {
        for (i in 0..999) {
            val left = setUpMotionController()
            assertEquals(40, left.toLong())
        }
    }

    @Test
    fun motionControllerArc1000xSetup() {
        for (i in 0..999) {
            val left = setUpMotionArcController()
            assertEquals(60, left.toLong())
        }
    }

    companion object {
        private const val DEBUG = false
    }
}
