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
            // A chain's own forward link is the one deliberate exception: it targets the next
            // (higher-indexed) member, which is what makes the run a chain. That exception is
            // scoped to exactly those (from, target) pairs — a chain member's *backward* link
            // still only ever targets the root or a lower-indexed widget, same as everything else.
            val chainForwardLinks = scenario.chains.flatMap { it.members.zipWithNext() }.toSet()
            for (connection in scenario.connections) {
                val target = connection.target
                if (target is Target.Widget && (connection.from to target.index) !in chainForwardLinks) {
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

    /**
     * `someScenariosCarryABarrier` and `someScenariosCarryAGuideline` below only prove those
     * participants get constructed — neither proves any connection actually points at one. An
     * off-by-one in the barrier index or axis filtering inside `pickTarget` could silently drop
     * every barrier (or guideline) connection while every other test, including those two, stayed
     * green: the branch would claim coverage it did not have. This also folds in what
     * `everyConnectionTargetResolves` used to check — every target's index is constructed from
     * `.indices` inside `pickTarget`, so resolution can never fail; a variant simply never
     * appearing was the real gap.
     */
    @Test
    fun everyTargetVariantIsEmittedAsAConnectionTarget() {
        val kinds = mutableSetOf<String>()
        for (seed in 1L..300L) {
            for (connection in Scenarios.generate(seed).connections) {
                kinds += connection.target::class.simpleName.orEmpty()
            }
        }
        assertEquals(setOf("Root", "Widget", "Barrier", "Guideline"), kinds, "generated kinds: $kinds")
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

    @Test
    fun someScenariosCarryAChain() {
        val matching = (1L..300L).count { Scenarios.generate(it).chains.isNotEmpty() }
        assertTrue(matching >= 30, "only $matching of 300 scenarios carry a chain")
    }

    @Test
    fun chainMembersAreContiguousAndAtLeastTwo() {
        for (seed in 1L..300L) {
            for (chain in Scenarios.generate(seed).chains) {
                assertTrue(chain.members.size >= 2, "seed $seed: chain of ${chain.members.size}")
                for ((a, b) in chain.members.zipWithNext()) {
                    assertEquals(
                        a + 1,
                        b,
                        "seed $seed: chain members are not contiguous: ${chain.members}",
                    )
                }
            }
        }
    }

    /**
     * The whole point. A chain is a bidirectional run: every adjacent pair links both ways, and the
     * two ends anchor outward. A partially emitted chain is the exact shape of the degenerate
     * connections that once passed unnoticed, so completeness is asserted rather than assumed.
     */
    @Test
    fun everyChainIsEmittedWhole() {
        for (seed in 1L..300L) {
            val scenario = Scenarios.generate(seed)
            for (chain in scenario.chains) {
                val (startSide, endSide) =
                    if (chain.horizontal) Side.LEFT to Side.RIGHT else Side.TOP to Side.BOTTOM
                val links = scenario.connections.filter { it.from in chain.members }

                for ((position, member) in chain.members.withIndex()) {
                    val previous = chain.members.getOrNull(position - 1)
                    val next = chain.members.getOrNull(position + 1)

                    val backward = links.single { it.from == member && it.fromSide == startSide }
                    if (previous == null) {
                        assertTrue(
                            backward.target is Target.Root,
                            "seed $seed: chain head $member is not anchored outward",
                        )
                    } else {
                        assertEquals(
                            Target.Widget(previous),
                            backward.target,
                            "seed $seed: chain member $member does not link back to $previous",
                        )
                        assertEquals(endSide, backward.toSide, "seed $seed: member $member")
                    }

                    val forward = links.single { it.from == member && it.fromSide == endSide }
                    if (next == null) {
                        assertTrue(
                            forward.target is Target.Root,
                            "seed $seed: chain tail $member is not anchored outward",
                        )
                    } else {
                        assertEquals(
                            Target.Widget(next),
                            forward.target,
                            "seed $seed: chain member $member does not link on to $next",
                        )
                        assertEquals(startSide, forward.toSide, "seed $seed: member $member")
                    }
                }
            }
        }
    }

    /**
     * Completes the guarantee `everyChainIsEmittedWhole` leaves open: that test pins down what a
     * chain member's two axis-side connections point at, but never asserts there are only two —
     * a stray connection on the orthogonal axis, or a duplicate on the same side, would still pass
     * it. Without this, chain-member acyclicity rests on reading `generate()`'s `continue` guard
     * rather than on the suite actually proving it.
     */
    @Test
    fun chainMembersHaveExactlyTheChainsTwoConnections() {
        for (seed in 1L..300L) {
            val scenario = Scenarios.generate(seed)
            for (chain in scenario.chains) {
                val (startSide, endSide) =
                    if (chain.horizontal) Side.LEFT to Side.RIGHT else Side.TOP to Side.BOTTOM
                for (member in chain.members) {
                    val fromMember = scenario.connections.filter { it.from == member }
                    assertEquals(
                        2,
                        fromMember.size,
                        "seed $seed: chain member $member has ${fromMember.size} connections, " +
                            "expected exactly the 2 the chain supplies",
                    )
                    assertEquals(
                        setOf(startSide, endSide),
                        fromMember.map { it.fromSide }.toSet(),
                        "seed $seed: chain member $member's connections are not exactly one per " +
                            "chain-axis side",
                    )
                }
            }
        }
    }

    @Test
    fun everyChainStyleIsGenerated() {
        val styles = mutableSetOf<ChainStyle>()
        for (seed in 1L..300L) {
            Scenarios.generate(seed).chains.forEach { styles += it.style }
        }
        assertEquals(ChainStyle.entries.toSet(), styles, "generated styles: $styles")
    }
}
