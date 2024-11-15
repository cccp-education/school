import Build_gradle.Constants.arrowKtVersion
import Build_gradle.Constants.asciidoctorGradleVersion
import Build_gradle.Constants.commonsIoVersion
import Build_gradle.Constants.jacksonVersion
import Build_gradle.Constants.jgitVersion
import Build_gradle.Constants.langchain4jVersion
import Build_gradle.Constants.testcontainersVersion
import Build_gradle.Constants.schoolVersion
//import org.gradle.api.logging.LogLevel.ERROR
//import org.gradle.api.logging.LogLevel.INFO

plugins {
    `kotlin-dsl`
    application
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.gradle.org/gradle/libs-releases/")
    maven("https://plugins.gradle.org/m2/")
    maven("https://maven.xillio.com/artifactory/libs-release/")
    maven("https://mvnrepository.com/repos/springio-plugins-release")
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

dependencies {
    try {
        setOf(
            "com.google.apis:google-api-services-forms:v1-rev20220908-2.0.0",
            "com.google.apis:google-api-services-drive:v3-rev197-1.25.0",
            "com.google.api-client:google-api-client-jackson2:2.3.0",
            "com.google.auth:google-auth-library-oauth2-http:1.23.0",
            "com.avast.gradle:gradle-docker-compose-plugin:0.17.6",
            "com.github.node-gradle:gradle-node-plugin:7.0.1",
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
            "org.jbake:jbake-gradle-plugin:5.5.0",
            "org.slf4j:slf4j-simple:2.0.16",
            "org.asciidoctor:asciidoctorj-diagram:2.3.1",
            "org.asciidoctor:asciidoctor-gradle-jvm-slides:$asciidoctorGradleVersion",
            "org.asciidoctor:asciidoctor-gradle-base:$asciidoctorGradleVersion",
            "org.asciidoctor:asciidoctor-gradle-jvm-gems:$asciidoctorGradleVersion",
            "com.burgstaller:okhttp-digest:1.10",
            "org.ysb33r.gradle:grolifant:0.12.1",
            "com.avast.gradle:gradle-docker-compose-plugin:0.14.2",
            "dev.langchain4j:langchain4j:$langchain4jVersion",
            "dev.langchain4j:langchain4j-ollama:$langchain4jVersion",
            "org.testcontainers:testcontainers:$testcontainersVersion",
            "org.testcontainers:ollama:$testcontainersVersion",
            "org.gradle:gradle-tooling-api:8.6",
            files("../api/build/libs/api-$schoolVersion.jar".run(::File).path),
        ).forEach(::implementation)
    } catch (_: Exception) {
        // si workspace-model lib n'existe pas alors lancer une exception qui demande de lancer son build avant
    }

    setOf("org.jetbrains.kotlin:kotlin-test-junit5")
        .forEach(::testImplementation)
    setOf("org.junit.platform:junit-platform-launcher")
        .forEach(::testRuntimeOnly)
    setOf("com.sun.xml.bind:jaxb-impl:4.0.5")
        .forEach(::runtimeOnly)
}

val buildApi by tasks.registering(GradleBuild::class) {
    dir = "../api".run(::File)
    tasks = listOf("build") // Or whatever tasks produce the lib.jar
}
// Add a dependency on the buildWorkspaceModel task
tasks.named("compileKotlin") { dependsOn(buildApi) }

val functionalTestSourceSet: SourceSet = sourceSets.create("functionalTest")

configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])
configurations["functionalTestRuntimeOnly"].extendsFrom(configurations["testRuntimeOnly"])

val functionalTest by tasks.registering(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
    useJUnitPlatform()
}

tasks.named<Task>("check") { dependsOn(functionalTest) }

tasks.named<Test>("test") { useJUnitPlatform() }

tasks.withType<JavaExec> {
    jvmArgs = setOf(
        "--add-modules=jdk.incubator.vector",
        "--enable-native-access=ALL-UNNAMED",
        "--enable-preview"
    ).toList()
}