// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

// Differential harness. Compares the ported parser against a vendored copy of the upstream Java
// one. Deliberately not published and not on the library's `assemble` path: it exists to be run,
// not shipped, so it applies neither convention plugin.
//
// It depends on the *shaded* flavour on purpose. `:compose` is itself in package
// `androidx.constraintlayout` — the oracle's package — so the two could never share a classpath.
// `:compose-shaded` relocates everything to `tech.annexflow.constraintlayout`, which is what makes
// this comparison possible at all.
plugins {
    alias(libs.plugins.jvm)
}

kotlin {
    jvmToolchain((rootProject.extra.get("jvmTarget") as String).toInt())
}

sourceSets {
    // The oracle is plain Java depending only on java.util, so it compiles exactly as upstream
    // compiles it. Keeping it in its own source set is what lets the vendored files stay untouched.
    val oracle by creating

    named("test") {
        compileClasspath += oracle.output
        runtimeClasspath += oracle.output
    }
}

dependencies {
    "oracleCompileOnly"(libs.jspecify)
    testImplementation(project(":compose-shaded"))
    testImplementation(kotlin("test"))
}
