package workspace.slides

import org.gradle.api.Project
import java.io.File
import java.util.*

object SlideManager{
    fun Project.deckFile(key: String): String = buildString {
        append("build/docs/asciidocRevealJs/")
        append(Properties().apply {
            "$projectDir/deck.properties"
                .let(::File)
                .inputStream()
                .use(::load)
        }[key].toString())
    }
}