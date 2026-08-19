// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.constraintset

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SubjectTest {
    private val subjects = listOf(OracleConstraintSet, PortConstraintSet)

    @Test
    fun bothSubjectsLayOutTheSameDocumentIdentically() {
        val outcomes = subjects.map { it.parse(baseSpec()) }
        assertEquals(
            outcomes[0],
            outcomes[1],
            "${subjects[0].name} and ${subjects[1].name} disagree on the same document",
        )
        assertTrue(outcomes[0] is ConstraintSetOutcome.Populated, "got ${outcomes[0]}")
    }

    @Test
    fun theCustomPropertyIsVisibleInTheOutcome() {
        val populated = OracleConstraintSet.parse(baseSpec()) as ConstraintSetOutcome.Populated
        assertTrue(populated.custom.contains("id1.shade="), populated.custom)
    }

    @Test
    fun bothSubjectsAgreeOnDesignElements() {
        val spec = DesignElementsSpec(1, listOf(DesignElementSpec("e1", "button", mapOf("text" to "hi"))))
        assertEquals(
            OracleConstraintSet.designElements(spec),
            PortConstraintSet.designElements(spec),
        )
    }
}
