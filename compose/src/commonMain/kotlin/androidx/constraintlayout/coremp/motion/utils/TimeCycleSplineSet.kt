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
import androidx.constraintlayout.coremp.motion.CustomAttribute
import androidx.constraintlayout.coremp.motion.CustomVariable
import androidx.constraintlayout.coremp.motion.MotionWidget
import androidx.constraintlayout.coremp.motion.utils.KeyFrameArray.CustomArray
import androidx.constraintlayout.coremp.motion.utils.KeyFrameArray.CustomVar
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sign
import kotlin.math.sin

abstract class TimeCycleSplineSet {
    protected var mCurveFit: CurveFit? = null
    protected var mWaveShape = 0
    protected var mTimePoints = IntArray(10)
    protected var mValues = Array(10) { FloatArray(3) }
    protected var mCount = 0
    protected var mType: String? = null
    protected var mCache = FloatArray(3)
    protected val CURVE_VALUE = 0
    protected val CURVE_PERIOD = 1
    protected val CURVE_OFFSET = 2
    protected var sVal2PI = (2 * PI).toFloat()
    protected var mContinue = false
    protected var mLastTime: Long = 0
    protected var mLastCycle = Float.NaN

    override fun toString(): String {
        var str = mType!!
        for (i in 0 until mCount) {
            str += ("[" + mTimePoints[i]) + " , " + limitDecimals(mValues[i], 2) + "] "
        }
        return str
    }

    fun setType(type: String) {
        mType = type
    }

    /**
     * @param period cycles per second
     */
    protected fun calcWave(period: Float): Float {
        return when (mWaveShape) {
            Oscillator.SIN_WAVE -> sin((period * sVal2PI).toDouble()).toFloat()
            Oscillator.SQUARE_WAVE -> sign((period * sVal2PI).toDouble()).toFloat()
            Oscillator.TRIANGLE_WAVE -> (1 - abs(period.toDouble())).toFloat()
            Oscillator.SAW_WAVE -> (period * 2 + 1) % 2 - 1
            Oscillator.REVERSE_SAW_WAVE -> 1 - (period * 2 + 1) % 2
            Oscillator.COS_WAVE -> cos((period * sVal2PI).toDouble()).toFloat()
            Oscillator.BOUNCE -> {
                val x = (1 - abs((period * 4 % 4 - 2).toDouble())).toFloat()
                1 - x * x
            }

            else -> sin((period * sVal2PI).toDouble()).toFloat()
        }
    }

    fun getCurveFit(): CurveFit {
        return mCurveFit!!
    }

    protected fun setStartTime(currentTime: Long) {
        mLastTime = currentTime
    }

    // @TODO: add description
    open fun setPoint(position: Int, value: Float, period: Float, shape: Int, offset: Float) {
        mTimePoints[mCount] = position
        mValues[mCount][CURVE_VALUE] = value
        mValues[mCount][CURVE_PERIOD] = period
        mValues[mCount][CURVE_OFFSET] = offset
        mWaveShape = max(mWaveShape.toDouble(), shape.toDouble()).toInt() // the highest value shape is chosen
        mCount++
    }

    class CustomSet : TimeCycleSplineSet {
        var mAttributeName: String
        var mConstraintAttributeList: CustomArray
        var mWaveProperties = KeyFrameArray.FloatArray()
        var mTempValues: FloatArray? = null
        var mCustomCache: FloatArray? = null

        constructor(attribute: String, attrList: CustomArray) {
            mAttributeName = attribute.split(",")[1]
            mConstraintAttributeList = attrList
        }

        // @TODO: add description
        override fun setup(curveType: Int) {
            val size = mConstraintAttributeList.size()
            val dimensionality = mConstraintAttributeList.valueAt(0)!!.numberOfInterpolatedValues()
            val time = DoubleArray(size)
            mTempValues = FloatArray(dimensionality + 2)
            mCustomCache = FloatArray(dimensionality)
            val values = Array(size) { DoubleArray(dimensionality + 2) }
            for (i in 0 until size) {
                val key = mConstraintAttributeList.keyAt(i)
                val ca = mConstraintAttributeList.valueAt(i)!!
                val waveProp = mWaveProperties.valueAt(i)
                time[i] = key * 1E-2
                ca.getValuesToInterpolate(mTempValues!!)
                for (k in 0 until mTempValues!!.size) {
                    values[i][k] = mTempValues!![k].toDouble()
                }
                values[i][dimensionality] = waveProp[0].toDouble()
                values[i][dimensionality + 1] = waveProp[1].toDouble()
            }
            mCurveFit = CurveFit.get(curveType, time, values)
        }

        // @TODO: add description
        override fun setPoint(position: Int, value: Float, period: Float, shape: Int, offset: Float) {
            throw RuntimeException(
                "don't call for custom attribute " +
                    "call setPoint(pos, ConstraintAttribute,...)",
            )
        }

        // @TODO: add description
        fun setPoint(
            position: Int,
            value: CustomAttribute?,
            period: Float,
            shape: Int,
            offset: Float,
        ) {
            mConstraintAttributeList.append(position, value)
            mWaveProperties.append(position, floatArrayOf(period, offset))
            mWaveShape = max(mWaveShape.toDouble(), shape.toDouble()).toInt() // the highest value shape is chosen
        }

        // @TODO: add description
        fun setProperty(view: MotionWidget, t: Float, time: Long, cache: KeyCache): Boolean {
            mCurveFit!!.getPos(t.toDouble(), mTempValues!!)
            val period = mTempValues!![mTempValues!!.size - 2]
            val offset = mTempValues!![mTempValues!!.size - 1]
            val delta_time = time - mLastTime
            if (mLastCycle.isNaN()) { // it has not been set
                mLastCycle = cache.getFloatValue(view, mAttributeName, 0) // check the cache
                if (mLastCycle.isNaN()) { // not in cache so set to 0 (start)
                    mLastCycle = 0f
                }
            }
            mLastCycle = ((mLastCycle + delta_time * 1E-9 * period) % 1.0).toFloat()
            mLastTime = time
            val wave = calcWave(mLastCycle)
            mContinue = false
            for (i in 0 until mCustomCache!!.size) {
                mContinue = mContinue or (mTempValues!![i] != 0.0f)
                mCustomCache!![i] = mTempValues!![i] * wave + offset
            }
            view.setInterpolatedValue(mConstraintAttributeList.valueAt(0)!!, mCustomCache!!)
            if (period != 0.0f) {
                mContinue = true
            }
            return mContinue
        }
    }

    // @TODO: add description
    open fun setup(curveType: Int) {
        if (mCount == 0) {
            println("Error no points added to $mType")
            return
        }
        Sort.doubleQuickSort(mTimePoints, mValues, 0, mCount - 1)
        var unique = 0
        for (i in 1 until mTimePoints.size) {
            if (mTimePoints[i] != mTimePoints[i - 1]) {
                unique++
            }
        }
        if (unique == 0) {
            unique = 1
        }
        val time = DoubleArray(unique)
        val values = Array(unique) { DoubleArray(3) }
        var k = 0
        for (i in 0 until mCount) {
            if (i > 0 && mTimePoints[i] == mTimePoints[i - 1]) {
                continue
            }
            time[k] = mTimePoints[i] * 1E-2
            values[k][0] = mValues[i][0].toDouble()
            values[k][1] = mValues[i][1].toDouble()
            values[k][2] = mValues[i][2].toDouble()
            k++
        }
        mCurveFit = CurveFit.get(curveType, time, values)
    }

    protected object Sort {
        fun doubleQuickSort(key: IntArray, value: Array<FloatArray>, low: Int, hi: Int) {
            var low = low
            var hi = hi
            val stack = IntArray(key.size + 10)
            var count = 0
            stack[count++] = hi
            stack[count++] = low
            while (count > 0) {
                low = stack[--count]
                hi = stack[--count]
                if (low < hi) {
                    val p = partition(key, value, low, hi)
                    stack[count++] = p - 1
                    stack[count++] = low
                    stack[count++] = hi
                    stack[count++] = p + 1
                }
            }
        }

        private fun partition(array: IntArray, value: Array<FloatArray>, low: Int, hi: Int): Int {
            val pivot = array[hi]
            var i = low
            for (j in low until hi) {
                if (array[j] <= pivot) {
                    swap(array, value, i, j)
                    i++
                }
            }
            swap(array, value, i, hi)
            return i
        }

        private fun swap(array: IntArray, value: Array<FloatArray>, a: Int, b: Int) {
            val tmp = array[a]
            array[a] = array[b]
            array[b] = tmp
            val tmpv = value[a]
            value[a] = value[b]
            value[b] = tmpv
        }
    }

    class CustomVarSet : TimeCycleSplineSet {
        var mAttributeName: String
        var mConstraintAttributeList: CustomVar
        var mWaveProperties = KeyFrameArray.FloatArray()
        var mTempValues: FloatArray? = null
        var mCustomCache: FloatArray? = null

        constructor(attribute: String, attrList: CustomVar) {
            mAttributeName = attribute.split(",")[1]
            mConstraintAttributeList = attrList
        }

        // @TODO: add description
        override fun setup(curveType: Int) {
            val size = mConstraintAttributeList.size()
            val dimensionality = mConstraintAttributeList.valueAt(0).numberOfInterpolatedValues()
            val time = DoubleArray(size)
            mTempValues = FloatArray(dimensionality + 2)
            mCustomCache = FloatArray(dimensionality)
            val values = Array(size) { DoubleArray(dimensionality + 2) }
            for (i in 0 until size) {
                val key = mConstraintAttributeList.keyAt(i)
                val ca = mConstraintAttributeList.valueAt(i)
                val waveProp = mWaveProperties.valueAt(i)
                time[i] = key * 1E-2
                ca.getValuesToInterpolate(mTempValues!!)
                for (k in 0 until mTempValues!!.size) {
                    values[i][k] = mTempValues!![k].toDouble()
                }
                values[i][dimensionality] = waveProp[0].toDouble()
                values[i][dimensionality + 1] = waveProp[1].toDouble()
            }
            mCurveFit = CurveFit.get(curveType, time, values)
        }

        // @TODO: add description
        override fun setPoint(position: Int, value: Float, period: Float, shape: Int, offset: Float) {
            throw RuntimeException(
                "don't call for custom attribute " +
                    "call setPoint(pos, ConstraintAttribute,...)",
            )
        }

        // @TODO: add description
        fun setPoint(
            position: Int,
            value: CustomVariable?,
            period: Float,
            shape: Int,
            offset: Float,
        ) {
            mConstraintAttributeList.append(position, value)
            mWaveProperties.append(position, floatArrayOf(period, offset))
            mWaveShape = max(mWaveShape.toDouble(), shape.toDouble()).toInt() // the highest value shape is chosen
        }

        // @TODO: add description
        fun setProperty(view: MotionWidget?, t: Float, time: Long, cache: KeyCache): Boolean {
            mCurveFit!!.getPos(t.toDouble(), mTempValues!!)
            val period = mTempValues!![mTempValues!!.size - 2]
            val offset = mTempValues!![mTempValues!!.size - 1]
            val delta_time = time - mLastTime
            if (mLastCycle.isNaN()) { // it has not been set
                mLastCycle = cache.getFloatValue(view!!, mAttributeName, 0) // check the cache
                if (mLastCycle.isNaN()) { // not in cache so set to 0 (start)
                    mLastCycle = 0f
                }
            }
            mLastCycle = ((mLastCycle + delta_time * 1E-9 * period) % 1.0).toFloat()
            mLastTime = time
            val wave = calcWave(mLastCycle)
            mContinue = false
            for (i in 0 until mCustomCache!!.size) {
                mContinue = mContinue or (mTempValues!![i] != 0.0f)
                mCustomCache!![i] = mTempValues!![i] * wave + offset
            }
            mConstraintAttributeList.valueAt(0).setInterpolatedValue(view!!, mCustomCache!!)
            if (period != 0.0f) {
                mContinue = true
            }
            return mContinue
        }
    }

    companion object {
        private const val TAG = "SplineSet"
    }
}
