// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.constraintset

import kotlin.test.Test
import kotlin.test.assertNotEquals

/**
 * Each case builds two documents differing in exactly one axis and requires the rendered outcome to
 * differ. A case could pass for the wrong reason — because both variants are rejected outright — so
 * every case also asserts that the baseline document actually lays out.
 *
 * Only the oracle is consulted here. This test asks whether an axis reaches the parser at all, which
 * is a property of the harness rather than of the port; running both sides would only make a real
 * port divergence show up as a liveness failure and confuse the diagnosis.
 *
 * Some axes are inert outside a context the single-widget baseline doesn't provide — bias needs
 * opposing anchors to have room to move in, chain weight needs a chain, a guideline path needs a
 * widget anchored to the guideline rather than the parent. Those cases build their own two-document
 * pair instead of mutating [baseSpec] directly; [assertAxisLive] is the shared assertion underneath
 * both that and the [assertLive] / [assertDocumentAxisLive] helpers.
 *
 * Every case in this file is live — see the task report for the two findings that took two rounds to
 * get there:
 *
 * `hBias`, `vBias`, `hRtlBias`, `centerVertically`, `chainStyle`, `hWeight` and `vWeight` are driven
 * through [OracleConstraintSet.measure] instead of the default [OracleConstraintSet.parse]. The
 * actual defect wasn't `layout()` vs `measure()` as first suspected — it was that neither entry point
 * ever told `State` the document's root size (`state.setWidth`/`setHeight`), so `State` defaulted the
 * root to `WRAP_CONTENT`. That default is harmless for a widget anchored on one side only, but
 * `ConstraintWidget.applyConstraints`'s bias-centering equation, and the chain/`MATCH_CONSTRAINT`
 * machinery, both special-case an unresolved parent and never compute a real bias/spread split once
 * it applies. `measure` (see [ConstraintSetSubject.measure] and `OracleConstraintSet.measure`'s kdoc
 * for the full bisection) sets the root size correctly in addition to using a real `Measurer`; `parse`
 * remains untouched and is still the entry point for every axis that doesn't need either.
 *
 * `variableNum` and `variableGenerator` reference a variable the document declares, via
 * [FloatValue.Named] on a widget's `alpha`, rather than writing a literal — see their own comment
 * below and [FloatValue]'s kdoc for why a literal can never make these two live.
 */
class AxisLivenessTest {
    private fun assertAxisLive(
        name: String,
        before: ConstraintSetSpec,
        after: ConstraintSetSpec,
        outcome: (ConstraintSetSpec) -> ConstraintSetOutcome = OracleConstraintSet::parse,
    ) {
        val a = outcome(before)
        val b = outcome(after)
        check(a is ConstraintSetOutcome.Populated) { "$name: the baseline document does not lay out: $a" }
        check(b is ConstraintSetOutcome.Populated) { "$name: the mutated document does not lay out: $b" }
        assertNotEquals(a, b, "$name is generated but changes nothing the harness observes")
    }

    private fun assertLive(
        name: String,
        outcome: (ConstraintSetSpec) -> ConstraintSetOutcome = OracleConstraintSet::parse,
        mutate: (WidgetSpec) -> WidgetSpec,
    ) {
        val before = baseSpec()
        assertAxisLive(name, before, before.copy(widgets = before.widgets.map(mutate)), outcome)
    }

    private fun assertDocumentAxisLive(name: String, mutate: (ConstraintSetSpec) -> ConstraintSetSpec) {
        assertAxisLive(name, baseSpec(), mutate(baseSpec()))
    }

    // ---- Step 2 baseline (kept here rather than duplicated from the brief) ----

    @Test fun width() = assertLive("width") { it.copy(width = DimensionSpec.Fixed(80)) }

    @Test fun height() = assertLive("height") { it.copy(height = DimensionSpec.Fixed(80)) }

    @Test fun margin() = assertLive("margin") {
        it.copy(anchors = it.anchors.map { a -> a.copy(margin = AnchorMargin.Margin(64)) })
    }

    @Test fun visibility() = assertLive("visibility") { it.copy(visibility = Visibility.GONE) }

    @Test fun customFloat() = assertLive("custom float") {
        it.copy(custom = mapOf("shade" to CustomValue.Num(0.9f)))
    }

    @Test fun isRtl() = assertDocumentAxisLive("isRtl") { it.copy(isRtl = true) }

    // ---- every anchor kind ----
    // Each test replaces the baseline's two anchors with a single anchor of the kind under test,
    // pinned to parent's matching edge at a margin the baseline never uses (80). The baseline
    // (start+top, margin 16) and the variant always disagree on position, which is enough to prove
    // that specific `Anchor` value is read by `parseConstraint` and reaches the box the harness
    // observes — see the `margin` test above for proof the *argument* on an anchor is live at all.

    private fun singleAnchor(from: Anchor, to: Anchor) = { w: WidgetSpec ->
        w.copy(anchors = listOf(AnchorSpec(from, AnchorTarget.Parent, to, AnchorMargin.Margin(80))))
    }

    @Test fun anchorStart() = assertLive("anchor start") { singleAnchor(Anchor.START, Anchor.START)(it) }

    @Test fun anchorEnd() = assertLive("anchor end") { singleAnchor(Anchor.END, Anchor.END)(it) }

    @Test fun anchorLeft() = assertLive("anchor left") { singleAnchor(Anchor.LEFT, Anchor.LEFT)(it) }

    @Test fun anchorRight() = assertLive("anchor right") { singleAnchor(Anchor.RIGHT, Anchor.RIGHT)(it) }

    @Test fun anchorTop() = assertLive("anchor top") { singleAnchor(Anchor.TOP, Anchor.TOP)(it) }

    @Test fun anchorBottom() = assertLive("anchor bottom") { singleAnchor(Anchor.BOTTOM, Anchor.BOTTOM)(it) }

    @Test fun anchorBaseline() = assertLive("anchor baseline") { singleAnchor(Anchor.BASELINE, Anchor.BASELINE)(it) }

    // ---- circular, center, centerHorizontally, centerVertically ----

    @Test fun circular() {
        fun spec(distance: Int) = baseSpec().copy(
            widgets = listOf(
                baseWidget(),
                baseWidget().copy(
                    id = "id2",
                    width = DimensionSpec.Fixed(20),
                    height = DimensionSpec.Fixed(20),
                    anchors = emptyList(),
                    circular = CircularSpec(target = "id1", angle = 0f, distance = distance),
                    custom = emptyMap(),
                ),
            ),
        )
        assertAxisLive("circular", spec(50), spec(150))
    }

    @Test fun center() = assertLive("center") { it.copy(center = AnchorTarget.Parent) }

    @Test fun centerHorizontally() = assertLive("centerHorizontally") { it.copy(centerHorizontally = AnchorTarget.Parent) }

    // Driven through `measure` — see the class kdoc.
    @Test fun centerVertically() = assertLive("centerVertically", outcome = OracleConstraintSet::measure) {
        it.copy(centerVertically = AnchorTarget.Parent)
    }

    // ---- hBias, vBias, hRtlBias ----
    // Bias only has room to act between two opposing anchors; the single-anchor baseline gives it
    // none, so each of these builds its own two-anchor-per-axis fixture. All three are driven through
    // `measure` — see the class kdoc for why `parse` cannot observe bias.

    private fun opposedHorizontal(): WidgetSpec = baseWidget().copy(
        anchors = baseWidget().anchors + AnchorSpec(Anchor.END, AnchorTarget.Parent, Anchor.END, AnchorMargin.Margin(16)),
    )

    private fun opposedVertical(): WidgetSpec = baseWidget().copy(
        anchors = baseWidget().anchors + AnchorSpec(Anchor.BOTTOM, AnchorTarget.Parent, Anchor.BOTTOM, AnchorMargin.Margin(16)),
    )

    @Test fun hBias() {
        val before = baseSpec().copy(widgets = listOf(opposedHorizontal()))
        val after = baseSpec().copy(widgets = listOf(opposedHorizontal().copy(hBias = 0.9f)))
        assertAxisLive("hBias", before, after, outcome = OracleConstraintSet::measure)
    }

    @Test fun vBias() {
        val before = baseSpec().copy(widgets = listOf(opposedVertical()))
        val after = baseSpec().copy(widgets = listOf(opposedVertical().copy(vBias = 0.9f)))
        assertAxisLive("vBias", before, after, outcome = OracleConstraintSet::measure)
    }

    // hRtlBias is only read once the document is RTL (it still sets horizontalBias when LTR, but the
    // brief calls for isRtl = true on both documents, since that's the situation the attribute exists
    // for — the reversal in `"hRtlBias" -> { ... if (state.isRtl) { value = 1f - value } ... }`).
    @Test fun hRtlBias() {
        val before = baseSpec().copy(isRtl = true, widgets = listOf(opposedHorizontal()))
        val after = baseSpec().copy(isRtl = true, widgets = listOf(opposedHorizontal().copy(hRtlBias = 0.9f)))
        assertAxisLive("hRtlBias", before, after, outcome = OracleConstraintSet::measure)
    }

    // ---- alpha, rotations, scales, translations, pivots ----
    // These land on `WidgetFrame`, not the box `left/top/width/height` — see the `GeometryRow`
    // widening in ConstraintSetOutcome.kt.

    @Test fun alpha() = assertLive("alpha") { it.copy(alpha = FloatValue.Literal(0.3f)) }

    @Test fun rotationX() = assertLive("rotationX") { it.copy(rotationX = 45f) }

    @Test fun rotationY() = assertLive("rotationY") { it.copy(rotationY = 45f) }

    @Test fun rotationZ() = assertLive("rotationZ") { it.copy(rotationZ = 45f) }

    @Test fun scaleX() = assertLive("scaleX") { it.copy(scaleX = 2f) }

    @Test fun scaleY() = assertLive("scaleY") { it.copy(scaleY = 2f) }

    @Test fun translationX() = assertLive("translationX") { it.copy(translationX = 25f) }

    @Test fun translationY() = assertLive("translationY") { it.copy(translationY = 25f) }

    @Test fun translationZ() = assertLive("translationZ") { it.copy(translationZ = 25f) }

    @Test fun pivotX() = assertLive("pivotX") { it.copy(pivotX = 0.25f) }

    @Test fun pivotY() = assertLive("pivotY") { it.copy(pivotY = 0.25f) }

    // ---- custom property kinds ----

    @Test fun customColor() = assertLive("custom color") {
        it.copy(custom = mapOf("shade" to CustomValue.Color("#ff0000")))
    }

    // ---- hWeight, vWeight ----
    // Weight is only read by a chain's run, and only changes anything for a MATCH_CONSTRAINT
    // (spread) member — a fixed-size member ignores it entirely. Both fixtures below are two widgets
    // wired into a chain by mutual anchors, each spread across the axis under test, so a heavier
    // `id1` should claim more of the shared space than `id2`.
    //
    // Both driven through `measure` — see the class kdoc. MATCH_CONSTRAINT sizing (a prerequisite
    // for weight to have anything to redistribute) resolves the same way bias does: only through the
    // dependency-graph analysis `measure(...)` reaches and `parse`'s bare `layout()` does not.

    private fun hChainWidgets(): List<WidgetSpec> = listOf(
        baseWidget().copy(
            width = DimensionSpec.Mode(DimensionMode.SPREAD),
            anchors = listOf(
                AnchorSpec(Anchor.START, AnchorTarget.Parent, Anchor.START, AnchorMargin.Margin(0)),
                AnchorSpec(Anchor.END, AnchorTarget.Widget("id2"), Anchor.START, AnchorMargin.Margin(0)),
                AnchorSpec(Anchor.TOP, AnchorTarget.Parent, Anchor.TOP, AnchorMargin.Margin(16)),
            ),
            custom = emptyMap(),
        ),
        baseWidget().copy(
            id = "id2",
            width = DimensionSpec.Mode(DimensionMode.SPREAD),
            anchors = listOf(
                AnchorSpec(Anchor.START, AnchorTarget.Widget("id1"), Anchor.END, AnchorMargin.Margin(0)),
                AnchorSpec(Anchor.END, AnchorTarget.Parent, Anchor.END, AnchorMargin.Margin(0)),
                AnchorSpec(Anchor.TOP, AnchorTarget.Parent, Anchor.TOP, AnchorMargin.Margin(16)),
            ),
            custom = emptyMap(),
        ),
    )

    private fun vChainWidgets(): List<WidgetSpec> = listOf(
        baseWidget().copy(
            height = DimensionSpec.Mode(DimensionMode.SPREAD),
            anchors = listOf(
                AnchorSpec(Anchor.TOP, AnchorTarget.Parent, Anchor.TOP, AnchorMargin.Margin(0)),
                AnchorSpec(Anchor.BOTTOM, AnchorTarget.Widget("id2"), Anchor.TOP, AnchorMargin.Margin(0)),
                AnchorSpec(Anchor.START, AnchorTarget.Parent, Anchor.START, AnchorMargin.Margin(16)),
            ),
            custom = emptyMap(),
        ),
        baseWidget().copy(
            id = "id2",
            height = DimensionSpec.Mode(DimensionMode.SPREAD),
            anchors = listOf(
                AnchorSpec(Anchor.TOP, AnchorTarget.Widget("id1"), Anchor.BOTTOM, AnchorMargin.Margin(0)),
                AnchorSpec(Anchor.BOTTOM, AnchorTarget.Parent, Anchor.BOTTOM, AnchorMargin.Margin(0)),
                AnchorSpec(Anchor.START, AnchorTarget.Parent, Anchor.START, AnchorMargin.Margin(16)),
            ),
            custom = emptyMap(),
        ),
    )

    @Test fun hWeight() {
        val widgets = hChainWidgets()
        val before = baseSpec().copy(widgets = widgets)
        val after = baseSpec().copy(widgets = listOf(widgets[0].copy(hWeight = 5f), widgets[1]))
        assertAxisLive("hWeight", before, after, outcome = OracleConstraintSet::measure)
    }

    @Test fun vWeight() {
        val widgets = vChainWidgets()
        val before = baseSpec().copy(widgets = widgets)
        val after = baseSpec().copy(widgets = listOf(widgets[0].copy(vWeight = 5f), widgets[1]))
        assertAxisLive("vWeight", before, after, outcome = OracleConstraintSet::measure)
    }

    // ---- guideline declaration path ----
    // `parseHelpers` -> `parseGuideline` (Helpers array) and `populateState` -> `parseGuidelineParams`
    // (typed top-level element) are different code paths for the same object. Each test anchors the
    // widget to the guideline instead of the parent, so a guideline that fails to apply through that
    // path leaves the widget positioned at the parent's edge instead — a difference from the no-
    // guideline baseline.

    private fun guidelineWidget(): WidgetSpec = baseWidget().copy(
        anchors = listOf(
            AnchorSpec(Anchor.START, AnchorTarget.Widget("g0"), Anchor.START, AnchorMargin.Margin(0)),
            AnchorSpec(Anchor.TOP, AnchorTarget.Parent, Anchor.TOP, AnchorMargin.Margin(16)),
        ),
    )

    private fun guidelineSpec(inHelpers: Boolean): ConstraintSetSpec = baseSpec().copy(
        widgets = listOf(guidelineWidget()),
        guidelines = listOf(
            GuidelineSpec(id = "g0", horizontal = false, position = GuidelinePosition.FromStart(200), inHelpers = inHelpers),
        ),
    )

    @Test fun guidelineViaHelpers() = assertAxisLive("guideline via Helpers", baseSpec(), guidelineSpec(inHelpers = true))

    @Test fun guidelineViaTypedElement() =
        assertAxisLive("guideline via typed element", baseSpec(), guidelineSpec(inHelpers = false))

    // ---- barrier direction, barrier margin ----

    private fun barrierWidgets(): List<WidgetSpec> = listOf(
        baseWidget(),
        baseWidget().copy(
            id = "id2",
            anchors = listOf(
                AnchorSpec(Anchor.START, AnchorTarget.Widget("b0"), Anchor.START, AnchorMargin.Margin(0)),
                AnchorSpec(Anchor.TOP, AnchorTarget.Parent, Anchor.TOP, AnchorMargin.Margin(16)),
            ),
        ),
    )

    private fun barrierSpec(direction: BarrierDirection, margin: Int?): ConstraintSetSpec = baseSpec().copy(
        widgets = barrierWidgets(),
        barriers = listOf(BarrierSpec(id = "b0", direction = direction, margin = margin, refs = listOf("id1"))),
    )

    @Test fun barrierDirection() =
        assertAxisLive("barrier direction", barrierSpec(BarrierDirection.END, null), barrierSpec(BarrierDirection.START, null))

    @Test fun barrierMargin() =
        assertAxisLive("barrier margin", barrierSpec(BarrierDirection.END, null), barrierSpec(BarrierDirection.END, 100))

    // ---- chain style ----

    private fun chainWidgets(): List<WidgetSpec> = listOf(
        baseWidget().copy(
            width = DimensionSpec.Fixed(100),
            anchors = listOf(
                AnchorSpec(Anchor.START, AnchorTarget.Parent, Anchor.START, AnchorMargin.Margin(0)),
                AnchorSpec(Anchor.END, AnchorTarget.Widget("id2"), Anchor.START, AnchorMargin.Margin(0)),
                AnchorSpec(Anchor.TOP, AnchorTarget.Parent, Anchor.TOP, AnchorMargin.Margin(16)),
            ),
        ),
        baseWidget().copy(
            id = "id2",
            width = DimensionSpec.Fixed(100),
            anchors = listOf(
                AnchorSpec(Anchor.START, AnchorTarget.Widget("id1"), Anchor.END, AnchorMargin.Margin(0)),
                AnchorSpec(Anchor.END, AnchorTarget.Parent, Anchor.END, AnchorMargin.Margin(0)),
                AnchorSpec(Anchor.TOP, AnchorTarget.Parent, Anchor.TOP, AnchorMargin.Margin(16)),
            ),
            custom = emptyMap(),
        ),
    )

    private fun chainSpec(style: ChainStyle?): ConstraintSetSpec = baseSpec().copy(
        widgets = chainWidgets(),
        chains = listOf(ChainSpec(id = "c0", horizontal = true, refs = listOf("id1", "id2"), style = style)),
    )

    // Driven through `measure` — see the class kdoc.
    @Test fun chainStyle() = assertAxisLive(
        "chain style", chainSpec(ChainStyle.PACKED), chainSpec(ChainStyle.SPREAD_INSIDE),
        outcome = OracleConstraintSet::measure,
    )

    // ---- variables, Generate ----
    // `VariableSpec.IdList` is exercised through `Generate` below, the only thing in this document
    // model that ever consumes a variable by name.

    @Test fun generate() = assertDocumentAxisLive("Generate") { spec ->
        spec.copy(
            variables = listOf(VariableSpec.IdList("ids", listOf("gen0"))),
            generate = GenerateSpec(
                listName = "ids",
                body = baseWidget().copy(
                    id = "generated",
                    anchors = listOf(
                        AnchorSpec(Anchor.START, AnchorTarget.Parent, Anchor.START, AnchorMargin.Margin(500)),
                        AnchorSpec(Anchor.TOP, AnchorTarget.Parent, Anchor.TOP, AnchorMargin.Margin(500)),
                    ),
                    custom = emptyMap(),
                ),
            ),
        )
    }

    // `VariableSpec.Num` and `VariableSpec.Generator` are declared into `LayoutVariables` by
    // `parseVariables`, and reach the parser meaningfully only when something references them by
    // name — a `FloatValue.Named` value does exactly that (see its kdoc). `alpha` is the one field
    // this harness's document model lets carry a `FloatValue`, so each case references the same
    // variable name from a widget's `alpha` in two documents whose only difference is the
    // variable's own declared value.

    private fun namedAlphaSpec(variable: VariableSpec): ConstraintSetSpec = baseSpec().copy(
        widgets = listOf(baseWidget().copy(alpha = FloatValue.Named(variable.name))),
        variables = listOf(variable),
    )

    @Test fun variableNum() = assertAxisLive(
        "variable num", namedAlphaSpec(VariableSpec.Num("v0", 10f)), namedAlphaSpec(VariableSpec.Num("v0", 80f)),
    )

    // `Generator`'s value is stateful (`LayoutVariables.Generator.value()` adds `step` to `from` on
    // read), so two different `step`s reliably resolve to two different alphas on first reference.
    @Test fun variableGenerator() = assertAxisLive(
        "variable generator",
        namedAlphaSpec(VariableSpec.Generator("v0", 0f, 1f)),
        namedAlphaSpec(VariableSpec.Generator("v0", 0f, 50f)),
    )
}
