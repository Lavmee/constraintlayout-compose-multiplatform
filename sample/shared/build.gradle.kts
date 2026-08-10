import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.kotlin.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

val extraJvmTarget = rootProject.extra.get("jvmTarget") as String

kotlin {
    applyDefaultHierarchyTemplate()

    android {
        namespace = "tech.annexflow.sample.shared"
        compileSdk { version = release(37) }
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(extraJvmTarget))
        }
    }

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(extraJvmTarget))
        }
    }

    js {
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    macosArm64 {
        binaries {
            executable {
                entryPoint = "main"
                freeCompilerArgs +=
                    listOf(
                        "-linker-option",
                        "-framework",
                        "-linker-option",
                        "Metal",
                    )
            }
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.compose.runtime)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.material3)
            implementation(project(":compose"))
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

// The package of the generated `Res` class defaults to `<project.group>.<project.name>`, and the
// root build script hands every subproject the publishing group. That coupling is invisible from
// here: changing the group renames this package and breaks the imports in `commonMain`, which is
// exactly what happened in 17be42a. Pin the package so publishing coordinates cannot move it.
compose.resources {
    packageOfResClass = "tech.annexflow.sample.shared.generated.resources"
}

compose.desktop.nativeApplication {
    targets(kotlin.targets.getByName("macosArm64"))
    distributions {
        targetFormats(TargetFormat.Dmg)
        packageName = "ConstraintLayoutSample"
        packageVersion = "1.0.0"
    }
}
