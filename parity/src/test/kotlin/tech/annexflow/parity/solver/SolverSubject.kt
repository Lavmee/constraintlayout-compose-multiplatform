// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.solver

/**
 * One side of the comparison. Implementations must never throw out of [layout] or [measure]: a
 * failure on one side against a success on the other is the finding, so it has to arrive as a value
 * rather than end the run before the remaining scenarios are tried.
 */
interface SolverSubject {
    val name: String

    /**
     * The solver's own entry point. Production does not call this directly, but both mutation
     * probes so far fired through it, so it stays compared in its own right.
     */
    fun layout(scenario: Scenario): LayoutOutcome

    /**
     * The entry point production uses: `ConstraintWidgetContainer.measure`, which runs the
     * orchestration in `BasicMeasure` and only reaches `layout` as one of its branches.
     *
     * Compared against the other implementation's `measure`, never against `layout` — the two are
     * different contracts and a cross comparison would fail for reasons that say nothing about the
     * port.
     */
    fun measure(scenario: Scenario): LayoutOutcome
}
