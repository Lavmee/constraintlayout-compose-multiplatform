/*
 * Copyright (C) 2016 The Android Open Source Project
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
package androidx.constraintlayout.coremp.scout

import androidx.constraintlayout.coremp.ext.Integer
import androidx.constraintlayout.coremp.ext.Rectangle
import androidx.constraintlayout.coremp.scout.Utils.leftTrim
import androidx.constraintlayout.coremp.scout.Utils.max
import androidx.constraintlayout.coremp.scout.Utils.toS
import androidx.constraintlayout.coremp.scout.Utils.zero
import kotlin.math.abs
import kotlin.math.min

/**
 * Inference Probability tables
 * There are two major entry points in this class
 * computeConstraints - which build the Inference tables
 * applyConstraints - applies the constraints to the widgets
 */
class ScoutProbabilities {
    lateinit var mProbability: Array<Array<FloatArray?>?>
    lateinit var mMargin: Array<Array<FloatArray?>?>
    lateinit var mBinaryBias: Array<Array<Array<FloatArray>>>
    lateinit var mBinaryProbability: Array<Array<Array<FloatArray>?>>
    var mLen = 0

    /**
     * This calculates a constraint tables
     *
     * @param list ordered list of widgets root must be list[0]
     */
    fun computeConstraints(list: Array<ScoutWidget>) {
        if (list.size < 2) {
            throw IllegalArgumentException("list must contain more than 1 widget")
        }
        if (!list[0].isRoot) {
            throw IllegalArgumentException("list[0] must be root")
        }
        mLen = list.size
        mProbability = arrayOfNulls<Array<FloatArray?>?>(mLen)
        mMargin = arrayOfNulls(mLen)

        // calculate probability for normal connections
        val result = FloatArray(2) // estimation function return 2 values probability & margin
        for (i in 1 until mLen) { // for all non root widgets
            val all: Array<Direction> = Direction.allDirections
            if (list[i].isGuideline) {
                continue
            }
            mProbability[i] = arrayOfNulls(all.size)
            mMargin[i] = arrayOfNulls(all.size)
            for (dir in all.indices) { // for all possible connections
                val direction = Direction[dir]
                val connectTypes = direction.connectTypes()

                // create the multidimensional array on the fly
                // to account for the variying size of the probability space
                mProbability[i]!![dir] = FloatArray(mLen * connectTypes)
                mMargin[i]?.set(dir, FloatArray(mLen * connectTypes))

                // fill in all candidate connections
                for (candidate in mMargin[i]?.get(dir)!!.indices) {
                    val widgetNumber = candidate / connectTypes
                    val opposite = candidate % connectTypes
                    val connectTo = if (opposite == 0) direction else direction.opposite
                    estimateProbability(
                        list[i],
                        direction,
                        list[widgetNumber],
                        connectTo,
                        list,
                        result,
                    )
                    mProbability[i]!![dir]!![candidate] = result[RESULT_PROBABILITY]
                    mMargin[i]?.get(dir)!![candidate] = result[RESULT_MARGIN]
                }
            }
        }

        // calculate probability for "centered" connections
        mBinaryProbability = Array(mLen) { Array(2) { Array(mLen * 2) { FloatArray(mLen * 2) } } }
        mBinaryBias = Array(mLen) { Array(2) { Array(mLen * 2) { FloatArray(mLen * 2) } } }
        val directions = arrayOf(arrayOf(Direction.NORTH, Direction.SOUTH), arrayOf(Direction.WEST, Direction.EAST))
        for (i in 1 until mLen) {
            for (horizontal in 0..1) { // vert=0 or horizantal=1
                val sides = directions[horizontal]
                for (candidate1 in 0 until mLen * 2) {
                    for (candidate2 in 0 until mLen * 2) {
                        // candidates are 2 per widget (left/right or above/below)
                        val widget1Number = candidate1 / 2
                        val widget2Number = candidate2 / 2

                        // pick the sides to connect
                        val widget1Side = sides[candidate1 and 0x1]
                        val widget2Side = sides[candidate2 and 0x1]
                        estimateBinaryProbability(
                            list[i],
                            horizontal,
                            list[widget1Number],
                            widget1Side,
                            list[widget2Number],
                            widget2Side,
                            list,
                            result,
                        )
                        mBinaryProbability[i][horizontal]!![candidate1][candidate2] = result[RESULT_PROBABILITY]
                        mBinaryBias[i][horizontal][candidate1][candidate2] = result[RESULT_MARGIN]
                    }
                }
            }
        }
        if (DEBUG) {
            printTable(list)
        }
    }

    /**
     * This applies a constraint set suggested by the Inference tables
     */
    fun applyConstraints(list: Array<ScoutWidget>) {
        // this provides the sequence of connections
        pickColumnWidgets(list)
        pickCenterOverlap(list)
        pickBaseLineConnections(list) // baseline first
        pickCenteredConnections(list, true) // centered connections that stretch
        pickMarginConnections(list, 10) // regular margin connections that are close
        pickCenteredConnections(list, false) // general centered connections
        pickMarginConnections(list, 100) // all remaining margins
        // pickWeakConstraints(list); // weak constraints for ensuring wrap content
        if (DEBUG) {
            printBaseTable(list)
        }
    }

    /**
     * Find and connect widgets centered over other widgets
     */
    private fun pickCenterOverlap(list: Array<ScoutWidget>) {
        // find any widget centered over the edge of another
        for (i in list.indices) {
            val scoutWidget: ScoutWidget = list[i]
            val centerX: Float = scoutWidget.x + scoutWidget.width / 2
            val centerY: Float = scoutWidget.y + scoutWidget.height / 2
            for (j in list.indices) {
                if (i == j) continue
                val widget: ScoutWidget = list[j]
                if (scoutWidget.isGuideline) {
                    continue
                }
                if (!widget.isGuideline && ScoutWidget.distance(scoutWidget, widget) > MAX_DIST_FOR_CENTER_OVERLAP) {
                    continue
                }
                if (!widget.isGuideline || widget.isVerticalGuideline) {
                    if (abs(widget.x - centerX) < CENTER_ERROR) {
                        scoutWidget.setEdgeCentered(1, widget, Direction.WEST)
                    }
                    if (abs(widget.x + widget.width - centerX) < CENTER_ERROR) {
                        scoutWidget.setEdgeCentered(1, widget, Direction.EAST)
                    }
                }
                if (!widget.isGuideline || widget.isHorizontalGuideline) {
                    if (abs(widget.y - centerY) < CENTER_ERROR) {
                        scoutWidget.setEdgeCentered(0, widget, Direction.NORTH)
                    }
                    if (abs(widget.y + widget.height - centerY) < CENTER_ERROR) {
                        scoutWidget.setEdgeCentered(0, widget, Direction.SOUTH)
                    }
                }
            }
        }
    }

    /**
     * force structure for column cases
     */
    private fun pickColumnWidgets(list: Array<ScoutWidget>) {
        val w: Array<ScoutWidget?> = arrayOfNulls(list.size - 1)
        for (i in 0 until list.size - 1) {
            w[i] = list[i + 1]
        }
        w.sortWith { w1, w2 ->
            var n: Int = Integer.compare(w1!!.mConstraintWidget!!.x, w2!!.mConstraintWidget!!.x)
            if (n == 0) {
                n = Integer.compare(
                    w1.mConstraintWidget!!.width,
                    w2.mConstraintWidget!!.width,
                )
            }
            n
        }
        val groups: ArrayList<ArrayList<ScoutWidget>> = ArrayList()
        var current: ArrayList<ScoutWidget> = ArrayList()
        for (i in 2 until w.size) {
            val scoutWidget: ScoutWidget = w[i]!!
            if (sameCol(w[i], w[i - 1])) {
                if (current.isEmpty()) {
                    groups.add(current)
                    current.add(w[i - 1]!!)
                    current.add(w[i]!!)
                } else {
                    if (sameCol(current[0], w[i])) {
                        current.add(w[i]!!)
                    } else {
                        current = ArrayList()
                        groups.add(current)
                        current.add(w[i - 1]!!)
                        current.add(w[i]!!)
                    }
                }
            }
        }
        val dualIndex = IntArray(2)
        for (group in groups) {
            if (SKIP_SPARSE_COLUMNS) {
                // skip columns that have lot of space to reject accidental columns.
                var union: Rectangle? = null
                var area = 0
                for (scoutWidget in group) {
                    val r: Rectangle = scoutWidget.rectangle
                    area += r.width * r.height
                    union = union?.union(r) ?: r
                }
                val unionArea: Int = union!!.width * union.height
                if (unionArea > 2 * area) { // more than have the area is empty
                    continue
                }
            }
            val widgets: Array<ScoutWidget> = group.toTypedArray()
            widgets.sortWith(ScoutWidget.sSortY)
            val reverse: Boolean = (widgets[0].rootDistanceY() > widgets[widgets.size - 1].rootDistanceY())
            val max = FloatArray(widgets.size)
            val map = IntArray(widgets.size)
            for (i in widgets.indices) {
                for (j in 1 until list.size) {
                    if (widgets[i] == list[j]) {
                        map[i] = j
                    }
                }
            }
            // zero out probabilities of connecting to each other
            // we are going to take care of it here
            for (i in widgets.indices) {
                for (j in widgets.indices) {
                    val l = map[j] * 2
                    for (k in 2 until 2 * list.size) {
                        mBinaryProbability[map[i]][1]!![l][k] = -1f
                        mBinaryProbability[map[i]][1]!![k][l] = -1f
                        mBinaryProbability[map[i]][1]!![l + 1][k] = -1f
                        mBinaryProbability[map[i]][1]!![k][l + 1] = -1f
                    }
                }
            }
            var bestToConnect = -1
            var maxVal = -1f
            for (i in widgets.indices) {
                max[i] = max(mBinaryProbability[map[i]][1]!!, dualIndex)
                if (maxVal < max[i]) {
                    bestToConnect = i
                    maxVal = max[i]
                }
            }
            if (reverse) {
                for (i in 1 until widgets.size) {
                    var gap: Int = widgets[i].mConstraintWidget!!.y
                    gap -= widgets[i - 1].mConstraintWidget!!.y
                    gap -= widgets[i - 1].mConstraintWidget!!.height
                    widgets[i - 1].setConstraint(
                        Direction.SOUTH.direction,
                        widgets[i],
                        Direction.NORTH.direction,
                        gap.toFloat(),
                    )
                }
            } else {
                for (i in 1 until widgets.size) {
                    var gap: Int = widgets[i].mConstraintWidget!!.y
                    gap -= widgets[i - 1].mConstraintWidget!!.y
                    gap -= widgets[i - 1].mConstraintWidget!!.height
                    widgets[i].setConstraint(
                        Direction.NORTH.direction,
                        widgets[i - 1],
                        Direction.SOUTH.direction,
                        gap.toFloat(),
                    )
                }
            }
            if (bestToConnect >= 0) {
                max(mBinaryProbability[map[bestToConnect]][1]!!, dualIndex)
                val w1: ScoutWidget = list[dualIndex[0] / 2]
                val w2: ScoutWidget = list[dualIndex[1] / 2]
                val dir1 = if (dualIndex[0] and 0x1 == 0) Direction.WEST else Direction.EAST
                val dir2 = if (dualIndex[1] and 0x1 == 0) Direction.WEST else Direction.EAST
                widgets[bestToConnect].setCentered(0, w1, w2, dir1, dir2, 0f)
                for (i in bestToConnect + 1 until widgets.size) {
                    widgets[i].setCentered(
                        1,
                        widgets[i - 1],
                        widgets[i - 1],
                        Direction.WEST,
                        Direction.EAST,
                        0f,
                    )
                }
                for (i in 1..bestToConnect) {
                    widgets[i - 1].setCentered(
                        1,
                        widgets[i],
                        widgets[i],
                        Direction.WEST,
                        Direction.EAST,
                        0f,
                    )
                }
            } else {
                if (reverse) {
                    for (i in 1 until widgets.size) {
                        widgets[i - 1].setCentered(
                            0,
                            widgets[i],
                            widgets[i],
                            Direction.WEST,
                            Direction.EAST,
                            0f,
                        )
                    }
                } else {
                    for (i in 1 until widgets.size) {
                        widgets[i].setCentered(
                            0,
                            widgets[i - 1],
                            widgets[i - 1],
                            Direction.WEST,
                            Direction.EAST,
                            0f,
                        )
                    }
                }
            }
        }
    }

    /**
     * This searches for baseline connections with a very narrow tolerance
     */
    private fun pickBaseLineConnections(list: Array<ScoutWidget>) {
        val baseline = Direction.BASE.direction
        val north = Direction.NORTH.direction
        val south = Direction.SOUTH.direction
        val east = Direction.EAST.direction
        val west = Direction.WEST.direction

        // Search for baseline connections
        for (i in 1 until mLen) {
            val widgetProbability = mProbability[i]
            if (widgetProbability == null || widgetProbability[baseline] == null) {
                continue
            }
            val maxValue = 0.0f
            val maxNorth = widgetProbability[north]!![max(widgetProbability[north]!!)]
            val maxSouth = widgetProbability[south]!![max(widgetProbability[south]!!)]
            val maxIndex = max(widgetProbability[baseline]!!)
            val maxBaseline = widgetProbability[baseline]!![maxIndex]
            if (maxBaseline < maxNorth || maxBaseline < maxSouth) {
                continue
            }
            var s = ""
            if (DEBUG) {
                println(" b check " + list[i] + " " + widgetProbability[4]!![maxIndex])
                s =
                    (
                        list[i].toString() + "(" + Direction.toString(baseline) + ") -> " + list[maxIndex] + " " + Direction.toString(
                            baseline,
                        )
                        )
                println("try $s")
            }
            if (list[i].setConstraint(baseline, list[maxIndex], baseline, 0f)) {
                zero(mBinaryProbability[i][Direction.ORIENTATION_VERTICAL]!!)
                widgetProbability[baseline]?.fill(0f)
                widgetProbability[north] = null
                widgetProbability[south]?.fill(0f)
                if (DEBUG) {
                    println("connect $s")
                }
            }
        }
    }

    /**
     * This searches for centered connections
     *
     * @param list            widgets (0 is root)
     * @param checkResizeable if true will attempt to make a stretchable widget
     */
    private fun pickCenteredConnections(list: Array<ScoutWidget>, checkResizeable: Boolean) {
        val side = arrayOf(arrayOf(Direction.NORTH, Direction.SOUTH), arrayOf(Direction.WEST, Direction.EAST))
        val dualIndex = IntArray(2)
        for (i in 1 until mLen) {
            val widgetBinaryProbability = mBinaryProbability[i]
            val widgetBinaryBias = mBinaryBias[i]
            for (horizontal in widgetBinaryProbability.indices) { // vert=0 or horizontals=1
                val pmatrix = widgetBinaryProbability[horizontal]
                val bias = widgetBinaryBias[horizontal]
                if (pmatrix == null) {
                    continue
                }
                var worked = false
                while (!worked) {
                    max(pmatrix, dualIndex)
                    val max1 = dualIndex[0]
                    val max2 = dualIndex[1]
                    val wNo1 = max1 / 2
                    val wNo2 = max2 / 2
                    val widget1Side = side[horizontal][max1 and 0x1]
                    val widget2Side = side[horizontal][max2 and 0x1]

                    // pick the sides to connect
                    val centerProbability = pmatrix[max1][max2]
                    worked = true
                    if (centerProbability > .9) {
                        if (checkResizeable && !list[i].isCandidateResizable(horizontal)) {
                            continue
                        }
                        worked = list[i].setCentered(
                            horizontal * 2,
                            list[wNo1],
                            list[wNo2],
                            widget1Side,
                            widget2Side,
                            bias[max1][max2],
                        )
                        if (worked) {
                            mProbability[i]!![horizontal * 2] = null
                            mProbability[i]!![horizontal * 2 + 1] = null
                        } else {
                            pmatrix[max1][max2] = 0f
                        }
                    }
                }
            }
        }
    }

    /**
     * This searches for Normal margin connections
     *
     * @param list             list of scouts
     * @param maxMarginPercent only margins less than that percent will be connected
     */
    private fun pickMarginConnections(list: Array<ScoutWidget>, maxMarginPercent: Int) {
        val baseline = Direction.BASE.direction
        val north = Direction.NORTH.direction
        val south = Direction.SOUTH.direction
        val east = Direction.EAST.direction
        val width: Int = list[0].mConstraintWidget!!.width
        val height: Int = list[0].mConstraintWidget!!.width
        val maxWidthMargin = width * maxMarginPercent / 100
        val maxHeightMargin = height * maxMarginPercent / 100
        val maxMargin = intArrayOf(maxHeightMargin, maxWidthMargin)
        val west = Direction.WEST.direction
        // pick generic connections
        val dirTypes = arrayOf(intArrayOf(north, south), intArrayOf(west, east))
        for (i in mLen - 1 downTo 1) {
            val widgetProbability = mProbability[i]
            for (horizontal in 0..1) {
                val dirs = dirTypes[horizontal]
                var found = false
                while (!found) {
                    found = true
                    val setlen = dirs.size
                    if (DEBUG) {
                        println(" check " + list[i] + " " + horizontal)
                    }
                    val dir = dirs[0]
                    if (widgetProbability == null || widgetProbability[dir] == null) {
                        continue
                    }
                    var maxIndex = 0
                    var maxDirection = 0
                    var maxValue = 0.0f
                    val rowType = 0
                    for (j in 0 until setlen) {
                        val rowMaxIndex = max(widgetProbability[dirs[j]]!!)
                        if (maxValue < widgetProbability[dirs[j]]!![rowMaxIndex]) {
                            maxDirection = dirs[j]
                            maxIndex = rowMaxIndex
                            maxValue = widgetProbability[dirs[j]]!![rowMaxIndex]
                        }
                    }
                    if (widgetProbability[maxDirection] == null) {
                        println(list[i].toString() + " " + maxDirection)
                        continue
                    }
                    var m: Int
                    var cDir: Int
                    if (maxDirection == baseline) { // baseline connection
                        m = maxIndex
                        cDir = baseline // always baseline
                    } else {
                        m = maxIndex / 2
                        cDir = maxDirection
                        if (maxIndex % 2 == 1) {
                            cDir = cDir xor 1
                        }
                    }
                    if (mMargin[i]?.get(maxDirection)!![maxIndex] > maxMargin[horizontal]) {
                        continue
                    }
                    val s: String =
                        (
                            list[i].toString() + "(" + Direction.toString(maxDirection) + ") -> " + list[m] + " " + Direction.toString(
                                cDir,
                            )
                            )
                    if (DEBUG) {
                        println("try $s")
                    }
                    if (!list[i].setConstraint(
                            maxDirection,
                            list[m],
                            cDir,
                            mMargin[i]?.get(maxDirection)!![maxIndex],
                        )
                    ) {
                        if (widgetProbability[maxDirection]!![maxIndex] >= 0) {
                            widgetProbability[maxDirection]!![maxIndex] = CONSTRAINT_FAILED_FLAG.toFloat()
                            found = false
                        }
                    } else {
                        mBinaryProbability[i][horizontal] = null
                        if (DEBUG) {
                            println("connect $s")
                        }
                    }
                }
            }
        }
    }

    /**
     * pick weak constraints
     */
    private fun pickWeakConstraints(list: Array<ScoutWidget>) {
        val directions = arrayOf(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
        val candidates: Array<Array<ScoutWidget?>?> = arrayOfNulls(directions.size) // no arrays of generics
        val maxCandidate: Array<ScoutWidget?> = arrayOfNulls(directions.size)
        var centeredVertical: ScoutWidget? = null
        var centeredHorizontal: ScoutWidget? = null
        val maxDist = floatArrayOf(-1f, -1f, -1f, -1f)

        // find the biggest widget centered
        for (i in 1 until list.size) {
            val widget: ScoutWidget = list[i]
            if (widget.isCentered(Direction.ORIENTATION_VERTICAL)) {
                if (centeredVertical == null || centeredVertical.height > widget.height) {
                    centeredVertical = widget
                }
            }
            if (widget.isCentered(Direction.ORIENTATION_HORIZONTAL)) {
                if (centeredHorizontal == null || centeredHorizontal.width > widget.width) {
                    centeredHorizontal = widget
                }
            }
        }
        val centeredMax: Array<ScoutWidget?> =
            arrayOf(centeredVertical!!, centeredVertical, centeredHorizontal!!, centeredHorizontal)
        // build table of widgets open from each direction
        for (j in directions.indices) {
            val direction = directions[j]
            val tmp: ArrayList<ScoutWidget> = ArrayList()
            for (i in 1 until list.size) {
                val widget: ScoutWidget = list[i]
                if (widget.isGuideline) {
                    continue
                }
                if (!widget.isConnected(directions[j].opposite)) {
                    val dist: Float = widget.connectedDistanceToRoot(list, direction)
                    if (!dist.isNaN()) {
                        if (dist > maxDist[j]) {
                            maxDist[j] = dist
                            maxCandidate[j] = widget
                        }
                        tmp.add(widget)
                    }
                }
            }
            candidates[j] = tmp.toTypedArray()
            if (DEBUG) {
                var s = "[$direction]"
                s += "max=" + maxCandidate[j] + " "
                for (i in candidates[j]!!.indices) {
                    val c: ScoutWidget? = candidates[j]?.get(i)
                    s += " " + c + " " + c!!.connectedDistanceToRoot(list, direction)
                }
                println(s)
            }
        }

        // when there is nothing on the other side add a constraint to the longest
        // optionally attaching to the center if it fits
        for (j in directions.indices) {
            if (candidates[j]!!.isNotEmpty() && candidates[j xor 1]?.size == 0) {
                val dirInt = directions[j].opposite.direction
                val rootDirInt = directions[j].opposite.direction
                var connect: ScoutWidget = list[0]
                var connectSide = rootDirInt
                if (SUPPORT_WEAK_TO_CENTER) {
                    if (centeredMax[j] != null) {
                        val centerPos: Float = centeredMax[j]!!.getLocation(directions[j])
                        val maxPos: Float = maxCandidate[j]!!.getLocation(directions[j].opposite)
                        var delta = centerPos - maxPos
                        if (directions[j] == Direction.EAST || directions[j] == Direction.SOUTH) {
                            delta = -delta
                        }
                        if (delta > 0) {
                            connectSide = directions[j].direction
                            connect = centeredMax[j]!!
                        }
                    }
                }
                maxCandidate[j]!!.setWeakConstraint(dirInt, connect, connectSide)
                candidates[j] = arrayOfNulls(0) // prevent next step from using
            }
        }

        // Where there is no overlap
        run {
            var j = 0
            while (j < directions.size) {
                if (candidates[j]!!.isNotEmpty() && candidates[j + 1]!!.isNotEmpty()) {
                    val side = directions[j].opposite
                    val otherSide = directions[j]
                    if (maxCandidate[j]!!.getLocation(side) < maxCandidate[j + 1]!!.getLocation(otherSide)) {
                        maxCandidate[j]!!.setWeakConstraint(
                            side.direction,
                            maxCandidate[j + 1]!!,
                            otherSide.direction,
                        )
                        candidates[j] = arrayOfNulls(0) // prevent next step from using
                        maxCandidate[j] = null
                    }
                }
                j += 2
            }
        }

        //  pick the closest clean shot to the other side if it is a maximum
        var j = 0
        while (j < directions.size) {
            if (candidates[j]!!.isNotEmpty() && candidates[j + 1]!!.isNotEmpty()) {
                val side = directions[j].opposite
                val otherSide = directions[j]
                val clearToRoot1: Boolean = maxCandidate[j]!!.getNeighbor(otherSide, list)!!.isRoot
                val clearToRoot2: Boolean = maxCandidate[j]!!.getNeighbor(side, list)!!.isRoot
                if (clearToRoot1 && clearToRoot2) {
                    if (maxDist[j] > maxDist[j + 1]) {
                        maxCandidate[j]!!.setWeakConstraint(
                            side.direction,
                            list[0],
                            side.direction,
                        )
                    } else {
                        maxCandidate[j + 1]!!.setWeakConstraint(
                            otherSide.direction,
                            list[0],
                            otherSide.direction,
                        )
                    }
                }
            }
            j += 2
        }
    }

    /**
     * Print the Tables
     */
    fun printTable(list: Array<ScoutWidget>) {
        printCenterTable(list)
        printBaseTable(list)
    }

    /**
     * Print the tables involved int centering the widgets
     */
    fun printCenterTable(list: Array<ScoutWidget>) {
        // PRINT DEBUG
        println("----------------- BASE TABLE --------------------")
        val size = 10
        val padd = CharArray(size).concatToString().replace('\u0000', ' ')
        print("  ")
        for (i in 0 until mLen) {
            var dbg = "[" + i + "] " + list[i] + "-------------------------"
            dbg = dbg.substring(0, 20)
            print(dbg + if (i == mLen - 1) "\n" else "")
        }
        var str = "["
        for (con in 0 until mLen * 2) {
            val opposite = con and 0x1
            str += ((con / 2).toString() + (if (opposite == 0) "->" else "<-") + "           ").substring(0, 10)
        }
        println("  $str")
        for (i in 1 until mLen) {
            for (dir in mBinaryProbability[i].indices) { // above, below, left, right
                var tab = ""
                for (k in mBinaryProbability[i][dir]!!.indices) {
                    tab += """${toS(mBinaryProbability[i][dir]!![k])}
  """
                }
                println(Direction.toString(dir) + " " + tab)
            }
        }
    }

    /**
     * Prints the tables involved in the normal widget asociations.
     */
    fun printBaseTable(list: Array<ScoutWidget>) {
        // PRINT DEBUG
        println("----------------- CENTER TABLE --------------------")
        val size = 10
        val padd = CharArray(size).concatToString().replace('\u0000', ' ')
        print(" ")
        for (i in 0 until mLen) {
            var dbg = "[" + i + "] " + list[i] + "-------------------------"
            dbg = if (i == 0) {
                padd + dbg.substring(0, 20)
            } else {
                dbg.substring(0, 20)
            }
            print(dbg + if (i == mLen - 1) "\n" else "")
        }
        var str = "["
        for (con in 0 until mLen * 2) {
            val opposite = con and 0x1
            str += ((con / 2).toString() + (if (opposite == 0) "->" else "<-") + "           ").substring(0, 10)
        }
        val header = "Connection $padd".substring(0, size)
        println("$header $str")
        for (i in 1 until mLen) {
            if (mProbability[i] == null) {
                continue
            }
            for (dir in mProbability[i]!!.indices) { // above, below, left, right
                println(
                    leftTrim(padd + i + " " + Direction.toString(dir), size) + " " + toS(mProbability[i]!![dir]),
                )
                println(padd + " " + toS(mMargin[i]?.get(dir)))
            }
        }
    }

    companion object {
        private const val DEBUG = false
        private const val BASELINE_ERROR = 4.0f
        private const val RESULT_PROBABILITY = 0
        private const val RESULT_MARGIN = 1
        private const val SUPPORT_CENTER_TO_NON_ROOT = true
        private const val SUPPORT_WEAK_TO_CENTER = true
        private const val NEGATIVE_GAP_FLAG = -3
        private const val CONSTRAINT_FAILED_FLAG = -2
        private const val CENTER_ERROR = 2f
        private const val SLOPE_CENTER_CONNECTION = 20f
        private const val MAX_DIST_FOR_CENTER_OVERLAP = 40
        private const val ROOT_MARGIN_DISCOUNT = 16
        private const val MAX_ROOT_OVERHANG = 10
        private const val SKIP_SPARSE_COLUMNS = true
        private fun sameCol(a: ScoutWidget?, b: ScoutWidget?): Boolean {
            return (a!!.mConstraintWidget!!.x == b!!.mConstraintWidget!!.x && a.mConstraintWidget!!.width == b.mConstraintWidget!!.width)
        }

        /**
         * This defines the "probability" of a constraint between two widgets.
         *
         * @param from    source widget
         * @param fromDir direction on that widget
         * @param to      destination widget
         * @param toDir   destination side to connect
         * @param result  populates results with probability and offset
         */
        private fun estimateProbability(
            from: ScoutWidget,
            fromDir: Direction,
            to: ScoutWidget,
            toDir: Direction,
            list: Array<ScoutWidget>,
            result: FloatArray,
        ) {
            result[RESULT_PROBABILITY] = 0f
            result[RESULT_MARGIN] = 0f
            if (from == to) { // 0 probability of connecting to yourself
                return
            }
            if (from.isGuideline) {
                return
            }
            if (to.isGuideline) {
                if ((toDir == Direction.NORTH || toDir == Direction.SOUTH) && to.isVerticalGuideline) {
                    return
                }
                if ((toDir == Direction.EAST || toDir == Direction.WEST) && to.isHorizontalGuideline) {
                    return
                }
            }

            // if it already has a baseline do not connect to it
            if ((toDir == Direction.NORTH || toDir == Direction.SOUTH) and from.hasBaseline()) {
                if (from.hasConnection(Direction.BASE)) {
                    return
                }
            }
            if (fromDir == Direction.BASE) { // if baseline 0  probability of connecting to non baseline
                if (!from.hasBaseline() || !to.hasBaseline()) { // no base line
                    return
                }
            }
            val fromLocation: Float = from.getLocation(fromDir)
            val toLocation: Float = to.getLocation(toDir)
            val positionDiff = if (fromDir.reverse()) fromLocation - toLocation else toLocation - fromLocation
            var distance: Float = 2 * ScoutWidget.distance(from, to)
            if (to.isRoot) {
                distance = abs((distance - ROOT_MARGIN_DISCOUNT).toDouble()).toFloat()
            }
            // probability decreases with distance and margin distance
            var probability = 1 / (1 + distance * distance + positionDiff * positionDiff)
            if (fromDir == Direction.BASE) { // prefer baseline
                if (abs(positionDiff.toDouble()) > BASELINE_ERROR) {
                    return
                }
                probability *= 2f
            }
            if (to.isRoot) {
                probability *= 2f
            }
            result[RESULT_PROBABILITY] = if (positionDiff >= 0) probability else NEGATIVE_GAP_FLAG.toFloat()
            result[RESULT_MARGIN] = positionDiff
        }

        /**
         * This defines the constraint between a widget and two widgets to the left and right of it.
         * Currently only encourages probability between widget and root for center purposes.
         *
         * @param from        source widget
         * @param orientation horizontal or vertical connections (1 is horizontal)
         * @param to1         connect to on one side
         * @param toDir1      direction on that widget
         * @param to2         connect to on other side
         * @param toDir2      direction on that widget
         * @param result      populates results with probability and offset
         */
        private fun estimateBinaryProbability(
            from: ScoutWidget,
            // 0 = north/south 1 = east/west
            orientation: Int,
            to1: ScoutWidget,
            toDir1: Direction,
            to2: ScoutWidget,
            toDir2: Direction,
            list: Array<ScoutWidget>,
            result: FloatArray,
        ) {
            result[RESULT_PROBABILITY] = 0f
            result[RESULT_MARGIN] = 0f
            if (from == to1 || from == to2) { // cannot center on yourself
                return
            }
            if (from.isGuideline) {
                return
            }
            // if it already has a baseline do not connect to it
            if ((orientation == Direction.ORIENTATION_VERTICAL) and from.hasBaseline()) {
                if (from.hasConnection(Direction.BASE)) {
                    return
                }
            }
            // distance normalizing scale factor
            val scale: Float =
                (0.5f * if (orientation == Direction.ORIENTATION_VERTICAL) from.parent!!.height else from.parent!!.width)
            val fromLeft = Direction.getDirections(orientation)[0]
            val fromRight = Direction.getDirections(orientation)[1]
            val location1: Float = from.getLocation(fromLeft)
            val location2: Float = from.getLocation(fromRight)
            val toLoc1: Float = to1.getLocation(toDir1)
            val toLoc2: Float = to2.getLocation(toDir2)
            val positionDiff1 = location1 - toLoc1
            var positionDiff2 = toLoc2 - location2
            if (positionDiff1 < 0 || positionDiff2 < 0) { // do not center if not aligned
                var badCandidate = true
                if (positionDiff2 < 0 && to2.isRoot && positionDiff2 > -MAX_ROOT_OVERHANG) {
                    badCandidate = false
                    positionDiff2 = 0f
                }
                if (positionDiff1 < 0 && to1.isRoot && positionDiff2 > -MAX_ROOT_OVERHANG) {
                    badCandidate = false
                    positionDiff2 = 0f
                }
                if (badCandidate) {
                    result[RESULT_PROBABILITY] = NEGATIVE_GAP_FLAG.toFloat()
                    return
                }
            }
            val distance1: Float = ScoutWidget.distance(from, to1) / scale
            val distance2: Float = ScoutWidget.distance(from, to2) / scale
            val diff = abs((positionDiff1 - positionDiff2).toDouble()).toFloat()
            var probability = (if (diff < SLOPE_CENTER_CONNECTION) 1 else 0).toFloat() // favor close distance
            probability /= (1 + distance1 + distance2)
            probability += 1 / (1 + abs((positionDiff1 - positionDiff2).toDouble())).toFloat()
            probability *= if (to1.isRoot && to2.isRoot) {
                2f
            } else if (SUPPORT_CENTER_TO_NON_ROOT) {
                1f
            } else {
                0f
            }
            result[RESULT_PROBABILITY] = probability
            result[RESULT_MARGIN] = min(positionDiff1.toDouble(), positionDiff2.toDouble()).toFloat()
        }
    }
}
