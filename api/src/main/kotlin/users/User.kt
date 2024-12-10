package users

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import app.database.EntityModel
import app.utils.Constants
import users.security.Role
import java.util.*

data class User(
    override val id: UUID? = null,

    @field:NotNull
    @field:Pattern(regexp = UserDao.Constraints.LOGIN_REGEX)
    @field:Size(min = 1, max = 50)
    val login: String = Constants.EMPTY_STRING,

    @JsonIgnore
    @field:NotNull
    @field:Size(min = 60, max = 60)
    val password: String = Constants.EMPTY_STRING,

    @field:Email
    @field:Size(min = 5, max = 254)
    val email: String = Constants.EMPTY_STRING,

    @JsonIgnore
    val roles: Set<Role> = emptySet(),

    @field:Size(min = 2, max = 10)
    val langKey: String = Locale.ENGLISH.language,

    @JsonIgnore
    val version: Long = 0,
) : EntityModel<UUID>() {
    companion object {
        val USERCLASS = User::class.java

        @JvmStatic
        val objectName: String = USERCLASS.simpleName.run {
            replaceFirst(
                first(),
                first().lowercaseChar()
            )
        }
    }

}