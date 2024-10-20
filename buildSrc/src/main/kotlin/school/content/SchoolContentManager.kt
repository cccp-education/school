package school.content

import org.apache.poi.xwpf.usermodel.XWPFDocument
import school.workspace.WorkspaceUtils
import java.io.File
import java.io.FileInputStream

object SchoolContentManager {
    @Suppress("MemberVisibilityCanBePrivate")
    val DOSSIER_PROFESSIONNELLE_PATH by lazy { "${WorkspaceUtils.sep}projects${WorkspaceUtils.sep}school${WorkspaceUtils.sep}rsrc${WorkspaceUtils.sep}docs${WorkspaceUtils.sep}dossier_professionnel_titre.docx" }
    fun printDossierProfessionnelle(projectDir: File) = projectDir.absolutePath
        .plus(DOSSIER_PROFESSIONNELLE_PATH)
        .apply(SchoolContentManager::printDocxStructure)
        .run(::println)

    @Suppress("MemberVisibilityCanBePrivate")
    fun printDocxStructure(path: String): Unit = path.run(::FileInputStream)
        .run(::XWPFDocument)
        .run {
            paragraphs.forEach { p ->
                p.text
                    .let { "Paragraph: $it" }
                    .let(::println)
            }
            tables.forEach { table ->
                "Table : ".let(::println)
                table.rows.forEach { row ->
                    row.tableCells.forEach { cell ->
                        cell.text
                            .let { "  Cell : $it" }
                            .let(::println)
                    }
                }
            }
        }
}