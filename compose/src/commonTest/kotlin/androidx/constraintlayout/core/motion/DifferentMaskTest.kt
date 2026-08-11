// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package androidx.constraintlayout.core.motion

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * `different()` accumulates, per animated property, whether it changes between two keyframes.
 * Upstream writes each slot as `mask[c++] |= expr`, which in Java reads and writes the same slot.
 * Kotlin has no `|=` for `Boolean`, and the expansion the port used evaluated the index twice — so
 * a slot took in its neighbour's value instead of its own, and `Motion` went on to build splines
 * for the wrong set of properties.
 *
 * These cases pin the mask down slot by slot, which is the only way the off-by-one shows: the
 * inherited suite checks rendered motion, where a wrong mask still produces plausible output.
 */
class DifferentMaskTest {
    private fun paths(x: Float, y: Float, width: Float, height: Float, position: Float) =
        MotionPaths().also {
            it.mX = x
            it.mY = y
            it.mWidth = width
            it.mHeight = height
            it.mPosition = position
        }

    @Test
    fun aSingleChangedPropertyMarksOnlyItsOwnSlot() {
        val a = paths(x = 0f, y = 0f, width = 10f, height = 20f, position = 0f)
        val b = paths(x = 0f, y = 0f, width = 99f, height = 20f, position = 0f)
        val mask = BooleanArray(5)

        a.different(b, mask, arrayOf(), false)

        // Slots are position, x, y, width, height. Only width differs.
        assertEquals(listOf(false, false, false, true, false), mask.toList())
    }

    @Test
    fun identicalPathsLeaveEverySlotClear() {
        val a = paths(x = 1f, y = 2f, width = 3f, height = 4f, position = 5f)
        val b = paths(x = 1f, y = 2f, width = 3f, height = 4f, position = 5f)
        val mask = BooleanArray(5)

        a.different(b, mask, arrayOf(), false)

        assertEquals(List(5) { false }, mask.toList())
    }

    @Test
    fun aChangedPositionMarksTheFirstSlot() {
        val a = paths(x = 0f, y = 0f, width = 10f, height = 20f, position = 0f)
        val b = paths(x = 0f, y = 0f, width = 10f, height = 20f, position = 1f)
        val mask = BooleanArray(5)

        a.different(b, mask, arrayOf(), false)

        assertEquals(listOf(true, false, false, false, false), mask.toList())
    }

    @Test
    fun accumulationAcrossCallsIsRetained() {
        val mask = BooleanArray(5)
        val base = paths(x = 0f, y = 0f, width = 10f, height = 20f, position = 0f)

        base.different(paths(0f, 0f, 99f, 20f, 0f), mask, arrayOf(), false)
        base.different(paths(0f, 0f, 10f, 77f, 0f), mask, arrayOf(), false)

        // Width from the first call, height from the second; neither clears the other.
        assertEquals(listOf(false, false, false, true, true), mask.toList())
    }

    @Test
    fun arcModeMarksBothPositionSlots() {
        val a = paths(x = 0f, y = 0f, width = 10f, height = 20f, position = 0f)
        val b = paths(x = 0f, y = 0f, width = 10f, height = 20f, position = 0f)
        val mask = BooleanArray(5)

        a.different(b, mask, arrayOf(), true)

        assertEquals(listOf(false, true, true, false, false), mask.toList())
    }
}
