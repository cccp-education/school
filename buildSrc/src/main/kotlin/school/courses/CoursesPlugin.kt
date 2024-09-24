package school.courses

import com.fasterxml.jackson.module.kotlin.readValue
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register
import school.WorkspaceUtils.yamlMapper
import school.courses.Courses.JSON_FILE
import school.courses.Courses.ROOT_NODE
import java.io.File

class CoursesPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register<DefaultTask>("schoolProcess") {
            group = "school"
            description = "Processes used in school."
            val text = """
La formation de pré-professionalisation "Concepteur Développeur d’Applications" est conçue pour préparer les futurs professionnels du développement logiciel en leur offrant une maîtrise complète des outils et techniques essentiels. Ce programme s'articule autour de plusieurs modules clés, chacun visant à développer des compétences spécifiques et nécessaires pour exceller dans le domaine.

Le premier module se concentre sur la communication, une compétence indispensable pour tout professionnel. Les participants apprendront à exploiter efficacement les moteurs de recherche et les réseaux sociaux, ainsi qu'à utiliser des outils avancés comme ChatGPT pour améliorer leur communication écrite et leur capacité à rédiger des prompts efficaces.

La formation inclut également une partie dédiée à l'outillage informatique, où les apprenants seront guidés dans la mise en place et la gestion de leur environnement de travail. Ce module leur permettra de maîtriser l'utilisation des logiciels essentiels et d'adopter les bonnes pratiques pour une productivité optimale.

Le programme se poursuit avec des sessions sur la rédaction de documents techniques en utilisant AsciiDoc, ainsi que sur la création de diagrammes professionnels avec PlantUML. Ces compétences sont cruciales pour documenter et présenter les projets de manière claire et professionnelle.

Enfin, la formation aborde les techniques de présentation, en utilisant des outils comme Gradle et RevealJs, permettant aux participants de créer des présentations dynamiques et interactives.

En résumé, cette formation offre une préparation complète pour ceux qui aspirent à devenir concepteurs développeurs d’applications, en leur fournissant les compétences techniques et communicationnelles nécessaires pour réussir dans ce domaine en constante évolution.
"""

            fun String.wordCount(): Int = replace(
                "[^a-zA-Z\\s]".toRegex(), ""
            ).split("\\s+".toRegex()).size
            doFirst { "word count : ${text.wordCount()}".let(::println) }
        }

        project.tasks.register<DefaultTask>("createPatronFormation") {
            group = "school"
            description = "Create patron formation in build folder."
            doLast {
                // Fonction récursive pour créer l'arborescence
                fun File.createStructure(structure: DirectoryStructure?) {
                    structure!!.apply {
// Créer les fichiers
                        files.forEach { fileName ->
                            File(this@createStructure, fileName).apply {
                                when {
                                    !exists() -> createNewFile()
                                }
                            }

                        }
// Créer les répertoires et leur contenu
                        directories.forEach { (dirName, subStructure) ->
                            File(this@createStructure, dirName).apply {
                                when {
                                    !exists() -> mkdirs()
                                }
                                createStructure(subStructure)
                            }
                        }
                    }
                }

// Démarrer la création de l'arborescence depuis le dossier de formation
                project.yamlMapper.readValue<Map<String, DirectoryStructure>>(
                    "${project.layout.projectDirectory}/$JSON_FILE".let(::File)
                )[ROOT_NODE].let(
                    File(
                        project.layout.buildDirectory.get().asFile, ROOT_NODE
                    ).apply {
                        when {
                            !exists() -> mkdirs()
                        }
                    }::createStructure
                )
            }
        }

    }
}