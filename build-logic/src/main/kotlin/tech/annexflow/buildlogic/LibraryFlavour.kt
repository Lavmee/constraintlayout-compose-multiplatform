// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.buildlogic

import org.gradle.api.Project

/**
 * One publishable flavour of the ConstraintLayout Compose Multiplatform library, described by the
 * `constraintlayout.*` properties in the module's own `gradle.properties` — the same place the
 * `POM_*` properties already live.
 *
 * A flavour with empty [shadeRules] compiles its own `src/` tree; that is `:compose`, the single
 * place where sources are edited. A flavour with non-empty [shadeRules] owns no sources at all: its
 * whole source tree is generated from `:compose` by rewriting package names, so every published
 * flavour stays in lockstep with `:compose` without a long-lived branch to merge.
 */
internal data class LibraryFlavour(
    /** Android namespace. Must be unique across every module of the build. */
    val androidNamespace: String,
    /** `baseName` of the produced iOS framework. Must be unique across every module of the build. */
    val frameworkBaseName: String,
    /** Package prefixes to rewrite while generating sources, e.g. `androidx.foo` -> `tech.annexflow.foo`. */
    val shadeRules: Map<String, String>,
) {
    companion object {
        fun of(project: Project): LibraryFlavour =
            LibraryFlavour(
                androidNamespace = project.requiredProperty("constraintlayout.androidNamespace"),
                frameworkBaseName = project.requiredProperty("constraintlayout.frameworkBaseName"),
                shadeRules = parseShadeRules(project.findProperty("constraintlayout.shadeRules") as String?),
            )

        /** Parses `from=to,from=to`; blank or absent means "not a shaded flavour". */
        private fun parseShadeRules(raw: String?): Map<String, String> =
            raw.orEmpty()
                .split(',')
                .map(String::trim)
                .filter(String::isNotEmpty)
                .associate { rule ->
                    val (from, to) = rule.split('=', limit = 2).also {
                        require(it.size == 2) { "Malformed constraintlayout.shadeRules entry: '$rule', expected 'from=to'" }
                    }
                    from.trim() to to.trim()
                }
    }
}

private fun Project.requiredProperty(name: String): String =
    requireNotNull(findProperty(name) as String?) { "$path is missing the '$name' property in its gradle.properties" }
