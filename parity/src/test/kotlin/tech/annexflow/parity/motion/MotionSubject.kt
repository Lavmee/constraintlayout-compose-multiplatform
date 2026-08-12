// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.motion

/**
 * One motion run. Implemented twice over the same scenario — once against the vendored upstream
 * Java, once against the shaded port — and the two results are compared verbatim.
 */
internal interface MotionSubject {
    fun run(scenario: MotionScenario): MotionOutcome
}
