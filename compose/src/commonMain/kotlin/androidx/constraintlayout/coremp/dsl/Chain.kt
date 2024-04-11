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

open class Chain(name: String) : Helper(name, HelperType("")) {
    enum class Style {
        PACKED,
        SPREAD,
        SPREAD_INSIDE,
    }

    private var mStyle: Style? = null
    protected var references: ArrayList<Ref?> = ArrayList()

    @Suppress("UNUSED")
    fun getStyle(): Style? {
        return mStyle
    }

    fun setStyle(style: Style) {
        mStyle = style
        configMap!!["style"] = styleMap[style]!!
    }

    /**
     * convert references to a String representation
     *
     * @return a String representation of references
     */
    fun referencesToString(): String {
        if (references.isEmpty()) {
            return ""
        }
        val builder = StringBuilder("[")
        for (ref in references) {
            builder.append(ref.toString())
        }
        builder.append("]")
        return builder.toString()
    }

    /**
     * Add a new reference
     *
     * @param ref reference
     * @return Chain
     */
    fun addReference(ref: Ref?): Chain {
        references.add(ref)
        configMap!!["contains"] = referencesToString()
        return this
    }

    /**
     * Add a new reference
     *
     * @param ref reference in a String representation
     * @return Chain
     */
    fun addReference(ref: String): Chain {
        return addReference(Ref.parseStringToRef(ref))
    }

    open inner class Anchor(side: Constraint.Side) {
        private val mSide: Constraint.Side = side
        var mConnection: Constraint.Anchor? = null
        var mMargin = 0
        var mGoneMargin = Int.MIN_VALUE

        fun getId(): String {
            return name
        }

        fun build(builder: StringBuilder) {
            if (mConnection != null) {
                builder.append(mSide.toString().lowercase())
                    .append(":").append(this).append(",\n")
            }
        }

        override fun toString(): String {
            val ret = StringBuilder("[")
            if (mConnection != null) {
                ret.append("'").append(mConnection!!.getId()).append("',")
                    .append("'")
                    .append(mConnection!!.mSide.toString().lowercase())
                    .append("'")
            }
            if (mMargin != 0) {
                ret.append(",").append(mMargin)
            }
            if (mGoneMargin != Int.MIN_VALUE) {
                if (mMargin == 0) {
                    ret.append(",0,").append(mGoneMargin)
                } else {
                    ret.append(",").append(mGoneMargin)
                }
            }
            ret.append("]")
            return ret.toString()
        }
    }

    companion object {
        protected val styleMap: Map<Style, String> = hashMapOf(
            Style.SPREAD to "'spread'",
            Style.SPREAD_INSIDE to "'spread_inside'",
            Style.PACKED to "'packed'",
        )
    }
}
