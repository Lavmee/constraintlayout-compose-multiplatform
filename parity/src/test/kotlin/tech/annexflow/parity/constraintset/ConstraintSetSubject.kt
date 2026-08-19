// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.constraintset

/**
 * One side of the comparison: parses the same emitted document with its own package's
 * `ConstraintSetParser` and reports a comparable outcome.
 *
 * Implementations must never throw out of [parse] or [designElements]. An exception on one side
 * against a success on the other is precisely the finding this module exists to surface, so it has
 * to arrive as a value rather than abort the run before the remaining inputs are tried.
 */
interface ConstraintSetSubject {
    val name: String

    fun parse(spec: ConstraintSetSpec): ConstraintSetOutcome

    fun designElements(spec: DesignElementsSpec): ConstraintSetOutcome
}
