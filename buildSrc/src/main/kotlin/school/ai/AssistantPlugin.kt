package school.ai

import dev.langchain4j.model.openai.OpenAiChatModel
import org.gradle.api.Plugin
import org.gradle.api.Project
import school.ai.AssistantManager.apiKey
import school.ai.AssistantManager.localModels
import school.ai.AssistantManager.runChat
import school.ai.AssistantManager.runStreamChat
import school.ai.AssistantManager.userMessage

class AssistantPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        project.run {
            task("displayAIPrompt") {
                group = "school-ai"
                description = "Dislpay on console AI prompt assistant"
                doFirst { userMessage.let(::println) }
            }

            task("displayOpenAIKey") {
                group = "school-ai"
                description = "Display the open ai api keys stored in private.properties"
                doFirst { "apiKey : ${project.apiKey}".let(::println) }
            }

            task("helloOpenAi") {
                group = "school-ai"
                description = "Display the open ai chatgpt hello prompt request."
                doFirst {
                    OpenAiChatModel
                        .withApiKey(apiKey)
                        .generate("Say 'Hello World'")
                        .run(::println)
                }
            }


            // Generic function for chat model tasks
            fun createChatTask(taskName: String, model: String) {
                task(taskName) {
                    group = "school-ai"
                    description = "Display the Ollama $model chatgpt prompt request."
                    doFirst { runChat(model) }
                }
            }

            // Generic function for streaming chat model tasks
            fun createStreamingChatTask(taskName: String, model: String) {
                task(taskName) {
                    group = "school-ai"
                    description = "Display the Ollama $model chatgpt stream prompt request."
                    doFirst { runStreamChat(model) }
                }
            }

            // Creating tasks for each model
            localModels.forEach { model ->
                createChatTask("helloOllama${model.value}", model.key)
                createStreamingChatTask("helloOllamaStream${model.value}", model.key)
            }
        }
    }
}