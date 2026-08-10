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
    /** `setDimensionRatio`'s documented format: `[H|V],[float|x:y]` or `[float|x:y]`. Null when unset. */
    val dimensionRatio: String?,
)

/** What a connection can point at. */
sealed interface Target {
    data object Root : Target

    data class Widget(val index: Int) : Target

    data class Barrier(val index: Int) : Target

    data class Guideline(val index: Int) : Target
}

data class ConnectionSpec(
    val from: Int,
    val fromSide: Side,
    val target: Target,
    val toSide: Side,
    val margin: Int,
)

/**
 * A barrier resolves to the extreme edge of the widgets it references. [referenced] is never empty:
 * a barrier over nothing resolves to nothing useful and would be inert coverage.
 */
data class BarrierSpec(
    val name: String,
    val side: Side,
    val margin: Int,
    val referenced: List<Int>,
)

/** The three ways `Guideline` accepts a position, matching its `RELATIVE_*` modes. */
sealed interface GuidelinePosition {
    data class Begin(val value: Int) : GuidelinePosition

    data class End(val value: Int) : GuidelinePosition

    data class Percent(val value: Float) : GuidelinePosition
}

data class GuidelineSpec(
    val name: String,
    val vertical: Boolean,
    val position: GuidelinePosition,
)

enum class ChainStyle { SPREAD, SPREAD_INSIDE, PACKED }

/**
 * A chain is not a class in the engine — it emerges from a run of widgets linked to each other in
 * both directions, with a style set on the head. This record exists so the pattern can be asserted
 * complete; the connections themselves still live in [Scenario.connections].
 */
data class ChainSpec(
    val members: List<Int>,
    val horizontal: Boolean,
    val style: ChainStyle,
)

/**
 * A circular constraint, which positions a widget at an angle and distance from another rather than
 * by anchors. [target] is always lower than [from], so it cannot reintroduce a cycle.
 */
data class CircularSpec(val from: Int, val target: Int, val angle: Float, val radius: Int)

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
    val circular: List<CircularSpec>,
    val barriers: List<BarrierSpec>,
    val guidelines: List<GuidelineSpec>,
    val chains: List<ChainSpec>,
)
