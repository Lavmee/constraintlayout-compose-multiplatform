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

import androidx.constraintlayout.coremp.SolverVariable.Companion.increaseErrorId
import androidx.constraintlayout.coremp.widgets.Chain
import androidx.constraintlayout.coremp.widgets.ConstraintAnchor
import androidx.constraintlayout.coremp.widgets.ConstraintWidget
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

class LinearSystem {

    /*
     * Default size for the object pools
     */
    private var mPoolSize = 1000
    var hasSimpleDefinition = false

    /*
     * Variable counter
     */
    var mVariablesID = 0

    /*
     * Store a map between name->SolverVariable and SolverVariable->Float for the resolution.
     */
    private var mVariables: HashMap<String, SolverVariable>? = null

    /*
     * The goal that is used when minimizing the system.
     */
    private var mGoal: Row? = null

    private var mTableSize = 32 // default table size for the allocation

    private var mMaxColumns = mTableSize
    var mRows: Array<ArrayRow?>? = null

    // if true, will use graph optimizations
    var graphOptimizer = false
    var newGraphOptimizer = false

    // Used in optimize()
    private var mAlreadyTestedCandidates = BooleanArray(mTableSize)

    var mNumColumns = 1
    var mNumRows = 0
    private var mMaxRows = mTableSize

    var mCache: Cache

    private var mPoolVariables: Array<SolverVariable?> = arrayOfNulls(mPoolSize)
    private var mPoolVariablesCount = 0

    private var mTempGoal: Row? = null

    inner class ValuesRow(cache: Cache) : ArrayRow() {
        init {
            variables = SolverVariableValues(this, cache)
        }
    }

    init {
        mRows = arrayOfNulls(mTableSize)
        releaseRows()
        mCache = Cache()
        mGoal = PriorityGoalRow(mCache)
        mTempGoal = if (OPTIMIZED_ENGINE) {
            ValuesRow(mCache)
        } else {
            ArrayRow(mCache)
        }
    }

    // @TODO: add description
    fun fillMetrics(metrics: Metrics) {
        sMetrics = metrics
    }

    interface Row {
        fun getPivotCandidate(system: LinearSystem?, avoid: BooleanArray?): SolverVariable?
        fun clear()
        fun initFromRow(row: Row?)
        fun addError(variable: SolverVariable?)
        fun updateFromSystem(system: LinearSystem?)
        val key: SolverVariable?
        val isEmpty: Boolean

        fun updateFromRow(system: LinearSystem?, definition: ArrayRow?, b: Boolean)
        fun updateFromFinalVariable(
            system: LinearSystem?,
            variable: SolverVariable?,
            removeFromDefinition: Boolean,
        )
    }

    /*--------------------------------------------------------------------------------------------*/
    // Memory management
    /*--------------------------------------------------------------------------------------------*/

    /**
     * Reallocate memory to accommodate increased amount of variables
     */
    private fun increaseTableSize() {
        if (DEBUG) {
            println("###########################")
            println(
                "### INCREASE TABLE TO " + mTableSize * 2 + " (num rows: " +
                    mNumRows + ", num cols: " + mNumColumns + "/" + mMaxColumns + ")",
            )
            println("###########################")
        }
        mTableSize *= 2
        mRows = mRows!!.copyOf(mTableSize)
        mCache.mIndexedVariables = mCache.mIndexedVariables.copyOf(mTableSize)
        mAlreadyTestedCandidates = BooleanArray(mTableSize)
        mMaxColumns = mTableSize
        mMaxRows = mTableSize
        if (sMetrics != null) {
            sMetrics!!.tableSizeIncrease++
            sMetrics!!.maxTableSize = max(sMetrics!!.maxTableSize, mTableSize.toLong())
            sMetrics!!.lastTableSize = sMetrics!!.maxTableSize
        }
    }

    /**
     * Release ArrayRows back to their pool
     */
    private fun releaseRows() {
        if (OPTIMIZED_ENGINE) {
            for (i in 0 until mNumRows) {
                val row: ArrayRow? = mRows!![i]
                if (row != null) {
                    mCache.mOptimizedArrayRowPool.release(row)
                }
                mRows!![i] = null
            }
        } else {
            for (i in 0 until mNumRows) {
                val row: ArrayRow? = mRows!![i]
                if (row != null) {
                    mCache.mArrayRowPool.release(row)
                }
                mRows!![i] = null
            }
        }
    }

    /**
     * Reset the LinearSystem object so that it can be reused.
     */
    fun reset() {
        if (DEBUG) {
            println("##################")
            println("## RESET SYSTEM ##")
            println("##################")
        }
        for (i in 0 until mCache.mIndexedVariables.size) {
            mCache.mIndexedVariables[i]?.reset()
        }
        mCache.mSolverVariablePool.releaseAll(mPoolVariables, mPoolVariablesCount)
        mPoolVariablesCount = 0
        mCache.mIndexedVariables.fill(null)
        mVariables?.clear()
        mVariablesID = 0
        mGoal!!.clear()
        mNumColumns = 1
        for (i in 0 until mNumRows) {
            if (mRows!![i] != null) {
                mRows!![i]!!.mUsed = false
            }
        }
        releaseRows()
        mNumRows = 0
        mTempGoal = if (OPTIMIZED_ENGINE) {
            ValuesRow(mCache)
        } else {
            ArrayRow(mCache)
        }
    }

    /*--------------------------------------------------------------------------------------------*/
    // Creation of rows / variables / errors
    /*--------------------------------------------------------------------------------------------*/

    /*--------------------------------------------------------------------------------------------*/
    // Creation of rows / variables / errors
    /*--------------------------------------------------------------------------------------------*/
    // @TODO: add description
    fun createObjectVariable(anchor: Any?): SolverVariable? {
        if (anchor == null) {
            return null
        }
        if (mNumColumns + 1 >= mMaxColumns) {
            increaseTableSize()
        }
        var variable: SolverVariable? = null
        if (anchor is ConstraintAnchor) {
            variable = anchor.getSolverVariable()
            if (variable == null) {
                anchor.resetSolverVariable(mCache)
                variable = anchor.getSolverVariable()
            }
            if (variable!!.id == -1 || variable.id > mVariablesID || mCache.mIndexedVariables[variable.id] == null
            ) {
                if (variable.id != -1) {
                    variable.reset()
                }
                mVariablesID++
                mNumColumns++
                variable.id = mVariablesID
                variable.mType = SolverVariable.Type.UNRESTRICTED
                mCache.mIndexedVariables[mVariablesID] = variable
            }
        }
        return variable
    }

    // @TODO: add description
    fun createRow(): ArrayRow {
        var row: ArrayRow?
        if (OPTIMIZED_ENGINE) {
            row = mCache.mOptimizedArrayRowPool.acquire()
            if (row == null) {
                row = ValuesRow(mCache)
                OPTIMIZED_ARRAY_ROW_CREATION++
            } else {
                row.reset()
            }
        } else {
            row = mCache.mArrayRowPool.acquire()
            if (row == null) {
                row = ArrayRow(mCache)
                ARRAY_ROW_CREATION++
            } else {
                row.reset()
            }
        }
        increaseErrorId()
        return row
    }

    // @TODO: add description
    public fun createSlackVariable(): SolverVariable {
        if (sMetrics != null) {
            sMetrics!!.slackvariables++
        }
        if (mNumColumns + 1 >= mMaxColumns) {
            increaseTableSize()
        }
        val variable: SolverVariable = acquireSolverVariable(SolverVariable.Type.SLACK, null)
        mVariablesID++
        mNumColumns++
        variable.id = mVariablesID
        mCache.mIndexedVariables[mVariablesID] = variable
        return variable
    }

    // @TODO: add description
    public fun createExtraVariable(): SolverVariable {
        if (sMetrics != null) {
            sMetrics!!.extravariables++
        }
        if (mNumColumns + 1 >= mMaxColumns) {
            increaseTableSize()
        }
        val variable: SolverVariable = acquireSolverVariable(SolverVariable.Type.SLACK, null)
        mVariablesID++
        mNumColumns++
        variable.id = mVariablesID
        mCache.mIndexedVariables[mVariablesID] = variable
        return variable
    }

    fun addSingleError(row: ArrayRow, sign: Int, strength: Int) {
        var prefix: String? = null
        if (DEBUG) {
            prefix = if (sign > 0) {
                "ep"
            } else {
                "em"
            }
            prefix = "em"
        }
        val error: SolverVariable = createErrorVariable(strength, prefix)
        row.addSingleError(error, sign)
    }

    private fun createVariable(name: String, type: SolverVariable.Type): SolverVariable {
        if (sMetrics != null) {
            sMetrics!!.variables++
        }
        if (mNumColumns + 1 >= mMaxColumns) {
            increaseTableSize()
        }
        val variable: SolverVariable = acquireSolverVariable(type, null)
        variable.setName(name)
        mVariablesID++
        mNumColumns++
        variable.id = mVariablesID
        if (mVariables == null) {
            mVariables = HashMap()
        }
        mVariables!![name] = variable
        mCache.mIndexedVariables[mVariablesID] = variable
        return variable
    }

    // @TODO: add description
    fun createErrorVariable(strength: Int, prefix: String?): SolverVariable {
        if (sMetrics != null) {
            sMetrics!!.errors++
        }
        if (mNumColumns + 1 >= mMaxColumns) {
            increaseTableSize()
        }
        val variable: SolverVariable = acquireSolverVariable(SolverVariable.Type.ERROR, prefix)
        mVariablesID++
        mNumColumns++
        variable.id = mVariablesID
        variable.strength = strength
        mCache.mIndexedVariables[mVariablesID] = variable
        mGoal!!.addError(variable)
        return variable
    }

    /**
     * Returns a SolverVariable instance of the given type
     *
     * @param type type of the SolverVariable
     * @return instance of SolverVariable
     */
    private fun acquireSolverVariable(type: SolverVariable.Type, prefix: String?): SolverVariable {
        var variable: SolverVariable? = mCache.mSolverVariablePool.acquire()
        if (variable == null) {
            variable = SolverVariable(type, prefix.toString())
            variable.setType(type, prefix)
        } else {
            variable.reset()
            variable.setType(type, prefix)
        }
        if (mPoolVariablesCount >= mPoolSize) {
            mPoolSize *= 2
            mPoolVariables = mPoolVariables.copyOf(mPoolSize)
        }
        mPoolVariables[mPoolVariablesCount++] = variable
        return variable
    }

    /*--------------------------------------------------------------------------------------------*/
    // Accessors of rows / variables / errors
    /*--------------------------------------------------------------------------------------------*/

    /*--------------------------------------------------------------------------------------------*/
    // Accessors of rows / variables / errors
    /*--------------------------------------------------------------------------------------------*/
    /**
     * Simple accessor for the current goal. Used when minimizing the system's goal.
     *
     * @return the current goal.
     */
    fun getGoal(): Row? {
        return mGoal
    }

    fun getRow(n: Int): ArrayRow? {
        return mRows!![n]
    }

    fun getValueFor(name: String?): Float {
        val v: SolverVariable = getVariable(name, SolverVariable.Type.UNRESTRICTED)
        return v.computedValue
    }

    // @TODO: add description
    fun getObjectVariableValue(obj: Any): Int {
        val anchor: ConstraintAnchor = obj as ConstraintAnchor
        if (Chain.USE_CHAIN_OPTIMIZATION) {
            if (anchor.hasFinalValue()) {
                return anchor.getFinalValue()
            }
        }
        val variable: SolverVariable? = anchor.getSolverVariable()
        return if (variable != null) {
            (variable.computedValue + 0.5f).toInt()
        } else {
            0
        }
    }

    /**
     * Returns a SolverVariable instance given a name and a type.
     *
     * @param name name of the variable
     * @param type [type][SolverVariable.Type] of the variable
     * @return a SolverVariable instance
     */
    fun getVariable(name: String?, type: SolverVariable.Type?): SolverVariable {
        if (mVariables == null) {
            mVariables = HashMap()
        }
        var variable = mVariables!![name]
        if (variable == null) {
            variable = createVariable(name!!, type!!)
        }
        return variable
    }

    /*--------------------------------------------------------------------------------------------*/
    // System resolution
    /*--------------------------------------------------------------------------------------------*/

    /*--------------------------------------------------------------------------------------------*/
    // System resolution
    /*--------------------------------------------------------------------------------------------*/
    /**
     * Minimize the current goal of the system.
     */
    @Throws(Exception::class)
    fun minimize() {
        if (sMetrics != null) {
            sMetrics!!.minimize++
        }
        if (mGoal!!.isEmpty) {
            if (DEBUG) {
                println("\n*** SKIPPING MINIMIZE! ***\n")
            }
            computeValues()
            return
        }
        if (DEBUG) {
            println("\n*** MINIMIZE ***\n")
        }
        if (graphOptimizer || newGraphOptimizer) {
            if (sMetrics != null) {
                sMetrics!!.graphOptimizer++
            }
            var fullySolved = true
            for (i in 0 until mNumRows) {
                val r: ArrayRow = mRows!![i]!!
                if (!r.mIsSimpleDefinition) {
                    fullySolved = false
                    break
                }
            }
            if (!fullySolved) {
                minimizeGoal(mGoal!!)
            } else {
                if (sMetrics != null) {
                    sMetrics!!.fullySolved++
                }
                computeValues()
            }
        } else {
            minimizeGoal(mGoal!!)
        }
        if (DEBUG) {
            println("\n*** END MINIMIZE ***\n")
        }
    }

    /**
     * Minimize the given goal with the current system.
     *
     * @param goal the goal to minimize.
     */
    // @Throws(java.lang.Exception::class)
    fun minimizeGoal(goal: Row) {
        if (sMetrics != null) {
            sMetrics!!.minimizeGoal++
            sMetrics!!.maxVariables = sMetrics!!.maxVariables.coerceAtLeast(mNumColumns.toLong())
            sMetrics!!.maxRows = max(sMetrics!!.maxRows, mNumRows.toLong())
        }
        // First, let's make sure that the system is in Basic Feasible Solved Form (BFS), i.e.
        // all the constants of the restricted variables should be positive.
        if (DEBUG) {
            println("minimize goal: $goal")
        }
        // we don't need this for now as we incrementally built the system
        // goal.updateFromSystem(this);
        if (DEBUG) {
            displayReadableRows()
        }
        enforceBFS(goal)
        if (DEBUG) {
            println("Goal after enforcing BFS $goal")
            displayReadableRows()
        }
        optimize(goal, false)
        if (DEBUG) {
            println("Goal after optimization $goal")
            displayReadableRows()
        }
        computeValues()
    }

    @Suppress("unused")
    fun cleanupRows() {
        var i = 0
        while (i < mNumRows) {
            val current: ArrayRow = mRows!![i]!!
            if (current.variables!!.currentSize == 0) {
                current.mIsSimpleDefinition = true
            }
            if (current.mIsSimpleDefinition) {
                current.mVariable!!.computedValue = current.mConstantValue
                current.mVariable!!.removeFromRow(current)
                for (j in i until mNumRows - 1) {
                    mRows!![j] = mRows!![j + 1]
                }
                mRows!![mNumRows - 1] = null
                mNumRows--
                i--
                if (OPTIMIZED_ENGINE) {
                    mCache.mOptimizedArrayRowPool.release(current)
                } else {
                    mCache.mArrayRowPool.release(current)
                }
            }
            i++
        }
    }

    /**
     * Add the equation to the system
     *
     * @param row the equation we want to add expressed as a system row.
     */
    fun addConstraint(row: ArrayRow?) {
        if (row == null) {
            return
        }
        if (sMetrics != null) {
            sMetrics!!.constraints++
            if (row.mIsSimpleDefinition) {
                sMetrics!!.simpleconstraints++
            }
        }
        if (mNumRows + 1 >= mMaxRows || mNumColumns + 1 >= mMaxColumns) {
            increaseTableSize()
        }
        if (DEBUG) {
            println("addConstraint <" + row.toReadableString() + ">")
            displayReadableRows()
        }
        var added = false
        if (!row.mIsSimpleDefinition) {
            // Update the equation with the variables already defined in the system
            row.updateFromSystem(this)
            if (row.isEmpty) {
                return
            }

            // First, ensure that if we have a constant it's positive
            row.ensurePositiveConstant()
            if (DEBUG) {
                println("addConstraint, updated row : " + row.toReadableString())
            }

            // Then pick a good variable to use for the row
            if (row.chooseSubject(this)) {
                // extra variable added... let's try to see if we can remove it
                val extra = createExtraVariable()
                row.mVariable = extra
                val numRows = mNumRows
                addRow(row)
                if (mNumRows == numRows + 1) {
                    added = true
                    mTempGoal!!.initFromRow(row)
                    optimize(mTempGoal!!, true)
                    if (extra.mDefinitionId == -1) {
                        if (DEBUG) {
                            println("row added is 0, so get rid of it")
                        }
                        if (row.mVariable == extra) {
                            // move extra to be parametric
                            val pivotCandidate: SolverVariable? = row.pickPivot(extra)
                            if (pivotCandidate != null) {
                                if (sMetrics != null) {
                                    sMetrics!!.pivots++
                                }
                                row.pivot(pivotCandidate)
                            }
                        }
                        if (!row.mIsSimpleDefinition) {
                            row.mVariable!!.updateReferencesWithNewDefinition(this, row)
                        }
                        if (OPTIMIZED_ENGINE) {
                            mCache.mOptimizedArrayRowPool.release(row)
                        } else {
                            mCache.mArrayRowPool.release(row)
                        }
                        mNumRows--
                    }
                }
            }
            if (!row.hasKeyVariable()) {
                // Can happen if row resolves to nil
                if (DEBUG) {
                    println("No variable found to pivot on " + row.toReadableString())
                    displayReadableRows()
                }
                return
            }
        }
        if (!added) {
            addRow(row)
        }
    }

    private fun addRow(row: ArrayRow) {
        if (SIMPLIFY_SYNONYMS && row.mIsSimpleDefinition) {
            row.mVariable!!.setFinalValue(this, row.mConstantValue)
        } else {
            mRows!![mNumRows] = row
            row.mVariable!!.mDefinitionId = mNumRows
            mNumRows++
            row.mVariable!!.updateReferencesWithNewDefinition(this, row)
        }
        if (DEBUG) {
            println("Row added: $row")
            println("here is the system:")
            displayReadableRows()
        }
        if (SIMPLIFY_SYNONYMS && hasSimpleDefinition) {
            // compact the rows...
            var i = 0
            while (i < mNumRows) {
                if (mRows!![i] == null) {
                    println("WTF")
                }
                if (mRows!![i] != null && mRows!![i]!!.mIsSimpleDefinition) {
                    val removedRow: ArrayRow = mRows!![i]!!
                    removedRow.mVariable!!.setFinalValue(this, removedRow.mConstantValue)
                    if (OPTIMIZED_ENGINE) {
                        mCache.mOptimizedArrayRowPool.release(removedRow)
                    } else {
                        mCache.mArrayRowPool.release(removedRow)
                    }
                    mRows!![i] = null
                    var lastRow = i + 1
                    for (j in i + 1 until mNumRows) {
                        mRows!![j - 1] = mRows!![j]
                        if (mRows!![j - 1]!!.mVariable!!.mDefinitionId == j) {
                            mRows!![j - 1]!!.mVariable!!.mDefinitionId = j - 1
                        }
                        lastRow = j
                    }
                    if (lastRow < mNumRows) {
                        mRows!![lastRow] = null
                    }
                    mNumRows--
                    i--
                }
                i++
            }
            hasSimpleDefinition = false
        }
    }

    // @TODO: add description
    @Suppress("unused")
    fun removeRow(row: ArrayRow) {
        if (row.mIsSimpleDefinition && row.mVariable != null) {
            if (row.mVariable!!.mDefinitionId != -1) {
                for (i in row.mVariable!!.mDefinitionId until mNumRows - 1) {
                    val rowVariable: SolverVariable = mRows!![i + 1]!!.mVariable!!
                    if (rowVariable.mDefinitionId == i + 1) {
                        rowVariable.mDefinitionId = i
                    }
                    mRows!![i] = mRows!![i + 1]
                }
                mNumRows--
            }
            if (!row.mVariable!!.isFinalValue) {
                row.mVariable!!.setFinalValue(this, row.mConstantValue)
            }
            if (OPTIMIZED_ENGINE) {
                mCache.mOptimizedArrayRowPool.release(row)
            } else {
                mCache.mArrayRowPool.release(row)
            }
        }
    }

    /**
     * Optimize the system given a goal to minimize. The system should be in BFS form.
     *
     * @param goal goal to optimize.
     * @return number of iterations.
     */
    private fun optimize(goal: Row, b: Boolean): Int {
        if (sMetrics != null) {
            sMetrics!!.optimize++
        }
        var done = false
        var tries = 0
        for (i in 0 until mNumColumns) {
            mAlreadyTestedCandidates[i] = false
        }
        if (DEBUG) {
            println("\n****************************")
            println("*       OPTIMIZATION       *")
            println("* mNumColumns: $mNumColumns")
            println("* GOAL: $goal $b")
            println("****************************\n")
        }
        while (!done) {
            if (sMetrics != null) {
                sMetrics!!.iterations++
            }
            tries++
            if (DEBUG) {
                println("\n******************************")
                println("* iteration: $tries")
            }
            if (tries >= 2 * mNumColumns) {
                if (DEBUG) {
                    println(
                        (
                            "=> Exit optimization because tries " +
                                tries + " >= " + (2 * mNumColumns)
                            ),
                    )
                }
                return tries
            }
            if (goal.key != null) {
                mAlreadyTestedCandidates[goal.key!!.id] = true
            }
            val pivotCandidate = goal.getPivotCandidate(this, mAlreadyTestedCandidates)
            if (DEBUG) {
                println("* Pivot candidate: $pivotCandidate")
                println("******************************\n")
            }
            if (pivotCandidate != null) {
                if (mAlreadyTestedCandidates[pivotCandidate.id]) {
                    if (DEBUG) {
                        println(
                            (
                                "* Pivot candidate " + pivotCandidate +
                                    " already tested, let's bail"
                                ),
                        )
                    }
                    return tries
                } else {
                    mAlreadyTestedCandidates[pivotCandidate.id] = true
                }
            }
            if (pivotCandidate != null) {
                if (DEBUG) {
                    println("valid pivot candidate: $pivotCandidate")
                }
                // there's a negative variable in the goal that we can pivot on.
                // We now need to select which equation of the system we should do
                // the pivot on.

                // Let's try to find the equation in the system that we can pivot on.
                // The rules are simple:
                // - only look at restricted variables equations (i.e. Cs)
                // - only look at equations containing the column we are trying to pivot on (duh)
                // - select preferably an equation with strong strength over weak strength
                var min = Float.MAX_VALUE
                var pivotRowIndex = -1
                for (i in 0 until mNumRows) {
                    val current: ArrayRow = mRows!![i]!!
                    val variable: SolverVariable = current.mVariable!!
                    if (variable.mType == SolverVariable.Type.UNRESTRICTED) {
                        // skip unrestricted variables equations (to only look at Cs)
                        continue
                    }
                    if (current.mIsSimpleDefinition) {
                        continue
                    }
                    if (current.hasVariable(pivotCandidate)) {
                        if (DEBUG) {
                            println(
                                (
                                    "equation " + i + " " +
                                        current + " contains " + pivotCandidate
                                    ),
                            )
                        }
                        // the current row does contains the variable
                        // we want to pivot on
                        val a_j: Float = current.variables!![pivotCandidate]
                        if (a_j < 0) {
                            val value: Float = -current.mConstantValue / a_j
                            if (value < min) {
                                min = value
                                pivotRowIndex = i
                            }
                        }
                    }
                }
                // At this point, we ought to have an equation to pivot on
                if (pivotRowIndex > -1) {
                    // We found an equation to pivot on
                    if (DEBUG) {
                        println("We pivot on $pivotRowIndex")
                    }
                    val pivotEquation: ArrayRow = mRows!![pivotRowIndex]!!
                    pivotEquation.mVariable!!.mDefinitionId = -1
                    if (sMetrics != null) {
                        sMetrics!!.pivots++
                    }
                    pivotEquation.pivot(pivotCandidate)
                    pivotEquation.mVariable!!.mDefinitionId = pivotRowIndex
                    pivotEquation.mVariable!!.updateReferencesWithNewDefinition(this, pivotEquation)
                    if (DEBUG) {
                        println("new system after pivot:")
                        displayReadableRows()
                        println("optimizing: $goal")
                    }
                    /*
                    try {
                        enforceBFS(goal);
                    } catch (Exception e) {
                        System.out.println("### EXCEPTION " + e);
                        e.printStackTrace();
                    }
                     */
                    // now that we pivoted, we're going to continue looping on the next goal
                    // columns, until we exhaust all the possibilities of improving the system
                } else {
                    if (DEBUG) {
                        println("we couldn't find an equation to pivot upon")
                    }
                }
            } else {
                // There is no candidate goals columns we should try to pivot on,
                // so let's exit the loop.
                if (DEBUG) {
                    println("no more candidate goals to pivot on, let's exit")
                }
                done = true
            }
        }
        return tries
    }

    /**
     * Make sure that the system is in Basic Feasible Solved form (BFS).
     *
     * @param goal the row representing the system goal
     * @return number of iterations
     */
    // @Throws(java.lang.Exception::class)
    private fun enforceBFS(goal: Row): Int {
        var tries = 0
        var done: Boolean
        if (DEBUG) {
            println("\n#################")
            println("# ENFORCING BFS #")
            println("#################\n")
        }

        // At this point, we might not be in Basic Feasible Solved form (BFS),
        // i.e. one of the restricted equation has a negative constant.
        // Let's check if that's the case or not.
        var infeasibleSystem = false
        for (i in 0 until mNumRows) {
            val variable: SolverVariable = mRows!![i]!!.mVariable!!
            if (variable.mType == SolverVariable.Type.UNRESTRICTED) {
                continue // C can be either positive or negative.
            }
            if (mRows!![i]!!.mConstantValue < 0) {
                infeasibleSystem = true
                break
            }
        }

        // The system happens to not be in BFS form, we need to go back to it to properly solve it.
        if (infeasibleSystem) {
            if (DEBUG) {
                println("the current system is infeasible, let's try to fix this.")
            }

            // Going back to BFS form can be done by selecting any equations in Cs containing
            // a negative constant, then selecting a potential pivot variable that would remove
            // this negative constant. Once we have
            done = false
            tries = 0
            while (!done) {
                if (sMetrics != null) {
                    sMetrics!!.bfs++
                }
                tries++
                if (DEBUG) {
                    println("iteration on infeasible system $tries")
                }
                var min = Float.MAX_VALUE
                var strength = 0
                var pivotRowIndex = -1
                var pivotColumnIndex = -1

                for (i in 0 until mNumRows) {
                    val current: ArrayRow = mRows!![i]!!
                    val variable: SolverVariable = current.mVariable!!
                    if (variable.mType == SolverVariable.Type.UNRESTRICTED) {
                        // skip unrestricted variables equations, as C
                        // can be either positive or negative.
                        continue
                    }
                    if (current.mIsSimpleDefinition) {
                        continue
                    }
                    if (current.mConstantValue < 0) {
                        // let's examine this row, see if we can find a good pivot
                        if (DEBUG) {
                            println("looking at pivoting on row $current")
                        }
                        if (SKIP_COLUMNS) {
                            val size: Int = current.variables!!.currentSize
                            for (j in 0 until size) {
                                val candidate: SolverVariable? = current.variables!!.getVariable(j)
                                val a_j: Float = current.variables!![candidate]
                                if (a_j <= 0) {
                                    continue
                                }
                                if (DEBUG) {
                                    println("candidate for pivot $candidate")
                                }
                                for (k in 0 until SolverVariable.MAX_STRENGTH) {
                                    val value = candidate!!.mStrengthVector[k] / a_j
                                    if (value < min && k == strength || k > strength) {
                                        min = value
                                        pivotRowIndex = i
                                        pivotColumnIndex = candidate.id
                                        strength = k
                                    }
                                }
                            }
                        } else {
                            for (j in 1 until mNumColumns) {
                                val candidate: SolverVariable? = mCache.mIndexedVariables[j]
                                val a_j: Float = current.variables!![candidate]
                                if (a_j <= 0) {
                                    continue
                                }
                                if (DEBUG) {
                                    println("candidate for pivot $candidate")
                                }
                                for (k in 0 until SolverVariable.MAX_STRENGTH) {
                                    val value = candidate!!.mStrengthVector[k] / a_j
                                    if (value < min && k == strength || k > strength) {
                                        min = value
                                        pivotRowIndex = i
                                        pivotColumnIndex = j
                                        strength = k
                                    }
                                }
                            }
                        }
                    }
                }
                if (pivotRowIndex != -1) {
                    // We have a pivot!
                    val pivotEquation: ArrayRow = mRows!![pivotRowIndex]!!
                    if (DEBUG) {
                        println(
                            "Pivoting on " + pivotEquation.mVariable + " with " +
                                mCache.mIndexedVariables[pivotColumnIndex],
                        )
                    }
                    pivotEquation.mVariable!!.mDefinitionId = -1
                    if (sMetrics != null) {
                        sMetrics!!.pivots++
                    }
                    pivotEquation.pivot(mCache.mIndexedVariables[pivotColumnIndex]!!)
                    pivotEquation.mVariable!!.mDefinitionId = pivotRowIndex
                    pivotEquation.mVariable!!.updateReferencesWithNewDefinition(this, pivotEquation)

                    if (DEBUG) {
                        println("new goal after pivot: $goal")
                        displayRows()
                    }
                } else {
                    done = true
                }
                if (tries > mNumColumns / 2) {
                    // fail safe -- tried too many times
                    done = true
                }
            }
        }
        if (DEBUG) {
            println(
                "the current system should now be feasible [" +
                    infeasibleSystem + "] after " + tries + " iterations",
            )
            displayReadableRows()

            // Let's make sure the system is correct
            infeasibleSystem = false
            for (i in 0 until mNumRows) {
                val variable: SolverVariable = mRows!![i]!!.mVariable!!
                if (variable.mType == SolverVariable.Type.UNRESTRICTED) {
                    continue // C can be either positive or negative.
                }
                if (mRows!![i]!!.mConstantValue < 0) {
                    infeasibleSystem = true
                    break
                }
            }
            if (DEBUG && infeasibleSystem) {
                println("IMPOSSIBLE SYSTEM, WTF")
                throw Exception()
            }
            if (infeasibleSystem) {
                return tries
            }
        }
        return tries
    }

    private fun computeValues() {
        for (i in 0 until mNumRows) {
            val row: ArrayRow = mRows!![i]!!
            row.mVariable!!.computedValue = row.mConstantValue
        }
    }

    @Suppress("unused")
    private fun displayRows() {
        displaySolverVariables()
        var s = ""
        for (i in 0 until mNumRows) {
            s += mRows!![i]
            s += "\n"
        }
        s += mGoal.toString() + "\n"
        println(s)
    }

    // @TODO: add description
    fun displayReadableRows() {
        displaySolverVariables()
        var s = " num vars $mVariablesID\n"
        for (i in 0 until mVariablesID + 1) {
            val variable: SolverVariable? = mCache.mIndexedVariables[i]
            if (variable != null && variable.isFinalValue) {
                s += " $[" + i + "] => " + variable + " = " + variable.computedValue + "\n"
            }
        }
        s += "\n"
        for (i in 0 until mVariablesID + 1) {
            val variable: SolverVariable? = mCache.mIndexedVariables[i]
            if (variable != null && variable.mIsSynonym) {
                val synonym: SolverVariable? = mCache.mIndexedVariables[variable.mSynonym]
                s += (
                    " ~[" + i + "] => " + variable + " = " +
                        synonym + " + " + variable.mSynonymDelta + "\n"
                    )
            }
        }
        s += "\n\n #  "
        for (i in 0 until mNumRows) {
            s += mRows!![i]!!.toReadableString()
            s += "\n #  "
        }
        if (mGoal != null) {
            s += "Goal: $mGoal\n"
        }
        println(s)
    }

    // @TODO: add description
    @Suppress("unused")
    fun displayVariablesReadableRows() {
        displaySolverVariables()
        var s = ""
        for (i in 0 until mNumRows) {
            if (mRows!![i]!!.mVariable!!.mType == SolverVariable.Type.UNRESTRICTED) {
                s += mRows!![i]!!.toReadableString()
                s += "\n"
            }
        }
        s += mGoal.toString() + "\n"
        println(s)
    }

    // @TODO: add description
    @Suppress("unused")
    fun getMemoryUsed(): Int {
        var actualRowSize = 0
        for (i in 0 until mNumRows) {
            if (mRows!![i] != null) {
                actualRowSize += mRows!![i]!!.sizeInBytes()
            }
        }
        return actualRowSize
    }

    @Suppress("unused")
    fun getNumEquations(): Int {
        return mNumRows
    }

    @Suppress("unused")
    fun getNumVariables(): Int {
        return mVariablesID
    }

    /**
     * Display current system information
     */
    fun displaySystemInformation() {
        val count = 0
        var rowSize = 0
        for (i in 0 until mTableSize) {
            if (mRows!![i] != null) {
                rowSize += mRows!![i]!!.sizeInBytes()
            }
        }
        var actualRowSize = 0
        for (i in 0 until mNumRows) {
            if (mRows!![i] != null) {
                actualRowSize += mRows!![i]!!.sizeInBytes()
            }
        }
        println(
            "Linear System -> Table size: " + mTableSize +
                " (" + getDisplaySize(mTableSize * mTableSize) +
                ") -- row sizes: " + getDisplaySize(rowSize) +
                ", actual size: " + getDisplaySize(actualRowSize) +
                " rows: " + mNumRows + "/" + mMaxRows +
                " cols: " + mNumColumns + "/" + mMaxColumns +
                " " + count + " occupied cells, " + getDisplaySize(count),
        )
    }

    private fun displaySolverVariables() {
        val s = "Display Rows (" + mNumRows + "x" + mNumColumns + ")\n"
        /*
        s += ":\n\t | C | ";
        for (int i = 1; i <= mNumColumns; i++) {
            SolverVariable v = mCache.mIndexedVariables[i];
            s += v;
            s += " | ";
        }
        s += "\n";
         */
        println(s)
    }

    private fun getDisplaySize(n: Int): String {
        val mb = (n * 4) / 1024 / 1024
        if (mb > 0) {
            return "$mb Mb"
        }
        val kb = (n * 4) / 1024
        return if (kb > 0) {
            "$kb Kb"
        } else {
            "" + (n * 4) + " bytes"
        }
    }

    fun getCache(): Cache {
        return mCache
    }

    private fun getDisplayStrength(strength: Int): String {
        if (strength == SolverVariable.STRENGTH_LOW) {
            return "LOW"
        }
        if (strength == SolverVariable.STRENGTH_MEDIUM) {
            return "MEDIUM"
        }
        if (strength == SolverVariable.STRENGTH_HIGH) {
            return "HIGH"
        }
        if (strength == SolverVariable.STRENGTH_HIGHEST) {
            return "HIGHEST"
        }
        if (strength == SolverVariable.STRENGTH_EQUALITY) {
            return "EQUALITY"
        }
        if (strength == SolverVariable.STRENGTH_FIXED) {
            return "FIXED"
        }
        return if (strength == SolverVariable.STRENGTH_BARRIER) {
            "BARRIER"
        } else {
            "NONE"
        }
    }

    // //////////////////////////////////////////////////////////////////////////////////////
    // Equations
    // //////////////////////////////////////////////////////////////////////////////////////

    /**
     * Add an equation of the form a >= b + margin
     *
     * @param a        variable a
     * @param b        variable b
     * @param margin   margin
     * @param strength strength used
     */
    fun addGreaterThan(a: SolverVariable?, b: SolverVariable?, margin: Int, strength: Int) {
        if (DEBUG_CONSTRAINTS) {
            println(
                "-> " + a + " >= " + b + (if (margin != 0) " + $margin" else "") +
                    " " + getDisplayStrength(strength),
            )
        }
        val row: ArrayRow = createRow()
        val slack = createSlackVariable()
        slack.strength = 0
        row.createRowGreaterThan(a, b, slack, margin)
        if (strength != SolverVariable.STRENGTH_FIXED) {
            val slackValue: Float = row.variables!![slack]
            addSingleError(row, (-1 * slackValue).toInt(), strength)
        }
        addConstraint(row)
    }

    // @TODO: add description
    fun addGreaterBarrier(
        a: SolverVariable,
        b: SolverVariable,
        margin: Int,
        @Suppress("UNUSED_PARAMETER")
        hasMatchConstraintWidgets: Boolean,
    ) {
        if (DEBUG_CONSTRAINTS) {
            println("-> Barrier $a >= $b")
        }
        val row: ArrayRow = createRow()
        val slack = createSlackVariable()
        slack.strength = 0
        row.createRowGreaterThan(a, b, slack, margin)
        addConstraint(row)
    }

    /**
     * Add an equation of the form a <= b + margin
     *
     * @param a        variable a
     * @param b        variable b
     * @param margin   margin
     * @param strength strength used
     */
    fun addLowerThan(a: SolverVariable, b: SolverVariable, margin: Int, strength: Int) {
        if (DEBUG_CONSTRAINTS) {
            println(
                "-> " + a + " <= " + b + (if (margin != 0) " + $margin" else "") +
                    " " + getDisplayStrength(strength),
            )
        }
        val row: ArrayRow = createRow()
        val slack = createSlackVariable()
        slack.strength = 0
        row.createRowLowerThan(a, b, slack, margin)
        if (strength != SolverVariable.STRENGTH_FIXED) {
            val slackValue: Float = row.variables!![slack]
            addSingleError(row, (-1 * slackValue).toInt(), strength)
        }
        addConstraint(row)
    }

    // @TODO: add description
    fun addLowerBarrier(
        a: SolverVariable,
        b: SolverVariable,
        margin: Int,
        @Suppress("UNUSED_PARAMETER")
        hasMatchConstraintWidgets: Boolean,
    ) {
        if (DEBUG_CONSTRAINTS) {
            println("-> Barrier $a <= $b")
        }
        val row: ArrayRow = createRow()
        val slack = createSlackVariable()
        slack.strength = 0
        row.createRowLowerThan(a, b, slack, margin)
        addConstraint(row)
    }

    /**
     * Add an equation of the form (1 - bias) * (a - b) = bias * (c - d)
     *
     * @param a        variable a
     * @param b        variable b
     * @param m1       margin 1
     * @param bias     bias between ab - cd
     * @param c        variable c
     * @param d        variable d
     * @param m2       margin 2
     * @param strength strength used
     */
    fun addCentering(
        a: SolverVariable,
        b: SolverVariable,
        m1: Int,
        bias: Float,
        c: SolverVariable,
        d: SolverVariable,
        m2: Int,
        strength: Int,
    ) {
        if (DEBUG_CONSTRAINTS) {
            println(
                "-> [center bias: " + bias + "] : " + a + " - " + b +
                    " - " + m1 +
                    " = " + c + " - " + d + " - " + m2 +
                    " " + getDisplayStrength(strength),
            )
        }
        val row: ArrayRow = createRow()
        row.createRowCentering(a, b, m1, bias, c, d, m2)
        if (strength != SolverVariable.STRENGTH_FIXED) {
            row.addError(this, strength)
        }
        addConstraint(row)
    }

    // @TODO: add description
    fun addRatio(
        a: SolverVariable,
        b: SolverVariable,
        c: SolverVariable,
        d: SolverVariable,
        ratio: Float,
        strength: Int,
    ) {
        if (DEBUG_CONSTRAINTS) {
            println(
                "-> [ratio: " + ratio + "] : " + a + " = " + b +
                    " + (" + c + " - " + d + ") * " + ratio + " " + getDisplayStrength(
                        strength,
                    ),
            )
        }
        val row: ArrayRow = createRow()
        row.createRowDimensionRatio(a, b, c, d, ratio)
        if (strength != SolverVariable.STRENGTH_FIXED) {
            row.addError(this, strength)
        }
        addConstraint(row)
    }

    // @TODO: add description
    @Suppress("UNUSED")
    fun addSynonym(a: SolverVariable, b: SolverVariable, margin: Int) {
        var a = a
        var b = b
        var margin = margin
        if (a.mDefinitionId == -1 && margin == 0) {
            if (DEBUG_CONSTRAINTS) {
                println("(S) -> " + a + " = " + b + if (margin != 0) " + $margin" else "")
            }
            if (b.mIsSynonym) {
                margin += b.mSynonymDelta.toInt()
                b = mCache.mIndexedVariables[b.mSynonym]!!
            }
            if (a.mIsSynonym) {
                margin -= a.mSynonymDelta.toInt()
                a = mCache.mIndexedVariables[a.mSynonym]!!
            } else {
                a.setSynonym(this, b, 0f)
            }
        } else {
            addEquality(a, b, margin, SolverVariable.STRENGTH_FIXED)
        }
    }

    /**
     * Add an equation of the form a = b + margin
     *
     * @param a        variable a
     * @param b        variable b
     * @param margin   margin used
     * @param strength strength used
     */
    fun addEquality(a: SolverVariable, b: SolverVariable, margin: Int, strength: Int): ArrayRow? {
        var a = a
        var b = b
        var margin = margin
        if (sMetrics != null) {
            sMetrics!!.mSimpleEquations++
        }
        if (USE_BASIC_SYNONYMS && strength == SolverVariable.STRENGTH_FIXED && b.isFinalValue && a.mDefinitionId == -1) {
            if (DEBUG_CONSTRAINTS) {
                println(
                    "=> " + a + " = " + b + (if (margin != 0) " + $margin" else "") +
                        " = " + (b.computedValue + margin) + " (Synonym)",
                )
            }
            a.setFinalValue(this, b.computedValue + margin)
            return null
        }
        if (DO_NOT_USE && USE_SYNONYMS && strength == SolverVariable.STRENGTH_FIXED && a.mDefinitionId == -1 && margin == 0) {
            if (DEBUG_CONSTRAINTS) {
                println(
                    "(S) -> " + a + " = " + b + (if (margin != 0) " + $margin" else "") +
                        " " + getDisplayStrength(strength),
                )
            }
            if (b.mIsSynonym) {
                margin += b.mSynonymDelta.toInt()
                b = mCache.mIndexedVariables[b.mSynonym]!!
            }
            if (a.mIsSynonym) {
                margin -= a.mSynonymDelta.toInt()
                a = mCache.mIndexedVariables[a.mSynonym]!!
            } else {
                a.setSynonym(this, b, margin.toFloat())
                return null
            }
        }
        if (DEBUG_CONSTRAINTS) {
            println(
                "-> " + a + " = " + b + (if (margin != 0) " + $margin" else "") +
                    " " + getDisplayStrength(strength),
            )
        }
        val row: ArrayRow = createRow()
        row.createRowEquals(a, b, margin)
        if (strength != SolverVariable.STRENGTH_FIXED) {
            row.addError(this, strength)
        }
        addConstraint(row)
        return row
    }

    /**
     * Add an equation of the form a = value
     *
     * @param a     variable a
     * @param value the value we set
     */
    fun addEquality(a: SolverVariable, value: Int) {
        if (sMetrics != null) {
            sMetrics!!.mSimpleEquations++
        }
        if (USE_BASIC_SYNONYMS && a.mDefinitionId == -1) {
            if (DEBUG_CONSTRAINTS) {
                println("=> $a = $value (Synonym)")
            }
            a.setFinalValue(this, value.toFloat())
            for (i in 0 until mVariablesID + 1) {
                val variable: SolverVariable? = mCache.mIndexedVariables[i]
                if (variable != null && variable.mIsSynonym && variable.mSynonym == a.id) {
                    variable.setFinalValue(this, value + variable.mSynonymDelta)
                }
            }
            return
        }
        if (DEBUG_CONSTRAINTS) {
            println("-> $a = $value")
        }
        val idx = a.mDefinitionId
        if (a.mDefinitionId != -1) {
            val row: ArrayRow = mRows!![idx]!!
            if (row.mIsSimpleDefinition) {
                row.mConstantValue = value.toFloat()
            } else {
                if (row.variables!!.currentSize == 0) {
                    row.mIsSimpleDefinition = true
                    row.mConstantValue = value.toFloat()
                } else {
                    val newRow: ArrayRow = createRow()
                    newRow.createRowEquals(a, value)
                    addConstraint(newRow)
                }
            }
        } else {
            val row: ArrayRow = createRow()
            row.createRowDefinition(a, value)
            addConstraint(row)
        }
    }

    /**
     * Add the equations constraining a widget center to another widget center, positioned
     * on a circle, following an angle and radius
     *
     * @param angle  from 0 to 360
     * @param radius the distance between the two centers
     */
    fun addCenterPoint(
        widget: ConstraintWidget,
        target: ConstraintWidget,
        angle: Float,
        radius: Int,
    ) {
        val Al = createObjectVariable(widget.getAnchor(ConstraintAnchor.Type.LEFT))
        val At = createObjectVariable(widget.getAnchor(ConstraintAnchor.Type.TOP))
        val Ar = createObjectVariable(widget.getAnchor(ConstraintAnchor.Type.RIGHT))
        val Ab = createObjectVariable(widget.getAnchor(ConstraintAnchor.Type.BOTTOM))
        val Bl = createObjectVariable(target.getAnchor(ConstraintAnchor.Type.LEFT))
        val Bt = createObjectVariable(target.getAnchor(ConstraintAnchor.Type.TOP))
        val Br = createObjectVariable(target.getAnchor(ConstraintAnchor.Type.RIGHT))
        val Bb = createObjectVariable(target.getAnchor(ConstraintAnchor.Type.BOTTOM))
        var row: ArrayRow = createRow()
        var angleComponent = (sin(angle.toDouble()) * radius).toFloat()
        row.createRowWithAngle(At, Ab, Bt, Bb, angleComponent)
        addConstraint(row)
        row = createRow()
        angleComponent = (cos(angle.toDouble()) * radius).toFloat()
        row.createRowWithAngle(Al, Ar, Bl, Br, angleComponent)
        addConstraint(row)
    }

    companion object {
        const val FULL_DEBUG = false
        const val DEBUG = false
        private const val DO_NOT_USE = false

        private const val DEBUG_CONSTRAINTS = FULL_DEBUG

        var USE_DEPENDENCY_ORDERING = false
        var USE_BASIC_SYNONYMS = true
        var SIMPLIFY_SYNONYMS = true
        var USE_SYNONYMS = true
        var SKIP_COLUMNS = true
        var OPTIMIZED_ENGINE = false

        var sMetrics: Metrics? = null

        fun getMetrics(): Metrics? {
            return sMetrics
        }

        var ARRAY_ROW_CREATION: Long = 0
        var OPTIMIZED_ARRAY_ROW_CREATION: Long = 0

        /**
         * Create a constraint to express A = C * percent
         *
         * @param linearSystem the system we create the row on
         * @param variableA    variable a
         * @param variableC    variable c
         * @param percent      the percent used
         * @return the created row
         */
        fun createRowDimensionPercent(
            linearSystem: LinearSystem,
            variableA: SolverVariable,
            variableC: SolverVariable,
            percent: Float,
        ): ArrayRow {
            if (DEBUG_CONSTRAINTS) {
                println("-> $variableA = $variableC * $percent")
            }
            val row: ArrayRow = linearSystem.createRow()
            return row.createRowDimensionPercent(variableA, variableC, percent)
        }
    }
}
