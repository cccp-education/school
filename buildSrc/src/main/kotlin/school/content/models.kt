@file:Suppress("unused")

package school.content

data class Formation(
    val nom: String,
    val spg: SPG
)

/**
 * SPG: Scénario Pédagogique Global
 */
data class SPG(
    val spd: SPD = SPD(),
    val content: List<Triple<String, String, String>>? = listOf(
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
data class SPD(
    val titre: String = "",
    val objectif: String = ""
)


