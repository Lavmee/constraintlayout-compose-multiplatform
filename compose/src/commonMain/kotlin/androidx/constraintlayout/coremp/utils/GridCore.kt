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
package androidx.constraintlayout.coremp.utils

import androidx.constraintlayout.coremp.LinearSystem
import androidx.constraintlayout.coremp.widgets.ConstraintWidget
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT
import androidx.constraintlayout.coremp.widgets.ConstraintWidgetContainer
import androidx.constraintlayout.coremp.widgets.VirtualLayout
import kotlin.collections.HashSet
import kotlin.math.max
import kotlin.math.sqrt

@Suppress("UNUSED")
class GridCore() : VirtualLayout() {
    /**
     * Container for all the ConstraintWidgets
     */
    private var mContainer: ConstraintWidgetContainer? = null

    /**
     * boxWidgets were created as anchor points for arranging the associated widgets
     */
    private var mBoxWidgets: Array<ConstraintWidget?>? = null

    /**
     * Check if skips/spans of a Row or a Columns is handled
     */
    private var mExtraSpaceHandled = false

    /**
     * number of rows of the grid
     */
    private var mRows = 0

    /**
     * number of rows set by the JSON or API
     */
    private var mRowsSet = 0

    /**
     * number of columns of the grid
     */
    private var mColumns = 0

    /**
     * number of columns set by the XML or API
     */
    private var mColumnsSet = 0

    /**
     * Horizontal gaps in Dp
     */
    private var mHorizontalGaps = 0f

    /**
     * Vertical gaps in Dp
     */
    private var mVerticalGaps = 0f

    /**
     * string format of the row weight
     */
    private var mRowWeights: String? = null

    /**
     * string format of the column weight
     */
    private var mColumnWeights: String? = null

    /**
     * string format of the input Spans
     */
    private var mSpans: String? = null

    /**
     * string format of the input Skips
     */
    private var mSkips: String? = null

    /**
     * orientation of the widget arrangement - vertical or horizontal
     */
    private var mOrientation = 0

    /**
     * Indicates what is the next available position to place a widget
     */
    private var mNextAvailableIndex = 0

    /**
     * A boolean matrix that tracks the positions that are occupied by skips and spans
     * true: available position
     * false: non-available position
     */
    private var mPositionMatrix: Array<BooleanArray>? = null

    /**
     * Store the widget ids of handled spans
     */
    private var mSpanIds: MutableSet<String> = HashSet()

    /**
     * A int matrix that contains the positions where a widget would constraint to at each direction
     * Each row contains 4 values that indicate the position to constraint of a widget.
     * Example row: [left, top, right, bottom]
     */
    private var mConstraintMatrix: Array<IntArray>? = null

    /**
     * A String array stores the flags
     */
    private var mFlags: IntArray? = null

    /**
     * A int matrix to store the span related information
     */
    private var mSpanMatrix: Array<IntArray>? = null

    /**
     * Index specify the next span to be handled.
     */
    private var mSpanIndex = 0

    /**
     * Flag to respect the order of the Widgets when arranging for span
     */
    private var mSpansRespectWidgetOrder = false

    /**
     * Flag to reverse the order of width/height specified in span
     * e.g., 1:3x2 -> 1:2x3
     */
    private var mSubGridByColRow = false

    constructor(rows: Int, columns: Int) : this() {
        mRowsSet = rows
        mColumnsSet = columns
        if (rows > MAX_ROWS) {
            mRowsSet = DEFAULT_SIZE
        }

        if (columns > MAX_COLUMNS) {
            mColumnsSet = DEFAULT_SIZE
        }
    }

    init {
        updateActualRowsAndColumns()
        initMatrices()
    }

    /**
     * get the parent ConstraintWidgetContainer
     *
     * @return the parent ConstraintWidgetContainer
     */
    fun getContainer(): ConstraintWidgetContainer? {
        return mContainer
    }

    /**
     * Set the parent ConstraintWidgetContainer
     * @param container the parent ConstraintWidgetContainer
     */
    fun setContainer(container: ConstraintWidgetContainer) {
        mContainer = container
    }

    /**
     * set new spans value
     *
     * @param spans new spans value
     */
    fun setSpans(spans: CharSequence) {
        if (mSpans != null && mSpans == spans.toString()) {
            return
        }
        mExtraSpaceHandled = false
        mSpans = spans.toString()
    }

    /**
     * set new skips value
     *
     * @param skips new spans value
     */
    fun setSkips(skips: String) {
        if (mSkips != null && mSkips == skips) {
            return
        }
        mExtraSpaceHandled = false
        mSkips = skips
    }

    /**
     * get the value of horizontalGaps
     *
     * @return the value of horizontalGaps
     */
    fun getHorizontalGaps(): Float {
        return mHorizontalGaps
    }

    /**
     * set new horizontalGaps value and also invoke invalidate
     *
     * @param horizontalGaps new horizontalGaps value
     */
    fun setHorizontalGaps(horizontalGaps: Float) {
        if (horizontalGaps < 0) {
            return
        }
        if (mHorizontalGaps == horizontalGaps) {
            return
        }
        mHorizontalGaps = horizontalGaps
    }

    /**
     * get the value of verticalGaps
     *
     * @return the value of verticalGaps
     */
    fun getVerticalGaps(): Float {
        return mVerticalGaps
    }

    /**
     * set new verticalGaps value and also invoke invalidate
     *
     * @param verticalGaps new verticalGaps value
     */
    fun setVerticalGaps(verticalGaps: Float) {
        if (verticalGaps < 0) {
            return
        }
        if (mVerticalGaps == verticalGaps) {
            return
        }
        mVerticalGaps = verticalGaps
    }

    /**
     * get the string value of rowWeights
     *
     * @return the string value of rowWeights
     */
    fun getRowWeights(): String? {
        return mRowWeights
    }

    /**
     * set new rowWeights value and also invoke invalidate
     *
     * @param rowWeights new rowWeights value
     */
    fun setRowWeights(rowWeights: String) {
        if (mRowWeights != null && mRowWeights == rowWeights) {
            return
        }
        mRowWeights = rowWeights
    }

    /**
     * get the string value of columnWeights
     *
     * @return the string value of columnWeights
     */
    fun getColumnWeights(): String? {
        return mColumnWeights
    }

    /**
     * set new columnWeights value and also invoke invalidate
     *
     * @param columnWeights new columnWeights value
     */
    fun setColumnWeights(columnWeights: String) {
        if (mColumnWeights != null && mColumnWeights == columnWeights) {
            return
        }
        mColumnWeights = columnWeights
    }

    /**
     * get the value of orientation
     *
     * @return the value of orientation
     */
    fun getOrientation(): Int {
        return mOrientation
    }

    /**
     * set new orientation value
     *
     * @param orientation new orientation value
     */
    fun setOrientation(orientation: Int) {
        if (!(orientation == ConstraintWidget.HORIZONTAL || orientation == ConstraintWidget.VERTICAL)) {
            return
        }
        if (mOrientation == orientation) {
            return
        }
        mOrientation = orientation
    }

    /**
     * set new rows value
     *
     * @param rows new rows value
     */
    fun setRows(rows: Int) {
        if (rows > MAX_ROWS) {
            return
        }
        if (mRowsSet == rows) {
            return
        }
        mRowsSet = rows
        updateActualRowsAndColumns()
        initVariables()
    }

    /**
     * set new columns value
     *
     * @param columns new rows value
     */
    fun setColumns(columns: Int) {
        if (columns > MAX_COLUMNS) {
            return
        }
        if (mColumnsSet == columns) {
            return
        }
        mColumnsSet = columns
        updateActualRowsAndColumns()
        initVariables()
    }

    /**
     * Get all the flags of a Grid
     * @return a int array containing all the flags
     */
    fun getFlags(): IntArray? {
        return mFlags
    }

    /**
     * Set flags of a Grid
     * @param flags a int array containing all the flags
     */
    fun setFlags(flags: IntArray) {
        mFlags = flags
    }

    /**
     * Handle the span use cases
     *
     * @param spansMatrix a int matrix that contains span information
     */
    private fun handleSpans(spansMatrix: Array<IntArray>) {
        if (mSpansRespectWidgetOrder) {
            return
        }
        for (i in spansMatrix.indices) {
            val row: Int = getRowByIndex(spansMatrix[i][0])
            val col: Int = getColByIndex(spansMatrix[i][0])
            if (!invalidatePositions(
                    row,
                    col,
                    spansMatrix[i][1],
                    spansMatrix[i][2],
                )
            ) {
                return
            }
            connectWidget(
                mWidgets[i]!!,
                row,
                col,
                spansMatrix[i][1],
                spansMatrix[i][2],
            )
            mSpanIds.add(mWidgets[i]!!.stringId!!)
        }
    }

    /**
     * Arrange the widgets in the constraint_referenced_ids
     */
    private fun arrangeWidgets() {
        var position: Int

        // @TODO handle RTL
        for (i in 0 until mWidgetsCount) {
            if (mSpanIds.contains(mWidgets[i]!!.stringId)) {
                // skip the widget Id that's already handled by handleSpans
                continue
            }
            position = getNextPosition()
            val row: Int = getRowByIndex(position)
            val col: Int = getColByIndex(position)
            if (position == -1) {
                // no more available position.
                return
            }
            if (mSpansRespectWidgetOrder && mSpanMatrix != null) {
                if (mSpanIndex < mSpanMatrix!!.size && mSpanMatrix!![mSpanIndex][0] == position) {
                    // when invoke getNextPosition this position would be set to false
                    mPositionMatrix!![row][col] = true
                    // if there is not enough space to constrain the span, don't do it.
                    if (!invalidatePositions(
                            row,
                            col,
                            mSpanMatrix!![mSpanIndex][1],
                            mSpanMatrix!![mSpanIndex][2],
                        )
                    ) {
                        continue
                    }
                    connectWidget(
                        mWidgets[i]!!,
                        row,
                        col,
                        mSpanMatrix!![mSpanIndex][1],
                        mSpanMatrix!![mSpanIndex][2],
                    )
                    mSpanIndex++
                    continue
                }
            }
            connectWidget(mWidgets[i]!!, row, col, 1, 1)
        }
    }

    /**
     * generate the Grid form based on the input attributes
     *
     * @param isUpdate whether to update the existing grid (true) or create a new one (false)
     */
    private fun setupGrid(isUpdate: Boolean) {
        if (mRows < 1 || mColumns < 1) {
            return
        }
        handleFlags()
        if (isUpdate) {
            for (i in 0 until mPositionMatrix!!.size) {
                for (j in 0 until mPositionMatrix!![0].size) {
                    mPositionMatrix!![i][j] = true
                }
            }
            mSpanIds.clear()
        }
        mNextAvailableIndex = 0
        if (mSkips != null && mSkips!!.trim { it <= ' ' }.isNotEmpty()) {
            val mSkips: Array<IntArray>? = parseSpans(mSkips!!, false)
            if (mSkips != null) {
                handleSkips(mSkips)
            }
        }
        if (mSpans != null && mSpans!!.trim { it <= ' ' }.isNotEmpty()) {
            mSpanMatrix = parseSpans(mSpans!!, true)
        }

        // Need to create boxes before handleSpans since the spanned widgets would be
        // constrained in this step.
        createBoxes()
        if (mSpanMatrix != null) {
            handleSpans(mSpanMatrix!!)
        }
    }

    /**
     * Convert a 1D index to a 2D index that has index for row
     *
     * @param index index in 1D
     * @return row as its values.
     */
    private fun getRowByIndex(index: Int): Int {
        return if (mOrientation == 1) {
            index % mRows
        } else {
            index / mColumns
        }
    }

    /**
     * Convert a 1D index to a 2D index that has index for column
     *
     * @param index index in 1D
     * @return column as its values.
     */
    private fun getColByIndex(index: Int): Int {
        return if (mOrientation == 1) {
            index / mRows
        } else {
            index % mColumns
        }
    }

    /**
     * Make positions in the grid unavailable based on the skips attr
     *
     * @param skipsMatrix a int matrix that contains skip information
     */
    private fun handleSkips(skipsMatrix: Array<IntArray>) {
        for (matrix in skipsMatrix) {
            val row = getRowByIndex(matrix[0])
            val col = getColByIndex(matrix[0])
            if (!invalidatePositions(
                    row,
                    col,
                    matrix[1],
                    matrix[2],
                )
            ) {
                return
            }
        }
    }

    /**
     * Make the specified positions in the grid unavailable.
     *
     * @param startRow the row of the staring position
     * @param startColumn the column of the staring position
     * @param rowSpan how many rows to span
     * @param columnSpan how many columns to span
     * @return true if we could properly invalidate the positions else false
     */
    private fun invalidatePositions(
        startRow: Int,
        startColumn: Int,
        rowSpan: Int,
        columnSpan: Int,
    ): Boolean {
        for (i in startRow until startRow + rowSpan) {
            for (j in startColumn until startColumn + columnSpan) {
                if (i >= mPositionMatrix!!.size || j >= mPositionMatrix!![0].size || !mPositionMatrix!![i][j]
                ) {
                    // the position is already occupied.
                    return false
                }
                mPositionMatrix!![i][j] = false
            }
        }
        return true
    }

    /**
     * parse the weights/pads in the string format into a float array
     *
     * @param size size of the return array
     * @param str  weights/pads in a string format
     * @return a float array with weights/pads values
     */
    private fun parseWeights(size: Int, str: String?): FloatArray? {
        if (str == null || str.trim { it <= ' ' }.isEmpty()) {
            return null
        }
        val values = str.split(",".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        if (values.size != size) {
            return null
        }
        val arr = FloatArray(size)
        for (i in arr.indices) {
            arr[i] = values[i].trim { it <= ' ' }.toFloat()
        }
        return arr
    }

    /**
     * Get the next available position for widget arrangement.
     * @return int[] -> [row, column]
     */
    private fun getNextPosition(): Int {
        var position = 0
        var positionFound = false
        while (!positionFound) {
            if (mNextAvailableIndex >= mRows * mColumns) {
                return -1
            }
            position = mNextAvailableIndex
            val row = getRowByIndex(mNextAvailableIndex)
            val col = getColByIndex(mNextAvailableIndex)
            if (mPositionMatrix!![row][col]) {
                mPositionMatrix!![row][col] = false
                positionFound = true
            }
            mNextAvailableIndex++
        }
        return position
    }

    /**
     * Compute the actual rows and columns given what was set
     * if 0,0 find the most square rows and columns that fits
     * if 0,n or n,0 scale to fit
     */
    private fun updateActualRowsAndColumns() {
        if (mRowsSet == 0 || mColumnsSet == 0) {
            if (mColumnsSet > 0) {
                mColumns = mColumnsSet
                mRows = (mWidgetsCount + mColumns - 1) / mColumnsSet // round up
            } else if (mRowsSet > 0) {
                mRows = mRowsSet
                mColumns = (mWidgetsCount + mRowsSet - 1) / mRowsSet // round up
            } else { // as close to square as possible favoring more rows
                mRows = (1.5 + sqrt(mWidgetsCount.toDouble())).toInt()
                mColumns = (mWidgetsCount + mRows - 1) / mRows
            }
        } else {
            mRows = mRowsSet
            mColumns = mColumnsSet
        }
    }

    /**
     * Create a new boxWidget for constraining widgets
     * @return the created boxWidget
     */
    private fun makeNewWidget(): ConstraintWidget {
        val widget = ConstraintWidget()
        widget.mListDimensionBehaviors[ConstraintWidget.HORIZONTAL] = MATCH_CONSTRAINT
        widget.mListDimensionBehaviors[ConstraintWidget.VERTICAL] = MATCH_CONSTRAINT
        widget.stringId = widget.hashCode().toString()
        return widget
    }

    /**
     * Connect the widget to the corresponding widgetBoxes based on the input params
     *
     * @param widget the widget that we want to add constraints to
     * @param row    row position to place the widget
     * @param column column position to place the widget
     */
    private fun connectWidget(
        widget: ConstraintWidget,
        row: Int,
        column: Int,
        rowSpan: Int,
        columnSpan: Int,
    ) {
        // Connect the 4 sides
        widget.mLeft.connect(mBoxWidgets!![column]!!.mLeft, 0)
        widget.mTop.connect(mBoxWidgets!![row]!!.mTop, 0)
        widget.mRight.connect(mBoxWidgets!![column + columnSpan - 1]!!.mRight, 0)
        widget.mBottom.connect(mBoxWidgets!![row + rowSpan - 1]!!.mBottom, 0)
    }

    /**
     * Set chain between boxWidget horizontally
     */
    private fun setBoxWidgetHorizontalChains() {
        val maxVal = max(mRows, mColumns)
        var widget = mBoxWidgets!![0]!!
        val columnWeights = parseWeights(mColumns, mColumnWeights)
        // chain all the widgets on the longer side (either horizontal or vertical)
        if (mColumns == 1) {
            clearHorizontalAttributes(widget)
            widget.mLeft.connect(mLeft, 0)
            widget.mRight.connect(mRight, 0)
            return
        }

        //  chains are grid <- box <-> box <-> box -> grid
        for (i in 0 until mColumns) {
            widget = mBoxWidgets!![i]!!
            clearHorizontalAttributes(widget)
            if (columnWeights != null) {
                widget.setHorizontalWeight(columnWeights[i])
            }
            if (i > 0) {
                widget.mLeft.connect(mBoxWidgets!![i - 1]!!.mRight, 0)
            } else {
                widget.mLeft.connect(mLeft, 0)
            }
            if (i < mColumns - 1) {
                widget.mRight.connect(mBoxWidgets!![i + 1]!!.mLeft, 0)
            } else {
                widget.mRight.connect(mRight, 0)
            }
            if (i > 0) {
                widget.mLeft.mMargin = mHorizontalGaps.toInt()
            }
        }
        // excess boxes are connected to grid those sides are not use
        // for efficiency they should be connected to parent
        for (i in mColumns until maxVal) {
            widget = mBoxWidgets!![i]!!
            clearHorizontalAttributes(widget)
            widget.mLeft.connect(mLeft, 0)
            widget.mRight.connect(mRight, 0)
        }
    }

    /**
     * Set chain between boxWidget vertically
     */
    private fun setBoxWidgetVerticalChains() {
        val maxVal = max(mRows, mColumns)
        var widget = mBoxWidgets!![0]!!
        val rowWeights = parseWeights(mRows, mRowWeights)
        // chain all the widgets on the longer side (either horizontal or vertical)
        if (mRows == 1) {
            clearVerticalAttributes(widget)
            widget.mTop.connect(mTop, 0)
            widget.mBottom.connect(mBottom, 0)
            return
        }

        // chains are constrained like this: grid <- box <-> box <-> box -> grid
        for (i in 0 until mRows) {
            widget = mBoxWidgets!![i]!!
            clearVerticalAttributes(widget)
            if (rowWeights != null) {
                widget.setVerticalWeight(rowWeights[i])
            }
            if (i > 0) {
                widget.mTop.connect(mBoxWidgets!![i - 1]!!.mBottom, 0)
            } else {
                widget.mTop.connect(mTop, 0)
            }
            if (i < mRows - 1) {
                widget.mBottom.connect(mBoxWidgets!![i + 1]!!.mTop, 0)
            } else {
                widget.mBottom.connect(mBottom, 0)
            }
            if (i > 0) {
                widget.mTop.mMargin = mVerticalGaps.toInt()
            }
        }

        // excess boxes are connected to grid those sides are not use
        // for efficiency they should be connected to parent
        for (i in mRows until maxVal) {
            widget = mBoxWidgets!![i]!!
            clearVerticalAttributes(widget)
            widget.mTop.connect(mTop, 0)
            widget.mBottom.connect(mBottom, 0)
        }
    }

    /**
     * Chains the boxWidgets and add constraints to the widgets
     */
    private fun addConstraints() {
        setBoxWidgetVerticalChains()
        setBoxWidgetHorizontalChains()
        arrangeWidgets()
    }

    /**
     * Create all the boxWidgets that will be used to constrain widgets
     */
    private fun createBoxes() {
        val boxCount = max(mRows, mColumns)
        if (mBoxWidgets == null) { // no box widgets build all
            mBoxWidgets = arrayOfNulls(boxCount)
            for (i in 0 until mBoxWidgets!!.size) {
                mBoxWidgets!![i] = makeNewWidget() // need to remove old Widgets
            }
        } else {
            if (boxCount != mBoxWidgets!!.size) {
                val temp = arrayOfNulls<ConstraintWidget>(boxCount)
                for (i in 0 until boxCount) {
                    if (i < mBoxWidgets!!.size) { // use old one
                        temp[i] = mBoxWidgets!![i]
                    } else { // make new one
                        temp[i] = makeNewWidget()
                    }
                }
                // remove excess
                for (j in boxCount until mBoxWidgets!!.size) {
                    val widget = mBoxWidgets!![j]
                    mContainer!!.remove(widget!!)
                }
                mBoxWidgets = temp
            }
        }
    }

    /**
     * Clear the vertical related attributes
     * @param widget widget that has the attributes to be cleared
     */
    private fun clearVerticalAttributes(widget: ConstraintWidget) {
        widget.setVerticalWeight(UNKNOWN.toFloat())
        widget.mTop.reset()
        widget.mBottom.reset()
        widget.mBaseline.reset()
    }

    /**
     * Clear the horizontal related attributes
     * @param widget widget that has the attributes to be cleared
     */
    private fun clearHorizontalAttributes(widget: ConstraintWidget) {
        widget.setHorizontalWeight(UNKNOWN.toFloat())
        widget.mLeft.reset()
        widget.mRight.reset()
    }

    /**
     * Initialize the relevant variables
     */
    private fun initVariables() {
        mPositionMatrix = Array(mRows) { BooleanArray(mColumns) }
        for (row in mPositionMatrix!!) {
            row.fill(true)
        }
        if (mWidgetsCount > 0) {
            mConstraintMatrix = Array(mWidgetsCount) { IntArray(4) }
            for (row in mConstraintMatrix!!) {
                row.fill(-1)
            }
        }
    }

    /**
     * parse the skips/spans in the string format into a int matrix
     * that each row has the information - [index, row_span, col_span]
     * the format of the input string is index:row_spanxcol_span.
     * index - the index of the starting position
     * row_span - the number of rows to span
     * col_span- the number of columns to span
     *
     * @param str string format of skips or spans
     * @param isSpans whether is spans to be parsed (it is skips if not)
     * @return a int matrix that contains skip information.
     */
    private fun parseSpans(str: String, isSpans: Boolean): Array<IntArray>? {
        return try {
            var extraRows = 0
            var extraColumns = 0
            val spans = str.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            // Sort the spans by the position
            spans.sortWith { span1: String, span2: String ->
                span1.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()[0].toInt() - span2.split(":".toRegex())
                    .dropLastWhile { it.isEmpty() }
                    .toTypedArray()[0].toInt()
            }
            val spanMatrix = Array(spans.size) {
                IntArray(
                    3,
                )
            }
            var indexAndSpan: Array<String>
            if (mRows == 1 || mColumns == 1) {
                for (i in spans.indices) {
                    indexAndSpan = spans[i].trim { it <= ' ' }.split(":".toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    spanMatrix[i][0] = indexAndSpan[0].toInt()
                    spanMatrix[i][1] = 1
                    spanMatrix[i][2] = 1
                    if (mColumns == 1) {
                        spanMatrix[i][1] = indexAndSpan[1].toInt()
                        extraRows += spanMatrix[i][1]
                        if (isSpans) {
                            extraRows--
                        }
                    }
                    if (mRows == 1) {
                        spanMatrix[i][2] = indexAndSpan[1].toInt()
                        extraColumns += spanMatrix[i][2]
                        if (isSpans) {
                            extraColumns--
                        }
                    }
                }
                if (extraRows != 0 && !mExtraSpaceHandled) {
                    setRows(mRows + extraRows)
                }
                if (extraColumns != 0 && !mExtraSpaceHandled) {
                    setColumns(mColumns + extraColumns)
                }
                mExtraSpaceHandled = true
            } else {
                var rowAndCol: Array<String>
                for (i in spans.indices) {
                    indexAndSpan = spans[i].trim { it <= ' ' }.split(":".toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    rowAndCol = indexAndSpan[1].split("x".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    spanMatrix[i][0] = indexAndSpan[0].toInt()
                    if (mSubGridByColRow) {
                        spanMatrix[i][1] = rowAndCol[1].toInt()
                        spanMatrix[i][2] = rowAndCol[0].toInt()
                    } else {
                        spanMatrix[i][1] = rowAndCol[0].toInt()
                        spanMatrix[i][2] = rowAndCol[1].toInt()
                    }
                }
            }
            spanMatrix
        } catch (e: Exception) {
            null
        }
    }

    /**
     * fill the constraintMatrix based on the input attributes
     *
     * @param isUpdate whether to update the existing grid (true) or create a new one (false)
     */
    private fun fillConstraintMatrix(isUpdate: Boolean) {
        if (isUpdate) {
            for (i in 0 until mPositionMatrix!!.size) {
                for (j in 0 until mPositionMatrix!![0].size) {
                    mPositionMatrix!![i][j] = true
                }
            }
            for (i in 0 until mConstraintMatrix!!.size) {
                for (j in 0 until mConstraintMatrix!![0].size) {
                    mConstraintMatrix!![i][j] = -1
                }
            }
        }
        mNextAvailableIndex = 0
        if (mSkips != null && mSkips!!.trim { it <= ' ' }.isNotEmpty()) {
            val mSkips = parseSpans(mSkips!!, false)
            mSkips?.let { handleSkips(it) }
        }
        if (mSpans != null && mSpans!!.trim { it <= ' ' }.isNotEmpty()) {
            val mSpans = parseSpans(mSpans!!, true)
            mSpans?.let { handleSpans(it) }
        }
    }

    /**
     * Set up the Grid engine.
     */
    private fun initMatrices() {
        val isUpdate =
            mConstraintMatrix != null &&
                mConstraintMatrix!!.size == mWidgetsCount &&
                mPositionMatrix != null &&
                mPositionMatrix!!.size == mRows &&
                mPositionMatrix!![0].size == mColumns
        if (!isUpdate) {
            initVariables()
        }
        fillConstraintMatrix(isUpdate)
    }

    /**
     * If flags are given, set the values of the corresponding variables to true.
     */
    private fun handleFlags() {
        if (mFlags == null) {
            return
        }
        for (flag in mFlags!!) {
            when (flag) {
                SPANS_RESPECT_WIDGET_ORDER -> mSpansRespectWidgetOrder = true
                SUB_GRID_BY_COL_ROW -> mSubGridByColRow = true
            }
        }
    }

    override fun measure(widthMode: Int, widthSize: Int, heightMode: Int, heightSize: Int) {
        super.measure(widthMode, widthSize, heightMode, heightSize)
        mContainer = parent as ConstraintWidgetContainer?
        setupGrid(false)
        mContainer!!.add(*mBoxWidgets!!.filterNotNull().toTypedArray())
    }

    override fun addToSolver(system: LinearSystem, optimize: Boolean) {
        super.addToSolver(system, optimize)
        addConstraints()
    }

    companion object {
        const val HORIZONTAL = 0
        const val VERTICAL = 1
        const val SUB_GRID_BY_COL_ROW = 0
        const val SPANS_RESPECT_WIDGET_ORDER = 1
        private const val DEFAULT_SIZE = 3 // default rows and columns.

        private const val MAX_ROWS = 50 // maximum number of rows can be specified.

        private const val MAX_COLUMNS = 50 // maximum number of columns can be specified.
    }
}
