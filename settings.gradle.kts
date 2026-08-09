rootProject.name = "constraintlayout-compose-multiplatform"
pluginManagement {
    includeBuild("build-logic")

    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

include(":sample:androidApp")
include(":sample:desktopApp")
include(":sample:webApp")
include(":sample:shared")

// The library is published in three flavours. Only `:compose` has sources; the two shaded flavours
// generate theirs from it at build time — see `build-logic`.
include(":compose")
include(":compose-shaded")
include(":compose-shaded-compose")
