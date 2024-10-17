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
import school.PluginTests.Workspace.WorkspaceEntry.CoreEntry.Education
import school.PluginTests.Workspace.WorkspaceEntry.CoreEntry.Education.EducationEntry.*
import school.forms.FormPlugin
import school.frontend.SchoolPlugin
import school.frontend.SchoolPlugin.Companion.TASK_HELLO
import school.jbake.JBakeGhPagesPlugin
import school.workspace.Office
import school.workspace.OfficeEntry
import java.lang.System.out
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class PluginTests {
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
//                                "bibliotheque" to Office(
//                                    "books-collection",
//                                    "datas",
//                                    "formations",
//                                    "bizness",
//                                    "notebooks",
//                                    "pilotage",
//                                    "schemas",
//                                    "slides",
//                                    "sites"
//                                ),
                    )
                )
//                "bibliotheque",
//                "job",
//                "configuration",
//                "communication",
//                "organisation",
//                "collaboration",
//                "dashboard",
            )

    }

    //Deskboard-Bibliotheque-Tiroir-Thematique-Dossier
//data class SchoolOffice(
//    val bibliotheque: Bibliotheque? = null,
//    val workspace: Workspace,
//    val humanResources: HumanResources? = null
//) {
//    data class Bibliotheque(
//        val courses: MutableMap<String, Course>?,
//        val catalogue: MutableMap<String, Training>?,
//        val projectDocs: MutableMap<String, ProjectDocumentation>,
//    ) {
//        data class Course(val name: String)
//        data class Training(val name: String)
//        data class ProjectDocumentation(val name: String)
//    }
//
//    data class Workspace(val portfolio: MutableMap<String, Project>) {
//        data class Project(
//            val name: String,
//            val cred: String,
//            val builds: MutableMap<String, ProjectBuild>
//        ) {
//            data class ProjectBuild(val name: String)
//        }
//    }
//
//    data class HumanResources(val cv: String)
//}
    data class Workspace(
        val workspace: WorkspaceEntry,
//        val core: WorkspaceEntry,
//        val office: String,
//        val job: String,
//        val configuration: String,
//        val communication: String,
//        val organisation: String,
//        val collaboration: String,
//        val dashboard: String,
    ) {

        data class WorkspaceEntry(val name: String, val cores: Map<String, CoreEntry>) {
            interface CoreEntry {
                data class Education(
                    val school: School,
                    val student: Student,
                    val teacher: Teacher,
                    val educationTools: EducationTools,
                ) : CoreEntry {
                    sealed class EducationEntry {
                        data class Student(val name: String) : EducationEntry()
                        data class Teacher(val name: String) : EducationEntry()
                        data class School(val name: String) : EducationEntry()
                        data class EducationTools(val name: String) : EducationEntry()
                    }
                }
            }

            interface JobEntry
            interface ConfigurationEntry
            interface CommunicationEntry
            interface OrganisationEntry
            interface CollaborationEntry
            interface DashboardEntry
            interface OfficeEntry


        }

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
        )// : CoreEntry
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
        .run(Office::isEmpty)
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
        fun Office.addEntry(entry: OfficeEntry) {
//        put(entry.first.last(),entry.second)
        }
        val ws: Office = initWorkspace
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