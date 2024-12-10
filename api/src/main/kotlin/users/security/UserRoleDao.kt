package users.security

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import kotlinx.coroutines.reactive.collect
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.await
import org.springframework.r2dbc.core.awaitSingle
import users.UserDao
import users.security.UserRoleDao.Fields.ROLE_FIELD
import users.security.UserRoleDao.Fields.USER_ID_FIELD
import java.util.*

@Suppress("unused")
object UserRoleDao {

    object Fields {
        const val ID_FIELD = "id"
        const val USER_ID_FIELD = "user_id"
        const val ROLE_FIELD = RoleDao.Fields.ID_FIELD
    }

    object Attributes {
        const val ID_ATTR = Fields.ID_FIELD
        const val USER_ID_ATTR = "userId"
        const val ROLE_ATTR = ROLE_FIELD
    }

    object Relations {
        const val TABLE_NAME = "user_authority"
        const val SQL_SCRIPT = """
    CREATE SEQUENCE IF NOT EXISTS user_authority_seq
    START WITH 1 INCREMENT BY 1;
    CREATE TABLE IF NOT EXISTS $TABLE_NAME(
        ${Fields.ID_FIELD}  BIGINT DEFAULT nextval('user_authority_seq') PRIMARY KEY,
        $USER_ID_FIELD      UUID,
        "$ROLE_FIELD"       VARCHAR,
        FOREIGN KEY ($USER_ID_FIELD) REFERENCES "${UserDao.Relations.TABLE_NAME}" (${UserDao.Fields.ID_FIELD})
            ON DELETE CASCADE
            ON UPDATE CASCADE,
        FOREIGN KEY ("$ROLE_FIELD") REFERENCES ${RoleDao.Relations.TABLE_NAME} ("${RoleDao.Fields.ID_FIELD}")
            ON DELETE CASCADE
            ON UPDATE CASCADE
    );

    CREATE UNIQUE INDEX IF NOT EXISTS uniq_idx_user_authority
    ON $TABLE_NAME ("$ROLE_FIELD", $USER_ID_FIELD);
"""
       const val INSERT = """
            INSERT INTO $TABLE_NAME ($USER_ID_FIELD,"${RoleDao.Fields.ID_FIELD}")
            VALUES (:${Attributes.USER_ID_ATTR}, :${Attributes.ROLE_ATTR})
            """
    }

    object Dao {
        suspend fun Pair<UserRole, ApplicationContext>.signup(): Either<Throwable, Long> = try {
            second.getBean<R2dbcEntityTemplate>()
                .databaseClient.sql(Relations.INSERT)
                .bind(Attributes.USER_ID_ATTR, first.userId)
                .bind(Attributes.ROLE_ATTR, first.role)
                .fetch()
                .one()
                .collect { it[Fields.ID_FIELD] }
                .toString()
                .toLong()
                .right()
        } catch (e: Exception) {
            e.left()
        }

        suspend fun ApplicationContext.countUserAuthority(): Int =
            "SELECT COUNT(*) FROM user_authority;"
                .let(getBean<DatabaseClient>()::sql)
                .fetch()
                .awaitSingle()
                .values
                .first()
                .toString()
                .toInt()

        suspend fun ApplicationContext.deleteAllUserAuthorities(): Unit =
            "DELETE FROM user_authority;"
                .let(getBean<DatabaseClient>()::sql)
                .await()

        suspend fun ApplicationContext.deleteAllUserAuthorityByUserId(id: UUID) =
            "delete from user_authority as ua where ua.user_id = :userId;"
                .let(getBean<DatabaseClient>()::sql)
                .bind("userId", id)
                .await()

        suspend fun ApplicationContext.deleteAuthorityByRole(role: String): Unit =
            """delete from authority as a where upper(a."${RoleDao.Fields.ID_FIELD}") = upper(:role)"""
                .let(getBean<DatabaseClient>()::sql)
                .bind("role", role)
                .await()

        suspend fun ApplicationContext.deleteUserByIdWithAuthorities_(id: UUID) =
            getBean<DatabaseClient>().run {
                "delete from user_authority where user_id = :userId"
                    .let(::sql)
                    .bind("userId", id)
                    .await()
                """delete from "user" as u where u.${UserDao.Fields.ID_FIELD} = :userId"""
                    .let(::sql)
                    .await()
            }

        val ApplicationContext.queryDeleteAllUserAuthorityByUserLogin
            get() = """delete from user_authority 
                    |where user_id = (
                    |select u.id from "user" as u where u."login" = :login
                    |);""".trimMargin()

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