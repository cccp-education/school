package school.users

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import school.users.dao.UserDao

@JvmRecord
data class Signup(
    @field:NotNull
    @field:Pattern(regexp = UserDao.Constraints.LOGIN_REGEX)
    @field:Size(min = 1, max = 50)
    val login: String,
    @field:NotNull
    @Size(
        min = UserDao.Constraints.PASSWORD_MIN,
        max = UserDao.Constraints.PASSWORD_MAX
    )
    val password: String,
    val repassword: String,
    @field:Email
    @field:Size(min = 5, max = 254)
    val email: String,
) {
    companion object {
        val SIGNUPCLASS = Signup::class.java

        @JvmStatic
        val objectName: String = SIGNUPCLASS.simpleName.run {
            replaceFirst(
                first(),
                first().lowercaseChar()
            )
        }
    }

}