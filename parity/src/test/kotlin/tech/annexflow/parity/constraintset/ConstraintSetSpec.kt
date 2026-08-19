// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.constraintset

/**
 * A ConstraintSet document described without reference to either implementation, and without JSON
 * syntax. [JsonEmitter] owns the syntax; this owns the vocabulary.
 */

/** The anchors `parseConstraint` accepts. */
enum class Anchor { START, END, LEFT, RIGHT, TOP, BOTTOM, BASELINE }

/** The three values `applyAttribute` accepts for `visibility`. */
enum class Visibility { VISIBLE, INVISIBLE, GONE }

/** The four bare-string forms `parseDimensionMode` accepts. */
enum class DimensionMode { WRAP, PREFER_WRAP, SPREAD, PARENT }

/** `min` and `max` inside the object dimension form: a number, or any string meaning wrap. */
sealed interface Bound {
    data class Pixels(val dp: Int) : Bound

    data object Wrap : Bound
}

/** The four shapes `parseDimension` branches on: number, string, percent/ratio string, object. */
sealed interface DimensionSpec {
    data class Fixed(val dp: Int) : DimensionSpec

    data class Mode(val mode: DimensionMode) : DimensionSpec

    /** Rendered as `"50%"`; `parseDimensionMode` divides by 100. */
    data class Percent(val percent: Float) : DimensionSpec

    /** Rendered verbatim, e.g. `"16:9"`; recognised by containing a colon. */
    data class Ratio(val ratio: String) : DimensionSpec

    data class Bounded(val value: DimensionMode?, val min: Bound?, val max: Bound?) : DimensionSpec
}

sealed interface AnchorTarget {
    data object Parent : AnchorTarget

    data class Widget(val id: String) : AnchorTarget
}

/**
 * `parseConstraint`'s anchor array is positional: margin sits at index 2, goneMargin at index 3,
 * and the parser only reads index 3 when the array is at least 4 elements long — which means it
 * only ever reads a goneMargin when a margin is present too. Modelling margin and goneMargin as
 * two independently-nullable `Int`s would let a caller build `margin = null, goneMargin = 20`, a
 * state the wire format cannot express; the emitter would then have to choose between silently
 * dropping it or refusing to render it. Folding them into one nullable holder, with goneMargin only
 * reachable through the variant that also carries a margin, makes that state unconstructable
 * instead of merely unrendered.
 */
sealed interface AnchorMargin {
    data class Margin(val dp: Int) : AnchorMargin

    data class MarginAndGone(val dp: Int, val goneDp: Int) : AnchorMargin
}

/** `from: [target, toAnchor, margin?, goneMargin?]` — a null [margin] renders the two-element form. */
data class AnchorSpec(
    val from: Anchor,
    val target: AnchorTarget,
    val to: Anchor,
    val margin: AnchorMargin?,
)

data class CircularSpec(val target: String, val angle: Float, val distance: Int)

/** A custom property is a number or a colour string; a malformed colour is deliberately allowed. */
sealed interface CustomValue {
    data class Num(val value: Float) : CustomValue

    data class Color(val literal: String) : CustomValue
}

/**
 * A float-valued widget attribute: a literal, or the name of a variable the document declares
 * under [ConstraintSetSpec.variables]. `ConstraintSetParser.applyAttribute` reads every transform
 * and bias attribute as `value = layoutVariables[element[attributeName]]`
 * (`ConstraintSetParser.kt`, `applyAttribute`, lines 1536-1627), and `LayoutVariables.get` resolves
 * a `CLString` by looking up a declared `Num` or `Generator` variable by that exact name — a bare
 * number goes through the same call unchanged. [Named] is how a generated document exercises that
 * lookup instead of always supplying a literal.
 *
 * [Named] only means something when the document actually declares that name as a `Num` or
 * `Generator` variable: `LayoutVariables.get` silently resolves an unknown name to `0f` rather than
 * failing, which would look "live" (a different number reaches the widget) without the variable's
 * own value ever being read — see `Scenarios.alphaValue`, the only place this harness constructs a
 * [Named] value, which only ever names a variable it just placed in the same document's
 * [ConstraintSetSpec.variables].
 */
sealed interface FloatValue {
    data class Literal(val value: Float) : FloatValue

    data class Named(val name: String) : FloatValue
}

data class WidgetSpec(
    val id: String,
    val width: DimensionSpec,
    val height: DimensionSpec,
    val anchors: List<AnchorSpec>,
    val circular: CircularSpec?,
    val centerHorizontally: AnchorTarget?,
    val centerVertically: AnchorTarget?,
    val center: AnchorTarget?,
    val hBias: Float?,
    val vBias: Float?,
    val hRtlBias: Float?,
    val hWeight: Float?,
    val vWeight: Float?,
    val visibility: Visibility?,
    val alpha: FloatValue?,
    val rotationX: Float?,
    val rotationY: Float?,
    val rotationZ: Float?,
    val scaleX: Float?,
    val scaleY: Float?,
    val translationX: Float?,
    val translationY: Float?,
    val translationZ: Float?,
    val pivotX: Float?,
    val pivotY: Float?,
    val custom: Map<String, CustomValue>,
)

enum class ChainStyle { SPREAD, SPREAD_INSIDE, PACKED }

/**
 * A chain, always emitted as a typed top-level element (`type: 'hChain'`) rather than through the
 * `Helpers` array, so it can carry a style and a bias. Guidelines cover the `Helpers` path.
 */
data class ChainSpec(
    val id: String,
    val horizontal: Boolean,
    val refs: List<String>,
    val style: ChainStyle?,
)

sealed interface GuidelinePosition {
    /**
     * Rendered as `start` regardless of orientation — `parseGuidelineParams` only recognises
     * `left`/`right`/`start`/`end`/`percent`; there is no `top` (or `bottom`) key for a horizontal
     * guideline, so `start` is the correct key for both orientations, not just the vertical one.
     */
    data class FromStart(val dp: Int) : GuidelinePosition

    data class FromEnd(val dp: Int) : GuidelinePosition

    data class Percent(val fraction: Float) : GuidelinePosition
}

/**
 * [inHelpers] chooses between the two declaration paths the parser supports for the same object:
 * an entry in the `Helpers` array (`parseHelpers` → `parseGuideline`) or a typed top-level element
 * (`populateState` → `parseGuidelineParams`). Both are generated; they are different code.
 */
data class GuidelineSpec(
    val id: String,
    val horizontal: Boolean,
    val position: GuidelinePosition,
    val inHelpers: Boolean,
)

enum class BarrierDirection { START, END, LEFT, RIGHT, TOP, BOTTOM }

/** Barriers are never in `Helpers` — `parseHelpers` has no case for them. Always typed elements. */
data class BarrierSpec(
    val id: String,
    val direction: BarrierDirection,
    val margin: Int?,
    val refs: List<String>,
)

sealed interface VariableSpec {
    val name: String

    data class Num(override val name: String, val value: Float) : VariableSpec

    /** `{from: x, step: y}` — `LayoutVariables.put(name, start, incrementBy)`. */
    data class Generator(override val name: String, val from: Float, val step: Float) : VariableSpec

    /** An id list, the only variable kind `Generate` can consume. */
    data class IdList(override val name: String, val ids: List<String>) : VariableSpec
}

/** One `Generate` entry: a widget body stamped across every id in the named list variable. */
data class GenerateSpec(val listName: String, val body: WidgetSpec)

data class ConstraintSetSpec(
    val seed: Long,
    val rootWidth: Int,
    val rootHeight: Int,
    val isRtl: Boolean,
    val widgets: List<WidgetSpec>,
    val chains: List<ChainSpec>,
    val guidelines: List<GuidelineSpec>,
    val barriers: List<BarrierSpec>,
    val variables: List<VariableSpec>,
    val generate: GenerateSpec?,
)

/** The separate `parseDesignElementsJSON` entry point. */
data class DesignElementSpec(val id: String, val type: String, val params: Map<String, String>)

data class DesignElementsSpec(val seed: Long, val elements: List<DesignElementSpec>)
