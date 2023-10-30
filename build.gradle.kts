plugins {
    alias(libs.plugins.multiplatform).apply(false)
    alias(libs.plugins.compose).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.kotlin.serialization).apply(false)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.spotless)
}

allprojects {
    apply(plugin = "com.diffplug.spotless")

    spotless {
        kotlin {
            target("**/*.kt")
            targetExclude("${layout.buildDirectory}/**/*.kt")
            targetExclude("bin/**/*.kt")
            ktlint()
        }

        kotlinGradle {
            target("**/*.kts")
            targetExclude("${layout.buildDirectory}/**/*.kts")
            ktlint()
        }
    }
}
