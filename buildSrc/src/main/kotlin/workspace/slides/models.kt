package workspace.slides

import workspace.GitPushConfiguration

@Suppress("unused")
data class SlidesConfiguration(
    val srcPath: String,
    val pushPage: GitPushConfiguration,
)