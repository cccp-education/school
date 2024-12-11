package users.signup

import jakarta.validation.constraints.Size
import users.security.SecurityUtils.generateActivationKey
import java.time.Instant
import java.time.Instant.now
import java.util.*

@JvmRecord
data class UserActivation(
    val id: UUID,
    @field:Size(max = 20)
    val activationKey: String = generateActivationKey,
    val createdDate: Instant = now(),
    val activationDate: Instant? = null,
)