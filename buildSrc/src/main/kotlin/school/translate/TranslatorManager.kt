package school.translate

import school.translate.TranslatorManager.PromptManager.translateMessage
import school.translate.TranslatorManager.PromptManager.userLanguage
import school.workspace.WorkspaceUtils.uppercaseFirstChar
import java.util.*

object TranslatorManager {
    @JvmStatic
    fun main(args: Array<String>) = translatorSupportedLanguages
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

    @JvmStatic
    val translatorSupportedLanguages: Set<String> = setOf(
        Locale.FRENCH,
        Locale.ENGLISH,
        Locale.GERMAN,
        Locale.of("es")
    ).map { it.language }.toSet()

    @JvmStatic
    fun Set<String>.translationTasks(): Set<String> = mutableSetOf<String>().apply {
        val map = this@translationTasks.map { it.uppercaseFirstChar }
        map.run {
            forEach { from ->
                "translate${from}To".run {
                    map.filter { to -> to != from }
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
}