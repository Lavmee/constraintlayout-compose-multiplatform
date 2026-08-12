// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.motion

import androidx.constraintlayout.core.motion.Motion
import androidx.constraintlayout.core.motion.MotionWidget
import androidx.constraintlayout.core.motion.key.MotionKeyAttributes
import androidx.constraintlayout.core.motion.key.MotionKeyPosition
import androidx.constraintlayout.core.motion.utils.KeyCache

/**
 * Drives the vendored upstream Java. `PortMotion` performs the same sequence against the shaded
 * port; keeping the two in step is the whole contract, so any edit here needs the mirror edit there.
 */
internal class OracleMotion : MotionSubject {
    override fun run(scenario: MotionScenario): MotionOutcome =
        try {
            MotionOutcome.Sampled(sample(scenario).render())
        } catch (throwable: Throwable) {
            MotionOutcome.Leaked(MotionOutcome.categorise(throwable))
        }

    private fun sample(scenario: MotionScenario): MotionSample {
        val start = widget(scenario.start, scenario.startAttributes, easing = scenario.easing)
        val end = widget(scenario.end, scenario.endAttributes, easing = null)

        val motion = Motion(start)
        motion.setStart(start)
        motion.setEnd(end)
        scenario.keys.forEach { motion.addKey(key(it)) }
        motion.setPathMotionArc(scenario.pathMotionArc)
        motion.setup(scenario.parentWidth, scenario.parentHeight, scenario.duration, 0L)

        val cache = KeyCache()
        val out = MotionWidget()
        val centre = FloatArray(2)
        val velocity = FloatArray(2)
        val positions =
            (0 until scenario.samples).map { index ->
                val p = index.toFloat() / (scenario.samples - 1)
                motion.interpolate(out, p, 0L, cache)
                motion.getCenter(p.toDouble(), centre, velocity)
                PositionSample(
                    p = p,
                    left = out.left, top = out.top, right = out.right, bottom = out.bottom,
                    rotationZ = out.rotationZ, scaleX = out.scaleX, scaleY = out.scaleY,
                    alpha = out.alpha, translationX = out.translationX, translationY = out.translationY,
                    centerX = centre[0], centerY = centre[1],
                    velocityX = velocity[0], velocityY = velocity[1],
                )
            }

        val path = FloatArray(scenario.samples * 2)
        motion.buildPath(path, scenario.samples)

        // Sized from the upper bound on MotionPaths: start, end, and one per position key. Both
        // arrays are pre-filled so unwritten slots read identically on both sides rather than
        // depending on whatever the allocator left behind.
        val pathCount = scenario.keys.count { it is KeySpec.PositionKey } + 2
        val keyFrames = FloatArray(pathCount * 2) { Float.NaN }
        val modes = IntArray(pathCount) { Int.MIN_VALUE }
        val positionsOut = IntArray(pathCount) { Int.MIN_VALUE }
        val count = motion.buildKeyFrames(keyFrames, modes, positionsOut)

        return MotionSample(
            positions = positions,
            path = path.toList(),
            keyFrames = keyFrames.toList(),
            keyFrameModes = modes.toList(),
            keyFramePositions = positionsOut.toList(),
            keyFrameCount = count,
        )
    }

    private fun widget(bounds: Bounds, attributes: Attributes, easing: String?): MotionWidget {
        val widget = MotionWidget()
        widget.setBounds(bounds.left, bounds.top, bounds.right, bounds.bottom)
        attribute(widget, TypeIds.ATTR_ROTATION_Z, attributes.rotationZ)
        attribute(widget, TypeIds.ATTR_SCALE_X, attributes.scaleX)
        attribute(widget, TypeIds.ATTR_SCALE_Y, attributes.scaleY)
        attribute(widget, TypeIds.ATTR_ALPHA, attributes.alpha)
        attribute(widget, TypeIds.ATTR_TRANSLATION_X, attributes.translationX)
        attribute(widget, TypeIds.ATTR_TRANSLATION_Y, attributes.translationY)
        if (easing != null) widget.setValueMotion(TypeIds.MOTION_EASING, easing)
        return widget
    }

    /** `NaN` means "not set" in the originals, so an unset attribute is one never applied at all. */
    private fun attribute(widget: MotionWidget, id: Int, value: Float) {
        if (!value.isNaN()) widget.setValueAttributes(id, value)
    }

    private fun key(spec: KeySpec) =
        when (spec) {
            is KeySpec.PositionKey ->
                MotionKeyPosition().apply {
                    mFramePosition = spec.framePosition
                    if (!spec.percentX.isNaN()) setValue(TypeIds.POSITION_PERCENT_X, spec.percentX)
                    if (!spec.percentY.isNaN()) setValue(TypeIds.POSITION_PERCENT_Y, spec.percentY)
                    if (!spec.percentWidth.isNaN()) setValue(TypeIds.POSITION_PERCENT_WIDTH, spec.percentWidth)
                    if (!spec.percentHeight.isNaN()) setValue(TypeIds.POSITION_PERCENT_HEIGHT, spec.percentHeight)
                    setValue(TypeIds.POSITION_CURVE_FIT, spec.curveFit)
                    mPathMotionArc = spec.pathMotionArc
                    spec.transitionEasing?.let { setValue(TypeIds.POSITION_TRANSITION_EASING, it) }
                }

            is KeySpec.AttributesKey ->
                MotionKeyAttributes().apply {
                    mFramePosition = spec.framePosition
                    if (!spec.rotationZ.isNaN()) setValue(TypeIds.ATTR_ROTATION_Z, spec.rotationZ)
                    if (!spec.scaleX.isNaN()) setValue(TypeIds.ATTR_SCALE_X, spec.scaleX)
                    if (!spec.scaleY.isNaN()) setValue(TypeIds.ATTR_SCALE_Y, spec.scaleY)
                    if (!spec.alpha.isNaN()) setValue(TypeIds.ATTR_ALPHA, spec.alpha)
                    setValue(TypeIds.ATTR_CURVE_FIT, spec.curveFit)
                }
        }
}
