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
import androidx.constraintlayout.coremp.widgets.ConstraintWidget

class HelperReferences(widget: ConstraintWidget) : WidgetRun(widget) {

    override fun clear() {
        mRunGroup = null
        start.clear()
    }

    override fun reset() {
        start.resolved = false
    }

    override fun supportsWrapComputation(): Boolean {
        return false
    }

    private fun addDependency(node: DependencyNode) {
        start.mDependencies.add(node)
        node.mTargets.add(start)
    }

    override fun apply() {
        if (mWidget is Barrier) {
            start.delegateToWidgetRun = true
            val barrier: Barrier = mWidget as Barrier
            val type: Int = barrier.getBarrierType()
            val allowsGoneWidget: Boolean = barrier.getAllowsGoneWidget()
            when (type) {
                Barrier.LEFT -> {
                    start.mType = DependencyNode.Type.LEFT
                    var i = 0
                    while (i < barrier.mWidgetsCount) {
                        val refWidget: ConstraintWidget = barrier.mWidgets[i]!!
                        if (!allowsGoneWidget &&
                            refWidget.visibility == ConstraintWidget.GONE
                        ) {
                            i++
                            continue
                        }
                        val target = refWidget.mHorizontalRun!!.start
                        target.mDependencies.add(start)
                        start.mTargets.add(target)
                        i++
                    }
                    addDependency(mWidget!!.mHorizontalRun!!.start)
                    addDependency(mWidget!!.mHorizontalRun!!.end)
                }

                Barrier.RIGHT -> {
                    start.mType = DependencyNode.Type.RIGHT
                    var i = 0
                    while (i < barrier.mWidgetsCount) {
                        val refWidget: ConstraintWidget = barrier.mWidgets[i]!!
                        if (!allowsGoneWidget &&
                            refWidget.visibility == ConstraintWidget.GONE
                        ) {
                            i++
                            continue
                        }
                        val target = refWidget.mHorizontalRun!!.end
                        target.mDependencies.add(start)
                        start.mTargets.add(target)
                        i++
                    }
                    addDependency(mWidget!!.mHorizontalRun!!.start)
                    addDependency(mWidget!!.mHorizontalRun!!.end)
                }

                Barrier.TOP -> {
                    start.mType = DependencyNode.Type.TOP
                    var i = 0
                    while (i < barrier.mWidgetsCount) {
                        val refwidget: ConstraintWidget = barrier.mWidgets[i]!!
                        if (!allowsGoneWidget &&
                            refwidget.visibility == ConstraintWidget.GONE
                        ) {
                            i++
                            continue
                        }
                        val target = refwidget.mVerticalRun!!.start
                        target.mDependencies.add(start)
                        start.mTargets.add(target)
                        i++
                    }
                    addDependency(mWidget!!.mVerticalRun!!.start)
                    addDependency(mWidget!!.mVerticalRun!!.end)
                }

                Barrier.BOTTOM -> {
                    start.mType = DependencyNode.Type.BOTTOM
                    var i = 0
                    while (i < barrier.mWidgetsCount) {
                        val refwidget: ConstraintWidget = barrier.mWidgets[i]!!
                        if (!allowsGoneWidget &&
                            refwidget.visibility == ConstraintWidget.GONE
                        ) {
                            i++
                            continue
                        }
                        val target = refwidget.mVerticalRun!!.end
                        target.mDependencies.add(start)
                        start.mTargets.add(target)
                        i++
                    }
                    addDependency(mWidget!!.mVerticalRun!!.start)
                    addDependency(mWidget!!.mVerticalRun!!.end)
                }
            }
        }
    }

    override fun update(node: Dependency) {
        val barrier: Barrier = mWidget as Barrier
        val type: Int = barrier.getBarrierType()
        var min = -1
        var max = 0
        for (target in start.mTargets) {
            val value = target.value
            if (min == -1 || value < min) {
                min = value
            }
            if (max < value) {
                max = value
            }
        }
        if (type == Barrier.LEFT || type == Barrier.TOP) {
            start.resolve(min + barrier.getMargin())
        } else {
            start.resolve(max + barrier.getMargin())
        }
    }

    override fun applyToWidget() {
        if (mWidget is Barrier) {
            val barrier: Barrier = mWidget as Barrier
            val type: Int = barrier.getBarrierType()
            if (type == Barrier.LEFT ||
                type == Barrier.RIGHT
            ) {
                mWidget!!.setX(start.value)
            } else {
                mWidget!!.setY(start.value)
            }
        }
    }
}
