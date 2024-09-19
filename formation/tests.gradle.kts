import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

class CreatePatronFormationTest {

    @Test
    fun `test createPatronFormation task`() {
        // Setup projet Gradle pour le test
        val project = ProjectBuilder.builder().build()

        // Exécute la tâche
        project.tasks.getByName("createPatronFormation").actions.forEach { it.execute(project.tasks.getByName("createPatronFormation")) }

        // Dossier racine à vérifier
        val rootDir = File(project.buildDir, Formation.ROOT_NODE)

        // Vérifier que le dossier racine a été créé
        assertTrue(rootDir.exists() && rootDir.isDirectory, "Root directory should exist")

        // Chemin de quelques fichiers et dossiers à vérifier
        val expectedFiles = listOf(
            "emargement.adoc",
            "evaluation.adoc",
            "fiche-produit.adoc",
            "formalisation-reponse.adoc",
            "formation.adoc",
            "inscription.adoc",
            "livret-apprenant.adoc",
            "satisfaction.adoc",
            "SPG.adoc"
        )

        expectedFiles.forEach {
            val file = File(rootDir, it)
            assertTrue(file.exists() && file.isFile, "File $it should exist")
        }

        val modulesDir = File(rootDir, "modules")
        assertTrue(modulesDir.exists() && modulesDir.isDirectory, "Modules directory should exist")

        // Continuer avec des vérifications similaires pour le reste de l'arborescence
        // Exemple: Vérification des sous-dossiers et fichiers dans 'modules', 'parties', 'sequences', 'seances'
    }
}
