import workspace.WorkspaceUtils.purchaseArtifact

plugins {
    id("com.github.node-gradle.node")
    id("com.avast.gradle.docker-compose")
}

apply<workspace.school.SchoolPlugin>()
apply<workspace.forms.FormPlugin>()

purchaseArtifact()

//kotlin { jvmToolchain(22) }