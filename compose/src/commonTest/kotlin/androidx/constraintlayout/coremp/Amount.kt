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
 * Represents the amount of a given [variable][EquationVariable], can be fractional.
 */
class Amount {
    /**
     * Accessor for the numerator
     *
     * @return the numerator
     */
    var numerator = 0
        private set

    /**
     * Accessor for the denominator
     *
     * @return the denominator
     */
    var denominator = 1
        private set

    /**
     * Base constructor, set the numerator and denominator.
     *
     * @param numerator   the numerator
     * @param denominator the denominator
     */
    constructor(numerator: Int, denominator: Int) {
        this.numerator = numerator
        this.denominator = denominator
        simplify()
    }

    /**
     * Alternate constructor, set the numerator, with the denominator set to one.
     *
     * @param numerator the amount's value
     */
    constructor(numerator: Int) {
        this.numerator = numerator
        denominator = 1
    }

    constructor(amount: Amount) {
        numerator = amount.numerator
        denominator = amount.denominator
        simplify()
    }

    /**
     * Set the numerator and denominator directly
     *
     * @param numerator   numerator
     * @param denominator denominator
     */
    operator fun set(numerator: Int, denominator: Int) {
        this.numerator = numerator
        this.denominator = denominator
        simplify()
    }

    /**
     * Add an amount to the current one.
     *
     * @param amount amount to add
     * @return this
     */
    fun add(amount: Amount): Amount {
        if (denominator == amount.denominator) {
            numerator += amount.numerator
        } else {
            numerator = numerator * amount.denominator + amount.numerator * denominator
            denominator *= amount.denominator
        }
        simplify()
        return this
    }

    /**
     * Add an integer amount
     *
     * @param amount amount to add
     * @return this
     */
    fun add(amount: Int): Amount {
        numerator += amount * denominator
        return this
    }

    /**
     * Subtract an amount to the current one.
     *
     * @param amount amount to subtract
     * @return this
     */
    fun subtract(amount: Amount): Amount {
        if (denominator == amount.denominator) {
            numerator -= amount.numerator
        } else {
            numerator = numerator * amount.denominator - amount.numerator * denominator
            denominator *= amount.denominator
        }
        simplify()
        return this
    }

    /**
     * Multiply an amount with the current one.
     *
     * @param amount amount to multiply by
     * @return this
     */
    fun multiply(amount: Amount): Amount {
        numerator *= amount.numerator
        denominator *= amount.denominator
        simplify()
        return this
    }

    /**
     * Divide the current amount by the given amount.
     *
     * @param amount amount to divide by
     * @return this
     */
    fun divide(amount: Amount): Amount {
        val preN = numerator
        val preD = denominator
        numerator *= amount.denominator
        denominator *= amount.numerator
        simplify()
        return this
    }

    /**
     * Inverse the current amount as a fraction (e.g. a/b becomes b/a)
     *
     * @return this
     */
    fun inverseFraction(): Amount {
        val n = numerator
        numerator = denominator
        denominator = n
        simplify()
        return this
    }

    /**
     * Inverse the current amount (positive to negative or negative to positive)
     *
     * @return this
     */
    fun inverse(): Amount {
        numerator *= -1
        simplify()
        return this
    }

    /**
     * Override equals method
     *
     * @param o compared object
     * @return true if the compared object is equals to this one (same numerator and denominator)
     */
    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o !is Amount) {
            return false
        }
        val a = o
        return numerator == a.numerator && denominator == a.denominator
    }

    /**
     * Simplify the current amount. If the amount is fractional,
     * we calculate the GCD and divide numerator and denominator by it.
     * If both numerator and denominator are negative, turns things back
     * to positive. If only the denominator is negative, make it positive
     * and make the numerator negative instead.
     */
    private fun simplify() {
        if (numerator < 0 && denominator < 0) {
            numerator *= -1
            denominator *= -1
        } else if (numerator >= 0 && denominator < 0) {
            numerator *= -1
            denominator *= -1
        }
        if (denominator > 1) {
            val commonDenominator: Int
            commonDenominator = if (denominator == 2 && numerator % 2 == 0) {
                2
            } else {
                gcd(numerator, denominator)
            }
            numerator /= commonDenominator
            denominator /= commonDenominator
        }
    }

    val isOne: Boolean
        /**
         * Returns true if the Amount is equals to one
         *
         * @return true if the Amount is equals to one
         */
        get() = numerator == 1 && denominator == 1
    val isMinusOne: Boolean
        /**
         * Returns true if the Amount is equals to minus one
         *
         * @return true if the Amount is equals to minus one
         */
        get() = numerator == -1 && denominator == 1
    val isPositive: Boolean
        /**
         * Returns true if the Amount is positive.
         *
         * @return true if the Amount is positive.
         */
        get() = numerator >= 0 && denominator >= 0
    val isNegative: Boolean
        /**
         * Returns true if the Amount is negative.
         *
         * @return true if the Amount is negative.
         */
        get() = numerator < 0
    val isNull: Boolean
        /**
         * Returns true if the value is zero
         *
         * @return true if the value is zero
         */
        get() = numerator == 0

    /**
     * Set the Amount to zero.
     */
    fun setToZero() {
        numerator = 0
        denominator = 1
    }

    /**
     * Returns the float value of the Amount
     *
     * @return the float value
     */
    fun toFloat(): Float {
        return if (denominator >= 1) {
            numerator / denominator.toFloat()
        } else {
            0f
        }
    }

    /**
     * Override the toString() method to display the amount (possibly as a fraction)
     *
     * @return formatted string
     */
    override fun toString(): String {
        if (denominator == 1) {
            if (numerator == 1 || numerator == -1) {
                return ""
            }
            return if (numerator < 0) {
                "" + numerator * -1
            } else {
                "" + numerator
            }
        }
        return if (numerator < 0) {
            "" + numerator * -1 + "/" + denominator
        } else {
            "" + numerator + "/" + denominator
        }
    }

    fun valueString(): String {
        return if (denominator == 1) {
            "" + numerator
        } else {
            "" + numerator + "/" + denominator
        }
    }

    companion object {
        /**
         * Iterative Binary GCD algorithm
         *
         * @param u first number
         * @param v second number
         * @return Greater Common Divisor
         */
        private fun gcd(u: Int, v: Int): Int {
            var u = u
            var v = v
            var shift: Int
            if (u < 0) {
                u *= -1
            }
            if (v < 0) {
                v *= -1
            }
            if (u == 0) {
                return v
            }
            if (v == 0) {
                return u
            }
            shift = 0
            while (u or v and 1 == 0) {
                u = u shr 1
                v = v shr 1
                shift++
            }
            while (u and 1 == 0) {
                u = u shr 1
            }
            do {
                while (v and 1 == 0) {
                    v = v shr 1
                }
                if (u > v) {
                    val t = v
                    v = u
                    u = t
                }
                v -= u
            } while (v != 0)
            return u shl shift
        }
    }
}
