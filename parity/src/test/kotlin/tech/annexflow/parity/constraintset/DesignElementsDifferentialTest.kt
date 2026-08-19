// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.constraintset

import kotlin.test.Test
import kotlin.test.fail

/**
 * `parseDesignElementsJSON` is `ConstraintSetParser`'s other public entry point: it doesn't lay out
 * a container, it walks a `Design` block and returns a flat list of `DesignElement`s. It is
 * exercised here the same way [ConstraintSetDifferentialTest] exercises `parse`/`measure` — generate
 * a document, run it through both implementations, and fail on any disagreement.
 *
 * Read this kdoc before touching the floors below. `parseDesignElementsJSON` carries two indexing
 * bugs, present verbatim in the vendored upstream Java (`ConstraintSetParser.java`) and reproduced
 * line-for-line in the port (`ConstraintSetParser.kt`) — same function, same mistakes. This is not a
 * port defect to fix: a "correction" here would diverge from upstream, which is the one thing this
 * harness exists to catch. What follows was confirmed by instrumented runs over the 500-seed corpus
 * this test uses, not guessed:
 *
 *  - the constructed `DesignElement`'s id is always `elementName`, the *outer* loop variable, which
 *    for every document this generator emits is the literal string `"Design"` — the document's only
 *    top-level key — never the per-element key (`d0`, `d1`, ...) the inner loop actually walks.
 *    Across all 531 rows produced over seeds 1..500, the id is `"Design"` on every single one; the
 *    ids `Scenarios.generateDesignElements` assigns never survive into the output at all.
 *
 *  - the inner parameter loop reads `designElement[j]`, where `j` is the *outer* per-element index
 *    (0 for the first design element in the `Design` block, 1 for the second, ...), not the inner
 *    loop counter `k` it iterates with. Three consequences, all confirmed by instrumentation:
 *      - every surviving element yields *at most one* parameter, never however many it declared: the
 *        `while (k < size)` loop re-reads the same fixed index `j` on every pass, so the map
 *        converges to one entry regardless of `size`.
 *      - that one entry is not fabricated garbage, but it is not that element's own parameter
 *        either. For the first element in a block (`j == 0`) it is always `"type" -> <that
 *        element's own type string>` — position 0 in every element's own key list is `type`, per the
 *        emitter's field order (330 of 531 rows). For a later element (`j >= 1`) position `j` lands
 *        on a real `paramN`/`valueN` pair *only* because the emitter always writes `type` first,
 *        so it looks right by coincidence of field order, not because the indexing is (201 of 531
 *        rows). Either way it is never "this element's declared parameters."
 *      - when position `j` does not exist in that element's own object — a later element whose own
 *        key count does not reach its outer index — `CLObject.get(Int)` throws `CLParsingException`
 *        (`"no element at index $j"`), which unwinds the *whole* `parseDesignElementsJSON` call, not
 *        just that element. A document with two or more design elements throws whenever some
 *        non-first element's own key count falls short of its position. Measured: 170 of 500
 *        documents throw this way — all `CLParsingException` (`Leaked`), on both `oracle` and `port`,
 *        with zero mismatched outcome types anywhere in the corpus.
 *
 * Net effect: the equality check below (`oracle != port`) is genuinely comparing two implementations
 * that both do the broken thing, so a pass here is a real, honest statement that the port matches
 * upstream — it is not a statement that `parseDesignElementsJSON` does anything useful. The two
 * floors exist only to keep the generator honest (catch it decaying toward "nothing ever produces"
 * or "nothing ever throws"), the same role [ConstraintSetDifferentialTest]'s `minimumPopulated`/
 * `minimumGeometryRows` floors play for `parse`/`measure` — they say nothing about whether the
 * *content* is meaningful, because for this entry point it structurally can't be.
 */
class DesignElementsDifferentialTest {
    private val seeds = 1L..500L

    /**
     * Measured on the current corpus: 330 of 500 documents produce a non-empty `Elements` outcome,
     * 170 throw during the per-element loop (see the class kdoc). `seeds.count() / 2` — the brief's
     * original floor — is 250, comfortably below the measured 330, so it stays: a real, satisfiable
     * floor that only fails if the generator regresses toward "almost everything throws," not a
     * number chosen to always pass regardless of what the generator does.
     */
    private val minimumProduced = seeds.count() / 2

    /**
     * The brief's floor only watched one failure direction. The other is just as real here as
     * `minimumGeometryRows` is for `parse`/`measure`: the `CLParsingException` path (170/500, 34%,
     * see the class kdoc) is over a third of this corpus's behaviour and the only place this entry
     * point's exception handling gets exercised at all. Nothing forces that number toward "half" — a
     * document only throws when it declares two or more design elements *and* some non-first
     * element's own key count falls short of its position — so without a floor here, a change to
     * `Scenarios.generateDesignElements` (e.g. capping every document at one element) could silently
     * delete this entire code path from the corpus and nothing here would notice. Set well below the
     * measured 170 for the same headroom [minimumProduced] uses.
     */
    private val minimumLeaked = 80

    private val maxExamplesPerEntry = 5

    @Test
    fun thePortAgreesWithTheOracle() {
        val examples = mutableListOf<String>()
        var produced = 0
        var leaked = 0
        var totalDivergences = 0
        for (seed in seeds) {
            val spec = Scenarios.generateDesignElements(seed)
            val oracle = OracleConstraintSet.designElements(spec)
            val port = PortConstraintSet.designElements(spec)
            if (oracle is ConstraintSetOutcome.Elements && oracle.rendered.isNotEmpty()) produced++
            if (oracle is ConstraintSetOutcome.Leaked) leaked++
            if (oracle != port) {
                totalDivergences++
                if (examples.size < maxExamplesPerEntry) {
                    examples += "seed $seed\n${emitDesignElements(spec)}\noracle: $oracle\nport:   $port"
                }
            }
        }
        if (produced < minimumProduced) fail("only $produced of ${seeds.count()} documents produced elements")
        if (leaked < minimumLeaked) {
            fail(
                "only $leaked of ${seeds.count()} documents leaked through the oracle; the generator may have " +
                    "stopped exercising parseDesignElementsJSON's CLParsingException path (see class kdoc)",
            )
        }
        if (totalDivergences > 0) {
            fail(
                "$totalDivergences of ${seeds.count()} diverged (showing up to $maxExamplesPerEntry):\n\n" +
                    examples.joinToString("\n\n"),
            )
        }
    }
}
