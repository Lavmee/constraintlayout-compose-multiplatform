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
class HChain : Chain {
    inner class HAnchor(side: Constraint.HSide) : Anchor(Constraint.Side.valueOf(side.name))

    private val mLeft = HAnchor(Constraint.HSide.LEFT)
    private val mRight = HAnchor(Constraint.HSide.RIGHT)
    private val mStart = HAnchor(Constraint.HSide.START)
    private val mEnd = HAnchor(Constraint.HSide.END)

    constructor(name: String) : super(name) {
        mType = HelperType(typeMap[Type.HORIZONTAL_CHAIN]!!)
    }

    constructor(name: String, config: String) : super(name) {
        this.mConfig = config
        mType = HelperType(typeMap[Type.HORIZONTAL_CHAIN]!!)
        configMap = convertConfigToMap()!!.toMutableMap()
        if (configMap!!.containsKey("contains")) {
            Ref.addStringToReferences(configMap!!["contains"], references)
        }
    }

    /**
     * Get the left anchor
     *
     * @return the left anchor
     */
    fun getLeft(): HAnchor {
        return mLeft
    }

    /**
     * Connect anchor to Left
     *
     * @param anchor anchor to be connected
     */
    fun linkToLeft(anchor: Constraint.HAnchor?) {
        linkToLeft(anchor, 0)
    }

    /**
     * Connect anchor to Left
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    fun linkToLeft(anchor: Constraint.HAnchor?, margin: Int) {
        linkToLeft(anchor, margin, Int.MIN_VALUE)
    }

    /**
     * Connect anchor to Left
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    fun linkToLeft(anchor: Constraint.HAnchor?, margin: Int, goneMargin: Int) {
        mLeft.mConnection = anchor
        mLeft.mMargin = margin
        mLeft.mGoneMargin = goneMargin
        configMap!!["left"] = mLeft.toString()
    }

    /**
     * Get the right anchor
     *
     * @return the right anchor
     */
    fun getRight(): HAnchor {
        return mRight
    }

    /**
     * Connect anchor to Right
     *
     * @param anchor anchor to be connected
     */
    fun linkToRight(anchor: Constraint.HAnchor?) {
        linkToRight(anchor, 0)
    }

    /**
     * Connect anchor to Right
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    fun linkToRight(anchor: Constraint.HAnchor?, margin: Int) {
        linkToRight(anchor, margin, Int.MIN_VALUE)
    }

    /**
     * Connect anchor to Right
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    fun linkToRight(anchor: Constraint.HAnchor?, margin: Int, goneMargin: Int) {
        mRight.mConnection = anchor
        mRight.mMargin = margin
        mRight.mGoneMargin = goneMargin
        configMap!!["right"] = mRight.toString()
    }

    /**
     * Get the start anchor
     *
     * @return the start anchor
     */
    fun getStart(): HAnchor {
        return mStart
    }

    /**
     * Connect anchor to Start
     *
     * @param anchor anchor to be connected
     */
    fun linkToStart(anchor: Constraint.HAnchor?) {
        linkToStart(anchor, 0)
    }

    /**
     * Connect anchor to Start
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    fun linkToStart(anchor: Constraint.HAnchor?, margin: Int) {
        linkToStart(anchor, margin, Int.MIN_VALUE)
    }

    /**
     * Connect anchor to Start
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    fun linkToStart(anchor: Constraint.HAnchor?, margin: Int, goneMargin: Int) {
        mStart.mConnection = anchor
        mStart.mMargin = margin
        mStart.mGoneMargin = goneMargin
        configMap!!["start"] = mStart.toString()
    }

    /**
     * Get the end anchor
     *
     * @return the end anchor
     */
    fun getEnd(): HAnchor {
        return mEnd
    }

    /**
     * Connect anchor to End
     *
     * @param anchor anchor to be connected
     */
    fun linkToEnd(anchor: Constraint.HAnchor?) {
        linkToEnd(anchor, 0)
    }

    /**
     * Connect anchor to End
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    fun linkToEnd(anchor: Constraint.HAnchor?, margin: Int) {
        linkToEnd(anchor, margin, Int.MIN_VALUE)
    }

    /**
     * Connect anchor to End
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    fun linkToEnd(anchor: Constraint.HAnchor?, margin: Int, goneMargin: Int) {
        mEnd.mConnection = anchor
        mEnd.mMargin = margin
        mEnd.mGoneMargin = goneMargin
        configMap!!["end"] = mEnd.toString()
    }
}
