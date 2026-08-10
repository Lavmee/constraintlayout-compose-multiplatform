// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.solver

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScenariosTest {
    @Test
    fun generationIsDeterministic() {
        assertEquals(Scenarios.generate(1), Scenarios.generate(1))
    }

    @Test
    fun differentSeedsGiveDifferentScenarios() {
        val scenarios = (1L..50L).map { Scenarios.generate(it) }
        assertTrue(scenarios.toSet().size > 40, "only ${scenarios.toSet().size} distinct of 50")
    }

    @Test
    fun connectionsAreAcyclic() {
        for (seed in 1L..200L) {
            val scenario = Scenarios.generate(seed)
            for (connection in scenario.connections) {
                val target = connection.target
                assertTrue(
                    target == null || target < connection.from,
                    "seed $seed: widget ${connection.from} targets $target",
                )
            }
        }
    }

    @Test
    fun connectionsJoinTheSameAxis() {
        for (seed in 1L..200L) {
            for (connection in Scenarios.generate(seed).connections) {
                assertEquals(
                    connection.fromSide.isHorizontal,
                    connection.toSide.isHorizontal,
                    "seed $seed: ${connection.fromSide} -> ${connection.toSide}",
                )
            }
        }
    }

    @Test
    fun everyBehaviourIsReachable() {
        val seen = mutableSetOf<Behaviour>()
        for (seed in 1L..200L) {
            val scenario = Scenarios.generate(seed)
            seen += scenario.rootHorizontal
            seen += scenario.rootVertical
            scenario.widgets.forEach { seen += it.horizontal; seen += it.vertical }
        }
        assertEquals(Behaviour.entries.toSet(), seen, "unreached: ${Behaviour.entries.toSet() - seen}")
    }

    /**
     * The acceptance probe in Task 4 reverts the minimum-size fix from #336, which only bites a
     * container whose wrap-content size lands below its minimum. If the generator never emits that
     * combination the probe cannot fire, so it is a requirement rather than a happy accident.
     */
    @Test
    fun someScenariosGiveTheRootWrapContentAndAMinimum() {
        val matching = (1L..200L).map { Scenarios.generate(it) }.count {
            (it.rootHorizontal == Behaviour.WRAP_CONTENT && it.rootMinWidth > 0) ||
                (it.rootVertical == Behaviour.WRAP_CONTENT && it.rootMinHeight > 0)
        }
        assertTrue(matching >= 10, "only $matching of 200 scenarios exercise the root minimum")
    }
}
