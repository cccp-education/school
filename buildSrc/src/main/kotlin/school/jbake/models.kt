package school.jbake

import school.git.GitPushConfiguration

//object SiteInfo{}
@JvmRecord
data class SiteConfiguration(
    val bake: BakeConfiguration,
    val pushPage: GitPushConfiguration,
    val pushSource: GitPushConfiguration? = null,
    val pushTemplate: GitPushConfiguration? = null,
)

@JvmRecord
data class BakeConfiguration(
    val srcPath: String,
    val destDirPath: String,
    val cname: String?,
)

@JvmRecord
data class SlideConfiguration(
    val bake: BakeConfiguration,
    val pushPage: GitPushConfiguration,
    val pushSource: GitPushConfiguration? = null,
    val pushTemplate: GitPushConfiguration? = null,
)
