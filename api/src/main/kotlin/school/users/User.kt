@file:Suppress(
    "RemoveRedundantQualifierName",
    "MemberVisibilityCanBePrivate",
    "SqlNoDataSourceInspection", "SqlDialectInspection", "SqlSourceToSinkFlow"
)

package school.users

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.left
import arrow.core.right
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.Validator
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
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
import school.base.utils.Constants.EMPTY_STRING
import school.base.utils.Constants.ROLE_USER
import school.users.User.UserActivation.UserActivationDao.Attributes.ACTIVATION_DATE_ATTR
import school.users.User.UserActivation.UserActivationDao.Attributes.ACTIVATION_KEY_ATTR
import school.users.User.UserActivation.UserActivationDao.Attributes.CREATED_DATE_ATTR
import school.users.User.UserActivation.UserActivationDao.Fields.ACTIVATION_DATE_FIELD
import school.users.User.UserActivation.UserActivationDao.Fields.ACTIVATION_KEY_FIELD
import school.users.User.UserActivation.UserActivationDao.Fields.CREATED_DATE_FIELD
import school.users.User.UserDao.Attributes.EMAIL_ATTR
import school.users.User.UserDao.Attributes.LANG_KEY_ATTR
import school.users.User.UserDao.Attributes.LOGIN_ATTR
import school.users.User.UserDao.Attributes.PASSWORD_ATTR
import school.users.User.UserDao.Attributes.VERSION_ATTR
import school.users.User.UserDao.Attributes.isEmail
import school.users.User.UserDao.Attributes.isLogin
import school.users.User.UserDao.Constraints.LOGIN_REGEX
import school.users.User.UserDao.Constraints.PASSWORD_MAX
import school.users.User.UserDao.Constraints.PASSWORD_MIN
import school.users.User.UserDao.Dao.findOneWithAuths
import school.users.User.UserDao.Fields.EMAIL_FIELD
import school.users.User.UserDao.Fields.LANG_KEY_FIELD
import school.users.User.UserDao.Fields.LOGIN_FIELD
import school.users.User.UserDao.Fields.PASSWORD_FIELD
import school.users.User.UserDao.Fields.VERSION_FIELD
import school.users.User.UserDao.Relations.FIND_USER_BY_ID
import school.users.User.UserDao.Relations.FIND_USER_BY_LOGIN_OR_EMAIL
import school.users.User.UserDao.Relations.SELECT_SIGNUP_AVAILABILITY
import school.users.User.UserDao.Relations.TABLE_NAME
import school.users.security.SecurityUtils.generateActivationKey
import school.users.security.UserRole
import school.users.security.UserRole.Role
import school.users.security.UserRole.UserRoleDao.Dao.signup
import java.lang.Boolean.parseBoolean
import java.time.Instant
import java.time.Instant.now
import java.util.*
import java.util.Locale.ENGLISH
import java.util.UUID.fromString
import jakarta.validation.constraints.Email as EmailConstraint
import org.springframework.security.core.userdetails.User as UserSecurity

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
    @JvmRecord
    data class Signup(
        @field:NotNull
        @field:Pattern(regexp = LOGIN_REGEX)
        @field:Size(min = 1, max = 50)
        val login: String,
        @field:NotNull
        @Size(
            min = PASSWORD_MIN,
            max = PASSWORD_MAX
        )
        val password: String,
        val repassword: String,
        @field:Email
        @field:Size(min = 5, max = 254)
        val email: String,
    ) {
        companion object {
            val SIGNUPCLASS = Signup::class.java

            @JvmStatic
            val objectName: String = SIGNUPCLASS.simpleName.run {
                replaceFirst(
                    first(),
                    first().lowercaseChar()
                )
            }
        }

    }

    @JvmRecord
    data class UserActivation(
        val id: UUID,
        @field:Size(max = 20)
        val activationKey: String = generateActivationKey,
        val createdDate: Instant = now(),
        val activationDate: Instant? = null,
    ) {
        companion object {
            @JvmStatic
            fun main(args: Array<String>): Unit = println(UserActivationDao.Relations.SQL_SCRIPT)
        }

        object UserActivationDao {
            object Fields {
                const val ID_FIELD = "`id`"
                const val ACTIVATION_KEY_FIELD = "`activation_key`"
                const val ACTIVATION_DATE_FIELD = "`activation_date`"
                const val CREATED_DATE_FIELD = "`created_date`"
            }

            object Attributes {
                const val ID_ATTR = "id"
                const val ACTIVATION_KEY_ATTR = "activationKey"
                const val CREATED_DATE_ATTR = "createdDate"
                const val ACTIVATION_DATE_ATTR = "activationDate"
            }

            object Relations {
                const val TABLE_NAME = "`user_activation`"
                const val SQL_SCRIPT = """
                CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                    ${Fields.ID_FIELD}            UUID PRIMARY KEY,
                    $ACTIVATION_KEY_FIELD         VARCHAR,
                    $CREATED_DATE_FIELD           datetime,
                    $ACTIVATION_DATE_FIELD        datetime,
                FOREIGN KEY (${Fields.ID_FIELD}) REFERENCES ${UserDao.Relations.TABLE_NAME} (${UserDao.Fields.ID_FIELD})
                    ON DELETE CASCADE
                    ON UPDATE CASCADE);
                CREATE UNIQUE INDEX IF NOT EXISTS `uniq_idx_user_activation_key`
                ON $TABLE_NAME ($ACTIVATION_KEY_FIELD);
                CREATE INDEX IF NOT EXISTS `idx_user_activation_date`
                ON $TABLE_NAME ($CREATED_DATE_FIELD);
                CREATE INDEX IF NOT EXISTS `idx_user_activation_creation_date`
                ON $TABLE_NAME ($ACTIVATION_DATE_FIELD);
            """

                const val INSERT = """
                insert into $TABLE_NAME (
                ${Fields.ID_FIELD} , $ACTIVATION_KEY_FIELD, $CREATED_DATE_FIELD, $ACTIVATION_DATE_FIELD)
                values (:${Attributes.ID_ATTR}, :$ACTIVATION_KEY_ATTR, :$CREATED_DATE_ATTR, :$ACTIVATION_DATE_ATTR)
            """
            }

            object Dao {
                suspend fun ApplicationContext.countUserActivation(): Int =
                    "SELECT COUNT(*) FROM `user_activation`"
                        .let(getBean<DatabaseClient>()::sql)
                        .fetch()
                        .awaitSingle()
                        .values
                        .first()
                        .toString()
                        .toInt()

                @Throws(EmptyResultDataAccessException::class)
                suspend fun Pair<UserActivation, ApplicationContext>.save(): Either<Throwable, Long> = try {
                    second.getBean<R2dbcEntityTemplate>()
                        .databaseClient
                        .sql(Relations.INSERT.trimIndent())
                        .bind(Attributes.ID_ATTR, first.id)
                        .bind(ACTIVATION_KEY_ATTR, first.activationKey)
                        .bind(CREATED_DATE_ATTR, first.createdDate)
                        .bind(ACTIVATION_DATE_ATTR, first.activationDate)
                        .fetch()
                        .awaitRowsUpdated()
                        .right()
                } catch (e: Throwable) {
                    e.left()
                }

                //Find userActivation by key
                //Update userActivation
                //TODO: activate user from key (Find userActivation then Update userActivation)  one query select+update
                @Throws(EmptyResultDataAccessException::class)
                suspend fun ApplicationContext.findByKey(key:String)
                        : Either<Throwable, UserActivation> = try {
                    getBean<R2dbcEntityTemplate>()
                        .databaseClient
                        .sql("SELECT * FROM `user_activation` WHERE $ACTIVATION_KEY_FIELD = :$ACTIVATION_KEY_ATTR")
                        .bind(ACTIVATION_KEY_ATTR, key)
                        .fetch()
                        .awaitSingleOrNull()
                        .let {
                            UserActivation(
                                id = it?.get(Fields.ID_FIELD) as UUID,
                                activationKey = it[ACTIVATION_KEY_FIELD] as String,
                                activationDate = it[ACTIVATION_DATE_FIELD] as Instant,
                                createdDate = it[CREATED_DATE_FIELD] as Instant,
                            )
                        }.right()
                } catch (e: Throwable) {
                    e.left()
                }
            }
        }
    }

    /**
     * Représente l'account domain model minimaliste pour la view
     */
    @JvmRecord
    data class Avatar(
        val id: UUID? = null,
        val login: String? = null
    )


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
                .validateValue(USERCLASS, EMAIL_ATTR, first)
                .isEmpty()

            fun Pair<String, ApplicationContext>.isLogin(): Boolean = second
                .getBean<Validator>()
                .validateValue(USERCLASS, LOGIN_ATTR, first)
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
                    User.UserDao.Relations.SQL_SCRIPT,
                    Role.RoleDao.Relations.SQL_SCRIPT,
                    UserRole.UserRoleDao.Relations.SQL_SCRIPT,
                    UserActivation.UserActivationDao.Relations.SQL_SCRIPT,
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
                    .sql(User.UserDao.Relations.INSERT)
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
                                    id = it[Fields.ID_FIELD] as UUID,
                                    email = it[EMAIL_FIELD] as String,
                                    login = it[LOGIN_FIELD] as String,
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
                                                .map { Role(it) }
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
                                .sql("SELECT `u`.${Fields.ID_FIELD} FROM $TABLE_NAME `u` WHERE LOWER(`u`.$LOGIN_FIELD) = LOWER(:$LOGIN_ATTR)".trimIndent())
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
                            parseBoolean(this[loginAndEmailAvailableColumn.uppercase()].toString()),
                            parseBoolean(this[emailAvailableColumn.uppercase()].toString()),
                            parseBoolean(this[loginAvailableColumn.uppercase()].toString())
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
                                return@mono UserSecurity(
                                    user.login,
                                    user.password,
                                    user.roles.map { SimpleGrantedAuthority(it.id) })
                            }.getOrNull() ?: throw UsernameNotFoundException("User $emailOrLogin was not found")
                        }
                    }
                }
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