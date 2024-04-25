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
package androidx.constraintlayout.coremp

/**
 * Utility class to track metrics during the system resolution
 */
class Metrics {
    var measuresWidgetsDuration: Long = 0 // time spent in child measures in nanoseconds
    var measuresLayoutDuration: Long = 0 // time spent in child measures in nanoseconds
    var measuredWidgets: Long = 0
    var measuredMatchWidgets: Long = 0
    var measures: Long = 0
    var additionalMeasures: Long = 0
    var resolutions: Long = 0
    var tableSizeIncrease: Long = 0
    var minimize: Long = 0
    var constraints: Long = 0
    var simpleconstraints: Long = 0
    var optimize: Long = 0
    var iterations: Long = 0
    var pivots: Long = 0
    var bfs: Long = 0
    var variables: Long = 0
    var errors: Long = 0
    var slackvariables: Long = 0
    var extravariables: Long = 0
    var maxTableSize: Long = 0
    var fullySolved: Long = 0
    var graphOptimizer: Long = 0
    var graphSolved: Long = 0
    var linearSolved: Long = 0
    var resolvedWidgets: Long = 0
    var minimizeGoal: Long = 0
    var maxVariables: Long = 0
    var maxRows: Long = 0
    var nonresolvedWidgets: Long = 0
    var problematicLayouts = ArrayList<String>()
    var lastTableSize: Long = 0
    var widgets: Long = 0
    var measuresWrap: Long = 0
    var measuresWrapInfeasible: Long = 0
    var infeasibleDetermineGroups: Long = 0
    var determineGroups: Long = 0
    var layouts: Long = 0
    var grouping: Long = 0
    var mNumberOfLayouts = 0 // the number of times ConstraintLayout onLayout gets called
    var mNumberOfMeasures = 0 // the number of times child measures gets called
    var mMeasureDuration: Long = 0 // time spent in measure in nanoseconds
    var mChildCount: Long = 0 // number of child Views of ConstraintLayout
    var mMeasureCalls: Long = 0 // number of time CL onMeasure is called
    var mSolverPasses: Long = 0
    var mEquations: Long = 0
    var mVariables: Long = 0
    var mSimpleEquations: Long = 0

    // @TODO: add description
    override fun toString(): String {
        return """
             
             *** Metrics ***
             measures: $measures
             measuresWrap: $measuresWrap
             measuresWrapInfeasible: $measuresWrapInfeasible
             determineGroups: $determineGroups
             infeasibleDetermineGroups: $infeasibleDetermineGroups
             graphOptimizer: $graphOptimizer
             widgets: $widgets
             graphSolved: $graphSolved
             linearSolved: $linearSolved
             
        """.trimIndent()
    }

    // @TODO: add description
    fun reset() {
        measures = 0
        widgets = 0
        additionalMeasures = 0
        resolutions = 0
        tableSizeIncrease = 0
        maxTableSize = 0
        lastTableSize = 0
        maxVariables = 0
        maxRows = 0
        minimize = 0
        minimizeGoal = 0
        constraints = 0
        simpleconstraints = 0
        optimize = 0
        iterations = 0
        pivots = 0
        bfs = 0
        variables = 0
        errors = 0
        slackvariables = 0
        extravariables = 0
        fullySolved = 0
        graphOptimizer = 0
        graphSolved = 0
        resolvedWidgets = 0
        nonresolvedWidgets = 0
        linearSolved = 0
        problematicLayouts.clear()
        mNumberOfMeasures = 0
        mNumberOfLayouts = 0
        measuresWidgetsDuration = 0
        measuresLayoutDuration = 0
        mChildCount = 0
        mMeasureDuration = 0
        mMeasureCalls = 0
        mSolverPasses = 0
        mVariables = 0
        mEquations = 0
        mSimpleEquations = 0
    }

    /**
     * Copy the values from and existing Metrics class
     * @param metrics
     */
    fun copy(metrics: Metrics) {
        mVariables = metrics.mVariables
        mEquations = metrics.mEquations
        mSimpleEquations = metrics.mSimpleEquations
        mNumberOfMeasures = metrics.mNumberOfMeasures
        mNumberOfLayouts = metrics.mNumberOfLayouts
        mMeasureDuration = metrics.mMeasureDuration
        mChildCount = metrics.mChildCount
        mMeasureCalls = metrics.mMeasureCalls
        measuresWidgetsDuration = metrics.measuresWidgetsDuration
        mSolverPasses = metrics.mSolverPasses
        measuresLayoutDuration = metrics.measuresLayoutDuration
        measures = metrics.measures
        widgets = metrics.widgets
        additionalMeasures = metrics.additionalMeasures
        resolutions = metrics.resolutions
        tableSizeIncrease = metrics.tableSizeIncrease
        maxTableSize = metrics.maxTableSize
        lastTableSize = metrics.lastTableSize
        maxVariables = metrics.maxVariables
        maxRows = metrics.maxRows
        minimize = metrics.minimize
        minimizeGoal = metrics.minimizeGoal
        constraints = metrics.constraints
        simpleconstraints = metrics.simpleconstraints
        optimize = metrics.optimize
        iterations = metrics.iterations
        pivots = metrics.pivots
        bfs = metrics.bfs
        variables = metrics.variables
        errors = metrics.errors
        slackvariables = metrics.slackvariables
        extravariables = metrics.extravariables
        fullySolved = metrics.fullySolved
        graphOptimizer = metrics.graphOptimizer
        graphSolved = metrics.graphSolved
        resolvedWidgets = metrics.resolvedWidgets
        nonresolvedWidgets = metrics.nonresolvedWidgets
    }
}
