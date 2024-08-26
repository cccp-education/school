package webapp.users.management

import java.time.Instant
import java.util.*

data class UserEnabled(
    val id: UUID,
    val userId: UUID,
    val modifiedBy: UUID,
    val date: Instant,
)