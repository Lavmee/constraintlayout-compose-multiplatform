// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.buildlogic

import org.gradle.api.Project

/**
 * One publishable flavour of the ConstraintLayout Compose Multiplatform library, described by the
 * `constraintlayout.*` properties in the module's own `gradle.properties` — the only per-flavour
 * configuration there is, since everything else about a flavour is shared by definition.
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
    /** Maven artifact id. Deliberately not the module name — `:compose` publishes as `constraintlayout-compose-multiplatform`. */
    val artifactId: String,
    /** POM `<name>`. */
    val pomName: String,
    /** POM `<description>`. The one line a consumer reads to tell the flavours apart. */
    val pomDescription: String,
    /** Package prefixes to rewrite while generating sources, e.g. `androidx.foo` -> `tech.annexflow.foo`. */
    val shadeRules: Map<String, String>,
) {
    companion object {
        fun of(project: Project): LibraryFlavour =
            LibraryFlavour(
                androidNamespace = project.requiredProperty("constraintlayout.androidNamespace"),
                frameworkBaseName = project.requiredProperty("constraintlayout.frameworkBaseName"),
                artifactId = project.requiredProperty("constraintlayout.artifactId"),
                pomName = project.requiredProperty("constraintlayout.pomName"),
                pomDescription = project.requiredProperty("constraintlayout.pomDescription"),
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
