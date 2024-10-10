package school.translate

import school.workspace.WorkspaceUtils.uppercaseFirstChar
import java.util.*

object TranslatorManager {
    @JvmStatic
    val translatorSupportedLanguages: Set<String> = setOf(
        Locale.FRENCH,
        Locale.ENGLISH,
        Locale.GERMAN,
        Locale.of("es")
    ).map { it.language }.toSet()


    @JvmStatic
    fun Set<String>.translationTasks(): Set<String> = mutableSetOf<String>().apply {
        this@translationTasks
            .map { it.uppercaseFirstChar }
            .forEach { from ->
                "translate${from}To".run {
                    this@translationTasks
                        .filter { to -> to != from }
                        .forEach { add("$this${it}") }
                }
            }
    }

    @JvmStatic
    fun main(args: Array<String>) = translatorSupportedLanguages.translationTasks().forEach(::println)

}