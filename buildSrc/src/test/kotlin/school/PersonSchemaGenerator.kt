package school

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator
import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.SchemaOutputResolver
import jakarta.xml.bind.annotation.*
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.IOException
import javax.xml.transform.Result
import javax.xml.transform.stream.StreamResult
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

class PersonSchemaGenerator {
    @XmlRootElement(name = "person")
    @XmlAccessorType(XmlAccessType.FIELD)
    data class Person(
        @field:XmlElement(required = true)
        val firstName: String,
        @field:XmlElement(required = true)
        val lastName: String,
        @field:XmlElement(required = true)
        val age: Int,
        @field:XmlElement(required = true)
        val address: Address,
        @field:XmlElementWrapper(name = "phoneNumbers")
        @field:XmlElement(name = "phoneNumber")
        val phoneNumbers: List<String>
    ) {
        constructor() : this("", "", -1, Address(), listOf(""))
    }

    @XmlType(name = "address")
    @XmlAccessorType(XmlAccessType.FIELD)
    data class Address(
        @field:XmlElement(required = true)
        val street: String,
        @field:XmlElement(required = true)
        val city: String,
        @field:XmlElement(required = true)
        val zipCode: String,
        @field:XmlElement(required = true)
        val country: String
    ) {
        constructor() : this("", "", "", "")
    }

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

            // Afficher le contenu du fichier généré
            println(File("person.xsd").readText())
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