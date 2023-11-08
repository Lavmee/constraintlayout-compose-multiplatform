/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package androidx.constraintlayout.compose.extra

internal class JSONObject : JSONElement {
    private val content: MutableMap<String, JSONElement> = linkedMapOf()

    fun put(key: String, value: String): JSONObject {
        content[key] = JSONPrimitive(isString = true, content = value)
        return this
    }

    fun put(key: String, value: Number): JSONObject {
        content[key] = JSONPrimitive(isString = false, content = value)
        return this
    }

    fun put(key: String, value: Boolean): JSONObject {
        content[key] = JSONPrimitive(isString = false, content = value)
        return this
    }

    fun put(key: String, element: JSONArray): JSONObject {
        content[key] = element
        return this
    }

    fun put(key: String, element: JSONObject): JSONObject {
        content[key] = JSONPrimitive(isString = false, content = element)
        return this
    }

    fun put(key: String, value: Any): JSONObject {
        content[key] = JSONPrimitive(isString = false, content = value)
        return this
    }

    override fun toString(): String = content.entries.joinToString(
        separator = ",",
        prefix = "{",
        postfix = "}",
        transform = { (k, v) ->
            buildString {
                printQuoted(k)
                append(':')
                append(v)
            }
        },
    )
}

internal class JSONArray : JSONElement {
    private val content: MutableList<JSONElement> = mutableListOf()
    fun put(element: JSONObject): JSONArray {
        content += element
        return this
    }

    fun put(value: String): JSONArray {
        content += JSONPrimitive(isString = true, content = value)
        return this
    }

    override fun toString(): String =
        content.joinToString(prefix = "[", postfix = "]", separator = ",")
}

private sealed interface JSONElement
private class JSONPrimitive(val isString: Boolean, val content: String) : JSONElement {
    constructor(isString: Boolean, content: Any): this(isString, content.toString())
    override fun toString(): String =
        if (isString) {
            buildString { printQuoted(content) }
        } else {
            content
        }
}

private const val STRING = '"'

private val ESCAPE_STRINGS: Array<String?> = arrayOfNulls<String>(93).apply {
    for (c in 0..0x1f) {
        val c1 = toHexChar(c shr 12)
        val c2 = toHexChar(c shr 8)
        val c3 = toHexChar(c shr 4)
        val c4 = toHexChar(c)
        this[c] = "\\u$c1$c2$c3$c4"
    }
    this['"'.code] = "\\\""
    this['\\'.code] = "\\\\"
    this['\t'.code] = "\\t"
    this['\b'.code] = "\\b"
    this['\n'.code] = "\\n"
    this['\r'.code] = "\\r"
    this[0x0c] = "\\f"
}

private fun StringBuilder.printQuoted(value: String) {
    append(STRING)
    var lastPos = 0
    for (i in value.indices) {
        val c = value[i].code
        if (c < ESCAPE_STRINGS.size && ESCAPE_STRINGS[c] != null) {
            append(value, lastPos, i)
            append(ESCAPE_STRINGS[c])
            lastPos = i + 1
        }
    }

    if (lastPos != 0) {
        append(value, lastPos, value.length)
    } else {
        append(value)
    }
    append(STRING)
}

private fun toHexChar(i: Int): Char {
    val d = i and 0xf
    return if (d < 10) {
        (d + '0'.code).toChar()
    } else {
        (d - 10 + 'a'.code).toChar()
    }
}
