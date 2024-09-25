import org.asciidoctor.gradle.jvm.slides.AsciidoctorJRevealJSTask
import school.jbake.JBakeGhPagesManager.createCnameFile
import school.jbake.JBakeGhPagesManager.sitePushDestPath
import school.jbake.JBakeGhPagesManager.sitePushPathTo
import school.slides.SlidesPlugin.RevealJsSlides.BUILD_GRADLE_KEY
import school.slides.SlidesPlugin.RevealJsSlides.CODERAY_CSS_KEY
import school.slides.SlidesPlugin.RevealJsSlides.DOCINFO_KEY
import school.slides.SlidesPlugin.RevealJsSlides.ENDPOINT_URL_KEY
import school.slides.SlidesPlugin.RevealJsSlides.GROUP_TASK_SLIDER
import school.slides.SlidesPlugin.RevealJsSlides.ICONS_KEY
import school.slides.SlidesPlugin.RevealJsSlides.IDPREFIX_KEY
import school.slides.SlidesPlugin.RevealJsSlides.IDSEPARATOR_KEY
import school.slides.SlidesPlugin.RevealJsSlides.IMAGEDIR_KEY
import school.slides.SlidesPlugin.RevealJsSlides.REVEALJS_HISTORY_KEY
import school.slides.SlidesPlugin.RevealJsSlides.REVEALJS_SLIDENUMBER_KEY
import school.slides.SlidesPlugin.RevealJsSlides.REVEALJS_THEME_KEY
import school.slides.SlidesPlugin.RevealJsSlides.REVEALJS_TRANSITION_KEY
import school.slides.SlidesPlugin.RevealJsSlides.SETANCHORS_KEY
import school.slides.SlidesPlugin.RevealJsSlides.SOURCE_HIGHLIGHTER_KEY
import school.slides.SlidesPlugin.RevealJsSlides.TASK_ASCIIDOCTOR_REVEALJS
import school.slides.SlidesPlugin.RevealJsSlides.TASK_CLEAN_SLIDES_BUILD
import school.slides.SlidesPlugin.RevealJsSlides.TOC_KEY
import school.workspace.WorkspaceManager.GROUP_TASK_SITE
import school.workspace.WorkspaceManager.TASK_BAKE_SITE
import school.workspace.WorkspaceManager.TASK_PUBLISH_SITE
import school.workspace.WorkspaceManager.bakeDestDirPath
import school.workspace.WorkspaceManager.bakeSrcPath
import school.workspace.WorkspaceManager.pushSiteToGhPages
import school.workspace.WorkspaceUtils.sep

plugins {
    id("org.jbake.site")
    id("org.asciidoctor.jvm.revealjs")
}
apply<school.slides.SlidesPlugin>()
apply<school.courses.CoursesPlugin>()

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
        "..$sep..${sep}bibliotheque${sep}slides"
            .let(::File)
            .let(::setSourceDir)
        baseDirFollowsSourceFile()
        resources {
            from("$sourceDir${sep}images") {
                include("**")
                into("images")
            }
        }
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
        ).let(::attributes)
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