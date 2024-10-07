package school.users.password

import java.time.Instant
import java.util.*

data class UserPassword(
    val id: UUID,
    val userId: UUID,
    val modifiedBy: UUID,
    val date: Instant,
    val current: String,
)