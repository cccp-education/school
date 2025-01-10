@file:Suppress("unused")

package school.content

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File

/*prompt: Genere moi un jeux de valeur en json pour cette classe, le sujet de la formation sera kotlin et langchain4j.  @JvmRecord @JsonRootName(value = "SPG") data class SPG(     val theme: Map<String, String> = mapOf("Thème" to ""),     val title: Map<String, String> = mapOf("Titre" to ""),     val presentation: String = "Présentation et description",     val mindmap: String = "Carte thématique",     @JsonProperty("public_ prospect" )      val publicProspect: String = "Public",     val prerequiz: String = "Pré-requis et conditions d’accès à la formation (Qualiopi)",     val objs: String = "Objectifs pédagogiques (Qualiopi)",     val competences: String = "Compétences visées (Qualiopi)",     val timing: String = "Durée (Temporisation)] (Qualiopi)",     val means: String = "Moyen d’accompagnement et Suivi pédagogique (Qualiopi)",     val prgm: String = "Programme pédagogique (Modalités pédagogiques)] (Qualiopi) : du contenu et du séquencement",     val eval: String = "Modalités d’évaluations] (Qualiopi)",     val certif: String = "Modalités de certification et Certification visé] (Qualiopi)",     val place: String = "Lieux] (Qualiopi)",     val price: String = "Tarifs",     val infra: String = "Moyens logistiques et matériels] (Qualiopi)",     val pursuit: String = "Poursuite en formation] (Qualiopi)",     @JsonProperty("access_ time" )      val accessTime: String = "Délais d’accès] (Réglementaire)",     val mobility: String = "Accessibilité et Handicap] (Qualiopi)",     val testimony: String = "Témoignage Evaluation de la formation] (Qualiopi)",     @JsonProperty("testimony_ customer" )      val testimonyCustomer: String = "Témoignage apprenant/commanditaire" ,  )*/
@JvmRecord
data class Formation(
    val nom: String,
    val spg: SPG
)

data class Training(
    val spg: SPG,
    val spds: Set<SPD> = emptySet()
)
// TODO: A la place de caption on mettra la caption_lang_key
val content: List<Triple</*json_key*/String,/*caption*/String, /*attribute*/String>> = listOf(
    Triple("theme", "Thème", "theme"),
    Triple("title", "Titre", "titre"),
    Triple("prez", "Présentation et description", "presentation"),
    Triple("mindmap", "Carte thématique", "mindmap"),
    Triple("public", "Public", "publicProspect"),
    Triple("prerequiz", "Pré-requis et conditions d’accès à la formation (Qualiopi)", "prerequiz"),
    Triple("objs", "Objectifs pédagogiques (Qualiopi)", "objs"),
    Triple("competences", "Compétences visées (Qualiopi)", "competences"),
    Triple("timing", "Durée (Temporisation)] (Qualiopi)", "timing"),
    Triple("means", "Moyen d’accompagnement et Suivi pédagogique (Qualiopi)", "means"),
    Triple("prgm", "Programme pédagogique (Modalités pédagogiques)] (Qualiopi) : du contenu et du séquencement", "prgm"),
    Triple("eval", "Modalités d’évaluations] (Qualiopi)", "eval"),
    Triple("certif", "Modalités de certification et Certification visé] (Qualiopi)", "certif"),
    Triple("place", "Lieux] (Qualiopi)", "place"),
    Triple("price", "Tarifs", "price"),
    Triple("infra", "Moyens logistiques et matériels] (Qualiopi)", "infra"),
    Triple("pursuit", "Poursuite en formation] (Qualiopi)", "pursuit"),
    Triple("access_time", "Délais d’accès] (Réglementaire)", "accessTime"),
    Triple("mobility", "Accessibilité et Handicap] (Qualiopi)", "mobility"),
    Triple("testimony", "Témoignage Evaluation de la formation] (Qualiopi)", "testimony"),
    Triple("testimony_customer", "Témoignage apprenant/commanditaire", "testimonyCustomer")
)
/** SPG: Scénario Pédagogique Global */
@JvmRecord
@JsonRootName(value = "SPG")
data class SPG(
    val theme: Map</*caption*/String, /*value*/String> = mapOf("Thème" to ""),
    val title: Map<String, String> = mapOf("Titre" to ""),
    val presentation: Map<String, String> = mapOf("Présentation et description" to ""),
    val mindmap: Map<String, String> = mapOf("Carte thématique" to ""),
    @JsonProperty("public_prospect")
    val publicProspect: Map<String, String> = mapOf("Public" to ""),
    val prerequiz: Map<String, String> = mapOf("Pré-requis et conditions d’accès à la formation (Qualiopi)" to ""),
    val objs: Map<String, String> = mapOf("Objectifs pédagogiques (Qualiopi)" to ""),
    val competences: Map<String, String> = mapOf("Compétences visées (Qualiopi)" to ""),
    val timing: Map<String, String> = mapOf("Durée (Temporisation)] (Qualiopi)" to ""),
    val means: Map<String, String> = mapOf("Moyen d’accompagnement et Suivi pédagogique (Qualiopi)" to ""),
    val prgm: Map<String, String> = mapOf("Programme pédagogique (Modalités pédagogiques)] (Qualiopi) : du contenu et du séquencement" to ""),
    val eval: Map<String, String> = mapOf("Modalités d’évaluations] (Qualiopi)" to ""),
    val certif: Map<String, String> = mapOf("Modalités de certification et Certification visé] (Qualiopi)" to ""),
    val place: Map<String, String> = mapOf("Lieux] (Qualiopi)" to ""),
    val price: Map<String, String> = mapOf("Tarifs" to ""),
    val infra: Map<String, String> = mapOf("Moyens logistiques et matériels] (Qualiopi)" to ""),
    val pursuit: Map<String, String> = mapOf("Poursuite en formation] (Qualiopi)" to ""),
    @JsonProperty("access_time")
    val accessTime: Map<String, String> = mapOf("Délais d’accès] (Réglementaire)" to ""),
    val mobility: Map<String, String> = mapOf("Accessibilité et Handicap] (Qualiopi)" to ""),
    val testimony: Map<String, String> = mapOf("Témoignage Evaluation de la formation] (Qualiopi)" to ""),
    @JsonProperty("testimony_customer")
    val testimonyCustomer: Map<String, String> = mapOf("Témoignage apprenant/commanditaire" to ""),
)

val SPG.toYaml: String
    get() = run(YAMLMapper()::writeValueAsString)
val SPG.toJson: String
    get() = run(JsonMapper()::writeValueAsString)
val String.spgYamlMapper: SPG
    get() = run(YAMLMapper()::readValue)
val String.spgJsonMapper: SPG
    get() = run(JsonMapper()::readValue)


/**
 * SPD : Scénario Pédagogique Détaillé
 */
@JvmRecord
data class SPD(
    val titre: String = "",
    val objectif: String = ""
)

object TeacherModels {
    @JvmStatic
    fun main(args: Array<String>): Unit {
        val trainingPath = "/workspace/school/buildSrc/src/main/resources/training_1.json"
        SPG().toJson.run(::println)
        SPG().toYaml.run(::println)
        "${System.getProperty("user.home")}$trainingPath"
            .run(::File)
            .readText()
            .apply(::println)
            .run(String::spgJsonMapper)
            .run(::println)

    }
}