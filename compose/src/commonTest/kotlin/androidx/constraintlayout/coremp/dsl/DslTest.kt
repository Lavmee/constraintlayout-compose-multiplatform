/*
 * Copyright (C) 2022 The Android Open Source Project
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

import androidx.constraintlayout.coremp.dsl.Helper.HelperType
import androidx.constraintlayout.coremp.parser.CLParser
import androidx.constraintlayout.coremp.parser.CLParsingException
import androidx.constraintlayout.coremp.state.CorePixelDp
import androidx.constraintlayout.coremp.state.TransitionParser
import kotlin.test.Test
import kotlin.test.assertEquals

class DslTest {
    @Test
    fun testTransition01() {
        val motionScene = MotionScene()
        motionScene.addTransition(Transition("start", "end"))
        println(motionScene)
        val exp = """
            {
            Transitions:{
            default:{
            from:'start',
            to:'end',
            },
            },
            }
            
        """.trimIndent()
        assertEquals(exp, motionScene.toString())
    }

    @Test
    fun testTransition02() {
        val motionScene = MotionScene()
        motionScene.addTransition(Transition("expand", "start", "end"))
        println(motionScene)
        val exp = """
            {
            Transitions:{
            expand:{
            from:'start',
            to:'end',
            },
            },
            }
            
        """.trimIndent()
        assertEquals(exp, motionScene.toString())
    }

    @Test
    @Throws(CLParsingException::class)
    fun testOnSwipe01() {
        val motionScene = MotionScene()
        val transition = Transition("expand", "start", "end")
        transition.setOnSwipe(OnSwipe())
        motionScene.addTransition(transition)
        println(motionScene)
        val exp = """
            {
            Transitions:{
            expand:{
            from:'start',
            to:'end',
            OnSwipe:{
            },
            },
            },
            }
            
        """.trimIndent()
        assertEquals(exp, motionScene.toString())
        TransitionParser.parse(
            CLParser.parse(transition.toString()),
            androidx.constraintlayout.coremp.dsl.DslTest.Companion.sTransitionState,
        )
    }

    @Test
    @Throws(CLParsingException::class)
    fun testOnSwipe02() {
        val motionScene = MotionScene()
        val transition = Transition("expand", "start", "end")
        transition.setOnSwipe(OnSwipe("button", OnSwipe.Side.RIGHT, OnSwipe.Drag.RIGHT))
        motionScene.addTransition(transition)
        println(motionScene)
        val exp = """
            {
            Transitions:{
            expand:{
            from:'start',
            to:'end',
            OnSwipe:{
            anchor:'button',
            direction:'right',
            side:'right',
            },
            },
            },
            }
            
        """.trimIndent()
        assertEquals(exp, motionScene.toString())
        TransitionParser.parse(
            CLParser.parse(transition.toString()),
            androidx.constraintlayout.coremp.dsl.DslTest.Companion.sTransitionState,
        )
    }

    @Test
    @Throws(CLParsingException::class)
    fun testOnKeyPosition01() {
        val motionScene = MotionScene()
        val transition = Transition("expand", "start", "end")
        transition.setOnSwipe(OnSwipe("button", OnSwipe.Side.RIGHT, OnSwipe.Drag.RIGHT))
        val kp = KeyPosition("button", 32)
        kp.setPercentX(0.5f)
        transition.setKeyFrames(kp)
        motionScene.addTransition(transition)
        println(motionScene)
        val exp = """
            {
            Transitions:{
            expand:{
            from:'start',
            to:'end',
            OnSwipe:{
            anchor:'button',
            direction:'right',
            side:'right',
            },
            keyFrames:{
            KeyPositions:{
            target:'button',
            frame:32,
            type:'CARTESIAN',
            percentX:0.5,
            },
            },
            },
            },
            }
            
        """.trimIndent()
        assertEquals(exp, motionScene.toString())
        val formattedJson: String = CLParser.parse(motionScene.toString()).toFormattedJSON()
        val formatExp = """{
  Transitions: {
    expand: {
      from: 'start',
      to: 'end',
      OnSwipe: { anchor: 'button', direction: 'right', side: 'right' },
      keyFrames: {
        KeyPositions: {
          target:           'button',
          frame:           32,
          type:           'CARTESIAN',
          percentX:           0.5
        }
      }
    }
  }
}"""
        assertEquals(formatExp, formattedJson)
        TransitionParser.parse(
            CLParser.parse(transition.toString()),
            androidx.constraintlayout.coremp.dsl.DslTest.Companion.sTransitionState,
        )
    }

    @Test
    @Throws(CLParsingException::class)
    fun testOnKeyPositions0() {
        val motionScene = MotionScene()
        val transition = Transition("expand", "start", "end")
        transition.setOnSwipe(OnSwipe("button", OnSwipe.Side.RIGHT, OnSwipe.Drag.RIGHT))
        val kp = KeyPositions(2, "button1", "button2")
        transition.setKeyFrames(kp)
        motionScene.addTransition(transition)
        println(motionScene)
        val exp = """
            {
            Transitions:{
            expand:{
            from:'start',
            to:'end',
            OnSwipe:{
            anchor:'button',
            direction:'right',
            side:'right',
            },
            keyFrames:{
            KeyPositions:{
            target:['button1','button2'],
            frame:[33, 66],
            },
            },
            },
            },
            }
            
        """.trimIndent()
        assertEquals(exp, motionScene.toString())
        val formattedJson: String = CLParser.parse(motionScene.toString()).toFormattedJSON()
        val formatExp = """{
  Transitions: {
    expand: {
      from: 'start',
      to: 'end',
      OnSwipe: { anchor: 'button', direction: 'right', side: 'right' },
      keyFrames: { KeyPositions: { target: ['button1', 'button2'], frame: [33, 66] } }
    }
  }
}"""
        assertEquals(formatExp, formattedJson)
        TransitionParser.parse(
            CLParser.parse(transition.toString()),
            androidx.constraintlayout.coremp.dsl.DslTest.Companion.sTransitionState,
        )
    }

    @Test
    @Throws(CLParsingException::class)
    fun testAnchor01() {
        val motionScene = MotionScene()
        val constraintSet = ConstraintSet("start")
        val constraint = Constraint("a")
        val constraint2 = Constraint("b")
        constraintSet.add(constraint)
        constraint.linkToLeft(constraint2.getLeft())
        motionScene.addConstraintSet(constraintSet)
        println(motionScene)
        val exp = """
            {
            ConstraintSets:{
            start:{
            a:{
            left:['b','left'],
            },
            },
            },
            }
            
        """.trimIndent()
        assertEquals(exp, motionScene.toString())
        val formattedJson: String = CLParser.parse(motionScene.toString()).toFormattedJSON()
        val formatExp = """{
  ConstraintSets: {
    start: {
      a: { left: ['b', 'left'] }
    }
  }
}"""
        assertEquals(formatExp, formattedJson)
        TransitionParser.parse(
            CLParser.parse(motionScene.toString()),
            androidx.constraintlayout.coremp.dsl.DslTest.Companion.sTransitionState,
        )
    }

    @Test
    @Throws(CLParsingException::class)
    fun testAnchor02() {
        val motionScene = MotionScene()
        val constraintSet = ConstraintSet("start")
        val constraint = Constraint("a")
        val constraint2 = Constraint("b")
        constraintSet.add(constraint)
        constraint.linkToLeft(constraint2.getLeft(), 15)
        motionScene.addConstraintSet(constraintSet)
        println(motionScene)
        val exp = """
            {
            ConstraintSets:{
            start:{
            a:{
            left:['b','left',15],
            },
            },
            },
            }
            
        """.trimIndent()
        assertEquals(exp, motionScene.toString())
        val formattedJson: String = CLParser.parse(motionScene.toString()).toFormattedJSON()
        val formatExp = """{
  ConstraintSets: {
    start: {
      a: { left: ['b', 'left', 15] }
    }
  }
}"""
        assertEquals(formatExp, formattedJson)
        TransitionParser.parse(
            CLParser.parse(motionScene.toString()),
            androidx.constraintlayout.coremp.dsl.DslTest.Companion.sTransitionState,
        )
    }

    @Test
    @Throws(CLParsingException::class)
    fun testAnchor03() {
        val motionScene = MotionScene()
        val constraintSet = ConstraintSet("start")
        val constraint = Constraint("a")
        val constraint2 = Constraint("b")
        val constraint3 = Constraint("c")
        val constraint4 = Constraint("d")
        constraintSet.add(constraint)
        constraint.linkToLeft(constraint2.getRight(), 5, 10)
        constraint.linkToTop(constraint3.getBottom(), 0, 15)
        constraint.linkToBaseline(constraint4.getBaseline())
        motionScene.addConstraintSet(constraintSet)
        println(motionScene)
        val exp = """
            {
            ConstraintSets:{
            start:{
            a:{
            left:['b','right',5,10],
            top:['c','bottom',0,15],
            baseline:['d','baseline'],
            },
            },
            },
            }
            
        """.trimIndent()
        assertEquals(exp, motionScene.toString())
        val formattedJson: String = CLParser.parse(motionScene.toString()).toFormattedJSON()
        val formatExp = """{
  ConstraintSets: {
    start: {
      a: {
        left: ['b', 'right', 5, 10],
        top: ['c', 'bottom', 0, 15],
        baseline: ['d', 'baseline']
      }
    }
  }
}"""
        assertEquals(formatExp, formattedJson)
        TransitionParser.parse(
            CLParser.parse(motionScene.toString()),
            androidx.constraintlayout.coremp.dsl.DslTest.Companion.sTransitionState,
        )
    }

    @Test
    @Throws(CLParsingException::class)
    fun testConstraint01() {
        val motionScene = MotionScene()
        val constraintSet = ConstraintSet("start")
        val constraint = Constraint("a")
        constraint.setHeight(0)
        constraint.setWidth(40)
        constraint.setDimensionRatio("1:1")
        constraintSet.add(constraint)
        motionScene.addConstraintSet(constraintSet)
        println(motionScene)
        val exp = """
            {
            ConstraintSets:{
            start:{
            a:{
            width:40,
            height:0,
            dimensionRatio:'1:1',
            },
            },
            },
            }
            
        """.trimIndent()
        assertEquals(exp, motionScene.toString())
        val formattedJson: String = CLParser.parse(motionScene.toString()).toFormattedJSON()
        val formatExp = """{
  ConstraintSets: {
    start: {
      a: { width: 40, height: 0, dimensionRatio: '1:1' }
    }
  }
}"""
        assertEquals(formatExp, formattedJson)
        TransitionParser.parse(
            CLParser.parse(motionScene.toString()),
            androidx.constraintlayout.coremp.dsl.DslTest.Companion.sTransitionState,
        )
    }

    @Test
    @Throws(CLParsingException::class)
    fun testConstraint02() {
        val motionScene = MotionScene()
        val constraintSet = ConstraintSet("start")
        val constraint = Constraint("a")
        constraint.setWidthPercent(50f)
        constraint.setHeightPercent(60f)
        constraint.setHorizontalBias(0.3f)
        constraint.setVerticalBias(0.2f)
        constraint.setCircleConstraint("parent")
        constraint.setCircleRadius(10)
        constraint.setVerticalWeight(2.1f)
        constraint.setHorizontalWeight(1f)
        constraintSet.add(constraint)
        motionScene.addConstraintSet(constraintSet)
        println(motionScene)
        val exp = """
            {
            ConstraintSets:{
            start:{
            a:{
            horizontalBias:0.3,
            verticalBias:0.2,
            circular:['parent',0,10],
            verticalWeight:2.1,
            horizontalWeight:1.0,
            width:'50%',
            height:'60%',
            },
            },
            },
            }
            
        """.trimIndent()
        assertEquals(exp, motionScene.toString())
        val formattedJson: String = CLParser.parse(motionScene.toString()).toFormattedJSON()
        val formatExp = """{
  ConstraintSets: {
    start: {
      a: {
        horizontalBias: 0.3,
        verticalBias: 0.2,
        circular: ['parent', 0, 10],
        verticalWeight: 2.1,
        horizontalWeight: 1,
        width: '50%',
        height: '60%'
      }
    }
  }
}"""
        assertEquals(formatExp, formattedJson)
        TransitionParser.parse(
            CLParser.parse(motionScene.toString()),
            androidx.constraintlayout.coremp.dsl.DslTest.Companion.sTransitionState,
        )
    }

    @Test
    @Throws(CLParsingException::class)
    fun testConstraint03() {
        val motionScene = MotionScene()
        val constraintSet = ConstraintSet("start")
        val constraint = Constraint("a")
        constraint.setWidthDefault(Constraint.Behaviour.WRAP)
        constraint.setHeightDefault(Constraint.Behaviour.SPREAD)
        constraint.setWidthMax(30)
        constraint.setCircleConstraint("parent")
        constraint.setCircleAngle(10f)
        constraint.setReferenceIds(arrayOf("a", "b", "c"))
        val constraint2 = Constraint("b")
        constraint2.setHorizontalChainStyle(Constraint.ChainMode.SPREAD_INSIDE)
        constraint2.setVerticalChainStyle(Constraint.ChainMode.PACKED)
        constraint2.setConstrainedWidth(true)
        constraint2.setConstrainedHeight(true)
        constraintSet.add(constraint)
        constraintSet.add(constraint2)
        motionScene.addConstraintSet(constraintSet)
        println(motionScene)
        val exp = """
            {
            ConstraintSets:{
            start:{
            a:{
            circular:['parent',10.0],
            width:{value:'wrap',max:30},
            height:'spread',
            referenceIds:['a','b','c'],
            },
            b:{
            horizontalChainStyle:'spread_inside',
            verticalChainStyle:'packed',
            constrainedWidth:true,
            constrainedHeight:true,
            },
            },
            },
            }
            
        """.trimIndent()
        assertEquals(exp, motionScene.toString())
        val formattedJson: String = CLParser.parse(motionScene.toString()).toFormattedJSON()
        val formatExp = """{
  ConstraintSets: {
    start: {
      a: {
        circular: ['parent', 10],
        width: { value: 'wrap', max: 30 },
        height: 'spread',
        referenceIds: ['a', 'b', 'c']
      },
      b: {
        horizontalChainStyle: 'spread_inside',
        verticalChainStyle: 'packed',
        constrainedWidth: true,
        constrainedHeight: true
      }
    }
  }
}"""
        assertEquals(formatExp, formattedJson)
        TransitionParser.parse(
            CLParser.parse(motionScene.toString()),
            androidx.constraintlayout.coremp.dsl.DslTest.Companion.sTransitionState,
        )
    }

    @Test
    @Throws(CLParsingException::class)
    fun testConstraintSet01() {
        val motionScene = MotionScene()
        val constraintSet1 = ConstraintSet("start")
        val constraintSet2 = ConstraintSet("end")
        val constraint1 = Constraint("a")
        val constraint2 = Constraint("b")
        val constraint3 = Constraint("c")
        constraintSet1.add(constraint1)
        constraintSet1.add(constraint2)
        constraintSet2.add(constraint3)
        motionScene.addConstraintSet(constraintSet1)
        motionScene.addConstraintSet(constraintSet2)
        println(motionScene)
        val exp = """
            {
            ConstraintSets:{
            start:{
            a:{
            },
            b:{
            },
            },
            end:{
            c:{
            },
            },
            },
            }
            
        """.trimIndent()
        assertEquals(exp, motionScene.toString())
        val formattedJson: String = CLParser.parse(motionScene.toString()).toFormattedJSON()
        val formatExp = """{
  ConstraintSets: {
    start: {
      a: {  },
      b: {  }
    },
    end: {
      c: {  }
    }
  }
}"""
        assertEquals(formatExp, formattedJson)
        TransitionParser.parse(
            CLParser.parse(motionScene.toString()),
            androidx.constraintlayout.coremp.dsl.DslTest.Companion.sTransitionState,
        )
    }

    @Test
    @Throws(CLParsingException::class)
    fun testConstraintSet02() {
        val motionScene = MotionScene()
        val constraintSet1 = ConstraintSet("start")
        val constraintSet2 = ConstraintSet("end")
        val constraint1 = Constraint("a")
        val constraint2 = Constraint("b")
        val constraint3 = Constraint("a")
        val constraint4 = Constraint("b")
        constraint1.setWidth(50)
        constraint1.setHeight(60)
        constraint2.setWidth(30)
        constraint2.setHeight(0)
        constraint2.setDimensionRatio("1:1")
        constraint1.linkToLeft(constraint2.getLeft(), 10)
        constraint1.linkToRight(constraint2.getRight(), 0, 15)
        constraintSet1.add(constraint1)
        constraint3.setHeightPercent(40f)
        constraint3.setWidthPercent(30f)
        constraint4.setHeight(20)
        constraint4.setHeight(30)
        constraint4.setWidthDefault(Constraint.Behaviour.SPREAD)
        constraint4.setHeightDefault(Constraint.Behaviour.WRAP)
        constraint4.setHeightMax(100)
        constraint4.linkToTop(constraint3.getTop(), 5, 10)
        constraint4.linkToBottom(constraint3.getBottom())
        constraintSet1.add(constraint2)
        constraintSet2.add(constraint3)
        constraintSet2.add(constraint4)
        motionScene.addConstraintSet(constraintSet1)
        motionScene.addConstraintSet(constraintSet2)
        println(motionScene)
        val exp = """
            {
            ConstraintSets:{
            start:{
            a:{
            left:['b','left',10],
            right:['b','right',0,15],
            width:50,
            height:60,
            },
            b:{
            width:30,
            height:0,
            dimensionRatio:'1:1',
            },
            },
            end:{
            a:{
            width:'30%',
            height:'40%',
            },
            b:{
            top:['a','top',5,10],
            bottom:['a','bottom'],
            height:30,
            width:'spread',
            height:{value:'wrap',max:100},
            },
            },
            },
            }
            
        """.trimIndent()
        assertEquals(exp, motionScene.toString())
        val formattedJson: String = CLParser.parse(motionScene.toString()).toFormattedJSON()
        val formatExp = """{
  ConstraintSets: {
    start: {
      a: {
        left: ['b', 'left', 10],
        right: ['b', 'right', 0, 15],
        width: 50,
        height: 60
      },
      b: { width: 30, height: 0, dimensionRatio: '1:1' }
    },
    end: {
      a: { width: '30%', height: '40%' },
      b: {
        top: ['a', 'top', 5, 10],
        bottom: ['a', 'bottom'],
        height: 30,
        width: 'spread',
        height: { value: 'wrap', max: 100 }
      }
    }
  }
}"""
        assertEquals(formatExp, formattedJson)
        TransitionParser.parse(
            CLParser.parse(motionScene.toString()),
            androidx.constraintlayout.coremp.dsl.DslTest.Companion.sTransitionState,
        )
    }

    @Test
    @Throws(CLParsingException::class)
    fun testHelper01() {
        val motionScene = MotionScene()
        val constraintSet = ConstraintSet("start")
        val helper = Helper(
            "barrier1",
            HelperType("barrier"),
            "margin:10,contains:[['a1', 1, 2],'b1']",
        )
        constraintSet.add(helper)
        motionScene.addConstraintSet(constraintSet)
        println(motionScene)
        val exp = """
            {
            ConstraintSets:{
            start:{
            barrier1:{
            type:'barrier',
            margin:10,
            contains:[['a1',1,2],'b1'],
            },
            },
            },
            }
            
        """.trimIndent()
        assertEquals(exp, motionScene.toString())
        val formattedJson: String = CLParser.parse(motionScene.toString()).toFormattedJSON()
        val formatExp = """{
  ConstraintSets: {
    start: {
      barrier1: { type: 'barrier', margin: 10, contains: [['a1', 1, 2], 'b1'] }
    }
  }
}"""
        assertEquals(formatExp, formattedJson)
        TransitionParser.parse(
            CLParser.parse(motionScene.toString()),
            androidx.constraintlayout.coremp.dsl.DslTest.Companion.sTransitionState,
        )
    }

    @Test
    @Throws(CLParsingException::class)
    fun testHelper02() {
        val motionScene = MotionScene()
        val constraintSet = ConstraintSet("start")
        val helper1 = Helper(
            "grid1",
            HelperType("grid"),
            "row:3, column:5",
        )
        constraintSet.add(helper1)
        val helper2 = Helper(
            "vchain",
            HelperType("vChain"),
            "style:'spread',top:['a1',1 , 2], bottom:['b1', 10], contains:['c1','d1']",
        )
        constraintSet.add(helper2)
        motionScene.addConstraintSet(constraintSet)
        println(motionScene)
        val exp = """
            {
            ConstraintSets:{
            start:{
            grid1:{
            type:'grid',
            column:5,
            row:3,
            },
            vchain:{
            type:'vChain',
            contains:['c1','d1'],
            top:['a1',1,2],
            bottom:['b1',10],
            style:'spread',
            },
            },
            },
            }
            
        """.trimIndent()
        assertEquals(exp, motionScene.toString())
        val formattedJson: String = CLParser.parse(motionScene.toString()).toFormattedJSON()
        val formatExp = """{
  ConstraintSets: {
    start: {
      grid1: { type: 'grid', column: 5, row: 3 },
      vchain: {
        type: 'vChain',
        contains: ['c1', 'd1'],
        top: ['a1', 1, 2],
        bottom: ['b1', 10],
        style: 'spread'
      }
    }
  }
}"""
        assertEquals(formatExp, formattedJson)
        TransitionParser.parse(
            CLParser.parse(motionScene.toString()),
            androidx.constraintlayout.coremp.dsl.DslTest.Companion.sTransitionState,
        )
    }

    @Test
    @Throws(CLParsingException::class)
    fun testVGuideline01() {
        val motionScene = MotionScene()
        val constraintSet = ConstraintSet("start")
        val guideline1 = VGuideline("g1")
        constraintSet.add(guideline1)
        val guideline2 = VGuideline("g2", "start:10")
        constraintSet.add(guideline2)
        val guideline3 = VGuideline("g3", "percent:0.5")
        constraintSet.add(guideline3)
        motionScene.addConstraintSet(constraintSet)
        println(motionScene)
        val exp = """
            {
            ConstraintSets:{
            start:{
            g1:{
            type:'vGuideline',
            },
            g2:{
            type:'vGuideline',
            start:10,
            },
            g3:{
            type:'vGuideline',
            percent:0.5,
            },
            },
            },
            }
            
        """.trimIndent()
        assertEquals(exp, motionScene.toString())
        val formattedJson: String = CLParser.parse(motionScene.toString()).toFormattedJSON()
        val formatExp = """{
  ConstraintSets: {
    start: {
      g1: { type: 'vGuideline' },
      g2: { type: 'vGuideline', start: 10 },
      g3: { type: 'vGuideline', percent: 0.5 }
    }
  }
}"""
        assertEquals(formatExp, formattedJson)
        TransitionParser.parse(
            CLParser.parse(motionScene.toString()),
            androidx.constraintlayout.coremp.dsl.DslTest.Companion.sTransitionState,
        )
    }

    @Test
    @Throws(CLParsingException::class)
    fun testVGuideline02() {
        val motionScene = MotionScene()
        val constraintSet = ConstraintSet("start")
        val guideline1 = VGuideline("g1")
        guideline1.setEnd(20)
        constraintSet.add(guideline1)
        val guideline2 = VGuideline("g2", "start:10")
        guideline2.setStart(40)
        constraintSet.add(guideline2)
        val guideline3 = VGuideline("g3", "percent:0.5")
        guideline3.setPercent(0.75f)
        constraintSet.add(guideline3)
        motionScene.addConstraintSet(constraintSet)
        println(motionScene)
        val exp = """
            {
            ConstraintSets:{
            start:{
            g1:{
            type:'vGuideline',
            end:20,
            },
            g2:{
            type:'vGuideline',
            start:40,
            },
            g3:{
            type:'vGuideline',
            percent:0.75,
            },
            },
            },
            }
            
        """.trimIndent()
        assertEquals(exp, motionScene.toString())
        val formattedJson: String = CLParser.parse(motionScene.toString()).toFormattedJSON()
        val formatExp = """{
  ConstraintSets: {
    start: {
      g1: { type: 'vGuideline', end: 20 },
      g2: { type: 'vGuideline', start: 40 },
      g3: { type: 'vGuideline', percent: 0.75 }
    }
  }
}"""
        assertEquals(formatExp, formattedJson)
        TransitionParser.parse(
            CLParser.parse(motionScene.toString()),
            androidx.constraintlayout.coremp.dsl.DslTest.Companion.sTransitionState,
        )
    }

    @Test
    @Throws(CLParsingException::class)
    fun testHGuideline01() {
        val motionScene = MotionScene()
        val constraintSet = ConstraintSet("start")
        val guideline1 = HGuideline("g1")
        constraintSet.add(guideline1)
        val guideline2 = HGuideline("g2", "start:10")
        constraintSet.add(guideline2)
        val guideline3 = HGuideline("g3", "percent:0.5")
        constraintSet.add(guideline3)
        motionScene.addConstraintSet(constraintSet)
        println(motionScene)
        val exp = """
            {
            ConstraintSets:{
            start:{
            g1:{
            type:'hGuideline',
            },
            g2:{
            type:'hGuideline',
            start:10,
            },
            g3:{
            type:'hGuideline',
            percent:0.5,
            },
            },
            },
            }
            
        """.trimIndent()
        assertEquals(exp, motionScene.toString())
        val formattedJson: String = CLParser.parse(motionScene.toString()).toFormattedJSON()
        val formatExp = """{
  ConstraintSets: {
    start: {
      g1: { type: 'hGuideline' },
      g2: { type: 'hGuideline', start: 10 },
      g3: { type: 'hGuideline', percent: 0.5 }
    }
  }
}"""
        assertEquals(formatExp, formattedJson)
        TransitionParser.parse(
            CLParser.parse(motionScene.toString()),
            androidx.constraintlayout.coremp.dsl.DslTest.Companion.sTransitionState,
        )
    }

    @Test
    @Throws(CLParsingException::class)
    fun testHGuideline02() {
        val motionScene = MotionScene()
        val constraintSet = ConstraintSet("start")
        val guideline1 = HGuideline("g1")
        constraintSet.add(guideline1)
        guideline1.setEnd(30)
        val guideline2 = HGuideline("g2", "start:10")
        constraintSet.add(guideline2)
        guideline2.setStart(50)
        val guideline3 = HGuideline("g3", "percent:0.5")
        guideline3.setPercent(0.25f)
        constraintSet.add(guideline3)
        motionScene.addConstraintSet(constraintSet)
        println(motionScene)
        val exp = """
            {
            ConstraintSets:{
            start:{
            g1:{
            type:'hGuideline',
            end:30,
            },
            g2:{
            type:'hGuideline',
            start:50,
            },
            g3:{
            type:'hGuideline',
            percent:0.25,
            },
            },
            },
            }
            
        """.trimIndent()
        assertEquals(exp, motionScene.toString())
        val formattedJson: String = CLParser.parse(motionScene.toString()).toFormattedJSON()
        val formatExp = """{
  ConstraintSets: {
    start: {
      g1: { type: 'hGuideline', end: 30 },
      g2: { type: 'hGuideline', start: 50 },
      g3: { type: 'hGuideline', percent: 0.25 }
    }
  }
}"""
        assertEquals(formatExp, formattedJson)
        TransitionParser.parse(
            CLParser.parse(motionScene.toString()),
            androidx.constraintlayout.coremp.dsl.DslTest.Companion.sTransitionState,
        )
    }

    @Test
    @Throws(CLParsingException::class)
    fun testVChain01() {
        val motionScene = MotionScene()
        val constraintSet = ConstraintSet("start")
        val chain1 = VChain("chain1")
        constraintSet.add(chain1)
        val chain2 = VChain("chain2", "style:'spread'")
        constraintSet.add(chain2)
        val chain3 = VChain("chain3", "style:'packed', top:['a1',1,2]")
        constraintSet.add(chain3)
        motionScene.addConstraintSet(constraintSet)
        println(motionScene)
        val exp = """
            {
            ConstraintSets:{
            start:{
            chain1:{
            type:'vChain',
            },
            chain2:{
            type:'vChain',
            style:'spread',
            },
            chain3:{
            type:'vChain',
            top:['a1',1,2],
            style:'packed',
            },
            },
            },
            }
            
        """.trimIndent()
        assertEquals(exp, motionScene.toString())
        val formattedJson: String = CLParser.parse(motionScene.toString()).toFormattedJSON()
        val formatExp = """{
  ConstraintSets: {
    start: {
      chain1: { type: 'vChain' },
      chain2: { type: 'vChain', style: 'spread' },
      chain3: { type: 'vChain', top: ['a1', 1, 2], style: 'packed' }
    }
  }
}"""
        assertEquals(formatExp, formattedJson)
        TransitionParser.parse(
            CLParser.parse(motionScene.toString()),
            androidx.constraintlayout.coremp.dsl.DslTest.Companion.sTransitionState,
        )
    }

    @Test
    @Throws(CLParsingException::class)
    fun testVChain02() {
        val motionScene = MotionScene()
        val constraintSet = ConstraintSet("start")
        val chain1 = VChain("chain1")
        chain1.setStyle(Chain.Style.SPREAD_INSIDE)
        constraintSet.add(chain1)
        val chain2 = VChain("chain2", "style:spread")
        chain2.setStyle(Chain.Style.PACKED)
        chain2.linkToBaseline(Constraint("c1").getBaseline())
        constraintSet.add(chain2)
        val chain3 = VChain("chain3", "style:packed, top:['a1',1,2]")
        chain3.setStyle(Chain.Style.SPREAD)
        chain3.linkToTop(Constraint("c2").getBottom(), 10)
        chain3.linkToBottom(Constraint("c3").getTop(), 25, 50)
        constraintSet.add(chain3)
        motionScene.addConstraintSet(constraintSet)
        println(motionScene)
        val exp = """
            {
            ConstraintSets:{
            start:{
            chain1:{
            type:'vChain',
            style:'spread_inside',
            },
            chain2:{
            type:'vChain',
            style:'packed',
            baseline:['c1','baseline'],
            },
            chain3:{
            type:'vChain',
            top:['c2','bottom',10],
            style:'spread',
            bottom:['c3','top',25,50],
            },
            },
            },
            }
            
        """.trimIndent()
        assertEquals(exp, motionScene.toString())
        val formattedJson: String = CLParser.parse(motionScene.toString()).toFormattedJSON()
        val formatExp = """{
  ConstraintSets: {
    start: {
      chain1: { type: 'vChain', style: 'spread_inside' },
      chain2: { type: 'vChain', style: 'packed', baseline: ['c1', 'baseline'] },
      chain3: {
        type: 'vChain',
        top: ['c2', 'bottom', 10],
        style: 'spread',
        bottom: ['c3', 'top', 25, 50]
      }
    }
  }
}"""
        assertEquals(formatExp, formattedJson)
        TransitionParser.parse(
            CLParser.parse(motionScene.toString()),
            androidx.constraintlayout.coremp.dsl.DslTest.Companion.sTransitionState,
        )
    }

    @Test
    @Throws(CLParsingException::class)
    fun testVChain03() {
        val motionScene = MotionScene()
        val constraintSet = ConstraintSet("start")
        val chain1 = VChain("chain1")
        chain1.addReference("['a1']")
        chain1.addReference(Ref("b1", 30f, 50f, 20f))
        chain1.addReference(Ref("c1"))
        chain1.addReference("['d1', 10]")
        constraintSet.add(chain1)
        val chain2 = VChain(
            "chain2",
            "contains:['a1', ['b1', 10, 15,20,,], ['c1', 25, 35  ,,,]]]",
        )
        val ref2 = Ref("ref2")
        ref2.setPreMargin(50f)
        ref2.setWeight(20f)
        chain2.addReference(ref2)
        constraintSet.add(chain2)
        val chain3 = VChain("chain3")
        val ref3 = Ref("ref3")
        ref3.setWeight(75f)
        chain3.addReference(ref3)
        constraintSet.add(chain3)
        motionScene.addConstraintSet(constraintSet)
        println(motionScene)
        val exp = """
            {
            ConstraintSets:{
            start:{
            chain1:{
            type:'vChain',
            contains:['a1',['b1',30.0,50.0,20.0],'c1',['d1',10.0],],
            },
            chain2:{
            type:'vChain',
            contains:['a1',['b1',10.0,15.0,20.0],['c1',25.0,35.0],['ref2',20.0,50.0],],
            },
            chain3:{
            type:'vChain',
            contains:[['ref3',75.0],],
            },
            },
            },
            }
            
        """.trimIndent()
        assertEquals(exp, motionScene.toString())
        val formattedJson: String = CLParser.parse(motionScene.toString()).toFormattedJSON()
        val formatExp = """{
  ConstraintSets: {
    start: {
      chain1: {
        type: 'vChain',
        contains: ['a1', ['b1', 30, 50, 20], 'c1', ['d1', 10]]
      },
      chain2: {
        type: 'vChain',
        contains: ['a1', ['b1', 10, 15, 20], ['c1', 25, 35], ['ref2', 20, 50]]
      },
      chain3: { type: 'vChain', contains: [['ref3', 75]] }
    }
  }
}"""
        assertEquals(formatExp, formattedJson)
        TransitionParser.parse(
            CLParser.parse(motionScene.toString()),
            androidx.constraintlayout.coremp.dsl.DslTest.Companion.sTransitionState,
        )
    }

    @Test
    @Throws(CLParsingException::class)
    fun testHChain01() {
        val motionScene = MotionScene()
        val constraintSet = ConstraintSet("start")
        val chain1 = HChain("chain1")
        constraintSet.add(chain1)
        val chain2 = HChain("chain2", "style:'spread'")
        constraintSet.add(chain2)
        val chain3 = HChain("chain3", "style:'packed', start:['a1',1,2]")
        constraintSet.add(chain3)
        motionScene.addConstraintSet(constraintSet)
        println(motionScene)
        val exp = """
            {
            ConstraintSets:{
            start:{
            chain1:{
            type:'hChain',
            },
            chain2:{
            type:'hChain',
            style:'spread',
            },
            chain3:{
            type:'hChain',
            start:['a1',1,2],
            style:'packed',
            },
            },
            },
            }
            
        """.trimIndent()
        assertEquals(exp, motionScene.toString())
        val formattedJson: String = CLParser.parse(motionScene.toString()).toFormattedJSON()
        val formatExp = """{
  ConstraintSets: {
    start: {
      chain1: { type: 'hChain' },
      chain2: { type: 'hChain', style: 'spread' },
      chain3: { type: 'hChain', start: ['a1', 1, 2], style: 'packed' }
    }
  }
}"""
        assertEquals(formatExp, formattedJson)
        TransitionParser.parse(
            CLParser.parse(motionScene.toString()),
            androidx.constraintlayout.coremp.dsl.DslTest.Companion.sTransitionState,
        )
    }

    @Test
    @Throws(CLParsingException::class)
    fun testHChain02() {
        val motionScene = MotionScene()
        val constraintSet = ConstraintSet("start")
        val chain1 = HChain("chain1")
        chain1.setStyle(Chain.Style.PACKED)
        chain1.linkToStart(Constraint("c1").getEnd(), 10, 25)
        chain1.linkToEnd(Constraint("c2").getStart())
        constraintSet.add(chain1)
        val chain2 = HChain("chain2", "style:'spread'")
        chain2.setStyle(Chain.Style.SPREAD_INSIDE)
        chain2.linkToLeft(Constraint("c3").getLeft(), 5)
        chain2.linkToRight(Constraint("c4").getRight(), 25, 40)
        constraintSet.add(chain2)
        val chain3 = HChain(
            "chain3",
            "style:'packed', " +
                "start:['a1',1,2], end:['b1',3,4]",
        )
        chain3.linkToStart(Constraint("a2").getStart(), 10, 20)
        chain3.linkToEnd(Constraint("b2").getEnd(), 30, 40)
        constraintSet.add(chain3)
        motionScene.addConstraintSet(constraintSet)
        println(motionScene)
        val exp = """
            {
            ConstraintSets:{
            start:{
            chain1:{
            type:'hChain',
            start:['c1','end',10,25],
            style:'packed',
            end:['c2','start'],
            },
            chain2:{
            type:'hChain',
            style:'spread_inside',
            left:['c3','left',5],
            right:['c4','right',25,40],
            },
            chain3:{
            type:'hChain',
            start:['a2','start',10,20],
            style:'packed',
            end:['b2','end',30,40],
            },
            },
            },
            }
            
        """.trimIndent()
        assertEquals(exp, motionScene.toString())
        val formattedJson: String = CLParser.parse(motionScene.toString()).toFormattedJSON()
        val formatExp = """{
  ConstraintSets: {
    start: {
      chain1: {
        type: 'hChain',
        start: ['c1', 'end', 10, 25],
        style: 'packed',
        end: ['c2', 'start']
      },
      chain2: {
        type: 'hChain',
        style: 'spread_inside',
        left: ['c3', 'left', 5],
        right: ['c4', 'right', 25, 40]
      },
      chain3: {
        type: 'hChain',
        start: ['a2', 'start', 10, 20],
        style: 'packed',
        end: ['b2', 'end', 30, 40]
      }
    }
  }
}"""
        assertEquals(formatExp, formattedJson)
        TransitionParser.parse(
            CLParser.parse(motionScene.toString()),
            androidx.constraintlayout.coremp.dsl.DslTest.Companion.sTransitionState,
        )
    }

    @Test
    @Throws(CLParsingException::class)
    fun testHChain03() {
        val motionScene = MotionScene()
        val constraintSet = ConstraintSet("start")
        val chain1 = HChain("chain1")
        chain1.addReference("['a1', 10, 20, 30]")
        chain1.addReference(Ref("b1"))
        chain1.addReference("'c1', 50")
        constraintSet.add(chain1)
        val chain2 = HChain("chain2", "contains:[['a1', 10, 15,20,,], 'a2']")
        val ref1 = Ref("a3")
        ref1.setPostMargin(100f)
        chain2.addReference(ref1)
        constraintSet.add(chain2)
        motionScene.addConstraintSet(constraintSet)
        println(motionScene)
        val exp = """
            {
            ConstraintSets:{
            start:{
            chain1:{
            type:'hChain',
            contains:[['a1',10.0,20.0,30.0],'b1',['c1',50.0],],
            },
            chain2:{
            type:'hChain',
            contains:[['a1',10.0,15.0,20.0],'a2',['a3',0.0,0.0,100.0],],
            },
            },
            },
            }
            
        """.trimIndent()
        assertEquals(exp, motionScene.toString())
        val formattedJson: String = CLParser.parse(motionScene.toString()).toFormattedJSON()
        val formatExp = """{
  ConstraintSets: {
    start: {
      chain1: { type: 'hChain', contains: [['a1', 10, 20, 30], 'b1', ['c1', 50]] },
      chain2: {
        type: 'hChain',
        contains: [['a1', 10, 15, 20], 'a2', ['a3', 0, 0, 100]]
      }
    }
  }
}"""
        assertEquals(formatExp, formattedJson)
        TransitionParser.parse(
            CLParser.parse(motionScene.toString()),
            androidx.constraintlayout.coremp.dsl.DslTest.Companion.sTransitionState,
        )
    }

    @Test
    @Throws(CLParsingException::class)
    fun testBarrier01() {
        val motionScene = MotionScene()
        val constraintSet = ConstraintSet("start")
        val barrier1 = Barrier(
            "barrier1",
            "direction:'bottom', margin:10,contains:[['a1', 1, 2 ],'b1']",
        )
        constraintSet.add(barrier1)
        motionScene.addConstraintSet(constraintSet)
        println(motionScene)
        val exp = """
            {
            ConstraintSets:{
            start:{
            barrier1:{
            type:'barrier',
            margin:10,
            contains:[['a1',1,2],'b1'],
            direction:'bottom',
            },
            },
            },
            }
            
        """.trimIndent()
        assertEquals(exp, motionScene.toString())
        val formattedJson: String = CLParser.parse(motionScene.toString()).toFormattedJSON()
        val formatExp = """{
  ConstraintSets: {
    start: {
      barrier1: {
        type: 'barrier',
        margin: 10,
        contains: [['a1', 1, 2], 'b1'],
        direction: 'bottom'
      }
    }
  }
}"""
        assertEquals(formatExp, formattedJson)
        TransitionParser.parse(
            CLParser.parse(motionScene.toString()),
            androidx.constraintlayout.coremp.dsl.DslTest.Companion.sTransitionState,
        )
    }

    @Test
    @Throws(CLParsingException::class)
    fun testBarrier02() {
        val motionScene = MotionScene()
        val constraintSet = ConstraintSet("start")
        val barrier1 = Barrier(
            "barrier1",
            "direction:bottom, margin:10,contains:[['a1', 1, 2],'b1']",
        )
        barrier1.addReference(Ref("c1", 10f, 15f))
        barrier1.setMargin(25)
        barrier1.setDirection(Constraint.Side.TOP)
        constraintSet.add(barrier1)
        val barrier2 = Barrier("barrier2")
        barrier2.addReference(Ref("a1"))
        barrier2.addReference(Ref("b1", 10f, 15f, 25f))
        barrier2.setDirection(Constraint.Side.START)
        barrier2.setMargin(15)
        constraintSet.add(barrier2)
        motionScene.addConstraintSet(constraintSet)
        println(motionScene)
        val exp = """
            {
            ConstraintSets:{
            start:{
            barrier1:{
            type:'barrier',
            margin:25,
            contains:[['a1',1.0,2.0],'b1',['c1',10.0,15.0],],
            direction:'top',
            },
            barrier2:{
            type:'barrier',
            contains:['a1',['b1',10.0,15.0,25.0],],
            margin:15,
            direction:'start',
            },
            },
            },
            }
            
        """.trimIndent()
        assertEquals(exp, motionScene.toString())
        val formattedJson: String = CLParser.parse(motionScene.toString()).toFormattedJSON()
        val formatExp = """{
  ConstraintSets: {
    start: {
      barrier1: {
        type: 'barrier',
        margin: 25,
        contains: [['a1', 1, 2], 'b1', ['c1', 10, 15]],
        direction: 'top'
      },
      barrier2: {
        type: 'barrier',
        contains: ['a1', ['b1', 10, 15, 25]],
        margin: 15,
        direction: 'start'
      }
    }
  }
}"""
        assertEquals(formatExp, formattedJson)
        TransitionParser.parse(
            CLParser.parse(motionScene.toString()),
            androidx.constraintlayout.coremp.dsl.DslTest.Companion.sTransitionState,
        )
    }

    @Test
    @Throws(CLParsingException::class)
    fun testDemo01() {
        val motionScene = MotionScene()
        motionScene.addTransition(Transition("start", "end"))
        val cs1 = ConstraintSet("start")
        val cs2 = ConstraintSet("end")
        val c1 = Constraint("id1")
        val c2 = Constraint("id1")
        c1.linkToStart(Constraint.PARENT.getStart(), 16)
        c1.linkToBottom(Constraint.PARENT.getBottom(), 16)
        c1.setWidth(40)
        c1.setHeight(40)
        cs1.add(c1)
        c2.linkToEnd(Constraint.PARENT.getEnd(), 16)
        c2.linkToTop(Constraint.PARENT.getTop(), 16)
        c2.setWidth(100)
        c2.setHeight(100)
        cs2.add(c2)
        motionScene.addConstraintSet(cs1)
        motionScene.addConstraintSet(cs2)
        println(motionScene)
        val exp = """
            {
            Transitions:{
            default:{
            from:'start',
            to:'end',
            },
            },
            ConstraintSets:{
            start:{
            id1:{
            bottom:['parent','bottom',16],
            start:['parent','start',16],
            width:40,
            height:40,
            },
            },
            end:{
            id1:{
            top:['parent','top',16],
            end:['parent','end',16],
            width:100,
            height:100,
            },
            },
            },
            }
            
        """.trimIndent()
        assertEquals(exp, motionScene.toString())
        val formattedJson: String = CLParser.parse(motionScene.toString()).toFormattedJSON()
        val formatExp = """{
  Transitions: {
    default: {
      from: 'start',
      to: 'end'
    }
  },
  ConstraintSets: {
    start: {
      id1: {
        bottom: ['parent', 'bottom', 16],
        start: ['parent', 'start', 16],
        width: 40,
        height: 40
      }
    },
    end: {
      id1: {
        top: ['parent', 'top', 16],
        end: ['parent', 'end', 16],
        width: 100,
        height: 100
      }
    }
  }
}"""
        assertEquals(formatExp, formattedJson)
        TransitionParser.parse(
            CLParser.parse(motionScene.toString()),
            androidx.constraintlayout.coremp.dsl.DslTest.Companion.sTransitionState,
        )
    }

    @Test
    @Throws(CLParsingException::class)
    fun testDemo02() {
        val motionScene = MotionScene()
        motionScene.addTransition(Transition("start", "end"))
        val cs1 = ConstraintSet("start")
        val cs2 = ConstraintSet("end")
        val c1 = Constraint("id1")
        val c2 = Constraint("id2")
        val c3 = Constraint("id3")
        val chain1 = HChain(
            "chain1",
            "contains:['id1','id2','id3'], " +
                "start:['parent', 'start', 10], end:['parent', 'end', 10]",
        )
        c1.setWidth(40)
        c1.setHeight(40)
        c2.setWidth(40)
        c2.setHeight(40)
        c3.setWidth(40)
        c3.setHeight(40)
        cs1.add(c1)
        cs1.add(c2)
        cs1.add(c3)
        cs1.add(chain1)
        val c4 = Constraint("id1")
        val c5 = Constraint("id2")
        val c6 = Constraint("id3")
        val chain2 = VChain(
            "chain2",
            "contains:['id1','id2','id3'], " +
                "top:['parent', 'top', 10], bottom:['parent', 'bottom', 10]",
        )
        c4.setWidth(50)
        c4.setHeight(50)
        c5.setWidth(60)
        c5.setHeight(60)
        c6.setWidth(70)
        c6.setHeight(70)
        cs2.add(c4)
        cs2.add(c5)
        cs2.add(c6)
        cs2.add(chain2)
        motionScene.addConstraintSet(cs1)
        motionScene.addConstraintSet(cs2)
        println(motionScene)
        val exp = """
            {
            Transitions:{
            default:{
            from:'start',
            to:'end',
            },
            },
            ConstraintSets:{
            start:{
            id1:{
            width:40,
            height:40,
            },
            id2:{
            width:40,
            height:40,
            },
            id3:{
            width:40,
            height:40,
            },
            chain1:{
            type:'hChain',
            contains:['id1','id2','id3'],
            start:['parent','start',10],
            end:['parent','end',10],
            },
            },
            end:{
            id1:{
            width:50,
            height:50,
            },
            id2:{
            width:60,
            height:60,
            },
            id3:{
            width:70,
            height:70,
            },
            chain2:{
            type:'vChain',
            contains:['id1','id2','id3'],
            top:['parent','top',10],
            bottom:['parent','bottom',10],
            },
            },
            },
            }
            
        """.trimIndent()
        assertEquals(exp, motionScene.toString())
        val formattedJson: String = CLParser.parse(motionScene.toString()).toFormattedJSON()
        val formatExp = """{
  Transitions: {
    default: {
      from: 'start',
      to: 'end'
    }
  },
  ConstraintSets: {
    start: {
      id1: { width: 40, height: 40 },
      id2: { width: 40, height: 40 },
      id3: { width: 40, height: 40 },
      chain1: {
        type: 'hChain',
        contains: ['id1', 'id2', 'id3'],
        start: ['parent', 'start', 10],
        end: ['parent', 'end', 10]
      }
    },
    end: {
      id1: { width: 50, height: 50 },
      id2: { width: 60, height: 60 },
      id3: { width: 70, height: 70 },
      chain2: {
        type: 'vChain',
        contains: ['id1', 'id2', 'id3'],
        top: ['parent', 'top', 10],
        bottom: ['parent', 'bottom', 10]
      }
    }
  }
}"""
        assertEquals(formatExp, formattedJson)
        TransitionParser.parse(
            CLParser.parse(motionScene.toString()),
            androidx.constraintlayout.coremp.dsl.DslTest.Companion.sTransitionState,
        )
    }

    companion object {
        //  test structures
        var sDpToPx: CorePixelDp = CorePixelDp { dp: Float -> dp }
        var sTransitionState = androidx.constraintlayout.coremp.state.Transition(androidx.constraintlayout.coremp.dsl.DslTest.Companion.sDpToPx)
    }
}
