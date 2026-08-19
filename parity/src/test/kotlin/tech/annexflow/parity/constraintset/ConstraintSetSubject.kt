// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.constraintset

/**
 * One side of the comparison: parses the same emitted document with its own package's
 * `ConstraintSetParser` and reports a comparable outcome.
 *
 * Implementations must never throw out of [parse], [measure] or [designElements]. An exception on
 * one side against a success on the other is precisely the finding this module exists to surface,
 * so it has to arrive as a value rather than abort the run before the remaining inputs are tried.
 */
interface ConstraintSetSubject {
    val name: String

    fun parse(spec: ConstraintSetSpec): ConstraintSetOutcome

    /**
     * Same document, driven through `ConstraintWidgetContainer.measure(...)` (backed by a real
     * `Measurer`) instead of [parse]'s bare `layout()`, and — the part that actually matters for
     * `AxisLivenessTest`'s `hBias`/`vBias`/`hRtlBias`/`centerVertically`/`chainStyle`/`hWeight`/
     * `vWeight` cases — telling `State` the root's width and height (`state.setWidth`/`setHeight`),
     * which [parse] never does. `Dimension`'s own default is `WRAP_CONTENT`, and neither entry point
     * previously overrode it, even though every `ConstraintSetSpec` carries a `rootWidth`/
     * `rootHeight`. That default is harmless for a widget anchored on one side only, but
     * `ConstraintWidget.applyConstraints`'s bias-centering equation, and the chain/`MATCH_CONSTRAINT`
     * machinery, both special-case an unresolved (`WRAP_CONTENT`) parent and never compute a real
     * bias/spread split once it applies — see `OracleConstraintSet.measure`'s kdoc for how this was
     * isolated (a raw `ConstraintWidget` reproduction outside `State` entirely).
     *
     * [parse] is deliberately left untouched by this addition. `ConstraintSetDifferentialTest`
     * exercises both entry points now, each against its own corpus-measured floors — see that
     * class's kdoc — but [parse]'s existing floors, measured before [measure] existed, must not
     * shift as a side effect of adding this second entry point.
     */
    fun measure(spec: ConstraintSetSpec): ConstraintSetOutcome

    fun designElements(spec: DesignElementsSpec): ConstraintSetOutcome
}
