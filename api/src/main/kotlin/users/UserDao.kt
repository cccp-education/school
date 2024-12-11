@file:Suppress("MemberVisibilityCanBePrivate")

package users

import app.database.EntityModel
import app.utils.Constants.ROLE_USER
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import jakarta.validation.Validator
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.core.*
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import users.UserDao.Attributes.EMAILORLOGIN
import users.UserDao.Attributes.EMAIL_ATTR
import users.UserDao.Attributes.ID_ATTR
import users.UserDao.Attributes.LANG_KEY_ATTR
import users.UserDao.Attributes.LOGIN_ATTR
import users.UserDao.Attributes.PASSWORD_ATTR
import users.UserDao.Attributes.VERSION_ATTR
import users.UserDao.Attributes.isEmail
import users.UserDao.Attributes.isLogin
import users.UserDao.Fields.EMAIL_FIELD
import users.UserDao.Fields.ID_FIELD
import users.UserDao.Fields.LANG_KEY_FIELD
import users.UserDao.Fields.LOGIN_FIELD
import users.UserDao.Fields.PASSWORD_FIELD
import users.UserDao.Fields.VERSION_FIELD
import users.UserDao.Members.ROLES_MEMBER
import users.UserDao.Relations.COUNT
import users.UserDao.Relations.DELETE_USER
import users.UserDao.Relations.DELETE_USER_BY_ID
import users.UserDao.Relations.EMAIL_AVAILABLE_COLUMN
import users.UserDao.Relations.FIND_USER_BY_EMAIL
import users.UserDao.Relations.FIND_USER_BY_ID
import users.UserDao.Relations.FIND_USER_BY_LOGIN
import users.UserDao.Relations.FIND_USER_BY_LOGIN_OR_EMAIL
import users.UserDao.Relations.FIND_USER_WITH_AUTHS_BY_EMAILOGIN
import users.UserDao.Relations.INSERT
import users.UserDao.Relations.LOGIN_AND_EMAIL_AVAILABLE_COLUMN
import users.UserDao.Relations.LOGIN_AVAILABLE_COLUMN
import users.UserDao.Relations.SELECT_SIGNUP_AVAILABILITY
import users.security.Role
import users.security.RoleDao
import users.security.UserRole
import users.security.UserRoleDao
import users.security.UserRoleDao.Dao.signup
import users.signup.Signup
import users.signup.UserActivation
import users.signup.UserActivationDao
import users.signup.UserActivationDao.Dao.save
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
        const val ID_FIELD = "id"
        const val LOGIN_FIELD = "login"
        const val PASSWORD_FIELD = "password"
        const val EMAIL_FIELD = "email"
        const val LANG_KEY_FIELD = "lang_key"
        const val VERSION_FIELD = "version"
    }

    object Attributes {
        const val ID_ATTR = "id"
        const val LOGIN_ATTR = "login"
        const val PASSWORD_ATTR = "password"
        const val EMAIL_ATTR = "email"
        const val LANG_KEY_ATTR = "langKey"
        const val VERSION_ATTR = "version"
        const val EMAILORLOGIN = "emailOrLogin"

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
        const val TABLE_NAME = "user"
        const val SQL_SCRIPT = """
        CREATE TABLE IF NOT EXISTS "$TABLE_NAME"(
        $ID_FIELD         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        "$LOGIN_FIELD"    TEXT,
        "$PASSWORD_FIELD" TEXT,
        "$EMAIL_FIELD"    TEXT,
        "$LANG_KEY_FIELD" VARCHAR,
        "$VERSION_FIELD"  BIGINT);

        CREATE UNIQUE INDEX IF NOT EXISTS "uniq_idx_user_login" ON "$TABLE_NAME" ("$LOGIN_FIELD");
        CREATE UNIQUE INDEX IF NOT EXISTS "uniq_idx_user_email" ON "$TABLE_NAME" ("$EMAIL_FIELD");
        """
        const val FIND_ALL_USERS = """SELECT * FROM "user";"""

        const val INSERT = """
                insert into "$TABLE_NAME" (
                    "$LOGIN_FIELD", "$EMAIL_FIELD",
                    "$PASSWORD_FIELD", $LANG_KEY_FIELD,
                    $VERSION_FIELD
                ) values ( 
                :$LOGIN_ATTR, 
                :$EMAIL_ATTR, 
                :$PASSWORD_ATTR, 
                :$LANG_KEY_ATTR, 
                :$VERSION_ATTR);"""

        const val FIND_USER_BY_LOGIN = """
                SELECT u.$ID_FIELD 
                FROM "$TABLE_NAME" AS u 
                WHERE u."$LOGIN_FIELD" = LOWER(:$LOGIN_ATTR);
                """
        const val FIND_USER_BY_LOGIN_OR_EMAIL = """
                SELECT u."$LOGIN_FIELD" 
                FROM "$TABLE_NAME" AS u 
                WHERE LOWER(u."$EMAIL_FIELD") = LOWER(:$EMAIL_ATTR) OR 
                LOWER(u."$LOGIN_FIELD") = LOWER(:$LOGIN_ATTR);
                """

        const val FIND_USER_BY_ID = """SELECT * FROM "$TABLE_NAME" AS u WHERE u.$ID_FIELD = :id;"""

        const val LOGIN_AND_EMAIL_AVAILABLE_COLUMN = "login_and_email_available"
        const val EMAIL_AVAILABLE_COLUMN = "email_available"
        const val LOGIN_AVAILABLE_COLUMN = "login_available"
        const val SELECT_SIGNUP_AVAILABILITY = """
                SELECT
                    CASE
                        WHEN EXISTS(
                                SELECT 1 FROM "$TABLE_NAME" 
                                WHERE LOWER("$LOGIN_FIELD") = LOWER(:$LOGIN_ATTR)
                             ) OR 
                             EXISTS(
                                SELECT 1 FROM "$TABLE_NAME" 
                                WHERE LOWER("$EMAIL_FIELD") = LOWER(:$EMAIL_ATTR)
                             )
                            THEN FALSE
                        ELSE TRUE
                    END AS login_and_email_available,
                    NOT EXISTS(
                            SELECT 1 FROM "$TABLE_NAME" 
                            WHERE LOWER("$EMAIL_FIELD") = LOWER(:$EMAIL_ATTR)
                        ) AS email_available,
                    NOT EXISTS(
                            SELECT 1 FROM "$TABLE_NAME" 
                            WHERE LOWER("$LOGIN_FIELD") = LOWER(:$LOGIN_ATTR)
                        ) AS login_available;
        """
        const val DELETE_USER_BY_ID = """DELETE FROM "$TABLE_NAME" AS u WHERE $ID_FIELD = :$ID_ATTR;"""
        const val DELETE_USER = """DELETE FROM "$TABLE_NAME";"""
        const val FIND_USER_WITH_AUTHS_BY_EMAILOGIN = """
                            SELECT 
                                u.id,
                                u."$EMAIL_FIELD",
                                u."$LOGIN_FIELD",
                                u."$PASSWORD_FIELD",
                                u.$LANG_KEY_FIELD,
                                u.$VERSION_FIELD,
                                STRING_AGG(DISTINCT a."${RoleDao.Fields.ID_FIELD}", ', ') AS $ROLES_MEMBER
                            FROM "$TABLE_NAME" as u
                            LEFT JOIN 
                                user_authority ua ON u.$ID_FIELD = ua.${UserRoleDao.Fields.USER_ID_FIELD}
                            LEFT JOIN 
                                authority as a ON UPPER(ua."${RoleDao.Fields.ID_FIELD}") = UPPER(a."${RoleDao.Attributes.ID_ATTR}")
                            WHERE 
                                lower(u."$EMAIL_FIELD") = lower(:$EMAILORLOGIN) 
                                OR 
                                lower(u."$LOGIN_FIELD") = lower(:$EMAILORLOGIN)
                            GROUP BY 
                                u.$ID_FIELD, u."$EMAIL_FIELD", u."$LOGIN_FIELD";
                        """

        const val COUNT = """SELECT COUNT(*) FROM "$TABLE_NAME";"""

        const val FIND_USER_BY_EMAIL = """
            SELECT u.$ID_FIELD 
            FROM "$TABLE_NAME" as u 
            WHERE LOWER(u."$EMAIL_FIELD") = LOWER(:$EMAIL_ATTR)"""

        @JvmStatic
        val CREATE_TABLES: String
            get() = listOf(
                SQL_SCRIPT,
                RoleDao.Relations.SQL_SCRIPT,
                UserRoleDao.Relations.SQL_SCRIPT,
                UserActivationDao.Relations.SQL_SCRIPT,
            ).joinToString(
                "",
                transform = String::trimIndent
            ).trimMargin()

    }

    object Dao {
        suspend fun ApplicationContext.countUsers(): Int = COUNT
            .trimIndent()
            .let(getBean<DatabaseClient>()::sql)
            .fetch()
            .awaitSingle()
            .values
            .first()
            .toString()
            .toInt()

        @Throws(EmptyResultDataAccessException::class)
        suspend fun Pair<User, ApplicationContext>.save(): Either<Throwable, UUID> = try {
            INSERT
                .trimIndent()
                .run(second.getBean<R2dbcEntityTemplate>().databaseClient::sql)
                .bind(LOGIN_ATTR, first.login)
                .bind(EMAIL_ATTR, first.email)
                .bind(PASSWORD_ATTR, first.password)
                .bind(LANG_KEY_ATTR, first.langKey)
                .bind(VERSION_ATTR, first.version)
                .fetch()
                .awaitOne()[ID_ATTR]
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
            .bind(ID_ATTR, id)
            .await()

        suspend inline fun <reified T : EntityModel<UUID>> ApplicationContext.findOne(
            emailOrLogin: String
        ): Either<Throwable, User> = when (T::class) {
            User::class -> try {
                FIND_USER_BY_LOGIN_OR_EMAIL
                    .trimIndent()
                    .run(getBean<DatabaseClient>()::sql)
                    .bind(EMAIL_ATTR, emailOrLogin)
                    .bind(LOGIN_ATTR, emailOrLogin)
                    .fetch()
                    .awaitSingle()
                    .let {
                        User(
                            id = fromString(it[ID_FIELD].toString()),
                            email = if ((emailOrLogin to this).isEmail()) emailOrLogin
                            else it[EMAIL_FIELD].toString(),
                            login = if ((emailOrLogin to this).isLogin()) emailOrLogin
                            else it[LOGIN_FIELD].toString(),
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
                FIND_USER_BY_ID
                    .trimIndent()
                    .run(getBean<DatabaseClient>()::sql)
                    .bind(EMAIL_ATTR, id)
                    .bind(LOGIN_ATTR, id)
                    .fetch()
                    .awaitSingleOrNull()
                    .let {
                        User(
                            id = it?.get(ID_FIELD)
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

                        FIND_USER_WITH_AUTHS_BY_EMAILOGIN
                            .trimIndent()
                            .run(getBean<DatabaseClient>()::sql)
                            .bind(EMAILORLOGIN, emailOrLogin)
                            .fetch()
                            .awaitSingleOrNull()
                            .run {
                                when {
                                    this == null -> Exception("not able to retrieve user id and roles").left()
                                    else -> User(
                                        id = fromString(get(ID_FIELD).toString()),
                                        email = get(EMAIL_FIELD).toString(),
                                        login = get(LOGIN_FIELD).toString(),
                                        roles = get(ROLES_MEMBER)
                                            .toString()
                                            .split(",")
                                            .map { Role(it) }
                                            .toSet(),
                                        password = get(PASSWORD_FIELD).toString(),
                                        langKey = get(LANG_KEY_FIELD).toString(),
                                        version = get(VERSION_FIELD).toString().toLong(),
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
                        FIND_USER_BY_LOGIN
                            .trimIndent()
                            .run(getBean<DatabaseClient>()::sql)
                            .bind(LOGIN_ATTR, login)
                            .fetch()
                            .awaitOne()
                            .let { fromString(it[ID_FIELD].toString()) }.right()
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
                        FIND_USER_BY_EMAIL
                            .trimIndent()
                            .run(getBean<DatabaseClient>()::sql)
                            .bind(EMAIL_ATTR, email)
                            .fetch()
                            .awaitOne()
                            .let { fromString(it[ID_ATTR].toString()) }
                            .right()
                    } catch (e: Throwable) {
                        e.left()
                    }
                }

                else -> Either.Left(IllegalArgumentException("Unsupported type: ${T::class.simpleName}"))
            }

        @Throws(EmptyResultDataAccessException::class)
        suspend fun Pair<User, ApplicationContext>.signup(): Either<Throwable, Pair<UUID, String>> = try {
            second.getBean<TransactionalOperator>().executeAndAwait {
                (first to second).save()
            }
            second.findOneByEmail<User>(first.email).mapLeft {
                return Exception("Unable to find user by email").left()
            }.map {
                (UserRole(userId = it, role = ROLE_USER) to second).signup()
                val userActivation = UserActivation(id = it)
                (userActivation to second).save()
                return (it to userActivation.activationKey).right()
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
                : Either<Throwable, Triple<Boolean/*OK*/, Boolean/*email*/, Boolean/*login*/>> = try {
            SELECT_SIGNUP_AVAILABILITY
                .trimIndent()
                .run(second.getBean<R2dbcEntityTemplate>().databaseClient::sql)
                .bind(LOGIN_ATTR, first.login)
                .bind(EMAIL_ATTR, first.email)
                .fetch()
                .awaitSingle()
                .run {
                    Triple(
                        parseBoolean(this[LOGIN_AND_EMAIL_AVAILABLE_COLUMN].toString()),
                        parseBoolean(this[EMAIL_AVAILABLE_COLUMN].toString()),
                        parseBoolean(this[LOGIN_AVAILABLE_COLUMN].toString())
                    ).right()
                }
        } catch (e: Throwable) {
            e.left()
        }
    }
}