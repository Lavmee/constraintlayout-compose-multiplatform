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

import androidx.constraintlayout.coremp.state.Reference
import androidx.constraintlayout.coremp.state.State
import androidx.constraintlayout.coremp.widgets.ConstraintWidget
import androidx.constraintlayout.coremp.widgets.Guideline

class GuidelineReference(state: State) : Facade, Reference {

    val mState: State = state
    private var mOrientation = 0
    private var mGuidelineWidget: Guideline? = null
    private var mStart = -1
    private var mEnd = -1
    private var mPercent = 0f

    private var mKey: Any? = null

    override fun setKey(key: Any?) {
        mKey = key
    }

    override fun getKey(): Any? {
        return mKey
    }

    // @TODO: add description
    fun start(margin: Any?): GuidelineReference {
        mStart = mState.convertDimension(margin!!)
        mEnd = -1
        mPercent = 0f
        return this
    }

    // @TODO: add description
    fun end(margin: Any?): GuidelineReference {
        mStart = -1
        mEnd = mState.convertDimension(margin!!)
        mPercent = 0f
        return this
    }

    // @TODO: add description
    fun percent(percent: Float): GuidelineReference {
        mStart = -1
        mEnd = -1
        mPercent = percent
        return this
    }

    fun setOrientation(orientation: Int) {
        mOrientation = orientation
    }

    fun getOrientation(): Int {
        return mOrientation
    }

    // @TODO: add description
    override fun apply() {
        mGuidelineWidget!!.setOrientation(mOrientation)
        if (mStart != -1) {
            mGuidelineWidget!!.setGuideBegin(mStart)
        } else if (mEnd != -1) {
            mGuidelineWidget!!.setGuideEnd(mEnd)
        } else {
            mGuidelineWidget!!.setGuidePercent(mPercent)
        }
    }

    override fun getFacade(): Facade? {
        return null
    }

    override fun getConstraintWidget(): ConstraintWidget {
        if (mGuidelineWidget == null) {
            mGuidelineWidget = Guideline()
        }
        return mGuidelineWidget!!
    }

    override fun setConstraintWidget(widget: ConstraintWidget?) {
        mGuidelineWidget = if (widget is Guideline) {
            widget
        } else {
            null
        }
    }
}
