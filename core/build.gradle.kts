plugins {
    kotlin("jvm") version "2.0.20"
    `java-library`
}

group = "workspace"
//group = properties["artifact.group"].toString()
version = properties["artifact.version"].toString()

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}