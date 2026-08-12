// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.motion

/**
 * Numeric ids from `TypedValues`, duplicated here by value on purpose. The scenario has to describe
 * its input without naming a type from either side: the moment it imports the oracle's constants,
 * the two subjects stop being fed the same thing by construction. #339 had to repair that leak.
 */
internal object TypeIds {
    const val ATTR_CURVE_FIT = 301
    const val ATTR_ALPHA = 303
    const val ATTR_TRANSLATION_X = 304
    const val ATTR_TRANSLATION_Y = 305
    const val ATTR_ROTATION_Z = 310
    const val ATTR_SCALE_X = 311
    const val ATTR_SCALE_Y = 312

    const val POSITION_TRANSITION_EASING = 501
    const val POSITION_PERCENT_WIDTH = 503
    const val POSITION_PERCENT_HEIGHT = 504
    const val POSITION_PERCENT_X = 506
    const val POSITION_PERCENT_Y = 507
    const val POSITION_CURVE_FIT = 508
    const val POSITION_PATH_MOTION_ARC = 509

    const val MOTION_EASING = 603
}

internal data class Bounds(val left: Int, val top: Int, val right: Int, val bottom: Int)

/** `Float.NaN` means "not set", exactly as the originals use it. */
internal data class Attributes(
    val rotationZ: Float,
    val scaleX: Float,
    val scaleY: Float,
    val alpha: Float,
    val translationX: Float,
    val translationY: Float,
)

internal sealed interface KeySpec {
    val framePosition: Int

    data class PositionKey(
        override val framePosition: Int,
        val percentX: Float,
        val percentY: Float,
        val percentWidth: Float,
        val percentHeight: Float,
        val curveFit: Int,
        val transitionEasing: String?,
        val pathMotionArc: Int,
    ) : KeySpec

    data class AttributesKey(
        override val framePosition: Int,
        val rotationZ: Float,
        val scaleX: Float,
        val scaleY: Float,
        val alpha: Float,
        val curveFit: Int,
    ) : KeySpec
}

internal data class MotionScenario(
    val seed: Long,
    val start: Bounds,
    val end: Bounds,
    val startAttributes: Attributes,
    val endAttributes: Attributes,
    val keys: List<KeySpec>,
    val pathMotionArc: Int,
    val easing: String?,
    val parentWidth: Int,
    val parentHeight: Int,
    val duration: Float,
    val samples: Int,
)
