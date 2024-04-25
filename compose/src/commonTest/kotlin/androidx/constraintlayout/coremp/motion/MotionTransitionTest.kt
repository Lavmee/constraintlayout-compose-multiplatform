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
package androidx.constraintlayout.coremp.motion

import androidx.constraintlayout.coremp.parser.CLObject
import androidx.constraintlayout.coremp.parser.CLParser
import androidx.constraintlayout.coremp.parser.CLParsingException
import androidx.constraintlayout.coremp.state.CorePixelDp
import androidx.constraintlayout.coremp.state.Transition
import androidx.constraintlayout.coremp.state.TransitionParser
import androidx.constraintlayout.coremp.state.WidgetFrame
import androidx.constraintlayout.coremp.widgets.ConstraintAnchor
import androidx.constraintlayout.coremp.widgets.ConstraintWidget
import androidx.constraintlayout.coremp.widgets.ConstraintWidgetContainer
import kotlin.math.max
import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MotionTransitionTest {
    fun makeLayout1(): ConstraintWidgetContainer {
        val root = ConstraintWidgetContainer(1000, 1000)
        root.debugName = "root"
        root.stringId = "root"
        val button0 = ConstraintWidget(200, 20)
        button0.debugName = "button0"
        button0.stringId = "button0"
        val button1 = ConstraintWidget(200, 20)
        button1.debugName = "button1"
        button1.stringId = "button1"
        button0.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        button0.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        button1.connect(ConstraintAnchor.Type.LEFT, button0, ConstraintAnchor.Type.LEFT)
        button1.connect(ConstraintAnchor.Type.TOP, button0, ConstraintAnchor.Type.BOTTOM)
        button1.connect(ConstraintAnchor.Type.RIGHT, button0, ConstraintAnchor.Type.RIGHT)
        root.add(button0)
        root.add(button1)
        root.layout()
        return root
    }

    fun makeLayout2(): ConstraintWidgetContainer {
        val root = ConstraintWidgetContainer(1000, 1000)
        root.debugName = "root"
        val button0 = ConstraintWidget(200, 20)
        button0.debugName = "button0"
        button0.stringId = "button0"
        val button1 = ConstraintWidget(20, 200)
        button1.debugName = "button1"
        button1.stringId = "button1"
        button0.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        button0.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        button1.connect(ConstraintAnchor.Type.LEFT, button0, ConstraintAnchor.Type.LEFT)
        button1.connect(ConstraintAnchor.Type.BOTTOM, button0, ConstraintAnchor.Type.TOP)
        button1.connect(ConstraintAnchor.Type.RIGHT, button0, ConstraintAnchor.Type.RIGHT)
        root.add(button0)
        root.add(button1)
        root.layout()
        return root
    }

    fun makeLayout(w1: Int, h1: Int, w2: Int, h2: Int): ConstraintWidgetContainer {
        val root = ConstraintWidgetContainer(1000, 1000)
        root.debugName = "root"
        root.stringId = "root"
        val button0 = ConstraintWidget(w1, h1)
        button0.debugName = "button0"
        button0.stringId = "button0"
        val button1 = ConstraintWidget(w2, h2)
        button1.debugName = "button1"
        button1.stringId = "button1"
        button0.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        button0.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        button1.connect(ConstraintAnchor.Type.LEFT, button0, ConstraintAnchor.Type.RIGHT)
        button1.connect(ConstraintAnchor.Type.TOP, button0, ConstraintAnchor.Type.BOTTOM)
        root.add(button0)
        root.add(button1)
        root.layout()
        return root
    }

    @Test
    fun testTransition() {
        val transition = Transition(
            CorePixelDp { dp: Float -> dp },
        )
        val cwc1: ConstraintWidgetContainer = makeLayout1()
        val cwc2: ConstraintWidgetContainer = makeLayout2()
        for (child in cwc1.children) {
            val wf: WidgetFrame = transition.getStart(child)
            wf.widget = child
        }
        transition.updateFrom(cwc1, Transition.START)
        for (child in cwc2.children) {
            val wf: WidgetFrame = transition.getEnd(child)
            wf.widget = child
        }
        transition.updateFrom(cwc2, Transition.END)
        transition.interpolate(cwc1.width, cwc1.height, 0.5f)
        val s1: WidgetFrame = transition.getStart("button1")
        val e1: WidgetFrame = transition.getEnd("button1")
        val f1: WidgetFrame = transition.getInterpolated("button1")
        assertNotNull(f1)
        assertEquals(20, s1.top)
        assertEquals(0, s1.left)
        assertEquals(780, e1.top)
        assertEquals(890, e1.left)
        println(
            s1.top.toString() + " ," + s1.left + " ----  " +
                s1.widget!!.top + " ," + s1.widget!!.left,
        )
        println(
            e1.top.toString() + " ," + e1.left + " ----  " +
                e1.widget!!.top + " ," + e1.widget!!.left,
        )
        println(f1.top.toString() + " ," + f1.left)
        assertEquals(400, f1.top)
        assertEquals(445, f1.left)
        assertEquals(20, s1.bottom - s1.top)
        assertEquals(200, s1.right - s1.left)
        assertEquals(110, f1.bottom - f1.top)
        assertEquals(110, f1.right - f1.left)
        assertEquals(200, e1.bottom - e1.top)
        assertEquals(20, e1.right - e1.left)
        println(
            s1.top.toString() + " ," + s1.left + " ----  " +
                s1.widget!!.top + " ," + s1.widget!!.left,
        )
        println(
            e1.top.toString() + " ," + e1.left + " ----  " +
                e1.widget!!.top + " ," + e1.widget!!.left,
        )
        println(f1.top.toString() + " ," + f1.left)
    }

    @Test
    fun testTransitionJson() {
        val transition = Transition(
            CorePixelDp { dp: Float -> dp },
        )
        val cwc1: ConstraintWidgetContainer = makeLayout1()
        val cwc2: ConstraintWidgetContainer = makeLayout2()
        for (child in cwc1.children) {
            val wf: WidgetFrame = transition.getStart(child)
            wf.widget = child
        }
        transition.updateFrom(cwc1, Transition.START)
        for (child in cwc2.children) {
            val wf: WidgetFrame = transition.getEnd(child)
            wf.widget = child
        }
        transition.updateFrom(cwc2, Transition.END)
        val jstr = """                  default: {
                    from: 'start',   to: 'end',
                    pathMotionArc: 'startVertical',
                    onSwipe: { 
                        anchor : 'button1',
                    side: 'top',
                        direction: 'up',
                        scale: 1,
                        threshold: 10,
                        mode:  'velocity',
                        maxVelocity: 4.0,
                        maxAccel: 4.0,
                   },                          KeyFrames: {
                     KeyPositions: [
                     {
                      target: ['button1'],
                      frames: [25, 50, 75],
                      percentX: [0.2, 0.3, 0.7],
                      percentY: [0.4, 0.5, 0.7]
                      percentHeight: [0.4, 0.9, 0.7]
                     }
                     ]
                  },
                  }
"""
        try {
            val json: CLObject = CLParser.parse(jstr)
            TransitionParser.parse(json, transition)
        } catch (e: CLParsingException) {
            e.printStackTrace()
        }
        assertTrue(transition.hasOnSwipe())
        // because a drag of 76 pixels (dy) is 1/10 the distance to travel  it returns 0.1
        val progress = transition.dragToProgress(
            0.5f,
            cwc1.width,
            cwc1.height,
            10f,
            47f,
        )
        assertEquals(0.1f, progress, 0.001f)
        transition.interpolate(cwc1.width, cwc1.height, 0.5f)
        val s1: WidgetFrame = transition.getStart("button1")
        val e1: WidgetFrame = transition.getEnd("button1")
        val f1: WidgetFrame = transition.getInterpolated("button1")
        assertNotNull(f1)
        assertEquals(20, s1.top)
        assertEquals(0, s1.left)
        assertEquals(780, e1.top)
        assertEquals(890, e1.left)
        assertEquals(20, s1.bottom - s1.top)
        assertEquals(200, s1.right - s1.left)
        assertEquals(182, f1.bottom - f1.top) // changed because of keyPosition
        assertEquals(110, f1.right - f1.left)
        assertEquals(200, e1.bottom - e1.top)
        assertEquals(20, e1.right - e1.left)
        print("start  ", s1)
        print("end    ", e1)
        print("at(0.5)", f1)
        println(
            "start   =" + s1.top + " ," + s1.left + " ----  " +
                s1.widget!!.top + " ," + s1.widget!!.left,
        )
        println(
            "end     =" + e1.top + " ," + e1.left + " ----  " +
                e1.widget!!.top + " ," + e1.widget!!.left,
        )
        println("at(0.5) =" + f1.top + " ," + f1.left)
        assertEquals(409, f1.top)
        assertEquals(267, f1.left)
        println(
            s1.top.toString() + " ," + s1.left + " ----  " +
                s1.widget!!.top + " ," + s1.widget!!.left,
        )
        println(
            e1.top.toString() + " ," + e1.left + " ----  " +
                e1.widget!!.top + " ," + e1.widget!!.left,
        )
        println(f1.top.toString() + " ," + f1.left)
    }

    @Test
    @Throws(CLParsingException::class)
    fun testTransitionJson2() {
        val transition = Transition(
            CorePixelDp { dp: Float -> dp },
        )
        val cwc1: ConstraintWidgetContainer = makeLayout(100, 100, 100, 100)
        val cwc2: ConstraintWidgetContainer = makeLayout(500, 900, 100, 100)
        // button1 move down 800 and to the right 400
        for (child in cwc1.children) {
            val wf: WidgetFrame = transition.getStart(child)
            wf.widget = child
        }
        transition.updateFrom(cwc1, Transition.START)
        for (child in cwc2.children) {
            val wf: WidgetFrame = transition.getEnd(child)
            wf.widget = child
        }
        transition.updateFrom(cwc2, Transition.END)
        val jsonString = """                  default: {
                    from: 'start',   to: 'end',
                    pathMotionArc: 'startHorizontal',
                    onSwipe: { 
                        anchor :'button1',
                        side: 'top',
                        direction: 'up',
                        scale: 1,
                        threshold: 10,
                        mode:  'velocity',
                        maxVelocity: 4.0,
                        maxAccel: 4.0,
                   },                          KeyFrames: {
                     KeyPositions: [
                     {
                      target: ['button1'],
                      type: 'pathRelative'
                      frames: [25, 50, 75],
                      percentX: [0.25, 0.5, 0.75],
                      percentY: [0.0, 0.1, 0.0]
                     }
                     ]
                  },
                  }
"""
        val json: CLObject = CLParser.parse(jsonString)
        TransitionParser.parse(json, transition)
        assertTrue(transition.hasOnSwipe())
        // because a drag of 80 pixels (dy) is 1/10 the distance to travel  it returns 0.1
        val progress = transition.dragToProgress(
            0.5f,
            cwc1.width,
            cwc1.height,
            10f,
            80f,
        )
        //        assertEquals(0.1f, progress, 0.001f);
//        progress = transition.dragToProgress(0.3f, 10f, 80);
//        assertEquals(0.1f, progress, 0.001f);

        // ============================================================
        // plot drag progress for 100 pixel drag at each point on the system
        val pos = FloatArray(100)
        val ratio = FloatArray(100)
        for (i in ratio.indices) {
            pos[i] = i / (ratio.size - 1).toFloat()
            ratio[i] = transition.dragToProgress(
                pos[i],
                cwc1.width,
                cwc1.height,
                10f,
                100f,
            )
        }
        transition.interpolate(cwc1.width, cwc1.height, 0.5f)
        println(textDraw(80, 30, pos, ratio, true))

        // ============================================================
        // Simulate touch up at mid point
        val nanoTime = 123123L
        transition.setTouchUp(.5f, nanoTime, 0.5f, 0.5f)
        val deltaSec = 0.01f
        val deltaT = (deltaSec * 1E9).toLong()
        for (i in pos.indices) {
            pos[i] = deltaSec * i
            ratio[i] = transition.getTouchUpProgress(nanoTime + deltaT * i)
        }
        println(textDraw(80, 30, pos, ratio, true))
        // ============================================================
        transition.interpolate(cwc1.width, cwc1.height, 0.5f)
        val s1: WidgetFrame = transition.getStart("button1")
        val e1: WidgetFrame = transition.getEnd("button1")
        val f1: WidgetFrame = transition.getInterpolated("button1")
        assertNotNull(f1)
        assertEquals(100, s1.top)
        assertEquals(100, s1.left)
        assertEquals(900, e1.top)
        assertEquals(500, e1.left)
        assertEquals(100, s1.bottom - s1.top)
        assertEquals(100, s1.right - s1.left)
        assertEquals(100, e1.bottom - e1.top)
        assertEquals(100, e1.right - e1.left)
        val xp = FloatArray(200)
        val yp = FloatArray(xp.size)
        for (i in yp.indices) {
            val v = yp[i]
            val p = i / (yp.size - 1f)
            transition.interpolate(cwc1.width, cwc1.height, p)
            val dynamic: WidgetFrame = transition.getInterpolated("button1")
            xp[i] = ((dynamic.left + dynamic.right) / 2).toFloat()
            yp[i] = ((dynamic.top + dynamic.bottom) / 2).toFloat()
        }
        val str = textDraw(80, 30, xp, yp, false)
        println(str)
        val expect = """
            |***                                                                             | 150.0
            |  ****                                                                          |
            |      ****                                                                      |
            |         ****                                                                   |
            |            ***                                                                 |
            |               ***                                                              | 287.931
            |                 ***                                                            |
            |                   **                                                           |
            |                    **                                                          |
            |                     *                                                          |
            |                    **                                                          | 425.862
            |                    *                                                           |
            |                    *                                                           |
            |                    *                                                           |
            |                    **                                                          |
            |                     ***                                                        | 563.793
            |                       *****                                                    |
            |                            *****                                               |
            |                                 *********                                      |
            |                                         ********                               |
            |                                                 *******                        | 701.724
            |                                                        *****                   |
            |                                                            ****                |
            |                                                                ***             |
            |                                                                   ***          |
            |                                                                     ***        | 839.655
            |                                                                       ***      |
            |                                                                          ***   |
            |                                                                            *** |
            |                                                                               *| 950.0
            150.0                                                                        550.0
            
        """.trimIndent()
        assertEquals(expect, str)
    }

    @Test
    @Throws(CLParsingException::class)
    fun testTransitionOnSwipe1() {
        val onSwipeString = """                    onSwipe: { 
                        anchor :'button1',
                        side: 'top',
                        direction: 'up',
                        scale: 1,
                        threshold: 1,
                        mode:  'spring',
                   },      """
        println(onSwipeString)
        val transition = setUpOnSwipe(onSwipeString)
        val w: ConstraintWidget? = transition.getStart("button1").widget!!.parent
        println("=============== drag")
        var result: String?
        // ============================================================
        // plot drag progress for 100 pixel drag at each point on the system
        val pos = FloatArray(100)
        val ratio = FloatArray(100)
        for (i in ratio.indices) {
            pos[i] = i / (ratio.size - 1).toFloat()
            ratio[i] = transition.dragToProgress(pos[i], w!!.width, w.height, 10f, 100f)
        }
        transition.interpolate(w!!.width, w.height, 0.5f)
        println(textDraw(80, 30, pos, ratio, true).also { result = it })
        println("=============== Calculate touch progress")
        var expect = """
            |                                                  *                             | 0.173
            |                                                ** *                            |
            |                                               *    **                          |
            |                                               *      *                         |
            |                                              *                                 |
            |                                             *         *                        | 0.16
            |                                                       *                        |
            |                                            *                                   |
            |                                                        *                       |
            |                                           *             *                      |
            |                                           *                                    | 0.147
            |                                                          *                     |
            |                                          *                                     |
            |                                                           *                    |
            |                                         *                 *                    |
            |                                                            *                   | 0.134
            |                                        *                    *                  |
            |    ****                                                      **                |
            | ***    ****                           *                       **               |
            |*           **                                                   **           **|
            |              **                       *                           ****   ****  | 0.121
            |               **                                                      ***      |
            |                 **                   *                                         |
            |                   *                 *                                          |
            |                   *                *                                           |
            |                    *              *                                            | 0.108
            |                     *             *                                            |
            |                      **          *                                             |
            |                       **       **                                              |
            |                         *******                                                | 0.098
            0.0                                                                            1.0
            
        """.trimIndent()
        assertEquals(expect, result)

        // ============================================================
        // Simulate touch up at mid point
        var start = 123123L
        transition.setTouchUp(.5f, start, 0.5f, 0.5f)
        val deltaSec = 0.01f
        val deltaT = (deltaSec * 1E9).toLong()
        start += deltaT
        for (i in pos.indices) {
            pos[i] = deltaSec * i
            ratio[i] = transition.getTouchUpProgress(start + deltaT * i)
        }
        println(textDraw(80, 30, pos, ratio, true).also { result = it })
        println("=============== 3")
        expect = """
            |           *                                                                    | 1.222
            |          ****                                                                  |
            |              *                                                                 |
            |         *     *                                                                |
            |        *      *                                                                |
            |                *                                                               | 1.099
            |       *         *                                                              |
            |                                                                                |
            |       *          *                *******                                      |
            |                   *            ***       ***              *                    |
            |                   *           *             ************** ********************| 0.976
            |      *             *         **                                                |
            |                     *      **                                                  |
            |     *                **  **                                                    |
            |                        **                                                      |
            |                                                                                | 0.854
            |    *                                                                           |
            |                                                                                |
            |                                                                                |
            |   *                                                                            |
            |                                                                                | 0.731
            |                                                                                |
            |   *                                                                            |
            |                                                                                |
            |  *                                                                             |
            |                                                                                | 0.608
            |                                                                                |
            | *                                                                              |
            |*                                                                               |
            |*                                                                               | 0.51
            0.0                                                                           0.99
            
        """.trimIndent()
        assertEquals(expect, result)
    }

    @Test
    fun testTransitionOnSwipeRemoved() {
        // Base setup
        val transition = Transition(
            CorePixelDp { dp: Float -> dp },
        )
        val cwc1: ConstraintWidgetContainer = makeLayout1()
        val cwc2: ConstraintWidgetContainer = makeLayout2()
        for (child in cwc1.children) {
            val wf: WidgetFrame = transition.getStart(child)
            wf.widget = child
        }
        transition.updateFrom(cwc1, Transition.START)
        for (child in cwc2.children) {
            val wf: WidgetFrame = transition.getEnd(child)
            wf.widget = child
        }
        transition.updateFrom(cwc2, Transition.END)

        // Load the Transition with the given Json
        val jsonString0 = """                  default: {
                    from: 'start',   to: 'end',
                    pathMotionArc: 'startHorizontal',
                    onSwipe: { 
                        anchor :'button1',
                        side: 'top',
                        direction: 'up',
                        scale: 1,
                        threshold: 10,
                        mode:  'velocity',
                        maxVelocity: 4.0,
                        maxAccel: 4.0,
                   },                          KeyFrames: {
                     KeyPositions: [
                     {
                      target: ['button1'],
                      type: 'pathRelative'
                      frames: [25, 50, 75],
                      percentX: [0.25, 0.5, 0.75],
                      percentY: [0.0, 0.1, 0.0]
                     }
                     ]
                  },
                  }
"""
        try {
            val json: CLObject = CLParser.parse(jsonString0)
            TransitionParser.parse(json, transition)
        } catch (e: CLParsingException) {
            e.printStackTrace()
        }
        assertTrue(transition.hasOnSwipe())

        // Load the Transition with a new Json (removing onSwipe)
        val jsonString1 = """                  default: {
                    from: 'start',   to: 'end',
                    pathMotionArc: 'startHorizontal',
                    KeyFrames: {
                     KeyPositions: [
                     {
                      target: ['button1'],
                      type: 'pathRelative'
                      frames: [25, 50, 75],
                      percentX: [0.25, 0.5, 0.75],
                      percentY: [0.0, 0.1, 0.0]
                     }
                     ]
                  },
                  }
"""
        try {
            val json: CLObject = CLParser.parse(jsonString1)
            TransitionParser.parse(json, transition)
        } catch (e: CLParsingException) {
            e.printStackTrace()
        }

        // Verify that onSwipe functionality is lost
        assertFalse(transition.hasOnSwipe())
    }

    @Throws(CLParsingException::class)
    fun setUpOnSwipe(onSwipeString: String): Transition {
        val transition = Transition(
            CorePixelDp { dp: Float -> dp },
        )
        val cwc1: ConstraintWidgetContainer = makeLayout(100, 100, 100, 100)
        val cwc2: ConstraintWidgetContainer = makeLayout(500, 900, 100, 100)
        // button1 move down 800 and to the right 400
        for (child in cwc1.children) {
            val wf: WidgetFrame = transition.getStart(child)
            wf.widget = child
        }
        transition.updateFrom(cwc1, Transition.START)
        for (child in cwc2.children) {
            val wf: WidgetFrame = transition.getEnd(child)
            wf.widget = child
        }
        transition.updateFrom(cwc2, Transition.END)
        val jsonString = """                  default: {
                    from: 'start',   to: 'end',
                    pathMotionArc: 'startHorizontal',
$onSwipeString                    KeyFrames: {
                     KeyPositions: [
                     {
                      target: ['button1'],
                      type: 'pathRelative'
                      frames: [25, 50, 75],
                      percentX: [0.25, 0.5, 0.75],
                      percentY: [0.0, 0.1, 0.0]
                     }
                     ]
                  },
                  }
"""
        val json: CLObject = CLParser.parse(jsonString)
        TransitionParser.parse(json, transition)
        return transition
    }

    fun textDraw(dimx: Int, dimy: Int, x: FloatArray, y: FloatArray, flip: Boolean): String {
        var minX = x[0]
        var maxX = x[0]
        var minY = y[0]
        var maxY = y[0]
        var ret = ""
        for (i in x.indices) {
            minX = min(minX.toDouble(), x[i].toDouble()).toFloat()
            maxX = max(maxX.toDouble(), x[i].toDouble()).toFloat()
            minY = min(minY.toDouble(), y[i].toDouble()).toFloat()
            maxY = max(maxY.toDouble(), y[i].toDouble()).toFloat()
        }
        val c = Array(dimy) { CharArray(dimx) }
        for (i in 0 until dimy) {
            c[i].fill(' ')
        }
        val dimx1 = dimx - 1
        val dimy1 = dimy - 1
        for (j in x.indices) {
            val xp = (dimx1 * (x[j] - minX) / (maxX - minX)).toInt()
            val yp = (dimy1 * (y[j] - minY) / (maxY - minY)).toInt()
            c[if (flip) dimy - yp - 1 else yp][xp] = '*'
        }
        for (i in c.indices) {
            var v: Float
            v = if (flip) {
                (minY - maxY) * (i / (c.size - 1.0f)) + maxY
            } else {
                (maxY - minY) * (i / (c.size - 1.0f)) + minY
            }
            v = (v * 1000 + 0.5).toInt() / 1000f
            ret += if (i % 5 == 0 || i == c.size - 1) {
                "|" + c[i].concatToString() + "| " + v + "\n"
            } else {
                "|" + c[i].concatToString() + "|\n"
            }
        }
        val minStr = ((minX * 1000 + 0.5).toInt() / 1000f).toString()
        val maxStr = ((maxX * 1000 + 0.5).toInt() / 1000f).toString()
        var s = minStr + CharArray(dimx).concatToString().replace('\u0000', ' ')
        s = s.substring(0, dimx - maxStr.length + 2) + maxStr + "\n"
        return ret + s
    }

    fun print(name: String, f: WidgetFrame) {
        println(
            name + " " + fix(f.left) + "," +
                fix(f.top) + "," + fix(f.bottom) + "," + fix(f.right),
        )
    }

    fun fix(p: Int): String {
        val str = "     $p"
        return str.substring(str.length - 4)
    }
}
