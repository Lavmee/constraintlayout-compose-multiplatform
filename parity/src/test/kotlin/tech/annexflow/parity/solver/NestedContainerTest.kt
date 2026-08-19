// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.solver

import kotlin.test.Test
import kotlin.test.assertEquals
import androidx.constraintlayout.core.widgets.ConstraintAnchor as OracleAnchor
import androidx.constraintlayout.core.widgets.ConstraintWidget as OracleWidget
import androidx.constraintlayout.core.widgets.ConstraintWidgetContainer as OracleContainer
import tech.annexflow.constraintlayout.core.widgets.ConstraintAnchor as PortAnchor
import tech.annexflow.constraintlayout.core.widgets.ConstraintWidget as PortWidget
import tech.annexflow.constraintlayout.core.widgets.ConstraintWidgetContainer as PortContainer

/**
 * The one scenario [Scenarios] cannot generate: a `ConstraintWidgetContainer` nested inside another.
 *
 * [Scenario] describes a single root and a flat list of widgets. Nesting would need a parent per
 * widget and a scope per barrier, guideline and chain — a change to the model rather than an axis
 * added to it. This test covers the case by hand in the meantime, because it is the case behind the
 * only `@Ignore` left in the ported suite: `androidx.constraintlayout.core.NestedLayout`.
 *
 * That test is disabled upstream too, by commenting out its `@Test`, and neither upstream nor the
 * port says why. The reason is that the feature does not work — and does not work identically on
 * both sides. Wrapping the inner container to its two children leaves it at its original width and
 * throws the children well outside it:
 *
 * ```
 * container.width  100, not the 150 the disabled test wants
 * container.left   450, not 425
 * a.left           -24, not 425
 * b.left            50, not 525
 * ```
 *
 * The wrap is not merely wrong but inert: switching the inner container between `WRAP_CONTENT`
 * and `FIXED` moves nothing at all, on either side.
 *
 * So the `@Ignore` is faithful rather than a port defect, which is what this test pins. Comparing
 * the two implementations rather than asserting those numbers is deliberate: the numbers are
 * upstream's behaviour, not a specification, and if a future oracle refresh fixes nested wrapping
 * this should report that the port now lags — not quietly keep asserting the old breakage.
 */
class NestedContainerTest {
    @Test
    fun thePortAgreesWithTheOracle() {
        assertEquals(oracle(), port())
    }

    private fun oracle(): String {
        val root = OracleContainer(20, 20, 1000, 1000)
        val container = OracleContainer(0, 0, 100, 100)
        root.debugName = "root"
        container.debugName = "container"
        container.connect(OracleAnchor.Type.LEFT, root, OracleAnchor.Type.LEFT)
        container.connect(OracleAnchor.Type.RIGHT, root, OracleAnchor.Type.RIGHT)
        root.add(container)
        root.layout()
        val afterFirstPass = render("container" to container)

        val a = OracleWidget(0, 0, 100, 20)
        val b = OracleWidget(0, 0, 50, 20)
        container.add(a)
        container.add(b)
        container.setHorizontalDimensionBehaviour(OracleWidget.DimensionBehaviour.WRAP_CONTENT)
        a.connect(OracleAnchor.Type.LEFT, container, OracleAnchor.Type.LEFT)
        a.connect(OracleAnchor.Type.RIGHT, b, OracleAnchor.Type.LEFT)
        b.connect(OracleAnchor.Type.RIGHT, container, OracleAnchor.Type.RIGHT)
        root.layout()
        return afterFirstPass + render("container" to container, "a" to a, "b" to b)
    }

    private fun port(): String {
        val root = PortContainer(20, 20, 1000, 1000)
        val container = PortContainer(0, 0, 100, 100)
        root.debugName = "root"
        container.debugName = "container"
        container.connect(PortAnchor.Type.LEFT, root, PortAnchor.Type.LEFT)
        container.connect(PortAnchor.Type.RIGHT, root, PortAnchor.Type.RIGHT)
        root.add(container)
        root.layout()
        val afterFirstPass = render("container" to container)

        val a = PortWidget(0, 0, 100, 20)
        val b = PortWidget(0, 0, 50, 20)
        container.add(a)
        container.add(b)
        container.setHorizontalDimensionBehaviour(PortWidget.DimensionBehaviour.WRAP_CONTENT)
        a.connect(PortAnchor.Type.LEFT, container, PortAnchor.Type.LEFT)
        a.connect(PortAnchor.Type.RIGHT, b, PortAnchor.Type.LEFT)
        b.connect(PortAnchor.Type.RIGHT, container, PortAnchor.Type.RIGHT)
        root.layout()
        return afterFirstPass + render("container" to container, "a" to a, "b" to b)
    }

    /**
     * Both overloads take the widget as `Any` because the two implementations have no common
     * supertype — that is the whole reason this harness exists — and the four accessors resolve on
     * each concrete class.
     */
    private fun render(vararg widgets: Pair<String, Any>): String =
        widgets.joinToString(separator = "", postfix = "--\n") { (name, widget) ->
            val (left, top, width, height) =
                when (widget) {
                    is OracleWidget -> listOf(widget.left, widget.top, widget.width, widget.height)
                    is PortWidget -> listOf(widget.left, widget.top, widget.width, widget.height)
                    else -> error("unexpected widget type ${widget::class}")
                }
            "$name l=$left t=$top w=$width h=$height\n"
        }
}
