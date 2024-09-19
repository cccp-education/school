@file:Suppress("unused")

package community

import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import community.accounts.Account
import community.accounts.User
import community.accounts.UserRole
import community.security.SecurityJwt
import community.security.generateActivationKey
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Description
import org.hamcrest.TypeSafeDiagnosingMatcher
import org.springframework.beans.factory.getBean
import org.springframework.boot.runApplication
import org.springframework.boot.web.reactive.context.StandardReactiveWebEnvironment
import org.springframework.context.ApplicationContext
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.select
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query.query
import org.springframework.security.authentication.RememberMeAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import java.io.IOException
import java.lang.Byte.parseByte
import java.time.Instant.now
import java.time.ZonedDateTime
import java.time.ZonedDateTime.parse
import java.time.format.DateTimeParseException
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

fun ApplicationContext.initActivatedDefaultAccount() {
    assertEquals(0, countAccount)
    assertEquals(0, countUserRole)
    //activation de l'account
    createActivatedDataAccounts(setOf(defaultAccount))
    findOneByEmail(defaultAccount.email!!).run {
        assertNotNull(this)
        assertEquals(defaultAccount.login, login)
    }
    assertEquals(1, countAccount)
    assertEquals(1, countUserRole)
}

fun launcher(vararg profiles: String) = runApplication<Application> {
    /**
     * before launching: configuration
     */
    setEnvironment(StandardReactiveWebEnvironment().apply {
        setDefaultProfiles(TEST)
        addActiveProfile(TEST)
        profiles.toMutableSet().apply {
            add(DEFAULT)
        }.map { addActiveProfile(it) }
    })
}.apply {
    /**
     * after launching: verification & post construct
     */
    (if (environment.defaultProfiles.isNotEmpty()) environment
        .defaultProfiles
        .reduce { acc, s -> "$acc, $s" }
    else "").run { i("defaultProfiles: $this") }

    (if (environment.activeProfiles.isNotEmpty()) environment
        .activeProfiles
        .reduce { acc, s -> "$acc, $s" }
    else "").run { i("activeProfiles: $this") }
}

fun ApplicationContext.userToken(account: Account) = mono {
    getBean<SecurityJwt>().createToken(
        RememberMeAuthenticationToken(
            account.login,
            getBean<ReactiveUserDetailsService>()
                .findByUsername(account.email)
                .awaitSingleOrNull(),
            setOf(GrantedAuthority { ROLE_USER }),
        ), true
    )
}.block()!!

fun ApplicationContext.createActivatedUserAndAdmin() {
    val countUserBefore = countAccount
    val countUserAuthBefore = countUserRole
    assertEquals(0, countUserBefore)
    assertEquals(0, countUserAuthBefore)
    createActivatedDataAccounts(setOf(defaultAccount, adminAccount))
    assertEquals(2, countAccount)
    assertEquals(3, countUserRole)
    findOneByEmail(defaultAccount.email!!).run {
        assertNotNull(this)
        assertEquals(defaultAccount.login, login)
    }
    findOneByEmail(adminAccount.email!!).run {
        assertNotNull(this)
        assertEquals(adminAccount.login, login)
    }
}

fun ByteArray.requestToString(): String = map {
    it.toInt().toChar().toString()
}.reduce { acc: String, s: String -> acc + s }

fun ApplicationContext.createDataAccounts(accounts: Set<Account>) {
    assertEquals(0, countAccount)
    assertEquals(0, countUserRole)
    accounts.map { acc ->
        User(acc.copy(
            langKey = DEFAULT_LANGUAGE,
            createdBy = SYSTEM_USER,
            createdDate = now(),
            lastModifiedBy = SYSTEM_USER,
            lastModifiedDate = now(),
            authorities = mutableSetOf(ROLE_USER).apply {
                if (acc.login == ADMIN) add(ROLE_ADMIN)
            }
        )).copy(activationKey = generateActivationKey).run {
            getBean<R2dbcEntityTemplate>().let {
                it.insert(this).block()!!.id!!.let { uuid ->
                    authorities!!.map { authority ->
                        it.insert(
                            UserRole(
                                userId = uuid,
                                role = authority.role
                            )
                        ).block()
                    }
                }
            }
        }
    }
    assertEquals(accounts.size, countAccount)
    assertTrue(accounts.size <= countUserRole)
}


val ByteArray.logBody: ByteArray
    get() = apply {
        if (isNotEmpty()) map { it.toInt().toChar().toString() }
            .reduce { request, s ->
                request + buildString {
                    append(s)
                    if (s == VIRGULE && request.last().isDigit())
                        append("\n\t")
                }
            }.replace("{\"", "\n{\n\t\"")
            .replace("\"}", "\"\n}")
            .replace("\",\"", "\",\n\t\"")
            .run { i("\nbody:$this") }
    }

val ByteArray.logBodyRaw: ByteArray
    get() = apply {
        if (isNotEmpty()) map {
            it.toInt()
                .toChar()
                .toString()
        }.reduce { request, s -> request + s }
            .run { i(this) }
    }


fun ApplicationContext.createActivatedDataAccounts(accounts: Set<Account>) {
    assertEquals(0, countAccount)
    assertEquals(0, countUserRole)
    accounts.map { acc ->
        User(
            acc.copy(
                langKey = DEFAULT_LANGUAGE,
                createdBy = SYSTEM_USER,
                createdDate = now(),
                lastModifiedBy = SYSTEM_USER,
                lastModifiedDate = now(),
                authorities = mutableSetOf(ROLE_USER).apply {
                    if (acc.login == ADMIN) add(ROLE_ADMIN)
                }.toSet()
            )
        ).run {
            getBean<R2dbcEntityTemplate>().let { dao ->
                dao.insert(this).block()!!.id!!.let { uuid ->
                    authorities!!.map { authority ->
                        dao.insert(
                            UserRole(
                                userId = uuid,
                                role = authority.role
                            )
                        ).block()
                    }
                }
            }
        }
    }
    assertEquals(accounts.size, countAccount)
    assertTrue(accounts.size <= countUserRole)
}

fun ApplicationContext.deleteAllAccounts() {
    deleteAllAccountAuthority()
    deleteAccounts()
    assertEquals(0, countAccount)
    assertEquals(0, countUserRole)
}

fun ApplicationContext.deleteAccounts() {
    getBean<R2dbcEntityTemplate>().delete(User::class.java).all().block()
}

fun ApplicationContext.deleteAllAccountAuthority() {
    getBean<R2dbcEntityTemplate>().delete(UserRole::class.java).all().block()
}

//TODO: revoir les updates avec id!=null
fun ApplicationContext.saveAccount(model: Account): Account? =
    getBean<R2dbcEntityTemplate>().run {
        when {
            model.id != null -> update(
                User(model).copy(
                    version = selectOne(
                        query(
                            where("login").`is`(model.login!!).ignoreCase(true)
                        ), User::class.java
                    ).block()!!.version
                )
            ).block()?.toAccount

            else -> insert(User(model)).block()?.toAccount
        }

    }

fun ApplicationContext.saveAccountAuthority(
    id: UUID,
    role: String,
): UserRole? = getBean<R2dbcEntityTemplate>()
    .insert(UserRole(userId = id, role = role))
    .block()


val ApplicationContext.countAccount: Int
    get() = getBean<R2dbcEntityTemplate>()
        .select(User::class.java)
        .count().block()?.toInt()!!


val ApplicationContext.countUserRole: Int
    get() = getBean<R2dbcEntityTemplate>()
        .select(UserRole::class.java)
        .count()
        .block()
        ?.toInt()!!


fun ApplicationContext.findOneByLogin(login: String): Account? =
    getBean<R2dbcEntityTemplate>()
        .select<User>()
        .matching(query(where("login").`is`(login).ignoreCase(true)))
        .one()
        .block()
        ?.toAccount

fun ApplicationContext.findOneByEmail(email: String): Account? =
    getBean<R2dbcEntityTemplate>()
        .select<User>()
        .matching(query(where("email").`is`(email).ignoreCase(true)))
        .one()
        .block()
        ?.toAccount

val ApplicationContext.findAllUserRoles: Set<UserRole>
    get() = getBean<R2dbcEntityTemplate>()
        .select(UserRole::class.java)
        .all()
        .toIterable()
        .toHashSet()

private val mapperFactory
    get() = ObjectMapper().apply {
        configure(WRITE_DURATIONS_AS_TIMESTAMPS, false)
        setSerializationInclusion(NON_EMPTY)
        registerModule(JavaTimeModule())
    }

/**
 * Convert an object to JSON byte array.
 *
 * @param object the object to convert.
 * @return the JSON byte array.
 * @throws IOException
 */
@Throws(IOException::class)
fun convertObjectToJsonBytes(`object`: Any): ByteArray = mapperFactory.writeValueAsBytes(`object`)

/**
 * Create a byte array with a specific size filled with specified data.
 *
 * @param size the size of the byte array.
 * @param data the data to put in the byte array.
 * @return the JSON byte array.
 */
fun createByteArray(size: Int, data: String) = ByteArray(size) { parseByte(data, 2) }

/**
 * A matcher that tests that the examined string represents the same instant as the reference datetime.
 */
class ZonedDateTimeMatcher(private val date: ZonedDateTime) : TypeSafeDiagnosingMatcher<String>() {

    override fun matchesSafely(item: String, mismatchDescription: Description): Boolean {
        try {
            if (!date.isEqual(parse(item))) {
                mismatchDescription.appendText("was ").appendValue(item)
                return false
            }
            return true
        } catch (e: DateTimeParseException) {
            mismatchDescription.appendText("was ")
                .appendValue(item)
                .appendText(", which could not be parsed as a ZonedDateTime")
            return false
        }
    }

    override fun describeTo(description: Description) {
        description.appendText("a String representing the same Instant as ")
            .appendValue(date)
    }
}

/**
 * Creates a matcher that matches when the examined string represents the same instant as the reference datetime.
 * @param date the reference datetime against which the examined string is checked.
 */
fun sameInstant(date: ZonedDateTime) = ZonedDateTimeMatcher(date)

/**
 * Verifies the equals/hashcode contract on the domain object.
 */
fun <T : Any> equalsVerifier(clazz: KClass<T>) {
    clazz.createInstance().apply i@{
        assertThat(toString()).isNotNull
        assertThat(this).isEqualTo(this)
        assertThat(hashCode()).isEqualTo(hashCode())
        // Test with an instance of another class
        assertThat(this).isNotEqualTo(Any())
        assertThat(this).isNotEqualTo(null)
        // Test with an instance of the same class
        clazz.createInstance().apply j@{
            assertThat(this@i).isNotEqualTo(this@j)
            // HashCodes are equals because the objects are not persisted yet
            assertThat(this@i.hashCode()).isEqualTo(this@j.hashCode())
        }
    }
}

val token64Zero
    get() = mutableListOf<String>().apply {
        repeat(64) { add(0.toString()) }
    }.reduce { acc, i -> "$acc$i" }
