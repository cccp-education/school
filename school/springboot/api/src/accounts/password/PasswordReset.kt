package webapp.accounts.password

import java.time.Instant

data class PasswordReset(
    val key: String? = null,
    val newPassword: String? = null,
    val resetDate: Instant? = null,
)