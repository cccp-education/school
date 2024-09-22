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
import workspace.slides.SlidesPlugin.RevealJsSlides.BUILD_GRADLE_KEY
import workspace.slides.SlidesPlugin.RevealJsSlides.CODERAY_CSS_KEY
import workspace.slides.SlidesPlugin.RevealJsSlides.DOCINFO_KEY
import workspace.slides.SlidesPlugin.RevealJsSlides.ENDPOINT_URL_KEY
import workspace.slides.SlidesPlugin.RevealJsSlides.GROUP_TASK_SLIDER
import workspace.slides.SlidesPlugin.RevealJsSlides.ICONS_KEY
import workspace.slides.SlidesPlugin.RevealJsSlides.IDPREFIX_KEY
import workspace.slides.SlidesPlugin.RevealJsSlides.IDSEPARATOR_KEY
import workspace.slides.SlidesPlugin.RevealJsSlides.IMAGEDIR_KEY
import workspace.slides.SlidesPlugin.RevealJsSlides.REVEALJS_HISTORY_KEY
import workspace.slides.SlidesPlugin.RevealJsSlides.REVEALJS_SLIDENUMBER_KEY
import workspace.slides.SlidesPlugin.RevealJsSlides.REVEALJS_THEME_KEY
import workspace.slides.SlidesPlugin.RevealJsSlides.REVEALJS_TRANSITION_KEY
import workspace.slides.SlidesPlugin.RevealJsSlides.SETANCHORS_KEY
import workspace.slides.SlidesPlugin.RevealJsSlides.SOURCE_HIGHLIGHTER_KEY
import workspace.slides.SlidesPlugin.RevealJsSlides.TASK_ASCIIDOCTOR_REVEALJS
import workspace.slides.SlidesPlugin.RevealJsSlides.TASK_CLEAN_SLIDES_BUILD
import workspace.slides.SlidesPlugin.RevealJsSlides.TOC_KEY

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
                BUILD_GRADLE_KEY to layout
                    .projectDirectory
                    .let { "$it${sep}build.gradle.kts" }
                    .let(::File),
                ENDPOINT_URL_KEY to "https://talaria-formation.github.io/",
                SOURCE_HIGHLIGHTER_KEY to "coderay",
                CODERAY_CSS_KEY to "style",
                IMAGEDIR_KEY to ".${sep}images",
                TOC_KEY to "left",
                ICONS_KEY to "font",
                SETANCHORS_KEY to "",
                IDPREFIX_KEY to "slide-",
                IDSEPARATOR_KEY to "-",
                DOCINFO_KEY to "shared",
                REVEALJS_THEME_KEY to "black",
                REVEALJS_TRANSITION_KEY to "linear",
                REVEALJS_HISTORY_KEY to "true",
                REVEALJS_SLIDENUMBER_KEY to "true"
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