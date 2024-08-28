
import workspace.WorkspaceUtils.purchaseArtifact
import workspace.ai.AssistantPlugin
import workspace.jamstack.JBakeGhPagesPlugin


plugins {
    idea
    id("com.github.node-gradle.node")
//    id("org.asciidoctor.jvm.convert")
}

apply<workspace.school.SchoolPlugin>()
apply<workspace.forms.FormPlugin>()
apply<JBakeGhPagesPlugin>()
apply<AssistantPlugin>()

purchaseArtifact()

tasks.wrapper {
    gradleVersion = "8.6"
    distributionType = Wrapper.DistributionType.ALL
}

//kotlin { jvmToolchain(22) }
tasks.withType<JavaExec> {
    jvmArgs = listOf(
        "--add-modules=jdk.incubator.vector",
        "--enable-native-access=ALL-UNNAMED",
        "--enable-preview"
    )
}
