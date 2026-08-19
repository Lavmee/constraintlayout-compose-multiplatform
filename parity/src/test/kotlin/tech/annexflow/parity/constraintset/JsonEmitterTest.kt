// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.constraintset

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JsonEmitterTest {
    private fun widget(id: String) = WidgetSpec(
        id = id,
        width = DimensionSpec.Fixed(40),
        height = DimensionSpec.Fixed(40),
        anchors = emptyList(),
        circular = null,
        centerHorizontally = null,
        centerVertically = null,
        center = null,
        hBias = null, vBias = null, hRtlBias = null,
        hWeight = null, vWeight = null,
        visibility = null, alpha = null,
        rotationX = null, rotationY = null, rotationZ = null,
        scaleX = null, scaleY = null,
        translationX = null, translationY = null, translationZ = null,
        pivotX = null, pivotY = null,
        custom = emptyMap(),
    )

    private fun spec(vararg w: WidgetSpec) = ConstraintSetSpec(
        seed = 1, rootWidth = 1000, rootHeight = 1000, isRtl = false,
        widgets = w.toList(), chains = emptyList(), guidelines = emptyList(),
        barriers = emptyList(), variables = emptyList(), generate = null,
    )

    @Test
    fun emitsAnObjectPerWidget() {
        val json = emit(spec(widget("a")))
        assertTrue(json.contains("a: {"), json)
        assertTrue(json.contains("width: 40"), json)
    }

    @Test
    fun emitsEveryDimensionForm() {
        val forms = listOf(
            DimensionSpec.Fixed(12) to "12",
            DimensionSpec.Mode(DimensionMode.PREFER_WRAP) to "'preferWrap'",
            DimensionSpec.Percent(50f) to "'50%'",
            DimensionSpec.Ratio("16:9") to "'16:9'",
        )
        for ((form, expected) in forms) {
            val json = emit(spec(widget("a").copy(width = form)))
            assertTrue(json.contains("width: $expected"), "$form -> $json")
        }
    }

    // `DimensionSpec.Bounded` isn't one of `emitsEveryDimensionForm`'s forms — it renders as its own
    // `{value: …, min: …, max: …}` object rather than a bare scalar or quoted string, and it's
    // roughly a fifth of every generated width/height draw (see `Scenarios.dimension`), so a wrong
    // key name here would quietly degrade a large slice of the corpus to default dimensions with
    // both sides agreeing and nothing catching it.
    @Test
    fun emitsBoundedDimensionShape() {
        val json = emit(
            spec(
                widget("a").copy(
                    width = DimensionSpec.Bounded(
                        value = DimensionMode.SPREAD,
                        min = Bound.Pixels(10),
                        max = Bound.Wrap,
                    ),
                ),
            ),
        )
        assertTrue(json.contains("width: {value: 'spread', min: 10, max: 'wrap'}"), json)
    }

    @Test
    fun anchorRendersAsAnArrayOfTargetAnchorMargin() {
        val w = widget("a").copy(
            anchors = listOf(
                AnchorSpec(Anchor.START, AnchorTarget.Parent, Anchor.START, margin = AnchorMargin.Margin(16)),
            ),
        )
        assertTrue(emit(spec(w)).contains("start: ['parent', 'start', 16]"), emit(spec(w)))
    }

    @Test
    fun anchorMarginRendersPositionally() {
        fun anchorJson(margin: AnchorMargin?): String {
            val w = widget("a").copy(
                anchors = listOf(AnchorSpec(Anchor.TOP, AnchorTarget.Widget("b"), Anchor.BOTTOM, margin)),
            )
            return emit(spec(w))
        }

        assertTrue(anchorJson(null).contains("top: ['b', 'bottom']"), anchorJson(null))
        assertTrue(
            anchorJson(AnchorMargin.Margin(8)).contains("top: ['b', 'bottom', 8]"),
            anchorJson(AnchorMargin.Margin(8)),
        )
        assertTrue(
            anchorJson(AnchorMargin.MarginAndGone(8, 4)).contains("top: ['b', 'bottom', 8, 4]"),
            anchorJson(AnchorMargin.MarginAndGone(8, 4)),
        )
    }

    @Test
    fun emissionIsDeterministic() {
        val s = spec(widget("a"), widget("b"))
        assertEquals(emit(s), emit(s))
    }
}
