plugins { `kotlin-dsl` }

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
    maven("https://plugins.gradle.org/m2/")
    maven("https://maven.xillio.com/artifactory/libs-release/")
    maven("https://mvnrepository.com/repos/springio-plugins-release")
    maven("https://archiva-repository.apache.org/archiva/repository/public/")
}


dependencies {
    val commonsIoVersion = "2.13.0"
    val jacksonVersion = "2.17.2"
    val arrowKtVersion = "1.2.4"
    val jgitVersion = "6.8.0.202311291450-r"
    val langchain4jVersion = "0.33.0"
    val asciidoctorGradleVersion = "4.0.0-alpha.1"
    val deps by lazy {
        setOf(
            "com.google.apis:google-api-services-forms:v1-rev20220908-2.0.0",
            "com.google.apis:google-api-services-drive:v3-rev197-1.25.0",
            "com.google.api-client:google-api-client-jackson2:2.3.0",
            "com.google.auth:google-auth-library-oauth2-http:1.23.0",
            "com.avast.gradle:gradle-docker-compose-plugin:0.17.6",
            "com.github.node-gradle:gradle-node-plugin:7.0.1",
            "org.jetbrains.kotlin:kotlin-stdlib",
            "commons-io:commons-io:$commonsIoVersion",
            "com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion",
            "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion",
            "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion",
            "org.eclipse.jgit:org.eclipse.jgit:$jgitVersion",
            "org.eclipse.jgit:org.eclipse.jgit.archive:$jgitVersion",
            "org.eclipse.jgit:org.eclipse.jgit.ssh.jsch:$jgitVersion",
            "org.tukaani:xz:1.9",
            "io.arrow-kt:arrow-core:$arrowKtVersion",
            "io.arrow-kt:arrow-fx-coroutines:$arrowKtVersion",
            "io.arrow-kt:arrow-integrations-jackson-module:0.14.1",
            "org.apache.poi:poi-ooxml:5.2.5",
            "org.jbake:jbake-gradle-plugin:5.5.0",
            "org.slf4j:slf4j-simple:2.0.7",
            "org.asciidoctor:asciidoctorj-diagram:2.3.1",
            "org.asciidoctor:asciidoctor-gradle-jvm-slides:$asciidoctorGradleVersion",
            "org.asciidoctor:asciidoctor-gradle-base:$asciidoctorGradleVersion",
            "org.asciidoctor:asciidoctor-gradle-jvm-gems:$asciidoctorGradleVersion",
            "com.burgstaller:okhttp-digest:1.10",
            "org.ysb33r.gradle:grolifant:0.12.1",
            "com.avast.gradle:gradle-docker-compose-plugin:0.14.2",
            "dev.langchain4j:langchain4j-open-ai:$langchain4jVersion",
            "dev.langchain4j:langchain4j:$langchain4jVersion",
            "dev.langchain4j:langchain4j-ollama:0.34.0",
            "org.testcontainers:testcontainers:1.20.1",
            "org.testcontainers:ollama:1.20.1",

        )
    }
    deps.forEach(::implementation)
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

//kotlin { jvmToolchain(JavaVersion.VERSION_21.ordinal) }

tasks.withType<JavaExec> {
    jvmArgs = listOf(
        "--add-modules=jdk.incubator.vector",
        "--enable-native-access=ALL-UNNAMED",
        "--enable-preview"
    )
}
