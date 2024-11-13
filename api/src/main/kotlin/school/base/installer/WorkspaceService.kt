package school.base.installer

import school.base.installer.WorkspaceService.InstallationType.ALL_IN_ONE
import school.base.installer.WorkspaceService.InstallationType.SEPARATED_FOLDERS
import java.io.File
import java.nio.file.Path

// Service class for workspace operations
class WorkspaceService {
    fun createWorkspace(config: WorkspaceConfig) {
        when (config.type) {
            ALL_IN_ONE -> createAllInOneWorkspace(config.basePath)
            SEPARATED_FOLDERS -> createSeparatedFoldersWorkspace(config.basePath, config.subPaths)
        }
    }

    private fun createAllInOneWorkspace(basePath: Path) {
        val directories = listOf("office", "education", "communication", "configuration", "job")
        directories.forEach { dir ->
            createDirectory(basePath.resolve(dir))
        }
        createConfigFiles(basePath)
    }

    private fun createSeparatedFoldersWorkspace(basePath: Path, subPaths: Map<String, Path>) {
        subPaths.forEach { (name, path) ->
            createDirectory(path)
            createConfigFiles(path)
        }
    }

    private fun createDirectory(path: Path) {
        path.toFile().mkdirs()
    }

    private fun createConfigFiles(basePath: Path) {
        // Création des fichiers de configuration nécessaires
        File(basePath.toFile(), "application.properties").writeText(
            """
            school.workspace.path=${basePath}
            # Add other configuration properties here
        """.trimIndent()
        )
    }

    enum class InstallationType {
        ALL_IN_ONE,
        SEPARATED_FOLDERS
    }

    data class WorkspaceConfig(
        val basePath: Path,
        val type: InstallationType,
        val subPaths: Map<String, Path> = emptyMap()
    )
}