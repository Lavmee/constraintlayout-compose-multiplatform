/*
 * Copyright (C) 2021 The Android Open Source Project
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

import androidx.constraintlayout.coremp.ext.Cloneable

open class CLElement internal constructor(content: CharArray) : Cloneable {
    internal val mContent: CharArray = content
    protected var mStart: Long = -1
    protected var mEnd = Long.MAX_VALUE
    protected var mContainer: CLContainer? = null
    private var mLine = 0

    internal constructor(clElement: CLElement) : this(clElement.mContent) {
        mStart = clElement.start
        mEnd = clElement.end
        mContainer = clElement.mContainer
        mLine = clElement.mLine
    }

    fun notStarted(): Boolean = mStart == -1L

    fun setLine(line: Int) {
        this.mLine = line
    }

    fun getLine(): Int {
        return this.mLine
    }

    var start: Long
        get() = this.mStart
        set(value) {
            this.mStart = value
        }

    var end: Long
        get() = this.mEnd
        set(value) {
            if (this.mEnd != Long.MAX_VALUE) {
                return
            }
            this.mEnd = value
            if (CLParser.DEBUG) {
                println("closing " + this.hashCode() + " -> " + this)
            }
            this.mContainer?.add(this)
        }

    protected fun addIndent(builder: StringBuilder, indent: Int) {
        for (i in 0 until indent) {
            builder.append(' ')
        }
    }

    override fun toString(): String {
        if (mStart > mEnd || mEnd == Long.MAX_VALUE) {
            return this::class.toString() + " (INVALID, " + mStart + "-" + mEnd + ")"
        }
        var content: String = mContent.joinToString("")
        content = content.substring(mStart.toInt(), mEnd.toInt() + 1)

        return getStrClass() + " (" + mStart + " : " + mEnd + ") <<" + content + ">>"
    }

    fun getStrClass(): String {
        val myClass = this::class.toString()
        return myClass.substring(myClass.lastIndexOf('.') + 1)
    }

    protected fun getDebugName(): String {
        return if (CLParser.DEBUG) {
            getStrClass() + " -> "
        } else {
            ""
        }
    }

    // @TODO: add description
    fun content(): String {
        val content: String = mContent.joinToString("")
        // Handle empty string
        if (content.isEmpty()) {
            return ""
        }
        return if (mEnd == Long.MAX_VALUE || mEnd < mStart) {
            content.substring(mStart.toInt(), mStart.toInt() + 1)
        } else {
            content.substring(mStart.toInt(), mEnd.toInt() + 1)
        }
    }

    fun hasContent(): Boolean {
        return mContent.isNotEmpty()
    }

    fun isDone(): Boolean {
        return mEnd != Long.MAX_VALUE
    }

    fun setContainer(element: CLContainer) {
        mContainer = element
    }

    fun getContainer(): CLElement? {
        return mContainer
    }

    fun isStarted(): Boolean {
        return mStart > -1
    }

    internal open fun toJSON(): String {
        return ""
    }

    internal open fun toFormattedJSON(indent: Int, forceIndent: Int): String? {
        return ""
    }

    // @TODO: add description
    open fun getInt(): Int {
        return if (this is CLNumber) {
            this.getInt()
        } else {
            0
        }
    }

    // @TODO: add description

    open val float: Float = 0.0f
        get() = if (this is CLNumber) {
            field
        } else {
            Float.NaN
        }

    override fun clone(): CLElement {
        return CLElement(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CLElement) return false

        val clElement = other

        if (mStart != clElement.mStart) return false
        if (mEnd != clElement.mEnd) return false
        if (mLine != clElement.mLine) return false
        if (!mContent.contentEquals(clElement.mContent)) return false
        return mContainer == clElement.mContainer
    }

    override fun hashCode(): Int {
        var result: Int = mContent.hashCode()
        result = 31 * result + (mStart xor (mStart ushr 32)).toInt()
        result = 31 * result + (mEnd xor (mEnd ushr 32)).toInt()
        result = 31 * result + if (mContainer != null) mContainer.hashCode() else 0
        result = 31 * result + mLine
        return result
    }

    companion object {
        const val S_MAX_LINE: Int = 80
        const val S_BASE_INDENT: Int = 2
    }
}
