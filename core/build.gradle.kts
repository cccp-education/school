plugins {
    kotlin("jvm") version "2.0.20"
    `java-library`
}

version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}