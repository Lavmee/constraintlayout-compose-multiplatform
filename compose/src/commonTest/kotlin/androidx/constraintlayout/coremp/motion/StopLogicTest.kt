/*
 * Copyright (C) 2022 The Android Open Source Project
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

import androidx.constraintlayout.coremp.motion.utils.SpringStopEngine
import androidx.constraintlayout.coremp.motion.utils.StopEngine
import androidx.constraintlayout.coremp.motion.utils.StopLogicEngine
import kotlin.math.max
import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertEquals

class StopLogicTest {
    @Test
    fun basicSpring() {
        val stop = SpringStopEngine()
        val position = 0.3f
        val destination = 1f
        val currentVelocity = 0.2f
        val maxTime = 0.9f
        val mass = 1f
        val stiffness = 100f
        val damping = 50f
        val stopThreshold = 0f
        val springBoundary = 0
        val expectStopped = false // Doesn't make it to 1.0f in the given time
        stop.springConfig(
            position,
            destination,
            currentVelocity,
            mass,
            stiffness,
            damping,
            stopThreshold,
            springBoundary,
        )
        val expect = """
            |*                                                           | 0.0
            |                                                            |
            |                                                            |
            |                                                            |
            |**                                                          |
            |  ****                                                      | 0.316
            |      ***                                                   |
            |         ****                                               |
            |             ****                                           |
            |                 *****                                      |
            |                      ******                                | 0.632
            |                            *******                         |
            |                                   **********               |
            |                                             *************  |
            |                                                           *| 0.885
            0.0                                                      0.885
            
        """.trimIndent()
        assertEquals(expect, verify(stop, position, maxTime, expectStopped))
    }

    @Test
    fun cruseDecelerate() {
        // cruse decelerate
        val stop = StopLogicEngine()
        val position = 0.9f
        val destination = 1f
        val currentVelocity = 0.2f
        val maxTime = 0.9f
        val maxAcceleration = 3.2f
        val maxVelocity = 3.2f
        val expectStopped = true
        stop.config(position, destination, currentVelocity, maxTime, maxAcceleration, maxVelocity)
        println(stop.debug("check1", 0f))
        val expect = """
            |*                                                           | 0.0
            |                                                            |
            |                                                            |
            |                                                            |
            |                                                            |
            |                                                            | 0.357
            |                                                            |
            |                                                            |
            |                                                            |
            |                                                            |
            |                                                            | 0.714
            |                                                            |
            |**********                                                  |
            |          *************************                         |
            |                                   *********************** *| 1.0
            0.0                                                      0.885
            
        """.trimIndent()
        assertEquals(expect, verify(stop, position, maxTime, expectStopped))
    }

    @Test
    fun backwardAccelerateDecelerate() {
        // backward accelerate, decelerate
        val stop = StopLogicEngine()
        val position = 0.9f
        val destination = 1f
        val currentVelocity = -0.2f
        val maxTime = 0.9f
        val maxAcceleration = 3.2f
        val maxVelocity = 3.2f
        val expectStopped = true
        stop.config(position, destination, currentVelocity, maxTime, maxAcceleration, maxVelocity)
        println(stop.debug("check1", 0f))
        val expect = """
            |*                                                           | 0.0
            |                                                            |
            |                                                            |
            |                                                            |
            |                                                            |
            |                                                            | 0.357
            |                                                            |
            |                                                            |
            |                                                            |
            |                                                            |
            |                                                            | 0.714
            |                                                            |
            |**************                                              |
            |              **************                                |
            |                            ****************************** *| 1.0
            0.0                                                      0.885
            
        """.trimIndent()
        assertEquals(expect, verify(stop, position, maxTime, expectStopped))
    }

    @Test
    fun hardStop() {
        val stop = StopLogicEngine()
        val position = 0.9f
        val destination = 1f
        val currentVelocity = 1.8f
        val maxTime = 0.9f
        val maxAcceleration = 3.2f
        val maxVelocity = 3.2f
        val expectStopped = true
        stop.config(position, destination, currentVelocity, maxTime, maxAcceleration, maxVelocity)
        println(stop.debug("check1", 0f))
        val expect = """
            |*                                                           | 0.0
            |                                                            |
            |                                                            |
            |                                                            |
            |                                                            |
            |                                                            | 0.357
            |                                                            |
            |                                                            |
            |                                                            |
            |                                                            |
            |                                                            | 0.714
            |                                                            |
            |**                                                          |
            |  ******                                                    |
            |        ************************************************** *| 1.0
            0.0                                                      0.885
            
        """.trimIndent()
        assertEquals(expect, verify(stop, position, maxTime, expectStopped))
    }

    @Test
    fun accelerateCruseDecelerate() {
        val stop = StopLogicEngine()
        val position = 0.3f
        val destination = 1f
        val currentVelocity = 0.1f
        val maxTime = 0.9f
        val maxAcceleration = 3.2f
        val maxVelocity = 1.2f
        val expectStopped = false // Doesn't make it to 1f in the given time
        stop.config(position, destination, currentVelocity, maxTime, maxAcceleration, maxVelocity)
        println(stop.debug("check1", 0f))
        val expect = """
            |*                                                           | 0.0
            |                                                            |
            |                                                            |
            |                                                            |
            |***********                                                 |
            |           ******                                           | 0.356
            |                 *****                                      |
            |                      ****                                  |
            |                          ***                               |
            |                             ****                           |
            |                                 ****                       | 0.712
            |                                     *****                  |
            |                                          *****             |
            |                                               ***********  |
            |                                                           *| 0.997
            0.0                                                      0.885
            
        """.trimIndent()
        assertEquals(expect, verify(stop, position, maxTime, expectStopped))
    }

    @Test
    fun accelerateDecelerate() {
        val stop = StopLogicEngine()
        val position = 0.3f
        val destination = 1f
        val currentVelocity = 0.2f
        val maxTime = 0.9f
        val maxAcceleration = 3.2f
        val maxVelocity = 3.2f
        val expectStopped = true
        stop.config(position, destination, currentVelocity, maxTime, maxAcceleration, maxVelocity)
        println(stop.debug("check1", 0f))
        val expect = """
            |*                                                           | 0.0
            |                                                            |
            |                                                            |
            |                                                            |
            |*********                                                   |
            |         ******                                             | 0.357
            |               *****                                        |
            |                    ****                                    |
            |                        ***                                 |
            |                           ***                              |
            |                              ****                          | 0.714
            |                                  ****                      |
            |                                      ******                |
            |                                            **************  |
            |                                                           *| 1.0
            0.0                                                      0.885
            
        """.trimIndent()
        assertEquals(expect, verify(stop, position, maxTime, expectStopped))
    }

    @Test
    fun backwardAccelerateCruseDecelerate() {
        val stop = StopLogicEngine()
        val position = 0.5f
        val destination = 1f
        val currentVelocity = -0.6f
        val maxTime = 0.9f
        val maxAcceleration = 5.2f
        val maxVelocity = 1.2f
        val expectStopped = true
        stop.config(position, destination, currentVelocity, maxTime, maxAcceleration, maxVelocity)
        println(stop.debug("check1", 0f))
        val expect = """
            |*                                                           | 0.0
            |                                                            |
            |                                                            |
            |                                                            |
            |                                                            |
            |                                                            | 0.357
            | ***************                                            |
            |*               *****                                       |
            |                     ****                                   |
            |                         ****                               |
            |                             ****                           | 0.714
            |                                 ****                       |
            |                                     *****                  |
            |                                          **********        |
            |                                                    ****** *| 1.0
            0.0                                                      0.885
            
        """.trimIndent()
        assertEquals(expect, verify(stop, position, maxTime, expectStopped))
    }

    companion object {
        private fun verify(
            stop: StopEngine,
            position: Float,
            maxTime: Float,
            expectStopped: Boolean,
        ): String {
            var p: Float = stop.getInterpolation(0f)
            assertEquals(p, position, 0.0001f)
            val count = 60
            val step = maxTime / (count - 1)
            val x = FloatArray(count)
            val y = FloatArray(count)
            var c = 0
            var t = 0f
            while (t < maxTime) {
                p = stop.getInterpolation(t)
                x[c] = t
                y[c] = p
                c++
                t += step
            }
            val ret = textDraw(count, count / 4, x, y, false)
            println(ret)
            assertEquals(expectStopped, stop.isStopped)
            return ret
        }

        fun textDraw(dimx: Int, dimy: Int, x: FloatArray, y: FloatArray, flip: Boolean): String {
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
