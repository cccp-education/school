package workspace.slides

import workspace.git.GitPushConfiguration

@Suppress("unused")
data class SlidesConfiguration(
    val srcPath: String,
    val pushPage: GitPushConfiguration,
)