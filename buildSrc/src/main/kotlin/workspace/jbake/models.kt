package workspace.jbake

import workspace.git.GitPushConfiguration

data class SiteConfiguration(
    val bake: BakeConfiguration,
    val pushPage: GitPushConfiguration,
    val pushSource: GitPushConfiguration? = null,
    val pushTemplate: GitPushConfiguration? = null,
)
data class BakeConfiguration(
    val srcPath: String,
    val destDirPath: String,
    val cname: String?,
)