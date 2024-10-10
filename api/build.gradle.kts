@file:Suppress(
    "GradlePackageUpdate",
    "PropertyName",
    "DEPRECATION",
    "UnstableApiUsage",
)

import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.file.FileSystems
import java.util.*

buildscript {
    repositories {
        gradlePluginPortal()
        google()
    }
    dependencies { classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.20-Beta2") }
}

plugins {
    java
    idea
    jacoco
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.allopen")
    kotlin("plugin.noarg")
    kotlin("plugin.serialization")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = properties["artifact.group"].toString()

version = ("artifact.version" to "artifact.version.key").artifactVersion

val Pair<String, String>.artifactVersion: String
    get() = first.run(
        Properties().apply {
            second.run(properties::get).let {
                "user.home"
                    .run(System::getProperty)
                    .run { "$this$it" }
            }.run(::File)
                .inputStream()
                .use(::load)
        }::get
    ).toString()

springBoot.mainClass.set("school.Application")

tasks.register("cli") {
    //./gradlew -q cli --args='your args there'
    group = "application"
    description = "Run school cli"
    doFirst { springBoot.mainClass.set("school.CommandLine") }
    finalizedBy("bootRun")
}

idea.module.excludeDirs.plusAssign(files("node_modules"))

repositories {
    mavenCentral()
    maven(url = "https://maven.repository.redhat.com/ga/")
    maven(url = "https://repo.spring.io/milestone")
    maven(url = "https://repo.spring.io/snapshot")
    maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap/")
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${properties["kotlinx-serialization-json.version"]}")

    // Jackson marshaller
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Spring
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
//    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security:spring-security-data")
    runtimeOnly("org.springframework.boot:spring-boot-properties-migrator")

    // Spring tests
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "mockito-core")
    }

    // JWT
    implementation("io.jsonwebtoken:jjwt-impl:${properties["jsonwebtoken.version"]}")
    implementation("io.jsonwebtoken:jjwt-jackson:${properties["jsonwebtoken.version"]}")

    // SSL
    implementation("io.netty:netty-tcnative-boringssl-static:${properties["boring_ssl.version"]}")

    // Database
    runtimeOnly("com.h2database:h2")
    runtimeOnly("io.r2dbc:r2dbc-h2")
    runtimeOnly("org.postgresql:r2dbc-postgresql:${properties["r2dbc-postgresql.version"]}")

    // Kotlin-JUnit5
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.projectreactor:reactor-test")

    // Mock
    testImplementation("org.mockito.kotlin:mockito-kotlin:${properties["mockito_kotlin_version"]}")
    testImplementation("org.mockito:mockito-junit-jupiter:${properties["mockito_jupiter.version"]}")

    testImplementation("io.mockk:mockk:${properties["mockk.version"]}")
    testImplementation("org.wiremock:wiremock:${properties["wiremock.version"]}") {
        exclude(module = "commons-fileupload")
    }
    testImplementation("commons-fileupload:commons-fileupload:1.5.0.redhat-00001")
    testImplementation("com.ninja-squad:springmockk:${properties["springmockk.version"]}")


    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("com.tngtech.archunit:archunit-junit5-api:${properties["archunit_junit5.version"]}")
    testRuntimeOnly("com.tngtech.archunit:archunit-junit5-engine:${properties["archunit_junit5.version"]}")
    testImplementation("org.springframework.cloud:spring-cloud-starter-contract-verifier:${properties["spring_cloud_starter.version"]}") {
        exclude(module = "commons-collections")
    }
    implementation("io.arrow-kt:arrow-core:${properties["arrow-kt.version"]}")
    implementation("io.arrow-kt:arrow-fx-coroutines:${properties["arrow-kt.version"]}")
    implementation("io.arrow-kt:arrow-integrations-jackson-module:${properties["arrow-kt_jackson.version"]}")

//    // Spring AI
//    implementation(platform("org.springframework.ai:spring-ai-bom:1.0.0-SNAPSHOT"))
//
//    // OpenAI
//    implementation("org.springframework.ai:spring-ai-openai")
//    implementation("org.springframework.ai:spring-ai-openai-spring-boot-starter")

    // misc
    implementation("org.apache.commons:commons-lang3")
    testImplementation("org.apache.commons:commons-collections4:4.5.0-M1")
}

configurations {
    compileOnly { extendsFrom(configurations.annotationProcessor.get()) }
    implementation.configure {
        setOf(
            "org.junit.vintage" to "junit-vintage-engine",
            "org.springframework.boot" to "spring-boot-starter-tomcat",
            "org.apache.tomcat" to null
        ).forEach {
            when {
                it.first.isNotBlank() && it.second?.isNotBlank() == true -> exclude(it.first, it.second)
            }
        }
    }
}

//java { sourceCompatibility = VERSION_21 }

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
//        jvmTarget = VERSION_21.toString()
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging { events(FAILED, SKIPPED) }
    reports {
        html.required.set(true)
        ignoreFailures = true
    }
}

val BLANK = ""
val sep: String get() = FileSystems.getDefault().separator

tasks.register<Delete>("cleanResources") {
    description = "Delete directory build/resources"
    group = "build"
    delete(buildString {
        append("build")
        append(sep)
        append("resources")
    })
}

tasks.jacocoTestReport {
    // Give jacoco the file generated with the cucumber tests for the coverage.
    executionData(
        files(
            "$buildDir/jacoco/test.exec",
        )
    )
    reports { xml.required.set(true) }
}

tasks.register<TestReport>("testReport") {
    description = "Generates an HTML test report from the results of testReport task."
    group = "report"
    destinationDirectory.set(file(buildString {
        append(buildDir)
        append(sep)
        append("reports")
        append(sep)
        append("tests")
    }))
    reportOn("test")
}

val cucumberRuntime: Configuration by configurations.creating {
    extendsFrom(configurations["testImplementation"])
}

data class DockerHub(
    val username: String = properties["docker_hub_login"].toString(),
    val password: String = properties["docker_hub_password"].toString(),
    val token: String = properties["docker_hub_login_token"].toString(),
    val email: String = properties["docker_hub_email"].toString(),
    val image: String = "cheroliv/e3po"
)

//TODO: create task startLocalPostgresql() stopLocalPostgresql()
val pullPostgresImage = tasks.register<Exec>("pullPostgresImage") {
    commandLine(
        "docker",
        "pull",
        "postgres@sha256:b2653f33e2b9a49dceb8f3444108222875e25d993c1ba03a89c116bbccc53be4"
    )
}

tasks.register<Exec>("startLocalPostgresql") {
    dependsOn(pullPostgresImage)
    commandLine("docker", "run", "-d", "-p", "5432:5432", "postgres")
}

tasks.register<Exec>("stopLocalPostgresql") {
    commandLine("docker", "stop", "postgres")
}

tasks.register<Exec>("springbootCheckOpenFirefox") {
    group = "tests"
    description = "Check springboot project then show report in firefox"
    dependsOn("check")
    commandLine(
        "firefox",
        "--new-tab",
        layout
            .projectDirectory
            .asFile
            .toPath()
            .resolve("build/reports/tests/test/index.html")
            .toAbsolutePath()
    )
}