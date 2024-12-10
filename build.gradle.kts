import org.gradle.api.tasks.wrapper.Wrapper.DistributionType.BIN

import school.workspace.WorkspaceUtils.purchaseArtifact

//TODO: rename school to forge
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

tasks.register<Exec>("runWorkspaceInstaller") {
    commandLine(
        "./gradlew",
        "-p",
        "workspace-model",
        ":lib:run"
    )
}

tasks.register<Exec>("runApi") {
    commandLine(
        "./gradlew",
        "-p",
        "api",
        ":api"
    )
}

tasks.register<Exec>("springbootCheckOpenFirefox") {
    commandLine(
        "./gradlew",
        "-p",
        "api",
        ":springbootCheckOpenFirefox"
    )
}
