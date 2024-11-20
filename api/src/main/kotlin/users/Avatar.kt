package users

import java.util.*

/**
 * Représente l'account domain model minimaliste pour la view
 */
@JvmRecord
data class Avatar(
    val id: UUID? = null,
    val login: String? = null
)