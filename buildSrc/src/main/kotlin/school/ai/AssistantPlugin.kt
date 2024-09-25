package school.ai

import arrow.core.Either.Left
import arrow.core.Either.Right
import dev.langchain4j.model.openai.OpenAiChatModel
import kotlinx.coroutines.runBlocking
import org.gradle.api.Plugin
import org.gradle.api.Project
import school.ai.AssistantManager.apiKey
import school.ai.AssistantManager.createOllamaChatModel
import school.ai.AssistantManager.createOllamaStreamingChatModel
import school.ai.AssistantManager.generateStreamingResponse
import school.ai.AssistantManager.localModels
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
                    doFirst {
                        createOllamaChatModel(model = model)
                            .run { generate(userMessage).let(::println) }
                    }
                }
            }

            // Generic function for streaming chat model tasks
            fun createStreamingChatTask(taskName: String, model: String) {
                task(taskName) {
                    group = "school-ai"
                    description = "Display the Ollama $model chatgpt stream prompt request."
                    doFirst {
                        runBlocking {
                            createOllamaStreamingChatModel(model).run {
                                when (val answer = generateStreamingResponse(this, userMessage)) {
                                    is Right -> "Complete response received:\n${
                                        answer.value.content().text()
                                    }".run(::println)

                                    is Left -> "Error during response generation:\n${answer.value}".run(::println)
                                }
                            }
                        }
                    }
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