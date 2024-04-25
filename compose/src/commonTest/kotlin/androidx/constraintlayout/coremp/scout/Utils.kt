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

import androidx.constraintlayout.coremp.ext.Rectangle
import androidx.constraintlayout.coremp.widgets.ConstraintWidget
import androidx.constraintlayout.coremp.widgets.Guideline
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Simple Utilities used by the Inference system
 */
object Utils {

    private fun Double.format(digits: Int = 5): String {
        return ((this * (10.0).pow(digits)).roundToInt() / 100.0).toString()
    }

    /**
     * Calculate the maximum of an array
     *
     * @return the index of the maximum
     */
    fun max(array: FloatArray): Int {
        var max = 0
        var value = array[0]
        for (i in 1 until array.size) {
            if (value < array[i]) {
                max = i
                value = array[i]
            }
        }
        return max
    }

    /**
     * Calculate the maximum of a 2D array
     *
     * @param result the index of the maximum filled by the function
     * @return the value of the maximum probabilities
     */
    fun max(array: Array<FloatArray>, result: IntArray): Float {
        var max1 = 0
        var max2 = 0
        var value = array[max1][max2]
        for (i in array.indices) {
            for (j in array[0].indices) {
                if (value < array[i][j]) {
                    max1 = i
                    max2 = j
                    value = array[max1][max2]
                }
            }
        }
        result[0] = max1
        result[1] = max2
        return value
    }

    /**
     * convert an array of floats to fixed length strings
     */
    fun toS(a: FloatArray?): String {
        var s = "["
        if (a == null) {
            return "[null]"
        }
        for (i in a.indices) {
            if (i != 0) {
                s += " , "
            }
            val t: String = a[i].toDouble().format() + "       "
            s += t.substring(0, 7)
        }
        s += "]"
        return s
    }

    /**
     * Left trim a string to a fixed length
     *
     * @param str String to trim
     * @param len length to trim to
     * @return the trimmed string
     */
    fun leftTrim(str: String, len: Int): String {
        return str.substring(str.length - len)
    }

    /**
     * Fill a 2D array of floats with 0.0
     */
    fun zero(array: Array<FloatArray>) {
        for (aFloat in array) {
            aFloat.fill(-1f)
        }
    }

    /**
     * Calculate the number of gaps + 1 given a start and end range
     *
     * @param start table of range starts
     * @param end   table of range ends
     */
    fun gaps(start: IntArray, end: IntArray): Int {
        start.sort()
        end.sort()
        var overlap = 0
        var gaps = 0
        var i = 0
        var j = 0
        while (j < end.size) {
            if (i < start.size && start[i] < end[j]) {
                overlap++
                i++
            } else {
                j++
                overlap--
            }
            if (overlap == 0) {
                gaps++
            }
        }
        return gaps
    }

    /**
     * calculate the ranges for the cells
     *
     * @param start table of range starts
     * @param end   table of range ends
     * @return array of integers 2 for each cell
     */
    fun cells(start: IntArray, end: IntArray): IntArray {
        start.sort()
        end.sort()
        var overlap = 0
        var gaps = 0
        run {
            var i = 0
            var j = 0
            while (j < end.size) {
                if (i < start.size && start[i] < end[j]) {
                    overlap++
                    i++
                } else {
                    j++
                    overlap--
                }
                if (overlap == 0) {
                    gaps++
                }
            }
        }
        val cells = IntArray(gaps * 2)
        overlap = 0
        gaps = 0
        var previousOverlap = 0
        var i = 0
        var j = 0
        while (j < end.size) {
            if (i < start.size && start[i] < end[j]) {
                overlap++
                if (previousOverlap == 0) {
                    cells[gaps++] = start[i]
                }
                i++
            } else {
                overlap--
                if (overlap == 0) {
                    cells[gaps++] = end[j]
                }
                j++
            }
            previousOverlap = overlap
        }
        return cells
    }

    /**
     * Search within the collection of ranges for the position
     *
     * @param pos range pairs
     * @param p1  start of widget
     * @param p2  end of widget
     * @return the pair of ranges it is within
     */
    fun getPosition(pos: IntArray, p1: Int, p2: Int): Int {
        var j = 0
        while (j < pos.size) {
            // linear search is best because N typically < 10
            if (pos[j] <= p1 && p2 <= pos[j + 1]) {
                return j / 2
            }
            j += 2
        }
        return -1
    }

    /**
     * Sort a list of integers and remove duplicates
     */
    fun sortUnique(list: IntArray): IntArray {
        list.sort()
        var count = 1
        for (i in 1 until list.size) {
            if (list[i] != list[i - 1]) {
                count++
            }
        }
        val ret = IntArray(count)
        count = 1
        ret[0] = list[0]
        for (i in 1 until list.size) {
            if (list[i] != list[i - 1]) {
                ret[count++] = list[i]
            }
        }
        return ret
    }

    /**
     * print a string that is a fixed width of size used in debugging
     */
    fun fwPrint(s: String, size: Int) {
        var s = s
        s += "                                             "
        s = s.substring(0, size)
        print(s)
    }

    /**
     * Get the bounding box around a list of widgets
     */
    fun getBoundingBox(widgets: ArrayList<ConstraintWidget>): Rectangle? {
        var all: Rectangle? = null
        val tmp = Rectangle()
        for (widget in widgets) {
            if (widget is Guideline) {
                continue
            }
            tmp.x = widget.x
            tmp.y = widget.y
            tmp.width = widget.width
            tmp.height = widget.height
            all = all?.union(tmp) ?: Rectangle(tmp)
        }
        return all
    }
}
