@file:Suppress(
    "RemoveRedundantQualifierName",
    "MemberVisibilityCanBePrivate",
    "SqlNoDataSourceInspection"
)

package school.users

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.left
import arrow.core.right
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.Validator
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.core.*
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import school.base.model.EntityModel
import school.base.utils.AppUtils.cleanField
import school.base.utils.EMPTY_STRING
import school.base.utils.ROLE_USER
import school.users.User.UserDao.Attributes.EMAIL_ATTR
import school.users.User.UserDao.Attributes.ID_ATTR
import school.users.User.UserDao.Attributes.LANG_KEY_ATTR
import school.users.User.UserDao.Attributes.LOGIN_ATTR
import school.users.User.UserDao.Attributes.PASSWORD_ATTR
import school.users.User.UserDao.Attributes.VERSION_ATTR
import school.users.User.UserDao.Attributes.isThisEmail
import school.users.User.UserDao.Attributes.isThisLogin
import school.users.User.UserDao.Constraints.LOGIN_REGEX
import school.users.User.UserDao.Fields.EMAIL_FIELD
import school.users.User.UserDao.Fields.ID_FIELD
import school.users.User.UserDao.Fields.LANG_KEY_FIELD
import school.users.User.UserDao.Fields.LOGIN_FIELD
import school.users.User.UserDao.Fields.PASSWORD_FIELD
import school.users.User.UserDao.Fields.VERSION_FIELD
import school.users.User.UserDao.Relations.FIND_USER_BY_LOGIN_OR_EMAIL
import school.users.User.UserDao.Relations.INSERT
import school.users.security.Role
import school.users.security.UserRole
import school.users.security.UserRole.UserRoleDao.Dao.signup
import java.util.*
import java.util.Locale.ENGLISH
import java.util.UUID.fromString
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
        fun main(args: Array<String>) = println(UserDao.Relations.CREATE_TABLES)
    }

    data class Signup(
        val login: String,
        val password: String,
        val repassword: String,
        val email: String,
    )

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
            fun Pair<String, ApplicationContext>.isThisEmail(): Boolean = second
                .getBean<Validator>()
                .validateValue(USERCLASS, EMAIL_ATTR, first)
                .isEmpty()

            fun Pair<String, ApplicationContext>.isThisLogin(): Boolean = second
                .getBean<Validator>()
                .validateValue(USERCLASS, LOGIN_ATTR, first)
                .isEmpty()
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
                "SELECT `u`.${UserDao.Fields.ID_FIELD} FROM ${UserDao.Relations.TABLE_NAME} AS `u` WHERE u.${UserDao.Fields.LOGIN_FIELD}= LOWER(:${UserDao.Attributes.LOGIN_ATTR})"
            val FIND_USER_BY_LOGIN_OR_EMAIL =
                "SELECT `u`.`id` FROM `user` AS `u` WHERE LOWER(`u`.`email`) = LOWER(:email) OR LOWER(`u`.`login`) = LOWER(:login)"
                    .trimIndent()

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

            //TODO: return the complete user from db without roles
            @Throws(EmptyResultDataAccessException::class)
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
                    .awaitOne()[ID_ATTR.uppercase()]
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
                                    id = it[ID_FIELD] as UUID,
                                    email = if ((emailOrLogin to this).isThisEmail()) emailOrLogin else it[EMAIL_FIELD] as String,
                                    login = if ((emailOrLogin to this).isThisLogin()) emailOrLogin else it[LOGIN_FIELD] as String,
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


            suspend inline fun <reified T : EntityModel<UUID>> ApplicationContext.findOneWithAuths(emailOrLogin: String): Either<Throwable, User> =
                when (T::class) {
                    User::class -> {
                        try {
                            if (!((emailOrLogin to this).isThisEmail() || (emailOrLogin to this).isThisLogin()))
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
                            val userWithAuths: MutableMap<String, Any>? = getBean<DatabaseClient>()
                                .sql(h2SQLquery)
                                .bind("emailOrLogin", emailOrLogin)
                                .fetch()
                                .awaitSingleOrNull()

                            userWithAuths.run {
                                if (this == null) Exception("not able to retrieve user id and roles").left()
                                else
                                    User(
                                        id = fromString(get("id".uppercase()).toString()),
                                        email = get("email".uppercase()).toString(),
                                        login = get("login".uppercase()).toString(),
                                        roles = get("user_roles".uppercase())
                                            .toString()
                                            .split(",")
                                            .map { Role(it) }
                                            .toSet(),
                                        password = get("password".uppercase()).toString(),
                                        langKey = get("lang_key".uppercase()).toString(),
                                        version = get("version".uppercase()).toString().toLong(),
                                    ).right()
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


//            suspend inline fun <reified T : EntityModel<UUID>> ApplicationContext.__findOneWithAuths__(emailOrLogin: String): Either<Throwable, User> =
//                when (T::class) {
//                    User::class -> {
//                        try {
//                            if (!((emailOrLogin to this).isThisEmail() || (emailOrLogin to this).isThisLogin()))
//                                "not a valid login or not a valid email"
//                                    .run(::Exception)
//                                    .left()
//                            //TODO: refactor à partir d'ici pour utiliser la requete avec jointures
//                            val user = findOne<User>(emailOrLogin).getOrNull()
//                            val roles: Set<Role>? = findAuthsByEmail(emailOrLogin).getOrNull()
//                            // No need for that test, let catch intercept throwable.
//                            when {
//                                user != null && roles != null -> user.copy(roles = roles).right()
//
//                                else -> Exception("not able to retrieve user id and roles").left()
//                            }
//                        } catch (e: Throwable) {
//                            e.left()
//                        }
//                    }
//
//                    else -> (T::class.simpleName)
//                        .run { "Unsupported type: $this" }
//                        .run(::IllegalArgumentException)
//                        .left()
//                }

//            //TODO: return the complete user from db with roles
//            suspend fun ApplicationContext.findAuthsByEmail(email: String): Either<Throwable, Set<Role>> = try {
//                mutableSetOf<Role>().apply {
//                    getBean<DatabaseClient>()
//                        .sql("SELECT `ua`.`role` FROM `user` `u` JOIN `user_authority` `ua` ON `u`.`id` = `ua`.`user_id` WHERE `u`.`email` = :email")
//                        .bind("email", email)
//                        .fetch()
//                        .all()
//                        .collect { add(Role(it["ROLE"].toString())) }
//                }.toSet().right()
//            } catch (e: Throwable) {
//                e.left()
//            }


            suspend inline fun <reified T : EntityModel<UUID>> ApplicationContext.findOneByLogin(login: String): Either<Throwable, UUID> =
                when (T::class) {
                    User::class -> {
                        try {
                            (getBean<DatabaseClient>()
                                .sql("SELECT `u`.`id` FROM `user` u WHERE LOWER(u.login) = LOWER(:login)")
                                .bind("login", login)
                                .fetch()
                                .awaitOne()
                                .let { it["`id`"] as UUID }).right()
                        } catch (e: Throwable) {
                            e.left()
                        }
                    }

                    else -> Left(IllegalArgumentException("Unsupported type: ${T::class.simpleName}"))
                }


            suspend inline fun <reified T : EntityModel<UUID>> ApplicationContext.findOneByEmail(email: String): Either<Throwable, UUID> =
                when (T::class) {
                    User::class -> {
                        try {
                            getBean<DatabaseClient>()
                                .sql("SELECT `u`.`id` FROM `user` `u` WHERE LOWER(`u`.`email`) = LOWER(:email)")
                                .bind("email", email)
                                .fetch()
                                .awaitOne()
                                .let { it["id"] as UUID }
                                .right()
                        } catch (e: Throwable) {
                            e.left()
                        }
                    }

                    else -> Left(IllegalArgumentException("Unsupported type: ${T::class.simpleName}"))
                }

            @Throws(EmptyResultDataAccessException::class)
            suspend fun Pair<User, ApplicationContext>.signup(): Either<Throwable, UUID> = try {
                second.getBean<TransactionalOperator>().executeAndAwait {
                    (first to second).save()
                }
                second.findOneByEmail<User>(first.email)
                    .mapLeft { return Exception("Unable to find user by email").left() }
                    .map {
                        (UserRole(userId = it, role = ROLE_USER) to second).signup()
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
//                    id = randomUUID(), // Génération d'un UUID
                    login = login,
                    password = getBean<PasswordEncoder>().encode(password),
                    email = email,
                    roles = emptySet(),//mutableSetOf(Role(ANONYMOUS_USER)), // Role par défaut
                    langKey = "en" // Valeur par défaut, ajustez si nécessaire
                )
            }
        }
    }
}
