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

class CLToken : CLElement {
    private var mIndex = 0
    var mType: Type = Type.UNKNOWN

    constructor(content: CharArray) : super(content)
    internal constructor(clToken: CLToken) : super(clToken) {
        this.mType = clToken.mType
        this.mIndex = clToken.mIndex
    }

    @Throws(CLParsingException::class)
    fun getBoolean(): Boolean {
        if (mType == Type.TRUE) {
            return true
        }
        if (mType == Type.FALSE) {
            return false
        }
        throw CLParsingException("this token is not a boolean: <" + content() + ">", this)
    }

    @Throws(CLParsingException::class)
    @Suppress("UNUSED")
    fun isNull(): Boolean {
        if (mType == Type.NULL) {
            return true
        }
        throw CLParsingException("this token is not a null: <" + content() + ">", this)
    }

    enum class Type {
        UNKNOWN,
        TRUE,
        FALSE,
        NULL,
    }

    private var mTokenTrue = "true".toCharArray()
    private var mTokenFalse = "false".toCharArray()
    private var mTokenNull = "null".toCharArray()

    override fun toJSON(): String {
        return if (CLParser.DEBUG) {
            "<" + content() + ">"
        } else {
            content()
        }
    }

    override fun toFormattedJSON(indent: Int, forceIndent: Int): String {
        val json = StringBuilder()
        addIndent(json, indent)
        json.append(content())
        return json.toString()
    }

    fun getType(): Type {
        return mType
    }

    fun validate(c: Char, position: Long): Boolean {
        var isValid = false
        when (mType) {
            Type.TRUE -> {
                isValid = mTokenTrue[mIndex] == c
                if (isValid && mIndex + 1 == mTokenTrue.size) {
                    end = position
                }
            }

            Type.FALSE -> {
                isValid = mTokenFalse[mIndex] == c
                if (isValid && mIndex + 1 == mTokenFalse.size) {
                    end = position
                }
            }

            Type.NULL -> {
                isValid = mTokenNull[mIndex] == c
                if (isValid && mIndex + 1 == mTokenNull.size) {
                    end = position
                }
            }

            Type.UNKNOWN -> {
                when (c) {
                    mTokenTrue[mIndex] -> {
                        mType = Type.TRUE
                        isValid = true
                    }
                    mTokenFalse[mIndex] -> {
                        mType = Type.FALSE
                        isValid = true
                    }
                    mTokenNull[mIndex] -> {
                        mType = Type.NULL
                        isValid = true
                    }
                }
            }
        }
        mIndex++
        return isValid
    }

    override fun clone(): CLElement {
        return CLToken(this)
    }

    companion object {
        fun allocate(content: CharArray): CLElement = CLToken(content)
    }
}
