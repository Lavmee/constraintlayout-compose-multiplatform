// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.constraintset

/**
 * One widget's laid-out box, named without reference to either implementation's classes.
 *
 * [visibility], [alpha], the rotations, the scales, the translations and the pivots never affect
 * `left`/`top`/`width`/`height` — they land on `WidgetFrame`, a sibling of the box computed by the
 * solver, not an input to it (see `WidgetFrame.kt`). Folded into the same row rather than a new
 * section: it keeps exactly one line per widget, which is what `ConstraintSetDifferentialTest`'s
 * `minimumGeometryRows` floor (a count of newlines in `geometry`) assumes stays true across edits.
 */
data class GeometryRow(
    val id: String,
    val left: Int,
    val top: Int,
    val width: Int,
    val height: Int,
    val visibility: Int,
    val alpha: Float,
    val rotationX: Float,
    val rotationY: Float,
    val rotationZ: Float,
    val scaleX: Float,
    val scaleY: Float,
    val translationX: Float,
    val translationY: Float,
    val translationZ: Float,
    val pivotX: Float,
    val pivotY: Float,
)

/** One custom attribute, already stringified by whichever subject read it. */
data class CustomRow(val widgetId: String, val name: String, val value: String)

/** One design element produced by `parseDesignElementsJSON`. */
data class ElementRow(val id: String, val type: String, val params: Map<String, String>)

fun renderGeometry(rows: List<GeometryRow>): String =
    rows.joinToString(separator = "") { r ->
        "${r.id} l=${r.left} t=${r.top} w=${r.width} h=${r.height} vis=${r.visibility} alpha=${r.alpha} " +
            "rX=${r.rotationX} rY=${r.rotationY} rZ=${r.rotationZ} sX=${r.scaleX} sY=${r.scaleY} " +
            "tX=${r.translationX} tY=${r.translationY} tZ=${r.translationZ} pvX=${r.pivotX} pvY=${r.pivotY}\n"
    }

// Sorted, unlike geometry: custom attributes come out of a HashMap, and the two implementations
// have no reason to iterate one in the same order. Geometry keeps the caller's order because the
// subjects walk the container's children, which is a list on both sides.
fun renderCustom(rows: List<CustomRow>): String =
    rows.sortedWith(compareBy({ it.widgetId }, { it.name }))
        .joinToString(separator = "") { "${it.widgetId}.${it.name}=${it.value}\n" }

fun renderElements(rows: List<ElementRow>): String =
    rows.sortedBy { it.id }.joinToString(separator = "") { row ->
        val params = row.params.entries.sortedBy { it.key }.joinToString(" ") { "${it.key}=${it.value}" }
        "${row.id} type=${row.type}${if (params.isEmpty()) "" else " $params"}\n"
    }

/**
 * Everything observable about parsing one document, normalised so the two implementations become
 * comparable despite living in different packages. The failure side is deliberately the same shape
 * as `LayoutOutcome` ([Leaked], [Crashed]); the success side is not — this type splits into
 * [Populated] and [Elements] for its two entry points, where `LayoutOutcome` has only one
 * (`LaidOut`).
 */
sealed interface ConstraintSetOutcome {
    /** The layout entry point: a document parsed, applied to a container and laid out. */
    data class Populated(val geometry: String, val custom: String) : ConstraintSetOutcome

    /**
     * The `parseDesignElementsJSON` entry point, which yields a list and no geometry at all. A
     * separate case rather than a third field on [Populated]: the two entry points never run
     * together, so a shared shape would leave half of it empty on every scenario.
     */
    data class Elements(val rendered: String) : ConstraintSetOutcome

    /**
     * An exception the parser is documented to raise — `CLParsingException` above all.
     *
     * Compared by portable category, not exception class: the port is multiplatform and cannot
     * raise JVM-specific classes on Native or JS, so class equality would demand something no
     * correct port could deliver.
     */
    data class Leaked(val category: String) : ConstraintSetOutcome

    /**
     * Anything else escaping. Distinct from [Leaked] on purpose: a parsing exception on malformed
     * input is the parser working, while an `IndexOutOfBoundsException` is a defect on whichever
     * side raised it. Collapsing the two would let a port that crashes match an oracle that rejects.
     */
    data class Crashed(val error: String) : ConstraintSetOutcome

    companion object {
        fun categorise(throwable: Throwable): String =
            when (throwable) {
                is IndexOutOfBoundsException -> "IndexOutOfBounds"
                is NullPointerException -> "NullPointer"
                is ArithmeticException -> "Arithmetic"
                is NumberFormatException -> "NumberFormat"
                else -> throwable::class.simpleName ?: "Unknown"
            }
    }
}
