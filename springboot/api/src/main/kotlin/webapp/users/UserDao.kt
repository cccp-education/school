@file:Suppress(
    "RemoveRedundantQualifierName",
    "MemberVisibilityCanBePrivate",
    "SqlNoDataSourceInspection"
)

package webapp.users

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import kotlinx.coroutines.reactive.collect
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.core.*
import webapp.core.model.EntityModel
import webapp.core.utils.AppUtils.cleanField
import webapp.users.UserDao.Attributes.EMAIL_ATTR
import webapp.users.UserDao.Attributes.LANG_KEY_ATTR
import webapp.users.UserDao.Attributes.LOGIN_ATTR
import webapp.users.UserDao.Attributes.PASSWORD_ATTR
import webapp.users.UserDao.Attributes.VERSION_ATTR
import webapp.users.UserDao.Dao.save
import webapp.users.UserDao.Fields.EMAIL_FIELD
import webapp.users.UserDao.Fields.ID_FIELD
import webapp.users.UserDao.Fields.LANG_KEY_FIELD
import webapp.users.UserDao.Fields.LOGIN_FIELD
import webapp.users.UserDao.Fields.PASSWORD_FIELD
import webapp.users.UserDao.Fields.VERSION_FIELD
import webapp.users.UserDao.Relations.INSERT
import webapp.users.security.Role
import webapp.users.security.UserRole
import java.util.*

object UserDao {
    @JvmStatic
    fun main(args: Array<String>) = println(UserDao.Relations.sqlScript)

    object Constraints {
        // Regex for acceptable logins
        const val LOGIN_REGEX =
            "^(?>[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*)|(?>[_.@A-Za-z0-9-]+)$"
        const val PASSWORD_MIN: Int = 4
        const val PASSWORD_MAX: Int = 16
        const val IMAGE_URL_DEFAULT = "https://placehold.it/50x50"
        const val PHONE_REGEX = "^(\\+|00)?[1-9]\\d{0,49}\$"
    }

    object Members {
        const val PASSWORD_MEMBER = "password"
        const val ROLES_MEMBER = "roles"
    }

    object Fields {
        const val ID_FIELD = "`id`"
        const val LOGIN_FIELD = "`login`"
        const val PASSWORD_FIELD = "`password`"
        const val EMAIL_FIELD = "`email`"
        const val LANG_KEY_FIELD = "`lang_key`"
        const val VERSION_FIELD = "`version`"
    }

    object Attributes {
        val ID_ATTR = ID_FIELD.cleanField()
        val LOGIN_ATTR = LOGIN_FIELD.cleanField()
        val PASSWORD_ATTR = PASSWORD_FIELD.cleanField()
        val EMAIL_ATTR = EMAIL_FIELD.cleanField()
        const val LANG_KEY_ATTR = "langKey"
        val VERSION_ATTR = VERSION_FIELD.cleanField()
    }

    object Relations {
        const val TABLE_NAME = "`user`"
        const val SQL_SCRIPT = """
        CREATE TABLE IF NOT EXISTS $TABLE_NAME (
            $ID_FIELD                     UUID default random_uuid() PRIMARY KEY,
            $LOGIN_FIELD                  VARCHAR,
            $PASSWORD_FIELD               VARCHAR,
            $EMAIL_FIELD                  VARCHAR,
            $LANG_KEY_FIELD               VARCHAR,
            $VERSION_FIELD                bigint
        );
        CREATE UNIQUE INDEX IF NOT EXISTS `uniq_idx_user_login`
        ON $TABLE_NAME ($LOGIN_FIELD);
        CREATE UNIQUE INDEX IF NOT EXISTS `uniq_idx_user_email`
        ON $TABLE_NAME ($EMAIL_FIELD);
"""

        @Suppress("SqlDialectInspection")
        const val INSERT = """
        insert into $TABLE_NAME (
            $LOGIN_FIELD, $EMAIL_FIELD,
            $PASSWORD_FIELD, $LANG_KEY_FIELD,
            $VERSION_FIELD
        ) values ( :login, :email, :password, :langKey, :version)"""

        @JvmStatic
        val sqlScript: String
            get() = setOf(
                UserDao.Relations.SQL_SCRIPT,
                Role.RoleDao.Relations.SQL_SCRIPT,
                UserRole.UserRoleDao.Relations.SQL_SCRIPT
            ).joinToString("")
                .trimMargin()
        //TODO: signup, findByLogin, findByEmailOrLogin,
    }

    object Dao {

        suspend fun ApplicationContext.countUsers(): Int =
            "SELECT COUNT(*) FROM `user`"
                .let(getBean<DatabaseClient>()::sql)
                .fetch()
                .awaitSingle()
                .values
                .first()
                .toString()
                .toInt()


        //TODO : change the return type in  Either<Throwable, UUID>
        suspend fun Pair<User, ApplicationContext>.save(): Either<Throwable, Long> = try {
            second.getBean<R2dbcEntityTemplate>()
                .databaseClient
                .sql(INSERT)
                .bind(LOGIN_ATTR, first.login)
                .bind(EMAIL_ATTR, first.email)
                .bind(PASSWORD_ATTR, first.password)
                .bind(LANG_KEY_ATTR, first.langKey)
                .bind(VERSION_ATTR, first.version)
                .fetch()
                .awaitRowsUpdated()
                .right()
        } catch (e: Throwable) {
            e.left()
        }


        suspend fun ApplicationContext.deleteAllUsersOnly(): Unit =
            "DELETE FROM `user`"
                .let(getBean<DatabaseClient>()::sql)
                .await()


        suspend inline fun <reified T : EntityModel<*>> ApplicationContext.findOneByEmail(email: String): Either<Throwable, T> =
            when (T::class) {
                User::class -> {
                    try {
                        val user = getBean<DatabaseClient>()
                            .sql("SELECT * FROM `user` u WHERE LOWER(u.email) = LOWER(:email)")
                            .bind("email", email)
                            .fetch()
                            .awaitOne()
                            .let { row ->
                                User(
                                    id = row["id"] as UUID?,
                                    login = row["login"] as String,
                                    password = row["password"] as String,
                                    email = row["email"] as String,
                                    langKey = row["lang_key"] as String,
                                    version = row["version"] as Long
                                )
                            }
                        Either.Right(user as T)
                    } catch (e: Throwable) {
                        Either.Left(e)
                    }
                }

                else -> Either.Left(IllegalArgumentException("Unsupported type: ${T::class.simpleName}"))
            }


        // passer par un est si rowsUpdated == 1,
        // alors repasser par un sql pour retrieve l'id generated par la dataBase
        //                    one()
        //                        .toString()
        //                        .let(::i)
        //                        .let {
        //                        if (it == null) i("It's empty")
        //                        else if (it.isEmpty()) i("It's empty")
        //                        else i(it.toString())
        //                    }
        suspend fun Pair<User, ApplicationContext>.saveWithId(): Either<Throwable, UUID> = try {
            (first to second).save()
            second.getBean<R2dbcEntityTemplate>()
                .databaseClient.sql("SELECT * FROM `user`")
                .fetch()
                .all()
                .collect { it["ID"] }
                .toString()
                .let(UUID::fromString)
                .right()
        } catch (e: Throwable) {
//            EmptyResultDataAccessException::class.left()
            e.left()
        }

    }
}