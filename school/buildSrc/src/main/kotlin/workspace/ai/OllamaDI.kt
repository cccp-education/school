package workspace.ai

import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import org.gradle.api.Project
import org.koin.dsl.module
import java.time.Duration

object OllamaDI {
    fun ollamaModule(project: Project) = module {
        single {
            OllamaChatModel.builder().apply {
                baseUrl(project.findProperty("ollama.baseUrl") as? String ?: "http://localhost:11434")
                modelName(project.findProperty("ollama.modelName") as? String ?: "phi3.5")
                temperature(project.findProperty("ollama.temperature") as? Double ?: 0.8)
                timeout(Duration.ofSeconds(project.findProperty("ollama.timeout") as? Long ?: 6_000))
                logRequests(true)
                logResponses(true)
            }.build()
        }

        single {
            OllamaStreamingChatModel.builder().apply {
                baseUrl(project.findProperty("ollama.baseUrl") as? String ?: "http://localhost:11434")
                modelName(project.findProperty("ollama.modelName") as? String ?: "phi3.5")
                temperature(project.findProperty("ollama.temperature") as? Double ?: 0.8)
                timeout(Duration.ofSeconds(project.findProperty("ollama.timeout") as? Long ?: 6_000))
                logRequests(true)
                logResponses(true)
            }.build()
        }
    }
}
