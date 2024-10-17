package school

// Classe principale pour la génération des schémas
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import school.PersonSchemaGenerator.Address
import school.PersonSchemaGenerator.Companion.generateJsonSchema
import school.PersonSchemaGenerator.Companion.generateYamlSchema
import school.PersonSchemaGenerator.Person
import school.PluginTests.Companion.workspace
import school.PluginTests.Workspace
import school.workspace.WorkspaceUtils.yamlMapper
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.PrintStream
import javax.xml.bind.JAXBContext
import javax.xml.bind.SchemaOutputResolver
import javax.xml.transform.Result
import javax.xml.transform.stream.StreamResult
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties


class PersonSchemaGenerator {

    // Data classes Kotlin (équivalent des Records Java)
    data class Person(
        val firstName: String,
        val lastName: String,
        val age: Int,
        val address: Address,
        val phoneNumbers: List<String>
    )

    data class Address(
        val street: String,
        val city: String,
        val zipCode: String,
        val country: String
    )

    companion object {
        @Throws(Exception::class)
        fun generateJsonSchema(): String {
            val mapper = ObjectMapper()
            val schemaGen = JsonSchemaGenerator(mapper)
            val schema = schemaGen.generateSchema(Person::class.java)
            return mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(schema)

        }

        @Throws(Exception::class)
        fun generateXmlSchema() {
            val context = JAXBContext.newInstance(Person::class.java)
            val outputResolver = object : SchemaOutputResolver() {
                @Throws(IOException::class)
                override fun createOutput(namespaceUri: String, suggestedFileName: String): Result {
                    val file = File("person.xsd")
                    val result = StreamResult(file)
                    result.systemId = file.toURI().toURL().toString()
                    return result
                }
            }
            context.generateSchema(outputResolver)
        }

        @Throws(Exception::class)
        fun generateYamlSchema(): String {
            val yaml = Yaml(DumperOptions().apply {
                defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
                isPrettyFlow = true
            })

            val schema = mutableMapOf<String, Any>()
            schema["type"] = "object"
            schema["properties"] = generateProperties(Person::class)

            return yaml.dump(schema)
        }

        private fun generateProperties(kClass: KClass<*>): Map<String, Any> {
            return kClass.memberProperties.associate { property ->
                property.name to mutableMapOf<String, Any>().apply {
                    put("type", getYamlType(property.returnType.classifier as KClass<*>))
                }
            }
        }

        private fun getYamlType(kClass: KClass<*>): String = when {
            kClass == String::class -> "string"
            kClass == Int::class -> "integer"
            kClass == List::class -> "array"
            kClass.isData -> "object"
            else -> "string"
        }
    }
}

object GradleTestUtils {

    fun Project.displayWorkspaceDataSchemaStructure(): Unit {
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
//                .apply { "$this xml DTD : ${generateXmlSchema()}"/*.run(::println)*/ }
        }

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
