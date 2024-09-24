package workspace.git

object RepositoryInfo {
    const val ORIGIN = "origin"
    const val CNAME = "CNAME"
    const val REMOTE = "remote"
}

data class RepositoryConfiguration(
    val name: String,
    val repository: String,
    val credentials: RepositoryCredentials,
)

data class RepositoryCredentials(
    val username: String,
    val password: String
)

data class GitPushConfiguration(
    val from: String,
    val to: String,
    val repo: RepositoryConfiguration,
    val branch: String,
    val message: String,
)