package users.security

import jakarta.validation.constraints.NotNull
import java.util.*

@JvmRecord
data class UserRole(
    val id: Long = -1,
    @field:NotNull
    val userId: UUID,
    @field:NotNull
    val role: String
)