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

/**
 * Possible directions for a connection
 */
enum class Direction(
    /**
     * get the direction as an integer
     *
     * @return direction as an integer
     */
    val direction: Int,
) {
    NORTH(0),
    SOUTH(1),
    WEST(2),
    EAST(3),
    BASE(4),
    ;

    override fun toString(): String {
        return when (this) {
            NORTH -> "N"
            SOUTH -> "S"
            EAST -> "E"
            WEST -> "W"
            BASE -> "B"
        }
    }

    val opposite: Direction
        /**
         * gets the opposite direction
         *
         * @return the opposite direction
         */
        get() = when (this) {
            NORTH -> SOUTH
            SOUTH -> NORTH
            EAST -> WEST
            WEST -> EAST
            BASE -> BASE
        }

    /**
     * Directions can be a positive or negative (right and down) being positive
     * reverse indicates the direction is negative
     *
     * @return true for north and east
     */
    fun reverse(): Boolean {
        return this == NORTH || this == WEST
    }

    /**
     * Return the number of connection types support by this direction
     *
     * @return number of types allowed for this connection
     */
    fun connectTypes(): Int {
        return when (this) {
            NORTH, SOUTH -> 2
            EAST, WEST -> 2
            BASE -> 1
        }
    }

    companion object {
        const val ORIENTATION_VERTICAL = 0
        const val ORIENTATION_HORIZONTAL = 1

        /**
         * Get an array of all directions
         *
         * @return array of all directions
         */
        val allDirections = entries.toTypedArray()
        private val sVertical = arrayOf(NORTH, SOUTH, BASE)
        private val sHorizontal = arrayOf(WEST, EAST)

        /**
         * get a String representing the direction integer
         *
         * @param directionInteger direction as an integer
         * @return single letter string to describe the direction
         */
        fun toString(directionInteger: Int): String {
            return Companion[directionInteger].toString()
        }

        /**
         * convert from an ordinal of direction to actual direction
         *
         * @return Enum member equivalent to integer
         */
        operator fun get(directionInteger: Int): Direction {
            return allDirections[directionInteger]
        }

        /**
         * gets the viable directions for horizontal or vertical
         *
         * @param orientation 0 = vertical 1 = horizontal
         * @return array of directions for vertical or horizontal
         */
        fun getDirections(orientation: Int): Array<Direction> {
            return if (orientation == ORIENTATION_VERTICAL) {
                sVertical
            } else {
                sHorizontal
            }
        }
    }
}
