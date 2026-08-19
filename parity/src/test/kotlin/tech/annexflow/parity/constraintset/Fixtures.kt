// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.constraintset

/**
 * Shared across [SubjectTest] and `AxisLivenessTest` so the two cannot drift apart. Neither file
 * may import either `ConstraintSetParser` package — this fixture, like the rest of the module,
 * describes a document without reference to either implementation.
 */

/** The smallest document that lays out: one widget pinned to the parent's top-start corner. */
fun baseWidget(): WidgetSpec = WidgetSpec(
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
)

fun baseSpec(): ConstraintSetSpec = ConstraintSetSpec(
    seed = 1, rootWidth = 1000, rootHeight = 1000, isRtl = false,
    widgets = listOf(baseWidget()),
    chains = emptyList(), guidelines = emptyList(), barriers = emptyList(),
    variables = emptyList(), generate = null,
)
