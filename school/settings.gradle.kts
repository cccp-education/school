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
        id("org.jbake.site").version("5.5.0")
        id("com.github.node-gradle.node").version("7.0.1")
//        id("org.gradle.toolchains.foojay-resolver-convention") version ("0.8.0")
//        id("org.asciidoctor:asciidoctor-gradle-jvm-slides") version (asciidoctorGradleVersion)
//        id("org.asciidoctor:asciidoctor-gradle-base") version (asciidoctorGradleVersion)
//        id("org.asciidoctor:asciidoctor-gradle-jvm-gems") version (asciidoctorGradleVersion)
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
    }
}

rootProject.name = "school"
include("formation")
include("school-gradle-plugin")
include("workspace-gradle-plugin")
