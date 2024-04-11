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
package androidx.constraintlayout.coremp.dsl

abstract class Guideline(name: String) : Helper(name, HelperType("")) {
    private var mStart = Int.MIN_VALUE
    private var mEnd = Int.MIN_VALUE
    private var mPercent = Float.NaN

    /**
     * Get the start position
     *
     * @return start position
     */
    @Suppress("UNUSED")
    open fun getStart(): Int {
        return mStart
    }

    /**
     * Set the start position
     *
     * @param start the start position
     */
    open fun setStart(start: Int) {
        mStart = start
        configMap!!["start"] = mStart.toString()
    }

    /**
     * Get the end position
     *
     * @return end position
     */
    @Suppress("UNUSED")
    open fun getEnd(): Int {
        return mEnd
    }

    /**
     * Set the end position
     *
     * @param end the end position
     */
    open fun setEnd(end: Int) {
        mEnd = end
        configMap!!["end"] = mEnd.toString()
    }

    /**
     * Get the position in percent
     *
     * @return position in percent
     */
    @Suppress("UNUSED")
    open fun getPercent(): Float {
        return mPercent
    }

    /**
     * Set the position in percent
     *
     * @param percent the position in percent
     */
    open fun setPercent(percent: Float) {
        mPercent = percent
        configMap!!["percent"] = mPercent.toString()
    }
}
