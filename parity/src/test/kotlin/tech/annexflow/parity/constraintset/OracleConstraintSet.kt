// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.constraintset

import androidx.constraintlayout.core.parser.CLParser
import androidx.constraintlayout.core.parser.CLParsingException
import androidx.constraintlayout.core.state.ConstraintSetParser
import androidx.constraintlayout.core.state.CorePixelDp
import androidx.constraintlayout.core.state.State
import androidx.constraintlayout.core.widgets.ConstraintWidgetContainer

/**
 * Drives the vendored upstream Java. `PortConstraintSet` performs the same sequence against the
 * shaded port; keeping the two in step is the whole contract, so any edit here needs the mirror
 * edit there.
 *
 * Calls `ConstraintSetParser.populateState` directly rather than the public `parseJSON` wrapper:
 * the wrapper swallows `CLParsingException` and prints it, leaving the `State` half-populated with
 * no signal at all. That is faithful to upstream, but blind as an observation point.
 */
object OracleConstraintSet : ConstraintSetSubject {
    override val name: String = "oracle"

    override fun parse(spec: ConstraintSetSpec): ConstraintSetOutcome =
        try {
            val json = emit(spec)
            val state = State()
            state.setDpToPixel(CorePixelDp { dp -> dp })
            state.setRtl(spec.isRtl)
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

    override fun designElements(spec: DesignElementsSpec): ConstraintSetOutcome =
        try {
            val list = ArrayList<ConstraintSetParser.DesignElement>()
            ConstraintSetParser.parseDesignElementsJSON(emitDesignElements(spec), list)
            val rows = list.map { ElementRow(it.getId(), it.getType(), it.getParams()) }
            ConstraintSetOutcome.Elements(renderElements(rows))
        } catch (e: CLParsingException) {
            ConstraintSetOutcome.Leaked("CLParsing")
        } catch (e: Throwable) {
            ConstraintSetOutcome.Crashed(ConstraintSetOutcome.categorise(e))
        }
}
