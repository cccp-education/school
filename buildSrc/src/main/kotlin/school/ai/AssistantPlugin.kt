package school.ai

import org.gradle.api.Plugin
import org.gradle.api.Project
import school.ai.AssistantManager.openAIapiKey
import school.ai.AssistantManager.createChatTasks
import school.ai.AssistantManager.createStreamingChatTask
import school.ai.AssistantManager.localModels
import school.workspace.WorkspaceManager.privateProps
import school.ai.AssistantManager.userMessage

class AssistantPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        project.task("displayAIPrompt") {
            group = "school-ai"
            description = "Dislpay on console AI prompt assistant"
            doFirst { userMessage.let(::println) }
        }

        project.task("displayOpenAIKey") {
            group = "school-ai"
            description = "Display the open ai api keys stored in private.properties"
            doFirst { "apiKey : ${project.openAIapiKey}".let(::println) }
        }

        project.task("displayPrivateProperties") {
            group = "school-ai"
            description = "Display the open ai api keys stored in private.properties"
            doFirst { println("PrivateProperties : ${project.privateProps}") }
        }

        // Creating tasks for each model
        project.createChatTasks()
    }
}
