// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.solver

/**
 * Everything observable about one layout pass, normalised so the two implementations become
 * comparable despite living in different packages. Deliberately the same shape as `ParseOutcome`.
 */
sealed interface LayoutOutcome {
    /**
     * @param geometry every widget's position and size plus the root's final size. The root belongs
     *   in here because wrap-content makes its size an output rather than an input, and that is
     *   exactly where the minimum-size defect in #336 lived.
     */
    data class LaidOut(val geometry: String) : LayoutOutcome

    /**
     * An exception escaped `layout()` or `measure()`. Compared by portable category, not exception
     * class: the port is multiplatform and cannot raise JVM-specific exception classes on Native or
     * JS, so class equality would demand something no correct port could deliver.
     */
    data class Leaked(val category: String) : LayoutOutcome

    data class Crashed(val error: String) : LayoutOutcome

    companion object {
        fun categorise(throwable: Throwable): String =
            when (throwable) {
                is IndexOutOfBoundsException -> "IndexOutOfBounds"
                is NullPointerException -> "NullPointer"
                is ArithmeticException -> "Arithmetic"
                else -> throwable::class.simpleName ?: "Unknown"
            }
    }
}
