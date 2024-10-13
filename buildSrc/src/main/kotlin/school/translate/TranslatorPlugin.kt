@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package school.translate

import org.gradle.api.Plugin
import org.gradle.api.Project
import school.translate.TranslatorManager.createTranslationTasks
import school.translate.TranslatorManager.createDisplaySupportedLanguagesTask

class TranslatorPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = project.run {
        createDisplaySupportedLanguagesTask()
        createTranslationTasks()
    }
}