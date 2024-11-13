package school.base.installer

import org.springframework.context.ApplicationContext
import school.base.installer.WorkspaceService.InstallationType.ALL_IN_ONE
import school.base.installer.WorkspaceService.InstallationType.SEPARATED_FOLDERS
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Path

class WorkspaceService(val context: ApplicationContext) {

    fun createWorkspace(config: WorkspaceConfig) = when (config.type) {
        ALL_IN_ONE -> createAllInOneWorkspace(config.basePath)
        SEPARATED_FOLDERS -> createSeparatedFoldersWorkspace(config.basePath, config.subPaths)
    }


    private fun createAllInOneWorkspace(basePath: Path) = listOf(
        "office",
        "education",
        "communication",
        "configuration",
        "job"
    ).forEach { dir -> createDirectory(basePath.resolve(dir)) }
        .also { createDirectory(basePath) }

    private fun createSeparatedFoldersWorkspace(
        basePath: Path,
        subPaths: Map<String, Path>
    ) = subPaths.forEach { (name, path) ->
        createDirectory(path)
        createConfigFiles(path)
    }


    private fun createDirectory(path: Path) = path.toFile().mkdirs()


    private fun createConfigFiles(basePath: Path) =
        // Création des fichiers de configuration nécessaires
        File(basePath.toFile(), "application.properties").writeText(
            """
            school.workspace.path=${basePath}
            # Add other configuration properties here
        """.trimIndent()
        )


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
