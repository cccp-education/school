@file:Suppress("MemberVisibilityCanBePrivate")

package school.users.security

import jakarta.validation.constraints.NotNull
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.await
import org.springframework.r2dbc.core.awaitSingle
import school.users.UserDao
import school.users.security.Role.RoleDao
import school.users.security.UserRole.UserRoleDao.Fields.ID_FIELD
import school.users.security.UserRole.UserRoleDao.Fields.ROLE_FIELD
import school.users.security.UserRole.UserRoleDao.Fields.USER_ID_FIELD
import java.util.*

@Suppress("unused")
data class UserRole(
    val id: Long,
    @field:NotNull
    val userId: UUID,
    @field:NotNull
    val role: String
) {
    @Suppress("unused")
    object UserRoleDao {
        object Fields {
            const val ID_FIELD = "`id`"
            const val USER_ID_FIELD = "`user_id`"
            const val ROLE_FIELD = RoleDao.Fields.ID_FIELD
        }

        object Relations {
            const val TABLE_NAME = "`user_authority`"
            const val SQL_SCRIPT = """
        CREATE TABLE IF NOT EXISTS $TABLE_NAME(
            $ID_FIELD         IDENTITY NOT NULL PRIMARY KEY,
            $USER_ID_FIELD    UUID,
            $ROLE_FIELD       VARCHAR,
            FOREIGN KEY ($USER_ID_FIELD) REFERENCES ${UserDao.Relations.TABLE_NAME} (${UserDao.Fields.ID_FIELD})
                ON DELETE CASCADE
                ON UPDATE CASCADE,
            FOREIGN KEY ($ROLE_FIELD) REFERENCES ${RoleDao.Relations.TABLE_NAME} (${RoleDao.Fields.ID_FIELD})
                ON DELETE CASCADE
                ON UPDATE CASCADE
        );

        CREATE UNIQUE INDEX IF NOT EXISTS `uniq_idx_user_authority`
        ON $TABLE_NAME ($ROLE_FIELD, $USER_ID_FIELD);
"""
            const val INSERT = ""
//                """
//            insert into $TABLE_NAME (
//            ${Fields.LOGIN_FIELD}, ${Fields.EMAIL_FIELD}, ${Fields.PASSWORD_FIELD},
//            ${Fields.FIRST_NAME_FIELD}, ${Fields.LAST_NAME_FIELD}, ${Fields.LANG_KEY_FIELD}, ${Fields.IMAGE_URL_FIELD},
//            ${Fields.ENABLED_FIELD}, ${Fields.ACTIVATION_KEY_FIELD}, ${Fields.RESET_KEY_FIELD}, ${Fields.RESET_DATE_FIELD},
//            ${Fields.CREATED_BY_FIELD}, ${Fields.CREATED_DATE_FIELD}, ${Fields.LAST_MODIFIED_BY_FIELD}, ${Fields.LAST_MODIFIED_DATE_FIELD}, ${Fields.VERSION_FIELD})
//            values (:login, :email, :password, :firstName, :lastName,
//            :langKey, :imageUrl, :enabled, :activationKey, :resetKey, :resetDate,
//            :createdBy, :createdDate, :lastModifiedBy, :lastModifiedDate, :version)
//            """
        }

        object Dao {
            suspend fun ApplicationContext.countUserAuthority(): Int =
                "select count(*) from `user_authority`"
                    .let(getBean<DatabaseClient>()::sql)
                    .fetch()
                    .awaitSingle()
                    .values
                    .first()
                    .toString()
                    .toInt()


            suspend fun ApplicationContext.deleteAllUserAuthorities(): Unit =
                "DELETE FROM `user_authority`"
                    .let(getBean<DatabaseClient>()::sql)
                    .await()

            suspend fun ApplicationContext.deleteAllUserAuthorityByUserId(
                id: UUID
            ) = "delete from user_authority where user_id = :userId"
                .let(getBean<DatabaseClient>()::sql)
                .bind("userId", id)
                .await()

            suspend fun ApplicationContext.deleteAuthorityByRole(
                role: String
            ): Unit = "delete from `authority` a where lower(a.role) = lower(:role)"
                .let(getBean<DatabaseClient>()::sql)
                .bind("role", role)
                .await()

            suspend fun ApplicationContext.deleteUserByIdWithAuthorities_(id: UUID) = getBean<DatabaseClient>().run {
                "delete from user_authority where user_id = :userId"
                    .let(::sql)
                    .bind("userId", id)
                    .await()
                "delete from `user` where user_id = :userId"
                    .let(::sql)
                    .await()
            }

            val ApplicationContext.queryDeleteAllUserAuthorityByUserLogin
                get() =
                    "delete from user_authority where user_id = (select u.id from `user` u where u.login=:login)"

            suspend fun ApplicationContext.deleteAllUserAuthorityByUserLogin(
                login: String
            ): Unit = getBean<DatabaseClient>()
                .sql(queryDeleteAllUserAuthorityByUserLogin)
                .bind("login", login)
                .await()

//            val Pair<User, ApplicationContext>.toJson: String
//                get() = second.getBean<ObjectMapper>().writeValueAsString(first)
//
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
        }
    }
}
