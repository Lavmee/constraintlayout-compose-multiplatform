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

import androidx.constraintlayout.coremp.motion.utils.ArcCurveFit
import androidx.constraintlayout.coremp.motion.utils.CurveFit
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals

class MotionArcCurveTest {
    @Test
    fun arcTest1() {
        val points =
            arrayOf(doubleArrayOf(0.0, 0.0), doubleArrayOf(1.0, 1.0), doubleArrayOf(2.0, 0.0))
        val time = doubleArrayOf(
            0.0,
            5.0,
            10.0,
        )
        val mode = intArrayOf(
            ArcCurveFit.ARC_START_VERTICAL,
            ArcCurveFit.ARC_START_HORIZONTAL,
        )
        val spline = CurveFit.getArc(mode, time, points)
        println("")
        for (i in time.indices) {
            assertEquals(points[i][0], spline.getPos(time[i], 0), 0.001)
            assertEquals(points[i][1], spline.getPos(time[i], 1), 0.001)
        }
        assertEquals(0.0, spline.getSlope(time[0] + 0.01, 0), 0.001)
        assertEquals(0.0, spline.getSlope(time[1] - 0.01, 1), 0.001)
        assertEquals(0.0, spline.getSlope(time[1] + 0.01, 1), 0.001)
        val dx = spline.getSlope((time[0] + time[1]) / 2, 0)
        val dy = spline.getSlope((time[0] + time[1]) / 2, 1)
        assertEquals(1.0, dx / dy, 0.001)
        val x = spline.getPos((time[0] + time[1]) / 2, 0)
        val y = spline.getPos((time[0] + time[1]) / 2, 1)
        assertEquals(1 - sqrt(0.5), x, 0.001)
        assertEquals(sqrt(0.5), y, 0.001)
    }

    @Test
    fun arcTest2() {
        val points =
            arrayOf(doubleArrayOf(0.0, 0.0), doubleArrayOf(1.0, 1.0), doubleArrayOf(2.0, 0.0))
        val time = doubleArrayOf(
            0.0,
            5.0,
            10.0,
        )
        val mode = intArrayOf(
            ArcCurveFit.ARC_BELOW,
            ArcCurveFit.ARC_BELOW,
        )
        val spline = CurveFit.getArc(mode, time, points)
        println("")
        for (i in time.indices) {
            assertEquals(points[i][0], spline.getPos(time[i], 0), 0.001)
            assertEquals(points[i][1], spline.getPos(time[i], 1), 0.001)
        }
        assertEquals(0.0, spline.getSlope(time[0] + 0.01, 0), 0.001)
        assertEquals(0.0, spline.getSlope(time[1] - 0.01, 1), 0.001)
        assertEquals(0.0, spline.getSlope(time[1] + 0.01, 1), 0.001)
        val dx = spline.getSlope((time[0] + time[1]) / 2, 0)
        val dy = spline.getSlope((time[0] + time[1]) / 2, 1)
        assertEquals(1.0, dx / dy, 0.001)
        val x = spline.getPos((time[0] + time[1]) / 2, 0)
        val y = spline.getPos((time[0] + time[1]) / 2, 1)
        assertEquals(1 - sqrt(0.5), x, 0.001)
        assertEquals(sqrt(0.5), y, 0.001)
    }

    @Test
    fun arcTest3() {
        val points =
            arrayOf(doubleArrayOf(0.0, 0.0), doubleArrayOf(1.0, 1.0), doubleArrayOf(2.0, 0.0))
        val time = doubleArrayOf(
            0.0,
            5.0,
            10.0,
        )
        val mode = intArrayOf(
            ArcCurveFit.ARC_ABOVE,
            ArcCurveFit.ARC_ABOVE,
        )
        val spline = CurveFit.getArc(mode, time, points)
        println("")
        for (i in time.indices) {
            assertEquals(points[i][0], spline.getPos(time[i], 0), 0.001)
            assertEquals(points[i][1], spline.getPos(time[i], 1), 0.001)
        }
        val count = 50
        val dt = (time[time.size - 1] - time[0]).toFloat() / count - 0.0001f
        val xp = FloatArray(count)
        val yp = FloatArray(count)
        for (i in xp.indices) {
            val p = time[0] + i * dt
            xp[i] = spline.getPos(p, 0).toFloat()
            yp[i] = spline.getPos(p, 1).toFloat()
        }
        val expect = """
            |*****                    *****| 0.0
            |     **                **     |
            |       **            **       |
            |        *            *        |
            |         *          *         |
            |          *        *          | 0.263
            |           *      **          |
            |            *    *            |
            |            *    *            |
            |             *  *             |
            |             *  *             | 0.526
            |                              |
            |             *  *             |
            |              **              |
            |              **              |
            |              **              | 0.789
            |              **              |
            |              **              |
            |                              |
            |              *               | 0.999
            0.0                        1.936
            
        """.trimIndent()
        assertEquals(expect, textDraw(30, 20, xp, yp, false))
        assertEquals(0.0, spline.getSlope(time[0] + 0.0001, 1), 0.001)
        assertEquals(0.0, spline.getSlope(time[1] - 0.01, 0), 0.001)
        assertEquals(0.0, spline.getSlope(time[1] + 0.01, 0), 0.001)
        val dx = spline.getSlope((time[0] + time[1]) / 2, 0)
        val dy = spline.getSlope((time[0] + time[1]) / 2, 1)
        assertEquals(1.0, dx / dy, 0.001)
        val x = spline.getPos((time[0] + time[1]) / 2, 1)
        val y = spline.getPos((time[0] + time[1]) / 2, 0)
        assertEquals(1 - sqrt(0.5), x, 0.001)
        assertEquals(sqrt(0.5), y, 0.001)
    }

    companion object {
        private fun textDraw(
            dimx: Int,
            dimy: Int,
            x: FloatArray,
            y: FloatArray,
            flip: Boolean,
        ): String {
            var minX = x[0]
            var maxX = x[0]
            var minY = y[0]
            var maxY = y[0]
            var ret = ""
            for (i in x.indices) {
                minX = min(minX.toDouble(), x[i].toDouble()).toFloat()
                maxX = max(maxX.toDouble(), x[i].toDouble()).toFloat()
                minY = min(minY.toDouble(), y[i].toDouble()).toFloat()
                maxY = max(maxY.toDouble(), y[i].toDouble()).toFloat()
            }
            val c = Array(dimy) { CharArray(dimx) }
            for (i in 0 until dimy) {
                c[i].fill(' ')
            }
            val dimx1 = dimx - 1
            val dimy1 = dimy - 1
            for (j in x.indices) {
                val xp = (dimx1 * (x[j] - minX) / (maxX - minX)).toInt()
                val yp = (dimy1 * (y[j] - minY) / (maxY - minY)).toInt()
                c[if (flip) dimy - yp - 1 else yp][xp] = '*'
            }
            for (i in c.indices) {
                var v: Float
                v = if (flip) {
                    (minY - maxY) * (i / (c.size - 1.0f)) + maxY
                } else {
                    (maxY - minY) * (i / (c.size - 1.0f)) + minY
                }
                v = (v * 1000 + 0.5).toInt() / 1000f
                ret += if (i % 5 == 0 || i == c.size - 1) {
                    "|" + c[i].concatToString() + "| " + v + "\n"
                } else {
                    "|" + c[i].concatToString() + "|\n"
                }
            }
            val minStr = ((minX * 1000 + 0.5).toInt() / 1000f).toString()
            val maxStr = ((maxX * 1000 + 0.5).toInt() / 1000f).toString()
            var s = minStr + CharArray(dimx).concatToString().replace('\u0000', ' ')
            s = s.substring(0, dimx - maxStr.length + 2) + maxStr + "\n"
            return ret + s
        }
    }
}
