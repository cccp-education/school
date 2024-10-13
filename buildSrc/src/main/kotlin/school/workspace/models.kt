package school.workspace

/**
- Ajouter une paire clé/map au bout du chemin de clé dans un list
- Supprimer une paire clé/map au bout du chemin de clé dans un list
- Ajouter une paire clé/valeur sur un chemin de clé dans une list de string
- Trouver une valeur par chemin de clé
- Mettre à jour une valeur par chemin de clé
 */

//Deskboard-Bibliotheque-Tiroir-Thematique-Dossier
//Office-
typealias Office = MutableMap<String, MutableMap<String, MutableMap<String, MutableMap<String, MutableMap<String, MutableMap<String, MutableMap<String, Any>>>>?>?>>
typealias OfficeEntry = Pair<List<String>, Any>

//data class SchoolOffice(
//    val bibliotheque: Bibliotheque? = null,
//    val workspace: Workspace,
//    val humanResources: HumanResources? = null
//) {
//    data class Bibliotheque(
//        val courses: MutableMap<String, Course>?,
//        val catalogue: MutableMap<String, Training>?,
//        val projectDocs: MutableMap<String, ProjectDocumentation>,
//    ) {
//        data class Course(val name: String)
//        data class Training(val name: String)
//        data class ProjectDocumentation(val name: String)
//    }
//
//    data class Workspace(val portfolio: MutableMap<String, Project>) {
//        data class Project(
//            val name: String,
//            val cred: String,
//            val builds: MutableMap<String, ProjectBuild>
//        ) {
//            data class ProjectBuild(val name: String)
//        }
//    }
//
//    data class HumanResources(val cv: String)
//}

