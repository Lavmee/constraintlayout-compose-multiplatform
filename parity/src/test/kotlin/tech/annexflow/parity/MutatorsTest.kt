// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Determinism is the property worth guarding here: a divergence reported by CI has to reproduce
 * exactly on a developer's machine, which it only does if the same entry always yields the same
 * mutations in the same order.
 */
class MutatorsTest {
    private val entry = CorpusEntry("sample", "{ test: ['hello', 'world'] }")

    @Test
    fun theOriginalComesFirst() {
        assertEquals(entry, Mutators.mutate(entry).first())
    }

    @Test
    fun mutationsAreDeterministic() {
        assertEquals(Mutators.mutate(entry), Mutators.mutate(entry))
    }

    @Test
    fun mutationsAreNamedAfterTheirSource() {
        for (mutation in Mutators.mutate(entry).drop(1)) {
            assertTrue(mutation.name.startsWith("sample#"), mutation.name)
        }
    }

    @Test
    fun aLongLiteralMutationExceedsTheThousandCharacterCap() {
        val long = Mutators.mutate(entry).single { it.name.endsWith("#long-literal") }
        assertTrue(long.json.length > 1000, "length was ${long.json.length}")
    }

    @Test
    fun truncationsAreShorterThanTheOriginal() {
        val truncations = Mutators.mutate(entry).filter { it.name.contains("#truncate-") }
        assertTrue(truncations.isNotEmpty())
        assertTrue(truncations.all { it.json.length < entry.json.length })
    }

    @Test
    fun everyMutationHasADistinctName() {
        val names = Mutators.mutate(entry).map { it.name }
        assertEquals(names.size, names.toSet().size, "duplicate names in $names")
    }
}
