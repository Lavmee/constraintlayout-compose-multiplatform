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
package androidx.constraintlayout.compose.core.parser

class CLNumber(content: CharArray) : CLElement(content) {

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
    fun putValue(value: Float) {
        mValue = value
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        if (!super.equals(other)) return false

        other as CLNumber

        return mValue == other.mValue
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
