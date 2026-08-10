// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.solver

import kotlin.random.Random

/**
 * Generates layouts from a seed.
 *
 * Two properties are load-bearing. Generation is a pure function of the seed, so a divergence
 * reported by CI reproduces exactly. And connections only ever target the root or a lower-indexed
 * widget, so the constraint graph is acyclic — a cycle would leave the solver failing to settle,
 * and the harness would be measuring that instead of the port.
 */
object Scenarios {
    private const val MIN_WIDGETS = 2
    private const val MAX_WIDGETS = 8

    fun generate(seed: Long): Scenario {
        val random = Random(seed)
        val count = random.nextInt(MIN_WIDGETS, MAX_WIDGETS + 1)

        val widgets = (0 until count).map { index ->
            WidgetSpec(
                name = "w$index",
                width = random.nextInt(20, 200),
                height = random.nextInt(20, 200),
                horizontal = behaviour(random),
                vertical = behaviour(random),
                minWidth = if (random.nextInt(4) == 0) random.nextInt(1, 150) else 0,
                minHeight = if (random.nextInt(4) == 0) random.nextInt(1, 150) else 0,
                maxWidth = if (random.nextInt(6) == 0) random.nextInt(150, 400) else Int.MAX_VALUE,
                maxHeight = if (random.nextInt(6) == 0) random.nextInt(150, 400) else Int.MAX_VALUE,
                horizontalBias = random.nextInt(0, 11) / 10f,
                verticalBias = random.nextInt(0, 11) / 10f,
            )
        }

        val connections = mutableListOf<ConnectionSpec>()
        for (index in 0 until count) {
            connections += axisConnections(random, index, horizontal = true)
            connections += axisConnections(random, index, horizontal = false)
        }

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
        )
    }

    private fun behaviour(random: Random): Behaviour =
        Behaviour.entries[random.nextInt(Behaviour.entries.size)]

    /**
     * One or two connections on a single axis. Both sides are needed for `MATCH_CONSTRAINT` to mean
     * anything, so the two-sided case is common rather than incidental.
     */
    private fun axisConnections(random: Random, index: Int, horizontal: Boolean): List<ConnectionSpec> {
        val (start, end) = if (horizontal) Side.LEFT to Side.RIGHT else Side.TOP to Side.BOTTOM
        val target = if (index == 0 || random.nextInt(2) == 0) null else random.nextInt(index)

        if (random.nextInt(3) == 0) {
            // A single connection. Targeting a sibling's opposite side chains the widgets one after
            // another; targeting the matching side of the root or a sibling anchors them together.
            // Both shapes matter, so pick between them rather than always chaining.
            val toSide = if (target != null && random.nextInt(2) == 0) end else start
            return listOf(ConnectionSpec(index, start, target, toSide, random.nextInt(0, 40)))
        }

        // Two connections anchor the widget to the container's two edges — the root's for a null
        // target, a sibling's for a non-null one — each side to its matching side, so the span
        // between them (and therefore a MATCH_CONSTRAINT dimension) is real and positive rather
        // than collapsing both anchors onto the same edge. Margins are drawn independently so
        // asymmetric margins can occur.
        return listOf(
            ConnectionSpec(index, start, target, start, random.nextInt(0, 40)),
            ConnectionSpec(index, end, target, end, random.nextInt(0, 40)),
        )
    }
}
