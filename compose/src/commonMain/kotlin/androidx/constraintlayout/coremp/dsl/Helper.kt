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
package androidx.constraintlayout.coremp.dsl

import kotlin.jvm.JvmStatic

open class Helper {
    enum class Type {
        VERTICAL_GUIDELINE,
        HORIZONTAL_GUIDELINE,
        VERTICAL_CHAIN,
        HORIZONTAL_CHAIN,
        BARRIER,
    }

    protected val name: String
    protected var mType: HelperType
    protected var mConfig: String? = null
    protected var configMap: MutableMap<String, String>? = HashMap()

    constructor(name: String, type: HelperType) {
        this.name = name
        this.mType = type
    }

    constructor(name: String, type: HelperType, config: String) {
        this.name = name
        this.mType = type
        this.mConfig = config
        configMap = convertConfigToMap()!!.toMutableMap()
    }

    fun getId(): String {
        return name
    }

    fun getType(): HelperType {
        return mType
    }

    fun getConfig(): String? {
        return mConfig
    }

    fun convertConfigToMap(): Map<String, String>? {
        if (mConfig == null || mConfig!!.isEmpty()) {
            return null
        }
        val map: MutableMap<String, String> = HashMap()
        val builder = StringBuilder()
        var key = ""
        var value: String
        var squareBrackets = 0
        var curlyBrackets = 0
        var ch: Char
        for (i in 0 until mConfig!!.length) {
            ch = mConfig!![i]
            if (ch == ':') {
                key = builder.toString()
                builder.setLength(0)
            } else if (ch == ',' && squareBrackets == 0 && curlyBrackets == 0) {
                value = builder.toString()
                map[key] = value
                value = ""
                key = value
                builder.setLength(0)
            } else if (ch != ' ') {
                when (ch) {
                    '[' -> squareBrackets++
                    '{' -> curlyBrackets++
                    ']' -> squareBrackets--
                    '}' -> curlyBrackets--
                }
                builder.append(ch)
            }
        }
        map[key] = builder.toString()
        return map
    }

    fun append(map: Map<String, String>, ret: StringBuilder) {
        if (map.isEmpty()) {
            return
        }
        for (key in map.keys) {
            ret.append(key).append(":").append(map[key]).append(",\n")
        }
    }

    override fun toString(): String {
        val ret = StringBuilder("$name:{\n")
        ret.append("type:'").append(mType.toString()).append("',\n")
        if (configMap != null) {
            append(configMap!!, ret)
        }
        ret.append("},\n")
        return ret.toString()
    }

    class HelperType(private val mName: String) {
        override fun toString(): String {
            return mName
        }
    }

    companion object {
        @JvmStatic
        protected val sideMap: Map<Constraint.Side, String> = hashMapOf(
            Constraint.Side.LEFT to "'left'",
            Constraint.Side.RIGHT to "'right'",
            Constraint.Side.TOP to "'top'",
            Constraint.Side.BOTTOM to "'bottom'",
            Constraint.Side.START to "'start'",
            Constraint.Side.END to "'end'",
            Constraint.Side.BASELINE to "'baseline'",
        )

        @JvmStatic
        protected val typeMap: Map<Type, String> = hashMapOf(
            Type.VERTICAL_GUIDELINE to "vGuideline",
            Type.HORIZONTAL_GUIDELINE to "hGuideline",
            Type.VERTICAL_CHAIN to "vChain",
            Type.HORIZONTAL_CHAIN to "hChain",
            Type.BARRIER to "barrier",
        )
    }
}
