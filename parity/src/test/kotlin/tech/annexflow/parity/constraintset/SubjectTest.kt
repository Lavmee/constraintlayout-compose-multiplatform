// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.constraintset

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SubjectTest {
    private val subjects = listOf(OracleConstraintSet, PortConstraintSet)

    private fun oneWidget() = ConstraintSetSpec(
        seed = 1, rootWidth = 1000, rootHeight = 1000, isRtl = false,
        widgets = listOf(
            WidgetSpec(
                id = "id1",
                width = DimensionSpec.Fixed(40),
                height = DimensionSpec.Fixed(40),
                anchors = listOf(
                    AnchorSpec(Anchor.START, AnchorTarget.Parent, Anchor.START, AnchorMargin.Margin(16)),
                    AnchorSpec(Anchor.TOP, AnchorTarget.Parent, Anchor.TOP, AnchorMargin.Margin(16)),
                ),
                circular = null, centerHorizontally = null, centerVertically = null, center = null,
                hBias = null, vBias = null, hRtlBias = null, hWeight = null, vWeight = null,
                visibility = null, alpha = null,
                rotationX = null, rotationY = null, rotationZ = null,
                scaleX = null, scaleY = null,
                translationX = null, translationY = null, translationZ = null,
                pivotX = null, pivotY = null,
                custom = mapOf("shade" to CustomValue.Num(0.5f)),
            ),
        ),
        chains = emptyList(), guidelines = emptyList(), barriers = emptyList(),
        variables = emptyList(), generate = null,
    )

    @Test
    fun bothSubjectsLayOutTheSameDocumentIdentically() {
        val outcomes = subjects.map { it.parse(oneWidget()) }
        assertEquals(outcomes[0], outcomes[1])
        assertTrue(outcomes[0] is ConstraintSetOutcome.Populated, "got ${outcomes[0]}")
    }

    @Test
    fun theCustomPropertyIsVisibleInTheOutcome() {
        val populated = OracleConstraintSet.parse(oneWidget()) as ConstraintSetOutcome.Populated
        assertTrue(populated.custom.contains("id1.shade="), populated.custom)
    }

    @Test
    fun bothSubjectsAgreeOnDesignElements() {
        val spec = DesignElementsSpec(1, listOf(DesignElementSpec("e1", "button", mapOf("text" to "hi"))))
        assertEquals(
            OracleConstraintSet.designElements(spec),
            PortConstraintSet.designElements(spec),
        )
    }
}
