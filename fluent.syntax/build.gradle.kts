group = "org.projectfluent"
version = "0.1"

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    alias(libs.plugins.kotlin)

    // Apply the java-library plugin for API and implementation separation.
    `java-library`

    // Add dokka to be able to generate documentation.
    alias(libs.plugins.dokka)
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    mavenCentral()
}

dependencies {
    // Align versions of all Kotlin components
//    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 7 standard library.
//    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7")

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation(libs.junit)

    // Use Klaxon for tests.
    testImplementation(libs.klaxon)
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
