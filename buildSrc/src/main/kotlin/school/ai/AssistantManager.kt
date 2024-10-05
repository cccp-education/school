package school.ai

import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.core.Either.Left
import arrow.core.Either.Right
import arrow.core.getOrElse
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.model.StreamingResponseHandler
import dev.langchain4j.model.chat.StreamingChatLanguageModel
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import dev.langchain4j.model.output.Response
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import school.workspace.WorkspaceManager.privateProps
import java.time.Duration.ofSeconds
import kotlin.coroutines.resume

@Suppress("MemberVisibilityCanBePrivate")
object AssistantManager {
    @JvmStatic
    fun main(args: Array<String>) {
        userMessage.run(::println)
    }

    val logger: Logger = LoggerFactory.getLogger(AssistantPlugin::class.java)

    @JvmStatic
    val Project.localModels
        get() = setOf(
            "llama3.2:3b" to "LlamaTiny",
            "llama3.1:8b" to "LlamaSmall",
            "mistral:7b" to "Mistral",
            "phi3.5:3.8b" to "Phi",
            "smollm:135m" to "SmollM",
            "aya:8b" to "Aya"
        )

    // Creating tasks for each model
    @JvmStatic
    fun Project.createChatTasks() = localModels.forEach {
        createChatTask(it.first, "helloOllama${it.second}")
        createStreamingChatTask(it.first, "helloOllamaStream${it.second}")
    }


    @JvmStatic
    val Project.openAIapiKey: String
        get() = privateProps["OPENAI_API_KEY"] as String


    val userName = System.getProperty("user.name")
    val assistantName = "E-3PO"

    val userMessage = """config```--lang=fr;```. 
                            | Salut je suis $userName, 
                            | toi tu es $assistantName, tu es mon assistant.
                            | Le cœur de métier de ${System.getProperty("user.name")} est le développement logiciel dans l'EdTech 
                            | et la formation professionnelle pour adulte. 
                            | La spécialisation de ${System.getProperty("user.name")} est dans l'ingénierie de la pédagogie pour adulte,
                            | et le software craftmanship avec les méthodes agiles.
                            | $assistantName ta mission est d'aider ${System.getProperty("user.name")} dans l'activité d'écriture de formation et génération de code.
                            | Réponds moi à ce premier échange uniquement en maximum 120 mots""".trimMargin()


    fun Project.createOllamaChatModel(model: String = "smollm:135m"): OllamaChatModel =
        OllamaChatModel.builder().apply {
            baseUrl(findProperty("ollama.baseUrl") as? String ?: "http://localhost:11434")
            modelName(findProperty("ollama.modelName") as? String ?: model)
            temperature(findProperty("ollama.temperature") as? Double ?: 0.8)
            timeout(ofSeconds(findProperty("ollama.timeout") as? Long ?: 6_000))
            logRequests(true)
            logResponses(true)
        }.build()

    fun Project.createOllamaStreamingChatModel(model: String = "smollm:135m"): OllamaStreamingChatModel =
        OllamaStreamingChatModel.builder().apply {
            baseUrl(findProperty("ollama.baseUrl") as? String ?: "http://localhost:11434")
            modelName(findProperty("ollama.modelName") as? String ?: model)
            temperature(findProperty("ollama.temperature") as? Double ?: 0.8)
            timeout(ofSeconds(findProperty("ollama.timeout") as? Long ?: 6_000))
            logRequests(true)
            logResponses(true)
        }.build()


    suspend fun generateStreamingResponse(
        model: StreamingChatLanguageModel, promptMessage: String
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

    fun Project.runChat(model: String) {
        createOllamaChatModel(model = model)
            .run { generate(userMessage).let(::println) }
    }

    fun Project.runStreamChat(model: String) {
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

    // Generic function for chat model tasks
    fun Project.createChatTask(model: String, taskName: String) {
        task(taskName) {
            group = "school-ai"
            description = "Display the Ollama $model chatgpt prompt request."
            doFirst { project.runChat(model) }
        }
    }

    // Generic function for streaming chat model tasks
    fun Project.createStreamingChatTask(model: String, taskName: String) {
        task(taskName) {
            group = "school-ai"
            description = "Display the Ollama $model chatgpt stream prompt request."
            doFirst { runStreamChat(model) }
        }
    }

}
