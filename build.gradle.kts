import org.gradle.api.tasks.wrapper.Wrapper.DistributionType.ALL
import school.workspace.WorkspaceUtils.purchaseArtifact
import school.jbake.JBakeGhPagesPlugin

plugins { idea }
apply<school.frontend.SchoolPlugin>()
apply<school.forms.FormPlugin>()
apply<JBakeGhPagesPlugin>()
apply<school.ai.AssistantPlugin>()

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
