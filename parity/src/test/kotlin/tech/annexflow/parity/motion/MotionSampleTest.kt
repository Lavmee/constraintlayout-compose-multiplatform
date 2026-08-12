// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.motion

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MotionSampleTest {
    private fun sample() =
        MotionSample(
            positions = listOf(
                PositionSample(
                    p = 0.5f, left = 1, top = 2, right = 3, bottom = 4,
                    rotationZ = 5f, scaleX = 6f, scaleY = 7f, alpha = 8f,
                    translationX = 9f, translationY = 10f,
                    centerX = 11f, centerY = 12f, velocityX = 13f, velocityY = 14f,
                ),
            ),
            path = listOf(1f, 2f),
            keyFrames = listOf(3f, 4f),
            keyFrameModes = listOf(5),
            keyFramePositions = listOf(6),
            keyFrameCount = 1,
        )

    @Test
    fun renderingIsStable() {
        assertEquals(sample().render(), sample().render())
    }

    @Test
    fun everyFieldReachesTheSnapshot() {
        val rendered = sample().render()
        listOf("0.5", "1,2,3,4", "5.0", "13.0", "count=1").forEach {
            assertTrue(it in rendered, "'$it' missing from:\n$rendered")
        }
    }

    @Test
    fun negativeZeroStaysDistinctFromZero() {
        val zero = sample().copy(path = listOf(0f))
        val negativeZero = sample().copy(path = listOf(-0f))
        assertTrue(zero.render() != negativeZero.render(), "-0.0 collapsed into 0.0")
    }

    @Test
    fun nanRendersIdenticallyOnBothSides() {
        val a = sample().copy(path = listOf(Float.NaN))
        val b = sample().copy(path = listOf(Float.NaN))
        assertEquals(a.render(), b.render())
    }

    @Test
    fun exceptionsAreCategorisedPortably() {
        assertEquals("IndexOutOfBounds", MotionOutcome.categorise(IndexOutOfBoundsException()))
        assertEquals("NullPointer", MotionOutcome.categorise(NullPointerException()))
        assertEquals("Arithmetic", MotionOutcome.categorise(ArithmeticException()))
        assertEquals("IllegalStateException", MotionOutcome.categorise(IllegalStateException()))
    }
}
