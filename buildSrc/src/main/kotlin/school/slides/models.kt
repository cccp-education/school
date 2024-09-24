package school.slides

import school.git.GitPushConfiguration

@Suppress("unused")
data class SlidesConfiguration(
    val srcPath: String,
    val pushPage: GitPushConfiguration,
)