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
import school.ai.AssistantManager.userMessage

class AssistantPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.run {
            task("helloOllamaMistral") {
                group = "school-ai"
                description = "Display the ollama mistral chatgpt prompt request."
                doFirst {
                    createOllamaChatModel(model = "mistral")
                        .run { generate(userMessage).let(::println) }
                }
            }

            task("helloOllamaStreamMistral") {
                group = "school-ai"
                description = "Display the ollama mistral chatgpt stream prompt request."
                doFirst {
                    runBlocking {
                        createOllamaStreamingChatModel("mistral").run {
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




            task("helloOllamaPhi") {
                group = "school-ai"
                description = "Display the ollama phi3.5 chatgpt prompt request."
                doFirst {
                    createOllamaChatModel(model = "phi3.5:latest")
                        .run { generate(userMessage).let(::println) }
                }
            }

            task("helloOllamaStreamPhi") {
                group = "school-ai"
                description = "Display the ollama phi3.5 chatgpt stream prompt request."
                doFirst {
                    runBlocking {
                        createOllamaStreamingChatModel("phi3.5:latest").run {
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

            task("helloOllamaSmollM") {
                group = "school-ai"
                description = "Display the ollama mistral chatgpt prompt request."
                doFirst {
                    createOllamaChatModel()
                        .run { generate(userMessage).let(::println) }
                }
            }

            task("helloOllamaStreamSmollM") {
                group = "school-ai"
                description = "Display the ollama mistral chatgpt stream prompt request."
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
        }
    }
}
