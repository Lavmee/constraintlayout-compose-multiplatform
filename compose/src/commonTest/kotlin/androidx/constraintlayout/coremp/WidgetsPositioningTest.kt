/*
 * Copyright (C) 2015 The Android Open Source Project
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
package androidx.constraintlayout.coremp

import androidx.constraintlayout.coremp.widgets.ConstraintAnchor
import androidx.constraintlayout.coremp.widgets.ConstraintWidget
import androidx.constraintlayout.coremp.widgets.ConstraintWidgetContainer
import androidx.constraintlayout.coremp.widgets.Guideline
import androidx.constraintlayout.coremp.widgets.Optimizer
import kotlinx.coroutines.Runnable
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class WidgetsPositioningTest {
    var mLS = LinearSystem()
    var mOptimize = false

    @BeforeTest
    fun setUp() {
        mLS = LinearSystem()
        LinearEquation.resetNaming()
    }

    @Test
    fun testCentering() {
        val root = ConstraintWidgetContainer(0, 0, 600, 600)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(20, 100)
        val c = ConstraintWidget(100, 20)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 200)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.TOP, 0)
        b.connect(ConstraintAnchor.Type.BOTTOM, a, ConstraintAnchor.Type.BOTTOM, 0)
        c.connect(ConstraintAnchor.Type.TOP, b, ConstraintAnchor.Type.TOP, 0)
        c.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.BOTTOM, 0)
        root.add(a)
        root.add(b)
        root.add(c)
        root.layout()
        println("A: $a B: $b C: $c")
    }

    @Test
    fun testDimensionRatio() {
        val a = ConstraintWidget(0, 0, 600, 600)
        val b = ConstraintWidget(100, 100)
        val widgets: ArrayList<ConstraintWidget> = ArrayList()
        widgets.add(a)
        widgets.add(b)
        val margin = 10
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.LEFT, margin)
        b.connect(ConstraintAnchor.Type.RIGHT, a, ConstraintAnchor.Type.RIGHT, margin)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.TOP, margin)
        b.connect(ConstraintAnchor.Type.BOTTOM, a, ConstraintAnchor.Type.BOTTOM, margin)
        b.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        b.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        a.debugName = "A"
        b.debugName = "B"
        val ratio = 0.3f
        // First, let's check vertical ratio
        b.setDimensionRatio(ratio, ConstraintWidget.VERTICAL)
        runTestOnWidgets(
            widgets,
            object : Runnable {
                override fun run() {
                    println("a) A: $a B: $b")
                    assertEquals(b.width, a.width - 2 * margin)
                    assertEquals(b.height, (ratio * b.width).toInt())
                    assertEquals(b.top - a.top, ((a.height - b.height) / 2))
                    assertEquals(
                        a.bottom - b.bottom,
                        ((a.height - b.height) / 2),
                    )
                    assertEquals(b.top - a.top, a.bottom - b.bottom)
                }
            },
        )
        b.verticalBiasPercent = 1f
        runTestOnWidgets(
            widgets,
            object : Runnable {
                override fun run() {
                    println("b) A: $a B: $b")
                    assertEquals(b.width, a.width - 2 * margin)
                    assertEquals(b.height, (ratio * b.width).toInt())
                    assertEquals(b.top, a.height - b.height - margin)
                    assertEquals(a.bottom, b.bottom + margin)
                }
            },
        )
        b.verticalBiasPercent = 0f
        runTestOnWidgets(
            widgets,
            object : Runnable {
                override fun run() {
                    println("c) A: $a B: $b")
                    assertEquals(b.width, a.width - 2 * margin)
                    assertEquals(b.height, (ratio * b.width).toInt())
                    assertEquals(b.top, a.top + margin)
                    assertEquals(b.bottom, a.top + b.height + margin)
                }
            },
        )
        // Then, let's check horizontal ratio
        b.setDimensionRatio(ratio, ConstraintWidget.HORIZONTAL)
        runTestOnWidgets(
            widgets,
            object : Runnable {
                override fun run() {
                    println("d) A: $a B: $b")
                    assertEquals(b.height, a.height - 2 * margin)
                    assertEquals(b.width, (ratio * b.height).toInt())
                    assertEquals(b.left - a.left, ((a.width - b.width) / 2))
                    assertEquals(
                        a.right - b.right,
                        ((a.width - b.width) / 2),
                    )
                }
            },
        )
        b.horizontalBiasPercent = 1f
        runTestOnWidgets(
            widgets,
            object : Runnable {
                override fun run() {
                    println("e) A: $a B: $b")
                    assertEquals(b.height, a.height - 2 * margin)
                    assertEquals(b.width, (ratio * b.height).toInt())
                    assertEquals(b.right, a.right - margin)
                    assertEquals(b.left, a.right - b.width - margin)
                }
            },
        )
        b.horizontalBiasPercent = 0f
        runTestOnWidgets(
            widgets,
            object : Runnable {
                override fun run() {
                    println("f) A: $a B: $b")
                    assertEquals(b.height, a.height - 2 * margin)
                    assertEquals(b.width, (ratio * b.height).toInt())
                    assertEquals(b.right, a.left + margin + b.width)
                    assertEquals(b.left, a.left + margin)
                }
            },
        )
    }

    @Test
    fun testCreateManyVariables() {
        val rootWidget = ConstraintWidgetContainer(0, 0, 600, 400)
        val previous = ConstraintWidget(0, 0, 100, 20)
        rootWidget.add(previous)
        for (i in 0..99) {
            val w = ConstraintWidget(0, 0, 100, 20)
            w.connect(ConstraintAnchor.Type.LEFT, previous, ConstraintAnchor.Type.RIGHT, 20)
            w.connect(ConstraintAnchor.Type.RIGHT, rootWidget, ConstraintAnchor.Type.RIGHT, 20)
            rootWidget.add(w)
        }
        rootWidget.layout()
    }

    @Test
    fun testWidgetCenterPositioning() {
        val x = 20
        val y = 30
        val rootWidget = ConstraintWidget(x, y, 600, 400)
        val centeredWidget = ConstraintWidget(100, 20)
        val widgets: ArrayList<ConstraintWidget> = ArrayList<ConstraintWidget>()
        centeredWidget.resetSolverVariables(mLS.getCache())
        rootWidget.resetSolverVariables(mLS.getCache())
        widgets.add(centeredWidget)
        widgets.add(rootWidget)
        centeredWidget.debugName = "A"
        rootWidget.debugName = "Root"
        centeredWidget.connect(
            ConstraintAnchor.Type.CENTER_X,
            rootWidget,
            ConstraintAnchor.Type.CENTER_X,
        )
        centeredWidget.connect(
            ConstraintAnchor.Type.CENTER_Y,
            rootWidget,
            ConstraintAnchor.Type.CENTER_Y,
        )
        runTestOnWidgets(
            widgets,
            object : Runnable {
                override fun run() {
                    println(
                        """
 *** rootWidget: $rootWidget centeredWidget: $centeredWidget""",
                    )
                    val left: Int = centeredWidget.left
                    val top: Int = centeredWidget.top
                    val right: Int = centeredWidget.right
                    val bottom: Int = centeredWidget.bottom
                    assertEquals(left, x + 250)
                    assertEquals(right, x + 350)
                    assertEquals(top, y + 190)
                    assertEquals(bottom, y + 210)
                }
            },
        )
    }

    @Test
    fun testBaselinePositioning() {
        val a = ConstraintWidget(20, 230, 200, 70)
        val b = ConstraintWidget(200, 60, 200, 100)
        val widgets: ArrayList<ConstraintWidget> = ArrayList()
        widgets.add(a)
        widgets.add(b)
        a.debugName = "A"
        b.debugName = "B"
        a.baselineDistance = 40
        b.baselineDistance = 60
        b.connect(ConstraintAnchor.Type.BASELINE, a, ConstraintAnchor.Type.BASELINE)
        val root = ConstraintWidgetContainer(0, 0, 1000, 1000)
        root.debugName = "root"
        root.add(a)
        root.add(b)
        root.layout()
        assertEquals(
            b.top + b.baselineDistance,
            a.top + a.baselineDistance,
        )
        runTestOnWidgets(
            widgets,
            object : Runnable {
                override fun run() {
                    assertEquals(
                        b.top + b.baselineDistance,
                        a.top + a.baselineDistance,
                    )
                }
            },
        )
    }

    // @Test
    fun testAddingWidgets() {
        val root = ConstraintWidgetContainer(0, 0, 1000, 1000)
        root.optimizationLevel = Optimizer.OPTIMIZATION_NONE
        val widgetsA: ArrayList<ConstraintWidget> =
            ArrayList<ConstraintWidget>()
        val widgetsB: ArrayList<ConstraintWidget> =
            ArrayList<ConstraintWidget>()
        for (i in 0..999) {
            val a = ConstraintWidget(0, 0, 200, 20)
            val b = ConstraintWidget(0, 0, 200, 20)
            a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
            a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
            a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
            b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
            b.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
            b.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
            widgetsA.add(a)
            widgetsB.add(b)
            root.add(a)
            root.add(b)
        }
        root.layout()
        for (widget in widgetsA) {
            assertEquals(widget.left, 200)
            assertEquals(widget.top, 0)
        }
        for (widget in widgetsB) {
            assertEquals(widget.left, 600)
            assertEquals(widget.top, 980)
        }
    }

    @Test
    fun testWidgetTopRightPositioning() {
        // Easy to tweak numbers to test larger systems
        val numLoops = 10
        val numWidgets = 100
        for (j in 0 until numLoops) {
            mLS.reset()
            val widgets: ArrayList<ConstraintWidget> =
                ArrayList()
            val w = 100 + j
            val h = 20 + j
            val first = ConstraintWidget(w, h)
            widgets.add(first)
            var previous: ConstraintWidget = first
            val margin = 20
            for (i in 0 until numWidgets) {
                val widget = ConstraintWidget(w, h)
                widget.connect(
                    ConstraintAnchor.Type.LEFT,
                    previous,
                    ConstraintAnchor.Type.RIGHT,
                    margin,
                )
                widget.connect(
                    ConstraintAnchor.Type.TOP,
                    previous,
                    ConstraintAnchor.Type.BOTTOM,
                    margin,
                )
                widgets.add(widget)
                previous = widget
            }
            for (widget in widgets) {
                widget.addToSolver(mLS, mOptimize)
            }
            try {
                mLS.minimize()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            for (i in widgets.indices) {
                val widget: ConstraintWidget = widgets.get(i)
                widget.updateFromSolver(mLS, mOptimize)
                val left: Int = widget.left
                val top: Int = widget.top
                val right: Int = widget.right
                val bottom: Int = widget.bottom
                assertEquals(left, i * (w + margin))
                assertEquals(right, i * (w + margin) + w)
                assertEquals(top, i * (h + margin))
                assertEquals(bottom, i * (h + margin) + h)
            }
        }
    }

    @Test
    fun testWrapSimpleWrapContent() {
        val root = ConstraintWidgetContainer(0, 0, 1000, 1000)
        val a = ConstraintWidget(0, 0, 200, 20)
        val widgets: ArrayList<ConstraintWidget> = ArrayList()
        widgets.add(root)
        widgets.add(a)
        root.setDebugSolverName(mLS, "root")
        a.setDebugSolverName(mLS, "A")
        root.add(a)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        runTestOnWidgets(
            widgets,
            object : Runnable {
                override fun run() {
                    println("Simple Wrap: $root, $a")
                    assertEquals(root.width, a.width)
                    assertEquals(root.height, a.height)
                    assertEquals(a.width, 200)
                    assertEquals(a.height, 20)
                }
            },
        )
    }

    @Test
    fun testMatchConstraint() {
        val root = ConstraintWidgetContainer(50, 50, 500, 500)
        val a = ConstraintWidget(10, 20, 100, 30)
        val b = ConstraintWidget(150, 200, 100, 30)
        val c = ConstraintWidget(50, 50)
        val widgets: ArrayList<ConstraintWidget> = ArrayList()
        a.debugName = "A"
        b.debugName = "B"
        c.debugName = "C"
        root.debugName = "root"
        root.add(a)
        root.add(b)
        root.add(c)
        widgets.add(root)
        widgets.add(a)
        widgets.add(b)
        widgets.add(c)
        c.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        c.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT)
        c.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        c.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        c.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        c.connect(ConstraintAnchor.Type.BOTTOM, b, ConstraintAnchor.Type.TOP)
        runTestOnWidgets(
            widgets,
            object : Runnable {
                override fun run() {
                    assertEquals(c.x, a.right)
                    assertEquals(c.right, b.x)
                    assertEquals(c.y, a.bottom)
                    assertEquals(c.bottom, b.y)
                }
            },
        )
    }

    // Obsolete @Test
    fun testWidgetStrengthPositioning() {
        val root = ConstraintWidget(400, 400)
        val a = ConstraintWidget(20, 20)
        val widgets: ArrayList<ConstraintWidget> = ArrayList()
        widgets.add(root)
        widgets.add(a)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        println("Widget A centered inside Root")
        runTestOnWidgets(
            widgets,
            object : Runnable {
                override fun run() {
                    assertEquals(a.left, 190)
                    assertEquals(a.right, 210)
                    assertEquals(a.top, 190)
                    assertEquals(a.bottom, 210)
                }
            },
        )
        println("Widget A weak left, should move to the right")
        a.getAnchor(ConstraintAnchor.Type.LEFT) // .setStrength(ConstraintAnchor.Strength.WEAK);
        runTestOnWidgets(
            widgets,
            object : Runnable {
                override fun run() {
                    assertEquals(a.left, 380)
                    assertEquals(a.right, 400)
                }
            },
        )
        println("Widget A weak right, should go back to center")
        a.getAnchor(ConstraintAnchor.Type.RIGHT) // .setStrength(ConstraintAnchor.Strength.WEAK);
        runTestOnWidgets(
            widgets,
            object : Runnable {
                override fun run() {
                    assertEquals(a.left, 190)
                    assertEquals(a.right, 210)
                }
            },
        )
        println("Widget A strong left, should move to the left")
        a.getAnchor(ConstraintAnchor.Type.LEFT) // .setStrength(ConstraintAnchor.Strength.STRONG);
        runTestOnWidgets(
            widgets,
            object : Runnable {
                override fun run() {
                    assertEquals(a.left, 0)
                    assertEquals(a.right, 20)
                    assertEquals(root.width, 400)
                }
            },
        )
    }

    @Test
    fun testWidgetPositionMove() {
        val a = ConstraintWidget(0, 0, 100, 20)
        val b = ConstraintWidget(0, 30, 200, 20)
        val c = ConstraintWidget(0, 60, 100, 20)
        val widgets: ArrayList<ConstraintWidget> = ArrayList<ConstraintWidget>()
        widgets.add(a)
        widgets.add(b)
        widgets.add(c)
        a.setDebugSolverName(mLS, "A")
        b.setDebugSolverName(mLS, "B")
        c.setDebugSolverName(mLS, "C")
        b.connect(ConstraintAnchor.Type.LEFT, a, ConstraintAnchor.Type.RIGHT)
        c.setOrigin(200, 0)
        b.connect(ConstraintAnchor.Type.RIGHT, c, ConstraintAnchor.Type.RIGHT)
        val check: Runnable = object : Runnable {
            override fun run() {
                assertEquals(a.width, 100)
                assertEquals(b.width, 200)
                assertEquals(c.width, 100)
            }
        }
        runTestOnWidgets(widgets, check)
        println("A: $a B: $b C: $c")
        c.setOrigin(100, 0)
        //        runTestOnUIWidgets(widgets);
        runTestOnWidgets(widgets, check)
        println("A: $a B: $b C: $c")
        c.setOrigin(50, 0)
        runTestOnWidgets(widgets, check)
        println("A: $a B: $b C: $c")
    }

    @Test
    fun testWrapProblem() {
        val root = ConstraintWidgetContainer(400, 400)
        val a = ConstraintWidget(80, 300)
        val b = ConstraintWidget(250, 80)
        val widgets: ArrayList<ConstraintWidget> = ArrayList()
        widgets.add(root)
        widgets.add(b)
        widgets.add(a)
        a.parent = root
        b.parent = root
        root.setDebugSolverName(mLS, "root")
        a.setDebugSolverName(mLS, "A")
        b.setDebugSolverName(mLS, "B")
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        b.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        //        B.getAnchor(ConstraintAnchor.Type.TOP).setStrength(ConstraintAnchor.Strength.WEAK);
        runTestOnWidgets(
            widgets,
            object : Runnable {
                override fun run() {
                    assertEquals(a.width, 80)
                    assertEquals(a.height, 300)
                    assertEquals(b.width, 250)
                    assertEquals(b.height, 80)
                    assertEquals(a.y, 0)
                    assertEquals(b.y, 110)
                }
            },
        )
    }

    @Test
    fun testGuideline() {
        val root = ConstraintWidgetContainer(400, 400)
        val a = ConstraintWidget(100, 20)
        val guideline = Guideline()
        root.add(guideline)
        root.add(a)
        guideline.setGuidePercent(0.50f)
        guideline.setOrientation(Guideline.VERTICAL)
        root.debugName = "root"
        a.debugName = "A"
        guideline.debugName = "guideline"
        val widgets: ArrayList<ConstraintWidget> = ArrayList<ConstraintWidget>()
        widgets.add(root)
        widgets.add(a)
        widgets.add(guideline)
        a.connect(ConstraintAnchor.Type.LEFT, guideline, ConstraintAnchor.Type.LEFT)
        val check: Runnable = object : Runnable {
            override fun run() {
                println("$root $a $guideline")
                assertEquals(a.width, 100)
                assertEquals(a.height, 20)
                assertEquals(a.x, 200)
            }
        }
        runTest(root, check)
        guideline.setGuidePercent(0)
        runTest(
            root,
            object : Runnable {
                override fun run() {
                    println("$root $a $guideline")
                    assertEquals(a.width, 100)
                    assertEquals(a.height, 20)
                    assertEquals(a.x, 0)
                }
            },
        )
        guideline.setGuideBegin(150)
        runTest(
            root,
            object : Runnable {
                override fun run() {
                    assertEquals(a.width, 100)
                    assertEquals(a.height, 20)
                    assertEquals(a.x, 150)
                }
            },
        )
        println("$root $a $guideline")
        guideline.setGuideEnd(150)
        runTest(
            root,
            object : Runnable {
                override fun run() {
                    assertEquals(a.width, 100)
                    assertEquals(a.height, 20)
                    assertEquals(a.x, 250)
                }
            },
        )
        println("$root $a $guideline")
        guideline.setOrientation(Guideline.HORIZONTAL)
        a.resetAnchors()
        a.connect(ConstraintAnchor.Type.TOP, guideline, ConstraintAnchor.Type.TOP)
        guideline.setGuideBegin(150)
        runTest(
            root,
            object : Runnable {
                override fun run() {
                    println("$root $a $guideline")
                    assertEquals(a.width, 100)
                    assertEquals(a.height, 20)
                    assertEquals(a.y, 150)
                }
            },
        )
        println("$root $a $guideline")
        a.resetAnchors()
        a.connect(ConstraintAnchor.Type.TOP, guideline, ConstraintAnchor.Type.BOTTOM)
        runTest(
            root,
            object : Runnable {
                override fun run() {
                    assertEquals(a.width, 100)
                    assertEquals(a.height, 20)
                    assertEquals(a.y, 150)
                }
            },
        )
        println("$root $a $guideline")
    }

    private fun runTest(root: ConstraintWidgetContainer, check: Runnable) {
        root.layout()
        check.run()
    }

    @Test
    fun testGuidelinePosition() {
        val root = ConstraintWidgetContainer(800, 400)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val guideline = Guideline()
        root.add(guideline)
        root.add(a)
        root.add(b)
        guideline.setGuidePercent(0.651f)
        guideline.setOrientation(Guideline.VERTICAL)
        root.setDebugSolverName(mLS, "root")
        a.setDebugSolverName(mLS, "A")
        b.setDebugSolverName(mLS, "B")
        guideline.setDebugSolverName(mLS, "guideline")
        val widgets: ArrayList<ConstraintWidget> = ArrayList<ConstraintWidget>()
        widgets.add(root)
        widgets.add(a)
        widgets.add(b)
        widgets.add(guideline)
        a.connect(ConstraintAnchor.Type.LEFT, guideline, ConstraintAnchor.Type.RIGHT)
        b.connect(ConstraintAnchor.Type.RIGHT, guideline, ConstraintAnchor.Type.RIGHT)
        val check: Runnable = object : Runnable {
            override fun run() {
                println(
                    "" + root + " A: " + a + " " +
                        " B: " + b + " guideline: " + guideline,
                )
                assertEquals(a.width, 100)
                assertEquals(a.height, 20)
                assertEquals(a.x, 521)
                assertEquals(b.right, 521)
            }
        }
        runTestOnWidgets(widgets, check)
    }

    @Test
    fun testWidgetInfeasiblePosition() {
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        val widgets: ArrayList<ConstraintWidget> = ArrayList<ConstraintWidget>()
        widgets.add(b)
        widgets.add(a)
        a.resetSolverVariables(mLS.getCache())
        b.resetSolverVariables(mLS.getCache())
        a.connect(ConstraintAnchor.Type.RIGHT, b, ConstraintAnchor.Type.LEFT)
        b.connect(ConstraintAnchor.Type.RIGHT, a, ConstraintAnchor.Type.LEFT)
        runTestOnWidgets(
            widgets,
            object : Runnable {
                override fun run() {
                    // TODO: this fail -- need to figure the best way to fix this.
                    //                assertEquals(A.getWidth(), 100);
                    //                assertEquals(B.getWidth(), 100);
                }
            },
        )
    }

    @Test
    fun testWidgetMultipleDependentPositioning() {
        val root = ConstraintWidget(400, 400)
        val a = ConstraintWidget(100, 20)
        val b = ConstraintWidget(100, 20)
        root.setDebugSolverName(mLS, "root")
        a.setDebugSolverName(mLS, "A")
        b.setDebugSolverName(mLS, "B")
        val widgets: ArrayList<ConstraintWidget> = ArrayList()
        widgets.add(root)
        widgets.add(b)
        widgets.add(a)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP, 10)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM, 10)
        b.connect(ConstraintAnchor.Type.TOP, a, ConstraintAnchor.Type.BOTTOM)
        b.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        root.resetSolverVariables(mLS.getCache())
        a.resetSolverVariables(mLS.getCache())
        b.resetSolverVariables(mLS.getCache())
        runTestOnWidgets(
            widgets,
            object : Runnable {
                override fun run() {
                    println("root: $root A: $a B: $b")
                    assertEquals(root.height, 400)
                    assertEquals(root.height, 400)
                    assertEquals(a.height, 20)
                    assertEquals(b.height, 20)
                    assertEquals(a.top - root.top, root.bottom - a.bottom)
                    assertEquals(b.top - a.bottom, root.bottom - b.bottom)
                }
            },
        )
    }

    @Test
    fun testMinSize() {
        val root = ConstraintWidgetContainer(600, 400)
        val a = ConstraintWidget(100, 20)
        a.connect(ConstraintAnchor.Type.LEFT, root, ConstraintAnchor.Type.LEFT)
        a.connect(ConstraintAnchor.Type.RIGHT, root, ConstraintAnchor.Type.RIGHT)
        a.connect(ConstraintAnchor.Type.TOP, root, ConstraintAnchor.Type.TOP)
        a.connect(ConstraintAnchor.Type.BOTTOM, root, ConstraintAnchor.Type.BOTTOM)
        root.debugName = "root"
        a.debugName = "A"
        root.add(a)
        root.optimizationLevel = 0
        root.layout()
        println("a) root: $root A: $a")
        assertEquals(root.width, 600)
        assertEquals(root.height, 400)
        assertEquals(a.width, 100)
        assertEquals(a.height, 20)
        assertEquals(a.left - root.left, root.right - a.right)
        assertEquals(a.top - root.top, root.bottom - a.bottom)
        root.setVerticalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("b) root: $root A: $a")
        assertEquals(root.height, a.height)
        assertEquals(a.width, 100)
        assertEquals(a.height, 20)
        assertEquals(a.left - root.left, root.right - a.right)
        assertEquals(a.top - root.top, root.bottom - a.bottom)
        root.setMinHeight(200)
        root.layout()
        println("c) root: $root A: $a")
        assertEquals(root.height, 200)
        assertEquals(a.width, 100)
        assertEquals(a.height, 20)
        assertEquals(a.left - root.left, root.right - a.right)
        assertEquals(a.top - root.top, root.bottom - a.bottom)
        root.setHorizontalDimensionBehaviour(ConstraintWidget.DimensionBehaviour.WRAP_CONTENT)
        root.layout()
        println("d) root: $root A: $a")
        assertEquals(root.width, a.width)
        assertEquals(root.height, 200)
        assertEquals(a.width, 100)
        assertEquals(a.height, 20)
        assertEquals(a.left - root.left, root.right - a.right)
        assertEquals(a.top - root.top, root.bottom - a.bottom)
        root.setMinWidth(300)
        root.layout()
        println("e) root: $root A: $a")
        assertEquals(root.width, 300)
        assertEquals(root.height, 200)
        assertEquals(a.width, 100)
        assertEquals(a.height, 20)
        assertEquals(a.left - root.left, root.right - a.right)
        assertEquals(a.top - root.top, root.bottom - a.bottom)
    }

    /*
     * Insert the widgets in all permutations
     * (to test that the insert order
     * doesn't impact the resolution)
     */
    private fun runTestOnWidgets(
        widgets: ArrayList<ConstraintWidget>,
        check: Runnable,
    ) {
        val tail: ArrayList<Int> = ArrayList()
        for (i in widgets.indices) {
            tail.add(i)
        }
        addToSolverWithPermutation(widgets, ArrayList<Int>(), tail, check)
    }

    private fun runTestOnUIWidgets(widgets: ArrayList<ConstraintWidget>) {
        for (i in widgets.indices) {
            val widget: ConstraintWidget = widgets[i]
            if (widget.debugName != null) {
                widget.setDebugSolverName(mLS, widget.debugName!!)
            }
            widget.resetSolverVariables(mLS.getCache())
            widget.addToSolver(mLS, mOptimize)
        }
        try {
            mLS.minimize()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        for (j in widgets.indices) {
            val w: ConstraintWidget = widgets.get(j)
            w.updateFromSolver(mLS, mOptimize)
            println(" $w")
        }
    }

    private fun addToSolverWithPermutation(
        widgets: ArrayList<ConstraintWidget>,
        list: ArrayList<Int>,
        tail: ArrayList<Int>,
        check: Runnable,
    ) {
        if (tail.size > 0) {
            val n: Int = tail.size
            for (i in 0 until n) {
                list.add(tail.get(i))
                val permuted: ArrayList<Int> = ArrayList<Int>(tail)
                permuted.removeAt(i)
                addToSolverWithPermutation(widgets, list, permuted, check)
                list.removeAt(list.size - 1)
            }
        } else {
            //            System.out.print("Adding widgets in order: ");
            mLS.reset()
            for (i in list.indices) {
                val index: Int = list.get(i)
                //                System.out.print(" " + index);
                val widget: ConstraintWidget = widgets.get(index)
                widget.resetSolverVariables(mLS.getCache())
            }
            for (i in list.indices) {
                val index: Int = list.get(i)
                //                System.out.print(" " + index);
                val widget: ConstraintWidget = widgets.get(index)
                if (widget.debugName != null) {
                    widget.setDebugSolverName(mLS, widget.debugName!!)
                }
                widget.addToSolver(mLS, mOptimize)
            }
            //            System.out.println("");
            //            s.displayReadableRows();
            try {
                mLS.minimize()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            for (j in widgets.indices) {
                val w: ConstraintWidget = widgets.get(j)
                w.updateFromSolver(mLS, mOptimize)
            }
            //            try {
            check.run()
            //            } catch (AssertionError e) {
            //                System.out.println("Assertion error: " + e);
            //                runTestOnUIWidgets(widgets);
            //            }
        }
    }
}
