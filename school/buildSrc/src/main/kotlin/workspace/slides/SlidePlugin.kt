package workspace.slides

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.repositories
import java.io.File

@Suppress("unused")
class SlidePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.repositories {
            mavenCentral()
            gradlePluginPortal()

        }

        project.tasks.register<DefaultTask>("cleanSlidesBuild") {
            group = "slider"
            description = "Delete generated presentation in build directory."
            doFirst {
                "${project.layout.buildDirectory}/docs/asciidocRevealJs".run {
                    "$this/images"
                        .let(::File)
                        .deleteRecursively()
                    let(::File)
                        .listFiles()
                        ?.filter { it.isFile && it.name.endsWith(".html") }
                        ?.forEach { it.delete() }
                }
            }
        }
    }
}
