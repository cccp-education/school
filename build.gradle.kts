import org.gradle.api.tasks.wrapper.Wrapper.DistributionType.ALL

import school.workspace.WorkspaceUtils.purchaseArtifact

plugins { idea }
apply<school.frontend.SchoolPlugin>()
apply<school.forms.FormPlugin>()
apply<school.jbake.JBakeGhPagesPlugin>()
apply<school.ai.AssistantPlugin>()
apply<school.translate.TranslatorPlugin>()


purchaseArtifact()
//TODO: add some tasks from detached projects, and python run, tests, deployment
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
//ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

tasks.register<Exec>("buildSchool") { commandLine("./gradlew", "-p", "api", "build") }

//tasks["build"].dependsOn(tasks["buildApi"])