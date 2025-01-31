package school.training.content

import com.fasterxml.jackson.annotation.JsonRootName

/*prompt: Genere moi un jeux de valeur en json pour cette classe, le sujet de la formation sera kotlin et langchain4j.  @JvmRecord @JsonRootName(value = "SPG") data class SPG(     val theme: Map<String, String> = mapOf("Thème" to ""),     val title: Map<String, String> = mapOf("Titre" to ""),     val presentation: String = "Présentation et description",     val mindmap: String = "Carte thématique",     @JsonProperty("public_ prospect" )      val publicProspect: String = "Public",     val prerequiz: String = "Pré-requis et conditions d’accès à la formation (Qualiopi)",     val objs: String = "Objectifs pédagogiques (Qualiopi)",     val competences: String = "Compétences visées (Qualiopi)",     val timing: String = "Durée (Temporisation)] (Qualiopi)",     val means: String = "Moyen d’accompagnement et Suivi pédagogique (Qualiopi)",     val prgm: String = "Programme pédagogique (Modalités pédagogiques)] (Qualiopi) : du contenu et du séquencement",     val eval: String = "Modalités d’évaluations] (Qualiopi)",     val certif: String = "Modalités de certification et Certification visé] (Qualiopi)",     val place: String = "Lieux] (Qualiopi)",     val price: String = "Tarifs",     val infra: String = "Moyens logistiques et matériels] (Qualiopi)",     val pursuit: String = "Poursuite en formation] (Qualiopi)",     @JsonProperty("access_ time" )      val accessTime: String = "Délais d’accès] (Réglementaire)",     val mobility: String = "Accessibilité et Handicap] (Qualiopi)",     val testimony: String = "Témoignage Evaluation de la formation] (Qualiopi)",     @JsonProperty("testimony_ customer" )      val testimonyCustomer: String = "Témoignage apprenant/commanditaire" ,  )*/
@JsonRootName(value = "Training")
data class Training(
    //development doc
    val pilotage: QuintilianApproach,
    val spg: SPG,
    val spds: Set<SPD> = mutableSetOf(),
)