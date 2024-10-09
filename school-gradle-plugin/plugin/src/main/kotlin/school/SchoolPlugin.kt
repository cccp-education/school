package org.example

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * A simple 'hello world' plugin.
 */
class SchoolPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        // Register a task
        project.tasks.register("greeting") { task ->
            task.doLast { println("Hello from plugin 'school-gradle-plugin'") }
        }
    }
}
