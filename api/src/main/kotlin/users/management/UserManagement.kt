package users.management

import java.time.Instant
import java.util.*

@JvmRecord
data class UserManagement(
    val id: UUID,
    val userId: UUID,
    val modifiedBy: UUID,
    val date: Instant,
)