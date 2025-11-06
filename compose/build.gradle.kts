import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.kotlin.library)
    alias(libs.plugins.maven.publish)
}

val extraJvmTarget = rootProject.extra.get("jvmTarget") as String

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
    kotlin.applyDefaultHierarchyTemplate()

    androidLibrary {
        namespace = "androidx.constraintlayout.compose"
        compileSdk = 36
        minSdk = 21
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(extraJvmTarget))
        }
    }
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(extraJvmTarget))
        }
    }

    js(IR) {
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

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

    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                }
            }
        }
    }

    sourceSets {
        all {
            languageSettings {
                optIn("kotlin.experimental.ExperimentalNativeApi")
            }
        }
        val commonMain by getting {
            dependencies {
                implementation(compose.ui)
                implementation(compose.uiUtil)
                implementation(compose.foundation)
                implementation(compose.runtime)
                implementation(libs.annotation)
                implementation(libs.collection)
                implementation(kotlin("reflect"))
            }
        }

        val nonAndroid by creating {
            dependsOn(commonMain)
        }

        val jvmCommonMain by creating {
            dependsOn(commonMain)
        }

        val jvmMain by getting {
            dependsOn(jvmCommonMain)
            dependsOn(nonAndroid)
        }
        val androidMain by getting {
            dependsOn(jvmCommonMain)
        }

        val nativeMain by getting {
            dependsOn(nonAndroid)
        }

        val wasmJsMain by getting {
            dependsOn(nonAndroid)
        }

        val jsMain by getting {
            dependsOn(nonAndroid)
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
