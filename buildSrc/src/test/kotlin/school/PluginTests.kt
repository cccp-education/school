package school

import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.assertDoesNotThrow
import school.PluginTests.Workspace.Education
import school.PluginTests.Workspace.WorkspaceEntry
import school.forms.FormPlugin
import school.frontend.SchoolPlugin
import school.frontend.SchoolPlugin.Companion.TASK_HELLO
import school.jbake.JBakeGhPagesPlugin
import school.workspace.Office
import school.workspace.OfficeEntry
import school.workspace.WorkspaceUtils.yamlMapper
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.lang.System.out
import java.lang.System.setOut
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

val captureOutput: ByteArrayOutputStream
    get() = "captureOutput"
        .let(::println).run {
            ByteArrayOutputStream().apply {
                let(::PrintStream).let(::setOut)
            }
        }


val PrintStream.releaseOutput
    get() = let(::setOut)
        .run { "releaseOutput" }
        .let(::println)


class PluginTests {
    companion object {
        @JvmStatic
        val projectInstance: Project get() = ProjectBuilder.builder().build()
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
//        val name:String,//Bad behavior
        val workspace: WorkspaceEntry,
//        val core: Map<String, CoreEntry>,
//        val office: String,
//        val job: String,
//        val configuration: String,
//        val communication: String,
//        val organisation: String,
//        val collaboration: String,
//        val dashboard: String,
    ) {
        interface CoreEntry
        data class WorkspaceEntry(val name: String, val core: Any)

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
        )

        data class Education(val name: String) : CoreEntry
    }

    @Test
    fun checkWorkspaceStruture() {
        assertDoesNotThrow {
            Workspace(
//                name= "cheroliworkspace",
                workspace = WorkspaceEntry(name = "forge", core = mapOf("school" to Education("talaria"))),
//                "bibliotheque",
//                mapOf("school" to Education("talaria")),
//                "job",
//                "configuration",
//                "communication",
//                "organisation",
//                "collaboration",
//                "dashboard",
            ).run workspace@{
                projectInstance
                    .yamlMapper
                    .writeValueAsString(this@workspace)
                    .apply { println(this) }
            }
        }
    }

    @Test
    fun checkInitWorkspace() = initWorkspace
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
    fun checkAddEntryToWorkspace() {
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

    val initWorkspace get() = mutableMapOf<String, MutableMap<String, MutableMap<String, MutableMap<String, MutableMap<String, MutableMap<String, MutableMap<String, Any>>>>?>?>>()

    fun Office.addEntry(entry: OfficeEntry) {
//        put(entry.first.last(),entry.second)
    }


    @Test
    fun `From FormPlugin, check 'form' task render expected message`() {
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
    fun `From FormPlugin, check 'form' task exists`() {
        projectInstance.run {
            apply<FormPlugin>()
            assertNotNull("isFormAuthOk".let(tasks::findByName))
        }
    }


    @Test
    fun `From SchoolPlugin, check 'hello' task exists`() {
        projectInstance.run {
            apply<SchoolPlugin>()
            assertNotNull(TASK_HELLO.let(tasks::findByName))
        }
    }

    @Test
    fun `From JBakeGhPagesPlugin, check 'helloJBakeGhPages' task exists`() {
        projectInstance.run {
            apply<JBakeGhPagesPlugin>()
            assertNotNull("helloJBakeGhPages".let(tasks::findByName))
        }
    }
}