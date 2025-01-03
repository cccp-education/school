import org.gradle.api.tasks.wrapper.Wrapper.DistributionType.BIN
import school.workspace.WorkspaceUtils.purchaseArtifact

plugins { idea }
apply<school.frontend.SchoolPlugin>()
apply<school.forms.FormPlugin>()
apply<school.jbake.JBakeGhPagesPlugin>()
//apply<school.ai.AssistantPlugin>()
//apply<school.translate.TranslatorPlugin>()

purchaseArtifact()

tasks.wrapper {
    gradleVersion = "8.6"
    distributionType = BIN
}

tasks.withType<JavaExec> {
    jvmArgs = listOf(
        "--add-modules=jdk.incubator.vector",
        "--enable-native-access=ALL-UNNAMED",
        "--enable-preview"
    )
}

tasks.register<Exec>("testApi") {
    group = "api"
    commandLine(
           "./gradlew",
        "-p",
        "../api",
        ":apiCheckFirefox"
    )
}

tasks.register<Exec>("runApi") {
    group = "api"
    commandLine(
        "./gradlew",
        "-p",
        "../api",
        ":bootRun"
    )
}

tasks.register<Exec>("runCli") {
    group = "api"
    commandLine(
        "./gradlew",
        "-p",
        "../api",
        ":cli"
    )
}

tasks.register<Exec>("runInstaller") {
    group = "installer"
    commandLine(
        "./gradlew",
        "-p",
        "../api",
        ":runWorkspaceInstaller"
    )
}