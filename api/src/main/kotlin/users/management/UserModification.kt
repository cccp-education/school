package users.management

import java.time.Instant
import java.util.*

@JvmRecord
data class UserModification(
    val id: UUID,
    val userId: UUID,
    val modifiedBy: UUID,
    val date: Instant,
    val action: UserModification.Action,
    val reason: String,
) {
    enum class Action {
        CREATED,
        UPDATED,
        DELETED,
        ENABLED,
        DISABLED,
        PASSWORD_CHANGED,
        PASSWORD_RESET,
        EMAIL_CHANGED,
        EMAIL_RESET,
    }
}