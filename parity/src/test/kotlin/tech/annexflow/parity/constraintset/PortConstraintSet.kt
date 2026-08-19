// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.constraintset

import tech.annexflow.constraintlayout.core.parser.CLParser
import tech.annexflow.constraintlayout.core.parser.CLParsingException
import tech.annexflow.constraintlayout.core.state.ConstraintSetParser
import tech.annexflow.constraintlayout.core.state.CorePixelDp
import tech.annexflow.constraintlayout.core.state.Dimension
import tech.annexflow.constraintlayout.core.state.State
import tech.annexflow.constraintlayout.core.widgets.ConstraintWidget
import tech.annexflow.constraintlayout.core.widgets.ConstraintWidgetContainer
import tech.annexflow.constraintlayout.core.widgets.Optimizer
import tech.annexflow.constraintlayout.core.widgets.analyzer.BasicMeasure

/**
 * Drives the shaded port. `OracleConstraintSet` performs the same sequence against the vendored
 * upstream Java; keeping the two in step is the whole contract, so any edit here needs the mirror
 * edit there.
 *
 * Calls `ConstraintSetParser.populateState` directly rather than the public `parseJSON` wrapper:
 * the wrapper swallows `CLParsingException` and prints it, leaving the `State` half-populated with
 * no signal at all. That is faithful to upstream, but blind as an observation point.
 */
object PortConstraintSet : ConstraintSetSubject {
    override val name: String = "port"

    override fun parse(spec: ConstraintSetSpec): ConstraintSetOutcome =
        try {
            val json = emit(spec)
            val state = State()
            state.setDpToPixel(CorePixelDp { dp -> dp })
            state.isRtl = spec.isRtl
            val variables = ConstraintSetParser.LayoutVariables()
            ConstraintSetParser.populateState(CLParser.parse(json), state, variables)
            val root = ConstraintWidgetContainer(0, 0, spec.rootWidth, spec.rootHeight)
            root.debugName = "root"
            state.apply(root)
            root.layout()
            val geometry = mutableListOf<GeometryRow>()
            val custom = mutableListOf<CustomRow>()
            for (child in root.children) {
                val id = child.stringId ?: "?"
                val frame = child.frame
                geometry += GeometryRow(
                    id, child.left, child.top, child.width, child.height,
                    frame.visibility, frame.alpha,
                    frame.rotationX, frame.rotationY, frame.rotationZ,
                    frame.scaleX, frame.scaleY,
                    frame.translationX, frame.translationY, frame.translationZ,
                    frame.pivotX, frame.pivotY,
                )
                for (attrName in frame.getCustomAttributeNames()) {
                    custom += CustomRow(id, attrName, frame.getCustomAttribute(attrName)?.toString() ?: "null")
                }
            }
            ConstraintSetOutcome.Populated(renderGeometry(geometry), renderCustom(custom))
        } catch (e: CLParsingException) {
            ConstraintSetOutcome.Leaked("CLParsing")
        } catch (e: Throwable) {
            ConstraintSetOutcome.Crashed(ConstraintSetOutcome.categorise(e))
        }

    /**
     * Every widget here is a synthetic solver primitive with no real content to wrap around, so a
     * `WRAP_CONTENT` axis measures to 0 — there is nothing to report beyond "no intrinsic size."
     * That is enough: none of `AxisLivenessTest`'s `measure`-backed cases put a `WRAP_CONTENT`
     * widget on the axis under test, they use `MATCH_CONSTRAINT`, which this measurer passes
     * straight through as `measure.horizontalDimension`/`verticalDimension` untouched.
     *
     * Stateless like `solver.OracleSolver`'s `SpecMeasurer`, for the same reason: `solverMeasure`
     * calls back repeatedly across re-measure passes, and a measurer that remembered anything would
     * make the outcome depend on call order.
     */
    private class ZeroMeasurer : BasicMeasure.Measurer {
        override fun measure(widget: ConstraintWidget, measure: BasicMeasure.Measure) {
            measure.measuredWidth =
                if (measure.horizontalBehavior == ConstraintWidget.DimensionBehaviour.WRAP_CONTENT) {
                    0
                } else {
                    measure.horizontalDimension
                }
            measure.measuredHeight =
                if (measure.verticalBehavior == ConstraintWidget.DimensionBehaviour.WRAP_CONTENT) {
                    0
                } else {
                    measure.verticalDimension
                }
            measure.measuredHasBaseline = false
            measure.measuredNeedsSolverPass = false
        }

        override fun didMeasures() = Unit
    }

    /**
     * [parse] deliberately untouched by this addition — see [ConstraintSetSubject.measure]'s kdoc.
     * This is a fully independent copy of [parse]'s setup rather than a shared helper, so a future
     * edit to one can never accidentally reach into the other.
     *
     * The two extra lines [parse] doesn't have — `state.setWidth`/`setHeight` — are the actual fix,
     * found by bisecting against a raw `ConstraintWidget` reproduction outside `State` entirely
     * (see the task report): `State`'s root dimension defaults to `WRAP_CONTENT` (`Dimension`'s own
     * default, per `Dimension.kt`) whenever nothing sets it, which [parse] never does either — every
     * `ConstraintSetSpec` here has a `rootWidth`/`rootHeight`, but neither entry point ever tells
     * `State` about it, only the real `ConstraintWidgetContainer`. That default is harmless for a
     * widget anchored on one side only (`begin = target + margin`, independent of the parent's own
     * resolution), which is why `parse`-backed cases were never affected — but
     * `ConstraintWidget.applyConstraints`'s bias-centering equation, and the chain/`MATCH_CONSTRAINT`
     * machinery built on the same "parent bounds are known" assumption, both special-case
     * `parentWrapContent`, and never receive a real bias/spread computation once it's true. Marking
     * the root `Dimension.createFixed(...)` here is what actually unlocks
     * `hBias`/`vBias`/`hRtlBias`/`centerVertically`/`chainStyle`/`hWeight`/`vWeight` — confirmed by
     * reproducing the bug with a raw `ConstraintWidget` + plain `.connect()` (bias worked with no
     * `State` involved at all), then narrowing it to exactly this by adding `State` back piece by
     * piece. `measure(...)` itself (this entry point's other difference from [parse]) turned out not
     * to be the fix; it's kept because a real `Measurer` is still the correct, honest way to drive
     * `MATCH_CONSTRAINT` sizing, and because it's this module's parallel to
     * `solver.OracleSolver`/`PortSolver`'s existing `layout`/`measure` pair.
     */
    override fun measure(spec: ConstraintSetSpec): ConstraintSetOutcome =
        try {
            val json = emit(spec)
            val state = State()
            state.setDpToPixel(CorePixelDp { dp -> dp })
            state.isRtl = spec.isRtl
            state.setWidth(Dimension.createFixed(spec.rootWidth))
            state.setHeight(Dimension.createFixed(spec.rootHeight))
            val variables = ConstraintSetParser.LayoutVariables()
            ConstraintSetParser.populateState(CLParser.parse(json), state, variables)
            val root = ConstraintWidgetContainer(0, 0, spec.rootWidth, spec.rootHeight)
            root.debugName = "root"
            state.apply(root)
            root.measurer = ZeroMeasurer()
            root.measure(
                Optimizer.OPTIMIZATION_STANDARD,
                BasicMeasure.EXACTLY,
                spec.rootWidth,
                BasicMeasure.EXACTLY,
                spec.rootHeight,
                0,
                0,
                0,
                0,
            )
            val geometry = mutableListOf<GeometryRow>()
            val custom = mutableListOf<CustomRow>()
            for (child in root.children) {
                val id = child.stringId ?: "?"
                val frame = child.frame
                geometry += GeometryRow(
                    id, child.left, child.top, child.width, child.height,
                    frame.visibility, frame.alpha,
                    frame.rotationX, frame.rotationY, frame.rotationZ,
                    frame.scaleX, frame.scaleY,
                    frame.translationX, frame.translationY, frame.translationZ,
                    frame.pivotX, frame.pivotY,
                )
                for (attrName in frame.getCustomAttributeNames()) {
                    custom += CustomRow(id, attrName, frame.getCustomAttribute(attrName)?.toString() ?: "null")
                }
            }
            ConstraintSetOutcome.Populated(renderGeometry(geometry), renderCustom(custom))
        } catch (e: CLParsingException) {
            ConstraintSetOutcome.Leaked("CLParsing")
        } catch (e: Throwable) {
            ConstraintSetOutcome.Crashed(ConstraintSetOutcome.categorise(e))
        }

    override fun designElements(spec: DesignElementsSpec): ConstraintSetOutcome =
        try {
            val list = ArrayList<ConstraintSetParser.DesignElement>()
            ConstraintSetParser.parseDesignElementsJSON(emitDesignElements(spec), list)
            val rows = list.map { ElementRow(it.id, it.type, it.params) }
            ConstraintSetOutcome.Elements(renderElements(rows))
        } catch (e: CLParsingException) {
            ConstraintSetOutcome.Leaked("CLParsing")
        } catch (e: Throwable) {
            ConstraintSetOutcome.Crashed(ConstraintSetOutcome.categorise(e))
        }
}
