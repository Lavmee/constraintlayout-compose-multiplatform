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
class KeyCycles(numOfFrames: Int, vararg targets: String) : KeyAttributes(numOfFrames, *targets) {
    enum class Wave {
        SIN,
        SQUARE,
        TRIANGLE,
        SAW,
        REVERSE_SAW,
        COS,
    }

    private var mWaveShape: Wave? = null
    private var mWavePeriod: FloatArray? = null
    private var mWaveOffset: FloatArray? = null
    private var mWavePhase: FloatArray? = null

    init {
        TYPE = "KeyCycle"
    }

    fun getWaveShape(): Wave? {
        return mWaveShape
    }

    fun setWaveShape(waveShape: Wave) {
        mWaveShape = waveShape
    }

    fun getWavePeriod(): FloatArray? {
        return mWavePeriod
    }

    fun setWavePeriod(vararg wavePeriod: Float) {
        mWavePeriod = wavePeriod
    }

    fun getWaveOffset(): FloatArray? {
        return mWaveOffset
    }

    fun setWaveOffset(vararg waveOffset: Float) {
        mWaveOffset = waveOffset
    }

    fun getWavePhase(): FloatArray? {
        return mWavePhase
    }

    fun setWavePhase(vararg wavePhase: Float) {
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
}
