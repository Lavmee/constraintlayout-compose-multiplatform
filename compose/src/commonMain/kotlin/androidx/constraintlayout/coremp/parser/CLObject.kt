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

class CLObject : CLContainer, Iterable<CLKey?> {
    constructor(content: CharArray) : super(content)

    internal constructor(clObject: CLObject) : super(clObject)

    /**
     * Returns objet as a JSON5 String
     */
    override fun toJSON(): String {
        val json = StringBuilder(getDebugName() + "{ ")
        var first = true
        for (element in mElements) {
            if (!first) {
                json.append(", ")
            } else {
                first = false
            }
            json.append(element.toJSON())
        }
        json.append(" }")
        return json.toString()
    }

    /**
     * Returns a object as a formatted JSON5 String
     */
    fun toFormattedJSON(): String {
        return toFormattedJSON(0, 0)
    }

    /**
     * Returns as a formatted JSON5 String with an indentation
     */
    override fun toFormattedJSON(indent: Int, forceIndent: Int): String {
        val json = StringBuilder(getDebugName())
        json.append("{\n")
        var first = true
        for (element in mElements) {
            if (!first) {
                json.append(",\n")
            } else {
                first = false
            }
            json.append(element.toFormattedJSON(indent + S_BASE_INDENT, forceIndent - 1))
        }
        json.append("\n")
        addIndent(json, indent)
        json.append("}")
        return json.toString()
    }

    override fun iterator(): Iterator<CLKey> {
        return CLObjectIterator(this)
    }

    private class CLObjectIterator(var mObject: CLObject) : Iterator<CLKey> {
        var mIndex = 0
        override fun hasNext(): Boolean {
            return mIndex < mObject.size()
        }

        override fun next(): CLKey {
            val key: CLKey = mObject.mElements[mIndex] as CLKey
            mIndex++
            return key
        }
    }

    override fun clone(): CLObject {
        return CLObject(this)
    }

    companion object {
        /**
         * Allocate a CLObject around an array of chars
         */
        fun allocate(content: CharArray): CLObject {
            return CLObject(content)
        }
    }
}
