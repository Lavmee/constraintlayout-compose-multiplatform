// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.buildlogic

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

private const val ANDROID_COMPILE_SDK = 37
private const val ANDROID_MIN_SDK = 21

/**
 * Configures one flavour of the ConstraintLayout Compose Multiplatform library. Every published
 * flavour shares this single definition of targets, source-set hierarchy and dependencies; they
 * differ only in the [LibraryFlavour] read from the module's own `gradle.properties`.
 */
@OptIn(ExperimentalWasmDsl::class)
class ConstraintLayoutLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            listOf(
                "org.jetbrains.kotlin.multiplatform",
                "com.android.kotlin.multiplatform.library",
                "org.jetbrains.compose",
                "org.jetbrains.kotlin.plugin.compose",
                "com.vanniktech.maven.publish",
            ).forEach(pluginManager::apply)

            val flavour = LibraryFlavour.of(this)
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
            val jvmTarget = JvmTarget.fromTarget(rootProject.extensions.extraProperties.get("jvmTarget") as String)

            extensions.configure<KotlinMultiplatformExtension> {
                applyDefaultHierarchyTemplate()

                android {
                    namespace = flavour.androidNamespace
                    compileSdk = ANDROID_COMPILE_SDK
                    minSdk = ANDROID_MIN_SDK
                    compilerOptions { this.jvmTarget.set(jvmTarget) }
                    withHostTest {}
                }

                jvm { compilerOptions { this.jvmTarget.set(jvmTarget) } }
                js { browser() }
                wasmJs { browser() }
                macosArm64()

                listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
                    iosTarget.binaries.framework {
                        baseName = flavour.frameworkBaseName
                        isStatic = true
                    }
                }

                targets.configureEach {
                    compilations.configureEach {
                        compileTaskProvider.configure {
                            compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")
                        }
                    }
                }

                configureSourceSets(libs)

                if (flavour.shadeRules.isNotEmpty()) {
                    registerShadedSources(this, flavour.shadeRules)
                }
            }
        }
    }

    private fun KotlinMultiplatformExtension.configureSourceSets(libs: VersionCatalog) = with(sourceSets) {
        configureEach {
            languageSettings { optIn("kotlin.experimental.ExperimentalNativeApi") }
        }

        val commonMain = getByName("commonMain")
        commonMain.dependencies {
            implementation(libs.library("compose-ui"))
            implementation(libs.library("compose-ui-util"))
            implementation(libs.library("compose-foundation"))
            implementation(libs.library("compose-runtime"))
            implementation(libs.library("annotation"))
            implementation(libs.library("collection"))
            implementation("org.jetbrains.kotlin:kotlin-reflect:${libs.version("kotlin")}")
        }

        // Two extra source sets on top of the default hierarchy: everything that is not Android
        // (`nonAndroid`) and everything that runs on a JVM, Android included (`jvmCommonMain`).
        val nonAndroid = create("nonAndroid").apply { dependsOn(commonMain) }
        val jvmCommonMain = create("jvmCommonMain").apply { dependsOn(commonMain) }

        getByName("jvmMain").apply {
            dependsOn(jvmCommonMain)
            dependsOn(nonAndroid)
        }
        getByName("androidMain").dependsOn(jvmCommonMain)
        getByName("nativeMain").dependsOn(nonAndroid)
        getByName("wasmJsMain").dependsOn(nonAndroid)
        getByName("jsMain").dependsOn(nonAndroid)

        getByName("commonTest").dependencies {
            implementation("org.jetbrains.kotlin:kotlin-test:${libs.version("kotlin")}")
        }
    }
}

/**
 * The `android { }` block of a Kotlin Multiplatform library. Build scripts reach it through a
 * generated accessor, which does not exist inside a binary plugin; AGP registers the target as an
 * extension of the Kotlin extension, so look it up by type instead.
 */
private fun KotlinMultiplatformExtension.android(configure: KotlinMultiplatformAndroidLibraryTarget.() -> Unit) =
    (this as ExtensionAware).extensions.configure(configure)

private fun VersionCatalog.library(alias: String) = findLibrary(alias).orElseThrow().get()

private fun VersionCatalog.version(alias: String) = findVersion(alias).orElseThrow().requiredVersion
