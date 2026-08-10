// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Guards the corpus wiring rather than the parsers. A missing resource directory or a stale index
 * would otherwise leave [DifferentialTest] comparing nothing at all and reporting success.
 */
class CorpusTest {
    @Test
    fun corpusIsFullyLoaded() {
        // Deliberately close to the real count: an empty or truncated classpath resource is the
        // failure mode worth catching, and "not empty" would not catch it.
        assertTrue(Corpus.entries().size >= 100, "found ${Corpus.entries().size} entries")
    }

    @Test
    fun everyEntryHasANameAndContent() {
        for (entry in Corpus.entries()) {
            assertTrue(entry.name.isNotBlank())
            assertTrue(entry.json.isNotEmpty(), "${entry.name} is empty")
        }
    }
}
