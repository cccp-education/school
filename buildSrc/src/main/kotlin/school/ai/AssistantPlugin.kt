package school.ai

import dev.langchain4j.model.openai.OpenAiChatModel
import org.gradle.api.Plugin
import org.gradle.api.Project
import school.ai.AssistantManager.apiKey
import school.ai.AssistantManager.createChatTask
import school.ai.AssistantManager.createStreamingChatTask
import school.ai.AssistantManager.localModels
import school.ai.AssistantManager.privateProps
import school.ai.AssistantManager.userMessage

class AssistantPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        project.task("displayAIPrompt") {
            group = "school-ai"
            description = "Dislpay on console AI prompt assistant"
            doFirst { userMessage.let(::println) }
        }

        project.task("displayOpenAIKey") {
            group = "school-ai"
            description = "Display the open ai api keys stored in private.properties"
            doFirst { "apiKey : ${project.apiKey}".let(::println) }
        }

        project.task("displayPrivateProperties") {
            group = "school-ai"
            description = "Display the open ai api keys stored in private.properties"
            doFirst { println("PrivateProperties : ${project.privateProps}") }
        }

        project.task("helloOpenAi") {
            group = "school-ai"
            description = "Display the open ai chatgpt hello prompt request."
            doFirst {
                OpenAiChatModel
                    .withApiKey(project.apiKey)
                    .generate("Say 'Hello World'")
                    .run(::println)
            }
        }

        // Creating tasks for each model
        project.run {
            localModels.forEach {
                createChatTask("helloOllama${it.value}", it.key)
                createStreamingChatTask("helloOllamaStream${it.value}", it.key)
            }
        }
    }
}
