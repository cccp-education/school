import Build_gradle.Constants.arrowKtVersion
import Build_gradle.Constants.commonsIoVersion
import Build_gradle.Constants.jacksonVersion
import Build_gradle.Constants.jgitVersion
import Build_gradle.Constants.langchain4jVersion
import Build_gradle.Constants.testcontainersVersion

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    alias(libs.plugins.kotlin.jvm)

    // Apply the java-library plugin for API and implementation separation.
    `java-library`

    application
}

repositories {
    google()
    mavenCentral()
//    gradlePluginPortal()
//    maven("https://repo.gradle.org/gradle/libs-releases/")
//    maven("https://plugins.gradle.org/m2/")
//    maven("https://maven.xillio.com/artifactory/libs-release/")
//    maven("https://mvnrepository.com/repos/springio-plugins-release")
    maven("https://archiva-repository.apache.org/archiva/repository/public/")
}
object Constants {
    const val langchain4jVersion = "0.35.0"
    const val testcontainersVersion = "1.20.1"
    const val asciidoctorGradleVersion = "4.0.0-alpha.1"
    const val commonsIoVersion = "2.13.0"
    const val jacksonVersion = "2.17.2"//2.18.0
    const val arrowKtVersion = "1.2.4"
    const val jgitVersion = "6.10.0.202406032230-r"
    const val schoolVersion = "0.0.1"
}
//TODO: add validator dep
dependencies {
    setOf(
        "com.google.apis:google-api-services-forms:v1-rev20220908-2.0.0",
        "com.google.apis:google-api-services-drive:v3-rev197-1.25.0",
        "com.google.api-client:google-api-client-jackson2:2.3.0",
        "com.google.auth:google-auth-library-oauth2-http:1.23.0",
        "org.jetbrains.kotlin:kotlin-stdlib",
        "commons-io:commons-io:$commonsIoVersion",
        "jakarta.xml.bind:jakarta.xml.bind-api:4.0.2",
        "com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion",
        "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion",
        "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion",
        "com.fasterxml.jackson.module:jackson-module-jsonSchema:$jacksonVersion",
        "org.eclipse.jgit:org.eclipse.jgit:$jgitVersion",
        "org.eclipse.jgit:org.eclipse.jgit.archive:$jgitVersion",
        "org.eclipse.jgit:org.eclipse.jgit.ssh.jsch:$jgitVersion",
        "org.tukaani:xz:1.9",
        "io.arrow-kt:arrow-core:$arrowKtVersion",
        "io.arrow-kt:arrow-fx-coroutines:$arrowKtVersion",
        "io.arrow-kt:arrow-integrations-jackson-module:0.14.1",
        "org.apache.poi:poi-ooxml:5.2.5",
        "org.slf4j:slf4j-simple:2.0.16",
        "org.asciidoctor:asciidoctorj-diagram:2.3.1",
//        "com.burgstaller:okhttp-digest:1.10",
        "io.github.rburgst:okhttp-digest:3.1.1",
        "org.ysb33r.gradle:grolifant:0.12.1",
        "dev.langchain4j:langchain4j:$langchain4jVersion",
        "dev.langchain4j:langchain4j-ollama:$langchain4jVersion",
        "org.testcontainers:testcontainers:$testcontainersVersion",
        "org.testcontainers:ollama:$testcontainersVersion",
        // This dependency is used internally, and not exposed to consumers on their own compile classpath.
        libs.guava,
    ).forEach(::implementation)
    setOf(
        // Use the Kotlin JUnit 5 integration.
        "org.jetbrains.kotlin:kotlin-test-junit5",
        // Use the JUnit 5 integration.
        libs.junit.jupiter.engine,
        // Swing test
        "org.assertj:assertj-swing:3.17.1",
        // Kotlin-JUnit5
        "org.jetbrains.kotlin:kotlin-test",
        "org.jetbrains.kotlin:kotlin-test-junit5",
//        "io.projectreactor:reactor-test",
        // Mock
        "org.mockito.kotlin:mockito-kotlin:${properties["mockito_kotlin_version"]}",
        "org.mockito:mockito-junit-jupiter:${properties["mockito_jupiter.version"]}",
        "io.mockk:mockk:${properties["mockk.version"]}",
    ).forEach(::testImplementation)
    testImplementation("org.wiremock:wiremock:${properties["wiremock.version"]}") {
        exclude(module = "commons-fileupload")
    }
    setOf("org.junit.platform:junit-platform-launcher")
        .forEach(::testRuntimeOnly)
    setOf("com.sun.xml.bind:jaxb-impl:4.0.5")
        .forEach(::runtimeOnly)
    setOf(
        // This dependency is exported to consumers, that is to say found on their compile classpath.
        libs.commons.math3
    ).forEach(::api)
}

tasks.named<Test>("test") { useJUnitPlatform() }

application.mainClass.set("workspace.Installer")