plugins {
    kotlin("jvm") version "2.0.20"
    `java-library`
}

group = properties["artifact.group"].toString()
version = properties["artifact.version"].toString()

repositories { mavenCentral() }

dependencies {
//    project
//        .projectDir
//        .parentFile
//        .listFiles()!!
//        .first { it.name == "base" }
//        .let(::fileTree)
//        .let(::implementation)
    val schoolVersion = "0.0.1"
    projectDir
        .parentFile// si base lib n'existe pas alors lancer une exception qui demande de lancer son build avant
        .listFiles()!!.find { it.name == "base" }!!
        .listFiles()!!.find { it.name == "build" }!!
        .listFiles()!!.find { it.name == "libs" }!!
        .listFiles()!!.first { it.name == "base-$version.jar" }!!
        .let(::fileTree)
        .let(::implementation)

//    implementation(project("base"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}