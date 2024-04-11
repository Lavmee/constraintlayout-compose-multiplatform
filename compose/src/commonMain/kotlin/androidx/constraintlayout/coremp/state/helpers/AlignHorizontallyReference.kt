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

class AlignHorizontallyReference(
    state: State,
) : HelperReference(state, State.Helper.ALIGN_VERTICALLY) {

    private val mBias = 0.5f

    override fun apply() {
        for (key in mReferences) {
            val reference: ConstraintReference? = mHelperState.constraints(key)
            reference!!.clearHorizontal()
            if (mStartToStart != null) {
                reference.startToStart(mStartToStart!!)
            } else if (mStartToEnd != null) {
                reference.startToEnd(mStartToEnd!!)
            } else {
                reference.startToStart(State.PARENT)
            }
            if (mEndToStart != null) {
                reference.endToStart(mEndToStart!!)
            } else if (mEndToEnd != null) {
                reference.endToEnd(mEndToEnd!!)
            } else {
                reference.endToEnd(State.PARENT)
            }
            if (mBias != 0.5f) {
                reference.horizontalBias(mBias)
            }
        }
    }
}
