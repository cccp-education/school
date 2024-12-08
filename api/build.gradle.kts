@file:Suppress(
    "GradlePackageUpdate",
    "PropertyName",
    "DEPRECATION",
    "UnstableApiUsage",
    "VulnerableLibrariesLocal",
)

import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.file.FileSystems
import java.util.*

buildscript {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
    dependencies { classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21") }
}

plugins {
    idea
    jacoco
    application
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.allopen")
    kotlin("plugin.noarg")
    kotlin("plugin.serialization")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

extra["springShellVersion"] = "3.3.3"
group = properties["artifact.group"].toString()
version = "0.0.1"
//version = ("artifact.version" to "artifact.version.key").artifactVersion
idea.module.excludeDirs.plusAssign(files("node_modules"))
springBoot.mainClass.set("app.Application")
val BLANK = ""
val sep: String get() = FileSystems.getDefault().separator

data class DockerHub(
    val username: String = properties["docker_hub_login"].toString(),
    val password: String = properties["docker_hub_password"].toString(),
    val token: String = properties["docker_hub_login_token"].toString(),
    val email: String = properties["docker_hub_email"].toString(),
    val image: String = "cheroliv/e3po"
)


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

repositories {
    mavenCentral()
    maven(url = "https://maven.repository.redhat.com/ga/")
    maven(url = "https://repo.spring.io/milestone")
    maven(url = "https://repo.spring.io/snapshot")
    maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap/")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.shell:spring-shell-dependencies:${property("springShellVersion")}")
    }
}

dependencies {
    files("../workspace-model/lib/build/libs/lib.jar".run(::File).path).run(::implementation)
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
    // Spring tools
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    runtimeOnly("org.springframework.boot:spring-boot-properties-migrator")
    //developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    // Springboot
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.shell:spring-shell-starter")
    // Spring security
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security:spring-security-data")
    // Spring cloud
    @Suppress("VulnerableLibrariesLocal", "RedundantSuppression")
    testImplementation("org.springframework.cloud:spring-cloud-starter-contract-verifier:${properties["spring_cloud_starter.version"]}") {
        exclude(module = "commons-collections")
    }
    // Spring tests
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "mockito-core")
    }
    testImplementation("org.springframework.shell:spring-shell-starter-test")
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
    implementation("io.arrow-kt:arrow-core:${properties["arrow-kt.version"]}")
    implementation("io.arrow-kt:arrow-fx-coroutines:${properties["arrow-kt.version"]}")
    implementation("io.arrow-kt:arrow-integrations-jackson-module:${properties["arrow-kt_jackson.version"]}")


    // Langchain4j
    implementation("dev.langchain4j:langchain4j-easy-rag:${properties["langchain4j.version"]}")
    implementation("dev.langchain4j:langchain4j-pgvector:${properties["langchain4j.version"]}")
    implementation("dev.langchain4j:langchain4j-document-parser-apache-pdfbox:${properties["langchain4j.version"]}")
    implementation("dev.langchain4j:langchain4j-web-search-engine-google-custom:${properties["langchain4j.version"]}")
    implementation("dev.langchain4j:langchain4j-hugging-face:${properties["langchain4j.version"]}")
    implementation("dev.langchain4j:langchain4j-spring-boot-starter:${properties["langchain4j.version"]}")
    implementation("dev.langchain4j:langchain4j-ollama-spring-boot-starter:${properties["langchain4j.version"]}")
    implementation("dev.langchain4j:langchain4j-vertex-ai-gemini-spring-boot-starter:${properties["langchain4j.version"]}")
    testImplementation("dev.langchain4j:langchain4j-spring-boot-tests:${properties["langchain4j.version"]}")
//    implementation("dev.langchain4j:langchain4j-vertex-ai:${properties["langchain4j.version"]}")
//    implementation("dev.langchain4j:langchain4j-google-ai-gemini:${properties["langchain4j.version"]}")
//    implementation("dev.langchain4j:langchain4j-vertex-ai-gemini:${properties["langchain4j.version"]}")


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

val buildWorkspaceModel: TaskProvider<GradleBuild> by tasks.registering(GradleBuild::class) {
    group = "api"
    dir = "../workspace-model/lib".run(::File)
    tasks = listOf("build") // Or whatever tasks produce the lib.jar
}
// Add a dependency on the buildWorkspaceModel task
tasks.named("compileKotlin") { dependsOn(buildWorkspaceModel) }


tasks.register("cli") {
    group = "api"
    description = "Run school cli : ./gradlew -p api :cli -Pargs=--gui"
    doFirst {
        with(springBoot) {
            tasks.bootRun.configure {
                args = (project.findProperty("args") as String?)
                    ?.trim()
                    ?.split(" ")
                    ?.filter(String::isNotEmpty)
                    ?.also { println("Passing args to Spring Boot: $it") }
                    ?: emptyList()
            }
            mainClass.set("app.cli.CommandLine")
        }
    }
    finalizedBy(tasks.bootRun)
}

tasks.register("api") {
    group = "api"
    description = "Run school api"
    doFirst { springBoot.mainClass.set("app.Application") }
    finalizedBy("bootRun")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
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

//tasks.register<Exec>("startLocalPostgresql") {
//    group = "api"
//    dependsOn(tasks.register<Exec>("pullPostgresImage") {
//        commandLine(
//            "docker",
//            "pull",
//            "postgres@sha256:b2653f33e2b9a49dceb8f3444108222875e25d993c1ba03a89c116bbccc53be4"
//        )
//    })
//    commandLine("docker", "run", "-d", "-p", "5432:5432", "postgres")
//}

//tasks.register<Exec>("stopLocalPostgresql") {
//    group = "api"
//    commandLine("docker", "stop", "postgres")
//}

tasks.register<Exec>("springbootCheckOpenFirefox") {
    group = "verification"
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