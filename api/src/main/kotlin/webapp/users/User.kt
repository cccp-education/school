package webapp.users

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import webapp.core.model.EntityModel
import webapp.core.property.ANONYMOUS_USER
import webapp.core.property.EMPTY_STRING
import webapp.users.UserDao.Constraints.LOGIN_REGEX
import webapp.users.security.Role
import java.util.*
import jakarta.validation.constraints.Email as EmailConstraint

data class User(
    override val id: UUID? = null,

    @field:NotNull
    @field:Pattern(regexp = LOGIN_REGEX)
    @field:Size(min = 1, max = 50)
    val login: String,

    @JsonIgnore
    @field:NotNull
    @field:Size(min = 60, max = 60)
    val password: String = EMPTY_STRING,

    @field:EmailConstraint
    @field:Size(min = 5, max = 254)
    val email: String = EMPTY_STRING,

    @JsonIgnore
    val roles: MutableSet<Role> = mutableSetOf(Role(ANONYMOUS_USER)),

    @field:Size(min = 2, max = 10)
    val langKey: String = EMPTY_STRING,

    @JsonIgnore
    val version: Long = -1,
) : EntityModel<UUID>()