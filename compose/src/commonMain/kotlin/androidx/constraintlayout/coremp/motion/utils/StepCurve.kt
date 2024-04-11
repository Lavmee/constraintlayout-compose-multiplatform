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
package androidx.constraintlayout.coremp.motion.utils

import androidx.constraintlayout.coremp.ext.limitDecimals

class StepCurve : Easing {
    var mCurveFit: MonotonicCurveFit

    constructor(configString: String) {
        // done this way for efficiency
        mStr = configString
        val values = DoubleArray(mStr.length / 2)
        var start = configString.indexOf('(') + 1
        var off1 = configString.indexOf(',', start)
        var count = 0
        while (off1 != -1) {
            val tmp = configString.substring(start, off1).trim()
            values[count++] = tmp.toDouble()
            off1 = configString.indexOf(',', (off1 + 1).also { start = it })
        }
        off1 = configString.indexOf(')', start)
        val tmp = configString.substring(start, off1).trim()
        values[count++] = tmp.toDouble()

        mCurveFit = genSpline(values.copyOf(count))
    }

    // @TODO: add description
    override fun getDiff(x: Double): Double {
        return mCurveFit.getSlope(x, 0)
    }

    // @TODO: add description
    override operator fun get(x: Double): Double {
        return mCurveFit.getPos(x, 0)
    }

    companion object {
        private const val DEBUG = false

        @Suppress("unused")
        private fun genSpline(str: String): MonotonicCurveFit {
            val sp = str.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val values = DoubleArray(sp.size)
            for (i in values.indices) {
                values[i] = sp[i].toDouble()
            }
            return genSpline(values)
        }

        private fun genSpline(values: DoubleArray): MonotonicCurveFit {
            val length = values.size * 3 - 2
            val len = values.size - 1
            val gap = 1.0 / len
            val points = Array(length) { DoubleArray(1) }
            val time = DoubleArray(length)
            for (i in values.indices) {
                val v = values[i]
                points[i + len][0] = v
                time[i + len] = i * gap
                if (i > 0) {
                    points[i + len * 2][0] = v + 1
                    time[i + len * 2] = i * gap + 1
                    points[i - 1][0] = v - 1 - gap
                    time[i - 1] = i * gap + -1 - gap
                }
            }
            if (DEBUG) {
                var t = "t "
                var v = "v "
                for (i in time.indices) {
                    t += limitDecimals(time[i], 2) + " "
                    v += limitDecimals(points[i][0], 2) + " "
                }
                println(t)
                println(v)
            }
            val ms = MonotonicCurveFit(time, points)
            println(" 0 " + ms.getPos(0.0, 0))
            println(" 1 " + ms.getPos(1.0, 0))
            return ms
        }
    }
}
