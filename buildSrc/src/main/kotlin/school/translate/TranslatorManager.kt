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
import school.translate.TranslatorManager.PromptManager.translateMessage
import school.translate.TranslatorManager.PromptManager.userLanguage
import school.workspace.WorkspaceUtils.uppercaseFirstChar
import java.util.*
import java.util.Locale.*

object TranslatorManager {
    const val MODEL = "aya:8b"

    @JvmStatic
    fun main(args: Array<String>) {
        supportedLanguages
            .translationTasks()
            .forEach(::println)
            .run {
                userLanguage.run { "userLanguage : $this" }.run(::println)
                translateMessage.run { "translateMessage : $this" }.run(::println)
            }.run {
                val currentLocale = Locale.getDefault()
                currentLocale.run { "currentLocale : $this" }.run(::println)
                // Force English language for display
                Locale.setDefault(ENGLISH)
                // Get the display name of the English locale
                val englishDisplayName = ENGLISH.getDisplayLanguage(ENGLISH)
                println("English display name: $englishDisplayName")
            }
    }

    @JvmStatic
    val supportedLanguages: Set<String> = setOf(
        FRENCH, ENGLISH, GERMAN,
        forLanguageTag("ru"),
        forLanguageTag("es"),
    ).map { it.language }.toSet()

    @JvmStatic
    fun Set<String>.translationTasks(): Set<String> = mutableSetOf<String>().apply {
        this@translationTasks
            .map { it.uppercaseFirstChar }
            .run langNames@{
                forEach { from: String ->
                    "translate${from}To".run {
                        this@langNames.filter {
                            it != from
                        }.forEach { add("$this$it") }
                    }
                }
            }
    }


    object PromptManager {
        val userLanguage = System.getProperty("user.language")!!
        val fromLanguage = System.getProperty("user.language")!!
        val toLanguage: String = ENGLISH.toLanguageTag()
        val text = ""
        val translateMessage = """Translate this sentence from $fromLanguage to $toLanguage : 
            $text""".trimMargin()
    }


    //translatorSupportedLanguages
    // Creating tasks for each model
    fun Project.createTranslationTasks() = supportedLanguages
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