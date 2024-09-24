import school.WorkspaceUtils.purchaseArtifact

plugins {
    id("com.github.node-gradle.node")
    id("com.avast.gradle.docker-compose")
}

apply<school.school.SchoolPlugin>()
apply<school.forms.FormPlugin>()

purchaseArtifact()

//kotlin { jvmToolchain(22) }