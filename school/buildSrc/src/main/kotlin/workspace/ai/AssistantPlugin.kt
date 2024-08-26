package workspace.ai

import org.gradle.api.Plugin
import org.gradle.api.Project

/*
val Project.apiKey: String
    get() = Properties().apply {
        "$projectDir/private.properties"
            .let(::File)
            .inputStream()
            .use(::load)
    }["OPENAI_API_KEY"] as String

tasks.register("displayOpenAIKey") {
    group = "school-ai"
    description = "Display the open ai api keys stored in private.properties"
    doFirst { println("apiKey : $apiKey") }
}

tasks.register("helloChatGPT") {
    group = "school-ai"
    description = "Display the open ai chatgpt hello prompt request."
    doFirst {
        OpenAiChatModel
            .withApiKey(apiKey)
            .generate("Say 'Hello World'")
            .apply(::println)
    }
}*/

@Suppress("unused")
class AssistantPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.task("helloAssistant") {
            doFirst { println("Hello from assistant") }
        }
    }
}
