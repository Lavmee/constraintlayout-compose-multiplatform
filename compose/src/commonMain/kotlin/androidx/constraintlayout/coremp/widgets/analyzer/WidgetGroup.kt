/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.constraintlayout.coremp.widgets.analyzer

import androidx.constraintlayout.coremp.LinearSystem
import androidx.constraintlayout.coremp.platform.WeakReference
import androidx.constraintlayout.coremp.widgets.Chain
import androidx.constraintlayout.coremp.widgets.ConstraintWidget
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.BOTH
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.HORIZONTAL
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.VERTICAL
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.DimensionBehaviour
import androidx.constraintlayout.coremp.widgets.ConstraintWidgetContainer

class WidgetGroup(orientation: Int) {

    var mWidgets = ArrayList<ConstraintWidget>()

    var mId = -1
    var mAuthoritative = false
    var mOrientation: Int = orientation
    var mResults: ArrayList<MeasureResult>? = null
    private var mMoveTo = -1

    val orientation: Int get() = mOrientation

    fun getId(): Int {
        return mId
    }

    // @TODO: add description
    fun add(widget: ConstraintWidget?): Boolean {
        if (mWidgets.contains(widget)) {
            return false
        }
        mWidgets.add(widget!!)
        return true
    }

    fun setAuthoritative(isAuthoritative: Boolean) {
        mAuthoritative = isAuthoritative
    }

    @Suppress("unused")
    fun isAuthoritative(): Boolean {
        return mAuthoritative
    }

    private fun getOrientationString(): String {
        return when (mOrientation) {
            HORIZONTAL -> {
                "Horizontal"
            }

            VERTICAL -> {
                "Vertical"
            }

            BOTH -> {
                "Both"
            }

            else -> "Unknown"
        }
    }

    override fun toString(): String {
        var ret = getOrientationString() + " [" + mId + "] <"
        for (widget in mWidgets) {
            ret += " " + widget.debugName
        }
        ret += " >"
        return ret
    }

    // @TODO: add description
    fun moveTo(orientation: Int, widgetGroup: WidgetGroup) {
        if (DEBUG) {
            println(
                "Move all widgets (" + this + ") from " +
                    mId + " to " + widgetGroup.getId() + "(" + widgetGroup + ")",
            )
            println(
                (
                    "" +
                        "do not call  " + measureWrap(orientation, ConstraintWidget())
                    ),
            )
        }
        for (widget: ConstraintWidget in mWidgets) {
            widgetGroup.add(widget)
            if (orientation == HORIZONTAL) {
                widget.horizontalGroup = widgetGroup.getId()
            } else {
                widget.verticalGroup = widgetGroup.getId()
            }
        }
        mMoveTo = widgetGroup.mId
    }

    // @TODO: add description
    fun clear() {
        mWidgets.clear()
    }

    private fun measureWrap(orientation: Int, widget: ConstraintWidget): Int {
        val behaviour = widget.getDimensionBehaviour(orientation)
        if (behaviour == DimensionBehaviour.WRAP_CONTENT || behaviour == DimensionBehaviour.MATCH_PARENT || behaviour == DimensionBehaviour.FIXED) {
            val dimension: Int = if (orientation == HORIZONTAL) {
                widget.width
            } else {
                widget.height
            }
            return dimension
        }
        return -1
    }

    // @TODO: add description
    fun measureWrap(system: LinearSystem, orientation: Int): Int {
        val count = mWidgets.size
        return if (count == 0) {
            0
        } else {
            solverMeasure(system, mWidgets, orientation)
        }
        // TODO: add direct wrap computation for simpler cases instead of calling the solver
    }

    private fun solverMeasure(
        system: LinearSystem,
        widgets: ArrayList<ConstraintWidget>,
        orientation: Int,
    ): Int {
        val container = widgets[0].parent as ConstraintWidgetContainer?
        system.reset()
        @Suppress("UNUSED_VARIABLE")
        val prevDebug = LinearSystem.FULL_DEBUG
        container!!.addToSolver(system, false)
        for (i in widgets.indices) {
            val widget = widgets[i]
            widget.addToSolver(system, false)
        }
        if (orientation == HORIZONTAL) {
            if (container.mHorizontalChainsSize > 0) {
                Chain.applyChainConstraints(container, system, widgets, HORIZONTAL)
            }
        }
        if (orientation == VERTICAL) {
            if (container.mVerticalChainsSize > 0) {
                Chain.applyChainConstraints(container, system, widgets, VERTICAL)
            }
        }
        try {
            system.minimize()
        } catch (e: Exception) {
            // TODO remove fancy version of e.printStackTrace()
            println(
                e.toString() + "\n" + e.stackTraceToString()
                    .replace("[", "   at ")
                    .replace(",", "\n   at")
                    .replace("]", ""),
            )
//            System.err.println(
//                e.toString() + "\n" + e.stackTrace.toString()
//                    .replace("[", "   at ")
//                    .replace(",", "\n   at")
//                    .replace("]", "")
//            )
        }

        // save results
        mResults = ArrayList()
        for (i in widgets.indices) {
            val widget = widgets[i]
            val result = MeasureResult(widget, system, orientation)
            mResults!!.add(result)
        }
        return if (orientation == HORIZONTAL) {
            val left = system.getObjectVariableValue(container.mLeft)
            val right = system.getObjectVariableValue(container.mRight)
            system.reset()
            right - left
        } else {
            val top = system.getObjectVariableValue(container.mTop)
            val bottom = system.getObjectVariableValue(container.mBottom)
            system.reset()
            bottom - top
        }
    }

    fun setOrientation(orientation: Int) {
        mOrientation = orientation
    }

    // @TODO: add description
    fun apply() {
        if (mResults == null) {
            return
        }
        if (!mAuthoritative) {
            return
        }
        for (i in mResults!!.indices) {
            val result: MeasureResult = mResults!![i]
            result.apply()
        }
    }

    // @TODO: add description
    @Suppress("unused")
    fun intersectWith(group: WidgetGroup): Boolean {
        for (i in mWidgets.indices) {
            val widget = mWidgets[i]
            if (group.contains(widget)) {
                return true
            }
        }
        return false
    }

    private operator fun contains(widget: ConstraintWidget): Boolean {
        return mWidgets.contains(widget)
    }

    // @TODO: add description
    fun size(): Int {
        return mWidgets.size
    }

    // @TODO: add description
    fun cleanup(dependencyLists: ArrayList<WidgetGroup>) {
        val count = mWidgets.size
        if (mMoveTo != -1 && count > 0) {
            for (i in dependencyLists.indices) {
                val group = dependencyLists[i]
                if (mMoveTo == group.mId) {
                    moveTo(mOrientation, group)
                }
            }
        }
        if (count == 0) {
            dependencyLists.remove(this)
            return
        }
    }

    class MeasureResult(widget: ConstraintWidget, system: LinearSystem, orientation: Int) {
        var mWidgetRef: WeakReference<ConstraintWidget>? = null
        var mLeft = 0
        var mTop = 0
        var mRight = 0
        var mBottom = 0
        var mBaseline = 0
        var mOrientation = orientation

        init {
            mWidgetRef = WeakReference(widget)
            mLeft = system.getObjectVariableValue(widget.mLeft)
            mTop = system.getObjectVariableValue(widget.mTop)
            mRight = system.getObjectVariableValue(widget.mRight)
            mBottom = system.getObjectVariableValue(widget.mBottom)
            mBaseline = system.getObjectVariableValue(widget.mBaseline)
        }

        fun apply() {
            val widget = mWidgetRef!!.get()
            widget?.setFinalFrame(mLeft, mTop, mRight, mBottom, mBaseline, mOrientation)
        }
    }

    companion object {
        private const val DEBUG = false

        var sCount = 0
    }

    init {
        mId = sCount++
    }
}
