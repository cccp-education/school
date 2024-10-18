@file:Suppress("MemberVisibilityCanBePrivate")

package school

import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.junit.jupiter.api.assertDoesNotThrow
import school.GradleTestUtils.captureOutput
import school.GradleTestUtils.displayWorkspaceDataSchemaStructure
import school.GradleTestUtils.displayWorkspaceStructure
import school.GradleTestUtils.initWorkspace
import school.GradleTestUtils.projectInstance
import school.GradleTestUtils.releaseOutput
import school.PluginTests.Workspace.WorkspaceEntry
import school.PluginTests.Workspace.WorkspaceEntry.CollaborationEntry.Collaboration
import school.PluginTests.Workspace.WorkspaceEntry.CommunicationEntry.Communication
import school.PluginTests.Workspace.WorkspaceEntry.ConfigurationEntry.Configuration
import school.PluginTests.Workspace.WorkspaceEntry.CoreEntry.Education
import school.PluginTests.Workspace.WorkspaceEntry.CoreEntry.Education.EducationEntry.*
import school.PluginTests.Workspace.WorkspaceEntry.DashboardEntry.Dashboard
import school.PluginTests.Workspace.WorkspaceEntry.JobEntry.Job
import school.PluginTests.Workspace.WorkspaceEntry.OfficeEntry.Office
import school.PluginTests.Workspace.WorkspaceEntry.OrganisationEntry.Organisation
import school.forms.FormPlugin
import school.frontend.SchoolPlugin
import school.frontend.SchoolPlugin.Companion.TASK_HELLO
import school.jbake.JBakeGhPagesPlugin
import java.lang.System.out
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class PluginTests {

    data class Workspace(
        //Deskboard-Bibliotheque-Tiroir-Thematique-Dossier
        val workspace: WorkspaceEntry,
    ) {
//        data class HumanResources(val cv: String)
//    data class Workspace(val portfolio: MutableMap<String, Project>) {
//        data class Project(//a mettre dans configuration
//            val name: String,
//            val cred: String,
//            val builds: MutableMap<String, ProjectBuild>
//        ) {
//            data class ProjectBuild(val name: String)
//        }
//    }
        data class WorkspaceEntry(
            val name: String,
            val cores: Map<String, CoreEntry>,
            val job: JobEntry,
            val configuration: ConfigurationEntry,
            val office: OfficeEntry,
            val communication: CommunicationEntry,
            val organisation: OrganisationEntry,
            val collaboration: CollaborationEntry,
            val dashboard: DashboardEntry,
        ) {
            sealed interface CoreEntry {
                data class Education(
                    val school: EducationEntry,
                    val student: EducationEntry,
                    val teacher: EducationEntry,
                    val educationTools: EducationEntry,
                ) : CoreEntry {
                    sealed class EducationEntry {
                        data class Student(val name: String) : EducationEntry()
                        data class Teacher(val name: String) : EducationEntry()
                        data class School(val name: String) : EducationEntry()
                        data class EducationTools(val name: String) : EducationEntry()
                    }
                }
            }

            sealed interface JobEntry {
                data class Job(val position: String) : JobEntry
            }

            sealed interface ConfigurationEntry {
                data class Configuration(val configuration: String) : ConfigurationEntry
            }

            sealed interface CommunicationEntry {
                data class Communication(val site: String) : CommunicationEntry
            }

            sealed interface OrganisationEntry {
                data class Organisation(val organisation: String) : OrganisationEntry
            }

            sealed interface CollaborationEntry {
                data class Collaboration(val collaboration: String) : CollaborationEntry
            }

            sealed interface DashboardEntry {
                data class Dashboard(val dashboard: String) : DashboardEntry
            }

            sealed interface OfficeEntry {
                data class Office(
                    val books: String,
                    val datas: String,
                    val formations: String,
                    val bizness: String,
                    val notebooks: String,
                    val pilotage: String,
                    val schemas: String,
                    val slides: String,
                    val sites: String
                ) : OfficeEntry
            }
        }
    }

    companion object {
        @JvmStatic
        val Project.workspace: Workspace
            get() = Workspace(
                workspace = WorkspaceEntry(
                    name = "fonderie",
                    cores = mapOf(
                        "education" to Education(
                            school = School(name = "talaria"),
                            student = Student(name = "olivier"),
                            teacher = Teacher(name = "cheroliv"),
                            educationTools = EducationTools(name = "edTools")
                        ),
                    ),
                    job = Job(position = "Teacher"),
                    configuration = Configuration(configuration = "school-configuration"),
                    communication = Communication(site = "static-website"),
                    office = Office(
                        books = "books-collection",
                        datas = "datas",
                        formations = "formations",
                        bizness = "bizness",
                        notebooks = "notebooks",
                        pilotage = "pilotage",
                        schemas = "schemas",
                        slides = "slides",
                        sites = "sites"
                    ),
                    organisation = Organisation(organisation = "organisation"),
                    collaboration = Collaboration(collaboration = "collaboration"),
                    dashboard = Dashboard(dashboard = "dashboard"),
                )
            )
    }

    @Test
    fun `test Workspace structure`(): Unit {
        assertDoesNotThrow {
            projectInstance.displayWorkspaceStructure()
            projectInstance.displayWorkspaceDataSchemaStructure()
        }
    }

    @Test
    fun checkInitWorkspace(): Unit = initWorkspace
        .run(school.workspace.Office::isEmpty)
        .run(::assertTrue)

    /**
     * Workspace est une map et cette map a besoin de fonctionnalités.
     *
     * - Ajouter une paire clé/map au bout du chemin de clé dans un list
     * - Supprimer une paire clé/map au bout du chemin de clé dans un list
     * - Ajouter une paire clé/valeur sur un chemin de clé dans une list de string
     * - Trouver une valeur par chemin de clé
     * - Mettre à jour une valeur par chemin de clé
     *
     */
    @Test
    fun checkAddEntryToWorkspace(): Unit {
        fun school.workspace.Office.addEntry(entry: school.workspace.OfficeEntry) {
//        put(entry.first.last(),entry.second)
        }

        val ws: school.workspace.Office = initWorkspace
        ws.addEntry(
            listOf(
                "workspace",
                "portfolio",
                "projects",
                "form",
                "cred"
            ) to "/home/foo/workspace/bureau/cred.json"
        )
    }

    @Test
    @Ignore
    fun `From FormPlugin, check 'form' task render expected message`(): Unit {
        val outputStreamCaptor = captureOutput
        projectInstance.run {
            apply<FormPlugin>()
            "isFormAuthOk".let(tasks::findByName)!!
            outputStreamCaptor
                .toString()
                .trim()
                .let { "Output: $it" }
                .let(::println)
        }
//        assertEquals(
//            "Task :isFormAuthOk",

//        )
        out.releaseOutput
    }

    @Test
    fun `From FormPlugin, check 'form' task exists`(): Unit {
        projectInstance.run {
            apply<FormPlugin>()
            assertNotNull("isFormAuthOk".let(tasks::findByName))
        }
    }


    @Test
    fun `From SchoolPlugin, check 'hello' task exists`(): Unit {
        projectInstance.run {
            apply<SchoolPlugin>()
            assertNotNull(TASK_HELLO.let(tasks::findByName))
        }
    }

    @Test
    fun `From JBakeGhPagesPlugin, check 'helloJBakeGhPages' task exists`(): Unit {
        projectInstance.run {
            apply<JBakeGhPagesPlugin>()
            assertNotNull("helloJBakeGhPages".let(tasks::findByName))
        }
    }
}