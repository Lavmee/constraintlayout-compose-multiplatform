// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.constraintset

import kotlin.random.Random

/**
 * Generates [ConstraintSetSpec] and [DesignElementsSpec] documents from a seed.
 *
 * Generation is a pure function of the seed, so a divergence reported by CI reproduces exactly.
 * Widgets are generated first, `w0` through `wn`; every element that comes after — guidelines,
 * barriers, chains — is generated in a later pass, so a widget anchor can only ever target `parent`
 * or a lower-indexed widget. That keeps the constraint graph acyclic, the same property
 * `solver/Scenarios.kt` relies on for the same reason: a cycle would leave the parser applying
 * constraints the two implementations have no reason to resolve the same way, and the harness would
 * be measuring that instead of the port.
 *
 * A widget that draws a circular constraint gets no ordinary anchors: `ConstraintReference` doesn't
 * refuse the combination, but circular positioning and edge-to-edge constraints answer the same
 * question two different ways, and there is nothing this harness wants to learn from watching them
 * fight.
 */
object Scenarios {
    private const val MIN_WIDGETS = 2
    private const val MAX_WIDGETS = 6

    /** `parseDimensionMode` only recognises a ratio string by finding a colon in it. */
    private val RATIOS = listOf("16:9", "4:3", "1:1", "3:2", "W,16:9", "H,2:3")

    private val HORIZONTAL_ANCHORS = listOf(Anchor.START, Anchor.END, Anchor.LEFT, Anchor.RIGHT)
    private val VERTICAL_ANCHORS = listOf(Anchor.TOP, Anchor.BOTTOM, Anchor.BASELINE)

    private val DESIGN_TYPES = listOf("button", "text", "image", "spinner")

    fun generate(seed: Long): ConstraintSetSpec {
        val random = Random(seed)
        val count = random.nextInt(MIN_WIDGETS, MAX_WIDGETS + 1)
        val widgets = (0 until count).map { index -> widget(random, index) }

        val guidelines = (0 until random.nextInt(0, 3)).map { index ->
            GuidelineSpec(
                id = "g$index",
                horizontal = random.nextBoolean(),
                position = guidelinePosition(random),
                // Half through `Helpers` (`parseHelpers` -> `parseGuideline`), half as a typed
                // top-level element (`populateState` -> `parseGuidelineParams`) — different code.
                inHelpers = random.nextBoolean(),
            )
        }

        val barriers = if (random.nextInt(2) == 0) {
            val refCount = random.nextInt(1, minOf(3, widgets.size) + 1)
            listOf(
                BarrierSpec(
                    id = "b0",
                    direction = BarrierDirection.entries[random.nextInt(BarrierDirection.entries.size)],
                    margin = if (random.nextInt(2) == 0) random.nextInt(0, 30) else null,
                    refs = widgets.map { it.id }.shuffled(random).take(refCount),
                ),
            )
        } else {
            emptyList()
        }

        // A contiguous run, not an arbitrary subset: a chain is the widgets between two points on
        // one axis, and a scattered `refs` list would not describe that shape.
        val chains = if (widgets.size >= 2 && random.nextInt(2) == 0) {
            val length = random.nextInt(2, minOf(3, widgets.size) + 1)
            val start = random.nextInt(0, widgets.size - length + 1)
            listOf(
                ChainSpec(
                    id = "c0",
                    horizontal = random.nextBoolean(),
                    refs = widgets.subList(start, start + length).map { it.id },
                    style = if (random.nextInt(2) == 0) {
                        ChainStyle.entries[random.nextInt(ChainStyle.entries.size)]
                    } else {
                        null
                    },
                ),
            )
        } else {
            emptyList()
        }

        val variables = if (random.nextInt(3) == 0) {
            (0 until random.nextInt(1, 4)).map { index -> variable(random, index) }
        } else {
            emptyList()
        }

        // `parseGenerate` takes ids from the named `IdList` variable, not from the body's own `id`
        // — a `Generate` block only does anything when one exists.
        val idLists = variables.filterIsInstance<VariableSpec.IdList>()
        val generate = if (idLists.isNotEmpty() && random.nextInt(2) == 0) {
            GenerateSpec(listName = idLists[random.nextInt(idLists.size)].name, body = generateBody(random))
        } else {
            null
        }

        return ConstraintSetSpec(
            seed = seed,
            rootWidth = random.nextInt(400, 1601),
            rootHeight = random.nextInt(400, 1601),
            isRtl = random.nextInt(4) == 0,
            widgets = widgets,
            chains = chains,
            guidelines = guidelines,
            barriers = barriers,
            variables = variables,
            generate = generate,
        )
    }

    fun generateDesignElements(seed: Long): DesignElementsSpec {
        val random = Random(seed)
        val elements = (0 until random.nextInt(1, 4)).map { index ->
            DesignElementSpec(
                id = "d$index",
                type = DESIGN_TYPES[random.nextInt(DESIGN_TYPES.size)],
                params = (0 until random.nextInt(0, 3)).associate { p -> "param$p" to "value$p" },
            )
        }
        return DesignElementsSpec(seed, elements)
    }

    private fun widget(random: Random, index: Int): WidgetSpec {
        // Mutually exclusive with ordinary anchors — see the class kdoc.
        val hasCircular = index > 0 && random.nextInt(6) == 0
        val anchors = if (hasCircular) {
            emptyList()
        } else {
            Anchor.entries.shuffled(random).take(random.nextInt(0, 4)).map { from -> anchor(random, index, from) }
        }
        val circular = if (hasCircular) {
            CircularSpec(
                target = "w${random.nextInt(index)}",
                angle = random.nextInt(0, 360).toFloat(),
                distance = random.nextInt(10, 300),
            )
        } else {
            null
        }

        val hasBias = random.nextInt(3) == 0
        val hasWeight = random.nextInt(3) == 0
        val hasVisibility = random.nextInt(3) == 0
        val hasAlpha = random.nextInt(3) == 0
        val hasRotation = random.nextInt(3) == 0
        val hasScale = random.nextInt(3) == 0
        val hasTranslation = random.nextInt(3) == 0
        val hasPivot = random.nextInt(3) == 0

        return WidgetSpec(
            id = "w$index",
            width = dimension(random),
            height = dimension(random),
            anchors = anchors,
            circular = circular,
            centerHorizontally = if (random.nextInt(8) == 0) anchorTarget(random, index) else null,
            centerVertically = if (random.nextInt(8) == 0) anchorTarget(random, index) else null,
            center = if (random.nextInt(8) == 0) anchorTarget(random, index) else null,
            hBias = if (hasBias) random.nextFloat() else null,
            vBias = if (hasBias) random.nextFloat() else null,
            hRtlBias = if (hasBias) random.nextFloat() else null,
            hWeight = if (hasWeight) 0.1f + random.nextFloat() * 3f else null,
            vWeight = if (hasWeight) 0.1f + random.nextFloat() * 3f else null,
            visibility = if (hasVisibility) Visibility.entries[random.nextInt(Visibility.entries.size)] else null,
            alpha = if (hasAlpha) random.nextFloat() else null,
            rotationX = if (hasRotation) random.nextFloat() * 360f else null,
            rotationY = if (hasRotation) random.nextFloat() * 360f else null,
            rotationZ = if (hasRotation) random.nextFloat() * 360f else null,
            scaleX = if (hasScale) 0.5f + random.nextFloat() * 1.5f else null,
            scaleY = if (hasScale) 0.5f + random.nextFloat() * 1.5f else null,
            translationX = if (hasTranslation) random.nextFloat() * 100f - 50f else null,
            translationY = if (hasTranslation) random.nextFloat() * 100f - 50f else null,
            translationZ = if (hasTranslation) random.nextFloat() * 100f - 50f else null,
            pivotX = if (hasPivot) random.nextFloat() else null,
            pivotY = if (hasPivot) random.nextFloat() else null,
            custom = (0 until random.nextInt(0, 3)).associate { i -> "custom$i" to customValue(random) },
        )
    }

    /**
     * The body a `Generate` block stamps across every id in its `IdList` variable. Those ids are
     * fresh names (see [idListVariable]), never widget ids already in the document, so anchoring
     * the body at `parent` can never make it target itself.
     */
    private fun generateBody(random: Random): WidgetSpec = WidgetSpec(
        id = "generated",
        width = dimension(random),
        height = dimension(random),
        anchors = listOf(
            AnchorSpec(Anchor.START, AnchorTarget.Parent, Anchor.START, anchorMargin(random)),
            AnchorSpec(Anchor.TOP, AnchorTarget.Parent, Anchor.TOP, anchorMargin(random)),
        ),
        circular = null,
        centerHorizontally = null,
        centerVertically = null,
        center = null,
        hBias = null,
        vBias = null,
        hRtlBias = null,
        hWeight = null,
        vWeight = null,
        visibility = if (random.nextInt(3) == 0) Visibility.entries[random.nextInt(Visibility.entries.size)] else null,
        alpha = null,
        rotationX = null,
        rotationY = null,
        rotationZ = null,
        scaleX = null,
        scaleY = null,
        translationX = null,
        translationY = null,
        translationZ = null,
        pivotX = null,
        pivotY = null,
        custom = emptyMap(),
    )

    /**
     * `from`'s "to" is drawn from the same category as `from` itself: `parseConstraint`'s `when`
     * on the constraint name only recognises the matching anchors as a value (e.g. `"top"` only
     * branches on `"top"`/`"bottom"`/`"baseline"`) — a cross-category pairing falls through and
     * applies nothing, which would silently under-constrain the widget.
     */
    private fun anchor(random: Random, index: Int, from: Anchor): AnchorSpec {
        val category = if (from in HORIZONTAL_ANCHORS) HORIZONTAL_ANCHORS else VERTICAL_ANCHORS
        return AnchorSpec(
            from = from,
            target = anchorTarget(random, index),
            to = category[random.nextInt(category.size)],
            margin = anchorMargin(random),
        )
    }

    private fun anchorTarget(random: Random, index: Int): AnchorTarget =
        if (index == 0 || random.nextInt(2) == 0) AnchorTarget.Parent else AnchorTarget.Widget("w${random.nextInt(index)}")

    private fun anchorMargin(random: Random): AnchorMargin {
        val dp = random.nextInt(0, 33)
        return if (random.nextInt(4) == 0) {
            AnchorMargin.MarginAndGone(dp, random.nextInt(0, 33))
        } else {
            AnchorMargin.Margin(dp)
        }
    }

    private fun dimension(random: Random): DimensionSpec = when (random.nextInt(5)) {
        0 -> DimensionSpec.Fixed(random.nextInt(20, 301))
        1 -> DimensionSpec.Mode(DimensionMode.entries[random.nextInt(DimensionMode.entries.size)])
        2 -> DimensionSpec.Percent(random.nextInt(0, 101).toFloat())
        3 -> DimensionSpec.Ratio(RATIOS[random.nextInt(RATIOS.size)])
        else -> DimensionSpec.Bounded(
            value = if (random.nextInt(5) != 0) DimensionMode.entries[random.nextInt(DimensionMode.entries.size)] else null,
            min = if (random.nextInt(2) == 0) bound(random) else null,
            max = if (random.nextInt(2) == 0) bound(random) else null,
        )
    }

    private fun bound(random: Random): Bound =
        if (random.nextInt(4) == 0) Bound.Wrap else Bound.Pixels(random.nextInt(10, 301))

    private fun guidelinePosition(random: Random): GuidelinePosition = when (random.nextInt(3)) {
        0 -> GuidelinePosition.FromStart(random.nextInt(0, 500))
        1 -> GuidelinePosition.FromEnd(random.nextInt(0, 500))
        else -> GuidelinePosition.Percent(random.nextInt(0, 101) / 100f)
    }

    /**
     * `'notacolour'` doesn't start with `#`, so `parseColorString` returns -1 and
     * `parseCustomProperties` silently drops the property — a malformed value the parser was
     * built to reject. `'#zz'` is a different animal: it starts with `#` and is short enough to
     * skip the "prepend FF" branch, so `parseColorString` reaches `"zz".toLong(16)`, which isn't
     * valid hex and throws `NumberFormatException` straight out of `applyAttribute` — not a parse
     * rejection but an uncaught crash. Both are deliberately in the pool (see the brief), but
     * `'#zz'` is weighted low: drawing it on every other widget turned roughly half of all
     * documents into `Crashed` outcomes rather than `Populated` ones, which is a poor way to spend
     * the seed space this generator is measured against.
     */
    private fun customColor(random: Random): String = when (random.nextInt(40)) {
        0 -> "#zz"
        1 -> "notacolour"
        in 2..20 -> "#ff0000"
        else -> "#80ff0000"
    }

    private fun customValue(random: Random): CustomValue =
        if (random.nextBoolean()) CustomValue.Num(random.nextFloat() * 100f) else CustomValue.Color(customColor(random))

    /**
     * A plain top-level `Num` variable is read by `parseVariables`'s `CLNumber` branch, which
     * always calls `element.getInt()` and stores an `Int` — never `getFloat()`. `CLNumber.getInt`
     * falls back to `content().toInt()`, so a fractional value like `85.10631` throws
     * `NumberFormatException`. A `Generator`'s `from`/`step` don't share the bug — those are read
     * through `LayoutVariables.get`, which calls `getFloat()` — so only the plain `Num` case is
     * affected.
     *
     * Mostly whole numbers, same treatment as `'#zz'` in [customColor]: a rare fractional draw
     * (1 in 15) keeps `getInt()`'s `content().toInt()` fallback reachable and its crash observed
     * on both sides of the harness, rather than generating around it and letting it go untested.
     * That matters more than it looks — `CLNumber` lives in the live Kotlin `compose/src`, not
     * the frozen Java oracle, so a future edit that symmetrises `getInt()` with `getFloat()` (or
     * swaps in `toIntOrNull()`) would silently diverge from the oracle if nothing ever exercised
     * this branch to catch it. Kept rare, the same way `'#zz'` is kept rare, so the crash stays a
     * small, deliberate minority of documents rather than eating the corpus.
     */
    private fun variable(random: Random, index: Int): VariableSpec = when (random.nextInt(3)) {
        0 -> VariableSpec.Num("v$index", numValue(random))
        1 -> VariableSpec.Generator("v$index", random.nextFloat() * 10f, 1f + random.nextFloat() * 5f)
        else -> idListVariable(random, "v$index")
    }

    private fun numValue(random: Random): Float =
        if (random.nextInt(15) == 0) random.nextFloat() * 100f else random.nextInt(0, 101).toFloat()

    private fun idListVariable(random: Random, name: String): VariableSpec.IdList =
        VariableSpec.IdList(name, (0 until random.nextInt(1, 4)).map { i -> "gen$i" })
}
