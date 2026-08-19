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

    // Measured against the current corpus (2000 seeds, unmutated port): oracle geometry totalled
    // 10647 rows across 1889 populated documents, averaging ~5.6 widgets per populated document.
    // 5000 is under half of that measured total — comfortable headroom for ordinary changes to
    // the generator's widget-count range — while remaining a total no corpus of substance-free
    // documents (each contributing zero rows) could ever reach. See below for the failure mode
    // this guards against.
    private val minimumGeometryRows = 5000
    private val maxExamples = 5

    @Test
    fun thePortAgreesWithTheOracle() {
        val examples = mutableListOf<String>()
        var divergences = 0
        var populated = 0
        var geometryRows = 0

        for (seed in seeds) {
            val spec = Scenarios.generate(seed)
            val oracle = OracleConstraintSet.parse(spec)
            val port = PortConstraintSet.parse(spec)
            if (oracle is ConstraintSetOutcome.Populated) {
                populated++
                geometryRows += oracle.geometry.count { it == '\n' }
            }
            if (oracle != port) {
                divergences++
                if (examples.size < maxExamples) examples += report(spec, oracle, port)
            }
        }

        // Two ways a decayed generator passes the equality check while testing nothing:
        // wholesale rejection, where both sides throw on every document and there is nothing left
        // to compare; and wholesale vacuity, where both sides return `Populated` for every
        // document but the documents carry no widgets — e.g. the emitter regressing to `{}` for
        // every spec. Both sides would then agree trivially on empty geometry for all 2000 seeds,
        // `populated` would clear the floor below, and the test would pass having compared
        // nothing at all. `minimumPopulated` catches the first; `minimumGeometryRows`, which a
        // corpus of empty documents cannot satisfy, catches the second.
        if (populated < minimumPopulated) {
            fail("only $populated of ${seeds.count()} documents laid out; the generator is emitting junk")
        }
        if (geometryRows < minimumGeometryRows) {
            fail(
                "only $geometryRows geometry rows across $populated populated documents; " +
                    "the generator is emitting substance-free documents",
            )
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
