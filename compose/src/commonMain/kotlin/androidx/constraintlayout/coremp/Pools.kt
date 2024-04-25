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
package androidx.constraintlayout.coremp

class Pools private constructor() {

    interface Pool<T> {
        /**
         * @return An instance from the pool if such, null otherwise.
         */
        fun acquire(): T?

        /**
         * Release an instance to the pool.
         *
         * @param instance The instance to release.
         * @return Whether the instance was put in the pool.
         * @throws IllegalStateException If the instance is already in the pool.
         */
        fun release(instance: T): Boolean

        /**
         * Try releasing all instances at the same time
         *
         * @param variables the variables to release
         * @param count     the number of variables to release
         */
        fun releaseAll(variables: Array<T?>, count: Int)
    }

    class SimplePool<T>(maxPoolSize: Int) : Pool<T> {
        private var mPool: Array<Any?>

        private var mPoolSize = 0

        init {
            if (maxPoolSize <= 0) {
                throw IllegalArgumentException("The max pool size must be > 0")
            }

            mPool = arrayOfNulls(maxPoolSize)
        }

        @Suppress("UNCHECKED_CAST")
        override fun acquire(): T? {
            if (mPoolSize > 0) {
                val lastPooledIndex = mPoolSize - 1
                val instance = mPool[lastPooledIndex] as T
                mPool[lastPooledIndex] = null
                mPoolSize--
                return instance
            }
            return null
        }

        override fun release(instance: T): Boolean {
            if (DEBUG) {
                if (isInPool(instance)) {
                    throw IllegalStateException("Already in the pool!")
                }
            }
            if (mPoolSize < mPool.size) {
                mPool[mPoolSize] = instance
                mPoolSize++
                return true
            }
            return false
        }

        @Suppress("NAME_SHADOWING")
        override fun releaseAll(variables: Array<T?>, count: Int) {
            var count = count
            if (count > variables.size) {
                count = variables.size
            }
            for (i in 0 until count) {
                val instance = variables[i]
                if (DEBUG) {
                    check(!isInPool(instance!!)) { "Already in the pool!" }
                }
                if (mPoolSize < mPool.size) {
                    mPool[mPoolSize] = instance
                    mPoolSize++
                }
            }
        }

        private fun isInPool(instance: T): Boolean {
            for (i in 0 until mPoolSize) {
                if (mPool[i] == instance) {
                    return true
                }
            }
            return false
        }
    }

    companion object {
        private const val DEBUG = false
    }
}
