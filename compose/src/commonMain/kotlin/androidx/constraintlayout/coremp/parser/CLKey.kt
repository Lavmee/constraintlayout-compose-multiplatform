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

class CLKey : CLContainer {
    val name: String
        get() = content()

    constructor(content: CharArray) : super(content)
    internal constructor(clKey: CLKey) : super(clKey)

    override fun toJSON(): String {
        return if (mElements.size > 0) {
            getDebugName() + content() + ": " + mElements[0].toJSON()
        } else {
            getDebugName() + content() + ": <> "
        }
    }

    override fun toFormattedJSON(indent: Int, forceIndent: Int): String {
        var forceIndent = forceIndent
        val json = StringBuilder(getDebugName())
        addIndent(json, indent)
        val content = content()
        if (mElements.size > 0) {
            json.append(content)
            json.append(": ")
            if (sSections.contains(content)) {
                forceIndent = 3
            }
            if (forceIndent > 0) {
                json.append(mElements[0].toFormattedJSON(indent, forceIndent - 1))
            } else {
                val value = mElements[0].toJSON()
                if (value.length + indent < S_MAX_LINE) {
                    json.append(value)
                } else {
                    json.append(mElements[0].toFormattedJSON(indent, forceIndent - 1))
                }
            }
            return json.toString()
        }
        return "$content: <> "
    }

    // @TODO: add description
    fun set(value: CLElement?) {
        if (mElements.size > 0) {
            mElements[0] = value!!
        } else {
            mElements.add(value!!)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other is CLKey) {
            if (this.name != other.name) {
                return false
            }
        }
        // Delegate the rest to parent
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    val value: CLElement
        // @TODO: add description
        get() = if (mElements.size > 0) {
            mElements[0]
        } else {
            null!!
        }

    override fun clone(): CLContainer {
        return CLKey(this)
    }

    companion object {
        private val sSections: ArrayList<String> = ArrayList()

        init {
            sSections.add("ConstraintSets")
            sSections.add("Variables")
            sSections.add("Generate")
            sSections.add("Transitions")
            sSections.add("KeyFrames")
            sSections.add("KeyAttributes")
            sSections.add("KeyPositions")
            sSections.add("KeyCycles")
        }

        fun allocate(content: CharArray): CLElement {
            return CLKey(content)
        }

        fun allocate(name: String, value: CLElement): CLElement {
            val key = CLKey(name.toCharArray())
            key.start = 0
            key.end = (name.length - 1).toLong()
            key.set(value)
            return key
        }
    }
}
