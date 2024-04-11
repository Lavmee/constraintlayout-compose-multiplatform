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
package androidx.constraintlayout.coremp.widgets

import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.MATCH_CONSTRAINT_PERCENT
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.MATCH_CONSTRAINT_RATIO
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.Companion.MATCH_CONSTRAINT_SPREAD
import androidx.constraintlayout.coremp.widgets.ConstraintWidget.DimensionBehaviour

class ChainHead {
    var mFirst: ConstraintWidget? = null
    var mFirstVisibleWidget: ConstraintWidget? = null
    var mLast: ConstraintWidget? = null
    var mLastVisibleWidget: ConstraintWidget? = null
    var mHead: ConstraintWidget? = null
    var mFirstMatchConstraintWidget: ConstraintWidget? = null
    var mLastMatchConstraintWidget: ConstraintWidget? = null
    var mWeightedMatchConstraintsWidgets: ArrayList<ConstraintWidget>? = null
    var mWidgetsCount = 0
    var mWidgetsMatchCount = 0
    var mTotalWeight = 0f
    var mVisibleWidgets = 0
    var mTotalSize = 0
    var mTotalMargins = 0
    var mOptimizable = false
    private var mOrientation = 0
    private var mIsRtl = false
    var mHasUndefinedWeights = false
    protected var mHasDefinedWeights = false
    var mHasComplexMatchWeights = false
    protected var mHasRatio = false
    private var mDefined = false

    /**
     * Initialize variables, then determine visible widgets, the head of chain and
     * matched constraint widgets.
     *
     * @param first       first widget in a chain
     * @param orientation orientation of the chain (either Horizontal or Vertical)
     * @param isRtl       Right-to-left layout flag to determine the actual head of the chain
     */
    constructor(first: ConstraintWidget, orientation: Int, isRtl: Boolean) {
        mFirst = first
        mOrientation = orientation
        mIsRtl = isRtl
    }

    private fun defineChainProperties() {
        val offset = mOrientation * 2
        var lastVisited = mFirst!!
        mOptimizable = true

        // TraverseChain
        var widget = mFirst!!
        var next = mFirst
        var done = false
        while (!done) {
            mWidgetsCount++
            widget.mNextChainWidget[mOrientation] = null
            widget.mListNextMatchConstraintsWidget[mOrientation] = null
            if (widget.visibility != ConstraintWidget.GONE) {
                mVisibleWidgets++
                if (widget.getDimensionBehaviour(mOrientation)
                    != DimensionBehaviour.MATCH_CONSTRAINT
                ) {
                    mTotalSize += widget.getLength(mOrientation)
                }
                mTotalSize += widget.mListAnchors[offset].margin
                mTotalSize += widget.mListAnchors[offset + 1].margin
                mTotalMargins += widget.mListAnchors[offset].margin
                mTotalMargins += widget.mListAnchors[offset + 1].margin
                // Visible widgets linked list.
                if (mFirstVisibleWidget == null) {
                    mFirstVisibleWidget = widget
                }
                mLastVisibleWidget = widget

                // Match constraint linked list.
                if (widget.mListDimensionBehaviors[mOrientation]
                    == DimensionBehaviour.MATCH_CONSTRAINT
                ) {
                    if ((
                            widget.mResolvedMatchConstraintDefault[mOrientation]
                                == MATCH_CONSTRAINT_SPREAD
                            ) || (
                            widget.mResolvedMatchConstraintDefault[mOrientation]
                                == MATCH_CONSTRAINT_RATIO
                            ) || (
                            widget.mResolvedMatchConstraintDefault[mOrientation]
                                == MATCH_CONSTRAINT_PERCENT
                            )
                    ) {
                        mWidgetsMatchCount++
                        // Note: Might cause an issue if we support MATCH_CONSTRAINT_RATIO_RESOLVED
                        // in chain optimization. (we currently don't)
                        val weight = widget.mWeight[mOrientation]
                        if (weight > 0) {
                            mTotalWeight += widget.mWeight[mOrientation]
                        }
                        if (isMatchConstraintEqualityCandidate(widget, mOrientation)) {
                            if (weight < 0) {
                                mHasUndefinedWeights = true
                            } else {
                                mHasDefinedWeights = true
                            }
                            if (mWeightedMatchConstraintsWidgets == null) {
                                mWeightedMatchConstraintsWidgets = ArrayList()
                            }
                            mWeightedMatchConstraintsWidgets!!.add(widget)
                        }
                        if (mFirstMatchConstraintWidget == null) {
                            mFirstMatchConstraintWidget = widget
                        }
                        if (mLastMatchConstraintWidget != null) {
                            mLastMatchConstraintWidget!!.mListNextMatchConstraintsWidget[mOrientation] =
                                widget
                        }
                        mLastMatchConstraintWidget = widget
                    }
                    if (mOrientation == ConstraintWidget.HORIZONTAL) {
                        if (widget.mMatchConstraintDefaultWidth
                            != MATCH_CONSTRAINT_SPREAD
                        ) {
                            mOptimizable = false
                        } else if (widget.mMatchConstraintMinWidth != 0 ||
                            widget.mMatchConstraintMaxWidth != 0
                        ) {
                            mOptimizable = false
                        }
                    } else {
                        if (widget.mMatchConstraintDefaultHeight
                            != MATCH_CONSTRAINT_SPREAD
                        ) {
                            mOptimizable = false
                        } else if (widget.mMatchConstraintMinHeight != 0 ||
                            widget.mMatchConstraintMaxHeight != 0
                        ) {
                            mOptimizable = false
                        }
                    }
                    if (widget.mDimensionRatio != 0.0f) {
                        // TODO: Improve (Could use ratio optimization).
                        mOptimizable = false
                        mHasRatio = true
                    }
                }
            }
            if (lastVisited != widget) {
                lastVisited.mNextChainWidget[mOrientation] = widget
            }
            lastVisited = widget

            // go to the next widget
            val nextAnchor = widget.mListAnchors[offset + 1].mTarget
            if (nextAnchor != null) {
                next = nextAnchor.mOwner
                if (next.mListAnchors[offset].mTarget == null ||
                    next.mListAnchors[offset].mTarget!!.mOwner != widget
                ) {
                    next = null
                }
            } else {
                next = null
            }
            if (next != null) {
                widget = next
            } else {
                done = true
            }
        }
        if (mFirstVisibleWidget != null) {
            mTotalSize -= mFirstVisibleWidget!!.mListAnchors[offset].margin
        }
        if (mLastVisibleWidget != null) {
            mTotalSize -= mLastVisibleWidget!!.mListAnchors[offset + 1].margin
        }
        mLast = widget
        mHead = if (mOrientation == ConstraintWidget.HORIZONTAL && mIsRtl) {
            mLast
        } else {
            mFirst
        }
        mHasComplexMatchWeights = mHasDefinedWeights && mHasUndefinedWeights
    }

    fun getFirst(): ConstraintWidget? {
        return mFirst
    }

    fun getFirstVisibleWidget(): ConstraintWidget? {
        return mFirstVisibleWidget
    }

    fun getLast(): ConstraintWidget? {
        return mLast
    }

    fun getLastVisibleWidget(): ConstraintWidget? {
        return mLastVisibleWidget
    }

    fun getHead(): ConstraintWidget? {
        return mHead
    }

    fun getFirstMatchConstraintWidget(): ConstraintWidget? {
        return mFirstMatchConstraintWidget
    }

    fun getLastMatchConstraintWidget(): ConstraintWidget? {
        return mLastMatchConstraintWidget
    }

    fun getTotalWeight(): Float {
        return mTotalWeight
    }

    // @TODO: add description
    fun define() {
        if (!mDefined) {
            defineChainProperties()
        }
        mDefined = true
    }

    companion object {
        /**
         * Returns true if the widget should be part of the match equality rules in the chain
         *
         * @param widget      the widget to test
         * @param orientation current orientation, HORIZONTAL or VERTICAL
         */
        private fun isMatchConstraintEqualityCandidate(
            widget: ConstraintWidget,
            orientation: Int,
        ): Boolean {
            return widget.visibility != ConstraintWidget.GONE &&
                (
                    widget.mListDimensionBehaviors[orientation]
                        == DimensionBehaviour.MATCH_CONSTRAINT
                    ) &&
                (
                    widget.mResolvedMatchConstraintDefault[orientation] == MATCH_CONSTRAINT_SPREAD ||
                        widget.mResolvedMatchConstraintDefault[orientation] == MATCH_CONSTRAINT_RATIO
                    )
        }
    }
}
