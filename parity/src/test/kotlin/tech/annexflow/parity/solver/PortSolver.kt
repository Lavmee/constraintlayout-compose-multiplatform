// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.solver

import tech.annexflow.constraintlayout.core.widgets.Barrier
import tech.annexflow.constraintlayout.core.widgets.ConstraintAnchor
import tech.annexflow.constraintlayout.core.widgets.ConstraintWidget
import tech.annexflow.constraintlayout.core.widgets.ConstraintWidgetContainer
import tech.annexflow.constraintlayout.core.widgets.Guideline

/** The ported solver, taken from the shaded flavour so its packages do not collide with the oracle's. */
object PortSolver : SolverSubject {
    override val name: String = "port"

    override fun layout(scenario: Scenario): LayoutOutcome =
        try {
            val root = ConstraintWidgetContainer(0, 0, scenario.rootWidth, scenario.rootHeight)
            root.debugName = "root"
            root.setHorizontalDimensionBehaviour(behaviour(scenario.rootHorizontal))
            root.setVerticalDimensionBehaviour(behaviour(scenario.rootVertical))
            if (scenario.rootMinWidth > 0) root.setMinWidth(scenario.rootMinWidth)
            if (scenario.rootMinHeight > 0) root.setMinHeight(scenario.rootMinHeight)

            val guidelines = scenario.guidelines.map { spec ->
                Guideline().apply {
                    debugName = spec.name
                    setOrientation(if (spec.vertical) Guideline.VERTICAL else Guideline.HORIZONTAL)
                    when (val position = spec.position) {
                        is GuidelinePosition.Begin -> setGuideBegin(position.value)
                        is GuidelinePosition.End -> setGuideEnd(position.value)
                        is GuidelinePosition.Percent -> setGuidePercent(position.value)
                    }
                    root.add(this)
                }
            }

            val widgets = scenario.widgets.map { spec ->
                ConstraintWidget(spec.width, spec.height).apply {
                    debugName = spec.name
                    setHorizontalDimensionBehaviour(behaviour(spec.horizontal))
                    setVerticalDimensionBehaviour(behaviour(spec.vertical))
                    if (spec.minWidth > 0) setMinWidth(spec.minWidth)
                    if (spec.minHeight > 0) setMinHeight(spec.minHeight)
                    if (spec.maxWidth != Int.MAX_VALUE) maxWidth = spec.maxWidth
                    if (spec.maxHeight != Int.MAX_VALUE) maxHeight = spec.maxHeight
                    horizontalBiasPercent = spec.horizontalBias
                    verticalBiasPercent = spec.verticalBias
                    spec.dimensionRatio?.let { setDimensionRatio(it) }
                    root.add(this)
                }
            }

            val barriers = scenario.barriers.map { spec ->
                Barrier().apply {
                    debugName = spec.name
                    setBarrierType(
                        when (spec.side) {
                            Side.LEFT -> Barrier.LEFT
                            Side.RIGHT -> Barrier.RIGHT
                            Side.TOP -> Barrier.TOP
                            Side.BOTTOM -> Barrier.BOTTOM
                        },
                    )
                    setMargin(spec.margin)
                    spec.referenced.forEach { add(widgets[it]) }
                    root.add(this)
                }
            }

            for (connection in scenario.connections) {
                val target = when (val to = connection.target) {
                    is Target.Root -> root
                    is Target.Widget -> widgets[to.index]
                    is Target.Barrier -> barriers[to.index]
                    is Target.Guideline -> guidelines[to.index]
                }
                widgets[connection.from].connect(
                    side(connection.fromSide),
                    target,
                    side(connection.toSide),
                    connection.margin,
                )
            }

            for (circle in scenario.circular) {
                widgets[circle.from].connectCircularConstraint(
                    widgets[circle.target],
                    circle.angle,
                    circle.radius,
                )
            }

            for (chain in scenario.chains) {
                val head = widgets[chain.members.first()]
                val style = when (chain.style) {
                    ChainStyle.SPREAD -> ConstraintWidget.CHAIN_SPREAD
                    ChainStyle.SPREAD_INSIDE -> ConstraintWidget.CHAIN_SPREAD_INSIDE
                    ChainStyle.PACKED -> ConstraintWidget.CHAIN_PACKED
                }
                if (chain.horizontal) {
                    head.setHorizontalChainStyle(style)
                } else {
                    head.setVerticalChainStyle(style)
                }
            }

            root.layout()
            LayoutOutcome.LaidOut(render(root, guidelines, widgets, barriers))
        } catch (e: Exception) {
            LayoutOutcome.Leaked(LayoutOutcome.categorise(e))
        } catch (e: StackOverflowError) {
            LayoutOutcome.Crashed("StackOverflowError")
        } catch (e: OutOfMemoryError) {
            LayoutOutcome.Crashed("OutOfMemoryError")
        } catch (e: Throwable) {
            LayoutOutcome.Crashed(e::class.simpleName ?: "Unknown")
        }

    private fun behaviour(value: Behaviour): ConstraintWidget.DimensionBehaviour =
        when (value) {
            Behaviour.FIXED -> ConstraintWidget.DimensionBehaviour.FIXED
            Behaviour.WRAP_CONTENT -> ConstraintWidget.DimensionBehaviour.WRAP_CONTENT
            Behaviour.MATCH_CONSTRAINT -> ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT
            Behaviour.MATCH_PARENT -> ConstraintWidget.DimensionBehaviour.MATCH_PARENT
        }

    private fun side(value: Side): ConstraintAnchor.Type =
        when (value) {
            Side.LEFT -> ConstraintAnchor.Type.LEFT
            Side.TOP -> ConstraintAnchor.Type.TOP
            Side.RIGHT -> ConstraintAnchor.Type.RIGHT
            Side.BOTTOM -> ConstraintAnchor.Type.BOTTOM
        }

    private fun render(
        root: ConstraintWidgetContainer,
        guidelines: List<ConstraintWidget>,
        widgets: List<ConstraintWidget>,
        barriers: List<ConstraintWidget>,
    ): String =
        buildString {
            append("root: ").append(root.width).append('x').append(root.height).append('\n')
            for (participant in guidelines + widgets + barriers) {
                append(participant.debugName).append(": (").append(participant.x).append(", ")
                    .append(participant.y).append(") ").append(participant.width).append('x')
                    .append(participant.height).append('\n')
            }
        }
}
