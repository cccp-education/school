package school.users.security

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

/*=================================================================================*/
@JvmRecord
data class Login(
    @field:NotNull
    val username:
    @Size(min = 1, max = 50)
    String? = null,
    @field:NotNull
    @field:Size(min = 4, max = 100)
    val password:
    String? = null,
    val rememberMe: Boolean? = null
)