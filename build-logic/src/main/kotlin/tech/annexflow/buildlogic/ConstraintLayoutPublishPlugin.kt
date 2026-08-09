// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package tech.annexflow.buildlogic

import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SourcesJar
import java.io.File
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.configure

/**
 * Everything about how a library flavour reaches Maven Central: coordinates, POM, signing, and the
 * local staging repository the release workflow inspects before it uploads anything.
 *
 * Separate from [ConstraintLayoutLibraryPlugin] because the two answer different questions — that
 * one is how a flavour is *built*, this one is how it is *shipped* — and because the sample
 * modules are built and never shipped.
 */
class ConstraintLayoutPublishPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // The `.base` plugin, not `com.vanniktech.maven.publish`: the non-base one configures
            // itself from SONATYPE_* / POM_* / RELEASE_SIGNING_ENABLED properties and picks a
            // platform on its own, which would leave the real configuration split between this
            // file and two gradle.properties. The base plugin configures nothing until asked, so
            // what is below is the whole of it.
            pluginManager.apply("com.vanniktech.maven.publish.base")

            val flavour = LibraryFlavour.of(this)

            extensions.configure<MavenPublishBaseExtension> {
                // Publishes every Kotlin target plus the shared metadata module.
                //
                // An empty javadoc jar, which is all Maven Central requires — it checks that the
                // file is there, not what is in it. Generating real documentation instead is a
                // one-word change here, but the plugin attaches the javadoc jar to all eight
                // publications of a flavour, so it was measured at 91 MB of a 152 MB release
                // against 61 MB of actual library. Sources are published, and those carry the
                // KDoc.
                configure(
                    KotlinMultiplatform(
                        javadocJar = JavadocJar.Empty(),
                        sourcesJar = SourcesJar.Sources(),
                    ),
                )

                coordinates(
                    groupId = project.group as String,
                    artifactId = flavour.artifactId,
                    version = project.version as String,
                )

                pom {
                    name.set(flavour.pomName)
                    description.set(flavour.pomDescription)
                    url.set(PROJECT_URL)
                    inceptionYear.set("2023")
                    licenses {
                        license {
                            name.set("The Apache Software License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                            distribution.set("repo")
                        }
                    }
                    developers {
                        developer {
                            id.set("lavmee")
                            name.set("Sergei Gagarin")
                        }
                    }
                    scm {
                        url.set(PROJECT_URL)
                        connection.set("scm:git:$PROJECT_URL.git")
                        developerConnection.set("scm:git:$PROJECT_URL.git")
                    }
                }

                // Credentials come from the plugin's standard Gradle properties or environment
                // variables — ORG_GRADLE_PROJECT_mavenCentralUsername / …Password /
                // …signingInMemoryKey — so nothing secret is committed here.
                //
                // signAllPublications() wires the signing plugin up; whether a missing key is
                // fatal is then Gradle's call, and it turns on the version. For a release version
                // signing is required, so a tag pushed without the GPG_KEY secret dies at the
                // first sign task. For a -SNAPSHOT it is not: the sign tasks are SKIPPED and the
                // build succeeds having produced a complete, entirely unsigned set of artifacts.
                //
                // That second case is precisely the workflow_dispatch rehearsal — the run whose
                // whole job is to tell you the secrets are in place before a tag depends on them.
                // Which is why `scripts/check-staged-release.sh` counts signatures itself instead
                // of trusting the build's exit code.
                publishToMavenCentral()
                signAllPublications()
            }

            // A local Maven repository the release workflow publishes to before it publishes to
            // Central. Publishing here is file copying with no network, so it costs almost
            // nothing, and it buys the two things a release needs and Maven Central will not tell
            // you: what the release weighs, and whether each root module actually references every
            // target.
            //
            // The second is the failure this arrangement exists to prevent. Three of the seven
            // targets are Apple ones, so a publish from a host without Xcode produces root modules
            // that are structurally valid and missing iOS and macOS — a green build, a successful
            // upload, and consumers who find out. `scripts/check-staged-release.sh` reads what
            // lands here and refuses that.
            //
            // All three flavours stage into one directory under the root build dir so the script
            // can check the release as a whole rather than a flavour at a time. Addressed through
            // `rootDir` rather than the root project's model, which a subproject must not reach
            // into.
            extensions.configure<PublishingExtension> {
                repositories.maven {
                    name = "localStaging"
                    url = File(rootDir, "build/localStaging").toURI()
                }
            }
        }
    }

    private companion object {
        /**
         * The project's home. Referenced by the POM's url and scm entries, so it is declared once
         * here rather than repeated three times.
         */
        const val PROJECT_URL = "https://github.com/Lavmee/constraintlayout-compose-multiplatform"
    }
}
