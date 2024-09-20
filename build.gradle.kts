import org.gradle.api.tasks.wrapper.Wrapper.DistributionType.ALL
import workspace.WorkspaceUtils.purchaseArtifact

plugins { idea }
apply<workspace.school.SchoolPlugin>()
apply<workspace.forms.FormPlugin>()
apply<workspace.jamstack.JBakeGhPagesPlugin>()
apply<workspace.ai.AssistantPlugin>()

purchaseArtifact()

tasks.wrapper {
    gradleVersion = "8.6"
    distributionType = ALL
}

tasks.withType<JavaExec> {
    jvmArgs = listOf(
        "--add-modules=jdk.incubator.vector",
        "--enable-native-access=ALL-UNNAMED",
        "--enable-preview"
    )
}