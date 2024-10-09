pluginManagement {
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
    }
}
rootProject.name = "school-gradle-plugin"
include("plugin")
