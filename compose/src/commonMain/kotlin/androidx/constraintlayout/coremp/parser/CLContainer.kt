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

open class CLContainer : CLElement {
    var mElements: ArrayList<CLElement> = ArrayList()
    constructor(content: CharArray) : super(content)
    internal constructor(clContainer: CLContainer) : super(clContainer) {
        val clonedArray = ArrayList<CLElement>(clContainer.mElements.size)
        for (element in clContainer.mElements) {
            val elementClone = element.clone()
            elementClone.setContainer(this)
            clonedArray.add(elementClone)
        }
        mElements = clonedArray
    }

    fun add(element: CLElement) {
        mElements.add(element)
        if (CLParser.DEBUG) {
            println("added element $element to $this")
        }
    }

    override fun toString(): String {
        val list = StringBuilder()
        for (element in mElements) {
            if (list.isNotEmpty()) {
                list.append("; ")
            }
            list.append(element)
        }
        return super.toString() + " = <" + list + " >"
    }

    fun size(): Int {
        return mElements.size
    }

    fun names(): ArrayList<String> {
        val names = ArrayList<String>()
        for (element in mElements) {
            if (element is CLKey) {
                val key: CLKey = element
                names.add(key.content())
            }
        }
        return names
    }

    fun has(name: String?): Boolean {
        for (element in mElements) {
            if (element is CLKey) {
                val key: CLKey = element
                if (key.content() == name) {
                    return true
                }
            }
        }
        return false
    }

    fun put(name: String, value: CLElement) {
        for (element in mElements) {
            val key: CLKey = element as CLKey
            if (key.content() == name) {
                key.set(value)
                return
            }
        }
        val key: CLKey = CLKey.allocate(name, value) as CLKey
        mElements.add(key)
    }

    fun putNumber(name: String, value: Float) {
        put(name, CLNumber(value))
    }

    fun putString(name: String, value: String) {
        val stringElement: CLElement = CLString(value.toCharArray())
        stringElement.start = 0L
        stringElement.end = (value.length - 1).toLong()
        put(name, stringElement)
    }

    fun remove(name: String?) {
        val toRemove = ArrayList<CLElement>()
        for (element in mElements) {
            val key: CLKey = element as CLKey
            if (key.content() == name) {
                toRemove.add(element)
            }
        }
        for (element in toRemove) {
            mElements.remove(element)
        }
    }

    fun clear() {
        mElements.clear()
    }

    operator fun get(name: String): CLElement {
        for (element in mElements) {
            val key: CLKey = element as CLKey
            if (key.content() == name) {
                return key.value
            }
        }
        throw CLParsingException("no element for key <$name>", this)
    }

    @Throws(CLParsingException::class)
    fun getInt(name: String): Int {
        val element: CLElement = get(name)
        return element.getInt()
    }

    @Throws(CLParsingException::class)
    fun getFloat(name: String): Float {
        val element: CLElement = get(name)
        return element.float
    }

    @Throws(CLParsingException::class)
    fun getArray(name: String): CLArray {
        val element = get(name)
        if (element is CLArray) {
            return element
        }
        throw CLParsingException(
            (
                "no array found for key <" + name + ">," +
                    " found [" + element.getStrClass() + "] : " + element
                ),
            this,
        )
    }

    @Throws(CLParsingException::class)
    fun getObject(name: String): CLObject {
        val element = get(name)
        if (element is CLObject) {
            return element
        }
        throw CLParsingException(
            (
                "no object found for key <" + name + ">," +
                    " found [" + element.getStrClass() + "] : " + element
                ),
            this,
        )
    }

    @Throws(CLParsingException::class)
    fun getString(name: String): String {
        val element: CLElement = get(name)
        if (element is CLString) {
            return element.content()
        }
        val strClass = element.getStrClass()
        throw CLParsingException(
            (
                "no string found for key <" + name + ">," +
                    " found [" + strClass + "] : " + element
                ),
            this,
        )
    }

    @Throws(CLParsingException::class)
    fun getBoolean(name: String): Boolean {
        val element = get(name)
        if (element is CLToken) {
            return element.getBoolean()
        }
        throw CLParsingException(
            (
                "no boolean found for key <" + name + ">," +
                    " found [" + element.getStrClass() + "] : " + element
                ),
            this,
        )
    }

    // ///////////////////////////////////////////////////////////////////////
    // Optional
    // ///////////////////////////////////////////////////////////////////////
    fun getOrNull(name: String?): CLElement? {
        for (element: CLElement in mElements) {
            val key: CLKey = element as CLKey
            if (key.content() == name) {
                return key.value
            }
        }
        return null
    }

    fun getObjectOrNull(name: String?): CLObject? {
        val element = getOrNull(name)
        return if (element is CLObject) {
            element
        } else {
            null
        }
    }

    fun getArrayOrNull(name: String?): CLArray? {
        val element = getOrNull(name)
        return if (element is CLArray) {
            element
        } else {
            null
        }
    }

    fun getArrayOrCreate(name: String): CLArray {
        var array: CLArray? = getArrayOrNull(name)
        if (array != null) {
            return array
        }
        array = CLArray(charArrayOf())
        put(name, array)
        return array
    }

    fun getStringOrNull(name: String?): String? {
        val element = getOrNull(name)
        return if (element is CLString?) {
            element?.content()
        } else {
            null
        }
    }

    fun getFloatOrNaN(name: String?): Float {
        val element = getOrNull(name)
        return if (element is CLNumber) {
            element.float
        } else {
            Float.NaN
        }
    }

    // ///////////////////////////////////////////////////////////////////////
    // By index
    // ///////////////////////////////////////////////////////////////////////

    @Throws(CLParsingException::class)
    operator fun get(index: Int): CLElement {
        if (index >= 0 && index < mElements.size) {
            return mElements[index]
        }
        throw CLParsingException("no element at index $index", this)
    }

    @Throws(CLParsingException::class)
    fun getInt(index: Int): Int {
        val element: CLElement = get(index)
        return element.getInt()
    }

    @Throws(CLParsingException::class)
    fun getFloat(index: Int): Float {
        val element: CLElement = get(index)
        return element.float
    }

    @Throws(CLParsingException::class)
    @Suppress("UNUSED")
    fun getArray(index: Int): CLArray {
        val element = get(index)
        if (element is CLArray) {
            return element
        }
        throw CLParsingException("no array at index $index", this)
    }

    @Throws(CLParsingException::class)
    fun getObject(index: Int): CLObject {
        val element = get(index)
        if (element is CLObject) {
            return element
        }
        throw CLParsingException("no object at index $index", this)
    }

    @Throws(CLParsingException::class)
    fun getString(index: Int): String {
        val element = get(index)
        if (element is CLString) {
            return element.content()
        }
        throw CLParsingException("no string at index $index", this)
    }

    @Throws(CLParsingException::class)
    fun getBoolean(index: Int): Boolean {
        val element = get(index)
        if (element is CLToken) {
            return element.getBoolean()
        }
        throw CLParsingException("no boolean at index $index", this)
    }

    // ///////////////////////////////////////////////////////////////////////
    // Optional
    // ///////////////////////////////////////////////////////////////////////
    fun getOrNull(index: Int): CLElement? {
        return if (index >= 0 && index < mElements.size) {
            mElements[index]
        } else {
            null
        }
    }

    fun getStringOrNull(index: Int): String? {
        val element = getOrNull(index)
        return if (element is CLString) {
            element.content()
        } else {
            null
        }
    }

    override fun clone(): CLContainer {
        return CLContainer(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        return if (other !is CLContainer) {
            false
        } else {
            mElements == other.mElements
        }
    }

    override fun hashCode(): Int {
        return arrayOf(mElements, super.hashCode()).hashCode()
    }

    companion object {
        @Suppress("UNUSED")
        fun allocate(content: CharArray): CLElement = CLContainer(content)
    }
}
