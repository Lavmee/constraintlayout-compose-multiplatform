// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.constraintset

/**
 * Renders a [ConstraintSetSpec] into the JSON5-ish dialect `CLParser` reads: unquoted keys,
 * single-quoted strings, bare numbers. This is the one file that knows that syntax — every shape
 * below was checked against `ConstraintSetParser` itself, not against the JSON5 spec.
 *
 * [ConstraintSetSpec.seed], [ConstraintSetSpec.rootWidth], [ConstraintSetSpec.rootHeight] and
 * [ConstraintSetSpec.isRtl] are not part of the emitted document: the parser never reads a root
 * size or a layout direction out of the JSON body, those are properties the caller sets on the
 * `State` before parsing. They exist on the spec for the harness driver, not for [emit].
 */

/**
 * A single site for float formatting, per the brief: strips the trailing `.0` a bare
 * `Float.toString()` would add, since the given test requires `'50%'`, not `'50.0%'`.
 */
private fun formatFloat(value: Float): String {
    val whole = value.toLong()
    return if (value == whole.toFloat()) whole.toString() else value.toString()
}

private fun quote(value: String): String = "'$value'"

private fun obj(entries: List<Pair<String, String>>): String =
    entries.joinToString(separator = ", ", prefix = "{", postfix = "}") { (key, value) -> "$key: $value" }

private fun arr(items: List<String>): String = items.joinToString(separator = ", ", prefix = "[", postfix = "]")

private fun anchorKey(anchor: Anchor): String = when (anchor) {
    Anchor.START -> "start"
    Anchor.END -> "end"
    Anchor.LEFT -> "left"
    Anchor.RIGHT -> "right"
    Anchor.TOP -> "top"
    Anchor.BOTTOM -> "bottom"
    Anchor.BASELINE -> "baseline"
}

private fun renderAnchor(anchor: Anchor): String = quote(anchorKey(anchor))

private fun renderAnchorTarget(target: AnchorTarget): String = when (target) {
    AnchorTarget.Parent -> quote("parent")
    is AnchorTarget.Widget -> quote(target.id)
}

/**
 * `parseConstraint`'s array form: `[target, anchor, margin, goneMargin]`. `AnchorMargin` already
 * makes "goneMargin without margin" unconstructable (see its kdoc); this just walks the three
 * remaining shapes positionally — 2, 3, or 4 elements — with no null-checking left to get wrong.
 */
private fun renderAnchorSpec(anchor: AnchorSpec): Pair<String, String> {
    val items = mutableListOf(renderAnchorTarget(anchor.target), renderAnchor(anchor.to))
    when (val margin = anchor.margin) {
        null -> Unit
        is AnchorMargin.Margin -> items += margin.dp.toString()
        is AnchorMargin.MarginAndGone -> {
            items += margin.dp.toString()
            items += margin.goneDp.toString()
        }
    }
    return anchorKey(anchor.from) to arr(items)
}

private fun dimensionModeWord(mode: DimensionMode): String = when (mode) {
    DimensionMode.WRAP -> "wrap"
    DimensionMode.PREFER_WRAP -> "preferWrap"
    DimensionMode.SPREAD -> "spread"
    DimensionMode.PARENT -> "parent"
}

private fun renderDimensionMode(mode: DimensionMode): String = quote(dimensionModeWord(mode))

private fun renderBound(bound: Bound): String = when (bound) {
    is Bound.Pixels -> bound.dp.toString()
    Bound.Wrap -> quote("wrap")
}

private fun renderDimension(dimension: DimensionSpec): String = when (dimension) {
    is DimensionSpec.Fixed -> dimension.dp.toString()
    is DimensionSpec.Mode -> renderDimensionMode(dimension.mode)
    is DimensionSpec.Percent -> quote("${formatFloat(dimension.percent)}%")
    is DimensionSpec.Ratio -> quote(dimension.ratio)
    is DimensionSpec.Bounded -> {
        val entries = mutableListOf<Pair<String, String>>()
        dimension.value?.let { entries += "value" to renderDimensionMode(it) }
        dimension.min?.let { entries += "min" to renderBound(it) }
        dimension.max?.let { entries += "max" to renderBound(it) }
        obj(entries)
    }
}

private fun renderVisibility(visibility: Visibility): String = quote(
    when (visibility) {
        Visibility.VISIBLE -> "visible"
        Visibility.INVISIBLE -> "invisible"
        Visibility.GONE -> "gone"
    },
)

private fun renderCustomValue(value: CustomValue): String = when (value) {
    is CustomValue.Num -> formatFloat(value.value)
    is CustomValue.Color -> quote(value.literal)
}

/**
 * A literal renders as a bare number, same as every other float attribute here. A [FloatValue.Named]
 * renders as a quoted string — `layoutVariables[element[attributeName]]` (`LayoutVariables.get`)
 * only takes the variable-lookup branch for a `CLString`; a bare number is read as itself. See
 * [FloatValue]'s kdoc for why a `Named` reference is only meaningful when the document declares
 * that name.
 */
private fun renderFloatValue(value: FloatValue): String = when (value) {
    is FloatValue.Literal -> formatFloat(value.value)
    is FloatValue.Named -> quote(value.name)
}

/** Attributes shared by an ordinary widget object and a `Generate` body — same `applyAttribute` loop. */
private fun renderWidgetAttributes(widget: WidgetSpec): List<Pair<String, String>> {
    val entries = mutableListOf<Pair<String, String>>()
    entries += "width" to renderDimension(widget.width)
    entries += "height" to renderDimension(widget.height)
    for (anchor in widget.anchors) {
        entries += renderAnchorSpec(anchor)
    }
    widget.circular?.let {
        entries += "circular" to arr(listOf(quote(it.target), formatFloat(it.angle), it.distance.toString()))
    }
    widget.center?.let { entries += "center" to renderAnchorTarget(it) }
    widget.centerHorizontally?.let { entries += "centerHorizontally" to renderAnchorTarget(it) }
    widget.centerVertically?.let { entries += "centerVertically" to renderAnchorTarget(it) }
    widget.hBias?.let { entries += "hBias" to formatFloat(it) }
    widget.vBias?.let { entries += "vBias" to formatFloat(it) }
    widget.hRtlBias?.let { entries += "hRtlBias" to formatFloat(it) }
    widget.hWeight?.let { entries += "hWeight" to formatFloat(it) }
    widget.vWeight?.let { entries += "vWeight" to formatFloat(it) }
    widget.visibility?.let { entries += "visibility" to renderVisibility(it) }
    widget.alpha?.let { entries += "alpha" to renderFloatValue(it) }
    widget.rotationX?.let { entries += "rotationX" to formatFloat(it) }
    widget.rotationY?.let { entries += "rotationY" to formatFloat(it) }
    widget.rotationZ?.let { entries += "rotationZ" to formatFloat(it) }
    widget.scaleX?.let { entries += "scaleX" to formatFloat(it) }
    widget.scaleY?.let { entries += "scaleY" to formatFloat(it) }
    widget.translationX?.let { entries += "translationX" to formatFloat(it) }
    widget.translationY?.let { entries += "translationY" to formatFloat(it) }
    widget.translationZ?.let { entries += "translationZ" to formatFloat(it) }
    widget.pivotX?.let { entries += "pivotX" to formatFloat(it) }
    widget.pivotY?.let { entries += "pivotY" to formatFloat(it) }
    if (widget.custom.isNotEmpty()) {
        entries += "custom" to obj(widget.custom.map { (name, value) -> name to renderCustomValue(value) })
    }
    return entries
}

private fun renderWidget(widget: WidgetSpec): Pair<String, String> = widget.id to obj(renderWidgetAttributes(widget))

private fun renderGuidelinePosition(position: GuidelinePosition): Pair<String, String> = when (position) {
    is GuidelinePosition.FromStart -> "start" to position.dp.toString()
    is GuidelinePosition.FromEnd -> "end" to position.dp.toString()
    // Bare number, not a percent-string: `parseGuidelineParams` reads this with `getFloat`, unlike
    // the dimension `Percent` form which is a quoted "n%" string read by `parseDimensionMode`.
    is GuidelinePosition.Percent -> "percent" to formatFloat(position.fraction)
}

private fun guidelineTypeWord(horizontal: Boolean): String = if (horizontal) "hGuideline" else "vGuideline"

/**
 * `parseHelpers` -> `parseGuideline`: `helper[1]` must be a `CLObject` carrying the `id` itself, so
 * the shape is `['hGuideline', {id: 'gid', start: 40}]` — a two-element array, not the three-element
 * `['hGuideline', 'id', {start: 40}]` the brief describes. See CLParserTest.testConstraints2, which
 * round-trips exactly this two-element shape. The parser is the authority; this follows the parser.
 */
private fun renderGuidelineInHelpers(guideline: GuidelineSpec): String {
    val (posKey, posValue) = renderGuidelinePosition(guideline.position)
    val params = obj(listOf("id" to quote(guideline.id), posKey to posValue))
    return arr(listOf(quote(guidelineTypeWord(guideline.horizontal)), params))
}

/** `populateState` -> `parseGuidelineParams`: a typed top-level element, `id: {type: ..., ...}`. */
private fun renderGuidelineTyped(guideline: GuidelineSpec): Pair<String, String> {
    val (posKey, posValue) = renderGuidelinePosition(guideline.position)
    val entries = listOf("type" to quote(guidelineTypeWord(guideline.horizontal)), posKey to posValue)
    return guideline.id to obj(entries)
}

private fun barrierDirectionWord(direction: BarrierDirection): String = when (direction) {
    BarrierDirection.START -> "start"
    BarrierDirection.END -> "end"
    BarrierDirection.LEFT -> "left"
    BarrierDirection.RIGHT -> "right"
    BarrierDirection.TOP -> "top"
    BarrierDirection.BOTTOM -> "bottom"
}

private fun renderBarrier(barrier: BarrierSpec): Pair<String, String> {
    val entries = mutableListOf<Pair<String, String>>()
    entries += "type" to quote("barrier")
    entries += "direction" to quote(barrierDirectionWord(barrier.direction))
    barrier.margin?.let { entries += "margin" to it.toString() }
    entries += "contains" to arr(barrier.refs.map { quote(it) })
    return barrier.id to obj(entries)
}

private fun chainStyleWord(style: ChainStyle): String = when (style) {
    ChainStyle.SPREAD -> "spread"
    ChainStyle.SPREAD_INSIDE -> "spread_inside"
    ChainStyle.PACKED -> "packed"
}

private fun renderChain(chain: ChainSpec): Pair<String, String> {
    val entries = mutableListOf<Pair<String, String>>()
    entries += "type" to quote(if (chain.horizontal) "hChain" else "vChain")
    entries += "contains" to arr(chain.refs.map { quote(it) })
    chain.style?.let { entries += "style" to quote(chainStyleWord(it)) }
    return chain.id to obj(entries)
}

private fun renderVariable(variable: VariableSpec): Pair<String, String> = when (variable) {
    is VariableSpec.Num -> variable.name to formatFloat(variable.value)
    is VariableSpec.Generator ->
        variable.name to obj(listOf("from" to formatFloat(variable.from), "step" to formatFloat(variable.step)))
    is VariableSpec.IdList -> variable.name to obj(listOf("ids" to arr(variable.ids.map { quote(it) })))
}

fun emit(spec: ConstraintSetSpec): String {
    val topLevel = mutableListOf<Pair<String, String>>()

    if (spec.variables.isNotEmpty()) {
        topLevel += "Variables" to obj(spec.variables.map { renderVariable(it) })
    }

    val (helperGuidelines, typedGuidelines) = spec.guidelines.partition { it.inHelpers }
    if (helperGuidelines.isNotEmpty()) {
        topLevel += "Helpers" to arr(helperGuidelines.map { renderGuidelineInHelpers(it) })
    }

    for (guideline in typedGuidelines) topLevel += renderGuidelineTyped(guideline)
    for (barrier in spec.barriers) topLevel += renderBarrier(barrier)
    for (chain in spec.chains) topLevel += renderChain(chain)
    for (widget in spec.widgets) topLevel += renderWidget(widget)

    spec.generate?.let { generate ->
        // `parseGenerate` stamps `generate.body` onto every id in the `listName` variable; the
        // body's own `WidgetSpec.id` has no destination in the JSON and is not emitted here.
        topLevel += "Generate" to obj(listOf(generate.listName to obj(renderWidgetAttributes(generate.body))))
    }

    return obj(topLevel)
}

/**
 * `parseDesignElementsJSON` only ever looks at the very first top-level key, and only proceeds if
 * it is literally `"Design"` — it `break`s out of the outer loop unconditionally after one
 * iteration. The brief's shape (`{ id: { type: 'button', params… } }`, no wrapper) would therefore
 * be silently ignored; this wraps in `Design` so the document is actually read. See report for
 * detail.
 */
fun emitDesignElements(spec: DesignElementsSpec): String {
    val elements = spec.elements.map { element ->
        val entries = mutableListOf<Pair<String, String>>()
        entries += "type" to quote(element.type)
        for ((name, value) in element.params) {
            entries += name to quote(value)
        }
        element.id to obj(entries)
    }
    return obj(listOf("Design" to obj(elements)))
}
