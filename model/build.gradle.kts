plugins {
    kotlin("jvm") version "2.0.20"
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}