// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.motion

import kotlin.test.Test
import kotlin.test.assertTrue

class MotionSubjectTest {
    private val trivial =
        MotionScenario(
            seed = 0L,
            start = Bounds(0, 0, 100, 100),
            end = Bounds(200, 200, 300, 300),
            startAttributes = Attributes(Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN),
            endAttributes = Attributes(Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN),
            keys = emptyList(),
            pathMotionArc = -1,
            easing = null,
            parentWidth = 1000,
            parentHeight = 1000,
            duration = 1000f,
            samples = 5,
        )

    @Test
    fun theOracleRunsTheTrivialScenario() {
        val outcome = OracleMotion().run(trivial)
        assertTrue(outcome is MotionOutcome.Sampled, "oracle returned $outcome")
    }

    @Test
    fun thePortRunsTheTrivialScenario() {
        val outcome = PortMotion().run(trivial)
        assertTrue(outcome is MotionOutcome.Sampled, "port returned $outcome")
    }

    @Test
    fun theSnapshotIsNotEmpty() {
        val outcome = OracleMotion().run(trivial) as MotionOutcome.Sampled
        assertTrue(outcome.snapshot.lines().size >= 5, "snapshot too short:\n${outcome.snapshot}")
    }

    @Test
    fun aScenarioWithKeysAlsoRuns() {
        val withKeys =
            trivial.copy(
                keys = listOf(
                    KeySpec.PositionKey(50, 0.5f, 0.5f, Float.NaN, Float.NaN, -1, null, -1),
                    KeySpec.AttributesKey(75, 45f, 2f, 2f, 0.5f, -1, null),
                ),
                pathMotionArc = 1,
            )
        assertTrue(OracleMotion().run(withKeys) is MotionOutcome.Sampled)
        assertTrue(PortMotion().run(withKeys) is MotionOutcome.Sampled)
    }
}
