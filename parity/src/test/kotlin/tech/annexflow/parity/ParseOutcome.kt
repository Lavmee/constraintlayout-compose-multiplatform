// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity

/**
 * Everything observable about one parse, normalised so the two implementations become comparable
 * despite living in different packages.
 */
sealed interface ParseOutcome {
    /**
     * @param structure a depth-first rendering of the tree — type, bounds and `content()` per
     *   element. Without it the comparison would see only the serialiser's output, and divergences
     *   confined to element internals would pass unnoticed.
     */
    data class Parsed(val formattedJson: String, val structure: String) : ParseOutcome

    /**
     * The parser rejected the input through its own contract. Compared exactly, message included:
     * these strings are the library's public error reporting.
     */
    data class Failed(val reason: String) : ParseOutcome

    /**
     * Something other than [Failed] escaped the parser — an implementation detail leaking, not a
     * decision it made. Both implementations do this on truncated input, where `content()` reads
     * past the buffer instead of raising a parse error.
     *
     * Only the [category] is compared, not the exception class. The JVM raises
     * `StringIndexOutOfBoundsException` from `new String(char[], int, int)` while Kotlin raises a
     * plain `IndexOutOfBoundsException` from `concatToString`; the port is multiplatform and cannot
     * reproduce JVM-specific exception classes on Native or JS at all. Demanding class equality
     * would be demanding something no correct port could deliver, while the category still catches
     * one side leaking where the other does not.
     */
    data class Leaked(val category: String) : ParseOutcome

    /**
     * Kept apart from [Leaked] because deeply nested input can exhaust the stack, and a stack
     * overflow on one side against a clean parse error on the other is a finding, not noise.
     */
    data class Crashed(val error: String) : ParseOutcome

    companion object {
        /** Collapses platform-specific exception classes onto a portable category. */
        fun categorise(throwable: Throwable): String =
            when (throwable) {
                is IndexOutOfBoundsException -> "IndexOutOfBounds"
                is NullPointerException -> "NullPointer"
                is NumberFormatException -> "NumberFormat"
                else -> throwable::class.simpleName ?: "Unknown"
            }
    }
}
