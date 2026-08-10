// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Checks the two adapters behave the same way as adapters — that they both parse, both report
 * structure, and both turn errors into values rather than throwing. The comparison itself lives in
 * `DifferentialTest`; if these fail, the harness is broken rather than the port.
 */
class SubjectTest {
    private val subjects = listOf(OracleParser, PortParser)

    @Test
    fun bothSubjectsParseAValidDocument() {
        for (subject in subjects) {
            val outcome = subject.parse("{ test: ['hello', 'world'] }")
            assertTrue(outcome is ParseOutcome.Parsed, "${subject.name} returned $outcome")
        }
    }

    @Test
    fun bothSubjectsReportStructureIncludingContent() {
        for (subject in subjects) {
            val outcome = subject.parse("{ a: 1 }") as ParseOutcome.Parsed
            assertTrue(outcome.structure.contains("CLObject"), "${subject.name}: ${outcome.structure}")
            assertTrue(outcome.structure.contains("CLKey"), "${subject.name}: ${outcome.structure}")
        }
    }

    @Test
    fun bothSubjectsTurnParseErrorsIntoFailed() {
        for (subject in subjects) {
            val outcome = subject.parse("no json here")
            assertEquals(
                ParseOutcome.Failed("invalid json content (unknown at line 0)"),
                outcome,
                subject.name,
            )
        }
    }

    /**
     * Truncated input makes `content()` read past the buffer, so an index error escapes the parser
     * instead of a `CLParsingException`. Upstream does this too — the port is faithful here — and
     * pinning it down keeps the distinction between a decision the parser made and a detail leaking
     * out of it honest.
     */
    @Test
    fun bothSubjectsLeakOnTruncatedInput() {
        for (subject in subjects) {
            val outcome = subject.parse("{ test: [")
            assertEquals(ParseOutcome.Leaked("IndexOutOfBounds"), outcome, subject.name)
        }
    }

    @Test
    fun subjectsAgreeOnAValidDocument() {
        assertEquals(OracleParser.parse("{ a: 1, b: ['x'] }"), PortParser.parse("{ a: 1, b: ['x'] }"))
    }
}
