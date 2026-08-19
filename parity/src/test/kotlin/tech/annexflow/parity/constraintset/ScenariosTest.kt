// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.parity.constraintset

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScenariosTest {
    @Test
    fun aSeedReproducesTheSameDocument() {
        assertEquals(Scenarios.generate(7), Scenarios.generate(7))
    }

    @Test
    fun differentSeedsDiffer() {
        val specs = (1L..50L).map { Scenarios.generate(it) }
        assertTrue(specs.distinct().size > 40, "generator is barely varying: ${specs.distinct().size}/50")
    }

    @Test
    fun everyAnchorTargetNamesAWidgetThatExists() {
        for (seed in 1L..200L) {
            val spec = Scenarios.generate(seed)
            val ids = spec.widgets.map { it.id }.toSet() +
                spec.guidelines.map { it.id } + spec.barriers.map { it.id } + spec.chains.map { it.id }
            for (w in spec.widgets) {
                for (a in w.anchors) {
                    val target = a.target
                    if (target is AnchorTarget.Widget) {
                        assertTrue(target.id in ids, "seed $seed: ${w.id} points at missing ${target.id}")
                    }
                }
            }
        }
    }

    @Test
    fun theGeneratorReachesEveryDimensionForm() {
        val forms = (1L..300L).flatMap { Scenarios.generate(it).widgets }
            .flatMap { listOf(it.width, it.height) }
            .map { it::class.simpleName }
            .toSet()
        assertEquals(
            setOf("Fixed", "Mode", "Percent", "Ratio", "Bounded"),
            forms,
            "some dimension form is never generated",
        )
    }
}
