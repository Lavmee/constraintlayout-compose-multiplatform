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
                if (target is Target.Widget) {
                    assertTrue(
                        target.index < connection.from,
                        "seed $seed: widget ${connection.from} targets ${target.index}",
                    )
                }
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
    fun everyChildBehaviourIsReachable() {
        val seen = mutableSetOf<Behaviour>()
        for (seed in 1L..200L) {
            Scenarios.generate(seed).widgets.forEach { seen += it.horizontal; seen += it.vertical }
        }
        assertEquals(Behaviour.entries.toSet(), seen, "unreached: ${Behaviour.entries.toSet() - seen}")
    }

    @Test
    fun rootBehaviourIsFixedOrWrapContentOnly() {
        val seen = mutableSetOf<Behaviour>()
        for (seed in 1L..200L) {
            val scenario = Scenarios.generate(seed)
            seen += scenario.rootHorizontal
            seen += scenario.rootVertical
        }
        assertEquals(
            setOf(Behaviour.FIXED, Behaviour.WRAP_CONTENT),
            seen,
            "unreached or unexpected: ${setOf(Behaviour.FIXED, Behaviour.WRAP_CONTENT) - seen} / " +
                "${seen - setOf(Behaviour.FIXED, Behaviour.WRAP_CONTENT)}",
        )
    }

    /**
     * A wrap-content root with a minimum is the case where the root's own size becomes an output
     * of the layout pass rather than an input — the minimum can only bind if wrap-content would
     * otherwise have shrunk the root below it. If the generator never emits that combination, this
     * corner of solver behaviour goes untested.
     */
    @Test
    fun someScenariosGiveTheRootWrapContentAndAMinimum() {
        val matching = (1L..200L).map { Scenarios.generate(it) }.count {
            (it.rootHorizontal == Behaviour.WRAP_CONTENT && it.rootMinWidth > 0) ||
                (it.rootVertical == Behaviour.WRAP_CONTENT && it.rootMinHeight > 0)
        }
        assertTrue(matching >= 10, "only $matching of 200 scenarios exercise the root minimum")
    }

    @Test
    fun ratiosOnlyAppearOnWidgetsWithAMatchConstraintAxis() {
        for (seed in 1L..300L) {
            for (widget in Scenarios.generate(seed).widgets) {
                if (widget.dimensionRatio != null) {
                    assertTrue(
                        widget.horizontal == Behaviour.MATCH_CONSTRAINT ||
                            widget.vertical == Behaviour.MATCH_CONSTRAINT,
                        "seed $seed: ${widget.name} has ratio ${widget.dimensionRatio} " +
                            "but neither axis is MATCH_CONSTRAINT, so the ratio is inert",
                    )
                }
            }
        }
    }

    @Test
    fun someScenariosCarryARatio() {
        val matching = (1L..300L).count { seed ->
            Scenarios.generate(seed).widgets.any { it.dimensionRatio != null }
        }
        assertTrue(matching >= 30, "only $matching of 300 scenarios carry a ratio")
    }

    @Test
    fun circularConstraintsTargetLowerIndices() {
        for (seed in 1L..300L) {
            for (circular in Scenarios.generate(seed).circular) {
                assertTrue(
                    circular.target < circular.from,
                    "seed $seed: widget ${circular.from} circles ${circular.target}",
                )
            }
        }
    }

    @Test
    fun circularlyConstrainedWidgetsHaveNoAnchorConnections() {
        for (seed in 1L..300L) {
            val scenario = Scenarios.generate(seed)
            val circled = scenario.circular.map { it.from }.toSet()
            for (connection in scenario.connections) {
                assertTrue(
                    connection.from !in circled,
                    "seed $seed: widget ${connection.from} has both a circular constraint and " +
                        "an anchor connection, which over-constrains it",
                )
            }
        }
    }

    @Test
    fun someScenariosCarryACircularConstraint() {
        val matching = (1L..300L).count { Scenarios.generate(it).circular.isNotEmpty() }
        assertTrue(matching >= 30, "only $matching of 300 scenarios carry a circular constraint")
    }

    @Test
    fun barriersAlwaysReferenceAtLeastOneWidget() {
        for (seed in 1L..300L) {
            for (barrier in Scenarios.generate(seed).barriers) {
                assertTrue(
                    barrier.referenced.isNotEmpty(),
                    "seed $seed: ${barrier.name} references nothing and resolves to nothing useful",
                )
            }
        }
    }

    @Test
    fun connectionsToABarrierComeFromWidgetsBelowItsReferences() {
        for (seed in 1L..300L) {
            val scenario = Scenarios.generate(seed)
            for (connection in scenario.connections) {
                val target = connection.target
                if (target is Target.Barrier) {
                    val highest = scenario.barriers[target.index].referenced.max()
                    assertTrue(
                        connection.from > highest,
                        "seed $seed: widget ${connection.from} targets a barrier over widget " +
                            "$highest, which would close a cycle",
                    )
                }
            }
        }
    }

    @Test
    fun everyConnectionTargetResolves() {
        for (seed in 1L..300L) {
            val scenario = Scenarios.generate(seed)
            for (connection in scenario.connections) {
                when (val target = connection.target) {
                    is Target.Root -> Unit
                    is Target.Widget -> assertTrue(target.index in scenario.widgets.indices)
                    is Target.Barrier -> assertTrue(target.index in scenario.barriers.indices)
                    is Target.Guideline -> assertTrue(target.index in scenario.guidelines.indices)
                }
            }
        }
    }

    @Test
    fun someScenariosCarryABarrier() {
        val matching = (1L..300L).count { Scenarios.generate(it).barriers.isNotEmpty() }
        assertTrue(matching >= 30, "only $matching of 300 scenarios carry a barrier")
    }

    @Test
    fun someScenariosCarryAGuideline() {
        val matching = (1L..300L).count { Scenarios.generate(it).guidelines.isNotEmpty() }
        assertTrue(matching >= 30, "only $matching of 300 scenarios carry a guideline")
    }

    @Test
    fun everyGuidelinePositionKindIsGenerated() {
        val kinds = mutableSetOf<String>()
        for (seed in 1L..300L) {
            for (guideline in Scenarios.generate(seed).guidelines) {
                kinds += guideline.position::class.simpleName.orEmpty()
            }
        }
        assertEquals(setOf("Begin", "End", "Percent"), kinds, "generated kinds: $kinds")
    }
}
