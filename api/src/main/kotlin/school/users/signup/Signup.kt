package school.users.signup

@JvmRecord
data class Signup(
    val login: String,
    val password: String,
    val repassword: String,
    val email: String,
)