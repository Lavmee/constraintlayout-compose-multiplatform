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
package androidx.constraintlayout.coremp.widgets

import androidx.constraintlayout.coremp.Cache
import androidx.constraintlayout.coremp.SolverVariable
import androidx.constraintlayout.coremp.widgets.analyzer.Grouping
import androidx.constraintlayout.coremp.widgets.analyzer.WidgetGroup

/**
 * Model a constraint relation. Widgets contains anchors, and a constraint relation between
 * two widgets is made by connecting one anchor to another. The anchor will contains a pointer
 * to the target anchor if it is connected.
 */
class ConstraintAnchor {
    private var mDependents: HashSet<ConstraintAnchor>? = null
    private var mFinalValue = 0
    private var mHasFinalValue = false

    // @TODO: add description
    fun findDependents(orientation: Int, list: ArrayList<WidgetGroup>?, group: WidgetGroup?) {
        if (mDependents != null) {
            for (anchor in mDependents!!) {
                Grouping.findDependents(anchor.mOwner, orientation, list!!, group)
            }
        }
    }

    fun getDependents(): HashSet<ConstraintAnchor>? {
        return mDependents
    }

    // @TODO: add description
    fun hasDependents(): Boolean {
        return if (mDependents == null) {
            false
        } else {
            mDependents!!.size > 0
        }
    }

    // @TODO: add description
    fun hasCenteredDependents(): Boolean {
        if (mDependents == null) {
            return false
        }
        for (anchor in mDependents!!) {
            val opposite: ConstraintAnchor? = anchor.getOpposite()
            if (opposite!!.isConnected) {
                return true
            }
        }
        return false
    }

    // @TODO: add description
    fun setFinalValue(finalValue: Int) {
        mFinalValue = finalValue
        mHasFinalValue = true
    }

    // @TODO: add description
    fun getFinalValue(): Int {
        return if (!mHasFinalValue) {
            0
        } else {
            mFinalValue
        }
    }

    // @TODO: add description
    fun resetFinalResolution() {
        mHasFinalValue = false
        mFinalValue = 0
    }

    // @TODO: add description
    fun hasFinalValue(): Boolean {
        return mHasFinalValue
    }

    /**
     * Define the type of anchor
     */
    enum class Type {
        NONE,
        LEFT,
        TOP,
        RIGHT,
        BOTTOM,
        BASELINE,
        CENTER,
        CENTER_X,
        CENTER_Y,
    }

    var mOwner: ConstraintWidget
    var mType: Type
    var mTarget: ConstraintAnchor? = null
    var mMargin = 0
    var mGoneMargin = UNSET_GONE_MARGIN

    var mSolverVariable: SolverVariable? = null

    // @TODO: add description
    fun copyFrom(source: ConstraintAnchor, map: HashMap<ConstraintWidget?, ConstraintWidget?>) {
        if (mTarget != null) {
            if (mTarget!!.mDependents != null) {
                mTarget!!.mDependents!!.remove(this)
            }
        }
        mTarget = if (source.mTarget != null) {
            val type: Type = source.mTarget!!.type
            val owner = map[source.mTarget!!.mOwner]
            owner!!.getAnchor(type)
        } else {
            null
        }
        if (mTarget != null) {
            if (mTarget!!.mDependents == null) {
                mTarget!!.mDependents = HashSet()
            }
            mTarget!!.mDependents!!.add(this)
        }
        mMargin = source.mMargin
        mGoneMargin = source.mGoneMargin
    }

    /**
     * Constructor
     *
     * @param owner the widget owner of this anchor.
     * @param type  the anchor type.
     */
    constructor(owner: ConstraintWidget, type: Type) {
        mOwner = owner
        mType = type
    }

    /**
     * Return the solver variable for this anchor
     */
    fun getSolverVariable(): SolverVariable? {
        return mSolverVariable
    }

    /**
     * Reset the solver variable
     */
    fun resetSolverVariable(cache: Cache?) {
        if (mSolverVariable == null) {
            mSolverVariable = SolverVariable(SolverVariable.Type.UNRESTRICTED, null)
        } else {
            mSolverVariable!!.reset()
        }
    }

    /**
     * Return the anchor's owner
     *
     * @return the Widget owning the anchor
     */

    val owner: ConstraintWidget get() = mOwner

    /**
     * Return the type of the anchor
     *
     * @return type of the anchor.
     */

    val type: Type get() = mType

    /**
     * Return the connection's margin from this anchor to its target.
     *
     * @return the margin value. 0 if not connected.
     */

    val margin: Int get() {
        if (mOwner.visibility == ConstraintWidget.GONE) {
            return 0
        }
        return if (mGoneMargin != UNSET_GONE_MARGIN && mTarget != null && mTarget!!.mOwner.visibility == ConstraintWidget.GONE) {
            mGoneMargin
        } else {
            mMargin
        }
    }

    /**
     * Return the connection's target (null if not connected)
     *
     * @return the ConstraintAnchor target
     */

    val target: ConstraintAnchor? get() = mTarget

    /**
     * Resets the anchor's connection.
     */
    fun reset() {
        if (mTarget != null && mTarget!!.mDependents != null) {
            mTarget!!.mDependents!!.remove(this)
            if (mTarget!!.mDependents!!.size == 0) {
                mTarget!!.mDependents = null
            }
        }
        mDependents = null
        mTarget = null
        mMargin = 0
        mGoneMargin = UNSET_GONE_MARGIN
        mHasFinalValue = false
        mFinalValue = 0
    }

    /**
     * Connects this anchor to another one.
     *
     * @return true if the connection succeeds.
     */
    fun connect(
        toAnchor: ConstraintAnchor?,
        margin: Int,
        goneMargin: Int,
        forceConnection: Boolean,
    ): Boolean {
        if (toAnchor == null) {
            reset()
            return true
        }
        if (!forceConnection && !isValidConnection(toAnchor)) {
            return false
        }
        mTarget = toAnchor
        if (mTarget!!.mDependents == null) {
            mTarget!!.mDependents = HashSet()
        }
        if (mTarget!!.mDependents != null) {
            mTarget!!.mDependents!!.add(this)
        }
        mMargin = margin
        mGoneMargin = goneMargin
        return true
    }

    /**
     * Connects this anchor to another one.
     *
     * @return true if the connection succeeds.
     */
    fun connect(toAnchor: ConstraintAnchor?, margin: Int): Boolean {
        return connect(toAnchor, margin, UNSET_GONE_MARGIN, false)
    }

    /**
     * Returns the connection status of this anchor
     *
     * @return true if the anchor is connected to another one.
     */

    val isConnected: Boolean get() = mTarget != null

    /**
     * Checks if the connection to a given anchor is valid.
     *
     * @param anchor the anchor we want to connect to
     * @return true if it's a compatible anchor
     */
    fun isValidConnection(anchor: ConstraintAnchor?): Boolean {
        if (anchor == null) {
            return false
        }
        val target = anchor.type
        if (target == mType) {
            return !(mType == Type.BASELINE && (!anchor.owner.hasBaseline || !owner.hasBaseline))
        }
        when (mType) {
            Type.CENTER -> {
                // allow everything but baseline and center_x/center_y
                return target != Type.BASELINE && target != Type.CENTER_X && target != Type.CENTER_Y
            }

            Type.LEFT, Type.RIGHT -> {
                var isCompatible = target == Type.LEFT || target == Type.RIGHT
                if (anchor.owner is Guideline) {
                    isCompatible = isCompatible || target == Type.CENTER_X
                }
                return isCompatible
            }

            Type.TOP, Type.BOTTOM -> {
                var isCompatible = target == Type.TOP || target == Type.BOTTOM
                if (anchor.owner is Guideline) {
                    isCompatible = isCompatible || target == Type.CENTER_Y
                }
                return isCompatible
            }

            Type.BASELINE -> {
                return !(target == Type.LEFT || target == Type.RIGHT)
            }

            Type.CENTER_X, Type.CENTER_Y, Type.NONE -> return false
        }
        throw AssertionError(mType.name)
    }

    /**
     * Return true if this anchor is a side anchor
     *
     * @return true if side anchor
     */
    fun isSideAnchor(): Boolean {
        return when (mType) {
            Type.LEFT, Type.RIGHT, Type.TOP, Type.BOTTOM -> true
            Type.BASELINE, Type.CENTER, Type.CENTER_X, Type.CENTER_Y, Type.NONE -> false
        }
        throw AssertionError(mType.name)
    }

    /**
     * Return true if the connection to the given anchor is in the
     * same dimension (horizontal or vertical)
     *
     * @param anchor the anchor we want to connect to
     * @return true if it's an anchor on the same dimension
     */
    fun isSimilarDimensionConnection(anchor: ConstraintAnchor): Boolean {
        val target = anchor.type
        if (target == mType) {
            return true
        }
        return when (mType) {
            Type.CENTER -> {
                target != Type.BASELINE
            }

            Type.LEFT, Type.RIGHT, Type.CENTER_X -> {
                target == Type.LEFT || target == Type.RIGHT || target == Type.CENTER_X
            }

            Type.TOP, Type.BOTTOM, Type.CENTER_Y, Type.BASELINE -> {
                target == Type.TOP || target == Type.BOTTOM || target == Type.CENTER_Y || target == Type.BASELINE
            }

            Type.NONE -> false
        }
        throw AssertionError(mType.name)
    }

    /**
     * Set the margin of the connection (if there's one)
     *
     * @param margin the new margin of the connection
     */
    fun setMargin(margin: Int) {
        if (isConnected) {
            mMargin = margin
        }
    }

    /**
     * Set the gone margin of the connection (if there's one)
     *
     * @param margin the new margin of the connection
     */
    fun setGoneMargin(margin: Int) {
        if (isConnected) {
            mGoneMargin = margin
        }
    }

    /**
     * Utility function returning true if this anchor is a vertical one.
     *
     * @return true if vertical anchor, false otherwise
     */
    fun isVerticalAnchor(): Boolean {
        return when (mType) {
            Type.LEFT, Type.RIGHT, Type.CENTER, Type.CENTER_X -> false
            Type.CENTER_Y, Type.TOP, Type.BOTTOM, Type.BASELINE, Type.NONE -> true
        }
        throw AssertionError(mType.name)
    }

    /**
     * Return a string representation of this anchor
     *
     * @return string representation of the anchor
     */
    override fun toString(): String {
        return mOwner.debugName + ":" + mType.toString()
    }

    /**
     * Return true if we can connect this anchor to this target.
     * We recursively follow connections in order to detect eventual cycles; if we
     * do we disallow the connection.
     * We also only allow connections to direct parent, siblings, and descendants.
     *
     * @param target the ConstraintWidget we are trying to connect to
     * @param anchor Allow anchor if it loops back to me directly
     * @return if the connection is allowed, false otherwise
     */
    fun isConnectionAllowed(target: ConstraintWidget, anchor: ConstraintAnchor?): Boolean {
        if (ALLOW_BINARY) {
            if (anchor != null && anchor.target == this) {
                return true
            }
        }
        return isConnectionAllowed(target)
    }

    /**
     * Return true if we can connect this anchor to this target.
     * We recursively follow connections in order to detect eventual cycles; if we
     * do we disallow the connection.
     * We also only allow connections to direct parent, siblings, and descendants.
     *
     * @param target the ConstraintWidget we are trying to connect to
     * @return true if the connection is allowed, false otherwise
     */
    fun isConnectionAllowed(target: ConstraintWidget): Boolean {
        val checked = HashSet<ConstraintWidget>()
        if (isConnectionToMe(target, checked)) {
            return false
        }
        val parent = owner.parent
        if (parent == target) { // allow connections to parent
            return true
        }
        return target.parent == parent // allow if we share the same parent
    }

    /**
     * Recursive with check for loop
     *
     * @param checked set of things already checked
     * @return true if it is connected to me
     */
    private fun isConnectionToMe(
        target: ConstraintWidget,
        checked: HashSet<ConstraintWidget>,
    ): Boolean {
        if (checked.contains(target)) {
            return false
        }
        checked.add(target)
        if (target == owner) {
            return true
        }
        val targetAnchors = target.anchors
        var i = 0
        val targetAnchorsSize = targetAnchors.size
        while (i < targetAnchorsSize) {
            val anchor = targetAnchors[i]
            if (anchor.isSimilarDimensionConnection(this) && anchor.isConnected) {
                if (isConnectionToMe(anchor.target!!.owner, checked)) {
                    return true
                }
            }
            i++
        }
        return false
    }

    /**
     * Returns the opposite anchor to this one
     *
     * @return opposite anchor
     */
    fun getOpposite(): ConstraintAnchor? {
        when (mType) {
            Type.LEFT -> {
                return mOwner.mRight
            }

            Type.RIGHT -> {
                return mOwner.mLeft
            }

            Type.TOP -> {
                return mOwner.mBottom
            }

            Type.BOTTOM -> {
                return mOwner.mTop
            }

            Type.BASELINE, Type.CENTER, Type.CENTER_X, Type.CENTER_Y, Type.NONE -> return null
        }
        throw AssertionError(mType.name)
    }

    companion object {
        private const val ALLOW_BINARY = false

        private const val UNSET_GONE_MARGIN = Int.MIN_VALUE
    }
}
