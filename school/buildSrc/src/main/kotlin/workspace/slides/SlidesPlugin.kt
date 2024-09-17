package workspace.slides

import org.asciidoctor.gradle.jvm.AsciidoctorTask
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.repositories
import workspace.WorkspaceManager.printConf
import workspace.slides.SlidesManager.deckFile
import workspace.slides.SlidesManager.slideSrcPath
import java.io.File

@Suppress("unused")
class SlidesPlugin : Plugin<Project> {
    companion object {
        const val GROUP_TASK_SLIDER = "slider"
        const val TASK_ASCIIDOCTOR_REVEALJS = "asciidoctorRevealJs"
        const val TASK_CLEAN_SLIDES_BUILD = "cleanSlidesBuild"
    }

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

        project.tasks.register<Exec>("openFirefox") {
            group = "slider"
            description = "Open the default.deck.file presentation in firefox"
            dependsOn("asciidoctor")
            commandLine("firefox", project.deckFile("default.deck.file"))
            workingDir = project.layout.projectDirectory.asFile
        }

        project.tasks.register<Exec>("openChromium") {
            group = "slider"
            description = "Open the default.deck.file presentation in chromium"
            dependsOn("asciidoctor")
            commandLine("chromium", project.deckFile("default.deck.file"))
            workingDir = project.layout.projectDirectory.asFile
        }


        project.tasks.register<DefaultTask>("deploySlides") {
            group = "slider"
            description = "Deploy sliders to remote repository"
            dependsOn("asciidoctor")
            doFirst { println("Task description :\n\t$description") }
            doLast {
                println("path :\n\t${project.layout.buildDirectory.get().asFile.absolutePath}/docs/asciidocRevealJs/")
                project.slideSrcPath
                    .let(::File)
                    .listFiles()!!
//            .forEach { it.name.let(::println) }
//            pushSlides(destPath = { slideDestDirPath },
//                pathTo = { "${layout.buildDirectory.get().asFile.absolutePath}${getDefault().separator}${localConf.pushPage.to}" })
                println("Affiche la config slide")
                project.printConf()
            }
        }

        project.tasks.register<AsciidoctorTask>("asciidoctor") {
            group = "slider"
            dependsOn(project.tasks.findByPath("asciidoctorRevealJs"))
        }

        project.tasks.register<Exec>("asciidocCapsule") {
            group = "capsule"
            dependsOn("asciidoctor")
            commandLine("chromium", project.deckFile("asciidoc.capsule.deck.file"))
            workingDir = project.layout.projectDirectory.asFile
        }
    }
}
