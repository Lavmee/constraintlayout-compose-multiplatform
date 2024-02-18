import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import java.util.Properties

// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.library)
    id("maven-publish")
    id("signing")
}

group = "tech.annexflow.compose"
version = "0.3.0-rc02"

kotlin {
    kotlin.applyDefaultHierarchyTemplate()

    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions {
                jvmTarget = rootProject.extra.get("jvmTarget") as String
            }
        }
    }
    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget = rootProject.extra.get("jvmTarget") as String
            }
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

    configure(targets) {
        if (this is KotlinNativeTarget && konanTarget.family.isAppleFamily) {
            compilations.getByName("main") {
                val objc by cinterops.creating {
                    defFile(project.file("src/iosMain/def/objc.def"))
                }
            }
        }
    }

    targets.configureEach {
        compilations.configureEach {
            compilerOptions.configure {
                freeCompilerArgs.add("-Xexpect-actual-classes")
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
                implementation(libs.ui.util)
                implementation(compose.foundation)
                implementation(compose.runtime)
                implementation(compose.components.resources)
                implementation(libs.kotlin.stdlib)
                implementation(kotlin("reflect"))
            }
        }

        val jvmCommonMain by creating {
            dependsOn(commonMain)
        }

        val jvmMain by getting {
            dependsOn(jvmCommonMain)
        }
        val androidMain by getting {
            dependsOn(jvmCommonMain)
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

android {
    namespace = "androidx.constraintlayout.compose"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(rootProject.extra.get("jvmTarget") as String)
        targetCompatibility = JavaVersion.toVersion(rootProject.extra.get("jvmTarget") as String)
    }
}

extra.apply {
    val publishPropFile = rootProject.file("publish.properties")
    if (publishPropFile.exists()) {
        Properties().apply {
            load(publishPropFile.inputStream())
        }.forEach { name, value ->
            if (name == "signing.secretKeyRingFile") {
                set(name.toString(), rootProject.file(value.toString()).absolutePath)
            } else {
                set(name.toString(), value)
            }
        }
    } else {
        set("signing.keyId", System.getenv("SIGNING_KEY_ID"))
        set("signing.password", System.getenv("SIGNING_PASSWORD"))
        set("signing.secretKeyRingFile", System.getenv("SIGNING_SECRET_KEY_RING_FILE"))
        set("ossrhUsername", System.getenv("OSSRH_USERNAME"))
        set("ossrhPassword", System.getenv("OSSRH_PASSWORD"))
    }
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}
// https://github.com/gradle/gradle/issues/26091
val signingTasks = tasks.withType<Sign>()
tasks.withType<AbstractPublishToMaven>().configureEach {
    dependsOn(signingTasks)
}

publishing {
    if (rootProject.file("publish.properties").exists()) {
        signing {
            sign(publishing.publications)
        }
        repositories {
            maven {
                val releasesRepoUrl =
                    "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                val snapshotsRepoUrl =
                    "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                url =
                    if (version.toString().endsWith("SNAPSHOT")) {
                        uri(snapshotsRepoUrl)
                    } else {
                        uri(releasesRepoUrl)
                    }
                credentials {
                    username = project.ext.get("ossrhUsername").toString()
                    password = project.ext.get("ossrhPassword").toString()
                }
            }
        }
    }

    publications.withType<MavenPublication> {
        artifact(javadocJar)
        pom {
            artifactId = "constraintlayout-compose-multiplatform"
            name.set("Compose ConstraintLayout")
            description.set("Provides Compose ConstraintLayout for building responsive UIs")
            url.set("https://github.com/lavmee/constraintlayout-compose-multiplatform/")
            inceptionYear.set("2023")

            licenses {
                license {
                    name.set("The Apache Software License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("lavmee")
                    name.set("Sergei Gagarin")
                    email.set("lavmeejetski@gmail.com")
                }
            }
            scm {
                url.set("scm:git:git://github.com/lavmee/constraintlayout-compose-multiplatform.git")
                connection.set("scm:git:git://github.com/lavmee/constraintlayout-compose-multiplatform.git")
                developerConnection.set("scm:git:git://github.com/lavmee/constraintlayout-compose-multiplatform.git")
            }
        }
    }
}
