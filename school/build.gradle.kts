import dev.langchain4j.model.openai.OpenAiChatModel
import workspace.WorkspaceUtils.purchaseArtifact
import workspace.jamstack.JBakeGhPagesPlugin
import java.util.*


plugins {
    idea
    id("com.github.node-gradle.node")
//    id("org.asciidoctor.jvm.convert")
}

apply<workspace.school.SchoolPlugin>()
apply<workspace.forms.FormPlugin>()
apply<JBakeGhPagesPlugin>()

purchaseArtifact()

tasks.wrapper {
    gradleVersion = "8.6"
    distributionType = Wrapper.DistributionType.ALL
}

//kotlin { jvmToolchain(22) }

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
}
