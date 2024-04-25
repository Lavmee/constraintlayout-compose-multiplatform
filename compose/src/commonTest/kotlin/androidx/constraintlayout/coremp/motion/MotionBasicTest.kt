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

import androidx.constraintlayout.coremp.motion.utils.CurveFit
import androidx.constraintlayout.coremp.motion.utils.Easing
import androidx.constraintlayout.coremp.motion.utils.HyperSpline
import androidx.constraintlayout.coremp.motion.utils.LinearCurveFit
import androidx.constraintlayout.coremp.motion.utils.Oscillator
import androidx.constraintlayout.coremp.motion.utils.StopLogicEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MotionBasicTest {
    @Test
    fun testBasic() {
        assertEquals(2, 1 + 1)
    }

    @Test
    @Throws(Exception::class)
    fun unit_test_framework_working() {
        assertEquals(4, 2 + 2)
    }

    @Test
    @Throws(Exception::class)
    fun testHyperSpline01() {
        val points =
            arrayOf(doubleArrayOf(0.0, 0.0), doubleArrayOf(1.0, 1.0), doubleArrayOf(2.0, 2.0))
        val spline = HyperSpline(points)
        val value: Double = spline.getPos(0.5, 1)
        assertEquals(1.0, value, 0.001)
    }

    @Test
    @Throws(Exception::class)
    fun testCurveFit01() {
        val points =
            arrayOf(doubleArrayOf(0.0, 0.0), doubleArrayOf(1.0, 1.0), doubleArrayOf(2.0, 0.0))
        val time = doubleArrayOf(
            0.0,
            5.0,
            10.0,
        )
        val spline = CurveFit.get(CurveFit.SPLINE, time, points)
        var value = spline.getPos(5.0, 0)
        assertEquals(1.0, value, 0.001)
        value = spline.getPos(7.0, 0)
        assertEquals(1.4, value, 0.001)
        value = spline.getPos(7.0, 1)
        assertEquals(0.744, value, 0.001)
    }

    @Test
    @Throws(Exception::class)
    fun testCurveFit02() {
        val points =
            arrayOf(doubleArrayOf(0.0, 0.0), doubleArrayOf(1.0, 1.0), doubleArrayOf(2.0, 0.0))
        val time = doubleArrayOf(
            0.0,
            5.0,
            10.0,
        )
        val spline = CurveFit.get(CurveFit.LINEAR, time, points)
        var value = spline.getPos(5.0, 0)
        assertEquals(1.0, value, 0.001)
        value = spline.getPos(7.0, 0)
        assertEquals(1.4, value, 0.001)
        value = spline.getPos(7.0, 1)
        assertEquals(0.6, value, 0.001)
    }

    @Test
    @Throws(Exception::class)
    fun testEasing01() {
        var value: Double
        var diffValue: Double
        var easing: Easing?
        easing = Easing.getInterpolator("cubic=(1,1,0,0)")
        value = easing!!.get(0.5)
        assertEquals(0.5, value, 0.001)
        diffValue = easing.getDiff(0.5)
        assertEquals(1.0, diffValue, 0.001)
        diffValue = easing.getDiff(0.1)
        assertEquals(1.0, diffValue, 0.001)
        diffValue = easing.getDiff(0.9)
        assertEquals(1.0, diffValue, 0.001)
        easing = Easing.getInterpolator("cubic=(1,0,0,1)")
        value = easing!!.get(0.5)
        assertEquals(0.5, value, 0.001)
        diffValue = easing.getDiff(0.001)
        assertEquals(0.0, diffValue, 0.001)
        diffValue = easing.getDiff(0.9999)
        assertEquals(0.0, diffValue, 0.001)
        easing = Easing.getInterpolator("cubic=(0.5,1,0.5,0)")
        value = easing!!.get(0.5)
        assertEquals(0.5, value, 0.001)
        diffValue = easing.getDiff(0.5)
        assertEquals(0.0, diffValue, 0.001)
        diffValue = easing.getDiff(0.00001)
        assertEquals(2.0, diffValue, 0.001)
        diffValue = easing.getDiff(0.99999)
        assertEquals(2.0, diffValue, 0.001)
    }

    @Test
    @Throws(Exception::class)
    fun testLinearCurveFit01() {
        var value: Double
        var diffValue: Double
        val points =
            arrayOf(doubleArrayOf(0.0, 0.0), doubleArrayOf(1.0, 1.0), doubleArrayOf(2.0, 0.0))
        val time = doubleArrayOf(
            0.0,
            5.0,
            10.0,
        )
        val lcurve = LinearCurveFit(time, points)
        value = lcurve.getPos(5.0, 0)
        assertEquals(1.0, value, 0.001)
        value = lcurve.getPos(7.0, 0)
        assertEquals(1.4, value, 0.001)
        value = lcurve.getPos(7.0, 1)
        assertEquals(0.6, value, 0.001)
    }

    @Test
    @Throws(Exception::class)
    fun testOscillator01() {
        val o = Oscillator()
        o.setType(Oscillator.SQUARE_WAVE, null)
        o.addPoint(0.0, 0f)
        o.addPoint(0.5, 10f)
        o.addPoint(1.0, 0f)
        o.normalize()
        assertEquals(19, countZeroCrossings(o, Oscillator.SIN_WAVE))
        assertEquals(19, countZeroCrossings(o, Oscillator.SQUARE_WAVE))
        assertEquals(19, countZeroCrossings(o, Oscillator.TRIANGLE_WAVE))
        assertEquals(19, countZeroCrossings(o, Oscillator.SAW_WAVE))
        assertEquals(19, countZeroCrossings(o, Oscillator.REVERSE_SAW_WAVE))
        assertEquals(20, countZeroCrossings(o, Oscillator.COS_WAVE))
    }

//    @Test
//    @Throws(Exception::class)
//    fun testStopLogic01() {
//        val results = arrayOf(
//            "[0.4, 0.36, 0.42, 0.578, 0.778, 0.938, 0.999, 1, 1, 1]",
//            "[0.4, 0.383, 0.464, 0.64, 0.838, 0.967, 1, 1, 1, 1]",
//            "[0.4, 0.405, 0.509, 0.697, 0.885, 0.986, 1, 1, 1, 1]",
//            "[0.4, 0.427, 0.553, 0.75, 0.921, 0.997, 1, 1, 1, 1]",
//            "[0.4, 0.449, 0.598, 0.798, 0.948, 1, 1, 1, 1, 1]",
//            "[0.4, 0.472, 0.64, 0.838, 0.967, 1, 1, 1, 1, 1]",
//            "[0.4, 0.494, 0.678, 0.87, 0.981, 1, 1, 1, 1, 1]",
//            "[0.4, 0.516, 0.71, 0.894, 0.989, 1, 1, 1, 1, 1]",
//            "[0.4, 0.538, 0.737, 0.913, 0.995, 1, 1, 1, 1, 1]",
//            "[0.4, 0.56, 0.76, 0.927, 0.998, 1, 1, 1, 1, 1]"
//        )
//        for (i in 0..9) {
//            val f = stopGraph((i - 4) * .1f)
//            assertEquals(" test $i", results[i], arrayToString(f))
//        }
//    }

    private fun countZeroCrossings(o: Oscillator, type: Int): Int {
        val n = 1000
        var last: Double = o.getValue(0.0, 0.0)
        var count = 0
        o.setType(type, null)
        for (i in 0 until n) {
            val v: Double = o.getValue(0.0001 + i / n.toDouble(), 0.0)
            if (v * last < 0) {
                count++
            }
            last = v
        }
        return count
    }

//    fun arrayToString(f: FloatArray): String {
//        var ret = "["
//        val df: DecimalFormat = DecimalFormat("###.###")
//        for (i in f.indices) {
//            val aFloat = f[i]
//            if (i > 0) {
//                ret += ", "
//            }
//            f[i].toDouble()
//            ret += df.format(f[i].toDouble())
//        }
//        return "$ret]"
//    }

    companion object {
        private fun stopGraph(vel: Float): FloatArray {
            val breakLogic = StopLogicEngine()
            breakLogic.config(.4f, 1f, vel, 1f, 2f, 0.9f)
            val ret = FloatArray(10)
            for (i in ret.indices) {
                val time = 2 * i / (ret.size - 1).toFloat()
                val pos: Float = breakLogic.getInterpolation(time)
                ret[i] = pos
            }
            assertTrue(breakLogic.isStopped)
            return ret
        }
    }
}
