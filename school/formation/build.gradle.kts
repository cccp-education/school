@file:Suppress("ImplicitThis")

import Build_gradle.Formation.JSON_FILE
import Build_gradle.Formation.ROOT_NODE
import Build_gradle.RepositoryConfiguration.Companion.CNAME
import Build_gradle.RepositoryConfiguration.Companion.ORIGIN
import Build_gradle.RepositoryConfiguration.Companion.REMOTE
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.asciidoctor.gradle.jvm.AsciidoctorTask
import org.asciidoctor.gradle.jvm.slides.AsciidoctorJRevealJSTask
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.Git.init
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.PushResult
import org.eclipse.jgit.transport.URIish
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.FileSystems.getDefault
import java.util.*


plugins {
    idea
    id("org.jbake.site")
    id("org.asciidoctor.jvm.gems")
    id("org.asciidoctor.jvm.revealjs")
}

repositories { ruby { gems() } }


//TODO: deploy slides to a repo per whole training program https://github.com/talaria-formation/prepro-cda.git


tasks.getByName<AsciidoctorJRevealJSTask>("asciidoctorRevealJs") {
    group = "slider"
    description = "Slider settings"
    dependsOn("cleanBuild")
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

fun Project.deckFile(key: String): String = buildString {
    append("build/docs/asciidocRevealJs/")
    append(Properties().apply {
        "$projectDir/deck.properties".let(::File).inputStream().use(::load)
    }[key].toString())
}


tasks.register("cleanBuild") {
    group = "slider"
    description = "Delete generated presentation in build directory."
    doFirst {
        "${layout.buildDirectory}/docs/asciidocRevealJs".run {
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

tasks.register("publishSite") {
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

data class GitPushConfiguration(
    val from: String,
    val to: String,
    val repo: RepositoryConfiguration,
    val branch: String,
    val message: String,
)

data class RepositoryConfiguration(
    val name: String,
    val repository: String,
    val credentials: RepositoryCredentials,
) {
    companion object {
        const val ORIGIN = "origin"
        const val CNAME = "CNAME"
        const val REMOTE = "remote"
    }
}

data class RepositoryCredentials(val username: String, val password: String)

data class SiteConfiguration(
    val bake: BakeConfiguration,
    val pushPage: GitPushConfiguration,
    val pushSource: GitPushConfiguration? = null,
    val pushTemplate: GitPushConfiguration? = null,
)

data class BakeConfiguration(
    val srcPath: String,
    val destDirPath: String,
    val cname: String?,
)

sealed class FileOperationResult {
    sealed class GitOperationResult {
        data class Success(
            val commit: RevCommit, val pushResults: MutableIterable<PushResult>?
        ) : GitOperationResult()

        data class Failure(val error: String) : GitOperationResult()
    }

    object Success : FileOperationResult()
    data class Failure(val error: String) : FileOperationResult()
}


val mapper: ObjectMapper by lazy {
    YAMLFactory().let(::ObjectMapper).disable(WRITE_DATES_AS_TIMESTAMPS).registerKotlinModule()
}


val localConf: SiteConfiguration by lazy {
    readSiteConfigurationFile {
        "$rootDir${getDefault().separator}${properties["managed_config_path"]}"
    }
}


fun readSiteConfigurationFile(
    configPath: () -> String
): SiteConfiguration = try {
    configPath().let(::File).let(mapper::readValue)
} catch (e: Exception) {
// Handle exception or log error
    SiteConfiguration(
        BakeConfiguration("", "", null), GitPushConfiguration(
            "", "", RepositoryConfiguration(
                "", "", RepositoryCredentials("", "")
            ), "", ""
        )
    )
}


val bakeSrcPath: String get() = localConf.bake.srcPath


val bakeDestDirPath: String get() = localConf.bake.destDirPath


fun createCnameFile() {
    when {
        localConf.bake.cname != null && localConf.bake.cname!!.isNotBlank() -> "${project.layout.buildDirectory.get().asFile.absolutePath}${
            getDefault().separator
        }${
            localConf.bake.destDirPath
        }${getDefault().separator}$CNAME".let(::File).run {
            when {
                exists() && isDirectory -> deleteRecursively()
                exists() -> delete()
            }
            when {
                exists() -> throw Exception("Destination path should exists : $this")
                !createNewFile() -> throw Exception("Can't create path : $this")
                else -> {
                    appendText(localConf.bake.cname ?: "", UTF_8)
                    if ((exists() && !isDirectory).not()) throw Exception("Destination created but not a directory : $this")
                }
            }
        }
    }
}


fun createRepoDir(path: String): File = path.let(::File).apply {
    when {
        exists() && !isDirectory -> when {
            !delete() -> throw Exception("Cant delete file named like repo dir")
        }
    }
    when {
        exists() -> when {
            !deleteRecursively() -> throw Exception("Cant delete current repo dir")
        }
    }
    when {
        exists() -> throw Exception("Repo dir should not already exists")
        !exists() -> when {
            !mkdir() -> throw Exception("Cant create repo dir")
        }
    }
}


fun copyBakedFilesToRepo(
    bakeDirPath: String, repoDir: File
): FileOperationResult = try {
    bakeDirPath.let(::File).apply {
        when {
            !copyRecursively(repoDir, true) -> throw Exception("Unable to copy baked directory to build directory")
        }
    }.deleteRecursively()
    FileOperationResult.Success
} catch (e: Exception) {
    FileOperationResult.Failure(e.message ?: "An error occurred during file copy.")
}

fun initAddCommit(
    repoDir: File,
    conf: SiteConfiguration,
): RevCommit {
    init()
        .setDirectory(repoDir)
        .call()
        .run {
            when {
                repository.isBare -> throw Exception("Repository should not be bare")
                !repository.directory.isDirectory -> throw Exception("Repository should be a directory")
                else -> {
                    remoteAdd().apply {
                        setName(ORIGIN)
                        setUri(URIish(conf.pushPage.repo.repository))
                    }.call()
                    add().addFilepattern(".").call()
                    return commit().setMessage(conf.pushPage.message).call()
                }
            }
        }
}


fun push(repoDir: File, conf: SiteConfiguration): MutableIterable<PushResult>? = FileRepositoryBuilder().setGitDir(
    "${repoDir.absolutePath}${getDefault().separator}.git".let(::File)
).readEnvironment()
    .findGitDir()
    .setMustExist(true)
    .build()
    .also {
        it.config.apply { getString(REMOTE, ORIGIN, conf.pushPage.repo.repository) }.save()
// No previous commit in branch
//    if (!it.isBare) throw Exception("Repo dir should be bare")
    }.let(::Git)
    .run {
        push().apply {
            setCredentialsProvider(
                UsernamePasswordCredentialsProvider(
                    conf.pushPage.repo.credentials.username,
                    conf.pushPage.repo.credentials.password
                )
            )
            remote = ORIGIN
            isForce = true
        }.call()
    }


fun pushPages(
    destPath: () -> String, pathTo: () -> String
) = pathTo().run(::createRepoDir).let { it: File ->
    copyBakedFilesToRepo(destPath(), it).takeIf { it is FileOperationResult.Success }?.run {
        initAddCommit(it, localConf)
        push(it, localConf)
        it.deleteRecursively()
        destPath().let(::File).deleteRecursively()
    }
}



tasks.register("schoolProcess") {
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


data class DirectoryStructure(
    val files: List<String> = emptyList(), val directories: Map<String, DirectoryStructure> = emptyMap()
)

object Formation {
    const val JSON_FILE = "patron-formation.json"
    const val ROOT_NODE = "formation"
}

tasks.register("createPatronFormation") {
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
        mapper.readValue<Map<String, DirectoryStructure>>(
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
