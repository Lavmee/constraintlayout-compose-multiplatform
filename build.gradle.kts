// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

// Declared here, applied by the convention plugins in `build-logic`. `apply false` puts each on
// the build classpath without applying it to the root project, which is what lets a convention
// plugin reference it by id.
plugins {
    alias(libs.plugins.multiplatform).apply(false)
    alias(libs.plugins.compose).apply(false)
    alias(libs.plugins.compose.compiler).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.kotlin.library).apply(false)
    alias(libs.plugins.maven.publish).apply(false)
    alias(libs.plugins.jvm).apply(false)
}

group = "tech.annexflow.compose"

// The release workflow passes `-PconstraintlayoutVersion=<tag>`; everything else — a local build, a
// snapshot publish, CI — takes the default. A distinct property name rather than `-Pversion`, which
// collides with Gradle's own `project.version` handling and is easy to set by accident.
//
// The default is a snapshot on purpose: the publish plugin routes a `-SNAPSHOT` version to the
// snapshot repository and performs no Portal release, so a publish that somehow runs without the
// property cannot release anything by accident.
version = providers.gradleProperty("constraintlayoutVersion").getOrElse("0.9.0-SNAPSHOT")

extra.apply {
    set("jvmTarget", "11")
}

subprojects {
    group = rootProject.group
    version = rootProject.version
}
