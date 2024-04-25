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
package androidx.constraintlayout.coremp.parser

class CLNumber : CLElement {

    constructor(content: CharArray) : super(content)
    internal constructor(clNumber: CLNumber) : super(clNumber) {
        this.mValue = clNumber.mValue
    }

    var mValue = Float.NaN

    constructor(value: Float) : this(charArrayOf()) {
        this.mValue = value
    }

    override fun toJSON(): String {
        val value = float
        val intValue = value.toInt()
        return if (intValue.toFloat() == value) {
            "" + intValue
        } else {
            "" + value
        }
    }

    override fun toFormattedJSON(indent: Int, forceIndent: Int): String {
        val json = StringBuilder()
        addIndent(json, indent)
        val value = float
        val intValue = value.toInt()
        if (intValue.toFloat() == value) {
            json.append(intValue)
        } else {
            json.append(value)
        }
        return json.toString()
    }

    @Suppress("UNUSED")
    fun isInt(): Boolean {
        val value = float
        val intValue = value.toInt()
        return intValue.toFloat() == value
    }

    override fun getInt(): Int {
        if (mValue.isNaN() && hasContent()) {
            // If the value is undefined, attempt to define it from the content
            mValue = content().toInt().toFloat()
        }
        return mValue.toInt()
    }

    override val float: Float
        get() {
            if (mValue.isNaN() && hasContent()) {
                // If the value is undefined, attempt to define it from the content
                mValue = content().toFloat()
            }
            return mValue
        }

    // @TODO: add description
    @Suppress("UNUSED")
    fun putValue(value: Float) {
        mValue = value
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other is CLNumber) {
            val thisFloat = float
            val otherFloat = other.float
            return if (thisFloat.isNaN() && otherFloat.isNaN()) {
                // Consider equal if both elements have a NaN value
                true
            } else {
                thisFloat == otherFloat
            }
        }

        return false
    }

    override fun clone(): CLElement {
        return CLNumber(this)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + mValue.hashCode()
        return result
    }

    companion object {
        fun allocate(content: CharArray): CLElement = CLNumber(content)
    }
}
