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
//            .apply { forEach(::println) }
            .let {
//                userLanguage.run { "userLanguage : $this" }.run(::println)
                "Gradle task name : ${it.first().first}".run(::println)
                it.first().second.getTranslatePromptMessage("SALUT LES AMIS COMMENT CA VA ?")
                    .run { "translateMessage : $this" }.run(::println)
            }
//            .run {
//                val currentLocale = Locale.getDefault()
//                currentLocale.run { "currentLocale : $this" }.run(::println)
                // Force English language for display
//                Locale.setDefault(ENGLISH)
                // Get the display name of the English locale
//                val englishDisplayName = ENGLISH.getDisplayLanguage(ENGLISH)
//                println("English display name: $englishDisplayName")
//            }
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
    fun Project.createTranslationTasks() = supportedLanguages
        .translationTasks()
        .forEach {
            val text = """ICI LE TEXT A TRADUIRE!"""
            createTranslationChatTask(MODEL, Triple(it.first, it.second, text))
            createStreamingTranslationChatTask(MODEL, Triple(it.first, it.second, text))
        }

    // Generic function for chat model tasks
    fun Project.createTranslationChatTask(model: String, taskComponent: Triple<String, Pair<String, String>, String>) {
        task(taskComponent.first) {
            group = "translator"
            description = "Translate using the Ollama $model chatgpt prompt request."
            doFirst {
                project.runTranslationChat(
                    model,
                    taskComponent.second.getTranslatePromptMessage(taskComponent.third)
                )
            }
        }
    }

    // Generic function for streaming chat model tasks
    fun Project.createStreamingTranslationChatTask(
        model: String,
        taskComponent: Triple<String, Pair<String, String>, String>
    ) {
        task(taskComponent.first) {
            group = "translator"
            description = "Translate the Ollama $model chatgpt stream prompt request."
            doFirst {
                project.runStreamTranslationChat(
                    model,
                    taskComponent.second.getTranslatePromptMessage(taskComponent.third)
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