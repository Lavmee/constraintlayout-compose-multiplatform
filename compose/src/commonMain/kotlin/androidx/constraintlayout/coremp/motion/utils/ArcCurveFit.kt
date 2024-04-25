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

import androidx.constraintlayout.coremp.ext.Math
import androidx.constraintlayout.coremp.ext.binarySearch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

class ArcCurveFit : CurveFit {

    private val mTime: DoubleArray
    private var mArcs: Array<Arc>
    private val mExtrapolate = true

    override fun getPos(t: Double, v: DoubleArray) {
        var t = t
        if (mExtrapolate) {
            if (t < mArcs[0].mTime1) {
                val t0: Double = mArcs[0].mTime1
                val dt: Double = t - mArcs[0].mTime1
                val p = 0
                if (mArcs[p].mLinear) {
                    v[0] = mArcs[p].getLinearX(t0) + dt * mArcs[p].getLinearDX(t0)
                    v[1] = mArcs[p].getLinearY(t0) + dt * mArcs[p].getLinearDY(t0)
                } else {
                    mArcs[p].setPoint(t0)
                    v[0] = mArcs[p].getX() + dt * mArcs[p].getDX()
                    v[1] = mArcs[p].getY() + dt * mArcs[p].getDY()
                }
                return
            }
            if (t > mArcs[mArcs.size - 1].mTime2) {
                val t0: Double = mArcs[mArcs.size - 1].mTime2
                val dt = t - t0
                val p: Int = mArcs.size - 1
                if (mArcs[p].mLinear) {
                    v[0] = mArcs[p].getLinearX(t0) + dt * mArcs[p].getLinearDX(t0)
                    v[1] = mArcs[p].getLinearY(t0) + dt * mArcs[p].getLinearDY(t0)
                } else {
                    mArcs[p].setPoint(t)
                    v[0] = mArcs[p].getX() + dt * mArcs[p].getDX()
                    v[1] = mArcs[p].getY() + dt * mArcs[p].getDY()
                }
                return
            }
        } else {
            if (t < mArcs[0].mTime1) {
                t = mArcs[0].mTime1
            }
            if (t > mArcs[mArcs.size - 1].mTime2) {
                t = mArcs[mArcs.size - 1].mTime2
            }
        }
        for (i in 0 until mArcs.size) {
            if (t <= mArcs[i].mTime2) {
                if (mArcs[i].mLinear) {
                    v[0] = mArcs[i].getLinearX(t)
                    v[1] = mArcs[i].getLinearY(t)
                    return
                }
                mArcs[i].setPoint(t)
                v[0] = mArcs[i].getX()
                v[1] = mArcs[i].getY()
                return
            }
        }
    }

    override fun getPos(t: Double, v: FloatArray) {
        var t = t
        if (mExtrapolate) {
            if (t < mArcs[0].mTime1) {
                val t0: Double = mArcs[0].mTime1
                val dt: Double = t - mArcs[0].mTime1
                val p = 0
                if (mArcs[p].mLinear) {
                    v[0] = (mArcs[p].getLinearX(t0) + dt * mArcs[p].getLinearDX(t0)) as Float
                    v[1] = (mArcs[p].getLinearY(t0) + dt * mArcs[p].getLinearDY(t0)) as Float
                } else {
                    mArcs[p].setPoint(t0)
                    v[0] = (mArcs[p].getX() + dt * mArcs[p].getDX()) as Float
                    v[1] = (mArcs[p].getY() + dt * mArcs[p].getDY()) as Float
                }
                return
            }
            if (t > mArcs[mArcs.size - 1].mTime2) {
                val t0: Double = mArcs[mArcs.size - 1].mTime2
                val dt = t - t0
                val p: Int = mArcs.size - 1
                if (mArcs[p].mLinear) {
                    v[0] = (mArcs[p].getLinearX(t0) + dt * mArcs[p].getLinearDX(t0)) as Float
                    v[1] = (mArcs[p].getLinearY(t0) + dt * mArcs[p].getLinearDY(t0)) as Float
                } else {
                    mArcs[p].setPoint(t)
                    v[0] = mArcs[p].getX() as Float
                    v[1] = mArcs[p].getY() as Float
                }
                return
            }
        } else {
            if (t < mArcs[0].mTime1) {
                t = mArcs[0].mTime1
            } else if (t > mArcs[mArcs.size - 1].mTime2) {
                t = mArcs[mArcs.size - 1].mTime2
            }
        }
        for (i in 0 until mArcs.size) {
            if (t <= mArcs[i].mTime2) {
                if (mArcs[i].mLinear) {
                    v[0] = mArcs[i].getLinearX(t) as Float
                    v[1] = mArcs[i].getLinearY(t) as Float
                    return
                }
                mArcs[i].setPoint(t)
                v[0] = mArcs[i].getX() as Float
                v[1] = mArcs[i].getY() as Float
                return
            }
        }
    }

    override fun getSlope(t: Double, v: DoubleArray) {
        var t = t
        if (t < mArcs[0].mTime1) {
            t = mArcs[0].mTime1
        } else if (t > mArcs[mArcs.size - 1].mTime2) {
            t = mArcs[mArcs.size - 1].mTime2
        }
        for (i in 0 until mArcs.size) {
            if (t <= mArcs[i].mTime2) {
                if (mArcs[i].mLinear) {
                    v[0] = mArcs[i].getLinearDX(t)
                    v[1] = mArcs[i].getLinearDY(t)
                    return
                }
                mArcs[i].setPoint(t)
                v[0] = mArcs[i].getDX()
                v[1] = mArcs[i].getDY()
                return
            }
        }
    }

    override fun getPos(t: Double, j: Int): Double {
        var t = t
        if (mExtrapolate) {
            if (t < mArcs[0].mTime1) {
                val t0: Double = mArcs[0].mTime1
                val dt: Double = t - mArcs[0].mTime1
                val p = 0
                return if (mArcs[p].mLinear) {
                    if (j == 0) {
                        mArcs[p].getLinearX(t0) + dt * mArcs[p].getLinearDX(t0)
                    } else {
                        mArcs[p].getLinearY(t0) + dt * mArcs[p].getLinearDY(t0)
                    }
                } else {
                    mArcs[p].setPoint(t0)
                    if (j == 0) {
                        mArcs[p].getX() + dt * mArcs[p].getDX()
                    } else {
                        mArcs[p].getY() + dt * mArcs[p].getDY()
                    }
                }
            }
            if (t > mArcs[mArcs.size - 1].mTime2) {
                val t0: Double = mArcs[mArcs.size - 1].mTime2
                val dt = t - t0
                val p: Int = mArcs.size - 1
                return if (j == 0) {
                    mArcs[p].getLinearX(t0) + dt * mArcs[p].getLinearDX(t0)
                } else {
                    mArcs[p].getLinearY(t0) + dt * mArcs[p].getLinearDY(t0)
                }
            }
        } else {
            if (t < mArcs[0].mTime1) {
                t = mArcs[0].mTime1
            } else if (t > mArcs[mArcs.size - 1].mTime2) {
                t = mArcs[mArcs.size - 1].mTime2
            }
        }
        for (i in 0 until mArcs.size) {
            if (t <= mArcs[i].mTime2) {
                if (mArcs[i].mLinear) {
                    return if (j == 0) {
                        mArcs[i].getLinearX(t)
                    } else {
                        mArcs[i].getLinearY(t)
                    }
                }
                mArcs[i].setPoint(t)
                return if (j == 0) {
                    mArcs[i].getX()
                } else {
                    mArcs[i].getY()
                }
            }
        }
        return Double.NaN
    }

    override fun getSlope(t: Double, j: Int): Double {
        var t = t
        if (t < mArcs[0].mTime1) {
            t = mArcs[0].mTime1
        }
        if (t > mArcs[mArcs.size - 1].mTime2) {
            t = mArcs[mArcs.size - 1].mTime2
        }
        for (i in 0 until mArcs.size) {
            if (t <= mArcs[i].mTime2) {
                if (mArcs[i].mLinear) {
                    return if (j == 0) {
                        mArcs[i].getLinearDX(t)
                    } else {
                        mArcs[i].getLinearDY(t)
                    }
                }
                mArcs[i].setPoint(t)
                return if (j == 0) {
                    mArcs[i].getDX()
                } else {
                    mArcs[i].getDY()
                }
            }
        }
        return Double.NaN
    }

    override fun getTimePoints(): DoubleArray {
        return mTime
    }

    constructor(arcModes: IntArray, time: DoubleArray, y: Array<DoubleArray>) {
        mTime = time
        var mode = START_VERTICAL
        var last = START_VERTICAL
        mArcs = Array(time.size - 1) { i ->
            when (arcModes[i]) {
                ARC_START_VERTICAL -> {
                    mode = START_VERTICAL
                    last = mode
                }

                ARC_START_HORIZONTAL -> {
                    mode = START_HORIZONTAL
                    last = mode
                }

                ARC_START_FLIP -> {
                    mode = if (last == START_VERTICAL) START_HORIZONTAL else START_VERTICAL
                    last = mode
                }

                ARC_START_LINEAR -> mode = START_LINEAR
                ARC_ABOVE -> mode = UP_ARC
                ARC_BELOW -> mode = DOWN_ARC
            }
            Arc(
                mode,
                time[i],
                time[i + 1],
                y[i][0],
                y[i][1],
                y[i + 1][0],
                y[i + 1][1],
            )
        }
    }

    companion object {
        const val ARC_START_VERTICAL = 1
        const val ARC_START_HORIZONTAL = 2
        const val ARC_START_FLIP = 3
        const val ARC_BELOW = 4
        const val ARC_ABOVE = 5

        const val ARC_START_LINEAR = 0

        private const val START_VERTICAL = 1
        private const val START_HORIZONTAL = 2
        private const val START_LINEAR = 3
        private const val DOWN_ARC = 4
        private const val UP_ARC = 5

        class Arc {

            private var mLut: DoubleArray? = null
            private var mArcDistance = 0.0
            var mTime1 = 0.0
            var mTime2 = 0.0
            private var mX1 = 0.0
            private var mX2: Double = 0.0
            private var mY1: Double = 0.0
            private var mY2: Double = 0.0
            private var mOneOverDeltaTime = 0.0
            private var mEllipseA = 0.0
            private var mEllipseB = 0.0
            private var mEllipseCenterX = 0.0 // also used to cache the slope in the unused center

            private var mEllipseCenterY = 0.0 // also used to cache the slope in the unused center

            private var mArcVelocity = 0.0
            private var mTmpSinAngle = 0.0
            private var mTmpCosAngle = 0.0
            private var mVertical = false
            var mLinear = false

            constructor(mode: Int, t1: Double, t2: Double, x1: Double, y1: Double, x2: Double, y2: Double) {
                val dx = x2 - x1
                val dy = y2 - y1
                mVertical = when (mode) {
                    START_VERTICAL -> true
                    UP_ARC -> dy < 0
                    DOWN_ARC -> dy > 0
                    else -> false
                }

                mTime1 = t1
                mTime2 = t2
                mOneOverDeltaTime = 1 / (mTime2 - mTime1)
                if (START_LINEAR == mode) {
                    mLinear = true
                }

                if (mLinear || abs(dx) < EPSILON || abs(dy) < EPSILON) {
                    mLinear = true
                    mX1 = x1
                    mX2 = x2
                    mY1 = y1
                    mY2 = y2
                    mArcDistance = hypot(dy, dx)
                    mArcVelocity = mArcDistance * mOneOverDeltaTime
                    mEllipseCenterX = dx / (mTime2 - mTime1) // cache the slope in the unused center
                    mEllipseCenterY = dy / (mTime2 - mTime1) // cache the slope in the unused center
                    return
                }
                mLut = DoubleArray(101)
                mEllipseA = dx * if (mVertical) -1 else 1
                mEllipseB = dy * if (mVertical) 1 else -1
                mEllipseCenterX = if (mVertical) x2 else x1
                mEllipseCenterY = if (mVertical) y1 else y2
                buildTable(x1, y1, x2, y2)
                mArcVelocity = mArcDistance * mOneOverDeltaTime
            }

            fun setPoint(time: Double) {
                val percent = (if (mVertical) mTime2 - time else time - mTime1) * mOneOverDeltaTime
                val angle: Double = PI * 0.5 * lookup(percent)
                mTmpSinAngle = sin(angle)
                mTmpCosAngle = cos(angle)
            }

            fun getX(): Double {
                return mEllipseCenterX + mEllipseA * mTmpSinAngle
            }

            fun getY(): Double {
                return mEllipseCenterY + mEllipseB * mTmpCosAngle
            }

            fun getDX(): Double {
                val vx = mEllipseA * mTmpCosAngle
                val vy = -mEllipseB * mTmpSinAngle
                val norm = mArcVelocity / hypot(vx, vy)
                return if (mVertical) -vx * norm else vx * norm
            }

            fun getDY(): Double {
                val vx = mEllipseA * mTmpCosAngle
                val vy = -mEllipseB * mTmpSinAngle
                val norm = mArcVelocity / hypot(vx, vy)
                return if (mVertical) -vy * norm else vy * norm
            }

            fun getLinearX(t: Double): Double {
                var t = t
                t = (t - mTime1) * mOneOverDeltaTime
                return mX1 + t * (mX2 - mX1)
            }

            fun getLinearY(t: Double): Double {
                var t = t
                t = (t - mTime1) * mOneOverDeltaTime
                return mY1 + t * (mY2 - mY1)
            }

            fun getLinearDX(t: Double): Double {
                return mEllipseCenterX
            }

            fun getLinearDY(t: Double): Double {
                return mEllipseCenterY
            }

            fun lookup(v: Double): Double {
                if (v <= 0) {
                    return 0.0
                }
                if (v >= 1) {
                    return 1.0
                }
                val pos: Double = v * (mLut!!.size - 1)
                val iv = pos.toInt()
                val off = pos - pos.toInt()
                return mLut!![iv] + off * (mLut!![iv + 1] - mLut!![iv])
            }

            private fun buildTable(x1: Double, y1: Double, x2: Double, y2: Double) {
                val a = x2 - x1
                val b = y1 - y2
                var lx = 0.0
                var ly = 0.0
                var dist = 0.0
                for (i in 0 until sOurPercent.size) {
                    val angle = Math.toRadians(90.0 * i / (sOurPercent.size - 1))
                    val s = sin(angle)
                    val c = cos(angle)
                    val px = a * s
                    val py = b * c
                    if (i > 0) {
                        dist += hypot(px - lx, py - ly)
                        sOurPercent[i] = dist
                    }
                    lx = px
                    ly = py
                }
                mArcDistance = dist
                for (i in 0 until sOurPercent.size) {
                    sOurPercent[i] /= dist
                }
                for (i in 0 until mLut!!.size) {
                    val pos = i / (mLut!!.size - 1).toDouble()
                    val index = sOurPercent.binarySearch(pos)
                    if (index >= 0) {
                        mLut!![i] = index / (sOurPercent.size - 1).toDouble()
                    } else if (index == -1) {
                        mLut!![i] = 0.0
                    } else {
                        val p1 = -index - 2
                        val p2 = -index - 1
                        val ans: Double = (
                            p1 + (pos - sOurPercent[p1]) /
                                (sOurPercent[p2] - sOurPercent[p1])
                            ) / (sOurPercent.size - 1)
                        mLut!![i] = ans
                    }
                }
            }

            companion object {
                private const val TAG = "Arc"
                private val sOurPercent = DoubleArray(91)
                private const val EPSILON = 0.001
            }
        }
    }
}
