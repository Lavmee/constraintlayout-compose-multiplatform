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
package androidx.constraintlayout.coremp.motion.parse

import androidx.constraintlayout.coremp.motion.utils.TypedBundle
import androidx.constraintlayout.coremp.motion.utils.TypedValues
import androidx.constraintlayout.coremp.parser.CLKey
import androidx.constraintlayout.coremp.parser.CLParser
import androidx.constraintlayout.coremp.parser.CLParsingException

class KeyParser {

    private fun interface Ids {
        operator fun get(str: String?): Int
    }

    private fun interface DataType {
        operator fun get(str: Int): Int
    }

    companion object {

        private fun parse(str: String, table: Ids, dtype: DataType): TypedBundle {
            val bundle = TypedBundle()
            try {
                val parsedContent = CLParser.parse(str)
                val n = parsedContent.size()
                for (i in 0 until n) {
                    val clkey = parsedContent[i] as CLKey
                    val type = clkey.content()
                    val value = clkey.value
                    val id = table[type]
                    if (id == -1) {
                        println("unknown type $type")
                        continue
                    }
                    when (dtype.get(id)) {
                        TypedValues.FLOAT_MASK -> {
                            bundle.add(id, value.float)
                            println("parse " + type + " FLOAT_MASK > " + value.float)
                        }

                        TypedValues.STRING_MASK -> {
                            bundle.add(id, value.content())
                            println("parse " + type + " STRING_MASK > " + value.content())
                        }

                        TypedValues.INT_MASK -> {
                            bundle.add(id, value.getInt())
                            println("parse " + type + " INT_MASK > " + value.getInt())
                        }

                        TypedValues.BOOLEAN_MASK -> bundle.add(id, parsedContent.getBoolean(i))
                    }
                }
            } catch (e: CLParsingException) {
                // TODO replace with something not equal to printStackTrace();
                println(
                    e.toString() + "\n" + e.stackTraceToString()
                        .replace("[", "   at ")
                        .replace(",", "\n   at")
                        .replace("]", ""),
                )
            }
            return bundle
        }

        // @TODO: add description
        fun parseAttributes(str: String): TypedBundle {
            return parse(
                str = str,
                table = { str -> TypedValues.AttributesType.getId(str) },
                dtype = { str -> TypedValues.AttributesType.getType(str) },
            )
        }

        // @TODO: add description
        fun main() {
            val str = """
             {frame:22,
             target:'widget1',
             easing:'easeIn',
             curveFit:'spline',
             progress:0.3,
             alpha:0.2,
             elevation:0.7,
             rotationZ:23,
             rotationX:25.0,
             rotationY:27.0,
             pivotX:15,
             pivotY:17,
             pivotTarget:'32',
             pathRotate:23,
             scaleX:0.5,
             scaleY:0.7,
             translationX:5,
             translationY:7,
             translationZ:11,
             }
            """.trimIndent()
            parseAttributes(str)
        }
    }
}
