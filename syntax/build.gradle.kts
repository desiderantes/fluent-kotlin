import org.jetbrains.kotlin.gradle.dsl.JvmTarget

group = "org.projectfluent"
version = "0.1"

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    alias(libs.plugins.kotlin.multiplatform)

    //alias(libs.plugins.android.library)
    // Add dokka to be able to generate documentation.
    alias(libs.plugins.dokka)
    id("module.publication")
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    mavenCentral()
}

kotlin {
    jvm {
        withJava()
        compilerOptions {
            jvmTarget.set(libs.versions.java.map(JvmTarget::fromTarget))
        }

        java {
            sourceCompatibility = JavaVersion.toVersion(libs.versions.java.get())
            targetCompatibility = JavaVersion.toVersion(libs.versions.java.get())
        }
    }
    linuxX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlin.codepoints)
                implementation(libs.kotlin.reflect)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val jvmTest by getting {
            dependencies {
                // Use the Kotlin JUnit integration.
                implementation(libs.junit)

                // Use Klaxon for tests.
                implementation(libs.klaxon)
            }

        }
    }
}
