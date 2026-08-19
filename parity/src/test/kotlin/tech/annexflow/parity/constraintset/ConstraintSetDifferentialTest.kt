// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.constraintset

import kotlin.test.Test
import kotlin.test.fail

/**
 * Parses a range of generated documents with both implementations and requires identical outcomes.
 *
 * The oracle is upstream, so a disagreement is a defect in the port until proven otherwise. When
 * this fails, read the report and fix the port — do not relax the harness to accommodate it.
 */
class ConstraintSetDifferentialTest {
    private val seeds = 1L..2000L
    private val minimumPopulated = 1800
    private val maxExamples = 5

    @Test
    fun thePortAgreesWithTheOracle() {
        val examples = mutableListOf<String>()
        var divergences = 0
        var populated = 0

        for (seed in seeds) {
            val spec = Scenarios.generate(seed)
            val oracle = OracleConstraintSet.parse(spec)
            val port = PortConstraintSet.parse(spec)
            if (oracle is ConstraintSetOutcome.Populated) populated++
            if (oracle != port) {
                divergences++
                if (examples.size < maxExamples) examples += report(spec, oracle, port)
            }
        }

        // A generator that decayed into emitting documents both sides reject would pass the
        // equality check while testing nothing. This is what notices.
        if (populated < minimumPopulated) {
            fail("only $populated of ${seeds.count()} documents laid out; the generator is emitting junk")
        }
        if (divergences > 0) {
            fail("$divergences of ${seeds.count()} documents diverged\n\n${examples.joinToString("\n\n")}")
        }
    }

    private fun report(
        spec: ConstraintSetSpec,
        oracle: ConstraintSetOutcome,
        port: ConstraintSetOutcome,
    ): String = buildString {
        appendLine("seed ${spec.seed}")
        appendLine(emit(spec))
        appendLine("oracle: $oracle")
        appendLine("port:   $port")
    }
}
