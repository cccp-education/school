package workspace

import arrow.integrations.jackson.module.registerArrowModule
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.gradle.api.Project
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.FileSystems

object WorkspaceUtils {
    fun Project.purchaseArtifact() = ("artifact.group" to "artifact.version").run {
        group = properties[first].toString()
        version = properties[second].toString()
    }

    fun constructTaskName(
        it: Map<Pair<String, String>, String>
    ) = "${it.values.first()}${it.keys.first().first}"

    val sep: String get() = FileSystems.getDefault().separator

    val Project.yamlMapper: ObjectMapper
        get() = YAMLFactory()
            .let(::ObjectMapper)
            .disable(WRITE_DATES_AS_TIMESTAMPS)
            .registerKotlinModule()
            .registerArrowModule()

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

    fun Project.createDirectory(
        path: String
    ): File = path.let(::File).apply {
        if (exists() && !isDirectory) assert(delete())
        if (exists()) assert(deleteRecursively())
        assert(!exists())
        if (!exists()) assert(mkdir())
    }

    @Throws(RuntimeException::class)
    fun Project.lsWorkingDir() = ByteArrayOutputStream().use { outputStream ->
        exec {
            standardOutput = outputStream
            workingDir = projectDir
            when {
                "os.name"
                    .run(System::getProperty)
                    .lowercase()
                    .contains("windows") -> commandLine(
                    "cmd.exe",
                    "/c",
                    "dir",
                    workingDir,
                    "/b"
                )

                else -> commandLine("ls", workingDir)
            }
        }.let { result ->
            when {
                result.exitValue != 0 -> throw RuntimeException("Command ls failed.")
                else -> outputStream.toString()
                    .trim()
                    .run { "Command ls output:\n$this" }
                    .let(::println)
            }
        }
    }
}