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

class ConstraintSet(name: String) {
    private val mName: String = name
    private var mConstraints = ArrayList<Constraint>()
    private var mHelpers = ArrayList<Helper>()

    fun add(c: Constraint) {
        mConstraints.add(c)
    }

    fun add(h: Helper) {
        mHelpers.add(h)
    }

    override fun toString(): String {
        val ret = StringBuilder("$mName:{\n")
        if (!mConstraints.isEmpty()) {
            for (cs in mConstraints) {
                ret.append(cs.toString())
            }
        }
        if (!mHelpers.isEmpty()) {
            for (h in mHelpers) {
                ret.append(h.toString())
            }
        }
        ret.append("},\n")
        return ret.toString()
    }
}
