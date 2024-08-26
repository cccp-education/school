package webapp.accounts.models

/*=================================================================================*/
data class PasswordChange(
    val currentPassword: String? = null,
    val newPassword: String? = null
)