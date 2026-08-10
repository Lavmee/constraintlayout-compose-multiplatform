// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.solver

import kotlin.test.Test
import kotlin.test.fail

/**
 * Lays out a range of generated scenarios with both solvers and requires identical geometry.
 *
 * The oracle is upstream, so a disagreement is a defect in the port until proven otherwise. When
 * this fails, read the report and fix the port — do not relax the harness to accommodate it.
 *
 * A caveat worth keeping in mind while triaging: a constraint solver can have several valid
 * solutions for an underconstrained system, so two correct implementations could in principle
 * disagree through collection iteration order alone. That would still be worth knowing — it would
 * mean the engine's output depends on iteration order, which matters well beyond this harness.
 */
class SolverDifferentialTest {
    private val seeds = 1L..2000L

    @Test
    fun thePortAgreesWithTheOracle() {
        val divergences = mutableListOf<String>()

        for (seed in seeds) {
            val scenario = Scenarios.generate(seed)
            val oracle = OracleSolver.layout(scenario)
            val port = PortSolver.layout(scenario)
            if (oracle != port) {
                divergences += report(scenario, oracle, port)
                if (divergences.size >= 5) break
            }
        }

        if (divergences.isNotEmpty()) {
            fail(
                "${divergences.size} of ${seeds.count()} scenarios diverged " +
                    "(stopped at the first 5):\n\n${divergences.joinToString("\n\n")}",
            )
        }
    }

    private fun report(scenario: Scenario, oracle: LayoutOutcome, port: LayoutOutcome): String =
        buildString {
            append("--- seed ").append(scenario.seed).append('\n')
            append("root     : ").append(scenario.rootWidth).append('x').append(scenario.rootHeight)
                .append(' ').append(scenario.rootHorizontal).append('/').append(scenario.rootVertical)
                .append(" min ").append(scenario.rootMinWidth).append('/').append(scenario.rootMinHeight)
                .append('\n')
            scenario.widgets.forEach { append("widget   : ").append(it).append('\n') }
            scenario.connections.forEach { append("connect  : ").append(it).append('\n') }
            append("oracle   :\n").append(indent(oracle)).append('\n')
            append("port     :\n").append(indent(port))
        }

    private fun indent(outcome: LayoutOutcome): String =
        when (outcome) {
            is LayoutOutcome.LaidOut -> outcome.geometry.trimEnd().lines().joinToString("\n") { "    $it" }
            else -> "    $outcome"
        }
}
