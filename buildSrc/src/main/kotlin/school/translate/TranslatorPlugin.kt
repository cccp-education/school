@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package school.translate

import org.gradle.api.Plugin
import org.gradle.api.Project
import school.translate.TranslatorManager.createTranslationTasks

class TranslatorPlugin : Plugin<Project> {
    override fun apply(project: Project) = project.createTranslationTasks()
}