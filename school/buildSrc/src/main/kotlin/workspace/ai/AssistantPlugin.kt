package workspace.ai

import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.core.Either.Left
import arrow.core.Either.Right
import arrow.core.getOrElse
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.model.StreamingResponseHandler
import dev.langchain4j.model.chat.StreamingChatLanguageModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.output.Response
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import org.gradle.api.Plugin
import org.gradle.api.Project
import workspace.ai.AssistantManager.apiKey
import workspace.ai.OllamaChat.createOllamaChatModel
import workspace.ai.OllamaChat.createOllamaStreamingChatModel
import kotlin.coroutines.resume

class AssistantPlugin : Plugin<Project> {

    companion object {
        val prompt = """config```--lang=fr;```. 
                            | Salut je suis ${System.getProperty("user.name")}, 
                            | toi tu es E3P0, tu es mon assistant.
                            | Le coeur de métier de ${System.getProperty("user.name")} est le développement logiciel dans l'EdTech 
                            | et la formation professionnelle pour adulte. 
                            | La spécialisation de ${System.getProperty("user.name")} est dans l'ingenieurie de pédagogie pour adulte,
                            | et le software craftmanship avec les méthodes agiles.
                            | E3P0 ta mission est d'aider ${System.getProperty("user.name")} dans l'activité d'écriture de formation et génération de code.
                            | Réponds moi à ce premier échange uniquement en maximum 200 mots"""
            .trimMargin()
    }

    override fun apply(project: Project) {
        project.run {
            task("helloOllama") {
                group = "school-ai"
                description = "Display the ollama chatgpt prompt request."
                doFirst { createOllamaChatModel().run { generate(prompt).let(::println) } }
            }

            task("helloOllamaStream") {
                group = "school-ai"
                description = "Display the ollama chatgpt stream prompt request."
                doFirst {
                    runBlocking {
                        createOllamaStreamingChatModel().run {
                            when (val answer = generateStreamingResponse(this, prompt)) {
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
                doFirst { prompt.let(::println) }
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


    private suspend fun generateStreamingResponse(
        model: StreamingChatLanguageModel,
        promptMessage: String
    ): Either<Throwable, Response<AiMessage>> = catch {
        suspendCancellableCoroutine { continuation ->
            model.generate(promptMessage, object : StreamingResponseHandler<AiMessage> {
                override fun onNext(token: String) {
                    print(token)
                }

                override fun onComplete(response: Response<AiMessage>) {
                    continuation.resume(response)
                }

                override fun onError(error: Throwable) {
                    continuation.resume(Left(error).getOrElse { throw it })
                }
            })
        }
    }
}
