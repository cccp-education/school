import workspace.WorkspaceUtils.purchaseArtifact


plugins {
    idea
    id("com.github.node-gradle.node")
//    id("org.asciidoctor.jvm.convert")
}

apply<workspace.school.SchoolPlugin>()
apply<workspace.forms.FormPlugin>()
apply<workspace.jamstack.JBakeGhPagesPlugin>()
apply<workspace.ai.AssistantPlugin>()

purchaseArtifact()

tasks.wrapper {
    gradleVersion = "8.6"
    distributionType = Wrapper.DistributionType.ALL
}

tasks.withType<JavaExec> {
    jvmArgs = listOf(
        "--add-modules=jdk.incubator.vector",
        "--enable-native-access=ALL-UNNAMED",
        "--enable-preview"
    )
}
