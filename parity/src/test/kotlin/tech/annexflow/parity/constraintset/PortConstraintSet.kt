// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.constraintset

import tech.annexflow.constraintlayout.core.parser.CLParser
import tech.annexflow.constraintlayout.core.parser.CLParsingException
import tech.annexflow.constraintlayout.core.state.ConstraintSetParser
import tech.annexflow.constraintlayout.core.state.CorePixelDp
import tech.annexflow.constraintlayout.core.state.State
import tech.annexflow.constraintlayout.core.widgets.ConstraintWidgetContainer

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
                geometry += GeometryRow(id, child.left, child.top, child.width, child.height)
                val frame = child.frame
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
