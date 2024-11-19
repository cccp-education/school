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
import school.base.utils.Constants
import school.users.Signup
import school.users.User
import school.users.dao.UserDao.Attributes.EMAIL_ATTR
import school.users.dao.UserDao.Attributes.LANG_KEY_ATTR
import school.users.dao.UserDao.Attributes.LOGIN_ATTR
import school.users.dao.UserDao.Attributes.PASSWORD_ATTR
import school.users.dao.UserDao.Attributes.VERSION_ATTR
import school.users.dao.UserDao.Attributes.isEmail
import school.users.dao.UserDao.Attributes.isLogin
import school.users.dao.UserDao.Dao.findOneWithAuths
import school.users.dao.UserDao.Fields.EMAIL_FIELD
import school.users.dao.UserDao.Fields.LANG_KEY_FIELD
import school.users.dao.UserDao.Fields.LOGIN_FIELD
import school.users.dao.UserDao.Fields.PASSWORD_FIELD
import school.users.dao.UserDao.Fields.VERSION_FIELD
import school.users.dao.UserDao.Relations.FIND_USER_BY_ID
import school.users.dao.UserDao.Relations.FIND_USER_BY_LOGIN_OR_EMAIL
import school.users.dao.UserDao.Relations.SELECT_SIGNUP_AVAILABILITY
import school.users.security.UserRole
import school.users.security.UserRole.UserRoleDao.Dao.signup
import java.lang.Long.getLong
import java.util.*

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
    ) values ( :login, :email, :password, :langKey, :version)"""

        //TODO: signup, findByEmailOrLogin,
        const val FIND_USER_BY_LOGIN =
            "SELECT `u`.${UserDao.Fields.ID_FIELD} FROM $TABLE_NAME AS `u` WHERE u.$LOGIN_FIELD= LOWER(:$LOGIN_ATTR)"
        val FIND_USER_BY_LOGIN_OR_EMAIL =
            "SELECT `u`.`id` FROM `user` AS `u` WHERE LOWER(`u`.`email`) = LOWER(:email) OR LOWER(`u`.`login`) = LOWER(:login)"
                .trimIndent()
        val FIND_USER_BY_ID =
            "SELECT * FROM `user` AS `u` WHERE `u`.`id` = :id"
                .trimIndent()
        const val SELECT_SIGNUP_AVAILABILITY = """
                SELECT
                    CASE
                        WHEN EXISTS(SELECT 1 FROM $TABLE_NAME WHERE LOWER($LOGIN_FIELD) = LOWER(:$LOGIN_ATTR))
                            OR EXISTS(SELECT 1 FROM $TABLE_NAME WHERE LOWER($EMAIL_FIELD) = LOWER(:$EMAIL_ATTR))
                            THEN FALSE
                        ELSE TRUE
                        END AS login_and_email_available,
                    NOT EXISTS(SELECT 1 FROM $TABLE_NAME WHERE LOWER($EMAIL_FIELD) = LOWER(:$EMAIL_ATTR)) AS email_available,
                    NOT EXISTS(SELECT 1 FROM $TABLE_NAME WHERE LOWER($LOGIN_FIELD) = LOWER(:$LOGIN_ATTR)) AS login_available;
            """

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
            "SELECT COUNT(*) FROM `user`"
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

        suspend fun ApplicationContext.deleteAllUsersOnly(): Unit =
            "DELETE FROM `user`"
                .let(getBean<DatabaseClient>()::sql)
                .await()

        suspend fun ApplicationContext.delete(id: UUID): Unit =
            "DELETE FROM `user` AS `u` WHERE `id` = :id"
                .let(getBean<DatabaseClient>()::sql)
                .bind(UserDao.Attributes.ID_ATTR, id)
                .await()

        suspend inline fun <reified T : EntityModel<UUID>> ApplicationContext.findOne(emailOrLogin: String): Either<Throwable, User> =
            when (T::class) {
                User::class -> try {
                    FIND_USER_BY_LOGIN_OR_EMAIL
                        .run(getBean<DatabaseClient>()::sql)
                        .bind(EMAIL_ATTR, emailOrLogin)
                        .bind(LOGIN_ATTR, emailOrLogin)
                        .fetch()
                        .awaitSingle()
                        .let {
                            User(
                                id = it[Fields.ID_FIELD] as UUID,
                                email = if ((emailOrLogin to this).isEmail()) emailOrLogin else it[EMAIL_FIELD] as String,
                                login = if ((emailOrLogin to this).isLogin()) emailOrLogin else it[LOGIN_FIELD] as String,
                                password = it[PASSWORD_FIELD] as String,
                                langKey = it[LANG_KEY_FIELD] as String,
                                version = it[VERSION_FIELD] as Long,
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

        suspend inline fun <reified T : EntityModel<UUID>> ApplicationContext.findOne(id: UUID): Either<Throwable, User> =
            when (T::class) {
                User::class -> try {
                    FIND_USER_BY_ID
                        .run(getBean<DatabaseClient>()::sql)
                        .bind(EMAIL_ATTR, id)
                        .bind(LOGIN_ATTR, id)
                        .fetch()
                        .awaitSingle()
                        .let {
                            User(
                                //TODO : fix with fromString and test nullability
                                id = it[Fields.ID_FIELD] as UUID,
                                email = it[EMAIL_FIELD].toString(),
                                login = it[LOGIN_FIELD].toString(),
                                password = it[PASSWORD_FIELD].toString(),
                                langKey = it[LANG_KEY_FIELD].toString(),
                                version = getLong(it[VERSION_FIELD].toString()),
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
                                        id = UUID.fromString(get("id".uppercase()).toString()),
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
            val loginAndEmailAvailableColumn = "login_and_email_available"
            val emailAvailableColumn = "email_available"
            val loginAvailableColumn = "login_available"
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
                        java.lang.Boolean.parseBoolean(this[loginAndEmailAvailableColumn.uppercase()].toString()),
                        java.lang.Boolean.parseBoolean(this[emailAvailableColumn.uppercase()].toString()),
                        java.lang.Boolean.parseBoolean(this[loginAvailableColumn.uppercase()].toString())
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