package workspace.database

import org.gradle.api.Plugin
import org.gradle.api.Project

//TODO: tasks:
// - startDatabase
// - stopDatabase
// - backupDatabase
// - recoverDatabase

@Suppress("unused")
class DatabasePlugin : Plugin<Project> {
    override fun apply(project: Project) {

        project.task("startProjectManager") {
            group = "Project"
            description = "Start Project Manager."
            doFirst { println(":startProjectManager:doFirst : ") }
            doLast { println(":startProjectManager:doLast : ") }
        }
    }
}