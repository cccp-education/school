pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
}
plugins { id("org.gradle.toolchains.foojay-resolver-convention") version ("0.7.0") }
rootProject.name = "functional-tests