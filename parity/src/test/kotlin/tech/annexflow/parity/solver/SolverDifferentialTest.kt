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
 *
 * Both `layout()` and `measure()` entry points are exercised, and each is compared only against
 * its own counterpart on the other side — never against the other entry point — since they are
 * different contracts and a cross comparison would fail for reasons that say nothing about the
 * port.
 */
class SolverDifferentialTest {
    private val seeds = 1L..2000L
    private val minimumLaidOut = 1800

    private enum class Entry(val label: String) {
        LAYOUT("layout"),
        MEASURE("measure"),
    }

    private fun run(entry: Entry, subject: SolverSubject, scenario: Scenario) =
        when (entry) {
            Entry.LAYOUT -> subject.layout(scenario)
            Entry.MEASURE -> subject.measure(scenario)
        }

    @Test
    fun thePortAgreesWithTheOracle() {
        val divergences = mutableListOf<String>()
        var totalDivergences = 0
        val laidOut = mutableMapOf(Entry.LAYOUT to 0, Entry.MEASURE to 0)

        for (seed in seeds) {
            val scenario = Scenarios.generate(seed)
            for (entry in Entry.entries) {
                val oracle = run(entry, OracleSolver, scenario)
                val port = run(entry, PortSolver, scenario)
                if (oracle is LayoutOutcome.LaidOut) laidOut[entry] = laidOut.getValue(entry) + 1
                if (oracle != port) {
                    totalDivergences++
                    if (divergences.size < 5) divergences += report(entry, scenario, oracle, port)
                }
            }
        }

        // A harness that silently compared nothing would report success, so the count is part of
        // the contract rather than an incidental detail — and it is checked per entry point,
        // because one of them failing to run at all is exactly the failure worth catching.
        for (entry in Entry.entries) {
            if (laidOut.getValue(entry) < minimumLaidOut) {
                fail(
                    "only ${laidOut.getValue(entry)} of ${seeds.count()} scenarios reached " +
                        "LaidOut through ${entry.label} — that path is not wired up",
                )
            }
        }

        if (totalDivergences > 0) {
            fail(
                "$totalDivergences of ${seeds.count() * Entry.entries.size} comparisons diverged " +
                    "(showing the first ${divergences.size}):\n\n${divergences.joinToString("\n\n")}",
            )
        }
    }

    private fun report(
        entry: Entry,
        scenario: Scenario,
        oracle: LayoutOutcome,
        port: LayoutOutcome,
    ): String =
        buildString {
            append("--- seed ").append(scenario.seed).append(" via ").append(entry.label).append('\n')
            append("root     : ").append(scenario.rootWidth).append('x').append(scenario.rootHeight)
                .append(' ').append(scenario.rootHorizontal).append('/').append(scenario.rootVertical)
                .append(" min ").append(scenario.rootMinWidth).append('/').append(scenario.rootMinHeight)
                .append('\n')
            append("measure  : ").append(scenario.measureSpec).append('\n')
            scenario.widgets.forEach { append("widget   : ").append(it).append('\n') }
            scenario.connections.forEach { append("connect  : ").append(it).append('\n') }
            scenario.circular.forEach { append("circular : ").append(it).append('\n') }
            scenario.barriers.forEach { append("barrier  : ").append(it).append('\n') }
            scenario.guidelines.forEach { append("guideline: ").append(it).append('\n') }
            scenario.chains.forEach { append("chain    : ").append(it).append('\n') }
            append("oracle   :\n").append(indent(oracle)).append('\n')
            append("port     :\n").append(indent(port))
        }

    private fun indent(outcome: LayoutOutcome): String =
        when (outcome) {
            is LayoutOutcome.LaidOut -> outcome.geometry.trimEnd().lines().joinToString("\n") { "    $it" }
            else -> "    $outcome"
        }
}
