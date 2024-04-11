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
package androidx.constraintlayout.coremp.motion.utils

import kotlin.math.abs
import kotlin.math.sqrt

class StopLogicEngine : StopEngine {

    // the velocity at the start of each period
    private var mStage1Velocity = 0f // the velocity at the start of each period
    private var mStage2Velocity = 0f // the velocity at the start of each period
    private var mStage3Velocity = 0f
    private var mStage1Duration = 0f
    private var mStage2Duration = 0f
    private var mStage3Duration = 0f // the time for each period

    private var mStage1EndPosition = 0f
    private var mStage2EndPosition = 0f
    private var mStage3EndPosition = 0f // ending position

    private var mNumberOfStages = 0
    private var mType: String? = null
    private var mBackwards = false
    private var mStartPosition = 0f
    private var mLastPosition = 0f
    private var mLastTime = 0f

    @Suppress("unused")
    private var mDone = false

    /**
     * Debugging logic to log the state.
     *
     * @param desc Description to pre append
     * @param time Time during animation
     * @return string useful for debugging the state of the StopLogic
     */
    override fun debug(desc: String, time: Float): String {
        var time = time
        var ret = "$desc ===== $mType\n"
        ret += (
            desc + (if (mBackwards) "backwards" else "forward ") +
                " time = " + time + "  stages " + mNumberOfStages + "\n"
            )
        ret += (
            desc + " dur " + mStage1Duration + " vel " +
                mStage1Velocity + " pos " + mStage1EndPosition + "\n"
            )
        if (mNumberOfStages > 1) {
            ret += (
                desc + " dur " + mStage2Duration + " vel " +
                    mStage2Velocity + " pos " + mStage2EndPosition + "\n"
                )
        }
        if (mNumberOfStages > 2) {
            ret += (
                desc + " dur " + mStage3Duration + " vel " +
                    mStage3Velocity + " pos " + mStage3EndPosition + "\n"
                )
        }
        if (time <= mStage1Duration) {
            ret += desc + "stage 0" + "\n"
            return ret
        }
        if (mNumberOfStages == 1) {
            ret += desc + "end stage 0" + "\n"
            return ret
        }
        time -= mStage1Duration
        if (time < mStage2Duration) {
            ret += "$desc stage 1\n"
            return ret
        }
        if (mNumberOfStages == 2) {
            ret += desc + "end stage 1" + "\n"
            return ret
        }
        time -= mStage2Duration
        if (time < mStage3Duration) {
            ret += "$desc stage 2\n"
            return ret
        }
        ret += "$desc end stage 2\n"
        return ret
    }

    // @TODO: add description
    override fun getVelocity(x: Float): Float {
        var x = x
        if (x <= mStage1Duration) {
            return mStage1Velocity + (mStage2Velocity - mStage1Velocity) * x / mStage1Duration
        }
        if (mNumberOfStages == 1) {
            return 0f
        }
        x -= mStage1Duration
        if (x < mStage2Duration) {
            return mStage2Velocity + (mStage3Velocity - mStage2Velocity) * x / mStage2Duration
        }
        if (mNumberOfStages == 2) {
            return 0f
        }
        x -= mStage2Duration
        return if (x < mStage3Duration) {
            mStage3Velocity - mStage3Velocity * x / mStage3Duration
        } else {
            0f
        }
    }

    private fun calcY(time: Float): Float {
        var time = time
        mDone = false
        if (time <= mStage1Duration) {
            return mStage1Velocity * time + (
                (mStage2Velocity - mStage1Velocity) *
                    time * time
                ) / (2 * mStage1Duration)
        }
        if (mNumberOfStages == 1) {
            return mStage1EndPosition
        }
        time -= mStage1Duration
        if (time < mStage2Duration) {
            return mStage1EndPosition + mStage2Velocity * time + (mStage3Velocity - mStage2Velocity) * time * time / (2 * mStage2Duration)
        }
        if (mNumberOfStages == 2) {
            return mStage2EndPosition
        }
        time -= mStage2Duration
        if (time <= mStage3Duration) {
            return mStage2EndPosition + mStage3Velocity * time - mStage3Velocity * time * time / (2 * mStage3Duration)
        }
        mDone = true
        return mStage3EndPosition
    }

    // @TODO: add description
    fun config(
        currentPos: Float,
        destination: Float,
        currentVelocity: Float,
        maxTime: Float,
        maxAcceleration: Float,
        maxVelocity: Float,
    ) {
        mDone = false
        mStartPosition = currentPos
        mBackwards = currentPos > destination
        if (mBackwards) {
            setup(
                -currentVelocity,
                currentPos - destination,
                maxAcceleration,
                maxVelocity,
                maxTime,
            )
        } else {
            setup(currentVelocity, destination - currentPos, maxAcceleration, maxVelocity, maxTime)
        }
    }

    // @TODO: add description
    override fun getInterpolation(v: Float): Float {
        val y = calcY(v)
        mLastPosition = y
        mLastTime = v
        return if (mBackwards) mStartPosition - y else mStartPosition + y
    }

    override val velocity: Float
        get() = if (mBackwards) -getVelocity(mLastTime) else getVelocity(mLastTime)

    override val isStopped: Boolean
        get() = velocity < EPSILON && abs((mStage3EndPosition - mLastPosition).toDouble()) < EPSILON

    private fun setup(
        velocity: Float,
        distance: Float,
        maxAcceleration: Float,
        maxVelocity: Float,
        maxTime: Float,
    ) {
        var velocity = velocity
        mDone = false
        mStage3EndPosition = distance
        if (velocity == 0f) {
            velocity = 0.0001f
        }
        val min_time_to_stop = velocity / maxAcceleration
        val stopDistance = min_time_to_stop * velocity / 2
        if (velocity < 0) { // backward
            val timeToZeroVelocity = -velocity / maxAcceleration
            val reversDistanceTraveled = timeToZeroVelocity * velocity / 2
            val totalDistance = distance - reversDistanceTraveled
            val peak_v = sqrt((maxAcceleration * totalDistance).toDouble()).toFloat()
            if (peak_v < maxVelocity) { // accelerate then decelerate
                mType = "backward accelerate, decelerate"
                mNumberOfStages = 2
                mStage1Velocity = velocity
                mStage2Velocity = peak_v
                mStage3Velocity = 0f
                mStage1Duration = (peak_v - velocity) / maxAcceleration
                mStage2Duration = peak_v / maxAcceleration
                mStage1EndPosition = (velocity + peak_v) * mStage1Duration / 2
                mStage2EndPosition = distance
                mStage3EndPosition = distance
                return
            }
            mType = "backward accelerate cruse decelerate"
            mNumberOfStages = 3
            mStage1Velocity = velocity
            mStage2Velocity = maxVelocity
            mStage3Velocity = maxVelocity
            mStage1Duration = (maxVelocity - velocity) / maxAcceleration
            mStage3Duration = maxVelocity / maxAcceleration
            val accDist = (velocity + maxVelocity) * mStage1Duration / 2
            val decDist = maxVelocity * mStage3Duration / 2
            mStage2Duration = (distance - accDist - decDist) / maxVelocity
            mStage1EndPosition = accDist
            mStage2EndPosition = distance - decDist
            mStage3EndPosition = distance
            return
        }
        if (stopDistance >= distance) { // we cannot make it hit the breaks.
            // we do a force hard stop
            mType = "hard stop"
            val time = 2 * distance / velocity
            mNumberOfStages = 1
            mStage1Velocity = velocity
            mStage2Velocity = 0f
            mStage1EndPosition = distance
            mStage1Duration = time
            return
        }
        val distance_before_break = distance - stopDistance
        val cruseTime = distance_before_break / velocity // do we just Cruse then stop?
        if (cruseTime + min_time_to_stop < maxTime) { // close enough maintain v then break
            mType = "cruse decelerate"
            mNumberOfStages = 2
            mStage1Velocity = velocity
            mStage2Velocity = velocity
            mStage3Velocity = 0f
            mStage1EndPosition = distance_before_break
            mStage2EndPosition = distance
            mStage1Duration = cruseTime
            mStage2Duration = velocity / maxAcceleration
            return
        }
        val peak_v = sqrt((maxAcceleration * distance + velocity * velocity / 2).toDouble()).toFloat()
        mStage1Duration = (peak_v - velocity) / maxAcceleration
        mStage2Duration = peak_v / maxAcceleration
        if (peak_v < maxVelocity) { // accelerate then decelerate
            mType = "accelerate decelerate"
            mNumberOfStages = 2
            mStage1Velocity = velocity
            mStage2Velocity = peak_v
            mStage3Velocity = 0f
            mStage1Duration = (peak_v - velocity) / maxAcceleration
            mStage2Duration = peak_v / maxAcceleration
            mStage1EndPosition = (velocity + peak_v) * mStage1Duration / 2
            mStage2EndPosition = distance
            return
        }
        mType = "accelerate cruse decelerate"
        // accelerate, cruse then decelerate
        mNumberOfStages = 3
        mStage1Velocity = velocity
        mStage2Velocity = maxVelocity
        mStage3Velocity = maxVelocity
        mStage1Duration = (maxVelocity - velocity) / maxAcceleration
        mStage3Duration = maxVelocity / maxAcceleration
        val accDist = (velocity + maxVelocity) * mStage1Duration / 2
        val decDist = maxVelocity * mStage3Duration / 2
        mStage2Duration = (distance - accDist - decDist) / maxVelocity
        mStage1EndPosition = accDist
        mStage2EndPosition = distance - decDist
        mStage3EndPosition = distance
    }

    class Decelerate : StopEngine {
        private var mDestination = 0f
        private var mInitialVelocity = 0f
        private var mAcceleration = 0f
        private var mLastVelocity = 0f
        private var mDuration = 0f
        private var mInitialPos = 0f
        private var mDone = false

        override fun debug(desc: String, time: Float): String {
            return "$mDuration $mLastVelocity"
        }

        override fun getVelocity(time: Float): Float {
            return if (time > mDuration) {
                0f
            } else {
                (mInitialVelocity + mAcceleration * time).also { mLastVelocity = it }
            }
        }

        override fun getInterpolation(time: Float): Float {
            if (time > mDuration) {
                mDone = true
                return mDestination
            }
            getVelocity(time)
            return mInitialPos + (mInitialVelocity + mAcceleration * time / 2) * time
        }

        override val velocity: Float
            get() = mLastVelocity

        override val isStopped: Boolean
            get() = mDone

        /**
         * Configure simple deceleration controller
         *
         * @param currentPos      the current position
         * @param destination     the destination position
         * @param currentVelocity the currentVelocity change in pos / second
         */
        fun config(currentPos: Float, destination: Float, currentVelocity: Float) {
            mDone = false
            mDestination = destination
            mInitialVelocity = currentVelocity
            mInitialPos = currentPos
            val distance = mDestination - currentPos
            mDuration = distance / (currentVelocity / 2)
            mAcceleration = -currentVelocity / mDuration
        }
    }

    companion object {
        private const val EPSILON = 0.00001f
    }
}
