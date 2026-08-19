// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.constraintset

import kotlin.test.Test
import kotlin.test.assertEquals

class ConstraintSetOutcomeTest {
    private fun row(id: String, left: Int, top: Int, width: Int, height: Int) = GeometryRow(
        id, left, top, width, height,
        visibility = 0, alpha = Float.NaN,
        rotationX = Float.NaN, rotationY = Float.NaN, rotationZ = Float.NaN,
        scaleX = Float.NaN, scaleY = Float.NaN,
        translationX = Float.NaN, translationY = Float.NaN, translationZ = Float.NaN,
        pivotX = Float.NaN, pivotY = Float.NaN,
    )

    @Test
    fun geometryRendersOneRowPerWidgetInGivenOrder() {
        val rendered = renderGeometry(listOf(row("b", 10, 20, 30, 40), row("a", 0, 0, 5, 5)))
        assertEquals(
            "b l=10 t=20 w=30 h=40 vis=0 alpha=NaN rX=NaN rY=NaN rZ=NaN sX=NaN sY=NaN tX=NaN tY=NaN tZ=NaN " +
                "pvX=NaN pvY=NaN\n" +
                "a l=0 t=0 w=5 h=5 vis=0 alpha=NaN rX=NaN rY=NaN rZ=NaN sX=NaN sY=NaN tX=NaN tY=NaN tZ=NaN " +
                "pvX=NaN pvY=NaN\n",
            rendered,
        )
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
