buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
    dependencies{
        classpath("com.github.node-gradle:gradle-node-plugin:7.0.1")
    }
}
plugins { id("com.github.node-gradle.node").version("7.0.1")}"""
//    apply<SchoolPlugin>()