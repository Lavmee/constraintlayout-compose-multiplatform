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

open class DependencyNode(
    var mRun: WidgetRun,
) : Dependency {

    var updateDelegate: Dependency? = null
    var delegateToWidgetRun = false
    var readyToSolve = false

    enum class Type {
        UNKNOWN,
        HORIZONTAL_DIMENSION,
        VERTICAL_DIMENSION,
        LEFT,
        RIGHT,
        TOP,
        BOTTOM,
        BASELINE,
    }

    var mType: Type = Type.UNKNOWN
    var mMargin: Int = 0
    var value: Int = 0
    var mMarginFactor: Int = 1
    var mMarginDependency: DimensionDependency? = null
    var resolved: Boolean = false

    val mDependencies: ArrayList<Dependency> = ArrayList()
    val mTargets: ArrayList<DependencyNode> = ArrayList()

    override fun toString(): String {
        return (
            mRun.mWidget!!.debugName + ":" + mType + "(" +
                (if (resolved) value else "unresolved") + ") <t=" +
                mTargets.size + ":d=" + mDependencies.size + ">"
            )
    }

    open fun resolve(value: Int) {
        if (resolved) {
            return
        }
        this.resolved = true
        this.value = value
        for (node in mDependencies) {
            node.update(node)
        }
    }

    override fun update(node: Dependency) {
        for (target in mTargets) {
            if (!target.resolved) {
                return
            }
        }

        readyToSolve = true
        if (updateDelegate != null) {
            updateDelegate!!.update(this)
        }
        if (delegateToWidgetRun) {
            mRun.update(this)
            return
        }
        var target: DependencyNode? = null
        var numTargets = 0
        for (t in mTargets) {
            if (t is DimensionDependency) {
                continue
            }
            target = t
            numTargets++
        }
        if (target != null && numTargets == 1 && target.resolved) {
            if (mMarginDependency != null) {
                if (mMarginDependency!!.resolved) {
                    mMargin = mMarginFactor * mMarginDependency!!.value
                } else {
                    return
                }
            }
            resolve(target.value + mMargin)
        }
        if (updateDelegate != null) {
            updateDelegate!!.update(this)
        }
    }

    fun addDependency(dependency: Dependency) {
        mDependencies.add(dependency)
        if (resolved) {
            dependency.update(dependency)
        }
    }

    fun name(): String {
        var definition: String = mRun.mWidget!!.debugName.toString()
        definition += if (mType == Type.LEFT ||
            mType == Type.RIGHT
        ) {
            "_HORIZONTAL"
        } else {
            "_VERTICAL"
        }
        definition += ":" + mType.name
        return definition
    }

    fun clear() {
        mTargets.clear()
        mDependencies.clear()
        resolved = false
        value = 0
        readyToSolve = false
        delegateToWidgetRun = false
    }
}
