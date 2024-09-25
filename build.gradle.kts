import org.gradle.api.tasks.wrapper.Wrapper.DistributionType.ALL
import school.ai.AssistantPlugin
import school.forms.FormPlugin
import school.frontend.SchoolPlugin
import school.workspace.WorkspaceUtils.purchaseArtifact
import school.jbake.JBakeGhPagesPlugin

plugins { idea }
apply<SchoolPlugin>()
apply<FormPlugin>()
apply<JBakeGhPagesPlugin>()
apply<AssistantPlugin>()

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
