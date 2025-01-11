@file:Suppress("unused")

package school.content

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.readValue
import school.content.SPG.Data.ACCESS_TIME_CAPTION
import school.content.SPG.Data.CERTIFICATION_CAPTION
import school.content.SPG.Data.EVALUATIONS_CAPTION
import school.content.SPG.Data.INFRASTRUCTURE_CAPTION
import school.content.SPG.Data.MEANS_CAPTION
import school.content.SPG.Data.MINDMAP_CAPTION
import school.content.SPG.Data.MOBILITY_CAPTION
import school.content.SPG.Data.OBJECTIVES_CAPTION
import school.content.SPG.Data.PLACE_CAPTION
import school.content.SPG.Data.PRESENTATION_CAPTION
import school.content.SPG.Data.PRICE_CAPTION
import school.content.SPG.Data.PROGRAM_CAPTION
import school.content.SPG.Data.PUBLIC_PROSPECT_CAPTION
import school.content.SPG.Data.PURSUIT_CAPTION
import school.content.SPG.Data.SKILLS_CAPTION
import school.content.SPG.Data.TESTIMONY_CAPTION
import school.content.SPG.Data.TESTIMONY_CUSTOMER_CAPTION
import school.content.SPG.Data.THEME_CAPTION
import school.content.SPG.Data.TIMING_CAPTION
import school.content.SPG.Data.TITLE_CAPTION


val content: List<Triple</*json_key*/String,/*caption*/String, /*attribute*/String>> = listOf(
    Triple("theme", "Thème", "theme"),
    Triple("title", "Titre", "titre"),
    Triple("presentation", "Présentation et description", "presentation"),
    Triple("mindmap", "Carte thématique", "mindmap"),
    Triple("public", "Public", "publicProspect"),
    Triple("pre_requis", "Pré-requis et conditions d’accès à la formation (Qualiopi)", "preRequis"),
    Triple("objectives", "Objectifs pédagogiques (Qualiopi)", "objectives"),
    Triple("skills", "Compétences visées (Qualiopi)", "skills"),
    Triple("timing", "Durée (Temporisation)] (Qualiopi)", "timing"),
    Triple("means", "Moyen d’accompagnement et Suivi pédagogique (Qualiopi)", "means"),
    Triple(
        "program",
        "Programme pédagogique (Modalités pédagogiques)] (Qualiopi) : du contenu et du séquencement",
        "program"
    ),
    Triple("evaluations", "Modalités d’évaluations] (Qualiopi)", "evaluations"),
    Triple("certification", "Modalités de certification et Certification visé] (Qualiopi)", "certif"),
    Triple("place", "Lieux] (Qualiopi)", "place"),
    Triple("price", "Tarifs", "price"),
    Triple("infrastructure", "Moyens logistiques et matériels] (Qualiopi)", "infrastructure"),
    Triple("pursuit", "Poursuite en formation] (Qualiopi)", "pursuit"),
    Triple("access_time", "Délais d’accès] (Réglementaire)", "accessTime"),
    Triple("mobility", "Accessibilité et Handicap] (Qualiopi)", "mobility"),
    Triple("testimony", "Témoignage Evaluation de la formation] (Qualiopi)", "testimony"),
    Triple("testimony_customer", "Témoignage apprenant/commanditaire", "testimonyCustomer")
)
/*prompt: Genere moi un jeux de valeur en json pour cette classe, le sujet de la formation sera kotlin et langchain4j.  @JvmRecord @JsonRootName(value = "SPG") data class SPG(     val theme: Map<String, String> = mapOf("Thème" to ""),     val title: Map<String, String> = mapOf("Titre" to ""),     val presentation: String = "Présentation et description",     val mindmap: String = "Carte thématique",     @JsonProperty("public_ prospect" )      val publicProspect: String = "Public",     val prerequiz: String = "Pré-requis et conditions d’accès à la formation (Qualiopi)",     val objs: String = "Objectifs pédagogiques (Qualiopi)",     val competences: String = "Compétences visées (Qualiopi)",     val timing: String = "Durée (Temporisation)] (Qualiopi)",     val means: String = "Moyen d’accompagnement et Suivi pédagogique (Qualiopi)",     val prgm: String = "Programme pédagogique (Modalités pédagogiques)] (Qualiopi) : du contenu et du séquencement",     val eval: String = "Modalités d’évaluations] (Qualiopi)",     val certif: String = "Modalités de certification et Certification visé] (Qualiopi)",     val place: String = "Lieux] (Qualiopi)",     val price: String = "Tarifs",     val infra: String = "Moyens logistiques et matériels] (Qualiopi)",     val pursuit: String = "Poursuite en formation] (Qualiopi)",     @JsonProperty("access_ time" )      val accessTime: String = "Délais d’accès] (Réglementaire)",     val mobility: String = "Accessibilité et Handicap] (Qualiopi)",     val testimony: String = "Témoignage Evaluation de la formation] (Qualiopi)",     @JsonProperty("testimony_ customer" )      val testimonyCustomer: String = "Témoignage apprenant/commanditaire" ,  )*/
@JvmRecord
@JsonRootName(value = "Training")
data class Training(
    val spg: SPG,
    val spds: Set<SPD> = emptySet()
)

/** SPG: Scénario Pédagogique Global */
// TODO: A la place de caption on mettra la caption_lang_key
@JvmRecord
@JsonRootName(value = "SPG")
data class SPG(
    val theme: Map</*caption*/String, /*value*/String> = mapOf(THEME_CAPTION to ""),
    val title: Map<String, String> = mapOf(TITLE_CAPTION to ""),
    val presentation: Map<String, String> = mapOf(PRESENTATION_CAPTION to ""),
    val mindmap: Map<String, String> = mapOf(MINDMAP_CAPTION to ""),
    @JsonProperty("public_prospect")
    val publicProspect: Map<String, String> = mapOf(PUBLIC_PROSPECT_CAPTION to ""),
    @JsonProperty("pre_requis")
    val preRequis: Map<String, String> = mapOf(PRESENTATION_CAPTION to ""),
    val objectives: Map<String, String> = mapOf(OBJECTIVES_CAPTION to ""),
    val skills: Map<String, String> = mapOf(SKILLS_CAPTION to ""),
    val timing: Map<String, String> = mapOf(TIMING_CAPTION to ""),
    val means: Map<String, String> = mapOf(MEANS_CAPTION to ""),
    val program: Map<String, String> = mapOf(PROGRAM_CAPTION to ""),
    val evaluations: Map<String, String> = mapOf(EVALUATIONS_CAPTION to ""),
    val certification: Map<String, String> = mapOf(CERTIFICATION_CAPTION to ""),
    val place: Map<String, String> = mapOf(PLACE_CAPTION to ""),
    val price: Map<String, String> = mapOf(PRICE_CAPTION to ""),
    val infrastructure: Map<String, String> = mapOf(INFRASTRUCTURE_CAPTION to ""),
    val pursuit: Map<String, String> = mapOf(PURSUIT_CAPTION to ""),
    @JsonProperty("access_time")
    val accessTime: Map<String, String> = mapOf(ACCESS_TIME_CAPTION to ""),
    val mobility: Map<String, String> = mapOf(MOBILITY_CAPTION to ""),
    val testimony: Map<String, String> = mapOf(TESTIMONY_CAPTION to ""),
    @JsonProperty("testimony_customer")
    val testimonyCustomer: Map<String, String> = mapOf(TESTIMONY_CUSTOMER_CAPTION to ""),
){
    object Data {
        const val THEME_CAPTION = "Thème"
        const val TITLE_CAPTION = "Titre"
        const val PRESENTATION_CAPTION = "Présentation et description"
        const val MINDMAP_CAPTION = "Carte thématique"
        const val PUBLIC_PROSPECT_CAPTION = "Public"
        const val PRE_REQUIS_CAPTION = "Pré-requis et conditions d’accès à la formation (Qualiopi)"
        const val OBJECTIVES_CAPTION = "Objectifs pédagogiques (Qualiopi)"
        const val SKILLS_CAPTION = "Compétences visées (Qualiopi)"
        const val TIMING_CAPTION = "Durée (Temporisation)] (Qualiopi)"
        const val MEANS_CAPTION = "Moyen d’accompagnement et Suivi pédagogique (Qualiopi)"
        const val PROGRAM_CAPTION =
            "Programme pédagogique (Modalités pédagogiques)] (Qualiopi) : du contenu et du séquencement"
        const val EVALUATIONS_CAPTION = "Modalités d’évaluations] (Qualiopi)"
        const val CERTIFICATION_CAPTION = "Modalités de certification et Certification visé] (Qualiopi)"
        const val PLACE_CAPTION = "Lieux] (Qualiopi)"
        const val PRICE_CAPTION = "Tarifs"
        const val INFRASTRUCTURE_CAPTION = "Moyens logistiques et matériels] (Qualiopi)"
        const val PURSUIT_CAPTION = "Poursuite en formation] (Qualiopi)"
        const val ACCESS_TIME_CAPTION = "Délais d’accès] (Réglementaire)"
        const val MOBILITY_CAPTION = "Accessibilité et Handicap] (Qualiopi)"
        const val TESTIMONY_CAPTION = "Témoignage Evaluation de la formation] (Qualiopi)"
        const val TESTIMONY_CUSTOMER_CAPTION = "Témoignage apprenant/commanditaire"
    }
}

/** SPD: Scénario Pédagogique Détaillé */
@JvmRecord
@JsonRootName(value = "SPD")
data class SPD(
    val titre: String = "",
    val objectif: String = ""
)

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
