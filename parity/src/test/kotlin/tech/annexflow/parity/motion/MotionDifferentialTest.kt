// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.motion

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Compares the ported `core/motion` against the vendored upstream Java over generated scenarios.
 *
 * A green run here means the two agree on every scenario the generator produced — not that the
 * animation is correct. If upstream animates wrongly, both sides are wrong together and this stays
 * green. That is the price of an oracle, and it is the same price the parser, solver and measure
 * harnesses pay.
 */
class MotionDifferentialTest {
    @Test
    fun thePortAgreesWithTheOracle() {
        val oracle = OracleMotion()
        val port = PortMotion()
        val divergences =
            (0L until SEEDS).mapNotNull { seed ->
                val scenario = Scenarios.generate(seed)
                val fromOracle = oracle.run(scenario)
                val fromPort = port.run(scenario)
                if (fromOracle == fromPort) null else report(scenario, fromOracle, fromPort)
            }

        assertTrue(
            divergences.isEmpty(),
            buildString {
                append(divergences.size).append(" of ").append(SEEDS).append(" scenarios diverged.\n\n")
                divergences.take(MAX_REPORTED).forEach { append(it).append('\n') }
                if (divergences.size > MAX_REPORTED) {
                    append("… and ").append(divergences.size - MAX_REPORTED).append(" more.")
                }
            },
        )
    }

    private fun report(scenario: MotionScenario, oracle: MotionOutcome, port: MotionOutcome): String =
        buildString {
            append("seed     : ").append(scenario.seed).append('\n')
            append("start    : ").append(scenario.start).append('\n')
            append("end      : ").append(scenario.end).append('\n')
            append("arc      : ").append(scenario.pathMotionArc)
                .append("  easing: ").append(scenario.easing).append('\n')
            scenario.keys.forEach { append("key      : ").append(it).append('\n') }
            append("oracle   :\n").append(indent(oracle)).append('\n')
            append("port     :\n").append(indent(port)).append('\n')
        }

    private fun indent(outcome: MotionOutcome): String =
        when (outcome) {
            is MotionOutcome.Sampled -> outcome.snapshot
            is MotionOutcome.Leaked -> "leaked ${outcome.category}"
            is MotionOutcome.Crashed -> "crashed ${outcome.error}"
        }.lines().joinToString("\n") { "    $it" }

    private companion object {
        const val SEEDS = 1000L
        const val MAX_REPORTED = 3
    }
}
