package webapp.accounts.models

import java.time.Instant
import java.util.*

data class AccountExtra(
    val password: String,
    val enabled: Boolean = false,
    val activationKey: String?,
    val resetKey: String? = null,
    val resetDate: Instant? = null,
    val createdBy: UUID,
    val lastModifiedBy: UUID,
)



