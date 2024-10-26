package school.users.mail

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import school.base.utils.EMPTY_STRING
import java.time.Instant
import java.util.*

@JvmRecord
data class UserEmail(
    @field:NotNull
    val id: UUID,
    @field:NotNull
    val userId: UUID,
    @field:Email
    @field:Size(min = 5, max = 254)
    val email: String = EMPTY_STRING,
    val date: Instant,
)