package workspace

import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.testfixtures.ProjectBuilder
import workspace.forms.FormPlugin
import workspace.jamstack.JBakeGhPagesPlugin
import workspace.school.SchoolPlugin
import workspace.school.SchoolPlugin.Companion.TASK_HELLO
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

    @Test
    fun checkWorkspaceStruture() {
//        val office: Office = Office(
//            workspace = Workspace(
////                mutableMapOf<String, Workspace.Project>("portfolio" to mutableMapOf<String, Workspace.Project>("",Project()))
//            )
//        )

////        val w: Workspace = Workspace(
////            portfolio = mutableMapOf("projects" to mutableMapOf("school" to Project))
////        )
//        office
//            .workspace
//            .toString()
//            .let(::println)
//        projectInstance.run {
//            yamlMapper.writeValueAsString(office).let(::println)
//        }
    }

    @Test
    fun checkInitWorkspace() = initWorkspace
        .run(Bureau::isEmpty)
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
        val ws: Bureau = initWorkspace
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

    fun Bureau.addEntry(entry: BureauEntry) {
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
}
