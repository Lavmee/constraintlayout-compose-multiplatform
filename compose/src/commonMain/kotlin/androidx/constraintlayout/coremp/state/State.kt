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
package androidx.constraintlayout.coremp.state

import androidx.constraintlayout.coremp.state.State.Helper.ALIGN_HORIZONTALLY
import androidx.constraintlayout.coremp.state.State.Helper.ALIGN_VERTICALLY
import androidx.constraintlayout.coremp.state.State.Helper.BARRIER
import androidx.constraintlayout.coremp.state.State.Helper.COLUMN
import androidx.constraintlayout.coremp.state.State.Helper.GRID
import androidx.constraintlayout.coremp.state.State.Helper.HORIZONTAL_CHAIN
import androidx.constraintlayout.coremp.state.State.Helper.HORIZONTAL_FLOW
import androidx.constraintlayout.coremp.state.State.Helper.ROW
import androidx.constraintlayout.coremp.state.State.Helper.VERTICAL_CHAIN
import androidx.constraintlayout.coremp.state.State.Helper.VERTICAL_FLOW
import androidx.constraintlayout.coremp.state.helpers.AlignHorizontallyReference
import androidx.constraintlayout.coremp.state.helpers.AlignVerticallyReference
import androidx.constraintlayout.coremp.state.helpers.BarrierReference
import androidx.constraintlayout.coremp.state.helpers.FlowReference
import androidx.constraintlayout.coremp.state.helpers.GridReference
import androidx.constraintlayout.coremp.state.helpers.GuidelineReference
import androidx.constraintlayout.coremp.state.helpers.HorizontalChainReference
import androidx.constraintlayout.coremp.state.helpers.VerticalChainReference
import androidx.constraintlayout.coremp.widgets.ConstraintWidget
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.CHAIN_PACKED
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.CHAIN_SPREAD
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.CHAIN_SPREAD_INSIDE
import androidx.constraintlayout.coremp.widgets.ConstraintWidgetContainer
import kotlin.math.roundToInt

open class State {
    private var mDpToPixel: CorePixelDp? = null
    private var mIsLtr = true
    protected var mReferences = HashMap<Any, Reference>()
    protected var mHelperReferences = HashMap<Any, HelperReference>()
    var mTags = HashMap<String, ArrayList<String>>()

    val mParent: ConstraintReference = ConstraintReference(this)

    enum class Constraint {
        LEFT_TO_LEFT,
        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT,
        RIGHT_TO_RIGHT,
        START_TO_START,
        START_TO_END,
        END_TO_START,
        END_TO_END,
        TOP_TO_TOP,
        TOP_TO_BOTTOM,
        TOP_TO_BASELINE,
        BOTTOM_TO_TOP,
        BOTTOM_TO_BOTTOM,
        BOTTOM_TO_BASELINE,
        BASELINE_TO_BASELINE,
        BASELINE_TO_TOP,
        BASELINE_TO_BOTTOM,
        CENTER_HORIZONTALLY,
        CENTER_VERTICALLY,
        CIRCULAR_CONSTRAINT,
    }

    enum class Direction {
        LEFT,
        RIGHT,
        START,
        END,
        TOP,
        BOTTOM,
    }

    enum class Helper {
        HORIZONTAL_CHAIN,
        VERTICAL_CHAIN,
        ALIGN_HORIZONTALLY,
        ALIGN_VERTICALLY,
        BARRIER,
        LAYER,
        HORIZONTAL_FLOW,
        VERTICAL_FLOW,
        GRID,
        ROW,
        COLUMN,
        FLOW,
    }

    enum class Chain {
        SPREAD,
        SPREAD_INSIDE,
        PACKED,
        ;

        companion object {
            val chainMap: Map<String, Chain> = hashMapOf(
                "packed" to PACKED,
                "spread_inside" to SPREAD_INSIDE,
                "spread" to SPREAD,
            )
            val valueMap: Map<String, Int> = hashMapOf(
                "packed" to CHAIN_PACKED,
                "spread_inside" to CHAIN_SPREAD_INSIDE,
                "spread" to CHAIN_SPREAD,
            )

            /**
             * Get the Enum value with a String
             * @param str a String representation of a Enum value
             * @return a Enum value
             */
            fun getValueByString(str: String?): Int {
                return if (valueMap.containsKey(str)) {
                    valueMap[str]!!
                } else {
                    UNKNOWN
                }
            }

            /**
             * Get the actual int value with a String
             * @param str a String representation of a Enum value
             * @return an actual int value
             */
            fun getChainByString(str: String?): Chain? {
                return if (chainMap.containsKey(str)) {
                    chainMap[str]
                } else {
                    null
                }
            }
        }
    }

    enum class Wrap {
        NONE,
        CHAIN,
        ALIGNED,
        ;

        companion object {
            val wrapMap: Map<String, Wrap> = hashMapOf(
                "none" to NONE,
                "chain" to CHAIN,
                "aligned" to ALIGNED,
            )
            val valueMap: Map<String, Int> = hashMapOf(
                "none" to 0,
                "chain" to 3,
                "aligned" to 2,
            )

            /**
             * Get the actual int value with a String
             * @param str a String representation of a Enum value
             * @return a actual int value
             */
            fun getValueByString(str: String?): Int {
                return if (valueMap.containsKey(str)) {
                    valueMap[str]!!
                } else {
                    UNKNOWN
                }
            }

            /**
             * Get the Enum value with a String
             * @param str a String representation of a Enum value
             * @return a Enum value
             */
            fun getChainByString(str: String?): Wrap? {
                return if (wrapMap.containsKey(str)) {
                    wrapMap[str]
                } else {
                    null
                }
            }
        }
    }

    constructor() {
        mParent.setKey(PARENT)
        mReferences[PARENT] = mParent
    }

    fun getDpToPixel(): CorePixelDp? {
        return mDpToPixel
    }

    /**
     * Set the function that converts dp to Pixels
     */
    fun setDpToPixel(dpToPixel: CorePixelDp?) {
        mDpToPixel = dpToPixel
    }

    /**
     * Set whether the layout direction is left to right (Ltr).
     *
     */
    @Deprecated("For consistency, use {@link #setRtl(boolean)} instead.")
    fun setLtr(isLtr: Boolean) {
        mIsLtr = isLtr
    }

    /**
     * Returns true if layout direction is left to right. False for right to left.
     *
     */
    @Deprecated("For consistency, use {@link #isRtl()} instead.")
    fun isLtr(): Boolean {
        return mIsLtr
    }

    var isRtl: Boolean
        get() = !mIsLtr
        set(value) {
            mIsLtr = !value
        }

    /**
     * Clear the state
     */
    fun reset() {
        for (ref in mReferences.keys) {
            mReferences[ref]!!.getConstraintWidget()!!.reset()
        }
        mReferences.clear()
        mReferences[PARENT] = mParent
        mHelperReferences.clear()
        mTags.clear()
        mBaselineNeeded.clear()
        mDirtyBaselineNeededWidgets = true
    }

    /**
     * Implements a conversion function for values, returning int.
     * This can be used in case values (e.g. margins) are represented
     * via an object, not directly an int.
     *
     * @param value the object to convert from
     */
    open fun convertDimension(value: Any?): Int {
        if (value is Float) {
            return value.roundToInt()
        }
        return if (value is Int) {
            value
        } else {
            0
        }
    }

    /**
     * Create a new reference given a key.
     */
    fun createConstraintReference(key: Any?): ConstraintReference {
        return ConstraintReference(this)
    }

    // @TODO: add description
    fun sameFixedWidth(width: Int): Boolean {
        return mParent.width.equalsFixedValue(width)
    }

    // @TODO: add description
    fun sameFixedHeight(height: Int): Boolean {
        return mParent.height.equalsFixedValue(height)
    }

    // @TODO: add description
    fun width(dimension: Dimension): State {
        return setWidth(dimension)
    }

    // @TODO: add description
    fun height(dimension: Dimension): State {
        return setHeight(dimension)
    }

    // @TODO: add description
    fun setWidth(dimension: Dimension): State {
        mParent.setWidth(dimension)
        return this
    }

    // @TODO: add description
    fun setHeight(dimension: Dimension): State {
        mParent.setHeight(dimension)
        return this
    }

    fun reference(key: Any?): Reference? {
        return mReferences[key]
    }

    // @TODO: add description
    fun constraints(key: Any?): ConstraintReference? {
        var reference = mReferences[key]
        if (reference == null) {
            reference = createConstraintReference(key)
            mReferences[key!!] = reference
            reference.setKey(key)
        }
        return if (reference is ConstraintReference) {
            reference
        } else {
            null
        }
    }

    private var mNumHelpers = 0

    private fun createHelperKey(): String {
        return "__HELPER_KEY_" + mNumHelpers++ + "__"
    }

    // @TODO: add description
    fun helper(key: Any?, type: Helper?): HelperReference {
        var key = key
        if (key == null) {
            key = createHelperKey()
        }
        var reference = mHelperReferences[key]
        if (reference == null) {
            when (type) {
                HORIZONTAL_CHAIN -> {
                    reference = HorizontalChainReference(this)
                }

                VERTICAL_CHAIN -> {
                    reference = VerticalChainReference(this)
                }

                ALIGN_HORIZONTALLY -> {
                    reference = AlignHorizontallyReference(this)
                }

                ALIGN_VERTICALLY -> {
                    reference = AlignVerticallyReference(this)
                }

                BARRIER -> {
                    reference = BarrierReference(this)
                }

                VERTICAL_FLOW, HORIZONTAL_FLOW -> {
                    reference = FlowReference(this, type)
                }

                GRID, ROW, COLUMN -> {
                    reference = GridReference(this, type)
                }

                else -> {
                    reference = HelperReference(this, type!!)
                }
            }
            reference.setKey(key)
            mHelperReferences[key] = reference
        }
        return reference
    }

    // @TODO: add description
    fun horizontalGuideline(key: Any?): GuidelineReference? {
        return guideline(key, ConstraintWidget.HORIZONTAL)
    }

    // @TODO: add description
    fun verticalGuideline(key: Any?): GuidelineReference? {
        return guideline(key, ConstraintWidget.VERTICAL)
    }

    // @TODO: add description
    fun guideline(key: Any?, orientation: Int): GuidelineReference? {
        val reference: ConstraintReference? = constraints(key)
        if (reference?.getFacade() == null ||
            reference.getFacade() !is GuidelineReference
        ) {
            val guidelineReference = GuidelineReference(this)
            guidelineReference.setOrientation(orientation)
            guidelineReference.setKey(key)
            reference?.setFacade(guidelineReference)
        }
        return reference?.getFacade() as GuidelineReference?
    }

    // @TODO: add description
    fun barrier(key: Any?, direction: Direction): BarrierReference? {
        val reference: ConstraintReference? = constraints(key)
        if (reference?.getFacade() == null || reference.getFacade() !is BarrierReference) {
            val barrierReference = BarrierReference(this)
            barrierReference.setBarrierDirection(direction)
            reference?.setFacade(barrierReference)
        }
        return reference?.getFacade() as BarrierReference?
    }

    /**
     * Get a Grid reference
     *
     * @param key name of the reference object
     * @param gridType type of Grid pattern - Grid, Row, or Column
     * @return a GridReference object
     */
    fun getGrid(key: Any, gridType: String): GridReference? {
        val reference: ConstraintReference? = constraints(key)
        if (reference?.getFacade() == null || reference.getFacade() !is GridReference) {
            var Type = GRID
            if (gridType[0] == 'r') {
                Type = ROW
            } else if (gridType[0] == 'c') {
                Type = COLUMN
            }
            val gridReference = GridReference(this, Type)
            reference?.setFacade(gridReference)
        }
        return reference?.getFacade() as GridReference?
    }

    /**
     * Gets a reference to a Flow object. Creating it if needed.
     * @param key id of the reference
     * @param vertical is it a vertical or horizontal flow
     * @return a FlowReference
     */
    fun getFlow(key: Any?, vertical: Boolean): FlowReference? {
        val reference: ConstraintReference? = constraints(key)
        if (reference?.getFacade() == null || reference.getFacade() !is FlowReference) {
            val flowReference: FlowReference =
                if (vertical) {
                    FlowReference(this, VERTICAL_FLOW)
                } else {
                    FlowReference(
                        this,
                        HORIZONTAL_FLOW,
                    )
                }
            reference?.setFacade(flowReference)
        }
        return reference?.getFacade() as FlowReference?
    }

    // @TODO: add description
    fun verticalChain(): VerticalChainReference {
        return helper(null, VERTICAL_CHAIN) as VerticalChainReference
    }

    // @TODO: add description
    fun verticalChain(vararg references: Any?): VerticalChainReference {
        val reference: VerticalChainReference =
            helper(null, VERTICAL_CHAIN) as VerticalChainReference
        reference.add(references)
        return reference
    }

    // @TODO: add description
    fun horizontalChain(): HorizontalChainReference {
        return helper(null, HORIZONTAL_CHAIN) as HorizontalChainReference
    }

    // @TODO: add description
    fun horizontalChain(vararg references: Any?): HorizontalChainReference {
        val reference: HorizontalChainReference =
            helper(null, HORIZONTAL_CHAIN) as HorizontalChainReference
        reference.add(references)
        return reference
    }

    /**
     * Get a VerticalFlowReference
     *
     * @return a VerticalFlowReference
     */
    fun getVerticalFlow(): FlowReference {
        return helper(null, VERTICAL_FLOW) as FlowReference
    }

    /**
     * Get a VerticalFlowReference and add it to references
     *
     * @param references where we add the VerticalFlowReference
     * @return a VerticalFlowReference
     */
    fun getVerticalFlow(vararg references: Any?): FlowReference {
        val reference: FlowReference = helper(null, VERTICAL_FLOW) as FlowReference
        reference.add(references)
        return reference
    }

    /**
     * Get a HorizontalFlowReference
     *
     * @return a HorizontalFlowReference
     */
    fun getHorizontalFlow(): FlowReference {
        return helper(null, HORIZONTAL_FLOW) as FlowReference
    }

    /**
     * Get a HorizontalFlowReference and add it to references
     *
     * @param references references where we the HorizontalFlowReference
     * @return a HorizontalFlowReference
     */
    fun getHorizontalFlow(vararg references: Any?): FlowReference {
        val reference: FlowReference = helper(null, HORIZONTAL_FLOW) as FlowReference
        reference.add(references)
        return reference
    }

    // @TODO: add description
    fun centerHorizontally(vararg references: Any?): AlignHorizontallyReference {
        val reference = helper(null, ALIGN_HORIZONTALLY) as AlignHorizontallyReference
        reference.add(references)
        return reference
    }

    // @TODO: add description
    fun centerVertically(vararg references: Any?): AlignVerticallyReference {
        val reference: AlignVerticallyReference =
            helper(null, ALIGN_VERTICALLY) as AlignVerticallyReference
        reference.add(references)
        return reference
    }

    // @TODO: add description
    fun directMapping() {
        for (key in mReferences.keys) {
            val ref: Reference =
                constraints(key) ?: continue
            val reference: ConstraintReference = ref as ConstraintReference
            reference.setView(key)
        }
    }

    // @TODO: add description
    fun map(key: Any?, view: Any) {
        val ref: ConstraintReference? = constraints(key)
        if (ref != null) {
            ref.setView(view)
        }
    }

    // @TODO: add description
    fun setTag(key: String, tag: String) {
        val ref: Reference? = constraints(key)
        if (ref is ConstraintReference) {
            val reference: ConstraintReference = ref
            reference.setTag(tag)
            var list: ArrayList<String>? = null
            if (!mTags.containsKey(tag)) {
                list = ArrayList()
                mTags[tag] = list
            } else {
                list = mTags[tag]
            }
            list!!.add(key)
        }
    }

    // @TODO: add description
    fun getIdsForTag(tag: String?): ArrayList<String>? {
        return if (mTags.containsKey(tag)) {
            mTags[tag]
        } else {
            null
        }
    }

    // @TODO: add description
    fun apply(container: ConstraintWidgetContainer) {
        container.removeAllChildren()
        mParent.width.apply(this, container, ConstraintWidget.HORIZONTAL)
        mParent.height.apply(this, container, ConstraintWidget.VERTICAL)
        // add helper references
        for (key in mHelperReferences.keys) {
            val reference = mHelperReferences[key]
            val helperWidget = reference?.helperWidget
            if (helperWidget != null) {
                var constraintReference = mReferences[key]
                if (constraintReference == null) {
                    constraintReference = constraints(key)
                }
                constraintReference!!.setConstraintWidget(helperWidget)
            }
        }
        for (key in mReferences.keys) {
            val reference = mReferences[key]
            if (reference != mParent && reference!!.getFacade() is HelperReference) {
                val helperWidget = (reference.getFacade() as HelperReference?)?.helperWidget
                if (helperWidget != null) {
                    var constraintReference = mReferences[key]
                    if (constraintReference == null) {
                        constraintReference = constraints(key)
                    }
                    constraintReference!!.setConstraintWidget(helperWidget)
                }
            }
        }
        for (key in mReferences.keys) {
            val reference = mReferences[key]
            if (reference != mParent) {
                val widget = reference!!.getConstraintWidget()
                widget!!.debugName = reference.getKey().toString()
                widget.parent = null
                if (reference.getFacade() is GuidelineReference) {
                    // we apply Guidelines first to correctly setup their ConstraintWidget.
                    reference.apply()
                }
                container.add(widget)
            } else {
                reference.setConstraintWidget(container)
            }
        }
        for (key in mHelperReferences.keys) {
            // We need this pass to apply chains properly
            val reference = mHelperReferences[key]
            val helperWidget = reference?.helperWidget
            if (helperWidget != null) {
                for (keyRef in reference.mReferences) {
                    val constraintReference = mReferences[keyRef]
                    reference.helperWidget!!.add(constraintReference!!.getConstraintWidget())
                }
                reference.apply()
            } else {
                reference?.apply()
            }
        }
        for (key in mReferences.keys) {
            val reference = mReferences[key]
            if (reference != mParent && reference!!.getFacade() is HelperReference) {
                val helperReference = reference.getFacade() as HelperReference?
                val helperWidget = helperReference?.helperWidget
                if (helperWidget != null) {
                    for (keyRef in helperReference.mReferences) {
                        val constraintReference = mReferences[keyRef]
                        if (constraintReference != null) {
                            helperWidget.add(constraintReference.getConstraintWidget())
                        } else if (keyRef is Reference) {
                            helperWidget.add(keyRef.getConstraintWidget())
                        } else {
                            println("couldn't find reference for $keyRef")
                        }
                    }
                    reference.apply()
                }
            }
        }
        for (key: Any? in mReferences.keys) {
            val reference = mReferences[key]
            reference!!.apply()
            val widget = reference.getConstraintWidget()
            if (widget != null && key != null) {
                widget.stringId = key.toString()
            }
        }
    }

    // ================= add baseline code================================
    var mBaselineNeeded = ArrayList<Any>()
    var mBaselineNeededWidgets = ArrayList<ConstraintWidget>()
    var mDirtyBaselineNeededWidgets = true

    /**
     * Baseline is needed for this object
     */
    fun baselineNeededFor(id: Any) {
        mBaselineNeeded.add(id)
        mDirtyBaselineNeededWidgets = true
    }

    /**
     * Does this constraintWidget need a baseline
     *
     * @return true if the constraintWidget needs a baseline
     */
    fun isBaselineNeeded(constraintWidget: ConstraintWidget?): Boolean {
        if (mDirtyBaselineNeededWidgets) {
            mBaselineNeededWidgets.clear()
            for (id in mBaselineNeeded) {
                val widget = mReferences[id]!!.getConstraintWidget()
                if (widget != null) mBaselineNeededWidgets.add(widget)
            }
            mDirtyBaselineNeededWidgets = false
        }
        return mBaselineNeededWidgets.contains(constraintWidget)
    }

    companion object {
        const val UNKNOWN = -1
        const val CONSTRAINT_SPREAD = 0
        const val CONSTRAINT_WRAP = 1
        const val CONSTRAINT_RATIO = 2

        const val PARENT = 0
    }
}
