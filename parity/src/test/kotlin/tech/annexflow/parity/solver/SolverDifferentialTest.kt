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

    // A floor on how many seeds must actually reach LaidOut. If scenario construction or layout()
    // started throwing for every seed, both sides would return an identical Leaked(...) and all
    // 2000 seeds would compare equal and pass green — this makes that silent failure loud instead.
    // In the spirit of DifferentialTest's `compared >= 1000` guard: all 2000 seeds currently reach
    // LaidOut, so 1800 is a floor with real margin, not one hugging the observed value.
    private val minimumLaidOut = 1800

    @Test
    fun thePortAgreesWithTheOracle() {
        val divergences = mutableListOf<String>()
        var totalDivergences = 0
        var laidOut = 0

        for (seed in seeds) {
            val scenario = Scenarios.generate(seed)
            val oracle = OracleSolver.layout(scenario)
            val port = PortSolver.layout(scenario)
            if (oracle is LayoutOutcome.LaidOut) laidOut++
            if (oracle != port) {
                totalDivergences++
                if (divergences.size < 5) divergences += report(scenario, oracle, port)
            }
        }

        if (laidOut < minimumLaidOut) {
            fail(
                "only $laidOut of ${seeds.count()} scenarios reached LaidOut (need at least " +
                    "$minimumLaidOut) — the generator or the solver wiring is broken, not just " +
                    "under-covering behaviour",
            )
        }

        if (totalDivergences > 0) {
            fail(
                "$totalDivergences of ${seeds.count()} scenarios diverged " +
                    "(showing the first ${divergences.size}):\n\n${divergences.joinToString("\n\n")}",
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
            scenario.circular.forEach { append("circular : ").append(it).append('\n') }
            scenario.barriers.forEach { append("barrier  : ").append(it).append('\n') }
            scenario.guidelines.forEach { append("guideline: ").append(it).append('\n') }
            append("oracle   :\n").append(indent(oracle)).append('\n')
            append("port     :\n").append(indent(port))
        }

    private fun indent(outcome: LayoutOutcome): String =
        when (outcome) {
            is LayoutOutcome.LaidOut -> outcome.geometry.trimEnd().lines().joinToString("\n") { "    $it" }
            else -> "    $outcome"
        }
}
