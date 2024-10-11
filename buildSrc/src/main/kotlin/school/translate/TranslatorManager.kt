@file:Suppress(
    "MemberVisibilityCanBePrivate",
    "MayBeConstant"
)

package school.translate

import arrow.core.Either.Left
import arrow.core.Either.Right
import kotlinx.coroutines.runBlocking
import org.gradle.api.Project
import school.ai.AssistantManager.createOllamaChatModel
import school.ai.AssistantManager.createOllamaStreamingChatModel
import school.ai.AssistantManager.generateStreamingResponse
import school.translate.TranslatorManager.PromptManager.getTranslatePromptMessage
import school.translate.TranslatorManager.PromptManager.userLanguage
import school.workspace.WorkspaceUtils.uppercaseFirstChar
import java.util.*
import java.util.Locale.*

typealias TranslationTasks = Set<Pair<String/*taskName*/, Pair<String/*from*/, String/*to*/>>>

object TranslatorManager {
    const val MODEL = "aya:8b"

    @JvmStatic
    fun main(args: Array<String>) {
        supportedLanguages
            .translationTasks()
            .apply { forEach(::println) }
            .let {
                userLanguage.run { "userLanguage : $this" }.run(::println)
                "Gradle task name : ${it.first().first}".run(::println)
                it.first().second.getTranslatePromptMessage("SALUT LES AMIS COMMENT CA VA ?")
                    .run { "translateMessage : $this" }.run(::println)
            }
    }

    @JvmStatic
    val supportedLanguages: Set<String> = setOf(
        FRENCH, ENGLISH, GERMAN,
        forLanguageTag("ru"),
        forLanguageTag("es"),
    ).map { it.language }.toSet()

    @JvmStatic
    fun Set<String>.translationTasks()
            : TranslationTasks = mutableSetOf<Pair<String, Pair<String, String>>>().apply {
        this@translationTasks
            .map { it.uppercaseFirstChar }
            .run langNames@{
                forEach { from: String ->
                    "${from}To".run task@{
                        this@langNames
                            .filter { it != from }
                            .forEach { add("${this@task}$it" to (from to it)) }
                    }
                }
            }
    }

    object PromptManager {
        val userLanguage = System.getProperty("user.language")!!
        val fromLanguage = System.getProperty("user.language")!!
        fun Pair<String, String>.getTranslatePromptMessage(text: String): String =
            """Translate this sentence from ${forLanguageTag(first).getDisplayLanguage(ENGLISH).lowercase()} to ${
                forLanguageTag(second).getDisplayLanguage(
                    ENGLISH
                ).lowercase()
            } : 
$text""".trimMargin()
    }


    //translatorSupportedLanguages
    // Creating tasks for each model
    fun Project.createTranslationTasks() = run {
        supportedLanguages
            .translationTasks()
            .forEach {
                createTranslationChatTask(MODEL, (it.first to it.second))
                createStreamingTranslationChatTask(MODEL, (it.first to it.second))
            }
    }

    // Generic function for chat model tasks
    fun Project.createTranslationChatTask(model: String, taskComponent: Pair<String, Pair<String, String>>) {
        class MyPluginExtension {
            var input: String? = null
        }

        task("translate${taskComponent.first}") {
            group = "translator"
            description = "Translate using the Ollama $model chatgpt prompt request."
            val text = this.project.objects.property(String::class.java)
//            val extension = project.extensions.create("myPlugin", MyPluginExtension::class.java)

            doLast {
                project.runTranslationChat(
                    model,
                    taskComponent.second.getTranslatePromptMessage(text.getOrElse(""))
                )
            }
        }
    }

    // Generic function for streaming chat model tasks
    fun Project.createStreamingTranslationChatTask(
        model: String,
        taskComponent: Pair<String, Pair<String, String>>
    ) {
        task("translateStream${taskComponent.first}") {
            group = "translator"
            description = "Translate the Ollama $model chatgpt stream prompt request."
            val text = project.objects.property(String::class.java)
            doFirst {
                project.runStreamTranslationChat(
                    model,
                    taskComponent.second.getTranslatePromptMessage(text.getOrElse(""))
                )
            }
        }
    }

    fun Project.runTranslationChat(model: String, text: String) =
        createOllamaChatModel(model = model).run { generate(text).let(::println) }

    fun Project.runStreamTranslationChat(model: String, text: String) = runBlocking {
        createOllamaStreamingChatModel(model).run {
            when (val answer = generateStreamingResponse(this, text)) {
                is Right -> "Complete response received:\n${
                    answer.value.content().text()
                }".run(::println)

                is Left -> "Error during response generation:\n${answer.value}".run(::println)
            }
        }
    }
}