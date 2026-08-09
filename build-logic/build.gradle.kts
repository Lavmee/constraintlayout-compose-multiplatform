// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

plugins {
    `kotlin-dsl`
}

// The plugins are `compileOnly` here and put on the build classpath by the root project, which
// declares all of them with `apply false`.
dependencies {
    compileOnly(libs.gradlePlugin.android)
    compileOnly(libs.gradlePlugin.compose)
    compileOnly(libs.gradlePlugin.composeCompiler)
    compileOnly(libs.gradlePlugin.kotlin)
    compileOnly(libs.gradlePlugin.mavenPublish)
}

gradlePlugin {
    plugins {
        register("constraintlayoutLibrary") {
            id = "tech.annexflow.constraintlayout-library"
            implementationClass = "tech.annexflow.buildlogic.ConstraintLayoutLibraryPlugin"
        }
        register("constraintlayoutPublish") {
            id = "tech.annexflow.constraintlayout-publish"
            implementationClass = "tech.annexflow.buildlogic.ConstraintLayoutPublishPlugin"
        }
    }
}
