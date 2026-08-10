// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity

import androidx.constraintlayout.core.parser.CLParser
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Proves the vendored oracle compiles and runs before anything is built on top of it. If this fails,
 * the problem is the module wiring, not the comparison.
 */
class OracleSmokeTest {
    @Test
    fun oracleParsesAndSerialises() {
        val root = CLParser.parse("{ test: ['hello', 'world'] }")
        assertEquals(
            """
            {
              test: ['hello', 'world']
            }
            """.trimIndent(),
            root.toFormattedJSON(),
        )
    }
}
