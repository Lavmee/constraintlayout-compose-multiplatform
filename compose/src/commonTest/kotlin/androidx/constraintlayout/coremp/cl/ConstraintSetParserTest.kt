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
package androidx.constraintlayout.coremp.cl

import androidx.constraintlayout.coremp.parser.CLParsingException
import androidx.constraintlayout.coremp.state.ConstraintReference
import androidx.constraintlayout.coremp.state.ConstraintSetParser
import androidx.constraintlayout.coremp.state.ConstraintSetParser.LayoutVariables
import androidx.constraintlayout.coremp.state.CorePixelDp
import androidx.constraintlayout.coremp.state.State
import androidx.constraintlayout.coremp.state.Transition
import androidx.constraintlayout.coremp.widgets.ConstraintWidget
import androidx.constraintlayout.coremp.widgets.ConstraintWidgetContainer
import kotlin.test.Test
import kotlin.test.assertNotNull

class ConstraintSetParserTest {
    @Test
    @Throws(CLParsingException::class)
    fun testSimpleConstraintSet1() {
        val jsonString = """     n                  start: {
                    id1: {
                      width: 40, height: 40,
                      start:  ['parent', 'start' , 16],
                      bottom: ['parent', 'bottom', 16]
                    }
                  },
"""
        val state = State()
        println(">>>>>> testSimpleConstraintSet1 <<<<<<")
        state.setDpToPixel(CorePixelDp { dp: Float -> dp })
        val vars = LayoutVariables()
        val root = ConstraintWidgetContainer(1000, 1000)
        val idWidget = ConstraintWidget()
        idWidget.stringId = "id1"
        ConstraintSetParser.parseJSON(jsonString, state, vars)
        root.add(idWidget)
        val id1: ConstraintReference? = state.constraints("id1")
        assertNotNull(id1)
        state.reset()
        println(">>>>>> " + root.stringId)
        for (child in root.children) {
            println(">>>>>>  " + child.stringId)
        }
        state.apply(root)
        print("children: ")
        for (child in root.children) {
            print(" " + child.stringId)
        }
        println()
        println(">>>>>> testSimpleConstraintSet1 <<<<<<")
    }

    @Test
    fun testSimpleConstraintSet2() {
        val jsonString = """    {
                Header: { exportAs: 'mtest01'},
                
                ConstraintSets: {
                  start: {
                    id1: {
                      width: 40, height: 40,
                      start:  ['parent', 'start' , 16],
                      bottom: ['parent', 'bottom', 16]
                    }
                  },
                  
                  end: {
                    id1: {
                      width: 40, height: 40,
                      end: ['parent', 'end', 16],
                      top: ['parent', 'top', 16]
                    }
                  }
                },
                
                Transitions: {
                  default: {
                    from: 'start',   to: 'end',
                  }
                }
            }"""
        val transition = Transition(
            CorePixelDp { dp: Float -> dp },
        )
        ConstraintSetParser.parseJSON(jsonString, transition, 0)
    }
}
