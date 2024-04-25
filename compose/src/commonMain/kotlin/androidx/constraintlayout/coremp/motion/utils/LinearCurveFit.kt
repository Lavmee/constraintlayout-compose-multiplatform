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

import kotlin.math.hypot

class LinearCurveFit : CurveFit {

    private val mT: DoubleArray
    private val mY: Array<DoubleArray>
    private var mTotalLength = Double.NaN
    private val mExtrapolate = true
    var mSlopeTemp: DoubleArray

    constructor(time: DoubleArray, y: Array<DoubleArray>) {
        val dim: Int = y[0].size
        mSlopeTemp = DoubleArray(dim)
        mT = time
        mY = y
        if (dim > 2) {
            @Suppress("unused")
            var sum = 0.0
            var lastx = 0.0
            var lasty = 0.0
            for (i in 0 until time.size) {
                val px = y[i][0]
                val py = y[i][0]
                if (i > 0) {
                    sum += hypot(px - lastx, py - lasty)
                }
                lastx = px
                lasty = py
            }
            mTotalLength = 0.0
        }
    }

    /**
     * Calculate the length traveled by the first two parameters assuming they are x and y.
     * (Added for future work)
     *
     * @param t the point to calculate the length to
     */
    @Suppress("unused")
    private fun getLength2D(t: Double): Double {
        if (mTotalLength.isNaN()) {
            return 0.0
        }
        val n: Int = mT.size
        if (t <= mT[0]) {
            return 0.0
        }
        if (t >= mT[n - 1]) {
            return mTotalLength
        }
        var sum = 0.0
        var last_x = 0.0
        var last_y = 0.0
        for (i in 0 until n - 1) {
            var px = mY[i][0]
            var py = mY[i][1]
            if (i > 0) {
                sum += hypot(px - last_x, py - last_y)
            }
            last_x = px
            last_y = py
            if (t == mT[i]) {
                return sum
            }
            if (t < mT[i + 1]) {
                val h = mT[i + 1] - mT[i]
                val x = (t - mT[i]) / h
                val x1 = mY[i][0]
                val x2 = mY[i + 1][0]
                val y1 = mY[i][1]
                val y2 = mY[i + 1][1]
                py -= y1 * (1 - x) + y2 * x
                px -= x1 * (1 - x) + x2 * x
                sum += hypot(py, px)
                return sum
            }
        }
        return 0.0
    }

    // @TODO: add description
    override fun getPos(t: Double, v: DoubleArray) {
        val n: Int = mT.size
        val dim: Int = mY[0].size
        if (mExtrapolate) {
            if (t <= mT[0]) {
                getSlope(mT[0], mSlopeTemp)
                for (j in 0 until dim) {
                    v[j] = mY[0][j] + (t - mT[0]) * mSlopeTemp[j]
                }
                return
            }
            if (t >= mT[n - 1]) {
                getSlope(mT[n - 1], mSlopeTemp)
                for (j in 0 until dim) {
                    v[j] = mY[n - 1][j] + (t - mT[n - 1]) * mSlopeTemp[j]
                }
                return
            }
        } else {
            if (t <= mT[0]) {
                for (j in 0 until dim) {
                    v[j] = mY[0][j]
                }
                return
            }
            if (t >= mT[n - 1]) {
                for (j in 0 until dim) {
                    v[j] = mY[n - 1][j]
                }
                return
            }
        }
        for (i in 0 until n - 1) {
            if (t == mT[i]) {
                for (j in 0 until dim) {
                    v[j] = mY[i][j]
                }
            }
            if (t < mT[i + 1]) {
                val h = mT[i + 1] - mT[i]
                val x = (t - mT[i]) / h
                for (j in 0 until dim) {
                    val y1 = mY[i][j]
                    val y2 = mY[i + 1][j]
                    v[j] = y1 * (1 - x) + y2 * x
                }
                return
            }
        }
    }

    // @TODO: add description
    override fun getPos(t: Double, v: FloatArray) {
        val n: Int = mT.size
        val dim: Int = mY[0].size
        if (mExtrapolate) {
            if (t <= mT[0]) {
                getSlope(mT[0], mSlopeTemp)
                for (j in 0 until dim) {
                    v[j] = (mY[0][j] + (t - mT[0]) * mSlopeTemp[j]).toFloat()
                }
                return
            }
            if (t >= mT[n - 1]) {
                getSlope(mT[n - 1], mSlopeTemp)
                for (j in 0 until dim) {
                    v[j] = (mY[n - 1][j] + (t - mT[n - 1]) * mSlopeTemp[j]).toFloat()
                }
                return
            }
        } else {
            if (t <= mT[0]) {
                for (j in 0 until dim) {
                    v[j] = mY[0][j].toFloat()
                }
                return
            }
            if (t >= mT[n - 1]) {
                for (j in 0 until dim) {
                    v[j] = mY[n - 1][j].toFloat()
                }
                return
            }
        }
        for (i in 0 until n - 1) {
            if (t == mT[i]) {
                for (j in 0 until dim) {
                    v[j] = mY[i][j].toFloat()
                }
            }
            if (t < mT[i + 1]) {
                val h = mT[i + 1] - mT[i]
                val x = (t - mT[i]) / h
                for (j in 0 until dim) {
                    val y1 = mY[i][j]
                    val y2 = mY[i + 1][j]
                    v[j] = (y1 * (1 - x) + y2 * x).toFloat()
                }
                return
            }
        }
    }

    // @TODO: add description
    override fun getPos(t: Double, j: Int): Double {
        val n: Int = mT.size
        if (mExtrapolate) {
            if (t <= mT[0]) {
                return mY[0][j] + (t - mT[0]) * getSlope(mT[0], j)
            }
            if (t >= mT[n - 1]) {
                return mY[n - 1][j] + (t - mT[n - 1]) * getSlope(mT[n - 1], j)
            }
        } else {
            if (t <= mT[0]) {
                return mY[0][j]
            }
            if (t >= mT[n - 1]) {
                return mY[n - 1][j]
            }
        }
        for (i in 0 until n - 1) {
            if (t == mT[i]) {
                return mY[i][j]
            }
            if (t < mT[i + 1]) {
                val h = mT[i + 1] - mT[i]
                val x = (t - mT[i]) / h
                val y1 = mY[i][j]
                val y2 = mY[i + 1][j]
                return y1 * (1 - x) + y2 * x
            }
        }
        return 0.0 // should never reach here
    }

    // @TODO: add description
    override fun getSlope(t: Double, v: DoubleArray) {
        var t = t
        val n: Int = mT.size
        val dim: Int = mY[0].size
        if (t <= mT[0]) {
            t = mT[0]
        } else if (t >= mT[n - 1]) {
            t = mT[n - 1]
        }
        for (i in 0 until n - 1) {
            if (t <= mT[i + 1]) {
                val h = mT[i + 1] - mT[i]
                for (j in 0 until dim) {
                    val y1 = mY[i][j]
                    val y2 = mY[i + 1][j]
                    v[j] = (y2 - y1) / h
                }
                break
            }
        }
        return
    }

    // @TODO: add description
    override fun getSlope(t: Double, j: Int): Double {
        var t = t
        val n: Int = mT.size
        if (t < mT[0]) {
            t = mT[0]
        } else if (t >= mT[n - 1]) {
            t = mT[n - 1]
        }
        for (i in 0 until n - 1) {
            if (t <= mT[i + 1]) {
                val h = mT[i + 1] - mT[i]
                val y1 = mY[i][j]
                val y2 = mY[i + 1][j]
                return (y2 - y1) / h
            }
        }
        return 0.0 // should never reach here
    }

    override fun getTimePoints(): DoubleArray {
        return mT
    }

    companion object {
        private const val TAG = "LinearCurveFit"
    }
}
