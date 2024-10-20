package school.slides

import school.git.GitPushConfiguration

@JvmRecord
data class SlidesConfiguration(
    val srcPath: String,
    val pushPage: GitPushConfiguration,
)