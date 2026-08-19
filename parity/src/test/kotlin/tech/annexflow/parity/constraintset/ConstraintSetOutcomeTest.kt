// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.constraintset

import kotlin.test.Test
import kotlin.test.assertEquals

class ConstraintSetOutcomeTest {
    @Test
    fun geometryRendersOneRowPerWidgetInGivenOrder() {
        val rendered = renderGeometry(
            listOf(GeometryRow("b", 10, 20, 30, 40), GeometryRow("a", 0, 0, 5, 5)),
        )
        assertEquals("b l=10 t=20 w=30 h=40\na l=0 t=0 w=5 h=5\n", rendered)
    }

    @Test
    fun customRowsAreSortedSoIterationOrderCannotLeakIn() {
        val rendered = renderCustom(
            listOf(
                CustomRow("w", "zeta", "1.0"),
                CustomRow("w", "alpha", "2.0"),
                CustomRow("a", "beta", "3.0"),
            ),
        )
        assertEquals("a.beta=3.0\nw.alpha=2.0\nw.zeta=1.0\n", rendered)
    }

    @Test
    fun elementParamsAreSorted() {
        val rendered = renderElements(
            listOf(ElementRow("e", "button", mapOf("b" to "2", "a" to "1"))),
        )
        assertEquals("e type=button a=1 b=2\n", rendered)
    }

    @Test
    fun categoriseMapsToPortableNames() {
        assertEquals("IndexOutOfBounds", ConstraintSetOutcome.categorise(IndexOutOfBoundsException()))
        assertEquals("NullPointer", ConstraintSetOutcome.categorise(NullPointerException()))
    }
}
