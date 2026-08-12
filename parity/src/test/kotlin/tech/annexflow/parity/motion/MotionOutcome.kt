// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.motion

/**
 * Everything observable about one motion run, normalised so the two implementations become
 * comparable despite living in different packages. Deliberately the same shape as `LayoutOutcome`.
 */
internal sealed interface MotionOutcome {
    data class Sampled(val snapshot: String) : MotionOutcome

    /**
     * An exception escaped `Motion`. Compared by portable category, not exception class: the port
     * is multiplatform and cannot raise JVM-specific exception classes on Native or JS, so class
     * equality would demand something no correct port could deliver.
     */
    data class Leaked(val category: String) : MotionOutcome

    data class Crashed(val error: String) : MotionOutcome

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
