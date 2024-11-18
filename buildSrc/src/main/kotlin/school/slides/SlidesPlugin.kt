package school.slides

import org.asciidoctor.gradle.jvm.AsciidoctorTask
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.repositories
import school.slides.SlidesManager.deckFile
import school.workspace.WorkspaceManager.localConf
import school.workspace.WorkspaceUtils.yamlMapper
import java.io.File

class SlidesPlugin : Plugin<Project> {
    object RevealJsSlides {
        const val GROUP_TASK_SLIDER = "slider"
        const val TASK_ASCIIDOCTOR_REVEALJS = "asciidoctorRevealJs"
        const val TASK_CLEAN_SLIDES_BUILD = "cleanSlidesBuild"
        const val BUILD_GRADLE_KEY = "build-gradle"
        const val ENDPOINT_URL_KEY = "endpoint-url"
        const val SOURCE_HIGHLIGHTER_KEY = "source-highlighter"
        const val CODERAY_CSS_KEY = "coderay-css"
        const val IMAGEDIR_KEY = "imagesdir"
        const val TOC_KEY = "toc"
        const val ICONS_KEY = "icons"
        const val SETANCHORS_KEY = "setanchors"
        const val IDPREFIX_KEY = "idprefix"
        const val IDSEPARATOR_KEY = "idseparator"
        const val DOCINFO_KEY = "docinfo"
        const val REVEALJS_THEME_KEY = "revealjs_theme"
        const val REVEALJS_TRANSITION_KEY = "revealjs_transition"
        const val REVEALJS_HISTORY_KEY = "revealjs_history"
        const val REVEALJS_SLIDENUMBER_KEY = "revealjs_slideNumber"
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
                project.localConf
                    .let(project.yamlMapper::writeValueAsString)
                    .let(::println)

//                project.workspaceEither.fold(
//                    { "Error: $it".run(::println) },
//                    { it: Office ->
//
//                        it.also(::println)
//                            .let(project.yamlMapper::writeValueAsString)
//                            .let(::println)
//                    }
//                )
//                println("path :\n\t${project.layout.buildDirectory.get().asFile.absolutePath}/docs/asciidocRevealJs/")
//                project.slideSrcPath
//                    .let(::File)
//                    .listFiles()!!
////            .forEach { it.name.let(::println) }
////            pushSlides(destPath = { slideDestDirPath },
////                pathTo = { "${layout.buildDirectory.get().asFile.absolutePath}${getDefault().separator}${localConf.pushPage.to}" })
//                println("Affiche la config slide")
//                project.printConf()
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
