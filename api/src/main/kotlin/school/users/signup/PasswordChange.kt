package school.users.signup

/*=================================================================================*/
@JvmRecord
data class PasswordChange(
    val currentPassword: String? = null,
    val newPassword: String? = null
)