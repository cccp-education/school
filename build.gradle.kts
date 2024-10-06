import org.gradle.api.tasks.wrapper.Wrapper.DistributionType.ALL

import school.workspace.WorkspaceUtils.purchaseArtifact

plugins { idea }
apply<school.frontend.SchoolPlugin>()
apply<school.forms.FormPlugin>()
apply<school.jbake.JBakeGhPagesPlugin>()
apply<school.ai.AssistantPlugin>()

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

gradle.settingsEvaluated {
    if (gradle.rootProject.name == "school") {
        logger.log(LogLevel.INFO, "gradle.settingsEvaluated: BuildSrc is being evaluated.")
    }
}

gradle.projectsEvaluated {
    if (gradle.rootProject.name == "school") {
        logger.log(LogLevel.INFO, "gradle.projectsEvaluated: BuildSrc project is evaluated.")
    }
}