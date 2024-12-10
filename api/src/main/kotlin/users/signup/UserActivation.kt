package users.signup

import jakarta.validation.constraints.Size
import users.security.SecurityUtils
import java.time.Instant
import java.util.*

@JvmRecord
data class UserActivation(
    val id: UUID,
    @field:Size(max = 20)
    val activationKey: String = SecurityUtils.generateActivationKey,
    val createdDate: Instant = Instant.now(),
    val activationDate: Instant? = null,
)