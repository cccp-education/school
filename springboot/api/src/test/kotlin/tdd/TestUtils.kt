@file:Suppress(
    "IdentifierGrammar",
    "MemberVisibilityCanBePrivate",
    "unused"
)

package tdd

import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.await
import org.springframework.r2dbc.core.awaitSingle
import webapp.core.property.*
import webapp.users.User
import java.time.Instant
import java.time.Instant.now
import java.util.*
import java.util.regex.Pattern
import kotlin.test.assertEquals

object TestUtils {

    object Data {
        const val OFFICIAL_SITE = "https://cheroliv.github.io/"
        const val DEFAULT_IMAGE_URL = "https://placehold.it/50x50"
        val admin: User by lazy { userFactory(ADMIN) }
        val user: User by lazy { userFactory(USER) }
        val users: Set<User> = setOf(admin, user)
        const val DEFAULT_USER_JSON = """{
    "login": "$USER",
    "email": "$USER@$DOMAIN_DEV_URL",
    "password": "$USER"}"""

        fun userFactory(login: String): User {
            val now: Instant = now()
            val uuid = UUID.randomUUID()
            return User(
                password = login,
                login = login,
                email = "$login@$DOMAIN_DEV_URL",
            )
        }
    }

    val ApplicationContext.PATTERN_LOCALE_2: Pattern
        get() = Pattern.compile("([a-z]{2})-([a-z]{2})")

    val ApplicationContext.PATTERN_LOCALE_3: Pattern
        get() = Pattern.compile("([a-z]{2})-([a-zA-Z]{4})-([a-z]{2})")

    val ApplicationContext.languages
        get() = setOf("en", "fr", "de", "it", "es")

    val ApplicationContext.queryDeleteAllUserAuthorityByUserLogin
        get() =
            "delete from user_authority where user_id = (select u.id from `user` u where u.login=:login)"

    val ApplicationContext.defaultRoles
        get() = setOf(ROLE_ADMIN, ROLE_USER, ROLE_ANONYMOUS)




    suspend fun ApplicationContext.countUsers(): Int =
        "select count(*) from `user`"
            .let(getBean<DatabaseClient>()::sql)
            .fetch()
            .awaitSingle()
            .values
            .first()
            .toString()
            .toInt()

    suspend fun ApplicationContext.countRoles(): Int =
        "select count(*) from `authority`"
            .let(getBean<DatabaseClient>()::sql)
            .fetch()
            .awaitSingle()
            .values
            .first()
            .toString()
            .toInt()

    suspend fun ApplicationContext.countUserAuthority(): Int =
        "select count(*) from `user_authority`"
            .let(getBean<DatabaseClient>()::sql)
            .fetch()
            .awaitSingle()
            .values
            .first()
            .toString()
            .toInt()

    suspend fun ApplicationContext.deleteAllUsersOnly(): Unit =
        "delete from `user`"
            .let(getBean<DatabaseClient>()::sql)
            .await()

    suspend fun ApplicationContext.deleteAllUserAuthorities(): Unit =
        "delete from user_authority"
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

    suspend fun ApplicationContext.deleteAllUserAuthorityByUserLogin(
        login: String
    ): Unit = getBean<DatabaseClient>()
        .sql(queryDeleteAllUserAuthorityByUserLogin)
        .bind("login", login)
        .await()

    fun ApplicationContext.checkProperty(
        property: String,
        value: String,
        injectedValue: String
    ) = property.apply {
        assertEquals(value, let(environment::getProperty))
        assertEquals(injectedValue, let(environment::getProperty))
    }
}