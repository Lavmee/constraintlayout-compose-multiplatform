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

import androidx.constraintlayout.coremp.widgets.Barrier
import androidx.constraintlayout.coremp.widgets.ConstraintAnchor
import androidx.constraintlayout.coremp.widgets.ConstraintWidget
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.BOTH
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.HORIZONTAL
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.VERTICAL
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.DimensionBehaviour
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.DimensionBehaviour.FIXED
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.DimensionBehaviour.MATCH_PARENT
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.DimensionBehaviour.WRAP_CONTENT
import androidx.constraintlayout.coremp.widgets.ConstraintWidgetContainer
import androidx.constraintlayout.coremp.widgets.Flow
import androidx.constraintlayout.coremp.widgets.Guideline
import androidx.constraintlayout.coremp.widgets.HelperWidget
import androidx.constraintlayout.coremp.widgets.analyzer.BasicMeasure.Measurer

class Grouping {

    companion object {
        private const val DEBUG = false
        private const val DEBUG_GROUPING = false
        private const val FORCE_USE = true

        // @TODO: add description
        fun validInGroup(
            layoutHorizontal: DimensionBehaviour,
            layoutVertical: DimensionBehaviour,
            widgetHorizontal: DimensionBehaviour,
            widgetVertical: DimensionBehaviour,
        ): Boolean {
            val fixedHorizontal =
                widgetHorizontal == FIXED || widgetHorizontal == WRAP_CONTENT || widgetHorizontal == MATCH_PARENT && layoutHorizontal != WRAP_CONTENT
            val fixedVertical =
                widgetVertical == FIXED || widgetVertical == WRAP_CONTENT || widgetVertical == MATCH_PARENT && layoutVertical != WRAP_CONTENT
            return fixedHorizontal || fixedVertical
        }

        // @TODO: add description
        fun simpleSolvingPass(
            layout: ConstraintWidgetContainer,
            measurer: Measurer?,
        ): Boolean {
            if (DEBUG) {
                println("*** GROUP SOLVING ***")
            }
            val children = layout.children
            val count = children.size
            var verticalGuidelines: ArrayList<Guideline>? = null
            var horizontalGuidelines: ArrayList<Guideline>? = null
            var horizontalBarriers: ArrayList<HelperWidget>? = null
            var verticalBarriers: ArrayList<HelperWidget>? = null
            var isolatedHorizontalChildren: ArrayList<ConstraintWidget>? = null
            var isolatedVerticalChildren: ArrayList<ConstraintWidget>? = null
            for (i in 0 until count) {
                val child = children[i]
                if (!validInGroup(
                        layout.horizontalDimensionBehaviour,
                        layout.verticalDimensionBehaviour,
                        child.horizontalDimensionBehaviour,
                        child.verticalDimensionBehaviour,
                    )
                ) {
                    if (DEBUG) {
                        println("*** NO GROUP SOLVING ***")
                    }
                    return false
                }
                if (child is Flow) {
                    return false
                }
            }
            if (layout.mMetrics != null) {
                layout.mMetrics!!.grouping++
            }
            for (i in 0 until count) {
                val child = children[i]
                if (!validInGroup(
                        layout.horizontalDimensionBehaviour,
                        layout.verticalDimensionBehaviour,
                        child.horizontalDimensionBehaviour,
                        child.verticalDimensionBehaviour,
                    )
                ) {
                    ConstraintWidgetContainer.measure(
                        0,
                        child,
                        measurer,
                        layout.mMeasure,
                        BasicMeasure.Measure.SELF_DIMENSIONS,
                    )
                }
                if (child is Guideline) {
                    val guideline: Guideline = child
                    if (guideline.orientation == HORIZONTAL) {
                        if (horizontalGuidelines == null) {
                            horizontalGuidelines = ArrayList()
                        }
                        horizontalGuidelines.add(guideline)
                    }
                    if (guideline.orientation == VERTICAL) {
                        if (verticalGuidelines == null) {
                            verticalGuidelines = ArrayList()
                        }
                        verticalGuidelines.add(guideline)
                    }
                }
                if (child is HelperWidget) {
                    if (child is Barrier) {
                        val barrier: Barrier = child
                        if (barrier.orientation == HORIZONTAL) {
                            if (horizontalBarriers == null) {
                                horizontalBarriers = ArrayList()
                            }
                            horizontalBarriers.add(barrier)
                        }
                        if (barrier.orientation == VERTICAL) {
                            if (verticalBarriers == null) {
                                verticalBarriers = ArrayList()
                            }
                            verticalBarriers.add(barrier)
                        }
                    } else {
                        val helper = child
                        if (horizontalBarriers == null) {
                            horizontalBarriers = ArrayList()
                        }
                        horizontalBarriers.add(helper)
                        if (verticalBarriers == null) {
                            verticalBarriers = ArrayList()
                        }
                        verticalBarriers.add(helper)
                    }
                }
                if (child.mLeft.mTarget == null && child.mRight.mTarget == null && child !is Guideline && child !is Barrier
                ) {
                    if (isolatedHorizontalChildren == null) {
                        isolatedHorizontalChildren = ArrayList()
                    }
                    isolatedHorizontalChildren.add(child)
                }
                if (child.mTop.mTarget == null && child.mBottom.mTarget == null && child.mBaseline.mTarget == null && child !is Guideline && child !is Barrier
                ) {
                    if (isolatedVerticalChildren == null) {
                        isolatedVerticalChildren = ArrayList()
                    }
                    isolatedVerticalChildren.add(child)
                }
            }
            val allDependencyLists = ArrayList<WidgetGroup>()
            if (FORCE_USE || layout.horizontalDimensionBehaviour
                == WRAP_CONTENT
            ) {
                // horizontalDependencyLists; //new ArrayList<>();
                val dependencyLists = allDependencyLists
                if (verticalGuidelines != null) {
                    for (guideline: Guideline in verticalGuidelines) {
                        findDependents(guideline, HORIZONTAL, dependencyLists, null)
                    }
                }
                if (horizontalBarriers != null) {
                    for (barrier: HelperWidget in horizontalBarriers) {
                        val group: WidgetGroup? =
                            findDependents(barrier, HORIZONTAL, dependencyLists, null)
                        barrier.addDependents(dependencyLists, HORIZONTAL, group)
                        group!!.cleanup(dependencyLists)
                    }
                }
                val left = layout.getAnchor(ConstraintAnchor.Type.LEFT)
                if (left!!.getDependents() != null) {
                    for (first: ConstraintAnchor in left.getDependents()!!) {
                        findDependents(
                            first.mOwner,
                            HORIZONTAL,
                            dependencyLists,
                            null,
                        )
                    }
                }
                val right = layout.getAnchor(ConstraintAnchor.Type.RIGHT)
                if (right!!.getDependents() != null) {
                    for (first: ConstraintAnchor in right.getDependents()!!) {
                        findDependents(
                            first.mOwner,
                            HORIZONTAL,
                            dependencyLists,
                            null,
                        )
                    }
                }
                val center = layout.getAnchor(ConstraintAnchor.Type.CENTER)
                if (center!!.getDependents() != null) {
                    for (first: ConstraintAnchor in center.getDependents()!!) {
                        findDependents(
                            first.mOwner,
                            HORIZONTAL,
                            dependencyLists,
                            null,
                        )
                    }
                }
                if (isolatedHorizontalChildren != null) {
                    for (widget: ConstraintWidget in isolatedHorizontalChildren) {
                        findDependents(widget, HORIZONTAL, dependencyLists, null)
                    }
                }
            }
            if (FORCE_USE || layout.verticalDimensionBehaviour
                == WRAP_CONTENT
            ) {
                // verticalDependencyLists; //new ArrayList<>();
                val dependencyLists = allDependencyLists
                if (horizontalGuidelines != null) {
                    for (guideline: Guideline? in horizontalGuidelines) {
                        findDependents(guideline!!, VERTICAL, dependencyLists, null)
                    }
                }
                if (verticalBarriers != null) {
                    for (barrier: HelperWidget in verticalBarriers) {
                        val group: WidgetGroup? =
                            findDependents(barrier, VERTICAL, dependencyLists, null)
                        barrier.addDependents(dependencyLists, VERTICAL, group)
                        group!!.cleanup(dependencyLists)
                    }
                }
                val top = layout.getAnchor(ConstraintAnchor.Type.TOP)
                if (top!!.getDependents() != null) {
                    for (first: ConstraintAnchor in top.getDependents()!!) {
                        findDependents(first.mOwner, VERTICAL, dependencyLists, null)
                    }
                }
                val baseline = layout.getAnchor(ConstraintAnchor.Type.BASELINE)
                if (baseline!!.getDependents() != null) {
                    for (first: ConstraintAnchor in baseline.getDependents()!!) {
                        findDependents(first.mOwner, VERTICAL, dependencyLists, null)
                    }
                }
                val bottom = layout.getAnchor(ConstraintAnchor.Type.BOTTOM)
                if (bottom!!.getDependents() != null) {
                    for (first: ConstraintAnchor in bottom.getDependents()!!) {
                        findDependents(first.mOwner, VERTICAL, dependencyLists, null)
                    }
                }
                val center = layout.getAnchor(ConstraintAnchor.Type.CENTER)
                if (center!!.getDependents() != null) {
                    for (first: ConstraintAnchor in center.getDependents()!!) {
                        findDependents(first.mOwner, VERTICAL, dependencyLists, null)
                    }
                }
                if (isolatedVerticalChildren != null) {
                    for (widget: ConstraintWidget in isolatedVerticalChildren) {
                        findDependents(widget, VERTICAL, dependencyLists, null)
                    }
                }
            }
            // Now we may have to merge horizontal/vertical dependencies
            for (i in 0 until count) {
                val child = children[i]
                if (child.oppositeDimensionsTied()) {
                    val horizontalGroup: WidgetGroup? =
                        findGroup(allDependencyLists, child.horizontalGroup)
                    val verticalGroup: WidgetGroup? =
                        findGroup(allDependencyLists, child.verticalGroup)
                    if (horizontalGroup != null && verticalGroup != null) {
                        if (DEBUG_GROUPING) {
                            println(
                                "Merging " + horizontalGroup +
                                    " to " + verticalGroup + " for " + child,
                            )
                        }
                        horizontalGroup.moveTo(HORIZONTAL, verticalGroup)
                        verticalGroup.setOrientation(BOTH)
                        allDependencyLists.remove(horizontalGroup)
                    }
                }
                if (DEBUG_GROUPING) {
                    println(
                        (
                            "Widget " + child + " => " +
                                child.horizontalGroup + " : " + child.verticalGroup
                            ),
                    )
                }
            }
            if (allDependencyLists.size <= 1) {
                return false
            }
            if (DEBUG) {
                println("----------------------------------")
                println("-- Horizontal dependency lists:")
                println("----------------------------------")
                for (list: WidgetGroup? in allDependencyLists) {
                    if (list!!.orientation != VERTICAL) {
                        println("list: $list")
                    }
                }
                println("----------------------------------")
                println("-- Vertical dependency lists:")
                println("----------------------------------")
                for (list: WidgetGroup? in allDependencyLists) {
                    if (list!!.orientation != HORIZONTAL) {
                        println("list: $list")
                    }
                }
                println("----------------------------------")
            }
            var horizontalPick: WidgetGroup? = null
            var verticalPick: WidgetGroup? = null
            if ((
                layout.horizontalDimensionBehaviour
                    == WRAP_CONTENT
                )
            ) {
                var maxWrap = 0
                var picked: WidgetGroup? = null
                for (list: WidgetGroup? in allDependencyLists) {
                    if (list!!.orientation == VERTICAL) {
                        continue
                    }
                    list.setAuthoritative(false)
                    val wrap = list.measureWrap((layout.getSystem()), HORIZONTAL)
                    if (wrap > maxWrap) {
                        picked = list
                        maxWrap = wrap
                    }
                    if (DEBUG) {
                        println("list: $list => $wrap")
                    }
                }
                if (picked != null) {
                    if (DEBUG) {
                        println("Horizontal MaxWrap : $maxWrap with group $picked")
                    }
                    layout.setHorizontalDimensionBehaviour(FIXED)
                    layout.width = (maxWrap)
                    picked.setAuthoritative(true)
                    horizontalPick = picked
                }
            }
            if ((
                layout.verticalDimensionBehaviour
                    == WRAP_CONTENT
                )
            ) {
                var maxWrap = 0
                var picked: WidgetGroup? = null
                for (list: WidgetGroup? in allDependencyLists) {
                    if (list!!.orientation == HORIZONTAL) {
                        continue
                    }
                    list.setAuthoritative(false)
                    val wrap = list.measureWrap((layout.getSystem()), VERTICAL)
                    if (wrap > maxWrap) {
                        picked = list
                        maxWrap = wrap
                    }
                    if (DEBUG) {
                        println("      $list => $wrap")
                    }
                }
                if (picked != null) {
                    if (DEBUG) {
                        println("Vertical MaxWrap : $maxWrap with group $picked")
                    }
                    layout.setVerticalDimensionBehaviour(FIXED)
                    layout.height = (maxWrap)
                    picked.setAuthoritative(true)
                    verticalPick = picked
                }
            }
            return horizontalPick != null || verticalPick != null
        }

        private fun findGroup(
            horizontalDependencyLists: ArrayList<WidgetGroup>,
            groupId: Int,
        ): WidgetGroup? {
            val count = horizontalDependencyLists.size
            for (i in 0 until count) {
                val group = horizontalDependencyLists[i]
                if (groupId == group.getId()) {
                    return group
                }
            }
            return null
        }

        // @TODO: add description
        fun findDependents(
            constraintWidget: ConstraintWidget,
            orientation: Int,
            list: ArrayList<WidgetGroup>,
            group: WidgetGroup?,
        ): WidgetGroup? {
            var group = group
            var groupId = if (orientation == HORIZONTAL) {
                constraintWidget.horizontalGroup
            } else {
                constraintWidget.verticalGroup
            }
            if (DEBUG_GROUPING) {
                println(
                    "--- find " + (if (orientation == HORIZONTAL) "Horiz" else "Vert") +
                        " dependents of " + constraintWidget.debugName +
                        " group " + group + " widget group id " + groupId,
                )
            }
            if (groupId != -1 && (group == null || (groupId != group.getId()))) {
                // already in a group!
                if (DEBUG_GROUPING) {
                    println(
                        (
                            "widget " + constraintWidget.debugName +
                                " already in group " + groupId + " group: " + group
                            ),
                    )
                }
                for (i in list.indices) {
                    val widgetGroup = list[i]
                    if (widgetGroup.getId() == groupId) {
                        if (group != null) {
                            if (DEBUG_GROUPING) {
                                println("Move group $group to $widgetGroup")
                            }
                            group.moveTo(orientation, (widgetGroup))
                            list.remove(group)
                        }
                        group = widgetGroup
                        break
                    }
                }
            } else if (groupId != -1) {
                return group
            }
            if (group == null) {
                if (constraintWidget is HelperWidget) {
                    groupId = constraintWidget.findGroupInDependents(orientation)
                    if (groupId != -1) {
                        for (i in list.indices) {
                            val widgetGroup = list[i]
                            if (widgetGroup.getId() == groupId) {
                                group = widgetGroup
                                break
                            }
                        }
                    }
                }
                if (group == null) {
                    group = WidgetGroup(orientation)
                }
                if (DEBUG_GROUPING) {
                    println(
                        (
                            "Create group " + group +
                                " for widget " + constraintWidget.debugName
                            ),
                    )
                }
                list.add(group)
            }
            if (group.add(constraintWidget)) {
                if (constraintWidget is Guideline) {
                    val guideline: Guideline = constraintWidget
                    guideline.getAnchor().findDependents(
                        if ((guideline.orientation == Guideline.HORIZONTAL)
                        ) {
                            VERTICAL
                        } else {
                            HORIZONTAL
                        },
                        list,
                        group,
                    )
                }
                if (orientation == HORIZONTAL) {
                    constraintWidget.horizontalGroup = group.getId()
                    if (DEBUG_GROUPING) {
                        println(
                            (
                                "Widget " + constraintWidget.debugName +
                                    " H group is " + constraintWidget.horizontalGroup
                                ),
                        )
                    }
                    constraintWidget.mLeft.findDependents(orientation, list, group)
                    constraintWidget.mRight.findDependents(orientation, list, group)
                } else {
                    constraintWidget.verticalGroup = group.getId()
                    if (DEBUG_GROUPING) {
                        println(
                            (
                                "Widget " + constraintWidget.debugName +
                                    " V group is " + constraintWidget.verticalGroup
                                ),
                        )
                    }
                    constraintWidget.mTop.findDependents(orientation, list, group)
                    constraintWidget.mBaseline.findDependents(orientation, list, group)
                    constraintWidget.mBottom.findDependents(orientation, list, group)
                }
                constraintWidget.mCenter.findDependents(orientation, list, group)
            }
            return group
        }
    }
}
