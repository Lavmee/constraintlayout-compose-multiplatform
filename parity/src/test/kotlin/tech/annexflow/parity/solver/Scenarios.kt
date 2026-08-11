// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.solver

import androidx.constraintlayout.core.widgets.Optimizer
import kotlin.random.Random

/**
 * Generates layouts from a seed.
 *
 * Two properties are load-bearing. Generation is a pure function of the seed, so a divergence
 * reported by CI reproduces exactly. And connections only ever target the root, a lower-indexed
 * widget, a guideline, or a barrier whose referenced widgets are all lower-indexed, so the
 * constraint graph is acyclic — a cycle would leave the solver failing to settle, and the harness
 * would be measuring that instead of the port. The one deliberate exception is a chain: its members
 * link to each other in both directions, which is what makes a run of widgets a chain at all.
 */
object Scenarios {
    private const val MIN_WIDGETS = 2
    private const val MAX_WIDGETS = 8

    private val RATIOS = listOf("16:9", "4:3", "1:1", "3:2", "W,16:9", "H,2:3", "0.5", "2.0")

    fun generate(seed: Long): Scenario {
        val random = Random(seed)
        val count = random.nextInt(MIN_WIDGETS, MAX_WIDGETS + 1)

        val widgets = (0 until count).map { index ->
            val horizontal = behaviour(random)
            val vertical = behaviour(random)
            val ratioIsMeaningful =
                horizontal == Behaviour.MATCH_CONSTRAINT || vertical == Behaviour.MATCH_CONSTRAINT
            WidgetSpec(
                name = "w$index",
                width = random.nextInt(20, 200),
                height = random.nextInt(20, 200),
                horizontal = horizontal,
                vertical = vertical,
                minWidth = if (random.nextInt(4) == 0) random.nextInt(1, 150) else 0,
                minHeight = if (random.nextInt(4) == 0) random.nextInt(1, 150) else 0,
                maxWidth = if (random.nextInt(6) == 0) random.nextInt(150, 400) else Int.MAX_VALUE,
                maxHeight = if (random.nextInt(6) == 0) random.nextInt(150, 400) else Int.MAX_VALUE,
                horizontalBias = random.nextInt(0, 11) / 10f,
                verticalBias = random.nextInt(0, 11) / 10f,
                // A ratio on a widget whose dimensions are both fixed is inert — the fixed sizes
                // win and nothing is exercised. Drawing it only when an axis is MATCH_CONSTRAINT
                // keeps generated coverage honest.
                dimensionRatio = if (ratioIsMeaningful && random.nextInt(3) == 0) {
                    RATIOS[random.nextInt(RATIOS.size)]
                } else {
                    null
                },
            )
        }

        // Guidelines depend on nothing, so any widget may target one.
        val guidelines = (0 until random.nextInt(0, 3)).map { index ->
            GuidelineSpec(
                name = "g$index",
                vertical = random.nextBoolean(),
                // The root is at least 400 (see rootWidth/rootHeight below), so an upper bound past
                // that lets some draws land beyond the root's edge, not just short of it. Percent
                // spans the full 0..100 inclusive so the 0% and 100% edges are reached too.
                position = when (random.nextInt(3)) {
                    0 -> GuidelinePosition.Begin(random.nextInt(10, 500))
                    1 -> GuidelinePosition.End(random.nextInt(10, 500))
                    else -> GuidelinePosition.Percent(random.nextInt(0, 101) / 100f)
                },
            )
        }

        // A barrier sits over widgets from the lower half, so widgets above it can target it
        // without closing a cycle. With fewer than two widgets there is no room for that.
        val barriers = if (count >= 2 && random.nextInt(2) == 0) {
            val ceiling = count / 2
            val referenced = (0 until ceiling).filter { random.nextBoolean() }.ifEmpty { listOf(0) }
            listOf(
                BarrierSpec(
                    name = "b0",
                    side = Side.entries[random.nextInt(Side.entries.size)],
                    margin = random.nextInt(0, 30),
                    referenced = referenced,
                ),
            )
        } else {
            emptyList()
        }

        // A chain claims a contiguous run of widgets on one axis. Its members get no ordinary
        // connections on that axis — the chain supplies them — and none on the other either, to
        // keep the generated shape simple enough to reason about when a divergence is reported.
        val chains = if (count >= 3 && random.nextInt(2) == 0) {
            val length = random.nextInt(2, minOf(count, 4) + 1)
            val first = random.nextInt(0, count - length + 1)
            listOf(
                ChainSpec(
                    members = (first until first + length).toList(),
                    horizontal = random.nextBoolean(),
                    style = ChainStyle.entries[random.nextInt(ChainStyle.entries.size)],
                ),
            )
        } else {
            emptyList()
        }
        val chained = chains.flatMap { it.members }.toSet()

        // A widget is anchored either by its edges or circularly, never both: the two together
        // over-constrain it, and the solver's resolution of that conflict is not what this
        // harness is measuring.
        val circular = mutableListOf<CircularSpec>()
        val connections = mutableListOf<ConnectionSpec>()
        for (index in 0 until count) {
            if (index in chained) continue
            if (index > 0 && random.nextInt(5) == 0) {
                circular += CircularSpec(
                    from = index,
                    target = random.nextInt(index),
                    angle = random.nextInt(0, 360).toFloat(),
                    radius = random.nextInt(10, 200),
                )
            } else {
                connections += axisConnections(random, index, horizontal = true, barriers, guidelines)
                connections += axisConnections(random, index, horizontal = false, barriers, guidelines)
            }
        }
        chains.forEach { connections += chainConnections(random, it) }

        // A wrap-content root only re-measures under AT_MOST, so that mode is drawn often rather
        // than uniformly — it is the branch the measure entry point exists to reach.
        val measureSpec = MeasureSpec(
            widthMode = measureMode(random),
            heightMode = measureMode(random),
            // Widening this is the next step, not this one: the graph path lives behind
            // OPTIMIZATION_GRAPH and belongs on the measure path once it is compared at all.
            optimizationLevel = Optimizer.OPTIMIZATION_STANDARD,
        )

        return Scenario(
            seed = seed,
            rootWidth = random.nextInt(400, 1200),
            rootHeight = random.nextInt(400, 1200),
            // The root is usually fixed — a wrap-content root is the interesting minority, and the
            // minimum below only means anything when it is one.
            rootHorizontal = if (random.nextInt(3) == 0) Behaviour.WRAP_CONTENT else Behaviour.FIXED,
            rootVertical = if (random.nextInt(3) == 0) Behaviour.WRAP_CONTENT else Behaviour.FIXED,
            rootMinWidth = if (random.nextInt(2) == 0) random.nextInt(1, 600) else 0,
            rootMinHeight = if (random.nextInt(2) == 0) random.nextInt(1, 600) else 0,
            widgets = widgets,
            connections = connections,
            circular = circular,
            barriers = barriers,
            guidelines = guidelines,
            chains = chains,
            measureSpec = measureSpec,
        )
    }

    private fun behaviour(random: Random): Behaviour =
        Behaviour.entries[random.nextInt(Behaviour.entries.size)]

    private fun measureMode(random: Random): MeasureMode =
        when (random.nextInt(4)) {
            0 -> MeasureMode.UNSPECIFIED
            1 -> MeasureMode.AT_MOST
            2 -> MeasureMode.AT_MOST
            else -> MeasureMode.EXACTLY
        }

    /**
     * Emits a chain as one unit: the head anchored outward, every adjacent pair linked in both
     * directions, the tail anchored outward. There is no path through this function that produces a
     * partial chain — the reverse links are what make it a chain at all, and a run missing one is
     * just a row of widgets that looks like coverage.
     */
    private fun chainConnections(random: Random, chain: ChainSpec): List<ConnectionSpec> {
        val (startSide, endSide) =
            if (chain.horizontal) Side.LEFT to Side.RIGHT else Side.TOP to Side.BOTTOM
        val out = mutableListOf<ConnectionSpec>()

        for ((position, member) in chain.members.withIndex()) {
            val previous = chain.members.getOrNull(position - 1)
            val next = chain.members.getOrNull(position + 1)

            // Margins are drawn independently per connection, same as the non-chain path, so
            // asymmetric margins can occur here too.
            out += if (previous == null) {
                ConnectionSpec(member, startSide, Target.Root, startSide, random.nextInt(0, 30))
            } else {
                ConnectionSpec(member, startSide, Target.Widget(previous), endSide, random.nextInt(0, 30))
            }

            out += if (next == null) {
                ConnectionSpec(member, endSide, Target.Root, endSide, random.nextInt(0, 30))
            } else {
                ConnectionSpec(member, endSide, Target.Widget(next), startSide, random.nextInt(0, 30))
            }
        }
        return out
    }

    /**
     * One or two connections on a single axis. Both sides are needed for `MATCH_CONSTRAINT` to mean
     * anything, so the two-sided case is common rather than incidental.
     */
    private fun axisConnections(
        random: Random,
        index: Int,
        horizontal: Boolean,
        barriers: List<BarrierSpec>,
        guidelines: List<GuidelineSpec>,
    ): List<ConnectionSpec> {
        val (start, end) = if (horizontal) Side.LEFT to Side.RIGHT else Side.TOP to Side.BOTTOM
        val target = pickTarget(random, index, horizontal, barriers, guidelines)
        val isLine = target is Target.Barrier || target is Target.Guideline

        if (isLine || random.nextInt(3) == 0) {
            // A single connection. Targeting a sibling's opposite side chains the widgets one after
            // another; targeting the matching side of the root or a sibling anchors them together.
            // Both shapes matter, so pick between them rather than always chaining. A barrier or
            // guideline is a line rather than a span — `Guideline.getAnchor` returns the same
            // anchor for both of its sides, and a barrier is likewise one-sided — so it is always
            // single-connected here regardless of the roll.
            val toSide = if (target is Target.Widget && random.nextInt(2) == 0) end else start
            return listOf(ConnectionSpec(index, start, target, toSide, random.nextInt(0, 40)))
        }

        // Two connections anchor the widget to the container's two edges — each side to its
        // matching side, so the span between them (and therefore a MATCH_CONSTRAINT dimension) is
        // real and positive rather than collapsing both anchors onto the same edge. That only holds
        // when the target has two distinct edges, true of the root and of a sibling widget; a
        // barrier or guideline is handled above instead. Margins are drawn independently so
        // asymmetric margins can occur.
        return listOf(
            ConnectionSpec(index, start, target, start, random.nextInt(0, 40)),
            ConnectionSpec(index, end, target, end, random.nextInt(0, 40)),
        )
    }

    /**
     * A barrier is only a legal target for a widget above every widget the barrier references —
     * otherwise the barrier would depend on a widget that depends on the barrier. Both a barrier
     * and a guideline are only legal on the axis they constrain: upstream `Barrier.addToSolver`
     * emits equations for its own axis alone and never calls `super`, so a LEFT/RIGHT barrier is a
     * vertical line usable only for horizontal connections, and likewise TOP/BOTTOM for vertical —
     * the same rule `Guideline` follows, where a vertical guideline is a vertical line.
     */
    private fun pickTarget(
        random: Random,
        index: Int,
        horizontal: Boolean,
        barriers: List<BarrierSpec>,
        guidelines: List<GuidelineSpec>,
    ): Target {
        val barrierIndex = barriers.indices.filter {
            index > barriers[it].referenced.max() && barriers[it].side.isHorizontal == horizontal
        }
        val guidelineIndex = guidelines.indices.filter { guidelines[it].vertical == horizontal }

        val choices = buildList {
            add { Target.Root }
            if (index > 0) add { Target.Widget(random.nextInt(index)) }
            if (barrierIndex.isNotEmpty()) {
                add { Target.Barrier(barrierIndex[random.nextInt(barrierIndex.size)]) }
            }
            if (guidelineIndex.isNotEmpty()) {
                add { Target.Guideline(guidelineIndex[random.nextInt(guidelineIndex.size)]) }
            }
        }
        return choices[random.nextInt(choices.size)]()
    }
}
