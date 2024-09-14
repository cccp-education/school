package workspace.slides

import workspace.GitPushConfiguration

data class SlidesConfiguration(
    val srcPath: String,
    val pushPage: GitPushConfiguration,
)