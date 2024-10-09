@file:Suppress(
    "RemoveRedundantQualifierName",
    "MemberVisibilityCanBePrivate",
    "SqlNoDataSourceInspection"
)

package school.users

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.core.*
import school.base.model.EntityModel
import school.base.property.ANONYMOUS_USER
import school.base.property.EMPTY_STRING
import school.base.property.ROLE_USER
import school.base.utils.AppUtils.cleanField
import school.users.User.UserDao.Attributes.EMAIL_ATTR
import school.users.User.UserDao.Attributes.ID_ATTR
import school.users.User.UserDao.Attributes.LANG_KEY_ATTR
import school.users.User.UserDao.Attributes.LOGIN_ATTR
import school.users.User.UserDao.Attributes.PASSWORD_ATTR
import school.users.User.UserDao.Attributes.VERSION_ATTR
import school.users.User.UserDao.Constraints.LOGIN_REGEX
import school.users.User.UserDao.Fields.EMAIL_FIELD
import school.users.User.UserDao.Fields.ID_FIELD
import school.users.User.UserDao.Fields.LANG_KEY_FIELD
import school.users.User.UserDao.Fields.LOGIN_FIELD
import school.users.User.UserDao.Fields.PASSWORD_FIELD
import school.users.User.UserDao.Fields.VERSION_FIELD
import school.users.User.UserDao.Relations.INSERT
import school.users.security.Role
import school.users.security.UserRole
import school.users.security.UserRole.UserRoleDao.Dao.signup
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
) : EntityModel<UUID>() {

    /** Account REST API URIs */
    object UserRestApiRoutes {
        const val API_AUTHORITY = "/api/authorities"
        const val API_USERS = "/api/users"
        const val API_SIGNUP = "/signup"
        const val API_SIGNUP_PATH = "$API_USERS$API_SIGNUP"
        const val API_ACTIVATE = "/activate"
        const val API_ACTIVATE_PATH = "$API_USERS$API_ACTIVATE?key="
        const val API_ACTIVATE_PARAM = "{activationKey}"
        const val API_ACTIVATE_KEY = "key"
        const val API_RESET_INIT = "/reset-password/init"
        const val API_RESET_FINISH = "/reset-password/finish"
        const val API_CHANGE = "/change-password"
        const val API_CHANGE_PATH = "$API_USERS$API_CHANGE"
    }

    object UserDao {
        @JvmStatic
        fun main(args: Array<String>) = println(UserDao.Relations.CREATE_TABLES)

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

            //TODO: signup, findByEmailOrLogin,
            val FIND_USER_BY_LOGIN =
                "SELECT u.${UserDao.Fields.ID_FIELD} FROM ${UserDao.Relations.TABLE_NAME} AS u WHERE u.${UserDao.Fields.LOGIN_FIELD}= LOWER(:${UserDao.Attributes.LOGIN_ATTR})"

            @JvmStatic
            val CREATE_TABLES: String
                get() = setOf(
                    UserDao.Relations.SQL_SCRIPT,
                    Role.RoleDao.Relations.SQL_SCRIPT,
                    UserRole.UserRoleDao.Relations.SQL_SCRIPT
                ).joinToString("")
                    .trimMargin()
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


            suspend fun Pair<User, ApplicationContext>.save(): Either<Throwable, UUID> = try {
                second.getBean<R2dbcEntityTemplate>()
                    .databaseClient
                    .sql(INSERT)
                    .bind(LOGIN_ATTR, first.login)
                    .bind(EMAIL_ATTR, first.email)
                    .bind(PASSWORD_ATTR, first.password)
                    .bind(LANG_KEY_ATTR, first.langKey)
                    .bind(VERSION_ATTR, first.version)
                    .fetch()
                    .awaitSingleOrNull()
                    ?.get(ID_ATTR.uppercase())
                    .toString()
                    .run(UUID::fromString)
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

            suspend fun Pair<User, ApplicationContext>.signup(): Either<Throwable, UUID> = try {
                (first to second).save()
                    .mapLeft { return Exception("Unable to save User").left() }
                    .map {
                        (UserRole(userId = it, role = ROLE_USER) to second).signup()
                        return it.right()
                    }
            } catch (e: Throwable) {
//            EmptyResultDataAccessException::class.left()
                e.left()
            }
        }
    }
}
