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
class KeyPosition(
    firstTarget: String,
    frame: Int,
) : Keys() {
    private var mTarget: String = firstTarget
    private var mTransitionEasing: String? = null
    private var mFrame = frame
    private var mPercentWidth = Float.NaN
    private var mPercentHeight = Float.NaN
    private var mPercentX = Float.NaN
    private var mPercentY = Float.NaN
    private var mPositionType: Type? = Type.CARTESIAN

    enum class Type {
        CARTESIAN,
        SCREEN,
        PATH,
    }

    fun getTransitionEasing(): String? {
        return mTransitionEasing
    }

    fun setTransitionEasing(transitionEasing: String) {
        mTransitionEasing = transitionEasing
    }

    fun getFrames(): Int {
        return mFrame
    }

    fun setFrames(frames: Int) {
        mFrame = frames
    }

    fun getPercentWidth(): Float {
        return mPercentWidth
    }

    fun setPercentWidth(percentWidth: Float) {
        mPercentWidth = percentWidth
    }

    fun getPercentHeight(): Float {
        return mPercentHeight
    }

    fun setPercentHeight(percentHeight: Float) {
        mPercentHeight = percentHeight
    }

    fun getPercentX(): Float {
        return mPercentX
    }

    fun setPercentX(percentX: Float) {
        mPercentX = percentX
    }

    fun getPercentY(): Float {
        return mPercentY
    }

    fun setPercentY(percentY: Float) {
        mPercentY = percentY
    }

    fun getPositionType(): Type? {
        return mPositionType
    }

    fun setPositionType(positionType: Type) {
        mPositionType = positionType
    }

    fun getTarget(): String {
        return mTarget
    }

    fun setTarget(target: String) {
        mTarget = target
    }

    override fun toString(): String {
        val ret = StringBuilder()
        ret.append("KeyPositions:{\n")
        append(ret, "target", mTarget)
        ret.append("frame:").append(mFrame).append(",\n")
        if (mPositionType != null) {
            ret.append("type:'").append(mPositionType).append("',\n")
        }
        append(ret, "easing", mTransitionEasing)
        append(ret, "percentX", mPercentX)
        append(ret, "percentY", mPercentY)
        append(ret, "percentWidth", mPercentWidth)
        append(ret, "percentHeight", mPercentHeight)
        ret.append("},\n")
        return ret.toString()
    }
}
