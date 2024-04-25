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
class VChain : Chain {
    inner class VAnchor(side: Constraint.VSide) : Anchor(Constraint.Side.valueOf(side.name))

    private val mTop = VAnchor(Constraint.VSide.TOP)
    private val mBottom = VAnchor(Constraint.VSide.BOTTOM)
    private val mBaseline = VAnchor(Constraint.VSide.BASELINE)

    constructor(name: String) : super(name) {
        mType = HelperType(typeMap[Type.VERTICAL_CHAIN]!!)
    }

    constructor(name: String, config: String) : super(name) {
        this.mConfig = config
        mType = HelperType(typeMap[Type.VERTICAL_CHAIN]!!)
        configMap = convertConfigToMap()!!.toMutableMap()
        if (configMap!!.containsKey("contains")) {
            Ref.addStringToReferences(configMap!!["contains"], references)
        }
    }

    /**
     * Get the top anchor
     *
     * @return the top anchor
     */
    fun getTop(): VAnchor {
        return mTop
    }

    /**
     * Connect anchor to Top
     *
     * @param anchor anchor to be connected
     */
    fun linkToTop(anchor: Constraint.VAnchor?) {
        linkToTop(anchor, 0)
    }

    /**
     * Connect anchor to Top
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    fun linkToTop(anchor: Constraint.VAnchor?, margin: Int) {
        linkToTop(anchor, margin, Int.MIN_VALUE)
    }

    /**
     * Connect anchor to Top
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    fun linkToTop(anchor: Constraint.VAnchor?, margin: Int, goneMargin: Int) {
        mTop.mConnection = anchor
        mTop.mMargin = margin
        mTop.mGoneMargin = goneMargin
        configMap!!["top"] = mTop.toString()
    }

    /**
     * Get the bottom anchor
     *
     * @return the bottom anchor
     */
    fun getBottom(): VAnchor {
        return mBottom
    }

    /**
     * Connect anchor to Bottom
     *
     * @param anchor anchor to be connected
     */
    fun linkToBottom(anchor: Constraint.VAnchor?) {
        linkToBottom(anchor, 0)
    }

    /**
     * Connect anchor to Bottom
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    fun linkToBottom(anchor: Constraint.VAnchor?, margin: Int) {
        linkToBottom(anchor, margin, Int.MIN_VALUE)
    }

    /**
     * Connect anchor to Bottom
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    fun linkToBottom(anchor: Constraint.VAnchor?, margin: Int, goneMargin: Int) {
        mBottom.mConnection = anchor
        mBottom.mMargin = margin
        mBottom.mGoneMargin = goneMargin
        configMap!!["bottom"] = mBottom.toString()
    }

    /**
     * Get the baseline anchor
     *
     * @return the baseline anchor
     */
    fun getBaseline(): VAnchor {
        return mBaseline
    }

    /**
     * Connect anchor to Baseline
     *
     * @param anchor anchor to be connected
     */
    fun linkToBaseline(anchor: Constraint.VAnchor?) {
        linkToBaseline(anchor, 0)
    }

    /**
     * Connect anchor to Baseline
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     */
    fun linkToBaseline(anchor: Constraint.VAnchor?, margin: Int) {
        linkToBaseline(anchor, margin, Int.MIN_VALUE)
    }

    /**
     * Connect anchor to Baseline
     *
     * @param anchor anchor to be connected
     * @param margin value of the margin
     * @param goneMargin value of the goneMargin
     */
    fun linkToBaseline(anchor: Constraint.VAnchor?, margin: Int, goneMargin: Int) {
        mBaseline.mConnection = anchor
        mBaseline.mMargin = margin
        mBaseline.mGoneMargin = goneMargin
        configMap!!["baseline"] = mBaseline.toString()
    }
}
