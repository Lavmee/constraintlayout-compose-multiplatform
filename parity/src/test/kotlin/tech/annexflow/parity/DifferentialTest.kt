// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity

import kotlin.test.Test
import kotlin.test.fail

/**
 * Runs every corpus entry and every mutation of it through both parsers and requires the outcomes
 * to be identical.
 *
 * The oracle is upstream, so a disagreement is a defect in the port until proven otherwise. When
 * this fails, read the report and fix the port — do not relax the harness to accommodate it.
 */
class DifferentialTest {
    @Test
    fun thePortAgreesWithTheOracle() {
        val divergences = mutableListOf<String>()
        var compared = 0

        for (entry in Corpus.entries()) {
            for (candidate in Mutators.mutate(entry)) {
                compared++
                val oracle = OracleParser.parse(candidate.json)
                val port = PortParser.parse(candidate.json)
                if (oracle != port) {
                    divergences += report(candidate, oracle, port)
                }
            }
        }

        // A harness that silently compared nothing would report success, so the count is part of
        // the contract rather than an incidental detail.
        if (compared < 1000) {
            fail("only $compared comparisons ran — the corpus or the mutators are not wired up")
        }

        if (divergences.isNotEmpty()) {
            fail(
                "${divergences.size} of $compared comparisons diverged:\n\n" +
                    divergences.joinToString("\n\n"),
            )
        }
    }

    private fun report(candidate: CorpusEntry, oracle: ParseOutcome, port: ParseOutcome): String =
        buildString {
            append("--- ").append(candidate.name).append('\n')
            append("input    : ").append(candidate.json.take(200)).append('\n')
            append("minimised: ").append(minimise(candidate.json)).append('\n')
            append("oracle   : ").append(oracle).append('\n')
            append("port     : ").append(port)
        }

    /**
     * Shrinks toward the shortest prefix that still diverges. A prefix is enough: every mutation is
     * itself a small edit, so the divergence is nearly always reachable by truncation, and this
     * keeps the reported input readable without a general shrinking algorithm.
     */
    private fun minimise(input: String): String {
        for (length in 1..input.length) {
            val prefix = input.take(length)
            if (OracleParser.parse(prefix) != PortParser.parse(prefix)) {
                return prefix
            }
        }
        return input
    }
}
