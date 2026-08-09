// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.buildlogic

import org.gradle.api.Project
import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/** Every source set of `:compose` that holds hand-written sources. */
internal val SHADED_SOURCE_SETS =
    listOf(
        "commonMain",
        "commonTest",
        "androidMain",
        "jvmCommonMain",
        "jvmMain",
        "nonAndroid",
        "nativeMain",
        "jsMain",
        "wasmJsMain",
    )

/** Directory the shaded flavours are generated from, relative to the root project. */
private const val ORIGIN_SOURCE_DIR = "compose/src"

/**
 * Registers one `relocate<SourceSet>` task per source set, each copying `:compose` sources while
 * rewriting [rules] both in file contents and in directory names, and wires the generated trees
 * into [kotlin] as source directories.
 */
internal fun Project.registerShadedSources(
    kotlin: KotlinMultiplatformExtension,
    rules: Map<String, String>,
) {
    val originSrc = rootProject.layout.projectDirectory.dir(ORIGIN_SOURCE_DIR)
    val generatedRoot = layout.buildDirectory.dir("generated/shaded")
    val pathRules = rules.map { (from, to) -> from.replace('.', '/') to to.replace('.', '/') }

    SHADED_SOURCE_SETS.forEach { sourceSet ->
        val relocate =
            tasks.register<Sync>("relocate${sourceSet.replaceFirstChar(Char::uppercaseChar)}") {
                group = "shading"
                description = "Generates the $sourceSet sources by rewriting :compose package names."
                // The rules only reach Gradle through the closures below, so declare them
                // explicitly — otherwise a changed rule would not invalidate the generated output.
                inputs.property("shadeRules", rules)

                from(originSrc.dir("$sourceSet/kotlin")) {
                    filteringCharset = Charsets.UTF_8.name()
                    filter { line: String ->
                        rules.entries.fold(line) { acc, (from, to) -> acc.replace(from, to) }
                    }
                    eachFile {
                        path = pathRules.fold(path) { acc, (from, to) -> acc.replace(from, to) }
                    }
                }
                into(generatedRoot.map { it.dir("$sourceSet/kotlin") })
                // `eachFile` only renames files; without this the emptied `androidx/**` directories
                // would survive next to the rewritten ones.
                includeEmptyDirs = false
            }

        kotlin.sourceSets.getByName(sourceSet).kotlin.srcDir(relocate)
    }
}
