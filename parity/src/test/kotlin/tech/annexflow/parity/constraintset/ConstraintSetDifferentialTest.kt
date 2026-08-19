// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.constraintset

import kotlin.test.Test
import kotlin.test.fail

/**
 * Parses/measures a range of generated documents with both implementations and requires identical
 * outcomes.
 *
 * The oracle is upstream, so a disagreement is a defect in the port until proven otherwise. When
 * this fails, read the report and fix the port — do not relax the harness to accommodate it.
 *
 * Both [ConstraintSetSubject.parse] and [ConstraintSetSubject.measure] entry points are exercised,
 * following `solver.SolverDifferentialTest`'s structure — and each is compared only against its own
 * counterpart on the other side, never `parse` against `measure`. The two are different contracts
 * (`measure` resolves a fixed root via a real `Measurer`; `parse` does not — see
 * `ConstraintSetSubject.measure`'s kdoc) and a cross comparison would fail for reasons that say
 * nothing about the port. Before this addition, `measure` — the only entry point that resolves bias
 * or a chain's style/weight (see `AxisLivenessTest`'s `hBias`/`chainStyle`/`hWeight` cases) — was
 * never run against the 2000-seed corpus at all: a liveness suite certifying those axes live through
 * `measure` said nothing about whether the port agrees with the oracle on them, because this test,
 * the one thing that actually compares the two implementations at scale, never called it.
 */
class ConstraintSetDifferentialTest {
    private val seeds = 1L..2000L
    private val maxExamplesPerEntry = 5

    /**
     * Both floors are measured against the current corpus (2000 seeds, unmutated port), not guessed
     * — see the task report for the instrumented run each number came from. `parse` and `measure`
     * land on the *same* populated count and geometry-row total: row count only depends on how many
     * widgets/guidelines/barriers a document materialises, which is identical either way — the two
     * entry points disagree (or, per this test, don't) on the *numbers inside* each row (a
     * `MATCH_CONSTRAINT` widget spread to width 0 under `parse` but to a real span under `measure`;
     * see `ConstraintSetSubject.measure`'s kdoc), not on how many rows exist. That distinction is
     * exactly why a separate divergence check per entry point matters even though the floors turned
     * out identical: two entry points can agree on shape while disagreeing on content, and it's the
     * content this test exists to compare.
     */
    private enum class Entry(val label: String, val minimumPopulated: Int, val minimumGeometryRows: Int) {
        // Measured: 1876 populated, 10576 geometry rows. 5000 stays under half of that, per the
        // rationale `minimumGeometryRows` originally carried (headroom for ordinary generator
        // changes, unreachable by a corpus of substance-free documents).
        PARSE("parse", 1800, 5000),

        // Measured: 1876 populated, 10576 geometry rows — identical to PARSE (see the class kdoc for
        // why). Same floors as PARSE follow directly from that.
        MEASURE("measure", 1800, 5000),
    }

    private fun run(entry: Entry, subject: ConstraintSetSubject, spec: ConstraintSetSpec): ConstraintSetOutcome =
        when (entry) {
            Entry.PARSE -> subject.parse(spec)
            Entry.MEASURE -> subject.measure(spec)
        }

    @Test
    fun thePortAgreesWithTheOracle() {
        // Keyed per entry point rather than one shared list: PARSE runs before MEASURE for every
        // seed, so a global cap would let early parse divergences crowd out every measure
        // divergence from the reported examples while the counts stayed correct and the diagnostic
        // went blind.
        val divergences = mutableMapOf(Entry.PARSE to mutableListOf<String>(), Entry.MEASURE to mutableListOf<String>())
        var totalDivergences = 0
        val populated = mutableMapOf(Entry.PARSE to 0, Entry.MEASURE to 0)
        val geometryRows = mutableMapOf(Entry.PARSE to 0, Entry.MEASURE to 0)

        for (seed in seeds) {
            val spec = Scenarios.generate(seed)
            for (entry in Entry.entries) {
                val oracle = run(entry, OracleConstraintSet, spec)
                val port = run(entry, PortConstraintSet, spec)
                if (oracle is ConstraintSetOutcome.Populated) {
                    populated[entry] = populated.getValue(entry) + 1
                    geometryRows[entry] = geometryRows.getValue(entry) + oracle.geometry.count { it == '\n' }
                }
                if (oracle != port) {
                    totalDivergences++
                    val examples = divergences.getValue(entry)
                    if (examples.size < maxExamplesPerEntry) examples += report(entry, spec, oracle, port)
                }
            }
        }

        // Two ways a decayed generator passes the equality check while testing nothing:
        // wholesale rejection, where both sides throw on every document and there is nothing left
        // to compare; and wholesale vacuity, where both sides return `Populated` for every
        // document but the documents carry no widgets — e.g. the emitter regressing to `{}` for
        // every spec. Both sides would then agree trivially on empty geometry for all 2000 seeds,
        // the populated count would clear the floor below, and the test would pass having compared
        // nothing at all. `minimumPopulated` catches the first; `minimumGeometryRows`, which a
        // corpus of empty documents cannot satisfy, catches the second — checked per entry point,
        // because one of them failing to run at all (e.g. `measure` throwing on every document) is
        // exactly the failure worth catching, and a shared total could hide it behind the other
        // entry's healthy numbers.
        for (entry in Entry.entries) {
            if (populated.getValue(entry) < entry.minimumPopulated) {
                fail(
                    "only ${populated.getValue(entry)} of ${seeds.count()} documents populated through " +
                        "${entry.label}; the generator is emitting junk",
                )
            }
            if (geometryRows.getValue(entry) < entry.minimumGeometryRows) {
                fail(
                    "only ${geometryRows.getValue(entry)} geometry rows across ${populated.getValue(entry)} " +
                        "populated documents through ${entry.label}; the generator is emitting substance-free " +
                        "documents",
                )
            }
        }

        if (totalDivergences > 0) {
            val examples = Entry.entries.flatMap { divergences.getValue(it) }
            fail(
                "$totalDivergences of ${seeds.count() * Entry.entries.size} comparisons diverged " +
                    "(showing up to $maxExamplesPerEntry per entry point):\n\n${examples.joinToString("\n\n")}",
            )
        }
    }

    private fun report(
        entry: Entry,
        spec: ConstraintSetSpec,
        oracle: ConstraintSetOutcome,
        port: ConstraintSetOutcome,
    ): String = buildString {
        appendLine("seed ${spec.seed} via ${entry.label}")
        appendLine(emit(spec))
        appendLine("oracle: $oracle")
        appendLine("port:   $port")
    }
}
