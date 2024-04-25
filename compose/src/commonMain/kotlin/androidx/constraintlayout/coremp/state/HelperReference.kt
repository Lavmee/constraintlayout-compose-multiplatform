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
package androidx.constraintlayout.coremp.state

import androidx.constraintlayout.coremp.state.helpers.Facade
import androidx.constraintlayout.coremp.widgets.ConstraintWidget
import androidx.constraintlayout.coremp.widgets.HelperWidget

open class HelperReference(
    state: State,
    type: State.Helper,
) : ConstraintReference(state), Facade {
    protected val mHelperState: State = state
    val mType: State.Helper = type
    var mReferences = ArrayList<Any>()
    private var mHelperWidget: HelperWidget? = null

    fun getType(): State.Helper {
        return mType
    }

    // @TODO: add description
    fun add(vararg objects: Any): HelperReference {
        mReferences.addAll(objects)
        return this
    }

    open fun setHelperWidget(helperWidget: HelperWidget) {
        mHelperWidget = helperWidget
    }

    open val helperWidget: HelperWidget? get() = mHelperWidget

    override fun getConstraintWidget(): ConstraintWidget? {
        return helperWidget
    }

    // @TODO: add description
    override fun apply() {
        // nothing
    }

    /**
     * Allows the derived classes to invoke the apply method in the ConstraintReference
     */
    fun applyBase() {
        super.apply()
    }
}
