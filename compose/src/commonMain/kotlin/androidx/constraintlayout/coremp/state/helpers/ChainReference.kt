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

import androidx.constraintlayout.coremp.state.HelperReference
import androidx.constraintlayout.coremp.state.State
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.UNKNOWN

open class ChainReference(state: State, type: State.Helper) : HelperReference(state, type) {

    protected var mBias = 0.5f

    @Deprecated("Unintended visibility, use {@link #getWeight(String)} instead")
    // TODO(b/253515185): Change to private visibility once we change major version
    protected var mMapWeights = HashMap<String, Float>()

    @Deprecated("Unintended visibility, use {@link #getPreMargin(String)} instead")
    // TODO(b/253515185): Change to private visibility once we change major version
    protected var mMapPreMargin = HashMap<String, Float>()

    @Deprecated("Unintended visibility, use {@link #getPostMargin(String)} instead")
    // TODO(b/253515185): Change to private visibility once we change major version
    protected var mMapPostMargin = HashMap<String, Float>()

    private var mMapPreGoneMargin: HashMap<String, Float>? = null
    private var mMapPostGoneMargin: HashMap<String, Float>? = null

    protected var mStyle = State.Chain.SPREAD

    fun getStyle(): State.Chain {
        return State.Chain.SPREAD
    }

    /**
     * Sets the [style][State.Chain].
     *
     * @param style Defines the way the chain will lay out its elements
     * @return This same instance
     */
    fun style(style: State.Chain): ChainReference {
        mStyle = style
        return this
    }

    /**
     * Adds the element by the given id to the Chain.
     *
     * The order in which the elements are added is important. It will represent the element's
     * position in the Chain.
     *
     * @param id         Id of the element to add
     * @param weight     Weight used to distribute remaining space to each element
     * @param preMargin  Additional space in pixels between the added element and the previous one
     * (if any)
     * @param postMargin Additional space in pixels between the added element and the next one (if
     * any)
     */
    fun addChainElement(
        id: String,
        weight: Float,
        preMargin: Float,
        postMargin: Float,
    ) {
        addChainElement(id, weight, preMargin, postMargin, 0f, 0f)
    }

    /**
     * Adds the element by the given id to the Chain.
     *
     * The object's [Object.toString] result will be used to map the given margins and
     * weight to it, so it must stable and comparable.
     *
     * The order in which the elements are added is important. It will represent the element's
     * position in the Chain.
     *
     * @param id             Id of the element to add
     * @param weight         Weight used to distribute remaining space to each element
     * @param preMargin      Additional space in pixels between the added element and the
     * previous one
     * (if any)
     * @param postMargin     Additional space in pixels between the added element and the next
     * one (if
     * any)
     * @param preGoneMargin  Additional space in pixels between the added element and the previous
     * one (if any) when the previous element has Gone visibility
     * @param postGoneMargin Additional space in pixels between the added element and the next
     * one (if any) when the next element has Gone visibility
     */
    // @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun addChainElement(
        id: Any,
        weight: Float,
        preMargin: Float,
        postMargin: Float,
        preGoneMargin: Float,
        postGoneMargin: Float,
    ) {
        super.add(id) // Add element id as is, it's expected to return the same given instance
        val idString = id.toString()
        if (!weight.isNaN()) {
            mMapWeights[idString] = weight
        }
        if (!preMargin.isNaN()) {
            mMapPreMargin[idString] = preMargin
        }
        if (!postMargin.isNaN()) {
            mMapPostMargin[idString] = postMargin
        }
        if (!preGoneMargin.isNaN()) {
            if (mMapPreGoneMargin == null) {
                mMapPreGoneMargin = HashMap()
            }
            mMapPreGoneMargin!![idString] = preGoneMargin
        }
        if (!postGoneMargin.isNaN()) {
            if (mMapPostGoneMargin == null) {
                mMapPostGoneMargin = HashMap()
            }
            mMapPostGoneMargin!![idString] = postGoneMargin
        }
    }

    protected fun getWeight(id: String): Float {
        return if (mMapWeights.containsKey(id)) {
            mMapWeights[id]!!
        } else {
            UNKNOWN.toFloat()
        }
    }

    protected fun getPostMargin(id: String): Float {
        return if (mMapPostMargin.containsKey(id)) {
            mMapPostMargin[id]!!
        } else {
            0f
        }
    }

    protected fun getPreMargin(id: String): Float {
        return if (mMapPreMargin.containsKey(id)) {
            mMapPreMargin[id]!!
        } else {
            0f
        }
    }

    fun getPostGoneMargin(id: String): Float {
        return if (mMapPostGoneMargin != null && mMapPostGoneMargin!!.containsKey(id)) {
            mMapPostGoneMargin!![id]!!
        } else {
            0f
        }
    }

    fun getPreGoneMargin(id: String): Float {
        return if (mMapPreGoneMargin != null && mMapPreGoneMargin!!.containsKey(id)) {
            mMapPreGoneMargin!![id]!!
        } else {
            0f
        }
    }

    fun getBias(): Float {
        return mBias
    }

    // @TODO: add description
    override fun bias(bias: Float): ChainReference {
        mBias = bias
        return this
    }
}
