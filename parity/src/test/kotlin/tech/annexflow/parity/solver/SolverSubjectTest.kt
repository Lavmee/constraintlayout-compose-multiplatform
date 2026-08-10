// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.solver

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SolverSubjectTest {
    private val subjects = listOf(OracleSolver, PortSolver)

    @Test
    fun bothSubjectsLayOutAScenario() {
        val scenario = Scenarios.generate(1)
        for (subject in subjects) {
            val outcome = subject.layout(scenario)
            assertTrue(outcome is LayoutOutcome.LaidOut, "${subject.name} returned $outcome")
        }
    }

    @Test
    fun geometryNamesEveryWidgetAndTheRoot() {
        val scenario = Scenarios.generate(1)
        for (subject in subjects) {
            val geometry = (subject.layout(scenario) as LayoutOutcome.LaidOut).geometry
            assertTrue(geometry.contains("root"), "${subject.name}: $geometry")
            for (widget in scenario.widgets) {
                assertTrue(geometry.contains(widget.name), "${subject.name} missing ${widget.name}")
            }
        }
    }

    @Test
    fun subjectsAgreeOnASingleScenario() {
        val scenario = Scenarios.generate(1)
        assertEquals(OracleSolver.layout(scenario), PortSolver.layout(scenario))
    }
}
