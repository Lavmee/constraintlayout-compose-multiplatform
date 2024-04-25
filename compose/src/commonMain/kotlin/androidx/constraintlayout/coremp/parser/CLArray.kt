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

class CLArray : CLContainer {

    constructor(content: CharArray) : super(content)
    internal constructor(clArray: CLArray) : super(clArray)

    override fun toJSON(): String {
        val content = StringBuilder(getDebugName() + "[")
        var first = true
        for (i in mElements.indices) {
            if (!first) {
                content.append(", ")
            } else {
                first = false
            }
            content.append(mElements[i].toJSON())
        }
        return "$content]"
    }

    override fun toFormattedJSON(indent: Int, forceIndent: Int): String {
        val json = StringBuilder()
        val value = toJSON()
        if (forceIndent <= 0 && value.length + indent < S_MAX_LINE) {
            json.append(value)
        } else {
            json.append("[\n")
            var first = true
            for (element in mElements) {
                if (!first) {
                    json.append(",\n")
                } else {
                    first = false
                }
                addIndent(json, indent + S_BASE_INDENT)
                json.append(element.toFormattedJSON(indent + S_BASE_INDENT, forceIndent - 1))
            }
            json.append("\n")
            addIndent(json, indent)
            json.append("]")
        }
        return json.toString()
    }

    override fun clone(): CLContainer {
        return CLArray(this)
    }

    companion object {
        fun allocate(content: CharArray): CLElement = CLArray(content)
    }
}
