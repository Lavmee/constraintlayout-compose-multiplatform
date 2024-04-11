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

/**
 * LinearEquation is used to represent the linear equations fed into the solver.<br></br>
 * A linear equation can be an equality or
 * an inequation (left term  or  to the right term).<br></br>
 * The general form will be similar to `a0x0 + a1x1 + ... = C + a2x2 + a3x3 + ... ,`
 * where `a0x0` is a term representing a variable x0 of an amount `a0`,
 * and `C` represent a constant term.
 * The amount of terms on the left side or the right side of the equation is arbitrary.
 */
class LinearEquation {
    private val mLeftSide: ArrayList<EquationVariable> =
        ArrayList<EquationVariable>()
    private val mRightSide: ArrayList<EquationVariable> =
        ArrayList<EquationVariable>()
    private var mCurrentSide: ArrayList<EquationVariable>? = null
    val isNull: Boolean
        get() {
            if (mLeftSide.size == 0 && mRightSide.size == 0) {
                return true
            }
            if (mLeftSide.size == 1 && mRightSide.size == 1) {
                val v1: EquationVariable = mLeftSide.get(0)
                val v2: EquationVariable = mRightSide.get(0)
                if (v1.isConstant && v2.isConstant &&
                    v1.amount!!.isNull && v2.amount!!.isNull
                ) {
                    return true
                }
            }
            return false
        }

    private enum class Type {
        EQUALS,
        LOWER_THAN,
        GREATER_THAN,
    }

    private var mType = Type.EQUALS
    private var mSystem: LinearSystem? = null

    /**
     * Copy constructor
     *
     * @param equation to copy
     */
    constructor(equation: LinearEquation) {
        val mLeftSide1: ArrayList<EquationVariable> = equation.mLeftSide
        run {
            var i = 0
            val mLeftSide1Size: Int = mLeftSide1.size
            while (i < mLeftSide1Size) {
                val v: EquationVariable = mLeftSide1.get(i)
                val v2 = EquationVariable(v)
                mLeftSide.add(v2)
                i++
            }
        }
        val mRightSide1: ArrayList<EquationVariable> = equation.mRightSide
        var i = 0
        val mRightSide1Size: Int = mRightSide1.size
        while (i < mRightSide1Size) {
            val v: EquationVariable = mRightSide1.get(i)
            val v2 = EquationVariable(v)
            mRightSide.add(v2)
            i++
        }
        mCurrentSide = mRightSide
    }

    /**
     * Insert the equation in the system
     */
    fun i() {
        if (mSystem == null) {
            return
        }
        val row = createRowFromEquation(
            mSystem!!,
            this,
        )
        mSystem!!.addConstraint(row)
    }

    /**
     * Set the current side to be the left side
     */
    fun setLeftSide() {
        mCurrentSide = mLeftSide
    }

    /**
     * Remove any terms on the left side of the equation
     */
    fun clearLeftSide() {
        mLeftSide.clear()
    }

    /**
     * Remove [EquationVariable] pointing to [SolverVariable]
     *
     * @param v the [SolverVariable] we want to remove from the equation
     */
    fun remove(v: SolverVariable) {
        var ev = find(v, mLeftSide)
        if (ev != null) {
            mLeftSide.remove(ev)
        }
        ev = find(v, mRightSide)
        if (ev != null) {
            mRightSide.remove(ev)
        }
    }

    /**
     * Base constructor, set the current side to the left side.
     */
    constructor() {
        mCurrentSide = mLeftSide
    }

    /**
     * Base constructor, set the current side to the left side.
     */
    constructor(system: LinearSystem?) {
        mCurrentSide = mLeftSide
        mSystem = system
    }

    /**
     * Set the current equation system for this equation
     *
     * @param system the equation system this equation belongs to
     */
    fun setSystem(system: LinearSystem?) {
        mSystem = system
    }

    /**
     * Set the equality operator for the equation, and switch the current side to the right side
     *
     * @return this
     */
    fun equalsTo(): LinearEquation {
        mCurrentSide = mRightSide
        return this
    }

    /**
     * Set the greater than operator for the equation, and switch the current side to the right side
     *
     * @return this
     */
    fun greaterThan(): LinearEquation {
        mCurrentSide = mRightSide
        mType = Type.GREATER_THAN
        return this
    }

    /**
     * Set the lower than operator for the equation, and switch the current side to the right side
     *
     * @return this
     */
    fun lowerThan(): LinearEquation {
        mCurrentSide = mRightSide
        mType = Type.LOWER_THAN
        return this
    }

    /**
     * Normalize the linear equation. If the equation is an equality, transforms it into
     * an equality, adding automatically slack or error variables.
     */
    fun normalize() {
        if (mType == Type.EQUALS) {
            return
        }
        mCurrentSide = mLeftSide
        if (mType == Type.LOWER_THAN) {
            withSlack(1)
        } else if (mType == Type.GREATER_THAN) {
            withSlack(-1)
        }
        mType = Type.EQUALS
        mCurrentSide = mRightSide
    }

    /**
     * Will simplify the equation per side -- regroup similar variables into one.
     * E.g. 2a + b + 3a = b - c will be turned into 5a + b = b - c.
     */
    fun simplify() {
        simplifySide(mLeftSide)
        simplifySide(mRightSide)
    }

    /**
     * Simplify an array of [EquationVariable]
     *
     * @param side Array of EquationVariable
     */
    private fun simplifySide(side: ArrayList<EquationVariable>) {
        var constant: EquationVariable? = null
        val variables: HashMap<String, EquationVariable> = HashMap<String, EquationVariable>()
        val variablesNames: ArrayList<String> = ArrayList<String>()
        run {
            var i = 0
            val sideSize: Int = side.size
            while (i < sideSize) {
                val v: EquationVariable = side.get(i)
                if (v.isConstant) {
                    if (constant == null) {
                        constant = v
                    } else {
                        constant!!.add(v)
                    }
                } else {
                    if (variables.containsKey(v.name)) {
                        val original: EquationVariable = variables.get(v.name)!!
                        original.add(v)
                    } else {
                        variables.put(v.name!!, v)
                        variablesNames.add(v.name!!)
                    }
                }
                i++
            }
        }
        side.clear()
        if (constant != null) {
            side.add(constant!!)
        }
        variablesNames.sort()
        var i = 0
        val variablesNamesSize: Int = variablesNames.size
        while (i < variablesNamesSize) {
            val name: String = variablesNames.get(i)
            val v: EquationVariable = variables.get(name)!!
            side.add(v)
            i++
        }
        removeNullTerms(side)
    }

    fun moveAllToTheRight() {
        var i = 0
        val mLeftSideSize: Int = mLeftSide.size
        while (i < mLeftSideSize) {
            val v: EquationVariable = mLeftSide.get(i)
            mRightSide.add(v.inverse())
            i++
        }
        mLeftSide.clear()
    }

    /**
     * Balance an equation to have only one term on the left side.
     * The preference is to first pick an unconstrained variable,
     * then a slack variable, then an error variable.
     */
    fun balance() {
        if (mLeftSide.size == 0 && mRightSide.size == 0) {
            return
        }
        mCurrentSide = mLeftSide
        run {
            var i = 0
            val mLeftSideSize: Int = mLeftSide.size
            while (i < mLeftSideSize) {
                val v: EquationVariable = mLeftSide.get(i)
                mRightSide.add(v.inverse())
                i++
            }
        }
        mLeftSide.clear()
        simplifySide(mRightSide)
        var found: EquationVariable? = null
        var i = 0
        val mRightSideSize: Int = mRightSide.size
        while (i < mRightSideSize) {
            val v: EquationVariable = mRightSide.get(i)
            if (v.type === SolverVariable.Type.UNRESTRICTED) {
                found = v
                break
            }
            i++
        }
        if (found == null) {
            var i = 0
            val mRightSideSize: Int = mRightSide.size
            while (i < mRightSideSize) {
                val v: EquationVariable = mRightSide.get(i)
                if (v.type === SolverVariable.Type.SLACK) {
                    found = v
                    break
                }
                i++
            }
        }
        if (found == null) {
            var i = 0
            val mRightSideSize: Int = mRightSide.size
            while (i < mRightSideSize) {
                val v: EquationVariable = mRightSide.get(i)
                if (v.type === SolverVariable.Type.ERROR) {
                    found = v
                    break
                }
                i++
            }
        }
        if (found == null) {
            return
        }
        mRightSide.remove(found)
        found.inverse()
        if (!found.amount!!.isOne) {
            val foundAmount = found.amount
            var i = 0
            val mRightSideSize: Int = mRightSide.size
            while (i < mRightSideSize) {
                val v: EquationVariable = mRightSide.get(i)
                v.amount!!.divide(foundAmount!!)
                i++
            }
            found.amount = Amount(1)
        }
        simplifySide(mRightSide)
        mLeftSide.add(found)
    }

    /**
     * Check the equation to possibly remove null terms
     */
    private fun removeNullTerms(list: ArrayList<EquationVariable>) {
        var hasNullTerm = false
        var i = 0
        val listSize: Int = list.size
        while (i < listSize) {
            val v: EquationVariable = list.get(i)
            if (v.amount!!.isNull) {
                hasNullTerm = true
                break
            }
            i++
        }
        if (hasNullTerm) {
            // if some elements are now zero, we need to remove them from the right side
            val newSide: ArrayList<EquationVariable>
            newSide = ArrayList<EquationVariable>()
            var i = 0
            val listSize: Int = list.size
            while (i < listSize) {
                val v: EquationVariable = list.get(i)
                if (!v.amount!!.isNull) {
                    newSide.add(v)
                }
                i++
            }
            list.clear()
            list.addAll(newSide)
        }
    }

    /**
     * Pivot this equation on the variable --
     * e.g. the variable will be the only term on the left side of the equation.
     *
     * @param variable variable pivoted on
     */
    fun pivot(variable: SolverVariable) {
        if (mLeftSide.size == 1 &&
            mLeftSide.get(0).solverVariable == variable
        ) {
            // no-op, we're already pivoted.
            return
        }
        run {
            var i = 0
            val mLeftSideSize: Int = mLeftSide.size
            while (i < mLeftSideSize) {
                val v: EquationVariable = mLeftSide.get(i)
                mRightSide.add(v.inverse())
                i++
            }
        }
        mLeftSide.clear()
        simplifySide(mRightSide)
        var found: EquationVariable? = null
        var i = 0
        val mRightSideSize: Int = mRightSide.size
        while (i < mRightSideSize) {
            val v: EquationVariable = mRightSide.get(i)
            if (v.solverVariable == variable) {
                found = v
                break
            }
            i++
        }
        if (found != null) {
            mRightSide.remove(found)
            found.inverse()
            if (!found.amount!!.isOne) {
                val foundAmount = found.amount
                var i = 0
                val mRightSideSize: Int = mRightSide.size
                while (i < mRightSideSize) {
                    val v: EquationVariable = mRightSide.get(i)
                    v.amount!!.divide(foundAmount!!)
                    i++
                }
                found.amount = Amount(1)
            }
            mLeftSide.add(found)
        }
    }

    /**
     * Returns true if the constant is negative
     *
     * @return true if the constant is negative.
     */
    fun hasNegativeConstant(): Boolean {
        var i = 0
        val mRightSideSize: Int = mRightSide.size
        while (i < mRightSideSize) {
            val v: EquationVariable = mRightSide.get(i)
            if (v.isConstant) {
                if (v.amount!!.isNegative) {
                    return true
                }
            }
            i++
        }
        return false
    }

    val constant: Amount?
        /**
         * If present, returns the constant on the right side of the equation.
         * The equation is expected to be balanced before using this function.
         *
         * @return The equation constant
         */
        get() {
            var i = 0
            val mRightSideSize: Int = mRightSide.size
            while (i < mRightSideSize) {
                val v: EquationVariable = mRightSide.get(i)
                if (v.isConstant) {
                    return v.amount
                }
                i++
            }
            return null
        }

    /**
     * Inverse the equation (multiply both left and right terms by -1)
     */
    fun inverse() {
        val amount = Amount(-1)
        run {
            var i = 0
            val mLeftSideSize: Int = mLeftSide.size
            while (i < mLeftSideSize) {
                val v: EquationVariable = mLeftSide.get(i)
                v.multiply(amount)
                i++
            }
        }
        var i = 0
        val mRightSideSize: Int = mRightSide.size
        while (i < mRightSideSize) {
            val v: EquationVariable = mRightSide.get(i)
            v.multiply(amount)
            i++
        }
    }

    val firstUnconstrainedVariable: EquationVariable?
        /**
         * Returns the first unconstrained variable encountered in this equation
         *
         * @return an unconstrained variable or null if none are found
         */
        get() {
            run {
                var i = 0
                val mLeftSideSize: Int = mLeftSide.size
                while (i < mLeftSideSize) {
                    val v: EquationVariable = mLeftSide.get(i)
                    if (v.type === SolverVariable.Type.UNRESTRICTED) {
                        return v
                    }
                    i++
                }
            }
            var i = 0
            val mRightSideSize: Int = mRightSide.size
            while (i < mRightSideSize) {
                val v: EquationVariable = mRightSide.get(i)
                if (v.type === SolverVariable.Type.UNRESTRICTED) {
                    return v
                }
                i++
            }
            return null
        }
    val leftVariable: EquationVariable?
        /**
         * Returns the basic variable of the equation
         *
         * @return basic variable
         */
        get() = if (mLeftSide.size == 1) {
            mLeftSide.get(0)
        } else {
            null
        }

    /**
     * Replace the variable v in this equation (left or right side)
     * by the right side of the equation l
     *
     * @param v the variable to replace
     * @param l the equation we use to replace it with
     */
    fun replace(v: SolverVariable, l: LinearEquation) {
        replace(v, l, mLeftSide)
        replace(v, l, mRightSide)
    }

    /**
     * Convenience function to replace the variable v possibly contained inside list
     * by the right side of the equation l
     *
     * @param v    the variable to replace
     * @param l    the equation we use to replace it with
     * @param list the list of [EquationVariable] to work on
     */
    private fun replace(
        v: SolverVariable,
        l: LinearEquation,
        list: ArrayList<EquationVariable>,
    ) {
        val toReplace = find(v, list)
        if (toReplace != null) {
            list.remove(toReplace)
            val amount = toReplace.amount
            val mRightSide1: ArrayList<EquationVariable> = l.mRightSide
            var i = 0
            val mRightSide1Size: Int = mRightSide1.size
            while (i < mRightSide1Size) {
                val lv: EquationVariable = mRightSide1.get(i)
                list.add(EquationVariable(amount, lv))
                i++
            }
        }
    }

    /**
     * Returns the [EquationVariable] associated to
     * the [SolverVariable] found in the
     * list of [EquationVariable]
     *
     * @param v    the variable to find
     * @param list list the list of [EquationVariable] to search in
     * @return the associated [EquationVariable]
     */
    private fun find(
        v: SolverVariable,
        list: ArrayList<EquationVariable>,
    ): EquationVariable? {
        var i = 0
        val listSize: Int = list.size
        while (i < listSize) {
            val ev: EquationVariable = list.get(i)
            if (ev.solverVariable == v) {
                return ev
            }
            i++
        }
        return null
    }

    val rightSide: ArrayList<EquationVariable>
        /**
         * Accessor for the right side of the equation.
         *
         * @return the equation's right side.
         */
        get() = mRightSide

    /**
     * Returns true if this equation contains a give variable
     *
     * @param solverVariable the variable we are looking for
     * @return true if found, false if not.
     */
    operator fun contains(solverVariable: SolverVariable): Boolean {
        if (find(solverVariable, mLeftSide) != null) {
            return true
        }
        return if (find(solverVariable, mRightSide) != null) {
            true
        } else {
            false
        }
    }

    /**
     * Returns the [EquationVariable] associated with a given
     * [SolverVariable] in this equation
     *
     * @param solverVariable the variable we are looking for
     * @return the [EquationVariable] associated if found, otherwise null
     */
    fun getVariable(solverVariable: SolverVariable): EquationVariable? {
        val variable = find(solverVariable, mRightSide)
        return variable ?: find(solverVariable, mLeftSide)
    }

    /**
     * Add a constant to the current side of the equation
     *
     * @param amount the value of the constant
     * @return this
     */
    fun `var`(amount: Int): LinearEquation {
        val e = EquationVariable(mSystem, amount)
        mCurrentSide!!.add(e)
        return this
    }

    /**
     * Add a fractional constant to the current side of the equation
     *
     * @param numerator   the value of the constant's numerator
     * @param denominator the value of the constant's denominator
     * @return this
     */
    fun `var`(numerator: Int, denominator: Int): LinearEquation {
        val e = EquationVariable(Amount(numerator, denominator))
        mCurrentSide!!.add(e)
        return this
    }

    /**
     * Add an unrestricted variable to the current side of the equation
     *
     * @param name the name of the variable
     * @return this
     */
    fun `var`(name: String?): LinearEquation {
        val e = EquationVariable(mSystem!!, name, SolverVariable.Type.UNRESTRICTED)
        mCurrentSide!!.add(e)
        return this
    }

    /**
     * Add an unrestricted variable to the current side of the equation
     *
     * @param amount the amount of the variable
     * @param name   the name of the variable
     * @return this
     */
    fun `var`(amount: Int, name: String?): LinearEquation {
        val e = EquationVariable(
            mSystem!!,
            amount,
            name,
            SolverVariable.Type.UNRESTRICTED,
        )
        mCurrentSide!!.add(e)
        return this
    }

    /**
     * Add an unrestricted fractional variable to the current side of the equation
     *
     * @param numerator   the value of the variable's numerator
     * @param denominator the value of the variable's denominator
     * @param name        the name of the variable
     * @return this
     */
    fun `var`(numerator: Int, denominator: Int, name: String?): LinearEquation {
        val amount = Amount(numerator, denominator)
        val e = EquationVariable(
            mSystem!!,
            amount,
            name,
            SolverVariable.Type.UNRESTRICTED,
        )
        mCurrentSide!!.add(e)
        return this
    }

    /**
     * Convenience function to add a variable, based on [var)][LinearEquation.var]
     *
     * @param name the variable's name
     * @return this
     */
    operator fun plus(name: String?): LinearEquation {
        `var`(name)
        return this
    }

    /**
     * Convenience function to add a variable, based on [var)][LinearEquation.var]
     *
     * @param amount the variable's amount
     * @param name   the variable's name
     * @return this
     */
    fun plus(amount: Int, name: String?): LinearEquation {
        `var`(amount, name)
        return this
    }

    /**
     * Convenience function to add a negative variable,
     * based on [var)][LinearEquation.var]
     *
     * @param name the variable's name
     * @return this
     */
    operator fun minus(name: String?): LinearEquation {
        `var`(-1, name)
        return this
    }

    /**
     * Convenience function to add a negative variable,
     * based on [var)][LinearEquation.var]
     *
     * @param amount the variable's amount
     * @param name   the variable's name
     * @return this
     */
    fun minus(amount: Int, name: String?): LinearEquation {
        `var`(-1 * amount, name)
        return this
    }

    /**
     * Convenience function to add a constant, based on [var)][LinearEquation.var]
     *
     * @param amount the constant's amount
     * @return this
     */
    operator fun plus(amount: Int): LinearEquation {
        `var`(amount)
        return this
    }

    /**
     * Convenience function to add a negative constant,
     * based on [var)][LinearEquation.var]
     *
     * @param amount the constant's amount
     * @return this
     */
    operator fun minus(amount: Int): LinearEquation {
        `var`(amount * -1)
        return this
    }

    /**
     * Convenience function to add a fractional constant,
     * based on [var)][LinearEquation.var]
     *
     * @param numerator   the value of the variable's numerator
     * @param denominator the value of the variable's denominator
     * @return this
     */
    fun plus(numerator: Int, denominator: Int): LinearEquation {
        `var`(numerator, denominator)
        return this
    }

    /**
     * Convenience function to add a negative fractional constant,
     * based on [var)][LinearEquation.var]
     *
     * @param numerator   the value of the constant's numerator
     * @param denominator the value of the constant's denominator
     * @return this
     */
    fun minus(numerator: Int, denominator: Int): LinearEquation {
        `var`(numerator * -1, denominator)
        return this
    }

    /**
     * Add an error variable to the current side
     *
     * @param name     the name of the error variable
     * @param strength the strength of the error variable
     * @return this
     */
    fun withError(name: String?, strength: Int): LinearEquation {
        val e = EquationVariable(
            mSystem!!,
            strength,
            name,
            SolverVariable.Type.ERROR,
        )
        mCurrentSide!!.add(e)
        return this
    }

    fun withError(amount: Amount?, name: String?): LinearEquation {
        val e = EquationVariable(mSystem!!, amount, name, SolverVariable.Type.ERROR)
        mCurrentSide!!.add(e)
        return this
    }

    /**
     * Add an error variable to the current side
     *
     * @return this
     */
    fun withError(): LinearEquation {
        val name = nextErrorVariableName
        withError("$name+", 1)
        withError("$name-", -1)
        return this
    }

    fun withPositiveError(): LinearEquation {
        val name = nextErrorVariableName
        withError("$name+", 1)
        return this
    }

    fun addArtificialVar(): EquationVariable {
        val e = EquationVariable(
            mSystem!!,
            1,
            nextArtificialVariableName,
            SolverVariable.Type.ERROR,
        )
        mCurrentSide!!.add(e)
        return e
    }

    /**
     * Add an error variable to the current side
     *
     * @param strength the strength of the error variable
     * @return this
     */
    fun withError(strength: Int): LinearEquation {
        withError(nextErrorVariableName, strength)
        return this
    }

    /**
     * Add a slack variable to the current side
     *
     * @param name     the name of the slack variable
     * @param strength the strength of the slack variable
     * @return this
     */
    fun withSlack(name: String?, strength: Int): LinearEquation {
        val e = EquationVariable(
            mSystem!!,
            strength,
            name,
            SolverVariable.Type.SLACK,
        )
        mCurrentSide!!.add(e)
        return this
    }

    fun withSlack(amount: Amount?, name: String?): LinearEquation {
        val e = EquationVariable(mSystem!!, amount, name, SolverVariable.Type.SLACK)
        mCurrentSide!!.add(e)
        return this
    }

    /**
     * Add a slack variable to the current side
     *
     * @return this
     */
    fun withSlack(): LinearEquation {
        withSlack(nextSlackVariableName, 1)
        return this
    }

    /**
     * Add a slack variable to the current side
     *
     * @param strength the strength of the slack variable
     * @return this
     */
    fun withSlack(strength: Int): LinearEquation {
        withSlack(nextSlackVariableName, strength)
        return this
    }

    /**
     * Override the toString() method to display the linear equation
     */
    override fun toString(): String {
        var result = ""
        result = sideToString(mLeftSide)
        result += when (mType) {
            Type.EQUALS -> {
                "= "
            }

            Type.LOWER_THAN -> {
                "<= "
            }

            Type.GREATER_THAN -> {
                ">= "
            }
        }
        result += sideToString(mRightSide)
        return result.trim { it <= ' ' }
    }

    /**
     * Returns a string representation of an array of [EquationVariable]
     *
     * @param side array of [EquationVariable]
     * @return a String representation of the array of variables
     */
    private fun sideToString(side: ArrayList<EquationVariable>): String {
        var result = ""
        var first = true
        var i = 0
        val sideSize: Int = side.size
        while (i < sideSize) {
            val v: EquationVariable = side.get(i)
            if (first) {
                result += if (v.amount!!.isPositive) {
                    "$v "
                } else {
                    v.signString() + " " + v + " "
                }
                first = false
            } else {
                result += v.signString() + " " + v + " "
            }
            i++
        }
        if (side.size == 0) {
            result = "0"
        }
        return result
    }

    companion object {
        private var sArtificialIndex = 0
        private var sSlackIndex = 0
        private var sErrorIndex = 0
        val nextArtificialVariableName: String
            get() = "a" + ++sArtificialIndex
        val nextSlackVariableName: String
            get() = "s" + ++sSlackIndex
        val nextErrorVariableName: String
            get() = "e" + ++sErrorIndex

        /**
         * Reset the counters for the automatic slack and error variable naming
         */
        fun resetNaming() {
            sArtificialIndex = 0
            sSlackIndex = 0
            sErrorIndex = 0
        }

        /**
         * Transform a LinearEquation into a Row
         *
         * @param e linear equation
         * @return a Row object
         */
        fun createRowFromEquation(linearSystem: LinearSystem, e: LinearEquation): ArrayRow {
            e.normalize()
            e.moveAllToTheRight()
            // Let's build a row from the LinearEquation
            val row = linearSystem.createRow()
            val eq: ArrayList<EquationVariable> = e.rightSide
            val count: Int = eq.size
            for (i in 0 until count) {
                val v: EquationVariable = eq.get(i)
                val sv = v.solverVariable
                if (sv != null) {
                    val previous = row.variables!![sv]
                    row.variables!!.put(sv, previous + v.amount!!.toFloat())
                } else {
                    row.mConstantValue = v.amount!!.toFloat()
                }
            }
            return row
        }
    }
}
