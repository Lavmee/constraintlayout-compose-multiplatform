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
package androidx.constraintlayout.coremp.state.helpers

import androidx.constraintlayout.coremp.state.ConstraintReference
import androidx.constraintlayout.coremp.state.State
import androidx.constraintlayout.coremp.widgets.ConstraintWidget
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.UNKNOWN

class VerticalChainReference(state: State) : ChainReference(state, State.Helper.VERTICAL_CHAIN) {

    // @TODO: add description
    override fun apply() {
        var first: ConstraintReference? = null
        var previous: ConstraintReference? = null
        for (key in mReferences) {
            val reference = mHelperState.constraints(key)
            reference!!.clearVertical()
        }
        for (key in mReferences) {
            val reference = mHelperState.constraints(key)
            if (first == null) {
                first = reference
                if (mTopToTop != null) {
                    first!!.topToTop(mTopToTop!!).margin(mMarginTop).marginGone(mMarginTopGone)
                } else if (mTopToBottom != null) {
                    first!!.topToBottom(mTopToBottom!!).margin(mMarginTop)
                        .marginGone(mMarginTopGone)
                } else {
                    // No constraint declared, default to Parent.
                    val refKey = reference!!.getKey().toString()
                    first!!.topToTop(State.PARENT).margin(getPreMargin(refKey)).marginGone(
                        getPreGoneMargin(refKey),
                    )
                }
            }
            if (previous != null) {
                val preKey = previous.getKey().toString()
                val refKey = reference!!.getKey().toString()
                previous.bottomToTop(reference.getKey()!!).margin(getPostMargin(preKey)).marginGone(
                    getPostGoneMargin(preKey),
                )
                reference.topToBottom(previous.getKey()!!).margin(getPreMargin(refKey)).marginGone(
                    getPreGoneMargin(refKey),
                )
            }
            val weight = getWeight(key.toString())
            if (weight != UNKNOWN.toFloat()) {
                reference!!.setVerticalChainWeight(weight)
            }
            previous = reference
        }
        if (previous != null) {
            if (mBottomToTop != null) {
                previous.bottomToTop(mBottomToTop!!)
                    .margin(mMarginBottom)
                    .marginGone(mMarginBottomGone)
            } else if (mBottomToBottom != null) {
                previous.bottomToBottom(mBottomToBottom!!)
                    .margin(mMarginBottom)
                    .marginGone(mMarginBottomGone)
            } else {
                // No constraint declared, default to Parent.
                val preKey = previous.getKey().toString()
                previous.bottomToBottom(State.PARENT).margin(getPostMargin(preKey)).marginGone(
                    getPostGoneMargin(preKey),
                )
            }
        }
        if (first == null) {
            return
        }
        if (mBias != 0.5f) {
            first.verticalBias(mBias)
        }
        when (mStyle) {
            State.Chain.SPREAD -> {
                first.setVerticalChainStyle(ConstraintWidget.CHAIN_SPREAD)
            }

            State.Chain.SPREAD_INSIDE -> {
                first.setVerticalChainStyle(ConstraintWidget.CHAIN_SPREAD_INSIDE)
            }

            State.Chain.PACKED -> {
                first.setVerticalChainStyle(ConstraintWidget.CHAIN_PACKED)
            }
        }
    }
}
