package school.translate

import org.gradle.api.Plugin
import org.gradle.api.Project
import school.translate.TranslatorManager.createDisplaySupportedLanguagesTask
import school.translate.TranslatorManager.createTranslationTasks

class TranslatorPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = project.run {
        createDisplaySupportedLanguagesTask()
        createTranslationTasks()
    }
}