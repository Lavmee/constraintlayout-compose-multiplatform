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
package androidx.constraintlayout.coremp.state

import androidx.constraintlayout.coremp.state.helpers.Facade
import androidx.constraintlayout.coremp.widgets.ConstraintWidget

interface Reference {
    // @TODO: add description
    fun getConstraintWidget(): ConstraintWidget?

    // @TODO: add description
    fun setConstraintWidget(widget: ConstraintWidget?)

    // @TODO: add description
    fun setKey(key: Any?)

    // @TODO: add description
    fun getKey(): Any?

    // @TODO: add description
    fun apply()

    // @TODO: add description
    fun getFacade(): Facade?
}
