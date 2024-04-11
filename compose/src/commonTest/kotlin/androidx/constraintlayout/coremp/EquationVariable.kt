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
 * EquationVariable is used to represent a variable in a [LinearEquation]
 */
class EquationVariable {
    /**
     * Accessor to retrieve the amount associated with this variable
     *
     * @return amount
     */
    /**
     * Accessor to set the amount associated with this variable
     *
     * @param amount the amount associated with this variable
     */
    var amount: Amount? = null

    /**
     * Accessor for the [SolverVariable]
     *
     * @return the [SolverVariable]
     */
    var solverVariable: SolverVariable? = null
        private set

    /**
     * Base constructor
     *
     * @param system the [linear system][LinearSystem] this equation variable belongs to
     * @param amount the amount associated with this variable
     * @param name   the variable name
     * @param type   the variable type
     */
    constructor(
        system: LinearSystem,
        amount: Amount?,
        name: String?,
        type: SolverVariable.Type?,
    ) {
        this.amount = amount
        solverVariable = system.getVariable(name, type)
    }

    /**
     * Alternate constructor, will set the type to be [CONSTANT][SolverVariable.Type]
     *
     * @param amount the amount associated with this variable
     */
    constructor(amount: Amount?) {
        this.amount = amount
    }

    /**
     * Alternate constructor, will construct an amount given an integer number
     *
     * @param system the [linear system][LinearSystem] this equation variable belongs to
     * @param amount the amount associated with this variable
     * @param name   the variable name
     * @param type   the variable type
     */
    constructor(
        system: LinearSystem,
        amount: Int,
        name: String?,
        type: SolverVariable.Type?,
    ) {
        this.amount = Amount(amount)
        solverVariable = system.getVariable(name, type)
    }

    /**
     * Alternate constructor, will set the type to be [CONSTANT][SolverVariable.Type]
     *
     * @param system the [linear system][LinearSystem] this equation variable belongs to
     * @param amount the amount associated with this variable
     */
    constructor(system: LinearSystem?, amount: Int) {
        this.amount = Amount(amount)
    }

    /**
     * Alternate constructor, will set the factor to be one by default
     *
     * @param system the [linear system][LinearSystem] this equation variable belongs to
     * @param name   the variable name
     * @param type   the variable type
     */
    constructor(system: LinearSystem, name: String?, type: SolverVariable.Type?) {
        amount = Amount(1)
        solverVariable = system.getVariable(name, type)
    }

    /**
     * Alternate constructor, will multiply an amount to a given [EquationVariable]
     *
     * @param amount   the amount given
     * @param variable the variable we'll multiply
     */
    constructor(amount: Amount?, variable: EquationVariable) {
        this.amount = Amount(amount!!)
        amount.multiply(variable.amount!!)
        solverVariable = variable.solverVariable
    }

    /**
     * Copy constructor
     *
     * @param v variable to copy
     */
    constructor(v: EquationVariable) {
        amount = Amount(v.amount!!)
        solverVariable = v.solverVariable
    }

    val name: String?
        /**
         * Accessor for the variable's name
         *
         * @return the variable's name
         */
        get() = if (solverVariable == null) {
            null
        } else {
            solverVariable!!.getName()
        }
    val type: SolverVariable.Type?
        /**
         * Accessor for the variable's type
         *
         * @return the variable's type
         */
        get() = if (solverVariable == null) {
            SolverVariable.Type.CONSTANT
        } else {
            solverVariable!!.mType
        }
    val isConstant: Boolean
        /**
         * Returns true if this is a constant
         *
         * @return true if a constant
         */
        get() = solverVariable == null

    /**
     * Inverse the current amount (from negative to positive or the reverse)
     *
     * @return this
     */
    fun inverse(): EquationVariable {
        amount!!.inverse()
        return this
    }

    /**
     * Returns true if the variables are isCompatible (same type, same name)
     *
     * @param variable another variable to compare this one to
     * @return true if isCompatible.
     */
    fun isCompatible(variable: EquationVariable): Boolean {
        if (isConstant) {
            return variable.isConstant
        } else if (variable.isConstant) {
            return false
        }
        return variable.solverVariable == solverVariable
    }

    /**
     * Add an amount from another variable to this variable
     *
     * @param variable variable added
     */
    fun add(variable: EquationVariable) {
        if (variable.isCompatible(this)) {
            amount!!.add(variable.amount!!)
        }
    }

    /**
     * Subtract an amount from another variable to this variable
     *
     * @param variable variable added
     */
    fun subtract(variable: EquationVariable) {
        if (variable.isCompatible(this)) {
            amount!!.subtract(variable.amount!!)
        }
    }

    /**
     * Multiply an amount from another variable to this variable
     *
     * @param variable variable multiplied
     */
    fun multiply(variable: EquationVariable) {
        multiply(variable.amount!!)
    }

    /**
     * Multiply this variable by a given amount
     *
     * @param amount specified amount multiplied
     */
    fun multiply(amount: Amount) {
        this.amount!!.multiply(amount)
    }

    /**
     * Divide an amount from another variable to this variable
     *
     * @param variable variable dividing
     */
    fun divide(variable: EquationVariable) {
        amount!!.divide(variable.amount!!)
    }

    /**
     * Override the toString() method to display the variable
     */
    override fun toString(): String {
        if (isConstant) {
            return "" + amount
        }
        return if (amount!!.isOne || amount!!.isMinusOne) {
            "" + solverVariable
        } else {
            "" + amount + " " + solverVariable
        }
    }

    /**
     * Returns a string displaying the sign of the variable (positive or negative, e.g. + or -)
     *
     * @return sign of the variable as a string, either + or -
     */
    fun signString(): String {
        return if (amount!!.isPositive) {
            "+"
        } else {
            "-"
        }
    }
}
