// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.motion

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScenariosTest {
    @Test
    fun theSameSeedProducesTheSameScenario() {
        assertEquals(Scenarios.generate(17L), Scenarios.generate(17L))
    }

    @Test
    fun differentSeedsProduceDifferentScenarios() {
        val distinct = (0L until 50L).map { Scenarios.generate(it) }.distinct()
        assertTrue(distinct.size > 40, "generator produced ${distinct.size} distinct scenarios in 50")
    }

    @Test
    fun bothKeyKindsAppear() {
        val keys = (0L until 200L).flatMap { Scenarios.generate(it).keys }
        assertTrue(keys.any { it is KeySpec.PositionKey }, "no position keys generated")
        assertTrue(keys.any { it is KeySpec.AttributesKey }, "no attributes keys generated")
    }

    @Test
    fun everyArcModeAppears() {
        val arcs = (0L until 200L).map { Scenarios.generate(it).pathMotionArc }.toSet()
        assertEquals(setOf(-1, 0, 1, 2, 3, 4, 5), arcs)
    }

    @Test
    fun framePositionsAreInsideTheTransition() {
        (0L until 200L).flatMap { Scenarios.generate(it).keys }.forEach {
            assertTrue(it.framePosition in 1..99, "frame position ${it.framePosition} outside 1..99")
        }
    }
}
