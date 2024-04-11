/*
 * Copyright (C) 2020 The Android Open Source Project
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
package androidx.constraintlayout.coremp.motion.utils

abstract class CurveFit {

    // @TODO: add description
    abstract fun getPos(t: Double, v: DoubleArray)

    // @TODO: add description
    abstract fun getPos(t: Double, v: FloatArray)

    // @TODO: add description
    abstract fun getPos(t: Double, j: Int): Double

    // @TODO: add description
    abstract fun getSlope(t: Double, v: DoubleArray)

    // @TODO: add description
    abstract fun getSlope(t: Double, j: Int): Double

    // @TODO: add description
    abstract fun getTimePoints(): DoubleArray

    companion object {
        const val SPLINE = 0
        const val LINEAR = 1
        const val CONSTANT = 2

        // @TODO: add description
        fun get(type: Int, time: DoubleArray, y: Array<DoubleArray>): CurveFit {
            var type = type
            if (time.size == 1) {
                type = CONSTANT
            }
            return when (type) {
                SPLINE -> MonotonicCurveFit(time, y)
                CONSTANT -> Constant(time[0], y[0])
                else -> LinearCurveFit(time, y)
            }
        }

        // @TODO: add description
        fun getArc(arcModes: IntArray, time: DoubleArray, y: Array<DoubleArray>): CurveFit {
            return ArcCurveFit(arcModes, time, y)
        }

        class Constant(time: Double, value: DoubleArray) : CurveFit() {
            var mTime = time
            var mValue: DoubleArray = value

            override fun getPos(t: Double, v: DoubleArray) {
                mValue.copyInto(v, 0, 0)
                // System.arraycopy(mValue, 0, v, 0, mValue.size)
            }

            override fun getPos(t: Double, v: FloatArray) {
                for (i in mValue.indices) {
                    v[i] = mValue[i].toFloat()
                }
            }

            override fun getPos(t: Double, j: Int): Double {
                return mValue[j]
            }

            override fun getSlope(t: Double, v: DoubleArray) {
                for (i in mValue.indices) {
                    v[i] = 0.0
                }
            }

            override fun getSlope(t: Double, j: Int): Double {
                return 0.0
            }

            override fun getTimePoints(): DoubleArray {
                return doubleArrayOf(mTime)
            }
        }
    }
}
