package school.frontend

import com.github.gradle.node.NodeExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import school.workspace.Office
import school.workspace.WorkspaceManager.DNS_CNAME
import school.workspace.WorkspaceManager.bakeDestDirPath
import school.workspace.WorkspaceManager.isCnameExists
import school.workspace.WorkspaceManager.localConf
import school.workspace.WorkspaceManager.pushSiteToGhPages
import school.workspace.WorkspaceManager.workspaceEither
import school.workspace.WorkspaceUtils.sep
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8

@Suppress(
    "MemberVisibilityCanBePrivate",
    "DEPRECATION"
)
object SchoolOpsManager {
    const val MOODLE_DOCKER_WWWROOT = "/path/to/moodle/code"
    const val MOODLE_DOCKER_DB = "pgsql"

    val String.fixEncodage: String get() = UTF_8.decode(UTF_8.encode(this)).toString()

    fun Project.createSchoolCnameFile(workspace: Office) {
        when {
            isSchoolFrontendCnameExists(workspace) ->
                workspace.schoolFrontendCnamePath
                    ?.let(::File)
                    ?.run {
                        when {
                            exists() && isDirectory -> deleteRecursively()

                            exists() -> delete()
                        }
                        createNewFile()
                        workspace.schoolFrontendCname?.let { appendText(it, UTF_8) }
                    }
        }
    }

    fun schoolFrontendPath(workspace: Office): String =
        workspace["workspace"]!!["portfolio"]!!["projects"]!!["school"]!!["builds"]!!["frontend"]!!["path"]!!.toString()


    fun NodeExtension.schoolFrontendDir(
        workspace: Office
    ): Unit = workspace
        .let(SchoolOpsManager::schoolFrontendPath)
        .let(::File)
        .let(nodeProjectDir::set)

    fun Project.nodeDirSchoolFrontend(node: NodeExtension) = workspaceEither.fold(
        ifLeft = { "Error: $it".let(::println) }
    ) { node.schoolFrontendDir(it) }

    fun Project.processSchoolFrontendCnameFile() = workspaceEither.fold(
        ifLeft = { "Error: $it".let(::println) }
    ) { createSchoolCnameFile(it) }


    fun Project.pushSchoolFrontendPages() = pushSiteToGhPages(
        schoolFrontendBakeDestDirPath(),
        schoolFrontendPathTo()
    )

    fun Project.loadSchoolFrontend() = extensions
        .getByType<NodeExtension>()
        .let { project.nodeDirSchoolFrontend(it) }

    fun Project.schoolFrontendBakeDestDirPath(): () -> String =
        { "${buildDir.absolutePath}$sep$bakeDestDirPath" }

    fun Project.schoolFrontendPathTo(): () -> String =
        { "${buildDir.absolutePath}$sep${localConf.pushPage.to}" }

    fun Project.isSchoolFrontendCnameExists(
        workspace: Office
    ): Boolean = workspace["school"]
        ?.get("portfolio")
        ?.get("projects")
        ?.get("school")
        ?.get("builds")
        ?.get("frontend")
        ?.get("repository")
        ?.let { it as? Map<*, *> }
        ?.isCnameExists
        ?: false

    val Office.schoolFrontendCnamePath: String?
        get() {
            val path =
                this["school"]
                    ?.get("portfolio")
                    ?.get("projects")
                    ?.get("school")
                    ?.get("builds")?.get("frontend")
                    ?.get("path") as String?
            val from =
                this["school"]?.get("portfolio")?.get("projects")?.get("school")?.get("builds")?.get("frontend")
                    ?.get("from") as String?
            val cname =
                this["school"]?.get("portfolio")?.get("projects")?.get("school")?.get("builds")?.get("frontend")
                    ?.get("cname") as String?
            return when {
                cname.isNullOrBlank() -> null
                else -> "$path$sep$from$sep$DNS_CNAME"
            }
        }

    @Suppress("unused", "RedundantNullableReturnType")
    val Office.schoolFrontendFromPath: String?
        get() = ""

    val Office.schoolFrontendFrom: String?
        get() = this["school"]
            ?.get("portfolio")
            ?.get("projects")
            ?.get("school")
            ?.get("builds")
            ?.get("frontend")
            ?.get("from")
            ?.let { it as String? }

    val Office.schoolFrontendTo: String?
        get() = this["school"]
            ?.get("portfolio")
            ?.get("projects")
            ?.get("school")
            ?.get("builds")
            ?.get("frontend")
            ?.get("to")
            ?.let { it as String? }

    val Office.schoolFrontendCname: String?
        get() = with(
            this["school"]
                ?.get("portfolio")
                ?.get("projects")
                ?.get("school")
                ?.get("builds")
                ?.get("frontend")
                ?.get("cname") as String?
        ) {
            when {
                isNullOrBlank() -> null
                else -> this
            }
        }
}