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

class CLString : CLElement {

    constructor(mContent: CharArray) : super(mContent)
    internal constructor(clString: CLString) : super(clString)

    override fun toJSON(): String {
        return "'" + content() + "'"
    }

    override fun toFormattedJSON(indent: Int, forceIndent: Int): String {
        val json = StringBuilder()
        addIndent(json, indent)
        json.append("'")
        json.append(content())
        json.append("'")
        return json.toString()
    }

    override fun clone(): CLElement {
        return CLString(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        return if (other is CLString && content() == other.content()) {
            true
        } else {
            super.equals(other)
        }
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    companion object {
        fun allocate(content: CharArray): CLElement = CLString(content)

        fun from(content: String): CLString {
            val stringElement = CLString(content.toCharArray())
            stringElement.start = 0L
            stringElement.end = content.length - 1L
            return stringElement
        }
    }
}
