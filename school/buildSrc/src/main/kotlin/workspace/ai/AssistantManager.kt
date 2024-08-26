package workspace.ai

import org.gradle.api.Project
import java.io.File
import java.util.*

object AssistantManager {
    val Project.apiKey: String
        get() = Properties().apply {
            "$projectDir/private.properties"
                .let(::File)
                .inputStream()
                .use(::load)
        }["OPENAI_API_KEY"] as String
}
