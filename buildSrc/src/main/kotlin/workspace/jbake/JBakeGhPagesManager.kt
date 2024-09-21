package workspace.jbake

import org.gradle.api.Project
import workspace.FileOperationResult
import workspace.FileOperationResult.Failure
import workspace.FileOperationResult.Success
import workspace.RepositoryConfiguration.Companion.CNAME
import workspace.WorkspaceManager.bakeDestDirPath
import workspace.WorkspaceManager.localConf
import workspace.WorkspaceUtils.sep
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8

object JBakeGhPagesManager {
    fun Project.copyFilesTo(
        bakeDirPath: String, repoDir: File
    ): FileOperationResult = try {
        bakeDirPath
            .let(::File)
            .apply {
                copyRecursively(repoDir, true)
                deleteRecursively()
            }
        Success
    } catch (e: Exception) {
        Failure(e.message ?: "An error occurred during file copy.")
    }

    fun Project.sitePushDestPath(): () -> String =
        { "${layout.buildDirectory.get().asFile.absolutePath}$sep$bakeDestDirPath" }

    fun Project.sitePushPathTo(): () -> String =
        { "${layout.buildDirectory.get().asFile.absolutePath}$sep${localConf.pushPage.to}" }

    fun Project.cnamePath() =
        "${project.layout.buildDirectory.get().asFile.absolutePath}$sep${localConf.bake.destDirPath}$sep$CNAME"

    fun Project.createCnameFile() {
        when {
            localConf.bake.cname != null && localConf.bake.cname!!.isNotBlank() ->
                cnamePath()
                    .let(::File)
                    .run {
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
}