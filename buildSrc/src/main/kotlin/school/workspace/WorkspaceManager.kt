package school.workspace

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.fasterxml.jackson.module.kotlin.readValue
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.PushResult
import org.eclipse.jgit.transport.URIish
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.gradle.api.Project
import school.git.*
import school.git.WorkspaceError.FileNotFound
import school.git.WorkspaceError.ParsingError
import school.jbake.BakeConfiguration
import school.jbake.JBakeGhPagesManager.copyFilesTo
import school.jbake.SiteConfiguration
import school.slides.SlidesConfiguration
import school.workspace.WorkspaceUtils.createDirectory
import school.workspace.WorkspaceUtils.sep
import school.workspace.WorkspaceUtils.yamlMapper
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets.UTF_8
import java.util.*


@Suppress("MemberVisibilityCanBePrivate")
object WorkspaceManager {
    const val CVS_ORIGIN: String = "origin"
    const val CVS_REMOTE: String = "remote"
    const val DNS_CNAME = "CNAME"
    const val WORKSPACE_PATH_KEY = "workspace_path"
    const val TASK_PUBLISH_SITE = "publishSite"
    const val TASK_BAKE_SITE = "bake"
    const val GROUP_TASK_SITE = "site"
    const val CONFIG_PATH_KEY = "managed_config_path"

    val Project.bakeSrcPath: String get() = localConf.bake.srcPath
    val Project.bakeDestDirPath: String get() = localConf.bake.destDirPath
    val Project.workspacePath get() = "$projectDir$sep${properties[WORKSPACE_PATH_KEY]}"
    val Project.localConf: SiteConfiguration
        get() = readSiteConfigurationFile { "$rootDir$sep${properties[CONFIG_PATH_KEY]}" }
    val Map<*, *>.isCnameExists: Boolean
        get() = DNS_CNAME
            .lowercase()
            .let { contains(it) && this[it] is String && this[it] as String != "" }

    @JvmStatic
    fun Project.gradleProperties(key: String = "artifact.version"): String =
        "/workspace/school/gradle.properties"
            .run { "${System.getProperty("user.home")}$this" }
            .let(::File)
            .run {
                Properties().apply {
                    inputStream().use(::load)
                }[key].toString()
            }

    @JvmStatic
    val Project.privateProps: Properties
        get() = Properties().apply {
            "$projectDir/private.properties"
                .let(::File)
                .inputStream()
                .let(::load)
        }


    val Project.workspaceEither: Either<WorkspaceError, Office>
        get() = try {
            workspacePath.let(::File).run {
                when {
                    exists() -> yamlMapper.readValue<Office>(readText(UTF_8)).right()

                    else -> FileNotFound.left()
                }
            }
        } catch (e: Throwable) {
            ParsingError(e.message ?: "Unknown error").left()
        }

    fun Project.printConf() {
        workspaceEither
            .mapLeft { "Error: $it".run(::println) }
            .map {
                it.also(::println)
                    .let(yamlMapper::writeValueAsString)
                    .let(::println)
            }

    }

    fun Project.readSiteConfigurationFile(
        configPath: () -> String
    ): SiteConfiguration = try {
        configPath()
            .run(::File)
            .run(yamlMapper::readValue)
    } catch (e: Exception) {
        // Handle exception or log error
        SiteConfiguration(
            BakeConfiguration(
                "",
                "",
                null
            ),
            GitPushConfiguration(
                "",
                "",
                RepositoryConfiguration(
                    "",
                    "",
                    RepositoryCredentials(
                        "",
                        ""
                    )
                ),
                "",
                ""
            )
        )
    }

    fun Project.readSlideConfigurationFile(
        configPath: () -> String
    ): SlidesConfiguration = try {
        configPath()
            .run(::File)
            .run(yamlMapper::readValue)
    } catch (e: Exception) {
        // Handle exception or log error
        SlidesConfiguration(
            srcPath = "",
            pushPage = GitPushConfiguration(
                "",
                "",
                RepositoryConfiguration(
                    "",
                    "",
                    RepositoryCredentials("", "")
                ),
                "",
                ""
            )
        )
    }

    fun Project.initAddCommitToSite(
        repoDir: File,
        conf: SiteConfiguration,
    ): RevCommit {
        //3) initialiser un repo dans le dossier cvs
        Git.init().setDirectory(repoDir).call().run {
            assert(!repository.isBare)
            assert(repository.directory.isDirectory)
            // add remote repo:
            remoteAdd().apply {
                setName(CVS_ORIGIN)
                setUri(URIish(conf.pushPage.repo.repository))
                // you can add more settings here if needed
            }.call()
            //4) ajouter les fichiers du dossier cvs à l'index
            add().addFilepattern(".").call()
            //5) commit
            return commit().setMessage(conf.pushPage.message).call()
        }
    }

    @Throws(IOException::class)
    fun Project.pushSite(
        repoDir: File,
        conf: SiteConfiguration,
    ): MutableIterable<PushResult>? = FileRepositoryBuilder()
        .setGitDir("${repoDir.absolutePath}$sep.git".let(::File))
        .readEnvironment()
        .findGitDir()
        .setMustExist(true)
        .build()
        .apply {
            config.apply {
                getString(
                    CVS_REMOTE,
                    CVS_ORIGIN,
                    conf.pushPage.repo.repository
                )
            }.save()
            if (isBare) throw IOException("Repo dir should not be bare")
        }
        .let(::Git)
        .run {
            // push to remote:
            return push().setCredentialsProvider(
                UsernamePasswordCredentialsProvider(
                    conf.pushPage.repo.credentials.username,
                    conf.pushPage.repo.credentials.password
                )
            ).apply {
                //you can add more settings here if needed
                remote = CVS_ORIGIN
                isForce = true
            }.call()
        }


    //    "deploy": "gh-pages -d dist -b 'master' history false message 'https://cheroliv.github.io/talaria' repo 'https://github.com/cheroliv/talaria.git https://git:${GITHUB_TOKEN}@github.com/cheroliv/talaria.git' dest '.' ",
    // passer par un workspaceEither pour récupérer le workspace
    fun Project.pushSiteToGhPages(
        destPath: () -> String,
        pathTo: () -> String
    ) = createDirectory(pathTo()).let { it: File ->
        copyFilesTo(destPath(), it)
            .takeIf { it is FileOperationResult.Success }
            ?.run {
                initAddCommitToSite(it, localConf)
                pushSite(it, localConf)
                it.deleteRecursively()
                destPath()
                    .let(::File)
                    .deleteRecursively()
            }
    }

    //TODO: si workspace.conf.path n'existe pas dans gradle properties, alors tu crées l'entrée dedans.
    fun Project.initWorkspace(): Either<WorkspaceError, Unit> = Either.catch {
        initialConf.let {
            workspacePath.let(::File).run {
                if (!exists() && createNewFile())
                    appendText(it.run(yamlMapper::writeValueAsString), UTF_8)
            }
        }
    }.mapLeft { ParsingError(it.message ?: "Unknown error") }

    @JvmStatic
    val Project.initialConf
        get() = mapOf(
            "workspace" to mapOf(
                "human-resources" to null,
                "portfolio" to mapOf(
                    "training-catalogue" to null,
                    "projects" to mapOf(
                        "form" to mapOf("cred" to "${projectDir}/training-institut-2598582b592a.json"),
                        "school" to mapOf(
                            "builds" to mapOf(
                                "frontend" to mapOf(
                                    "path" to "${projectDir}/projects/school/frontend",
                                    "repository" to mapOf(
                                        "from" to "${projectDir}/projects/school/frontend/dist",
                                        "to" to "${projectDir}/build/cvs",
                                        "url" to "somewhere",
                                        "credentials" to mapOf(
                                            "username" to "john.doe@acme.com",
                                            "token" to "smk_051c1781f6b3439aa91083f23d944a23"
                                        ),
                                        "branch" to "master",
                                        "message" to "exemple commit message",
                                        "cname" to "acme.com"
                                    )
                                ),
                                "backoffice" to mapOf(
                                    "path" to "${projectDir}/projects/school/backoffice"
                                ),
                            )
                        ), "jbake-ghpages" to mapOf(
                            "builds" to mapOf(
                                "frontend" to mapOf(
                                    "path" to "${projectDir}/codebase/kotlin/jbake-ghpages/site"
                                ), "jbake" to mapOf(
                                    "path" to "${projectDir}/codebase/kotlin/jbake-ghpages"
                                )
                            )
                        )
                    )
                )
            )
        )

    fun createRepoDir(path: String): File = path
        .let(::File)
        .apply {
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
}