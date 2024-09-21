import org.asciidoctor.gradle.jvm.slides.AsciidoctorJRevealJSTask
import workspace.WorkspaceManager.GROUP_TASK_SITE
import workspace.WorkspaceManager.TASK_BAKE_SITE
import workspace.WorkspaceManager.TASK_PUBLISH_SITE
import workspace.WorkspaceManager.bakeDestDirPath
import workspace.WorkspaceManager.bakeSrcPath
import workspace.WorkspaceManager.pushSiteToGhPages
import workspace.WorkspaceUtils.sep
import workspace.jbake.JBakeGhPagesManager.createCnameFile
import workspace.jbake.JBakeGhPagesManager.sitePushDestPath
import workspace.jbake.JBakeGhPagesManager.sitePushPathTo
import workspace.slides.SlidesPlugin.Companion.GROUP_TASK_SLIDER
import workspace.slides.SlidesPlugin.Companion.TASK_ASCIIDOCTOR_REVEALJS
import workspace.slides.SlidesPlugin.Companion.TASK_CLEAN_SLIDES_BUILD

plugins {
    id("org.jbake.site")
    id("org.asciidoctor.jvm.revealjs")
}
apply<workspace.slides.SlidesPlugin>()
apply<workspace.courses.CoursesPlugin>()

repositories { ruby { gems() } }

tasks.getByName<AsciidoctorJRevealJSTask>(TASK_ASCIIDOCTOR_REVEALJS) {
    group = GROUP_TASK_SLIDER
    description = "Slider settings"
    dependsOn(TASK_CLEAN_SLIDES_BUILD)
    revealjs {
        version = "3.1.0"
        templateGitHub {
            setOrganisation("hakimel")
            setRepository("reveal.js")
            setTag("3.9.1")
        }
    }
    revealjsOptions {
        setSourceDir("..$sep..${sep}bibliotheque${sep}slides".let(::File))
        baseDirFollowsSourceFile()
        resources {
            from("$sourceDir${sep}images") {
                include("**")
                into("images")
            }
        }
        attributes(
            mapOf(
                "build-gradle" to layout.projectDirectory.let {
                    "$it${sep}build.gradle.kts"
                }.let(::File),
                "endpoint-url" to "https://talaria-formation.github.io/",
                "source-highlighter" to "coderay",
                "coderay-css" to "style",
                "imagesdir" to ".${sep}images",
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
    doLast { pushSiteToGhPages(sitePushDestPath(), sitePushPathTo()) }
}