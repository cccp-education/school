package school

import com.fasterxml.jackson.module.kotlin.readValue
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.testfixtures.ProjectBuilder
import school.PluginTests.Forge.Workspace
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
import kotlin.test.assertEquals
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

    data class Forge(val workspace: Workspace){
        data class Workspace(
            val bibliotheque: String,
            val coreBusiness: String,
            val job: String,
            val configuration: String,
            val communication: String,
            val organisation: String,
            val collaboration: String,
            val dashboard: String,
        )
    }

    @Test
    fun checkWorkspaceStruture() {
        val workspace = Workspace(
            "bibliotheque",
            "coreBusiness",
            "job",
            "configuration",
            "communication",
            "organisation",
            "collaboration",
            "dashboard",
        )
        val hardCodedYamlConf = """
            workspace:
              bibliotheque: "bibliotheque"
              coreBusiness: "coreBusiness"
              job: "job"
              configuration: "configuration"
              communication: "communication"
              organisation: "organisation"
              collaboration: "collaboration"
              dashboard: "dashboard"
                """.trimIndent()
        projectInstance.run {
            yamlMapper.run {
                readValue<Forge>(hardCodedYamlConf).run {
                    assertEquals(workspace, workspace)
                    assertEquals(workspace, readValue<Forge>(writeValueAsString(Forge(workspace))).workspace)
                }
            }
        }
    }
//    data class Workspace(entry: WorspaceEntry)

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

    companion object {
        @JvmStatic
        val projectInstance: Project
            get() = ProjectBuilder.builder().build()
    }
    val yamlConf = """
        workspace:
          portfolio:
            projects:
              school:
                builds:
                  frontend:
                    path: "/home/cheroliv/workspace/atelier/school/frontend"
                    repository:
                      from: "dist"
                      to: "cvs"
                      url: "https://github.com/cheroliv/talaria.git"
                      credentials:
                        username: "cheroliv"
                        token: "token-value"
                      branch: "master"
                      message: "https://cheroliv.github.io/talaria"
    """.trimIndent()
}
