// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.buildlogic

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.file.Directory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest

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

                val generateKarmaConfig = registerKarmaConfig()
                js { browser { testTask { useKarmaConfig(generateKarmaConfig) } } }
                wasmJs { browser { testTask { useKarmaConfig(generateKarmaConfig) } } }
                excludeFloatSensitiveTestsFromJs()
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
                            compilerOptions.freeCompilerArgs.addAll(
                                "-Xexpect-actual-classes",
                                // Casts the compiler can already prove will always throw. In a
                                // mechanical Java-to-Kotlin port these are never a style question:
                                // Java's `(float) x` narrows, Kotlin's `x as Float` throws, and the
                                // two read almost alike. That is how #342 shipped a
                                // ClassCastException in ArcCurveFit — reported as a warning nobody
                                // read.
                                //
                                // Promoting these two and nothing else is deliberate. Blanket
                                // `allWarningsAsErrors` would be the wrong tool: of the 74 warnings
                                // on this tree, the ones checked against the Java original turned
                                // out to be upstream's own redundancy made visible by Kotlin's
                                // types — dead null checks on fields Java initialises at their
                                // declaration. Silencing those would mean diverging from the
                                // original in 74 places, and line-by-line comparison against
                                // upstream is how defects get found here.
                                "-Xwarning-level=NUMERIC_CAST_NEVER_SUCCEEDS_BUT_CAN_BE_REPLACED_WITH_TO_CALL:error",
                                "-Xwarning-level=CAST_NEVER_SUCCEEDS:error",
                            )
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
 * Karma runs the browser tests under Mocha, whose per-test timeout defaults to 2 seconds. That is
 * far too tight for the solver tests that run thousands of layout passes (`RandomLayoutTest`), so
 * they time out on `js` and `wasmJs` while passing everywhere else. Karma reads no timeout from
 * Gradle; it has to come from a config snippet, which this task generates.
 *
 * The snippet mutates `config` in place rather than calling `config.set({ client: ... })`, because
 * a wholesale `client` replacement would drop the arguments the Kotlin Gradle plugin passes through
 * it — among them the `--tests` filters.
 */
private fun Project.registerKarmaConfig(): TaskProvider<*> {
    val target = karmaConfigDir.map { it.file("mocha-timeout.js") }
    return tasks.register("generateKarmaConfig") {
        group = "verification"
        description = "Generates the Karma config snippet shared by the js and wasmJs test tasks."
        outputs.file(target)
        doLast {
            target.get().asFile.writeText(
                """
                // Generated by the constraintlayout-library convention plugin. Do not edit.
                config.client = config.client || {};
                config.client.mocha = config.client.mocha || {};
                config.client.mocha.timeout = $KARMA_MOCHA_TIMEOUT_MS;

                """.trimIndent(),
            )
        }
    }
}

/**
 * Points a browser test task at the generated snippet, and makes sure it is generated first.
 *
 * `useKarma` installs a fresh framework instance, dropping the browser the `browser { }` default
 * had selected — hence the explicit `useChromeHeadless()`, which is the one it used to pick.
 */
private fun KotlinJsTest.useKarmaConfig(generate: TaskProvider<*>) {
    val configDir = project.karmaConfigDir.get().asFile
    dependsOn(generate)
    useKarma {
        useChromeHeadless()
        useConfigDirectory(configDir)
    }
}

private val Project.karmaConfigDir: Provider<Directory>
    get() = layout.buildDirectory.dir("karma.config.d")

private const val KARMA_MOCHA_TIMEOUT_MS = 120_000

/**
 * Drops [JS_FLOAT_SENSITIVE_TESTS] from the `js` test task, and from that task only.
 *
 * Applied here rather than inside `js { browser { testTask { … } } }` because the receiver that
 * block exposes does not carry Gradle's `filter`. Matching on the task name is what keeps `wasmJs`
 * out of it: the two tasks are `jsBrowserTest` and `wasmJsBrowserTest`, and only the former starts
 * with "js".
 */
private fun Project.excludeFloatSensitiveTestsFromJs() {
    tasks.withType(KotlinJsTest::class.java).configureEach {
        if (!name.startsWith("js")) return@configureEach
        JS_FLOAT_SENSITIVE_TESTS.forEach { pattern -> filter.excludeTestsMatching(pattern) }
    }
}

/**
 * Tests excluded from the `js` target only.
 *
 * Kotlin/JS has no 32-bit `Float` — it is a JS Number, that is, a double — and these twenty tests
 * depend on the difference in one of two ways. Some compare rendered text: `1.0f.toString()` is
 * "1.0" on every other target and "1" on JS, so `DslTest` reads `horizontalWeight:1` where it
 * expects `horizontalWeight:1.0`. The rest compare computed geometry, where a double's rounding
 * accumulates until a laid-out edge moves by a pixel, or an ASCII plot of a trajectory diverges.
 *
 * `wasmJs` has a true f32 and passes all twenty, so it is deliberately left unfiltered.
 *
 * These are upstream's own tests, kept near-verbatim. Excluding them here rather than annotating
 * them keeps those files byte-identical to the original, which is what makes comparing them against
 * upstream worth doing — and that comparison is how every translation defect in this port has been
 * found so far.
 *
 * The patterns lead with `*` because `commonTest` is relocated into the shaded flavours, where the
 * same classes live under `tech.annexflow.constraintlayout`.
 */
private val JS_FLOAT_SENSITIVE_TESTS =
    listOf(
        "*.DslTest.testBarrier02",
        "*.DslTest.testConstraint02",
        "*.DslTest.testConstraint03",
        "*.DslTest.testHChain03",
        "*.DslTest.testVChain03",
        "*.LinearSystemTest.testAddEquation1",
        "*.LinearSystemTest.testAddEquation2",
        "*.MotionArcCurveTest.arcTest3",
        "*.MotionTransitionTest.testTransitionJson",
        "*.MotionTransitionTest.testTransitionJson2",
        "*.MotionTransitionTest.testTransitionOnSwipe1",
        "*.RatioTest.testChainRatio4",
        "*.RatioTest.testNestedRatio2",
        "*.StopLogicTest.accelerateCruseDecelerate",
        "*.StopLogicTest.accelerateDecelerate",
        "*.StopLogicTest.backwardAccelerateCruseDecelerate",
        "*.StopLogicTest.backwardAccelerateDecelerate",
        "*.StopLogicTest.basicSpring",
        "*.StopLogicTest.cruseDecelerate",
        "*.StopLogicTest.hardStop",
    )

/**
 * The `android { }` block of a Kotlin Multiplatform library. Build scripts reach it through a
 * generated accessor, which does not exist inside a binary plugin; AGP registers the target as an
 * extension of the Kotlin extension, so look it up by type instead.
 */
private fun KotlinMultiplatformExtension.android(configure: KotlinMultiplatformAndroidLibraryTarget.() -> Unit) =
    (this as ExtensionAware).extensions.configure(configure)

private fun VersionCatalog.library(alias: String) = findLibrary(alias).orElseThrow().get()

private fun VersionCatalog.version(alias: String) = findVersion(alias).orElseThrow().requiredVersion
