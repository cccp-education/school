package school.base.utils

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.reflect.KClass
import kotlin.streams.asSequence

object AppUtils {
    val Pair<Any, ApplicationContext>.toJson: String
        get() = second.getBean<ObjectMapper>().writeValueAsString(first)

    val KClass<Any>.objectName
        get() = java.simpleName.run {
            replaceFirst(
                first(),
                first().lowercaseChar()
            )
        }

    fun List<String>.nameToLogin(): List<String> = map { StringUtils.stripAccents(it.lowercase().replace(' ', '.')) }

    fun String.cleanField(): String = StringBuilder(this)
        .deleteCharAt(0)
        .deleteCharAt(length - 2)
        .toString()

    @JvmStatic
    val String.upperFirstLetter
        get() = run {
            replaceFirst(
                first(),
                first().uppercaseChar()
            )
        }

    @JvmStatic
    val String.lowerFirstLetter
        get() = run {
            replaceFirst(
                first(),
                first().lowercaseChar()
            )
        }

    fun ApplicationContext.lsWorkingDirProcess(workingDir: File = File(".")): String {
        return ByteArrayOutputStream().use { outputStream ->
            val isWindows = System.getProperty("os.name")
                .lowercase()
                .contains("windows")

            val command = if (isWindows) {
                listOf("cmd.exe", "/c", "dir", workingDir.absolutePath, "/b")
            } else {
                listOf("ls", workingDir.absolutePath)
            }

            val process = ProcessBuilder(command)
                .directory(workingDir)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            val result = process.waitFor()

            val output = process.inputStream.use {
                it.readAllBytes()
            }

            when {
                result != 0 -> {
                    val errorOutput = process.errorStream.use {
                        String(it.readAllBytes(), UTF_8)
                    }
                    throw RuntimeException("Command ls failed: $errorOutput")
                }

                else -> {
                    val outputString = String(output, UTF_8).trim()
                    println("Command ls output:\n$outputString")
                    outputString
                }
            }
        }
    }


    /**
     * Main extension function to list the contents of a directory using NIO.
     * @param directory The path of the directory to list
     * @param maxDepth Maximum recursion depth (default: 1 = no recursion)
     * @param pattern Glob pattern to filter files (optional)
     * @return A string containing the directory listing
     */
    fun ApplicationContext.lsWorkingDir(
        directory: Path,
        maxDepth: Int = 1,
        pattern: String? = null
    ): String {
        require(Files.exists(directory)) { "Le répertoire $directory n'existe pas" }
        require(Files.isDirectory(directory)) { "$directory n'est pas un répertoire" }

        return try {
            val matcher = pattern?.let {
                directory.fileSystem.getPathMatcher("glob:$it")
            }

            Files.walk(directory, maxDepth)
                .use { stream ->
                    stream.asSequence()
                        .drop(1) // On ignore le répertoire racine
                        .filter { path ->
                            matcher?.matches(path.fileName) ?: true
                        }
                        .map { path ->
                            buildString {
                                append(path.fileName)
                                when {
                                    Files.isDirectory(path) -> append("/")
                                    Files.isSymbolicLink(path) -> append("@")
                                    Files.isExecutable(path) -> append("*")
                                }
                            }
                        }
                        .sorted()
                        .joinToString("\n")
                }
                .also { output ->
                    println("Contenu du répertoire $directory:")
                    println(output)
                }
        } catch (e: Exception) {
            throw RuntimeException("Erreur lors du listage du répertoire $directory", e)
        }
    }

    /**
     * Overload to directly use the current directory
     */
    fun ApplicationContext.lsWorkingDir(): String =
        lsWorkingDir(Paths.get("").toAbsolutePath())

    /**
     * Overload to use a String as the path
     */
    fun ApplicationContext.lsWorkingDir(
        directory: String,
        maxDepth: Int = 1,
        pattern: String? = null
    ): String = lsWorkingDir(
        directory = Paths.get(directory).toAbsolutePath(),
        maxDepth = maxDepth,
        pattern = pattern
    )
}
