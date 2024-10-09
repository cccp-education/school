package school.users.signup

import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.security.crypto.password.PasswordEncoder
import school.base.property.ANONYMOUS_USER
import school.users.User
import school.users.security.Role
import java.util.*

data class Signup(
    val login: String,
    val password: String,
    val repassword: String,
    val email: String,
)

