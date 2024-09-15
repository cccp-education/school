package workspace.slides

import org.gradle.api.Project
import workspace.FileOperationResult
import workspace.WorkspaceManager
import workspace.WorkspaceManager.initAddCommit
import workspace.WorkspaceManager.localConf
import workspace.WorkspaceManager.push
import java.io.File
import java.util.*

//TODO: deploy slides to a repo per whole training program https://github.com/talaria-formation/prepro-cda.git

object SlideManager {

    val Project.slideSrcPath: String get() = "${layout.buildDirectory.get().asFile.absolutePath}/docs/asciidocRevealJs/"
    val Project.slideDestDirPath: String get() = localConf.bake.destDirPath

    fun Project.deckFile(key: String): String = buildString {
        append("build/docs/asciidocRevealJs/")
        append(Properties().apply {
            "$projectDir/deck.properties"
                .let(::File)
                .inputStream()
                .use(::load)
        }[key].toString())
    }

    fun Project.pushSlides(
        destPath: () -> String,
        pathTo: () -> String
    ) = pathTo()
        .run(WorkspaceManager::createRepoDir)
        .let { it: File ->
            copySlideFilesToRepo(destPath(), it)
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

    fun copySlideFilesToRepo(
        slidesDirPath: String,
        repoDir: File
    ): FileOperationResult = try {
        slidesDirPath
            .let(::File)
            .apply {
                when {
                    !copyRecursively(
                        repoDir,
                        true
                    ) -> throw Exception("Unable to copy slides directory to build directory")
                }
            }.deleteRecursively()
        FileOperationResult.Success
    } catch (e: Exception) {
        FileOperationResult.Failure(e.message ?: "An error occurred during file copy.")
    }
}