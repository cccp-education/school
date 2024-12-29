import org.gradle.api.tasks.wrapper.Wrapper.DistributionType.BIN
import school.workspace.WorkspaceUtils.purchaseArtifact

plugins { idea }
apply<school.frontend.SchoolPlugin>()
apply<school.forms.FormPlugin>()
apply<school.jbake.JBakeGhPagesPlugin>()
apply<school.ai.AssistantPlugin>()
apply<school.translate.TranslatorPlugin>()

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

tasks.register<Exec>("springbootCheckOpenFirefox") {
    group = "api"
    commandLine(
           "./gradlew",
        "-p",
        "../api",
        ":springbootCheckOpenFirefox"
    )
}

tasks.register<Exec>("api") {
    group = "api"
    commandLine(
        "./gradlew",
        "-p",
        "../api",
        ":bootRun"
    )
}

tasks.register<Exec>("cli") {
    group = "api"
    commandLine(
        "./gradlew",
        "-p",
        "../api",
        ":cli"
    )
}

tasks.register<Exec>("installer") {
    group = "installer"
    commandLine(
        "./gradlew",
        "-p",
        "../api",
        ":runWorkspaceInstaller"
    )
}