import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.library)
    alias(libs.plugins.maven.publish)
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    targetHierarchy.default()

    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
    jvm("desktop")

    macosArm64()
    macosX64()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "compose"
            isStatic = true
        }
    }

    configure(targets) {
        if (this is KotlinNativeTarget && konanTarget.family.isAppleFamily) {
            compilations.getByName("main") {
                val objc by cinterops.creating {
                    defFile(project.file("src/iosMain/def/objc.def"))
                }
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.ui)
                implementation(libs.ui.util)
                implementation(libs.kotlinx.serialization.json)
                implementation(compose.foundation)
                implementation(compose.runtime)
                implementation(libs.annotation)
                implementation(libs.collection)
                implementation(libs.kotlin.stdlib)
                implementation(kotlin("reflect"))
            }
        }
        val androidMain by getting
        val desktopMain by getting

        val jvmMain by creating {
            dependsOn(commonMain)
        }

        androidMain.dependsOn(jvmMain)
        desktopMain.dependsOn(jvmMain)

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

android {
    namespace = "androidx.constraintlayout.compose"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
