import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

val extraJvmTarget = rootProject.extra.get("jvmTarget") as String

android {
    namespace = "tech.annexflow.sample"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
        targetSdk = 37

        applicationId = "tech.annexflow.sample.androidApp"
        versionCode = 1
        versionName = "1.0.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(extraJvmTarget)
        targetCompatibility = JavaVersion.toVersion(extraJvmTarget)
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(extraJvmTarget))
    }
}

dependencies {
    implementation(project(":sample:shared"))
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activityCompose)
}
