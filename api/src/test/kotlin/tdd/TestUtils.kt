@file:Suppress(
    "IdentifierGrammar",
    "MemberVisibilityCanBePrivate",
    "unused", "UNUSED_VARIABLE"
)

package tdd

import org.springframework.context.ApplicationContext
import webapp.core.property.*
import webapp.users.User
import java.time.Instant.now
import java.util.UUID.randomUUID
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
            val now = now()
            val uuid = randomUUID()
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

    val ApplicationContext.defaultRoles
        get() = setOf(ROLE_ADMIN, ROLE_USER, ROLE_ANONYMOUS)

    fun ApplicationContext.checkProperty(
        property: String,
        value: String,
        injectedValue: String
    ) = property.apply {
        assertEquals(value, let(environment::getProperty))
        assertEquals(injectedValue, let(environment::getProperty))
    }
}