plugins { `kotlin-dsl` }

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.gradle.org/gradle/libs-releases/")
    maven("https://plugins.gradle.org/m2/")
    maven("https://maven.xillio.com/artifactory/libs-release/")
    maven("https://mvnrepository.com/repos/springio-plugins-release")
    maven("https://archiva-repository.apache.org/archiva/repository/public/")
}

dependencies {
    // les dependances : base, model, assistant n'existe pas alors il faut les builder
    val langchain4jVersion = "0.35.0"
    val testcontainersVersion = "1.20.1"
    val asciidoctorGradleVersion = "4.0.0-alpha.1"
    val commonsIoVersion = "2.13.0"
    val jacksonVersion = "2.17.2"//2.18.0
    val arrowKtVersion = "1.2.4"
    val jgitVersion = "6.10.0.202406032230-r"
    val schoolVersion = "0.0.1"
//    rootDir.parentFile
//        .listFiles()!!.find { it.name == "base" }!!
//        .listFiles()!!.find { it.name == "build" }!!
//        .listFiles()!!.find { it.name == "libs" }!!
//        .listFiles()!!.first { it.name == "base-$schoolVersion.jar" }!!
//        .let(::fileTree)
//        .let(::implementation)
//    rootDir.parentFile
//        .listFiles()!!.find { it.name == "model" }!!
//        .listFiles()!!.find { it.name == "build" }!!
//        .listFiles()!!.find { it.name == "libs" }!!
//        .listFiles()!!.first { it.name == "model-$schoolVersion.jar" }!!
//        .let(::fileTree)
//        .let(::implementation)
//    rootDir
//        .parentFile// si base lib n'existe pas alors lancer une exception qui demande de lancer son build avant
//        .listFiles()!!.find { it.name == "api" }!!
//        .listFiles()!!.find { it.name == "build" }!!
//        .listFiles()!!.find { it.name == "libs" }!!
//        .listFiles()!!.first { it.name == "base-$schoolVersion.jar" }!!
//        .let(::fileTree)
//        .let(::implementation)
    setOf(
        "com.google.apis:google-api-services-forms:v1-rev20220908-2.0.0",
        "com.google.apis:google-api-services-drive:v3-rev197-1.25.0",
        "com.google.api-client:google-api-client-jackson2:2.3.0",
        "com.google.auth:google-auth-library-oauth2-http:1.23.0",
        "com.avast.gradle:gradle-docker-compose-plugin:0.17.6",
        "com.github.node-gradle:gradle-node-plugin:7.0.1",
        "org.jetbrains.kotlin:kotlin-stdlib",
        "commons-io:commons-io:$commonsIoVersion",
//        "javax.xml.bind:jaxb-api:2.4.0-b180830.0359",
        "jakarta.xml.bind:jakarta.xml.bind-api:4.0.2",
        "com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion",
        "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion",
        "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion",
        "com.fasterxml.jackson.module:jackson-module-jsonSchema:$jacksonVersion",
        "org.eclipse.jgit:org.eclipse.jgit:$jgitVersion",
        "org.eclipse.jgit:org.eclipse.jgit.archive:$jgitVersion",
        "org.eclipse.jgit:org.eclipse.jgit.ssh.jsch:$jgitVersion",
        "org.tukaani:xz:1.9",
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
//        "dev.langchain4j:langchain4j-open-ai:$langchain4jVersion",
        "dev.langchain4j:langchain4j:$langchain4jVersion",
        "dev.langchain4j:langchain4j-ollama:$langchain4jVersion",
        "org.testcontainers:testcontainers:$testcontainersVersion",
        "org.testcontainers:ollama:$testcontainersVersion",
        "org.gradle:gradle-tooling-api:8.6",
    ).forEach(::implementation)

    setOf("org.jetbrains.kotlin:kotlin-test-junit5")
        .forEach(::testImplementation)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

//tasks.named("build") {
//    // Avant de lancer la tâche "build", assure-toi que le JAR du projet 'base' est à jour
//    dependsOn(":buildBaseJar")
//}

//gradle.beforeProject {
//    if (name == "buildSrc") {
//        tasks.named("build") {
//            logger.log(LogLevel.INFO, "gradle.beforeProject call.")
////            dependsOn(":buildBaseJar")
//        }
//    }
//}


//tasks.register("logBuildBaseJar") {
//    group = "build"
//    description = "Builds the base project and generates the JAR file."
//    doLast {
//        exec {
//            commandLine("gradle", "-p", "../base", "build")
//        }
//    }
//}
//tasks["build"].dependsOn(":logBuildBaseJar")


tasks["build"].dependsOn(":buildBaseJar")

tasks.register<Exec>("buildBaseJar") {
    description = "Build the base project to generate the updated JAR."
    // Lancer la commande gradle pour construire le projet base
    commandLine("./gradlew", ":build")

    doLast {
        // Vérifie que le fichier JAR est bien mis à jour
        val jarFile = file("../base/build/libs/base-0.0.1.jar")
        if (!jarFile.exists()) {
            val messageError = "The base JAR was not built successfully."
            logger.log(LogLevel.ERROR, messageError)
            throw GradleException(messageError)
        }
        logger.log(LogLevel.INFO, "The base JAR was built successfully.")
    }
    workingDir = rootDir
        .parentFile
        .listFiles()!!.find { it.name == "base" }!!
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

//kotlin { jvmToolchain(JavaVersion.VERSION_21.ordinal) }
