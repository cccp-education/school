@file:Suppress(
    "SqlNoDataSourceInspection",
    "SqlResolve",
    "SqlDialectInspection", "unused"
)

package backend.tdd

import backend.AccountEntity
import backend.Constants.ROLE_ADMIN
import backend.Constants.ROLE_ANONYMOUS
import backend.Constants.ROLE_USER
import backend.data.Data.defaultAccount
import backend.data.Data.defaultAccountEntity
import backend.Log.log
import backend.AccountAuthority
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.allAndAwait
import org.springframework.data.r2dbc.core.awaitOneOrNull
import org.springframework.data.r2dbc.core.flow
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query.query
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.await
import org.springframework.r2dbc.core.awaitSingle
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.util.*
import java.util.regex.Pattern
import java.util.regex.Pattern.compile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import javax.inject.Inject

@SpringBootTest
@ActiveProfiles("test")
/* AbstractBaseFunctionalTest */
internal abstract class AbstractIntegrationTest {
    fun AccountEntity.unlockUser() {
        apply {
            if (id != null) {
                id = null
                version = null
            }
        }
    }

    companion object {
        val languages = arrayOf(
            "en",
            "fr",
            "de",
            "it",
            "es"
        )
        val PATTERN_LOCALE_3: Pattern = compile("([a-z]{2})-([a-zA-Z]{4})-([a-z]{2})")
        val PATTERN_LOCALE_2: Pattern = compile("([a-z]{2})-([a-z]{2})")

        val defaultRoles = arrayOf(
            ROLE_ADMIN,
            ROLE_USER,
            ROLE_ANONYMOUS
        )
    }

    @Inject
    lateinit var context: ApplicationContext

    @Inject
    lateinit var db: DatabaseClient

    @Inject
    lateinit var r2dbcEntityTemplate: R2dbcEntityTemplate

    @Inject
    lateinit var objectMapper: ObjectMapper

    suspend fun countAuthority(): Long = db
        .sql("SELECT COUNT(*) FROM `authority`")
        .fetch()
        .awaitSingle()
        .values
        .first() as Long

    suspend fun countUser(): Long = db
        .sql("SELECT COUNT(*) FROM `user`")
        .fetch()
        .awaitSingle()
        .values
        .first() as Long

    suspend fun countUserAuthority(): Long = db
        .sql("SELECT COUNT(*) FROM `user_authority`")
        .fetch()
        .awaitSingle()
        .values
        .first() as Long


    suspend fun deleteAllUserAuthorityByUserId(id: UUID) = db
        .sql("DELETE FROM user_authority WHERE user_id = :userId")
        .bind("userId", id)
        .await()


    suspend fun deleteAuthorityByRole(role: String): Unit = db
        .sql("delete from `authority` a where lower(a.role)=lower(:role)")
        .bind("role", role)
        .await()


    suspend fun deleteAllUserAuthorityByUserLogin(login: String): Unit = db
        .sql(
            "DELETE FROM user_authority WHERE user_id = " +
                    "(select u.id from User u where u.login=:login)"
        )
        .bind("login", login)
        .await()


    suspend fun deleteUserByIdWithAuthorities_(id: UUID) = db
        .sql("DELETE FROM user_authority WHERE user_id = :userId")
        .bind("userId", id)
        .await()
        .also {
            r2dbcEntityTemplate.delete(AccountEntity::class.java)
                .matching(query(where("id").`is`(id)))
                .allAndAwait()
        }

    suspend fun deleteAllUserAuthorities(): Unit = db
        .sql("DELETE FROM user_authority")
        .await()

    suspend fun deleteAllUsersWithoutUserAuthorites(): Unit = db
        .sql("DELETE FROM `user`")
        .await()

    suspend fun findAllUsers(): Flow<AccountEntity> = r2dbcEntityTemplate
        .select(AccountEntity::class.java)
        .flow<AccountEntity>()


    suspend fun deleteAllUsers() {
        findAllUsers().map {
            it.unlockUser()
        }.run {
            deleteAllUserAuthorities()
            deleteAllUsersWithoutUserAuthorites()
        }
    }

    suspend fun deleteUserByIdWithAuthorities(id: UUID) =
        deleteAllUserAuthorityByUserId(id).also {
            r2dbcEntityTemplate.delete(AccountEntity::class.java)
                .matching(query(where("id").`is`(id)))
                .allAndAwait()
        }

    suspend fun deleteUserByLoginWithAuthorities(login: String) =
        deleteAllUserAuthorityByUserLogin(login).also {
            r2dbcEntityTemplate.delete(AccountEntity::class.java)
                .matching(query(where("login").`is`(login)))
                .allAndAwait()
        }

    suspend fun logCountUser() = i("countUser: ${countUser()}")

    suspend fun logCountUserAuthority() = log
        .info("countUserAuthority: ${countUserAuthority()}")

    suspend fun saveUser(u: AccountEntity): AccountEntity? = r2dbcEntityTemplate
        .insert(u)
        .awaitFirstOrNull()

    suspend fun saveUserWithAutorities(user: AccountEntity): AccountEntity? = r2dbcEntityTemplate
        .insert(user)
        .awaitSingle().apply {
            authorities?.forEach {
                if (id != null)
                    r2dbcEntityTemplate
                        .insert(
                            AccountAuthority(
                                userId = id!!,
                                role = it.role
                            )
                        )
                        .awaitSingle()
            }
        }

    suspend fun findAllAuthorites(): Flow<AccountAuthority> = r2dbcEntityTemplate
        .select(AccountAuthority::class.java)
        .flow<AccountAuthority>()

    suspend fun findOneUserByEmail(email: String): AccountAuthority? = r2dbcEntityTemplate
        .select(AccountAuthority::class.java)
        .matching(query(where("email").`is`(email)))
        .awaitOneOrNull()

    suspend fun findOneUserByLogin(login: String): AccountAuthority? = r2dbcEntityTemplate
        .select(AccountAuthority::class.java)
        .matching(query(where("login").`is`(login)))
        .awaitOneOrNull()

    suspend fun findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(
        dateTime: LocalDateTime
    ): Flow<AccountAuthority> = r2dbcEntityTemplate
        .select(AccountAuthority::class.java)
        .matching(
            query(
                where("activated")
                    .`is`(false)
                    .and("activation_key")
                    .isNotNull
                    .and("created_date").lessThan(dateTime)
            )
        ).flow()

    fun checkProperty(
        property: String,
        value: String,
        injectedValue: String
    ) = property.apply {
        assertEquals(
            value,
            context.environment.getProperty(this)
        )
        assertEquals(
            injectedValue,
            context.environment.getProperty(this)
        )
    }

    suspend fun checkInitDatabaseWithDefaultUser(): AccountEntity =
        saveUserWithAutorities(
            defaultAccountEntity
                .copy()
                .apply {
                    unlockUser()
                    activated = true
                }.run {
                    deleteUserByLoginWithAuthorities(login!!)
                    return@run this
                }
        )?.apply {
            assertNotNull(id)
            assertTrue(activated)
            assertEquals(defaultAccount.email, email)
            assertEquals(defaultAccount.login, login)
        }!!

    suspend fun logUsers() {
        findAllUsers().apply {
            if (count() == 1 || count() == 0)
                i(single()::toString)
            else map { i(it::toString) }
        }
    }
}

internal class TestAbstractIntegrationTest : AbstractIntegrationTest() {


    @Test
    fun `test AbstractBaseSpringBootTest_saveUserWithAuthorities`(): Unit = runBlocking {
        val countUserBeforeSave = countUser()
//        assertEquals(0, countUserBeforeSave)
        val countUserAuthorityBeforeSave = countUserAuthority()
//        assertEquals(0, countUserAuthorityBeforeSave)
        defaultAccountEntity.copy().apply {
            unlockUser()
            val id = saveUserWithAutorities(this)?.id
            assertEquals(countUserBeforeSave + 1, countUser())
            assertEquals(countUserAuthorityBeforeSave + 1, countUserAuthority())
            when {
                id != null -> {
                    unlockUser()
                    deleteUserByIdWithAuthorities(id)
                }
            }
        }
        assertEquals(countUserBeforeSave, countUser())
        assertEquals(countUserAuthorityBeforeSave, countUserAuthority())
    }
}
