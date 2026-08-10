// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.solver

/**
 * A layout described without reference to either implementation.
 *
 * The parser harness could put its inputs in files because a parser's input is data. A solver's
 * input is a program — a sequence of API calls — and the two implementations expose that API under
 * different package names, so no single piece of code can drive both. This is the shared
 * description each subject replays with its own classes.
 */

/** Mirrors `ConstraintWidget.DimensionBehaviour`, which exists separately in each package. */
enum class Behaviour { FIXED, WRAP_CONTENT, MATCH_CONSTRAINT, MATCH_PARENT }

/** Mirrors the subset of `ConstraintAnchor.Type` this harness uses. */
enum class Side { LEFT, TOP, RIGHT, BOTTOM }

val Side.isHorizontal: Boolean
    get() = this == Side.LEFT || this == Side.RIGHT

/** `0` for a minimum means unset; `Int.MAX_VALUE` for a maximum means unset. */
data class WidgetSpec(
    val name: String,
    val width: Int,
    val height: Int,
    val horizontal: Behaviour,
    val vertical: Behaviour,
    val minWidth: Int,
    val minHeight: Int,
    val maxWidth: Int,
    val maxHeight: Int,
    val horizontalBias: Float,
    val verticalBias: Float,
)

/** [target] is an index into [Scenario.widgets], or `null` for the root. */
data class ConnectionSpec(
    val from: Int,
    val fromSide: Side,
    val target: Int?,
    val toSide: Side,
    val margin: Int,
)

data class Scenario(
    val seed: Long,
    val rootWidth: Int,
    val rootHeight: Int,
    val rootHorizontal: Behaviour,
    val rootVertical: Behaviour,
    val rootMinWidth: Int,
    val rootMinHeight: Int,
    val widgets: List<WidgetSpec>,
    val connections: List<ConnectionSpec>,
)
