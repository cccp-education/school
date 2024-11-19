@file:Suppress("SqlDialectInspection", "MemberVisibilityCanBePrivate")

package school.users.dao

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import jakarta.validation.Validator
import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.core.*
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import reactor.core.publisher.Mono
import school.base.model.EntityModel
import school.base.utils.AppUtils.cleanField
import school.base.utils.Constants
import school.users.Signup
import school.users.User
import school.users.dao.UserActivationDao.Fields
import school.users.dao.UserDao.Attributes.EMAIL_ATTR
import school.users.dao.UserDao.Attributes.ID_ATTR
import school.users.dao.UserDao.Attributes.LANG_KEY_ATTR
import school.users.dao.UserDao.Attributes.LOGIN_ATTR
import school.users.dao.UserDao.Attributes.PASSWORD_ATTR
import school.users.dao.UserDao.Attributes.VERSION_ATTR
import school.users.dao.UserDao.Attributes.isEmail
import school.users.dao.UserDao.Attributes.isLogin
import school.users.dao.UserDao.Dao.findOneWithAuths
import school.users.dao.UserDao.Fields.EMAIL_FIELD
import school.users.dao.UserDao.Fields.ID_FIELD
import school.users.dao.UserDao.Fields.LANG_KEY_FIELD
import school.users.dao.UserDao.Fields.LOGIN_FIELD
import school.users.dao.UserDao.Fields.PASSWORD_FIELD
import school.users.dao.UserDao.Fields.VERSION_FIELD
import school.users.dao.UserDao.Relations.DELETE_USER
import school.users.dao.UserDao.Relations.DELETE_USER_BY_ID
import school.users.dao.UserDao.Relations.EMAIL_AVAILABLE_COLUMN
import school.users.dao.UserDao.Relations.FIND_USER_BY_ID
import school.users.dao.UserDao.Relations.FIND_USER_BY_LOGIN_OR_EMAIL
import school.users.dao.UserDao.Relations.LOGIN_AND_EMAIL_AVAILABLE_COLUMN
import school.users.dao.UserDao.Relations.LOGIN_AVAILABLE_COLUMN
import school.users.dao.UserDao.Relations.SELECT_SIGNUP_AVAILABILITY
import school.users.dao.UserDao.Relations.TABLE_NAME
import school.users.security.UserRole
import school.users.security.UserRole.UserRoleDao.Dao.signup
import java.lang.Boolean.parseBoolean
import java.lang.Long.getLong
import java.util.*
import java.util.UUID.fromString

object UserDao {
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
        const val ID_ATTR = "id"
        const val LOGIN_ATTR = "login"
        const val PASSWORD_ATTR = "password"
        const val EMAIL_ATTR = "email"
        const val LANG_KEY_ATTR = "langKey"
        const val VERSION_ATTR = "version"

        fun Pair<String, ApplicationContext>.isEmail(): Boolean = second
            .getBean<Validator>()
            .validateValue(User.USERCLASS, EMAIL_ATTR, first)
            .isEmpty()

        fun Pair<String, ApplicationContext>.isLogin(): Boolean = second
            .getBean<Validator>()
            .validateValue(User.USERCLASS, LOGIN_ATTR, first)
            .isEmpty()
    }

    object Relations {
        const val TABLE_NAME = "`user`"
        const val SQL_SCRIPT = """
    CREATE TABLE IF NOT EXISTS $TABLE_NAME (
        ${Fields.ID_FIELD}            UUID default random_uuid() PRIMARY KEY,
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
    ) values ( :$LOGIN_ATTR, :$EMAIL_ATTR, :$PASSWORD_ATTR, :$LANG_KEY_ATTR, :$VERSION_ATTR)"""

        //TODO: signup, findByEmailOrLogin,
        const val FIND_USER_BY_LOGIN =
            """
                SELECT `u`.${UserDao.Fields.ID_FIELD} 
                FROM $TABLE_NAME AS `u` 
                WHERE u.$LOGIN_FIELD= LOWER(:$LOGIN_ATTR)
                """
        const val FIND_USER_BY_LOGIN_OR_EMAIL =
            """
                SELECT `u`.$LOGIN_FIELD 
                FROM $TABLE_NAME AS `u` 
                WHERE LOWER(`u`.$EMAIL_FIELD) = LOWER(:$EMAIL_ATTR) OR 
                LOWER(`u`.$LOGIN_FIELD) = LOWER(:$LOGIN_ATTR)
                """

        const val FIND_USER_BY_ID =
            "SELECT * FROM $TABLE_NAME AS `u` WHERE `u`.$ID_FIELD = :id"

        const val LOGIN_AND_EMAIL_AVAILABLE_COLUMN = "login_and_email_available"
        const val EMAIL_AVAILABLE_COLUMN = "email_available"
        const val LOGIN_AVAILABLE_COLUMN = "login_available"

        const val SELECT_SIGNUP_AVAILABILITY = """
                SELECT
                    CASE
                        WHEN EXISTS(
                                SELECT 1 FROM $TABLE_NAME 
                                WHERE LOWER($LOGIN_FIELD) = LOWER(:$LOGIN_ATTR)
                             ) OR 
                             EXISTS(
                                SELECT 1 FROM $TABLE_NAME 
                                WHERE LOWER($EMAIL_FIELD) = LOWER(:$EMAIL_ATTR)
                             )
                            THEN FALSE
                        ELSE TRUE
                        END AS $LOGIN_AND_EMAIL_AVAILABLE_COLUMN,
                    NOT EXISTS(
                            SELECT 1 FROM $TABLE_NAME 
                            WHERE LOWER($EMAIL_FIELD) = LOWER(:$EMAIL_ATTR)
                        ) AS $EMAIL_AVAILABLE_COLUMN,
                    NOT EXISTS(
                            SELECT 1 FROM $TABLE_NAME 
                            WHERE LOWER($LOGIN_FIELD) = LOWER(:$LOGIN_ATTR)
                        ) AS $LOGIN_AVAILABLE_COLUMN;
            """
        const val DELETE_USER_BY_ID = "DELETE FROM $TABLE_NAME AS `u` WHERE $ID_FIELD = :$ID_ATTR"
        const val DELETE_USER = "DELETE FROM $TABLE_NAME"

        @JvmStatic
        val CREATE_TABLES: String
            get() = setOf(
                UserDao.Relations.SQL_SCRIPT,
                UserRole.Role.RoleDao.Relations.SQL_SCRIPT,
                UserRole.UserRoleDao.Relations.SQL_SCRIPT,
                UserActivationDao.Relations.SQL_SCRIPT,
            ).joinToString("")
                .trimMargin()

        @Suppress("unused")
        object Rdbms {
            object Pg {}
            object H2 {}
        }
    }

    object Dao {
        suspend fun ApplicationContext.countUsers(): Int =
            "SELECT COUNT(*) FROM $TABLE_NAME"
                .let(getBean<DatabaseClient>()::sql)
                .fetch()
                .awaitSingle()
                .values
                .first()
                .toString()
                .toInt()

        //TODO: return the complete user from db without roles
        @Throws(EmptyResultDataAccessException::class)
        suspend fun Pair<User, ApplicationContext>.save(): Either<Throwable, UUID> = try {
            second.getBean<R2dbcEntityTemplate>()
                .databaseClient
                .sql(UserDao.Relations.INSERT)
                .bind(LOGIN_ATTR, first.login)
                .bind(EMAIL_ATTR, first.email)
                .bind(PASSWORD_ATTR, first.password)
                .bind(LANG_KEY_ATTR, first.langKey)
                .bind(VERSION_ATTR, first.version)
                .fetch()
                .awaitOne()[Attributes.ID_ATTR.uppercase()]
                .toString()
                .run(UUID::fromString)
                .right()
        } catch (e: Throwable) {
            e.left()
        }

        suspend fun ApplicationContext.deleteAllUsersOnly(): Unit = DELETE_USER
            .trimIndent()
            .let(getBean<DatabaseClient>()::sql)
            .await()

        suspend fun ApplicationContext.delete(id: UUID): Unit = DELETE_USER_BY_ID
            .trimIndent()
            .let(getBean<DatabaseClient>()::sql)
            .bind(UserDao.Attributes.ID_ATTR, id)
            .await()

        suspend inline fun <reified T : EntityModel<UUID>> ApplicationContext.findOne(
            emailOrLogin: String
        ): Either<Throwable, User> = when (T::class) {
            User::class -> try {
                FIND_USER_BY_LOGIN_OR_EMAIL.trimIndent()
                    .run(getBean<DatabaseClient>()::sql)
                    .bind(EMAIL_ATTR, emailOrLogin)
                    .bind(LOGIN_ATTR, emailOrLogin)
                    .fetch()
                    .awaitSingle()
                    .let {
                        User(
                            id = it[Fields.ID_FIELD] as UUID,
                            email = if ((emailOrLogin to this).isEmail()) emailOrLogin else it[EMAIL_FIELD].toString(),
                            login = if ((emailOrLogin to this).isLogin()) emailOrLogin else it[LOGIN_FIELD].toString(),
                            password = it[PASSWORD_FIELD].toString(),
                            langKey = it[LANG_KEY_FIELD].toString(),
                            version = it[VERSION_FIELD].toString().run(::getLong),
                        )
                    }.right()
            } catch (e: Throwable) {
                e.left()
            }

            else -> (T::class.simpleName)
                .run { "Unsupported type: $this" }
                .run(::IllegalArgumentException)
                .left()
        }

        suspend inline fun <reified T : EntityModel<UUID>> ApplicationContext.findOne(
            id: UUID
        ): Either<Throwable, User> = when (T::class) {
            User::class -> try {
                FIND_USER_BY_ID.trimIndent()
                    .run(getBean<DatabaseClient>()::sql)
                    .bind(EMAIL_ATTR, id)
                    .bind(LOGIN_ATTR, id)
                    .fetch()
                    .awaitSingleOrNull()
                    .let {
                        User(
                            id = it?.get(ID_FIELD.cleanField().uppercase())
                                .toString()
                                .run(UUID::fromString),
                            email = it?.get(EMAIL_FIELD).toString(),
                            login = it?.get(LOGIN_FIELD).toString(),
                            password = it?.get(PASSWORD_FIELD).toString(),
                            langKey = it?.get(LANG_KEY_FIELD).toString(),
                            version = getLong(it?.get(VERSION_FIELD).toString()),
                        )
                    }.right()
            } catch (e: Throwable) {
                e.left()
            }

            else -> (T::class.simpleName)
                .run { "Unsupported type: $this" }
                .run(::IllegalArgumentException)
                .left()
        }


        suspend inline fun <reified T : EntityModel<UUID>> ApplicationContext.findOneWithAuths(emailOrLogin: String): Either<Throwable, User> =
            when (T::class) {
                User::class -> {
                    try {
                        if (!((emailOrLogin to this).isEmail() || (emailOrLogin to this).isLogin()))
                            "not a valid login or not a valid email"
                                .run(::Exception)
                                .left()
                        val h2SQLquery = """
                            SELECT 
                                u.id,
                                u.email,
                                u.login,
                                u.password,
                                u.lang_key,
                                u.version,
                                `GROUP_CONCAT`(DISTINCT `a`.`role`) AS `user_roles`
                            FROM `user` as `u`
                            LEFT JOIN 
                                `user_authority` ua ON u.id = ua.user_id
                            LEFT JOIN 
                                `authority` as a ON ua.role = a.role
                            WHERE 
                                lower(u.email) = lower(:emailOrLogin) OR lower(u.login) = lower(:emailOrLogin)
                            GROUP BY 
                                u.id, u.email, u.login;
        """
                        getBean<DatabaseClient>()
                            .sql(h2SQLquery)
                            .bind("emailOrLogin", emailOrLogin)
                            .fetch()
                            .awaitSingleOrNull()
                            .run {
                                when {
                                    this == null -> Exception("not able to retrieve user id and roles").left()
                                    else -> User(
                                        id = fromString(get("id".uppercase()).toString()),
                                        email = get("email".uppercase()).toString(),
                                        login = get("login".uppercase()).toString(),
                                        roles = get("user_roles".uppercase())
                                            .toString()
                                            .split(",")
                                            .map { UserRole.Role(it) }
                                            .toSet(),
                                        password = get("password".uppercase()).toString(),
                                        langKey = get("lang_key".uppercase()).toString(),
                                        version = get("version".uppercase()).toString().toLong(),
                                    ).right()
                                }
                            }
                    } catch (e: Throwable) {
                        e.left()
                    }
                }

                else -> (T::class.simpleName)
                    .run { "Unsupported type: $this" }
                    .run(::IllegalArgumentException)
                    .left()
            }

        suspend inline fun <reified T : EntityModel<UUID>> ApplicationContext.findOneByLogin(login: String): Either<Throwable, UUID> =
            when (T::class) {
                User::class -> {
                    try {
                        (getBean<DatabaseClient>()
                            .sql("SELECT `u`.${Fields.ID_FIELD} FROM ${UserDao.Relations.TABLE_NAME} `u` WHERE LOWER(`u`.$LOGIN_FIELD) = LOWER(:$LOGIN_ATTR)".trimIndent())
                            .bind(LOGIN_ATTR, login)
                            .fetch()
                            .awaitOne()
                            .let { it["`id`"] as UUID }).right()
                    } catch (e: Throwable) {
                        e.left()
                    }
                }

                else -> IllegalArgumentException("Unsupported type: ${T::class.simpleName}").left()
            }


        suspend inline fun <reified T : EntityModel<UUID>> ApplicationContext.findOneByEmail(email: String): Either<Throwable, UUID> =
            when (T::class) {
                User::class -> {
                    try {
                        getBean<DatabaseClient>()
                            .sql("SELECT `u`.`id` FROM `user` `u` WHERE LOWER(`u`.`email`) = LOWER(:$EMAIL_ATTR)")
                            .bind(EMAIL_ATTR, email)
                            .fetch()
                            .awaitOne()
                            .let { it["id"] as UUID }
                            .right()
                    } catch (e: Throwable) {
                        e.left()
                    }
                }

                else -> Either.Left(IllegalArgumentException("Unsupported type: ${T::class.simpleName}"))
            }

        @Throws(EmptyResultDataAccessException::class)
        suspend fun Pair<User, ApplicationContext>.signup(): Either<Throwable, UUID> = try {
            second.getBean<TransactionalOperator>().executeAndAwait {
                (first to second).save()
            }
            second.findOneByEmail<User>(first.email)
                .mapLeft { return Exception("Unable to find user by email").left() }
                .map {
                    (UserRole(userId = it, role = Constants.ROLE_USER) to second).signup()
                    return it.right()
                }
        } catch (e: Throwable) {
            e.left()
        }

        fun ApplicationContext.signupToUser(signup: Signup): User = signup.apply {
            // Validation du mot de passe et de la confirmation
            require(password == repassword) { "Passwords do not match!" }
        }.run {
            // Création d'un utilisateur à partir des données de Signup
            User(
                login = login,
                password = getBean<PasswordEncoder>().encode(password),
                email = email,
            )
        }

        suspend fun Pair<Signup, ApplicationContext>.signupAvailability()
                : Either<Throwable, Triple<Boolean/*OK*/,
                Boolean/*email*/,
                Boolean/*login*/>> = try {
            second
                .getBean<R2dbcEntityTemplate>()
                .databaseClient
                .sql(SELECT_SIGNUP_AVAILABILITY.trimIndent())
                .bind(LOGIN_ATTR, first.login)
                .bind(EMAIL_ATTR, first.email)
                .fetch()
                .awaitSingle()
                .run {
                    Triple(
                        parseBoolean(this[LOGIN_AND_EMAIL_AVAILABLE_COLUMN.uppercase()].toString()),
                        parseBoolean(this[EMAIL_AVAILABLE_COLUMN.uppercase()].toString()),
                        parseBoolean(this[LOGIN_AVAILABLE_COLUMN.uppercase()].toString())
                    ).right()
                }
        } catch (e: Throwable) {
            e.left()
        }
    }

    fun ApplicationContext.userDetailsMono(emailOrLogin: String): Mono<UserDetails> =
        getBean<Validator>()
            .run {
                when {
                    validateProperty(
                        User(email = emailOrLogin),
                        EMAIL_FIELD
                    ).isNotEmpty() && validateProperty(
                        User(login = emailOrLogin),
                        LOGIN_FIELD
                    ).isNotEmpty() -> throw UsernameNotFoundException("User $emailOrLogin was not found")

                    else -> mono {
                        findOneWithAuths<User>(emailOrLogin).map { user ->
                            return@mono org.springframework.security.core.userdetails.User(
                                user.login,
                                user.password,
                                user.roles.map { SimpleGrantedAuthority(it.id) })
                        }.getOrNull() ?: throw UsernameNotFoundException("User $emailOrLogin was not found")
                    }
                }
            }

    /** User REST API URIs */
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
}