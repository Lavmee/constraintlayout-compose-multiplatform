// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity

/**
 * One side of the comparison.
 *
 * Implementations must never throw out of [parse]. An exception on one side against a success on
 * the other is precisely the finding this module exists to surface, so it has to arrive as a value
 * rather than abort the run before the remaining inputs are tried.
 */
interface ParserSubject {
    val name: String

    fun parse(input: String): ParseOutcome
}
