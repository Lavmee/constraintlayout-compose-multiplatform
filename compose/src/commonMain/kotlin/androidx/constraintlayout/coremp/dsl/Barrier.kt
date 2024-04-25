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

class Barrier : Helper {
    private var mDirection: Constraint.Side? = null
    private var mMargin = Int.MIN_VALUE
    private val references: ArrayList<Ref?> = ArrayList()

    constructor(name: String) : super(name, HelperType(typeMap[Type.BARRIER]!!))

    constructor(name: String, config: String) : super(name, HelperType(typeMap[Type.BARRIER]!!), config) {
        configMap = convertConfigToMap()!!.toMutableMap()
        if (configMap!!.containsKey("contains")) {
            Ref.addStringToReferences(
                configMap!!["contains"],
                references,
            )
        }
    }

    /**
     * Get the direction of the Barrier
     *
     * @return direction
     */
    @Suppress("UNUSED")
    fun getDirection(): Constraint.Side? {
        return mDirection
    }

    /**
     * Set the direction of the Barrier
     *
     * @param direction
     */
    fun setDirection(direction: Constraint.Side) {
        mDirection = direction
        configMap!!["direction"] = sideMap[direction]!!
    }

    /**
     * Get the margin of the Barrier
     *
     * @return margin
     */
    @Suppress("UNUSED")
    fun getMargin(): Int {
        return mMargin
    }

    /**
     * Set the margin of the Barrier
     *
     * @param margin
     */
    fun setMargin(margin: Int) {
        mMargin = margin
        configMap!!["margin"] = margin.toString()
    }

    /**
     * Convert references into a String representation
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
     * @return Barrier
     */
    fun addReference(ref: Ref?): Barrier {
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
    @Suppress("UNUSED")
    fun addReference(ref: String): Barrier {
        return addReference(Ref.parseStringToRef(ref))
    }
}
