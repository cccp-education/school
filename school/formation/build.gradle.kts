@file:Suppress("ImplicitThis")

import com.fasterxml.jackson.module.kotlin.readValue
import org.asciidoctor.gradle.jvm.AsciidoctorTask
import org.asciidoctor.gradle.jvm.slides.AsciidoctorJRevealJSTask
import workspace.WorkspaceManager.bakeDestDirPath
import workspace.WorkspaceManager.bakeSrcPath
import workspace.WorkspaceManager.createCnameFile
import workspace.WorkspaceManager.localConf
import workspace.WorkspaceManager.printConf
import workspace.WorkspaceManager.pushPages
import workspace.WorkspaceUtils.yamlMapper
import workspace.courses.Courses.JSON_FILE
import workspace.courses.Courses.ROOT_NODE
import workspace.courses.DirectoryStructure
import workspace.slides.SlideManager.deckFile
import workspace.slides.SlideManager.slideSrcPath
import java.nio.file.FileSystems.getDefault

plugins {
    idea
    id("org.jbake.site")
    id("org.asciidoctor.jvm.gems")
    id("org.asciidoctor.jvm.revealjs")
}

apply<workspace.slides.SlidePlugin>()

repositories { ruby { gems() } }

tasks.getByName<AsciidoctorJRevealJSTask>("asciidoctorRevealJs") {
    group = "slider"
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

tasks.register<AsciidoctorTask>("asciidoctor") {
    group = "slider"
    dependsOn(tasks.asciidoctorRevealJs)
}

//tasks.register<DefaultTask>("cleanBuild") {
//    group = "slider"
//    description = "Delete generated presentation in build directory."
//    doFirst {
//        "${layout.buildDirectory}/docs/asciidocRevealJs".run {
//            "$this/images"
//                .let(::File)
//                .deleteRecursively()
//            let(::File)
//                .listFiles()
//                ?.filter { it.isFile && it.name.endsWith(".html") }
//                ?.forEach { it.delete() }
//        }
//    }
//}

tasks.register<DefaultTask>("deploySlides") {
    group = "slider"
    description = "Deploy sliders to remote repository"
    dependsOn("asciidoctor")
    doFirst { println("Task description :\n\t$description") }
    doLast {
        println("path :\n\t${layout.buildDirectory.get().asFile.absolutePath}/docs/asciidocRevealJs/")
        slideSrcPath
            .let(::File)
            .listFiles()!!
//            .forEach { it.name.let(::println) }
//            pushSlides(destPath = { slideDestDirPath },
//                pathTo = { "${layout.buildDirectory.get().asFile.absolutePath}${getDefault().separator}${localConf.pushPage.to}" })
        println("Affiche la config slide")
        printConf()

    }
}

tasks.register<Exec>("openFirefox") {
    group = "slider"
    description = "Open the default.deck.file presentation in firefox"
    dependsOn("asciidoctor")
    commandLine("firefox", deckFile("default.deck.file"))
    workingDir = layout.projectDirectory.asFile
}

tasks.register<Exec>("openChromium") {
    group = "slider"
    description = "Open the default.deck.file presentation in chromium"
    dependsOn("asciidoctor")
    commandLine("chromium", deckFile("default.deck.file"))
    workingDir = layout.projectDirectory.asFile
}

tasks.register<Exec>("asciidocCapsule") {
    group = "capsule"
    dependsOn("asciidoctor")
    commandLine("chromium", deckFile("asciidoc.capsule.deck.file"))
    workingDir = layout.projectDirectory.asFile
}

tasks.register<DefaultTask>("publishSite") {
    group = "site"
    description = "Publish site online."
    dependsOn("bake")
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

tasks.register<DefaultTask>("schoolProcess") {
    group = "school"
    description = "Processes used in school."
    val text = """
La formation de pré-professionalisation "Concepteur Développeur d’Applications" est conçue pour préparer les futurs professionnels du développement logiciel en leur offrant une maîtrise complète des outils et techniques essentiels. Ce programme s'articule autour de plusieurs modules clés, chacun visant à développer des compétences spécifiques et nécessaires pour exceller dans le domaine.

Le premier module se concentre sur la communication, une compétence indispensable pour tout professionnel. Les participants apprendront à exploiter efficacement les moteurs de recherche et les réseaux sociaux, ainsi qu'à utiliser des outils avancés comme ChatGPT pour améliorer leur communication écrite et leur capacité à rédiger des prompts efficaces.

La formation inclut également une partie dédiée à l'outillage informatique, où les apprenants seront guidés dans la mise en place et la gestion de leur environnement de travail. Ce module leur permettra de maîtriser l'utilisation des logiciels essentiels et d'adopter les bonnes pratiques pour une productivité optimale.

Le programme se poursuit avec des sessions sur la rédaction de documents techniques en utilisant AsciiDoc, ainsi que sur la création de diagrammes professionnels avec PlantUML. Ces compétences sont cruciales pour documenter et présenter les projets de manière claire et professionnelle.

Enfin, la formation aborde les techniques de présentation, en utilisant des outils comme Gradle et RevealJs, permettant aux participants de créer des présentations dynamiques et interactives.

En résumé, cette formation offre une préparation complète pour ceux qui aspirent à devenir concepteurs développeurs d’applications, en leur fournissant les compétences techniques et communicationnelles nécessaires pour réussir dans ce domaine en constante évolution.
"""

    fun String.wordCount(): Int = replace(
        "[^a-zA-Z\\s]".toRegex(), ""
    ).split("\\s+".toRegex()).size
    doFirst { "word count : ${text.wordCount()}".let(::println) }
}

tasks.register<DefaultTask>("createPatronFormation") {
    group = "school"
    description = "Create patron formation in build folder."
    doLast {
        // Fonction récursive pour créer l'arborescence
        fun File.createStructure(structure: DirectoryStructure?) {
            structure!!.apply {
// Créer les fichiers
                files.forEach { fileName ->
                    File(this@createStructure, fileName).apply {
                        when {
                            !exists() -> createNewFile()
                        }
                    }

                }
// Créer les répertoires et leur contenu
                directories.forEach { (dirName, subStructure) ->
                    File(this@createStructure, dirName).apply {
                        when {
                            !exists() -> mkdirs()
                        }
                        createStructure(subStructure)
                    }
                }
            }
        }

// Démarrer la création de l'arborescence depuis le dossier de formation
        yamlMapper.readValue<Map<String, DirectoryStructure>>(
            "${layout.projectDirectory}/$JSON_FILE".let(::File)
        )[ROOT_NODE].let(
            File(
                layout.buildDirectory.get().asFile, ROOT_NODE
            ).apply {
                when {
                    !exists() -> mkdirs()
                }
            }::createStructure
        )
    }
}