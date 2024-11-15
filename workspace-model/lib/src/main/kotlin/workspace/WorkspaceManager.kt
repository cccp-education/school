@file:Suppress("unused")

package workspace

import workspace.Workspace.InstallationType.ALL_IN_ONE
import workspace.Workspace.InstallationType.SEPARATED_FOLDERS
import workspace.Workspace.WorkspaceConfig
import java.io.File
import java.nio.file.Path
import kotlin.io.path.pathString

object WorkspaceManager {
    /**
    user scenarios :
     * ALL_IN_ONE
     * SEPARATED_FOLDERS
     */
    fun createWorkspace(config: WorkspaceConfig)
            : WorkspaceConfig = when (config.type) {
        ALL_IN_ONE -> config.createAllInOneFolder(config.basePath)
        SEPARATED_FOLDERS -> config.createSeparatedFolders(config.basePath)
    }.also { createConfigFiles(config.basePath) }

    private fun WorkspaceConfig.createAllInOneFolder(basePath: Path): WorkspaceConfig = listOf(
        "office",
        "education",
        "communication",
        "configuration",
        "job"
    ).forEach { dir -> basePath.resolve(dir).run(WorkspaceManager::createDirectory) }
        .let { this@createAllInOneFolder }

    private fun WorkspaceConfig.createSeparatedFolders(
        basePath: Path,
    ): WorkspaceConfig = /*TODO: va chercher les valeurs des fileChoosers*/
//        school.base.utils.Log.i("basePath.pathString : ${basePath.pathString}")
        println("basePath.pathString : ${basePath.pathString}")

            .let { this@createSeparatedFolders }


    private fun createDirectory(path: Path) = path.toFile().apply {
        when {
            !exists() -> mkdirs()
        }
    }


    private fun createConfigFiles(basePath: Path) =
        // Création des fichiers de configuration nécessaires
        File(basePath.toFile(), "application.properties").writeText(
            """
            school.workspace.path=${basePath}
            # Add other configuration properties here
        """.trimIndent()
        ).apply {

        }
}