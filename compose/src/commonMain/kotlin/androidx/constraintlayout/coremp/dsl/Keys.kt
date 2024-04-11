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

open class Keys {
    protected fun unpack(str: Array<out String>): String {
        val ret = StringBuilder("[")
        for (i in str.indices) {
            ret.append(if (i == 0) "'" else ",'")
            ret.append(str[i])
            ret.append("'")
        }
        ret.append("]")
        return ret.toString()
    }

    protected fun append(builder: StringBuilder, name: String?, value: Int) {
        if (value != Int.MIN_VALUE) {
            builder.append(name)
            builder.append(":'").append(value).append("',\n")
        }
    }

    protected fun append(builder: StringBuilder, name: String?, value: String?) {
        if (value != null) {
            builder.append(name)
            builder.append(":'").append(value).append("',\n")
        }
    }

    protected fun append(builder: StringBuilder, name: String?, value: Float) {
        if (value.isNaN()) {
            return
        }
        builder.append(name)
        builder.append(":").append(value).append(",\n")
    }

    protected fun append(builder: StringBuilder, name: String?, array: Array<out String>?) {
        if (array != null) {
            builder.append(name)
            builder.append(":").append(unpack(array)).append(",\n")
        }
    }

    protected fun append(builder: StringBuilder, name: String?, array: FloatArray?) {
        if (array != null) {
            builder.append(name)
            builder.append("percentWidth:").append(array.contentToString()).append(",\n")
        }
    }
}
