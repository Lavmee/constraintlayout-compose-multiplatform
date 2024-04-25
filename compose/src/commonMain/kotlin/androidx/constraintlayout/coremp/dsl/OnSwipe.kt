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
class OnSwipe() {
    private var mDragDirection: Drag? = null
    private var mTouchAnchorSide: Side? = null

    private var mTouchAnchorId: String? = null
    private var mLimitBoundsTo: String? = null
    private var mOnTouchUp: TouchUp? = null
    private var mRotationCenterId: String? = null
    private var mMaxVelocity = Float.NaN
    private var mMaxAcceleration = Float.NaN
    private var mDragScale = Float.NaN

    private var mDragThreshold = Float.NaN
    private var mSpringDamping = Float.NaN
    private var mSpringMass = Float.NaN
    private var mSpringStiffness = Float.NaN
    private var mSpringStopThreshold = Float.NaN
    private var mSpringBoundary: Boundary? = null
    private var mAutoCompleteMode: Mode? = null

    constructor(anchor: String, side: Side, dragDirection: Drag) : this() {
        mTouchAnchorId = anchor
        mTouchAnchorSide = side
        mDragDirection = dragDirection
    }

    enum class Mode {
        VELOCITY,
        SPRING,
    }

    enum class Boundary {
        OVERSHOOT,
        BOUNCE_START,
        BOUNCE_END,
        BOUNCE_BOTH,
    }

    enum class Drag {
        UP,
        DOWN,
        LEFT,
        RIGHT,
        START,
        END,
        CLOCKWISE,
        ANTICLOCKWISE,
    }

    enum class Side {
        TOP,
        LEFT,
        RIGHT,
        BOTTOM,
        MIDDLE,
        START,
        END,
    }

    enum class TouchUp {
        AUTOCOMPLETE,
        TO_START,
        NEVER_COMPLETE_END,
        TO_END,
        STOP,
        DECELERATE,
        DECELERATE_COMPLETE,
        NEVER_COMPLETE_START,
    }

    /**
     * The id of the view who's movement is matched to your drag
     * If not specified it will map to a linear movement across the width of the motionLayout
     */
    fun setTouchAnchorId(id: String): OnSwipe {
        mTouchAnchorId = id
        return this
    }

    fun getTouchAnchorId(): String? {
        return mTouchAnchorId
    }

    /**
     * This side of the view that matches the drag movement.
     * Only meaning full if the object changes size during the movement.
     * (rotation is not considered)
     */
    fun setTouchAnchorSide(side: Side): OnSwipe {
        mTouchAnchorSide = side
        return this
    }

    fun getTouchAnchorSide(): Side? {
        return mTouchAnchorSide
    }

    /**
     * The direction of the drag.
     */
    fun setDragDirection(dragDirection: Drag): OnSwipe {
        mDragDirection = dragDirection
        return this
    }

    fun getDragDirection(): Drag? {
        return mDragDirection
    }

    /**
     * The maximum velocity (Change in progress per second) animation can achieve
     */
    fun setMaxVelocity(maxVelocity: Int): OnSwipe {
        mMaxVelocity = maxVelocity.toFloat()
        return this
    }

    fun getMaxVelocity(): Float {
        return mMaxVelocity
    }

    /**
     * The maximum acceleration and deceleration of the animation
     * (Change in Change in progress per second)
     * Faster makes the object seem lighter and quicker
     */
    fun setMaxAcceleration(maxAcceleration: Int): OnSwipe {
        mMaxAcceleration = maxAcceleration.toFloat()
        return this
    }

    fun getMaxAcceleration(): Float {
        return mMaxAcceleration
    }

    /**
     * Normally 1 this can be tweaked to make the acceleration faster
     */
    fun setDragScale(dragScale: Int): OnSwipe {
        mDragScale = dragScale.toFloat()
        return this
    }

    fun getDragScale(): Float {
        return mDragScale
    }

    /**
     * This sets the threshold before the animation is kicked off.
     * It is important when have multi state animations the have some play before the
     * System decides which animation to jump on.
     */
    fun setDragThreshold(dragThreshold: Int): OnSwipe {
        mDragThreshold = dragThreshold.toFloat()
        return this
    }

    fun getDragThreshold(): Float {
        return mDragThreshold
    }

    /**
     * Configures what happens when the user releases on mouse up.
     * One of: ON_UP_AUTOCOMPLETE, ON_UP_AUTOCOMPLETE_TO_START, ON_UP_AUTOCOMPLETE_TO_END,
     * ON_UP_STOP, ON_UP_DECELERATE, ON_UP_DECELERATE_AND_COMPLETE
     *
     * @param mode default = ON_UP_AUTOCOMPLETE
     */
    fun setOnTouchUp(mode: TouchUp): OnSwipe {
        mOnTouchUp = mode
        return this
    }

    fun getOnTouchUp(): TouchUp? {
        return mOnTouchUp
    }

    /**
     * Only allow touch actions to be initiated within this region
     */
    fun setLimitBoundsTo(id: String): OnSwipe {
        mLimitBoundsTo = id
        return this
    }

    fun getLimitBoundsTo(): String? {
        return mLimitBoundsTo
    }

    /**
     * The view to center the rotation about
     *
     * @return this
     */
    fun setRotateCenter(rotationCenterId: String): OnSwipe {
        mRotationCenterId = rotationCenterId
        return this
    }

    fun getRotationCenterId(): String? {
        return mRotationCenterId
    }

    fun getSpringDamping(): Float {
        return mSpringDamping
    }

    /**
     * Set the damping of the spring if using spring.
     * c in "a = (-k*x-c*v)/m" equation for the acceleration of a spring
     *
     * @return this
     */
    fun setSpringDamping(springDamping: Float): OnSwipe {
        mSpringDamping = springDamping
        return this
    }

    /**
     * Get the mass of the spring.
     * the m in "a = (-k*x-c*v)/m" equation for the acceleration of a spring
     */
    fun getSpringMass(): Float {
        return mSpringMass
    }

    /**
     * Set the Mass of the spring if using spring.
     * m in "a = (-k*x-c*v)/m" equation for the acceleration of a spring
     *
     * @return this
     */
    fun setSpringMass(springMass: Float): OnSwipe {
        mSpringMass = springMass
        return this
    }

    /**
     * get the stiffness of the spring
     *
     * @return NaN if not set
     */
    fun getSpringStiffness(): Float {
        return mSpringStiffness
    }

    /**
     * set the stiffness of the spring if using spring.
     * If this is set the swipe will use a spring return system.
     * If set to NaN it will revert to the norm system.
     * K in "a = (-k*x-c*v)/m" equation for the acceleration of a spring
     */
    fun setSpringStiffness(springStiffness: Float): OnSwipe {
        mSpringStiffness = springStiffness
        return this
    }

    /**
     * The threshold for spring motion to stop.
     */
    fun getSpringStopThreshold(): Float {
        return mSpringStopThreshold
    }

    /**
     * set the threshold for spring motion to stop.
     * This is in change in progress / second
     * If the spring will never go above that threshold again it will stop.
     *
     * @param springStopThreshold when to stop.
     */
    fun setSpringStopThreshold(springStopThreshold: Float): OnSwipe {
        mSpringStopThreshold = springStopThreshold
        return this
    }

    /**
     * The behaviour at the boundaries 0 and 1
     */
    fun getSpringBoundary(): Boundary? {
        return mSpringBoundary
    }

    /**
     * The behaviour at the boundaries 0 and 1.
     *
     * @param springBoundary behaviour at the boundaries
     */
    fun setSpringBoundary(springBoundary: Boundary): OnSwipe {
        mSpringBoundary = springBoundary
        return this
    }

    fun getAutoCompleteMode(): Mode? {
        return mAutoCompleteMode
    }

    /**
     * sets the behaviour at the boundaries 0 and 1
     * COMPLETE_MODE_CONTINUOUS_VELOCITY = 0;
     * COMPLETE_MODE_SPRING = 1;
     */
    fun setAutoCompleteMode(autoCompleteMode: Mode) {
        mAutoCompleteMode = autoCompleteMode
    }

    override fun toString(): String {
        val ret = StringBuilder()
        ret.append("OnSwipe:{\n")
        if (mTouchAnchorId != null) {
            ret.append("anchor:'").append(mTouchAnchorId).append("',\n")
        }
        if (mDragDirection != null) {
            ret.append("direction:'")
                .append(mDragDirection.toString().lowercase()).append(
                    "',\n",
                )
        }
        if (mTouchAnchorSide != null) {
            ret.append("side:'").append(mTouchAnchorSide.toString().lowercase())
                .append("',\n")
        }
        if (!mDragScale.isNaN()) {
            ret.append("scale:'").append(mDragScale).append("',\n")
        }
        if (!mDragThreshold.isNaN()) {
            ret.append("threshold:'").append(mDragThreshold).append("',\n")
        }
        if (!mMaxVelocity.isNaN()) {
            ret.append("maxVelocity:'").append(mMaxVelocity).append("',\n")
        }
        if (!mMaxAcceleration.isNaN()) {
            ret.append("maxAccel:'").append(mMaxAcceleration).append("',\n")
        }
        if (mLimitBoundsTo != null) {
            ret.append("limitBounds:'").append(mLimitBoundsTo).append("',\n")
        }
        if (mAutoCompleteMode != null) {
            ret.append("mode:'").append(mAutoCompleteMode.toString().lowercase())
                .append("',\n")
        }
        if (mOnTouchUp != null) {
            ret.append("touchUp:'").append(mOnTouchUp.toString().lowercase())
                .append("',\n")
        }
        if (!mSpringMass.isNaN()) {
            ret.append("springMass:'").append(mSpringMass).append("',\n")
        }
        if (!mSpringStiffness.isNaN()) {
            ret.append("springStiffness:'").append(mSpringStiffness).append("',\n")
        }
        if (!mSpringDamping.isNaN()) {
            ret.append("springDamping:'").append(mSpringDamping).append("',\n")
        }
        if (!mSpringStopThreshold.isNaN()) {
            ret.append("stopThreshold:'").append(mSpringStopThreshold).append("',\n")
        }
        if (mSpringBoundary != null) {
            ret.append("springBoundary:'").append(mSpringBoundary).append("',\n")
        }
        if (mRotationCenterId != null) {
            ret.append("around:'").append(mRotationCenterId).append("',\n")
        }
        ret.append("},\n")
        return ret.toString()
    }

    companion object {
        const val FLAG_DISABLE_POST_SCROLL = 1
        const val FLAG_DISABLE_SCROLL = 2
    }
}
