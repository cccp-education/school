package school.forms

import org.gradle.api.Project
import school.workspace.Office
import school.workspace.WorkspaceManager.workspaceEither
import java.io.IOException
import java.util.*

object FormManager {
    const val KRS_ONE_QUOTE = """
        You know, you don't see with your eyes
        You see with your brain
        And the more words your brain has
        The more things you can see
"""

    @get:Throws(IOException::class)
    val Project.formAccessToken: String?
        get() = workspaceEither.fold<String?>(
            ifLeft = {
                "Error: $it".let(::println)
                return null
            }
        ) {
            it.formCred

//            val credential: GoogleCredentials = GoogleCredentials.fromStream(
//                Objects.requireNonNull(FormPlugin::class.java.getResourceAsStream(CRED))
//            ).createScoped(FormsScopes.all())
//            return when {
//                credential.getAccessToken() != null -> credential.accessToken.tokenValue
//
//                else -> credential.refreshAccessToken().tokenValue
//            }
        }

        @Suppress("MemberVisibilityCanBePrivate")
    val Office.formCred: String
        get() = this["workspace"]
            ?.get("portfolio")
            ?.get("projects")
            ?.get("projects")
            ?.get("form")
            ?.get("cred")
            ?.let { it as String? }.toString()

}