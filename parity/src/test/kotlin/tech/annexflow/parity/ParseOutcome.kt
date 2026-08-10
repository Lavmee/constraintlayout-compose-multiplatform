// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity

/**
 * Everything observable about one parse, normalised so the two implementations become comparable
 * despite living in different packages.
 *
 * [Crashed] is separate from [Failed] on purpose: deeply nested input can exhaust the stack, and a
 * stack overflow on one side against a clean parse error on the other is a finding, not noise.
 */
sealed interface ParseOutcome {
    /**
     * @param structure a depth-first rendering of the tree — type, bounds and `content()` per
     *   element. Without it the comparison would see only the serialiser's output, and divergences
     *   confined to element internals would pass unnoticed.
     */
    data class Parsed(val formattedJson: String, val structure: String) : ParseOutcome

    data class Failed(val exception: String, val message: String) : ParseOutcome

    data class Crashed(val error: String) : ParseOutcome
}
