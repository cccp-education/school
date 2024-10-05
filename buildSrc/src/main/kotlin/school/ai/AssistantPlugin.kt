package school.ai

import org.gradle.api.Plugin
import org.gradle.api.Project
import school.ai.AssistantManager.createChatTasks
import school.ai.AssistantManager.openAIapiKey
import school.ai.AssistantManager.userMessage
import school.workspace.WorkspaceManager.privateProps

class AssistantPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        project.task("displayE3POPrompt") {
            group = "school-ai"
            description = "Dislpay on console AI prompt assistant"
            doFirst { userMessage.let(::println) }
        }

        // Creating tasks for each model
        project.createChatTasks()
    }
}
