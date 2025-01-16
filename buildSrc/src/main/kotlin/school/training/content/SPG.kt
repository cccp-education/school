package school.training.content

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import school.training.content.SPG.Data.ACCESS_TIME_CAPTION
import school.training.content.SPG.Data.CERTIFICATION_CAPTION
import school.training.content.SPG.Data.EVALUATIONS_CAPTION
import school.training.content.SPG.Data.INFRASTRUCTURE_CAPTION
import school.training.content.SPG.Data.MEANS_CAPTION
import school.training.content.SPG.Data.MINDMAP_CAPTION
import school.training.content.SPG.Data.MOBILITY_CAPTION
import school.training.content.SPG.Data.OBJECTIVES_CAPTION
import school.training.content.SPG.Data.PLACE_CAPTION
import school.training.content.SPG.Data.PRESENTATION_CAPTION
import school.training.content.SPG.Data.PRE_REQUIS_CAPTION
import school.training.content.SPG.Data.PRICE_CAPTION
import school.training.content.SPG.Data.PROGRAM_CAPTION
import school.training.content.SPG.Data.PUBLIC_PROSPECT_CAPTION
import school.training.content.SPG.Data.PURSUIT_CAPTION
import school.training.content.SPG.Data.SKILLS_CAPTION
import school.training.content.SPG.Data.TESTIMONY_CAPTION
import school.training.content.SPG.Data.TESTIMONY_CUSTOMER_CAPTION
import school.training.content.SPG.Data.THEME_CAPTION
import school.training.content.SPG.Data.TIMING_CAPTION
import school.training.content.SPG.Data.TITLE_CAPTION

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
    val preRequis: Map<String, String> = mapOf(PRE_REQUIS_CAPTION to ""),
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
) {
    object Data {
        const val THEME_CAPTION = "Thème"
        const val TITLE_CAPTION = "Titre"
        const val PRESENTATION_CAPTION = "Présentation et description"
        const val MINDMAP_CAPTION = "Carte thématique"
        const val PUBLIC_PROSPECT_CAPTION = "Public"
        const val PRE_REQUIS_CAPTION = "Pré-requis et conditions d’accès à la formation (Qualiopi)"
        const val OBJECTIVES_CAPTION = "Objectifs pédagogiques (Qualiopi)"
        const val SKILLS_CAPTION = "Compétences visées (Qualiopi)"
        const val TIMING_CAPTION = "Durée (Temporisation) (Qualiopi)"
        const val MEANS_CAPTION = "Moyen d’accompagnement et Suivi pédagogique (Qualiopi)"
        const val PROGRAM_CAPTION =
            "Programme pédagogique (Modalités pédagogiques) (Qualiopi) : du contenu et du séquencement"
        const val EVALUATIONS_CAPTION = "Modalités d’évaluations (Qualiopi)"
        const val CERTIFICATION_CAPTION = "Modalités de certification et Certification visé (Qualiopi)"
        const val PLACE_CAPTION = "Lieux] (Qualiopi)"
        const val PRICE_CAPTION = "Tarifs"
        const val INFRASTRUCTURE_CAPTION = "Moyens logistiques et matériels (Qualiopi)"
        const val PURSUIT_CAPTION = "Poursuite en formation (Qualiopi)"
        const val ACCESS_TIME_CAPTION = "Délais d’accès (Réglementaire)"
        const val MOBILITY_CAPTION = "Accessibilité et Handicap (Qualiopi)"
        const val TESTIMONY_CAPTION = "Témoignage Evaluation de la formation (Qualiopi)"
        const val TESTIMONY_CUSTOMER_CAPTION = "Témoignage apprenant/commanditaire"

        const val THEME_EXPECTATIONS =
            """Thème : on dissocie le thème qui est au global dans laquelle s'insère la formation, le thème n’est pas la formation mais le sujet général dans lequel il s'insère."""

        const val TITLE_EXPECTATIONS =
            """Titre : La formulation du titre, elle reprends soit le titre d’un métier ou d’une posture professionnelle, ou bien un titre qui reprends la formulation d’un objectif pédagogique, commençant par un verbe d’action et un contexte professionnelle d’application, si on garde la notion de phrase génériques (les fondamentaux de l'élevage de poulet), on doit donner des éléments de contextes précisant le pourquoi et le comment de la formation pour être suffisamment spécifique"""

        const val PRESENTATION_EXPECTATIONS =
            """Présentation et description : Une introduction dont L’objectif est de donner des tendances sur le mood de la formation, l’orientation et le parti pris, c’est comme une bande annonce avec une introduction sur le secteur d’activité ou le domaine d’application, la description c’est la deuxième partie de l’introduction, où il est question de présenter en détail l’articulation, le fonctionnement de la formation ou les grandes thématiques abordées dans la formation"""

        const val MINDMAP_EXPECTATIONS =
            """Carte thématique : Des mindmaps pour faciliter la compréhension, et avoir une representation graphique de la structure globale de la formation, permettant de visualiser rapidement ce que l’on va faire."""

        const val PUBLIC_PROSPECT_EXPECTATIONS = """Public (Qualiopi) : 
        a) Cible privilégié pour les personnes amenés à suivre la formation, on ne donne pas de restriction sur les situations des personnes et sur les commanditaires, on va donner des critères d’expériences, d’objectif professionnelles, parfois d’âges, etc...
        b) On précise le nombre de personne minimum et maximum par session de formation."""

        const val PRE_REQUIS_EXPECTATIONS = """Pré-requis et conditions d’accès à la formation (Qualiopi) : 
        a) Il faut préciser de manière spécifique les conditions minimales à maîtriser ou posséder : diplômes, âges, expériences. 
        b) Le pré-requis ne concerne que la situation de la personne au moment de son entrée en formation, elle n’est pas dépendante de son évolution dans la formation (sur le plan pédagogique)"""

        const val OBJECTIVES_EXPECTATIONS = """Objectifs pédagogiques :
        a) Référentiel emploi et activités : document qui décrit l’ensemble des différentes missions types d’un emploi en particulier
        b) Référentiel formation : document qui présente la transformation d’un ensemble d’activités en compétences professionnelles à développer en formation
        c) Scénario pédagogique globale (SPG) ou Fiche formation/programme : déclinaison du référentiel formation en proposition pédagogique avec une ingénierie pédagogique adapté à un public spécifique
        d) L’objectif pédagogique est une formulation explicite des savoir, savoir-faire et les savoir-être qui sont attendus à l’issue de la formation
            i) On utilisera toujours la Taxonomie de Bloom pour formaliser un objectif pédagogique"""

        const val SKILLS_EXPECTATIONS =
            """Compétence visée ou objectif opérationnel ou objectif de formation : l’ensemble des acquis issus de la formation qui sont mobilisables en une situation professionnelle contextualisée
            i) On reprend une formulation plus globale qu’un objectif pédagogique d’une sous-compétence, soit la formulation d’un bloc de compétences ou domaines d’activités
            ii) Il vise l’insertion des objectifs pédagogiques avec une application professionnelle, parfois on va juste reprendre les termes du commanditaire, la Taxonomie de Bloom peut-être utilisé, mais elle n’a pas de caractère obligatoire
        Ex 1 : “Analyser la situation financière d’une entreprise” est un objectif pédagogique mais qui est tellement large qu’il faut plusieurs savoir, savoir-faire et savoir-être pour mener à bien cette action sous forme de compétence. Cet objectif pédagogique pourrait être un bloc de compétences. On va la décomposer en plusieurs autres sous-objectif pédagogiques pour être plus précis dans les savoirs nécessaires, par exemple “Identifier les éléments clefs dans un bilan et un résultat comptable”, “Lister l’ensemble des critères de la Norme X pour vérifier la bonne tenu des comptes”, (ces exemples de compétences ne sont pas véritables, il s’agit d’un exemple de décomposition).
        Ex 2 : Concevoir une stratégie marketing adapté à un public cible (Bloc de compétences) : 
    1. Définir le produit/service du public cible (objectif pédagogique et compétences à part entière)
    2. Concevoir des personas (clients types) du public cible
    3. Réaliser une enquête des besoins du public cible sur le produit/service
    4. Illustrer les exemples d’usages par des vidéos et des photos sur un format publicitaire
    5. Diffuser les publicités sur les différents canaux de communication
    6. Suivre la performance des publicités sur les canaux de communication en adaptant en fonction des réactions sur les réseaux sociaux"""

        const val TIMING_EXPECTATIONS = """Durée (Temporisation) (Qualiopi) : 	
        a) La durée, c’est le temps total de formation en journée (rappel : une journée = 7h) et en heure (obligatoire)
        b) La temporisation, il n’est pas nécessaire de marquer Temporisation dans le titre de votre partie, il est plutôt important dans la durée de préciser les répartitions en volumes horaires si il y a des temps dissociés, des stages, de l’alternance, etc (mentionné parfois)"""

        const val MEANS_EXPECTATIONS = """Moyen d’accompagnement et Suivi pédagogique : 
        a) Nommer les acteurs accompagnant l’apprenant durant la formation : responsable pédagogique, référent handicap, formateurs, coordinateur pédagogique, technicien de production éventuel, etc en précisant les contacts professionnels
        b) Donner les éléments de suivis : réunion ponctuelle, points de rdv collectif, les temps d’échanges spécifiques, système d’email ou de ticket de suivi, hotline téléphonique, émargements, plateforme intranet (pas elearning mais administrative), etc
        c) Suivi pédagogique -> Organisation formatif : dans la formation, comment est organisé la répartition des différents temps, ce n’est pas seulement les interlocuteurs ou les temps d’échanges formelles, mais c’est l’organisation durant le temps de formation
            i) Ex : suivi par le formateur sur les temps de cours, disponibilité pour des échanges sur RDV à propos des cours ou des projets, existence de coach ou de tuteur et temps dédié à ce suivi"""

        const val PROGRAM_EXPECTATIONS =
            """Programme pédagogique (Modalités pédagogiques): du contenu et du séquencement
        a) Modalités pédagogiques/Motif pédagogique : Modalité = quel action / Motif = quel rythme, un rythme dans le découpage théorique, pratique et évaluatif qui se répète dans la formation et dans le temps en s’adaptant aux différents thèmes, on présente son style pédagogique en décrivant un motif pédagogique (pas obligatoire). On précise ce qui est en présentiel, et distanciel, avec une plateforme e-learning, avec des machines ou des outils en présentiel (de manière synthétique).
        b) Pour les modules plus ou moins courts (plus ou moins 10j mais ce n’est pas un absolu), Il reprend pour chaque module/séance, la répartition des modalités dans l’ordre d’apparition et précise sa nature et sa durée.
        ```asciidoc
        |===
        |Thématique |Sujet |Modalité |Durée
        |Management
        |Découverte de la notion de management agile
        |Présentation avec un support
        |60 min
        |Management
        |Les avantages/inconvénients de l’agilité
        |Débat collectif avec rédaction d’un rapport individuel
        |90 min
        |Management
        |Temps d’évaluations
        |QCM de 40 questions
        |30 min
        |===```
        (La forme du tableau est conseillé mais pas obligatoire)

        c) Pour les formations longues, plusieurs mois, le programme est découpé dans le temps, compétences par compétences est une excellente idée en précisant le type de contenu au fur et à mesure, il est aussi possible de faire le programme thème par thème au delà du découpage par compétence, on peut donner un niveau de détail qui précise les grandes lignes sans avoir forcément le découpage par séquence et par séance
        d) Pour les blocs à découper en Module, Séquence et Séance : on parle du SPD, qui est à utiliser lorsque le programme est beaucoup trop long (pas obligatoire au niveau du SPG)
            i) Module : réponds à un bloc de compétences (1BC = 1 ou plusieurs modules)
            ii) Séquence : réponds à une sous-compétence ou compétences clefs (1 compétence = plusieurs séquences)
            iii) Séance : est un sous ensemble d’une séquence
        e) Le SPD n’est qu’un document qui détaille l’articulation entre les modules, les séquences et les séances. On précise l’organisation pédagogique et notamment les évaluations (ingénierie pédagogique et ingénierie d’évaluation/certification)"""

        const val EVALUATIONS_EXPECTATIONS = """Modalités d’évaluations :
        a) Le recueil des besoins en début de formation et l’évaluation diagnostique : il est nécessaire pour comprendre les attentes mais aussi les acquis de manière précise, c’est un gros test réalisé en général en amont du recrutement dont on doit tenir
        b) On précise les différentes évaluations, leurs sens et ce qui sera évalué, pour permettre à chacun de comprendre le parcours évaluatif de la formation en précisant ce qui vient ou non dans un objectif de certification"""

        const val CERTIFICATION_EXPECTATIONS = """Modalités de certification et Certification visé : 
        a) Modalités de certification : L’ensemble des critères évaluations et hors évaluations qui sont nécessaire à l’obtention de la certification
        b) Certification visé : On explique le lien avec la certification ou les certifications qui composent la formation, préciser la nature de la certification (RNCP, autres certificateurs, etc), le nom précis et complet, le numéro de délivrance (RNCP), la date de validité/publication (RNCP)  et la date de publication du décret sur l’avis de France Compétences sur le diplôme (RNCP), le propriétaire de la certification."""

        const val PLACE_EXPECTATIONS = """Lieux : 
        a) Si présentiel : adresse et modalités d’accès au lieu
        b) Si elearning : Nom de la plateforme et conditions d’accès informatique"""

        const val PRICE_EXPECTATIONS = """Tarifs : 
        a) Avec et sans TVA ou TVA appliquée
        b) On privilégie les modalités de paiements sur le contrat ou CGV"""

        const val INFRASTRUCTURE_EXPECTATIONS = """Moyens logistique et matériels (Qualiopi) : 
        a) Matériel requis pour la formation, prêté en formation, disponible en formation
        b) Configuration minimal pour travailler en formation : configuration d’un ordinateur, d’un logiciel, etc
        c) Liste des éléments éventuels à acheter en précisant achat collectif possible ou non et obligation ou non de ce matériel."""

        const val PURSUIT_EXPECTATIONS = """Poursuite en formation (Qualiopi) : 
        a) Les évolutions en terme de spécialisation ou de diversification après la formation, lien vers des écoles ou des diplômes."""

        const val ACCESS_TIME_EXPECTATIONS = """Délais d’accès (Réglementaire) : 
        a) Durée de mise à disposition par le CF ou le Formateur de la formation, le dispositif légale impose 14 jours de délai minimum."""

        const val MOBILITY_EXPECTATIONS = """Accessibilité et Handicap (Qualiopi) : 
        a) Préciser qu’un référent handicap est disponible pour écouter les besoins spécifiques et prévoir les adaptations en formation ou l’orientation possible selon la situation de chaque apprenant avec le responsable formation et les formateurs."""

        const val TESTIMONY_EXPECTATIONS = """Témoignage Evaluation de la formation (Qualiopi) : 
        a) A la fin de chaque formation, un questionnaire de satisfaction est envoyé à la fin du dernier jour (à chaud) et 3 à 6 mois plus tard (à froid)
        b) Les statistiques de satisfaction et le taux de réussite des certifications sont à présenter pour chaque formation."""

        const val TESTIMONY_CUSTOMER_EXPECTATIONS = """Témoignage apprenants/commanditaires : 
        a) Listes des clients éventuels et des témoignages comme gage de qualité."""

        val data = listOf(
            mapOf(THEME_CAPTION to THEME_EXPECTATIONS),
            mapOf(TITLE_CAPTION to TITLE_EXPECTATIONS),
            mapOf(PRESENTATION_CAPTION to PRESENTATION_EXPECTATIONS),
            mapOf(MINDMAP_CAPTION to MINDMAP_EXPECTATIONS),
            mapOf(PUBLIC_PROSPECT_CAPTION to PUBLIC_PROSPECT_EXPECTATIONS),
            mapOf(PRESENTATION_CAPTION to PRE_REQUIS_EXPECTATIONS),
            mapOf(OBJECTIVES_CAPTION to OBJECTIVES_EXPECTATIONS),
            mapOf(SKILLS_CAPTION to SKILLS_EXPECTATIONS),
            mapOf(TIMING_CAPTION to TIMING_EXPECTATIONS),
            mapOf(MEANS_CAPTION to MEANS_EXPECTATIONS),
            mapOf(PROGRAM_CAPTION to PROGRAM_EXPECTATIONS),
            mapOf(EVALUATIONS_CAPTION to EVALUATIONS_EXPECTATIONS),
            mapOf(CERTIFICATION_CAPTION to CERTIFICATION_EXPECTATIONS),
            mapOf(PLACE_CAPTION to PLACE_EXPECTATIONS),
            mapOf(PRICE_CAPTION to PRICE_EXPECTATIONS),
            mapOf(INFRASTRUCTURE_CAPTION to INFRASTRUCTURE_EXPECTATIONS),
            mapOf(PURSUIT_CAPTION to PURSUIT_EXPECTATIONS),
            mapOf(ACCESS_TIME_CAPTION to ACCESS_TIME_EXPECTATIONS),
            mapOf(MOBILITY_CAPTION to MOBILITY_EXPECTATIONS),
            mapOf(TESTIMONY_CAPTION to TESTIMONY_EXPECTATIONS),
            mapOf(TESTIMONY_CUSTOMER_CAPTION to TESTIMONY_CUSTOMER_EXPECTATIONS),
        )
    }
}