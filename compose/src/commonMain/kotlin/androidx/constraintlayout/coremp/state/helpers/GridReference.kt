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
package androidx.constraintlayout.coremp.state.helpers

import androidx.constraintlayout.coremp.state.HelperReference
import androidx.constraintlayout.coremp.state.State
import androidx.constraintlayout.coremp.utils.GridCore
import androidx.constraintlayout.coremp.widgets.HelperWidget

class GridReference : HelperReference {

    constructor(state: State, type: State.Helper) : super(state, type) {
        if (type == State.Helper.ROW) {
            this.mRowsSet = 1
        } else if (type == State.Helper.COLUMN) {
            this.mColumnsSet = 1
        }
    }

    /**
     * The Grid Object
     */
    private var mGrid: GridCore? = null

    /**
     * padding start
     */
    private var mPaddingStart = 0

    /**
     * padding end
     */
    private var mPaddingEnd = 0

    /**
     * padding top
     */
    private var mPaddingTop = 0

    /**
     * padding bottom
     */
    private var mPaddingBottom = 0

    /**
     * The orientation of the widgets arrangement horizontally or vertically
     */
    private var mOrientation = 0

    /**
     * Number of rows of the Grid
     */
    private var mRowsSet = 0

    /**
     * Number of columns of the Grid
     */
    private var mColumnsSet = 0

    /**
     * The horizontal gaps between widgets
     */
    private var mHorizontalGaps = 0f

    /**
     * The vertical gaps between widgets
     */
    private var mVerticalGaps = 0f

    /**
     * The weight of each widget in a row
     */
    private var mRowWeights: String? = null

    /**
     * The weight of each widget in a column
     */
    private var mColumnWeights: String? = null

    /**
     * Specify the spanned areas of widgets
     */
    private var mSpans: String? = null

    /**
     * Specify the positions to be skipped in the Grid
     */
    private var mSkips: String? = null

    /**
     * All the flags of a Grid
     */
    private var mFlags: IntArray? = null

    /**
     * get padding left
     * @return padding left
     */
    fun getPaddingStart(): Int {
        return mPaddingStart
    }

    /**
     * set padding left
     * @param paddingStart padding left to be set
     */
    fun setPaddingStart(paddingStart: Int) {
        mPaddingStart = paddingStart
    }

    /**
     * get padding right
     * @return padding right
     */
    fun getPaddingEnd(): Int {
        return mPaddingEnd
    }

    /**
     * set padding right
     * @param paddingEnd padding right to be set
     */
    fun setPaddingEnd(paddingEnd: Int) {
        mPaddingEnd = paddingEnd
    }

    /**
     * get padding top
     * @return padding top
     */
    fun getPaddingTop(): Int {
        return mPaddingTop
    }

    /**
     * set padding top
     * @param paddingTop padding top to be set
     */
    fun setPaddingTop(paddingTop: Int) {
        mPaddingTop = paddingTop
    }

    /**
     * get padding bottom
     * @return padding bottom
     */
    fun getPaddingBottom(): Int {
        return mPaddingBottom
    }

    /**
     * set padding bottom
     * @param paddingBottom padding bottom to be set
     */
    fun setPaddingBottom(paddingBottom: Int) {
        mPaddingBottom = paddingBottom
    }

    /**
     * Get all the flags of a Grid
     * @return a String array containing all the flags
     */
    fun getFlags(): IntArray {
        return mFlags!!
    }

    /**
     * Set flags of a Grid
     * @param flags a String array containing all the flags
     */
    fun setFlags(flags: IntArray) {
        mFlags = flags
    }

    /**
     * Set flags of a Grid
     * @param flags a String containing all the flags
     */
    fun setFlags(flags: String) {
        if (flags.isEmpty()) {
            return
        }
        val strArr = flags.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val flagList = ArrayList<Int>()
        for (flag in strArr) {
            when (flag.lowercase()) {
                SUB_GRID_BY_COL_ROW -> flagList.add(0)
                SPANS_RESPECT_WIDGET_ORDER -> flagList.add(1)
            }
        }
        val flagArr = IntArray(flagList.size)
        var i = 0
        for (flag in flagList) {
            flagArr[i++] = flag
        }
        mFlags = flagArr
    }

    /**
     * Get the number of rows
     * @return the number of rows
     */
    fun getRowsSet(): Int {
        return mRowsSet
    }

    /**
     * Set the number of rows
     * @param rowsSet the number of rows
     */
    fun setRowsSet(rowsSet: Int) {
        if (super.getType() == State.Helper.COLUMN) {
            return
        }
        mRowsSet = rowsSet
    }

    /**
     * Get the number of columns
     * @return the number of columns
     */
    fun getColumnsSet(): Int {
        return mColumnsSet
    }

    /**
     * Set the number of columns
     * @param columnsSet the number of columns
     */
    fun setColumnsSet(columnsSet: Int) {
        if (super.getType() == State.Helper.ROW) {
            return
        }
        mColumnsSet = columnsSet
    }

    /**
     * Get the horizontal gaps
     * @return the horizontal gaps
     */
    fun getHorizontalGaps(): Float {
        return mHorizontalGaps
    }

    /**
     * Set the horizontal gaps
     * @param horizontalGaps the horizontal gaps
     */
    fun setHorizontalGaps(horizontalGaps: Float) {
        mHorizontalGaps = horizontalGaps
    }

    /**
     * Get the vertical gaps
     * @return the vertical gaps
     */
    fun getVerticalGaps(): Float {
        return mVerticalGaps
    }

    /**
     * Set the vertical gaps
     * @param verticalGaps  the vertical gaps
     */
    fun setVerticalGaps(verticalGaps: Float) {
        mVerticalGaps = verticalGaps
    }

    /**
     * Get the row weights
     * @return the row weights
     */
    fun getRowWeights(): String? {
        return mRowWeights
    }

    /**
     * Set the row weights
     * @param rowWeights the row weights
     */
    fun setRowWeights(rowWeights: String) {
        mRowWeights = rowWeights
    }

    /**
     * Get the column weights
     * @return the column weights
     */
    fun getColumnWeights(): String? {
        return mColumnWeights
    }

    /**
     * Set the column weights
     * @param columnWeights the column weights
     */
    fun setColumnWeights(columnWeights: String) {
        mColumnWeights = columnWeights
    }

    /**
     * Get the spans
     * @return the spans
     */
    fun getSpans(): String? {
        return mSpans
    }

    /**
     * Set the spans
     * @param spans the spans
     */
    fun setSpans(spans: String) {
        mSpans = spans
    }

    /**
     * Get the skips
     * @return the skips
     */
    fun getSkips(): String? {
        return mSkips
    }

    /**
     * Set the skips
     * @param skips the skips
     */
    fun setSkips(skips: String) {
        mSkips = skips
    }

    /**
     * Get the helper widget (Grid)
     * @return the helper widget (Grid)
     */

    override val helperWidget: HelperWidget
        get() {
            if (mGrid == null) {
                mGrid = GridCore()
            }
            return mGrid!!
        }

    /**
     * Set the helper widget (Grid)
     * @param widget the helper widget (Grid)
     */
    override fun setHelperWidget(widget: HelperWidget) {
        mGrid = if (widget is GridCore) {
            widget
        } else {
            null
        }
    }

    /**
     * Get the Orientation
     * @return the Orientation
     */
    fun getOrientation(): Int {
        return mOrientation
    }

    /**
     * Set the Orientation
     * @param orientation the Orientation
     */
    fun setOrientation(orientation: Int) {
        mOrientation = orientation
    }

    /**
     * Apply all the attributes to the helper widget (Grid)
     */
    override fun apply() {
        helperWidget
        mGrid!!.setOrientation(mOrientation)
        if (mRowsSet != 0) {
            mGrid!!.setRows(mRowsSet)
        }
        if (mColumnsSet != 0) {
            mGrid!!.setColumns(mColumnsSet)
        }
        if (mHorizontalGaps != 0f) {
            mGrid!!.setHorizontalGaps(mHorizontalGaps)
        }
        if (mVerticalGaps != 0f) {
            mGrid!!.setVerticalGaps(mVerticalGaps)
        }
        if (mRowWeights != null && mRowWeights != "") {
            mGrid!!.setRowWeights(mRowWeights!!)
        }
        if (mColumnWeights != null && mColumnWeights != "") {
            mGrid!!.setColumnWeights(mColumnWeights!!)
        }
        if (mSpans != null && mSpans != "") {
            mGrid!!.setSpans(mSpans!!)
        }
        if (mSkips != null && mSkips != "") {
            mGrid!!.setSkips(mSkips!!)
        }
        if (mFlags != null && mFlags!!.isNotEmpty()) {
            mGrid!!.setFlags(mFlags!!)
        }

        // General attributes of a widget
        applyBase()
    }

    companion object {
        private const val SPANS_RESPECT_WIDGET_ORDER = "spansrespectwidgetorder"
        private const val SUB_GRID_BY_COL_ROW = "subgridbycolrow"
    }
}
