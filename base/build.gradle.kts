import java.util.*

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    alias(libs.plugins.kotlin.jvm)

    // Apply the java-library plugin for API and implementation separation.
    `java-library`
}

version = project.artifactVersion

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use the Kotlin JUnit 5 integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    // Use the JUnit 5 integration.
    testImplementation(libs.junit.jupiter.engine)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // This dependency is exported to consumers, that is to say found on their compile classpath.
    api(libs.commons.math3)

    // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    implementation(libs.guava)
}

//java {
//    toolchain {
//        languageVersion = JavaLanguageVersion.of(21)
//    }
//}

tasks.named<Test>("test") { useJUnitPlatform() }

tasks.register<DefaultTask>("displayBaseProperties") {
    doFirst {
        project.artifactVersion
            .run { "BASE VERSION : $this" }
            .run(::println)
    }
}

val Project.artifactVersion: String
    get() = Properties().apply {
        "artifact.version.key"
            .run(properties::get)
            .let {
                "user.home"
                    .run(System::getProperty)
                    .run { "$this$it" }
            }.run(::File)
            .inputStream()
            .use(::load)
    }.get("artifact.version")
        .toString()

