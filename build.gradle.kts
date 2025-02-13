plugins { idea }

apply<school.frontend.SchoolPlugin>()
apply<school.forms.FormPlugin>()
apply<school.jbake.JBakeGhPagesPlugin>()
apply<school.ai.AssistantPlugin>()
apply<school.translate.TranslatorPlugin>()

object School {
    const val GROUP_KEY = "artifact.group"
    const val VERSION_KEY = "artifact.version"
    const val SERVER = "app.Server"
    const val CLI = "app.CommandLine"
    const val SQL_SCHEMA = "app.users.api.dao.DatabaseConfiguration"
    const val SPRING_PROFILE_KEY = "spring.profiles.active"
    const val MOCKITO_AGENT = "mockito-agent"
    const val KOTLIN_COMPILER_OPTION_JSR305 = "-Xjsr305=strict"
    const val MAIN_FUNCTION = "main"
    const val NODE_MODULES = "node_modules"
}

allprojects {
    fun Project.purchaseArtifact() = (School.GROUP_KEY to School.VERSION_KEY).run {
        group = properties[first].toString()
        version = properties[second].toString()
    }
    purchaseArtifact()
}

tasks.run {
    wrapper {
        gradleVersion = "8.6"
        distributionType = Wrapper.DistributionType.BIN
    }
    withType<JavaExec> {
        jvmArgs = listOf(
            "--add-modules=jdk.incubator.vector",
            "--enable-native-access=ALL-UNNAMED",
            "--enable-preview"
        )
    }
    register<Exec>("reportTestApi") {
        group = "api"
        commandLine(
            "./gradlew",
            "-q",
            "-s",
            "-p",
            "../api",
            ":apiCheckFirefox"
        )
    }
    register<Exec>("testApi") {
        group = "api"
        commandLine(
            "./gradlew",
            "-q",
            "-s",
            "-p",
            "../api",
            ":check",
            "--rerun-tasks",
        )
    }
    register<Exec>("runInstaller") {
        group = "installer"
        commandLine(
            "java",
            "-jar",
            "../api/installer/build/libs/installer-${project.properties["artifact.version"]}.jar"
        )
    }
    register<Exec>("runApi") {
        group = "api"
        commandLine(
            "java",
            "-jar",
            "../api/build/libs/api-${project.properties["artifact.version"]}.jar"
        )
    }
    register<Exec>("runLocalApi") {
        group = "api"
        commandLine(
            "java",
            "-D${School.SPRING_PROFILE_KEY}=local",
            "-jar",
            "../api/build/libs/api-${project.properties["artifact.version"]}.jar"
        )
    }
}

tasks.register<Exec>("runCli") {
    group = "api"
    commandLine(
        "./gradlew",
        "-q",
        "-s",
        "-p",
        "../api",
        ":cli"
    )
}