@file:Suppress("unused")

package school.workspace

import org.gradle.api.Plugin
import org.gradle.api.Project
import school.workspace.WorkspaceManager.privateProps

class WorkspacePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.task("displayPrivateProperties") {
            group = "school"
            description = "Display the key/value pairs stored in private.properties"
            doFirst { println("PrivateProperties : ${project.privateProps}") }
        }
    }
}