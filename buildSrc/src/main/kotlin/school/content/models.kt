@file:Suppress("unused")

package school.content

@JvmRecord
data class Formation(
    val nom: String,
    val spg: SPG
)

/**
 * SPG: Scénario Pédagogique Global
 */
@JvmRecord
data class SPG(
    val spds: Set<SPD> = emptySet(),
    val theme: String?,
    val title: String?,
    val presentation: String?,
    val mindmap: String?,
    val publicProspect: String?,
    val prerequiz: String?,
    val objs: String?,
    val competences: String?,
    val timing: String?,
    val means: String?,
    val prgm: String?,
    val eval: String?,
    val certif: String?,
    val place: String?,
    val price: String?,
    val infra: String?,
    val pursuit: String?,
    val accessTime: String?,
    val mobility: String?,
    val testimony: String?,
    val testimonyCustomer: String?,
    val content: List<Triple</*json_key*/String,/*caption*/String, /*attribute*/String>>? = listOf(
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
)

/**
 * SPD : Scénario Pédagogique Détaillé
 */
@JvmRecord
data class SPD(
    val titre: String = "",
    val objectif: String = ""
)