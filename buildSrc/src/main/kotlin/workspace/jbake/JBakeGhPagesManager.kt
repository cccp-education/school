package workspace.jbake

import org.gradle.api.Project
import workspace.FileOperationResult
import java.io.File

object JBakeGhPagesManager {
    fun Project.copyFilesTo(
        bakeDirPath: String, repoDir: File
    ): FileOperationResult = try {
        bakeDirPath.also { "bakeDirPath : $it".let(::println) }.let(::File).apply {
            copyRecursively(repoDir, true)
            deleteRecursively()
        }.let { "Est-ce que $it existe après copy & delete? ${it.exists()}" }
            .let(::println)
        FileOperationResult.Success
    } catch (e: Exception) {
        FileOperationResult.Failure(e.message ?: "An error occurred during file copy.")
    }
}