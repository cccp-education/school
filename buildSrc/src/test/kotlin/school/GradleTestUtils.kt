package school

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import school.PluginTests.Companion.workspace
import school.PluginTests.Workspace
import school.workspace.WorkspaceUtils.yamlMapper
import java.io.ByteArrayOutputStream
import java.io.PrintStream

object GradleTestUtils {

    fun Project.displayWorkspaceDataSchemaStructure(): Unit {

    }

    fun Project.displayWorkspaceStructure(): Workspace = workspace.apply {
        run(yamlMapper::writeValueAsString)
            .run(::println)
    }

    @JvmStatic
    val projectInstance: Project by lazy(ProjectBuilder.builder()::build)


    val initWorkspace get() = mutableMapOf<String, MutableMap<String, MutableMap<String, MutableMap<String, MutableMap<String, MutableMap<String, MutableMap<String, Any>>>>?>?>>()

    val captureOutput: ByteArrayOutputStream
        get() = "captureOutput"
            .let(::println).run {
                ByteArrayOutputStream().apply {
                    let(::PrintStream).let(System::setOut)
                }
            }


    val PrintStream.releaseOutput
        get() = let(System::setOut)
            .run { "releaseOutput" }
            .let(::println)
}
