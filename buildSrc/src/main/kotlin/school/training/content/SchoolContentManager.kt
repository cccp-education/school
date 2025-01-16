package school.training.content

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.poi.xwpf.usermodel.XWPFDocument
import school.workspace.WorkspaceUtils
import java.io.File
import java.io.FileInputStream


object SchoolContentManager {
    val SPD.toYaml: String
        get() = run(YAMLMapper()::writeValueAsString)
    val SPD.toJson: String
        get() = run(JsonMapper()::writeValueAsString)
    val String.spdYamlMapper: SPD
        get() = run(YAMLMapper()::readValue)
    val String.spdJsonMapper: SPD
        get() = run(JsonMapper()::readValue)

    val SPG.toYaml: String
        get() = run(YAMLMapper()::writeValueAsString)
    val SPG.toJson: String
        get() = run(JsonMapper()::writeValueAsString)
    val String.spgYamlMapper: SPG
        get() = run(YAMLMapper()::readValue)
    val String.spgJsonMapper: SPG
        get() = run(JsonMapper()::readValue)

    val Training.toYaml: String
        get() = run(YAMLMapper()::writeValueAsString)
    val Training.toJson: String
        get() = run(JsonMapper()::writeValueAsString)
    val String.trainingYamlMapper: Training
        get() = run(YAMLMapper()::readValue)
    val String.trainingJsonMapper: Training
        get() = run(JsonMapper()::readValue)

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