package workspace

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
import workspace.WorkspaceError.FileNotFound
import workspace.WorkspaceError.ParsingError
import workspace.WorkspaceUtils.copyFilesTo
import workspace.WorkspaceUtils.createDirectory
import workspace.WorkspaceUtils.sep
import workspace.WorkspaceUtils.yamlMapper
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8


@Suppress("MemberVisibilityCanBePrivate")
object WorkspaceManager {

    const val CVS_ORIGIN: String = "origin"
    const val CVS_REMOTE: String = "remote"
    const val DNS_CNAME = "CNAME"
    const val WORKSPACE_PATH_KEY = "workspace_path"

    val Project.bakeSrcPath: String get() = localConf.bake.srcPath

    val Project.bakeDestDirPath: String get() = localConf.bake.destDirPath

    val Project.workspacePath get() = "$projectDir$sep${properties[WORKSPACE_PATH_KEY]}"

    val Project.localConf: SiteConfiguration
        get() = readSiteConfigurationFile { "$rootDir$sep${properties["managed_config_path"]}" }

    val Map<*, *>.isCnameExists: Boolean
        get() = contains("cname") &&
                this["cname"] is String &&
                this["cname"] as String != ""

    val Project.workspaceEither: Either<WorkspaceError, Bureau>
        get() = try {
            workspacePath.let(::File).run {
                when {
                    exists() -> yamlMapper.readValue<Bureau>(readText(UTF_8)).right()

                    else -> FileNotFound.left()
                }
            }
        } catch (e: Throwable) {
            ParsingError(e.message ?: "Unknown error").left()
        }

    fun Project.printConf() {
        workspaceEither.fold({ "Error: $it".run(::println) }, {
            it.also(::println)
                .let(yamlMapper::writeValueAsString)
                .let(::println)
        })
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
            BakeConfiguration("", "", null), GitPushConfiguration(
                "", "", RepositoryConfiguration("", "", RepositoryCredentials("", "")), "", ""
            )
        )
    }

    fun Project.initAddCommit(
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

    fun Project.push(
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
            if (!isBare) throw Exception("Repo is not bare")
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
    fun Project.pushPages(
        destPath: () -> String,
        pathTo: () -> String
    ) = createDirectory(pathTo()).let { it: File ->
        copyFilesTo(destPath(), it)
            .takeIf { it is FileOperationResult.Success }
            ?.run {
                initAddCommit(it, localConf)
                push(it, localConf)
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
}

