package school

// Classe principale pour la génération des schémas
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import school.PersonSchemaGenerator.Address
import school.PersonSchemaGenerator.Companion.generateJsonSchema
import school.PersonSchemaGenerator.Companion.generateXmlSchema
import school.PersonSchemaGenerator.Companion.generateYamlSchema
import school.PersonSchemaGenerator.Person
import school.PluginTests.Companion.workspace
import school.PluginTests.Workspace
import school.workspace.WorkspaceUtils.yamlMapper
import java.io.ByteArrayOutputStream
import java.io.PrintStream


object GradleTestUtils {

    fun Project.displayPersonDataSchemaStructure(): Unit {
//        println(workspace)
        val person = Person(
            "john",
            "doe",
            33,
            Address(
                "sesame street",
                "Utopia",
                "42",
                "us"
            ),
            listOf("0033606060606"),
        ).run {
            println(this)
            this::class.java.simpleName
                .apply { "$this json data schema : \n${generateJsonSchema()}".run(::println) }
                .apply { "$this yaml data schema : \n${generateYamlSchema()}".run(::println) }
                .apply { "$this xml DTD : ".run(::println).run { generateXmlSchema() } }
        }

    }

    fun Project.displayWorkspaceDataSchemaStructure(): Unit {
        println(workspace)
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
