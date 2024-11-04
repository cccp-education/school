pluginManagement {
    val asciidoctorGradleVersion = "4.0.0-alpha.1"

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://plugins.gradle.org/m2/")
        maven("https://maven.xillio.com/artifactory/libs-release/")
        maven("https://mvnrepository.com/repos/springio-plugins-release")
        maven("https://archiva-repository.apache.org/archiva/repository/public/")
    }
    plugins {
        id("org.jbake.site").version(extra["jbake-gradle.version"].toString())
        id("com.github.node-gradle.node").version(extra["node-gradle.version"].toString())
//        id("org.gradle.toolchains.foojay-resolver-convention") version ("0.8.0")
//        id("org.asciidoctor:asciidoctor-gradle-jvm-slides") version (asciidoctorGradleVersion)
//        id("org.asciidoctor:asciidoctor-gradle-base") version (asciidoctorGradleVersion)
//        id("org.asciidoctor:asciidoctor-gradle-jvm-gems") version (asciidoctorGradleVersion)
        kotlin("jvm").version(extra["kotlin.version"].toString())
        kotlin("plugin.serialization").version(extra["kotlin.version"].toString())
        kotlin("plugin.allopen").version(extra["kotlin.version"].toString())
        kotlin("plugin.noarg").version(extra["kotlin.version"].toString())
        kotlin("plugin.spring").version(extra["kotlin.version"].toString())
        id("org.springframework.boot").version(extra["springboot.version"].toString())
        id("io.spring.dependency-management").version(extra["spring_dependency_management.version"].toString())

    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
        maven("https://maven.xillio.com/artifactory/libs-release/")
        maven("https://mvnrepository.com/repos/springio-plugins-release")
        maven("https://archiva-repository.apache.org/archiva/repository/public/")
        maven ("https://plugins.gradle.org/m2/")
    }
}

rootProject.name = "api"
//include("core")
//include("model")
//include("api")
//include("formation")
//include("school-gradle-plugin")

// Inclure le projet model comme un build composite
//includeBuild("core") {
//    dependencySubstitution {
//        substitute(module("school:core")).using(project(":core"))
//        substitute(module("school:model")).using(project(":model"))
//    }
//}