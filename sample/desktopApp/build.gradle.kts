import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

val extraJvmTarget = rootProject.extra.get("jvmTarget") as String

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(extraJvmTarget))
        }
    }

    sourceSets {
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(project(":sample:shared"))
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "tech.annexflow.sample.desktopApp"
            packageVersion = "1.0.0"
        }
    }
}
