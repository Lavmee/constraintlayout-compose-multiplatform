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
package androidx.constraintlayout.coremp

/**
 * Utility to draw an histogram
 */
class HistogramCounter(val mName: String) {
    var mCalls = LongArray(256)
    fun inc(value: Int) {
        if (value < 255) {
            mCalls[value]++
        } else {
            mCalls[255]++
        }
    }

    fun reset() {
        mCalls = LongArray(256)
    }

    private fun print(n: Long): String {
        var ret = ""
        for (i in 0 until n) {
            ret += "X"
        }
        return ret
    }

    override fun toString(): String {
        var ret = "$mName :\n"
        var lastValue = 255
        for (i in 255 downTo 0) {
            if (mCalls[i] != 0L) {
                lastValue = i
                break
            }
        }
        var total = 0
        for (i in 0..lastValue) {
            ret += "[" + i + "] = " + mCalls[i] + " -> " + print(mCalls[i]) + "\n"
            total += mCalls[i].toInt()
        }
        ret += "Total calls $total"
        return ret
    }
}
