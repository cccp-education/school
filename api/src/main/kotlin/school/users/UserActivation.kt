package school.users

import jakarta.validation.constraints.Size
import school.users.dao.UserActivationDao
import school.users.security.SecurityUtils
import java.time.Instant
import java.util.*

@JvmRecord
data class UserActivation(
    val id: UUID,
    @field:Size(max = 20)
    val activationKey: String = SecurityUtils.generateActivationKey,
    val createdDate: Instant = Instant.now(),
    val activationDate: Instant? = null,
) {
    companion object {
        @JvmStatic
        fun main(args: Array<String>): Unit = println(UserActivationDao.Relations.SQL_SCRIPT)
    }

}