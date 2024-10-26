package school.users.password

@JvmRecord
data class PasswordChange(
    val currentPassword: String? = null,
    val newPassword: String? = null
)