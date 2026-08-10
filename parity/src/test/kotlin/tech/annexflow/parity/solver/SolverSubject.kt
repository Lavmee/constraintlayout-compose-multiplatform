// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.solver

/**
 * One side of the comparison. Implementations must never throw out of [layout]: a failure on one
 * side against a success on the other is the finding, so it has to arrive as a value rather than
 * end the run before the remaining scenarios are tried.
 */
interface SolverSubject {
    val name: String

    fun layout(scenario: Scenario): LayoutOutcome
}
