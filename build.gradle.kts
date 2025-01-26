plugins {
    alias(libs.plugins.multiplatform).apply(false)
    alias(libs.plugins.compose).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library).apply(false)
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
            ktlint().editorConfigOverride(
                mapOf(
                    "standard:no-consecutive-comments" to "disabled",
                    "ktlint_standard_property-naming" to "disabled",
                    "ktlint_standard_function-naming" to "disabled",
                    "ktlint_standard_no-empty-file" to "disabled",
                ),
            )
        }

        kotlinGradle {
            target("**/*.kts")
            targetExclude("${layout.buildDirectory}/**/*.kts")
            ktlint()
        }
    }
}

extra.apply {
    set("jvmTarget", "1.8")
}
