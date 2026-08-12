// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package androidx.constraintlayout.core.motion

import androidx.constraintlayout.core.motion.utils.ArcCurveFit
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * `CurveFit.getPos(Double, FloatArray)` is public API, and `ArcCurveFit`'s override of it went
 * unexercised: the library's own arc paths all use the `DoubleArray` overload, and the inherited
 * suite reads positions through `getPos(t, index)`.
 *
 * Nothing therefore noticed that the port had translated upstream's narrowing `(float)` conversions
 * as `as Float`, a type cast that throws `ClassCastException` on a `Double`. Every call to this
 * overload failed, in range and extrapolating alike. These cases keep it exercised.
 */
class ArcCurveFitFloatPosTest {
    private fun fit() = ArcCurveFit(
        intArrayOf(ArcCurveFit.ARC_START_VERTICAL, ArcCurveFit.ARC_START_HORIZONTAL),
        doubleArrayOf(0.0, 1.0, 2.0),
        arrayOf(
            doubleArrayOf(0.0, 0.0),
            doubleArrayOf(1.0, 1.0),
            doubleArrayOf(2.0, 0.0),
        ),
    )

    @Test
    fun inRangePositionMatchesTheIndexedOverload() {
        val spline = fit()
        val out = FloatArray(2)
        spline.getPos(0.5, out)
        assertEquals(spline.getPos(0.5, 0).toFloat(), out[0], 0.0001f)
        assertEquals(spline.getPos(0.5, 1).toFloat(), out[1], 0.0001f)
    }

    @Test
    fun atAKeyframePositionMatchesTheIndexedOverload() {
        val spline = fit()
        val out = FloatArray(2)
        spline.getPos(1.0, out)
        assertEquals(spline.getPos(1.0, 0).toFloat(), out[0], 0.0001f)
        assertEquals(spline.getPos(1.0, 1).toFloat(), out[1], 0.0001f)
    }

    @Test
    fun extrapolatingBelowTheRangeDoesNotThrow() {
        val out = FloatArray(2)
        fit().getPos(-1.0, out)
    }

    @Test
    fun extrapolatingAboveTheRangeDoesNotThrow() {
        val out = FloatArray(2)
        fit().getPos(3.0, out)
    }

}
