@file:Suppress("MemberVisibilityCanBePrivate")

package school.translate

import arrow.core.Either.Left
import arrow.core.Either.Right
import kotlinx.coroutines.runBlocking
import org.gradle.api.Project
import school.ai.AssistantManager.createOllamaChatModel
import school.ai.AssistantManager.createOllamaStreamingChatModel
import school.ai.AssistantManager.generateStreamingResponse
import school.translate.TranslatorManager.PromptManager.translateMessage
import school.translate.TranslatorManager.PromptManager.userLanguage
import school.workspace.WorkspaceUtils.uppercaseFirstChar
import java.util.*

object TranslatorManager {
    const val MODEL = "aya:8b"

    @JvmStatic
    fun main(args: Array<String>) {
        translatorSupportedLanguages
            .translationTasks()
            .forEach(::println)
            .run {
                userLanguage.run { "userLanguage : $this" }.run(::println)
                translateMessage.run { "translateMessage : $this" }.run(::println)
            }.run {
                val currentLocale = Locale.getDefault()
                currentLocale.run { "currentLocale : $this" }.run(::println)
                // Force English language for display
                Locale.setDefault(Locale.ENGLISH)
                // Get the display name of the English locale
                val englishDisplayName = Locale.ENGLISH.getDisplayLanguage(Locale.ENGLISH)
                println("English display name: $englishDisplayName")
            }
    }


    @JvmStatic
    val translatorSupportedLanguages: Set<String> = setOf(
        Locale.FRENCH,
        Locale.ENGLISH,
        Locale.GERMAN,
        Locale.forLanguageTag("es")
    ).map { it.language }.toSet()

    @JvmStatic
    fun Set<String>.translationTasks(): Set<String> = mutableSetOf<String>().apply {
        val uppercaseFirstCharLangs = this@translationTasks.map { it.uppercaseFirstChar }
        uppercaseFirstCharLangs.run {
            forEach { from ->
                "translate${from}To".run {
                    uppercaseFirstCharLangs.filter { to -> to != from }
                        .forEach { add("$this${it}") }
                }
            }
        }
    }


    object PromptManager {
        val userLanguage = System.getProperty("user.language")!!
        val translateMessage = """Translate this sentence from $userLanguage to english : 
            | """.trimMargin()
    }


    //translatorSupportedLanguages
    // Creating tasks for each model
    fun Project.createTranslationTasks() = translatorSupportedLanguages
        .translationTasks()
        .forEach {
            createTranslationChatTask(MODEL, "translate${it}")
            createStreamingTranslationChatTask(MODEL, "translateStream${it}")
        }

    // Generic function for chat model tasks
    fun Project.createTranslationChatTask(model: String, taskName: String) {
        task(taskName) {
            group = "translator"
            description = "Translate using the Ollama $model chatgpt prompt request."
            doFirst {
                //TODO : add content mechanism to capture text, from gradle tasks input
                val text = ""
                project.runTranslationChat(model, translateMessage.run { "$this : \n$text" })
            }
        }
    }

    // Generic function for streaming chat model tasks
    fun Project.createStreamingTranslationChatTask(model: String, taskName: String) {
        task(taskName) {
            group = "translator"
            description = "Translate the Ollama $model chatgpt stream prompt request."
            doFirst {
                val text = ""
                runStreamTranslationChat(model, translateMessage.run {
                    //TODO : add content to the prompt, from gradle tasks input
                    "$this : \n$text"
                })
            }
        }
    }

    fun Project.runTranslationChat(model: String, text: String) = createOllamaChatModel(model = model)
        .run { generate(text).let(::println) }

    fun Project.runStreamTranslationChat(model: String, text: String) = runBlocking {
        createOllamaStreamingChatModel(model).run {
            when (val answer = generateStreamingResponse(this, translateMessage.run {
                //TODO : add content to the prompt, from gradle tasks input
                "$this : \n$text"
            })) {
                is Right -> "Complete response received:\n${
                    answer.value.content().text()
                }".run(::println)

                is Left -> "Error during response generation:\n${answer.value}".run(::println)
            }
        }
    }
}