// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity

import kotlin.random.Random

/**
 * Deterministic input mutations.
 *
 * The corpus is well-formed by construction — it came from tests that expect it to parse — so on its
 * own it never reaches the parser's error paths. Those paths are where the divergences found so far
 * live: a dropped bounds check and a dropped length cap, neither of which a valid document touches.
 *
 * Everything is derived from a fixed seed, so a divergence reported by CI reproduces exactly.
 */
object Mutators {
    private const val SEED = 20260810L
    private const val STRUCTURAL = "{}[]\"':,"
    private const val TRUNCATION_POINTS = 8
    private const val DROP_SITES = 6

    fun mutate(entry: CorpusEntry): List<CorpusEntry> {
        val random = Random(SEED)
        val out = mutableListOf(entry)
        val json = entry.json

        // Truncation reaches "input ended mid-element", the parser's most common error path.
        for (i in 1..TRUNCATION_POINTS) {
            val cut = json.length * i / (TRUNCATION_POINTS + 1)
            if (cut > 0) {
                out += CorpusEntry("${entry.name}#truncate-$i", json.take(cut))
            }
        }

        // Removing one structural character unbalances the document without shortening it.
        for (index in json.indices.filter { json[it] in STRUCTURAL }.take(DROP_SITES)) {
            out += CorpusEntry("${entry.name}#drop-at-$index", json.removeRange(index, index + 1))
        }

        // Inserting one reaches the "unexpected token" paths instead.
        for (character in STRUCTURAL) {
            val at = random.nextInt(json.length + 1)
            out += CorpusEntry(
                "${entry.name}#insert-${character.code}",
                json.substring(0, at) + character + json.substring(at),
            )
        }

        // Past the 1000-character cap that upstream applies to `content()` and `toString()`.
        out += CorpusEntry("${entry.name}#long-literal", "{ key: '${"y".repeat(1500)}' }")
        out += CorpusEntry("${entry.name}#empty", "")
        out += CorpusEntry("${entry.name}#whitespace", "   \t\n  ")

        return out
    }
}
