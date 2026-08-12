// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.motion

/** One sweep position: what `interpolate` produced there, plus the centre and velocity. */
internal data class PositionSample(
    val p: Float,
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
    val rotationZ: Float,
    val scaleX: Float,
    val scaleY: Float,
    val alpha: Float,
    val translationX: Float,
    val translationY: Float,
    val centerX: Float,
    val centerY: Float,
    val velocityX: Float,
    val velocityY: Float,
)

/**
 * Raw values collected from one side, before rendering. Subjects fill this and nothing else.
 *
 * Rendering lives here rather than in the subjects for the same reason the scenario names no
 * side's types: if each subject formatted its own string, a difference in how one of them formats
 * would be indistinguishable from a difference in what `Motion` computed, and the harness would
 * report a defect that does not exist.
 */
internal data class MotionSample(
    val positions: List<PositionSample>,
    val path: List<Float>,
    val keyFrames: List<Float>,
    val keyFrameModes: List<Int>,
    val keyFramePositions: List<Int>,
    val keyFrameCount: Int,
) {
    /**
     * `Float.toString` is the whole formatting strategy, deliberately. Both sides run on the JVM,
     * where it is the same `java.lang.Float.toString`, so matching computations produce identical
     * text down to the last bit. `NaN` reads as "NaN" on both sides — agreement, which is what two
     * implementations degenerating the same way should report — and `-0.0` stays distinct from
     * "0.0", which a tolerance would have erased.
     */
    fun render(): String = buildString {
        positions.forEach { s ->
            append("p=").append(s.p)
            append(" bounds=").append(s.left).append(',').append(s.top).append(',')
                .append(s.right).append(',').append(s.bottom)
            append(" rotationZ=").append(s.rotationZ)
            append(" scaleX=").append(s.scaleX)
            append(" scaleY=").append(s.scaleY)
            append(" alpha=").append(s.alpha)
            append(" translationX=").append(s.translationX)
            append(" translationY=").append(s.translationY)
            append(" center=").append(s.centerX).append(',').append(s.centerY)
            append(" velocity=").append(s.velocityX).append(',').append(s.velocityY)
            append('\n')
        }
        append("path=").append(path.joinToString(",")).append('\n')
        append("keyFrames=").append(keyFrames.joinToString(",")).append('\n')
        append("keyFrameModes=").append(keyFrameModes.joinToString(",")).append('\n')
        append("keyFramePositions=").append(keyFramePositions.joinToString(",")).append('\n')
        append("count=").append(keyFrameCount)
    }
}
