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
class Ref(
    private var mId: String,
    private var mWeight: Float = Float.NaN,
    private var mPreMargin: Float = Float.NaN,
    private var mPostMargin: Float = Float.NaN,
) {

    /**
     * Get the Id of the reference
     *
     * @return the Id of the reference
     */
    fun getId(): String {
        return mId
    }

    /**
     * Set the Id of the reference
     *
     * @param id
     */
    fun setId(id: String) {
        mId = id
    }

    /**
     * Get the weight of the reference
     *
     * @return the weight of the reference
     */
    fun getWeight(): Float {
        return mWeight
    }

    /**
     * Set the weight of the reference
     *
     * @param weight
     */
    fun setWeight(weight: Float) {
        mWeight = weight
    }

    /**
     * Get the preMargin of the reference
     *
     * @return the preMargin of the reference
     */
    fun getPreMargin(): Float {
        return mPreMargin
    }

    /**
     * Set the preMargin of the reference
     *
     * @param preMargin
     */
    fun setPreMargin(preMargin: Float) {
        mPreMargin = preMargin
    }

    /**
     * Get the postMargin of the reference
     *
     * @return the preMargin of the reference
     */
    fun getPostMargin(): Float {
        return mPostMargin
    }

    /**
     * Set the postMargin of the reference
     *
     * @param postMargin
     */
    fun setPostMargin(postMargin: Float) {
        mPostMargin = postMargin
    }

    override fun toString(): String {
        if (mId.isEmpty()) {
            return ""
        }
        val ret = StringBuilder()
        var isArray = false
        if (!mWeight.isNaN() || !mPreMargin.isNaN() ||
            !mPostMargin.isNaN()
        ) {
            isArray = true
        }
        if (isArray) {
            ret.append("[")
        }
        ret.append("'").append(mId).append("'")
        if (!mPostMargin.isNaN()) {
            ret.append(",").append(if (!mWeight.isNaN()) mWeight else 0f).append(",")
            ret.append(if (!mPreMargin.isNaN()) mPreMargin else 0f).append(",")
            ret.append(mPostMargin)
        } else if (!mPreMargin.isNaN()) {
            ret.append(",").append(if (!mWeight.isNaN()) mWeight else 0f).append(",")
            ret.append(mPreMargin)
        } else if (!mWeight.isNaN()) {
            ret.append(",").append(mWeight)
        }
        if (isArray) {
            ret.append("]")
        }
        ret.append(",")
        return ret.toString()
    }

    companion object {
        /**
         * Try to parse an object into a float number
         *
         * @param obj object to be parsed
         * @return a number
         */
        fun parseFloat(obj: Any?): Float {
            var value = Float.NaN
            try {
                value = obj.toString().toFloat()
            } catch (e: Exception) {
                // ignore
            }
            return value
        }

        fun parseStringToRef(str: String): Ref? {
            val values = str.replace("[\\[\\]']".toRegex(), "").split(",".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()
            if (values.isEmpty()) {
                return null
            }
            val arr = arrayOfNulls<Any>(4)
            for (i in values.indices) {
                if (i >= 4) {
                    break
                }
                arr[i] = values[i]
            }
            return Ref(
                arr[0].toString().replace("'", ""),
                parseFloat(
                    arr[1],
                ),
                parseFloat(arr[2]),
                parseFloat(arr[3]),
            )
        }

        /**
         * Add references in a String representation to a Ref ArrayList
         * Used to add the Ref(s) property in the Config to references
         *
         * @param str references in a String representation
         * @param refs  a Ref ArrayList
         */
        fun addStringToReferences(str: String?, refs: ArrayList<Ref?>) {
            if (str.isNullOrEmpty()) {
                return
            }
            val arr = arrayOfNulls<Any>(4)
            val builder = StringBuilder()
            var squareBrackets = 0
            var varCount = 0
            var ch: Char
            for (element in str) {
                ch = element
                when (ch) {
                    '[' -> squareBrackets++
                    ']' -> if (squareBrackets > 0) {
                        squareBrackets--
                        arr[varCount] = builder.toString()
                        builder.setLength(0)
                        if (arr[0] != null) {
                            refs.add(
                                Ref(
                                    arr[0].toString(),
                                    parseFloat(arr[1]),
                                    parseFloat(arr[2]),
                                    parseFloat(arr[3]),
                                ),
                            )
                            varCount = 0
                            arr.fill(null)
                        }
                    }

                    ',' -> {
                        // deal with the first 3 values in the nested array,
                        // the fourth value (postMargin) would be handled at case ']'
                        if (varCount < 3) {
                            arr[varCount++] = builder.toString()
                            builder.setLength(0)
                        }
                        // squareBrackets == 1 indicate the value is not in a nested array.
                        if (squareBrackets == 1 && arr[0] != null) {
                            refs.add(Ref(arr[0].toString()))
                            varCount = 0
                            arr[0] = null
                        }
                    }

                    ' ', '\'' -> {}
                    else -> builder.append(ch)
                }
            }
        }
    }
}
