package school.ai

import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.core.Either.Left
import arrow.core.getOrElse
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.model.StreamingResponseHandler
import dev.langchain4j.model.chat.StreamingChatLanguageModel
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import dev.langchain4j.model.output.Response
import kotlinx.coroutines.suspendCancellableCoroutine
import org.gradle.api.Project
import java.io.File
import java.time.Duration
import java.util.*
import kotlin.coroutines.resume
import org.slf4j.LoggerFactory


object AssistantManager {
    val logger = LoggerFactory.getLogger(AssistantPlugin::class.java)

    val Project.apiKey: String
        get() = Properties().apply {
            "$projectDir/private.properties"
                .let(::File)
                .inputStream()
                .use(::load)
        }["OPENAI_API_KEY"] as String

    val userName = System.getProperty("user.name")
    val assistantName = "E-3PO"

    val userMessage = """config```--lang=fr;```. 
                            | Salut je suis $userName, 
                            | toi tu es $assistantName, tu es mon assistant.
                            | Le coeur de métier de ${System.getProperty("user.name")} est le développement logiciel dans l'EdTech 
                            | et la formation professionnelle pour adulte. 
                            | La spécialisation de ${System.getProperty("user.name")} est dans l'ingenieurie de pédagogie pour adulte,
                            | et le software craftmanship avec les méthodes agiles.
                            | $assistantName ta mission est d'aider ${System.getProperty("user.name")} dans l'activité d'écriture de formation et génération de code.
                            | Réponds moi à ce premier échange uniquement en maximum 120 mots""".trimMargin()

    fun Project.createOllamaChatModel(model: String = "smollm:135m"): OllamaChatModel =
        OllamaChatModel.builder().apply {
            baseUrl(project.findProperty("ollama.baseUrl") as? String ?: "http://localhost:11434")
            modelName(project.findProperty("ollama.modelName") as? String ?: model)
            temperature(project.findProperty("ollama.temperature") as? Double ?: 0.8)
            timeout(Duration.ofSeconds(project.findProperty("ollama.timeout") as? Long ?: 6_000))
            logRequests(true)
            logResponses(true)
        }.build()

    fun Project.createOllamaStreamingChatModel(model: String = "smollm:135m"): OllamaStreamingChatModel =
        OllamaStreamingChatModel.builder().apply {
            baseUrl(project.findProperty("ollama.baseUrl") as? String ?: "http://localhost:11434")
            modelName(project.findProperty("ollama.modelName") as? String ?: model)
            temperature(project.findProperty("ollama.temperature") as? Double ?: 0.8)
            timeout(Duration.ofSeconds(project.findProperty("ollama.timeout") as? Long ?: 6_000))
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
}
