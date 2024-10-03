plugins {
    kotlin("jvm") version "2.0.20"
    `java-library`
}

group = properties["artifact.group"].toString()
version = properties["artifact.version"].toString()

repositories { mavenCentral() }

dependencies {
    implementation(project(":core"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}