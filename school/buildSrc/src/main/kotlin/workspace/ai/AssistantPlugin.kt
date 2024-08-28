package workspace.ai

import dev.langchain4j.model.openai.OpenAiChatModel
import org.gradle.api.Plugin
import org.gradle.api.Project
import workspace.ai.AssistantManager.apiKey


class AssistantPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.run {
            task("helloAssistant") {
                group = "school-ai"
                description = "Greetings from AI assistant!"
                doFirst { println("Hello from assistant") }
            }

            task("displayOpenAIKey") {
                group = "school-ai"
                description = "Display the open ai api keys stored in private.properties"
                doFirst { println("apiKey : ${project.apiKey}") }
            }

            task("helloOpenAiChatGPT") {
                group = "school-ai"
                description = "Display the open ai chatgpt hello prompt request."
                doFirst {
                    OpenAiChatModel
                        .withApiKey(apiKey)
                        .generate("Say 'Hello World'")
                        .apply(::println)
                }
            }
        }
    }
}
