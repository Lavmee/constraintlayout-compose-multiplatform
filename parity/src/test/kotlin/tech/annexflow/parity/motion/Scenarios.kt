// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.motion

import kotlin.random.Random

/**
 * Builds one scenario per seed. Seeded rather than curated because a motion scenario is a
 * combination of parameters, not a document: the parser harness wants a corpus, this one wants
 * enumeration. The seed travels in the divergence report, so a red run reproduces from one line.
 */
internal object Scenarios {
    private val EASINGS = listOf(null, "standard", "accelerate", "decelerate", "linear", "cubic(0.4, 0.0, 0.2, 1)")

    /** UNSET plus every mode `ArcCurveFit` understands. */
    private val ARCS = listOf(-1, 0, 1, 2, 3, 4, 5)

    private const val SAMPLES = 12

    fun generate(seed: Long): MotionScenario {
        val random = Random(seed)
        val parentWidth = random.nextInt(200, 1200)
        val parentHeight = random.nextInt(200, 1200)
        return MotionScenario(
            seed = seed,
            start = bounds(random, parentWidth, parentHeight),
            end = bounds(random, parentWidth, parentHeight),
            startAttributes = attributes(random),
            endAttributes = attributes(random),
            keys = keys(random),
            // Indexed by seed rather than drawn at random, so every mode is guaranteed to appear
            // across a run instead of merely being likely to.
            pathMotionArc = ARCS[(seed % ARCS.size).toInt()],
            easing = EASINGS[random.nextInt(EASINGS.size)],
            parentWidth = parentWidth,
            parentHeight = parentHeight,
            duration = random.nextInt(100, 2000).toFloat(),
            samples = SAMPLES,
        )
    }

    private fun bounds(random: Random, parentWidth: Int, parentHeight: Int): Bounds {
        val left = random.nextInt(0, parentWidth - 40)
        val top = random.nextInt(0, parentHeight - 40)
        return Bounds(left, top, left + random.nextInt(10, 200), top + random.nextInt(10, 200))
    }

    private fun attributes(random: Random): Attributes =
        Attributes(
            rotationZ = maybe(random) { random.nextInt(-180, 180).toFloat() },
            scaleX = maybe(random) { random.nextInt(1, 30) / 10f },
            scaleY = maybe(random) { random.nextInt(1, 30) / 10f },
            alpha = maybe(random) { random.nextInt(0, 10) / 10f },
            translationX = maybe(random) { random.nextInt(-100, 100).toFloat() },
            translationY = maybe(random) { random.nextInt(-100, 100).toFloat() },
        )

    /** `NaN` is how the originals spell "not set", so an unset attribute is one that stays `NaN`. */
    private fun maybe(random: Random, value: () -> Float): Float =
        if (random.nextInt(3) == 0) Float.NaN else value()

    private fun keys(random: Random): List<KeySpec> {
        val positions = (1..random.nextInt(0, 4)).map { random.nextInt(1, 100) }.distinct().sorted()
        return positions.map { framePosition ->
            if (random.nextBoolean()) {
                KeySpec.PositionKey(
                    framePosition = framePosition,
                    percentX = maybe(random) { random.nextInt(0, 100) / 100f },
                    percentY = maybe(random) { random.nextInt(0, 100) / 100f },
                    percentWidth = maybe(random) { random.nextInt(0, 100) / 100f },
                    percentHeight = maybe(random) { random.nextInt(0, 100) / 100f },
                    curveFit = random.nextInt(-1, 2),
                    transitionEasing = EASINGS[random.nextInt(EASINGS.size)],
                    pathMotionArc = ARCS[random.nextInt(ARCS.size)],
                )
            } else {
                KeySpec.AttributesKey(
                    framePosition = framePosition,
                    rotationZ = maybe(random) { random.nextInt(-180, 180).toFloat() },
                    scaleX = maybe(random) { random.nextInt(1, 30) / 10f },
                    scaleY = maybe(random) { random.nextInt(1, 30) / 10f },
                    alpha = maybe(random) { random.nextInt(0, 10) / 10f },
                    curveFit = random.nextInt(-1, 2),
                )
            }
        }
    }
}
