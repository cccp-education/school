package workspace.ai

import arrow.core.Either.Left
import arrow.core.Either.Right
import dev.langchain4j.model.openai.OpenAiChatModel
import kotlinx.coroutines.runBlocking
import org.gradle.api.Plugin
import org.gradle.api.Project
import workspace.ai.AssistantManager.apiKey
import workspace.ai.AssistantManager.createOllamaChatModel
import workspace.ai.AssistantManager.createOllamaStreamingChatModel
import workspace.ai.AssistantManager.generateStreamingResponse
import workspace.ai.AssistantManager.userMessage

class AssistantPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.run {
            task("helloOllama") {
                group = "school-ai"
                description = "Display the ollama chatgpt prompt request."
                doFirst { createOllamaChatModel().run { generate(userMessage).let(::println) } }
            }

            task("helloOllamaStream") {
                group = "school-ai"
                description = "Display the ollama chatgpt stream prompt request."
                doFirst {
                    runBlocking {
                        createOllamaStreamingChatModel().run {
                            when (val answer = generateStreamingResponse(this, userMessage)) {
                                is Right ->
                                    "Complete response received: \n${answer.value.content().text()}".run(::println)

                                is Left ->
                                    "Error during response generation: \n${answer.value}".run(::println)
                            }
                        }
                    }
                }
            }

            task("displayAIPrompt") {
                group = "school-ai"
                description = "Dislpay on console AI prompt assistant"
                doFirst { userMessage.let(::println) }
            }

            task("displayOpenAIKey") {
                group = "school-ai"
                description = "Display the open ai api keys stored in private.properties"
                doFirst { println("apiKey : ${project.apiKey}") }
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
        }
    }
}
