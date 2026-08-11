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

    @Test
    fun bothSubjectsMeasureAScenario() {
        val scenario = Scenarios.generate(1)
        for (subject in subjects) {
            val outcome = subject.measure(scenario)
            assertTrue(outcome is LayoutOutcome.LaidOut, "${subject.name} returned $outcome")
        }
    }

    @Test
    fun measuredGeometryNamesEveryParticipant() {
        val scenario = Scenarios.generate(1)
        for (subject in subjects) {
            val geometry = (subject.measure(scenario) as LayoutOutcome.LaidOut).geometry
            assertTrue(geometry.contains("root"), "${subject.name}: $geometry")
            for (widget in scenario.widgets) {
                assertTrue(geometry.contains(widget.name), "${subject.name} missing ${widget.name}")
            }
        }
    }

    @Test
    fun subjectsAgreeWhenMeasuringASingleScenario() {
        val scenario = Scenarios.generate(1)
        assertEquals(OracleSolver.measure(scenario), PortSolver.measure(scenario))
    }

    /**
     * The two entry points answer different questions, so they are never compared against each
     * other — only oracle-to-port within each. This case documents that they genuinely differ, so a
     * future change that silently made `measure` delegate to `layout` would be visible.
     */
    @Test
    fun measuringAndLayingOutAreDistinctPaths() {
        val differing = (1L..200L).count { seed ->
            val scenario = Scenarios.generate(seed)
            OracleSolver.measure(scenario) != OracleSolver.layout(scenario)
        }
        assertTrue(differing > 0, "measure() and layout() produced identical results on 200 seeds")
    }
}
