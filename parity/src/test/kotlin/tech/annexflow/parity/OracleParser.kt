// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity

import androidx.constraintlayout.core.parser.CLContainer
import androidx.constraintlayout.core.parser.CLElement
import androidx.constraintlayout.core.parser.CLParser
import androidx.constraintlayout.core.parser.CLParsingException

/**
 * The vendored upstream parser. It defines correct behaviour for every comparison in this module —
 * where the two disagree, the port is wrong until proven otherwise.
 */
object OracleParser : ParserSubject {
    override val name: String = "oracle"

    override fun parse(input: String): ParseOutcome =
        try {
            val root = CLParser.parse(input)
            ParseOutcome.Parsed(
                formattedJson = root.toFormattedJSON(),
                structure = buildString { describe(root, 0, this) },
            )
        } catch (e: CLParsingException) {
            ParseOutcome.Failed(e.reason())
        } catch (e: Exception) {
            ParseOutcome.Leaked(ParseOutcome.categorise(e))
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
                describe(element.get(i), depth + 1, out)
            }
        }
    }
}
