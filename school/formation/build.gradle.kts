//@file:Suppress("ImplicitThis")

import org.asciidoctor.gradle.jvm.slides.AsciidoctorJRevealJSTask
import workspace.WorkspaceManager.GROUP_TASK_SITE
import workspace.WorkspaceManager.TASK_BAKE_SITE
import workspace.WorkspaceManager.TASK_PUBLISH_SITE
import workspace.WorkspaceManager.bakeDestDirPath
import workspace.WorkspaceManager.bakeSrcPath
import workspace.WorkspaceManager.createCnameFile
import workspace.WorkspaceManager.localConf
import workspace.WorkspaceManager.pushPages
import workspace.slides.SlidesPlugin.Companion.GROUP_TASK_SLIDER
import workspace.slides.SlidesPlugin.Companion.TASK_CLEAN_SLIDES_BUILD
import java.nio.file.FileSystems.getDefault

plugins {
    id("org.jbake.site")
    id("org.asciidoctor.jvm.revealjs")
}
apply<workspace.slides.SlidesPlugin>()
apply<workspace.courses.CoursesPlugin>()

repositories { ruby { gems() } }

tasks.getByName<AsciidoctorJRevealJSTask>(TASK_CLEAN_SLIDES_BUILD) {
    group = GROUP_TASK_SLIDER
    description = "Slider settings"
    dependsOn("cleanSlidesBuild")
    revealjs {
        version = "3.1.0"
        templateGitHub {
            setOrganisation("hakimel")
            setRepository("reveal.js")
            setTag("3.9.1")
        }
    }
    revealjsOptions {
        setSourceDir(File("../../../bibliotheque/slides"))
        baseDirFollowsSourceFile()
        resources {
            from("$sourceDir/images") {
                include("**")
                into("images")
            }
        }
        attributes(
            mapOf(
                "build-gradle" to layout.projectDirectory.let { "$it/build.gradle.kts" }.let(::File),
                "endpoint-url" to "https://talaria-formation.github.io/",
                "source-highlighter" to "coderay",
                "coderay-css" to "style",
                "imagesdir" to "./images",
                "toc" to "left",
                "icons" to "font",
                "setanchors" to "",
                "idprefix" to "slide-",
                "idseparator" to "-",
                "docinfo" to "shared",
                "revealjs_theme" to "black",
                "revealjs_transition" to "linear",
                "revealjs_history" to "true",
                "revealjs_slideNumber" to "true"
            )
        )
    }
}

tasks.register<DefaultTask>(TASK_PUBLISH_SITE) {
    group = GROUP_TASK_SITE
    description = "Publish site online."
    dependsOn(TASK_BAKE_SITE)
    doFirst { createCnameFile() }
    jbake {
        srcDirName = bakeSrcPath
        destDirName = bakeDestDirPath
    }
    doLast {
        pushPages(destPath = { "${layout.buildDirectory.get().asFile.absolutePath}${getDefault().separator}$bakeDestDirPath" },
            pathTo = { "${layout.buildDirectory.get().asFile.absolutePath}${getDefault().separator}${localConf.pushPage.to}" })
    }
}