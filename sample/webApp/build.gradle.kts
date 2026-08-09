import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "sample.js"
            }
        }

        binaries.executable()
    }

    sourceSets {
        wasmJsMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(project(":sample:shared"))
        }
    }
}
