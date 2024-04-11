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
import androidx.constraintlayout.coremp.state.HelperReference
import androidx.constraintlayout.coremp.state.State
import androidx.constraintlayout.coremp.widgets.Barrier
import androidx.constraintlayout.coremp.widgets.HelperWidget

class BarrierReference(
    state: State,
) : HelperReference(state, State.Helper.BARRIER) {

    private var mDirection: State.Direction? = null
    private var mMargin = 0
    private var mBarrierWidget: Barrier? = null

    fun setBarrierDirection(barrierDirection: State.Direction) {
        mDirection = barrierDirection
    }

    override fun margin(marginValue: Any): ConstraintReference {
        margin(mHelperState.convertDimension(marginValue))
        return this
    }

    // @TODO: add description
    override fun margin(value: Int): ConstraintReference {
        mMargin = value
        return this
    }

    override val helperWidget: HelperWidget
        get() {
            if (mBarrierWidget == null) {
                mBarrierWidget = Barrier()
            }
            return mBarrierWidget!!
        }

    // @TODO: add description
    override fun apply() {
        helperWidget
        var direction = Barrier.LEFT
        when (mDirection) {
            State.Direction.LEFT, State.Direction.START -> {}
            State.Direction.RIGHT, State.Direction.END -> {
                // TODO: handle RTL
                direction = Barrier.RIGHT
            }

            State.Direction.TOP -> {
                direction = Barrier.TOP
            }

            State.Direction.BOTTOM -> {
                direction = Barrier.BOTTOM
            }

            else -> {}
        }
        mBarrierWidget!!.setBarrierType(direction)
        mBarrierWidget!!.setMargin(mMargin)
    }
}
