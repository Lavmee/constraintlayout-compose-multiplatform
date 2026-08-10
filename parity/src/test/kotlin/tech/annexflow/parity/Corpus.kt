// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity

/** One parser input, named after the test file it was taken from. */
data class CorpusEntry(val name: String, val json: String)

/**
 * Parser inputs, held as resource files rather than scraped from the test sources at build time:
 * they stay reviewable in a diff, and editing a test cannot silently change what is compared.
 *
 * The entries were lifted from the JSON literals already exercised by the ported suite, so the
 * corpus covers the shapes the library actually parses rather than shapes invented here.
 */
object Corpus {
    private const val INDEX = "/corpus/index.txt"

    fun entries(): List<CorpusEntry> {
        // The JVM cannot list a classpath directory portably, hence the checked-in index.
        val index = checkNotNull(javaClass.getResourceAsStream(INDEX)) {
            "$INDEX is missing — regenerate it with: (cd parity/src/test/resources/corpus && ls *.json | sort > index.txt)"
        }
        return index.bufferedReader().readLines()
            .map(String::trim)
            .filter { it.isNotEmpty() }
            .map { name ->
                val stream = checkNotNull(javaClass.getResourceAsStream("/corpus/$name")) {
                    "corpus/$name is listed in the index but absent"
                }
                CorpusEntry(name, stream.bufferedReader().readText())
            }
    }
}
