package workspace

/**
- Ajouter une paire clé/map au bout du chemin de clé dans un list
- Supprimer une paire clé/map au bout du chemin de clé dans un list
- Ajouter une paire clé/valeur sur un chemin de clé dans une list de string
- Trouver une valeur par chemin de clé
- Mettre à jour une valeur par chemin de clé
 */

//Deskboard-Bibliotheque-Tiroir-Thematique-Dossier
//Bureau-

data class Office(
    val bibliotheque: Bibliotheque? = null,
    val workspace: Workspace,
    val humanResources: HumanResources? = null
) {
    data class Bibliotheque(
        val courses: MutableMap<String, Course>?,
        val catalogue: MutableMap<String, Training>?,
        val projectDocs: MutableMap<String, ProjectDocumentation>,
    ) {
        data class Course(val name: String)
        data class Training(val name: String)
        data class ProjectDocumentation(val name: String)
    }

    data class Workspace(val portfolio: MutableMap<String, Project>) {
        data class Project(
            val name: String,
            val cred: String,
            val builds: MutableMap<String, ProjectBuild>
        ) {
            data class ProjectBuild(val name: String)
        }
    }

    data class HumanResources(val cv: String)
}

typealias Bureau = MutableMap<String, MutableMap<String, MutableMap<String, MutableMap<String, MutableMap<String, MutableMap<String, MutableMap<String, Any>>>>?>?>>
typealias BureauEntry = Pair<List<String>, Any>


data class BakeConfiguration(
    val srcPath: String,
    val destDirPath: String,
    val cname: String?,
)

data class GitPushConfiguration(
    val from: String,
    val to: String,
    val repo: RepositoryConfiguration,
    val branch: String,
    val message: String,
)


data class RepositoryConfiguration(
    val name: String,
    val repository: String,
    val credentials: RepositoryCredentials,
) {
    companion object {
        const val ORIGIN = "origin"
        const val CNAME = "CNAME"
        const val REMOTE = "remote"
    }
}

data class RepositoryCredentials(
    val username: String, val password: String
)

data class SiteConfiguration(
    val bake: BakeConfiguration,
    val pushPage: GitPushConfiguration,
    val pushSource: GitPushConfiguration? = null,
    val pushTemplate: GitPushConfiguration? = null,
)
