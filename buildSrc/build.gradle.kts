import Build_gradle.Constants.arrowKtVersion
import Build_gradle.Constants.asciidoctorGradleVersion
import Build_gradle.Constants.jacksonVersion
import Build_gradle.Constants.jgitVersion
import Build_gradle.Constants.langchain4jVersion

plugins { `kotlin-dsl` }

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
    setOf(
        "https://repo.gradle.org/gradle/libs-releases/",
        "https://plugins.gradle.org/m2/",
        "https://maven.xillio.com/artifactory/libs-release/",
        "https://mvnrepository.com/repos/springio-plugins-release",
        "https://archiva-repository.apache.org/archiva/repository/public/"
    ).forEach(::maven)
}

object Constants {
    const val langchain4jVersion = "1.0.0-beta1"//"0.36.2"
    const val asciidoctorGradleVersion = "4.0.0-alpha.1"
    const val jacksonVersion = "2.17.2"//2.18.0
    const val arrowKtVersion = "1.2.4"
    const val jgitVersion = "6.10.0.202406032230-r"
}

dependencies {
    setOf(
        files("../../api/build/libs/api-0.0.1.jar".run(::File).path),
        "com.avast.gradle:gradle-docker-compose-plugin:0.17.6",
        "com.github.node-gradle:gradle-node-plugin:7.0.1",
        "jakarta.xml.bind:jakarta.xml.bind-api:4.0.2",
        "com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion",
        "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion",
        "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion",
        "com.fasterxml.jackson.module:jackson-module-jsonSchema:$jacksonVersion",
        "org.eclipse.jgit:org.eclipse.jgit:$jgitVersion",
        "org.eclipse.jgit:org.eclipse.jgit.archive:$jgitVersion",
        "org.eclipse.jgit:org.eclipse.jgit.ssh.jsch:$jgitVersion",
        "io.arrow-kt:arrow-core:$arrowKtVersion",
        "io.arrow-kt:arrow-fx-coroutines:$arrowKtVersion",
        "io.arrow-kt:arrow-integrations-jackson-module:0.14.1",
        "org.apache.poi:poi-ooxml:5.2.5",
        "org.jbake:jbake-gradle-plugin:5.5.0",
        "org.slf4j:slf4j-simple:2.0.16",
        "org.asciidoctor:asciidoctorj-diagram:2.3.1",
        "org.asciidoctor:asciidoctor-gradle-jvm-slides:$asciidoctorGradleVersion",
        "org.asciidoctor:asciidoctor-gradle-base:$asciidoctorGradleVersion",
        "org.asciidoctor:asciidoctor-gradle-jvm-gems:$asciidoctorGradleVersion",
        "com.burgstaller:okhttp-digest:1.10",
        "org.ysb33r.gradle:grolifant:0.12.1",
        "com.avast.gradle:gradle-docker-compose-plugin:0.14.2",
        "org.gradle:gradle-tooling-api:8.6",
//        "com.google.apis:google-api-services-forms:v1-rev20220908-2.0.0",
//        "com.google.apis:google-api-services-drive:v3-rev197-1.25.0",
//        "com.google.api-client:google-api-client-jackson2:2.3.0",
//        "com.google.auth:google-auth-library-oauth2-http:1.23.0",
//        "org.jetbrains.kotlin:kotlin-stdlib",
//        "commons-io:commons-io:$commonsIoVersion",
//        "org.tukaani:xz:1.9",
        "dev.langchain4j:langchain4j:$langchain4jVersion",
        "dev.langchain4j:langchain4j-ollama:$langchain4jVersion",
        "dev.langchain4j:langchain4j-hugging-face:$langchain4jVersion",
        "dev.langchain4j:langchain4j-google-ai-gemini:$langchain4jVersion",
        "dev.langchain4j:langchain4j-mistral-ai:$langchain4jVersion",
        "dev.langchain4j:langchain4j-pgvector:$langchain4jVersion",

//        "org.testcontainers:testcontainers:$testcontainersVersion",
//        "org.testcontainers:ollama:$testcontainersVersion",
    ).forEach(::implementation)
    runtimeOnly("com.sun.xml.bind:jaxb-impl:4.0.5")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

val functionalTestSourceSet: SourceSet = sourceSets.create("functionalTest")

configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])
configurations["functionalTestRuntimeOnly"].extendsFrom(configurations["testRuntimeOnly"])

val functionalTest by tasks.registering(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
    useJUnitPlatform()
}

tasks.named<Task>("check") { dependsOn(functionalTest) }

tasks.named<Test>("test") { useJUnitPlatform() }

tasks.withType<JavaExec> {
    jvmArgs = setOf(
        "--add-modules=jdk.incubator.vector",
        "--enable-native-access=ALL-UNNAMED",
        "--enable-preview"
    ).toList()
}