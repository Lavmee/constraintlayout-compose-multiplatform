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

@Suppress("UNUSED")
class KeyCycle(frame: Int, target: String) : KeyAttribute(frame, target) {
    private var mWaveShape: Wave? = null
    private var mWavePeriod = Float.NaN
    private var mWaveOffset = Float.NaN
    private var mWavePhase = Float.NaN

    init {
        TYPE = "KeyCycle"
    }

    enum class Wave {
        SIN,
        SQUARE,
        TRIANGLE,
        SAW,
        REVERSE_SAW,
        COS,
    }

    fun getShape(): Wave? {
        return mWaveShape
    }

    fun setShape(waveShape: Wave) {
        mWaveShape = waveShape
    }

    fun getPeriod(): Float {
        return mWavePeriod
    }

    fun setPeriod(wavePeriod: Float) {
        mWavePeriod = wavePeriod
    }

    fun getOffset(): Float {
        return mWaveOffset
    }

    fun setOffset(waveOffset: Float) {
        mWaveOffset = waveOffset
    }

    fun getPhase(): Float {
        return mWavePhase
    }

    fun setPhase(wavePhase: Float) {
        mWavePhase = wavePhase
    }

    override fun attributesToString(builder: StringBuilder) {
        super.attributesToString(builder)
        if (mWaveShape != null) {
            builder.append("shape:'").append(mWaveShape).append("',\n")
        }
        append(builder, "period", mWavePeriod)
        append(builder, "offset", mWaveOffset)
        append(builder, "phase", mWavePhase)
    }

    companion object {
        private const val TAG = "KeyCycle"
    }
}
