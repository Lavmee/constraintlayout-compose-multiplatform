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

// the default is evenly spaced  1 at 50, 2 at 33 & 66, 3 at 25,50,75
@Suppress("UNUSED")
class KeyPositions(numOfFrames: Int, vararg targets: String) : Keys() {
    private var mTarget: Array<out String> = targets
    private var mTransitionEasing: String? = null
    private var mPositionType: Type? = null

    private var mFrames: IntArray
    private var mPercentWidth: FloatArray? = null
    private var mPercentHeight: FloatArray? = null
    private var mPercentX: FloatArray? = null
    private var mPercentY: FloatArray? = null

    enum class Type {
        CARTESIAN,
        SCREEN,
        PATH,
    }

    init {
        mFrames = IntArray(numOfFrames)
        val gap: Float = 100f / (mFrames.size + 1)
        for (i in mFrames.indices) {
            mFrames[i] = (i * gap + gap).toInt()
        }
    }

    fun getTransitionEasing(): String? {
        return mTransitionEasing
    }

    fun setTransitionEasing(transitionEasing: String) {
        mTransitionEasing = transitionEasing
    }

    fun getFrames(): IntArray {
        return mFrames
    }

    fun setFrames(vararg frames: Int) {
        mFrames = frames
    }

    fun getPercentWidth(): FloatArray? {
        return mPercentWidth
    }

    fun setPercentWidth(vararg percentWidth: Float) {
        mPercentWidth = percentWidth
    }

    fun getPercentHeight(): FloatArray? {
        return mPercentHeight
    }

    fun setPercentHeight(vararg percentHeight: Float) {
        mPercentHeight = percentHeight
    }

    fun getPercentX(): FloatArray? {
        return mPercentX
    }

    fun setPercentX(vararg percentX: Float) {
        mPercentX = percentX
    }

    fun getPercentY(): FloatArray? {
        return mPercentY
    }

    fun setPercentY(vararg percentY: Float) {
        mPercentY = percentY
    }

    fun getPositionType(): Type? {
        return mPositionType
    }

    fun setPositionType(positionType: Type) {
        mPositionType = positionType
    }

    fun getTarget(): Array<out String> {
        return mTarget
    }

    override fun toString(): String {
        val ret = StringBuilder()
        ret.append("KeyPositions:{\n")
        append(ret, "target", mTarget)
        ret.append("frame:").append(mFrames.contentToString()).append(",\n")
        if (mPositionType != null) {
            ret.append("type:'").append(mPositionType).append("',\n")
        }
        append(ret, "easing", mTransitionEasing)
        append(ret, "percentX", mPercentX)
        append(ret, "percentX", mPercentY)
        append(ret, "percentWidth", mPercentWidth)
        append(ret, "percentHeight", mPercentHeight)
        ret.append("},\n")
        return ret.toString()
    }
}
