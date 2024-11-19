@file:Suppress(
    "RemoveRedundantQualifierName",
    "MemberVisibilityCanBePrivate",
    "SqlNoDataSourceInspection", "SqlDialectInspection", "SqlSourceToSinkFlow"
)

package school.users

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import school.base.model.EntityModel
import school.base.utils.Constants.EMPTY_STRING
import school.users.dao.UserDao
import school.users.dao.UserDao.Constraints.LOGIN_REGEX
import school.users.security.UserRole.Role
import java.util.*
import java.util.Locale.ENGLISH
import jakarta.validation.constraints.Email as EmailConstraint

data class User(
    override val id: UUID? = null,

    @field:NotNull
    @field:Pattern(regexp = LOGIN_REGEX)
    @field:Size(min = 1, max = 50)
    val login: String = EMPTY_STRING,

    @JsonIgnore
    @field:NotNull
    @field:Size(min = 60, max = 60)
    val password: String = EMPTY_STRING,

    @field:EmailConstraint
    @field:Size(min = 5, max = 254)
    val email: String = EMPTY_STRING,

    @JsonIgnore
    val roles: Set<Role> = emptySet(),

    @field:Size(min = 2, max = 10)
    val langKey: String = ENGLISH.language,

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

        @JvmStatic
        fun main(args: Array<String>): Unit = println(UserDao.Relations.CREATE_TABLES)
    }


}
//            suspend fun Pair<User, ApplicationContext>.save(): Either<Throwable, Long> = try {
//                second
//                    .getBean<R2dbcEntityTemplate>()
//                    .databaseClient
//                    .sql(Relations.INSERT)
//                    .bind("login", first.login)
//                    .bind("email", first.email)
//                    .bind("password", first.password)
////                .bind("firstName", first.firstName)
////                .bind("lastName", first.lastName)
//                    .bind("langKey", first.langKey)
////                .bind("imageUrl", first.imageUrl)
//                    .bind("enabled", first.enabled)
////                .bind("activationKey", first.activationKey)
////                .bind("resetKey", first.resetKey)
////                .bind("resetDate", first.resetDate)
////                .bind("createdBy", first.createdBy)
////                .bind("createdDate", first.createdDate)
////                .bind("lastModifiedBy", first.lastModifiedBy)
////                .bind("lastModifiedDate", first.lastModifiedDate)
//                    .bind("version", first.version)
//                    .fetch()
//                    .awaitRowsUpdated()
//                    .right()
//            } catch (e: Throwable) {
//                e.left()
//            }