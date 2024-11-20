package users.password

import java.time.Instant
import java.util.*

@JvmRecord
data class UserPassword(
    val id: UUID,
    val userId: UUID,
    val modifiedBy: UUID,
    val date: Instant,
    val current: String,
)