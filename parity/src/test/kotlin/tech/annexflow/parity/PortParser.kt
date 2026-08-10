// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity

import tech.annexflow.constraintlayout.core.parser.CLContainer
import tech.annexflow.constraintlayout.core.parser.CLElement
import tech.annexflow.constraintlayout.core.parser.CLParser
import tech.annexflow.constraintlayout.core.parser.CLParsingException

/**
 * The ported parser, taken from the shaded flavour so its packages do not collide with the oracle's.
 *
 * Deliberately a near-duplicate of [OracleParser]: these two files are the only place in the module
 * where the package split exists, and reading one should not require the other. The single
 * syntactic difference is that the port declares `get(index)` as an `operator`.
 */
object PortParser : ParserSubject {
    override val name: String = "port"

    override fun parse(input: String): ParseOutcome =
        try {
            val root = CLParser.parse(input)
            ParseOutcome.Parsed(
                formattedJson = root.toFormattedJSON(),
                structure = buildString { describe(root, 0, this) },
            )
        } catch (e: CLParsingException) {
            ParseOutcome.Failed("CLParsingException", e.reason())
        } catch (e: Exception) {
            ParseOutcome.Failed(e::class.simpleName.orEmpty(), e.message.orEmpty())
        } catch (e: StackOverflowError) {
            ParseOutcome.Crashed("StackOverflowError")
        } catch (e: OutOfMemoryError) {
            ParseOutcome.Crashed("OutOfMemoryError")
        }

    private fun describe(element: CLElement, depth: Int, out: StringBuilder) {
        out.append("  ".repeat(depth))
            .append(element::class.simpleName)
            .append(" [").append(element.start).append(", ").append(element.end).append("] <<")
            .append(element.content())
            .append(">>\n")
        if (element is CLContainer) {
            for (i in 0 until element.size()) {
                describe(element[i], depth + 1, out)
            }
        }
    }
}
