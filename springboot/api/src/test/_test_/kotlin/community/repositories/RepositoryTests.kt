@file:Suppress("NonAsciiCharacters")

package community.repositories

import community.*
import community.accounts.Account.Companion.LOGIN_FIELD
import community.accounts.AccountRepository
import community.accounts.User
import community.accounts.signup.Signup
import community.security.generateActivationKey
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.getBean
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query.query
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
@kotlin.test.Ignore
internal class AccountRepositoryR2dbcTest {
    private lateinit var context: ConfigurableApplicationContext
    private val repository: AccountRepository by lazy { context.getBean() }

    @BeforeAll
    fun `lance le server en profile test`() {
        context = launcher()
    }

    @AfterAll
    fun `arrête le serveur`() = context.close()


    @AfterEach
    fun tearDown() = context.deleteAllAccounts()


    @Test
    fun test_save() {
        mono {
            val countBefore = context.countAccount
            assertEquals(0, countBefore)
            repository.save(defaultAccount)
            assertEquals(countBefore + 1, context.countAccount)
        }
    }

    @Test
    fun test_delete() = runBlocking {
        assertEquals(0, context.countAccount)
        context.createDataAccounts(threeAccounts.toList().toSet())
        assertEquals(threeAccounts.toList().size, context.countAccount)
        assertEquals(threeAccounts.toList().size + 1, context.countUserRole)
        repository.delete(defaultAccount)
        assertEquals(threeAccounts.toList().size - 1, context.countAccount)
        assertEquals(threeAccounts.toList().size, context.countUserRole)
    }

    @Test
    fun test_findOne_with_Email() = runBlocking {
        assertEquals(0, context.countAccount)
        context.createDataAccounts(threeAccounts.toList().toSet())
        assertEquals(threeAccounts.toList().size, context.countAccount)
        assertEquals(
            defaultAccount.login,
            repository.findOne(defaultAccount.email!!)!!.first.login
        )
    }

    @Test
    fun test_findOne_with_Login() = runBlocking {
        assertEquals(0, context.countAccount)
        context.createDataAccounts(threeAccounts.toList().toSet())
        assertEquals(threeAccounts.toList().size, context.countAccount)
        assertEquals(
            defaultAccount.email,
            repository.findOne(defaultAccount.login!!)!!.first.email
        )
    }

    @Test
    fun test_save_signup() {
        assertEquals(0, context.countAccount)
        assertEquals(0, context.countUserRole)
        runBlocking {
            repository.save(
                Signup(
                    account = defaultAccount.copy(
                        langKey = DEFAULT_LANGUAGE,
                        createdDate = Instant.now(),
                    ),
                    password = defaultAccount.login,
                    repass = defaultAccount.login,
                    activationKey = generateActivationKey
                )
            )
        }
        assertEquals(1, context.countAccount)
        assertEquals(1, context.countUserRole)
    }

    @Test
    fun test_findActivationKeyByLogin() {
        assertEquals(0, context.countAccount)
        context.createDataAccounts(threeAccounts.toList().toSet())
        assertEquals(threeAccounts.toList().size, context.countAccount)
        assertEquals(threeAccounts.toList().size + 1, context.countUserRole)
        runBlocking {
            assertEquals(
                context.getBean<R2dbcEntityTemplate>().selectOne(
                    query(
                        where("email").`is`(defaultAccount.email!!).ignoreCase(true)
                    ), User::class.java
                ).block()!!.activationKey,
                repository.findActivationKeyByLogin(defaultAccount.login!!)
            )
        }
    }

    @Test
    fun test_findOneByActivationKey() {
        assertEquals(0, context.countAccount)
        context.createDataAccounts(threeAccounts.toList().toSet())
        assertEquals(threeAccounts.toList().size, context.countAccount)
        assertEquals(threeAccounts.toList().size + 1, context.countUserRole)
        context.findOneByLogin(defaultAccount.login!!).run findOneByLogin@{
            assertNotNull(this@findOneByLogin)
            val activationKey = context.getBean<R2dbcEntityTemplate>().selectOne(
                query(
                    where(LOGIN_FIELD).`is`(defaultAccount.login!!).ignoreCase(true)
                ), User::class.java
            ).block()!!.activationKey
            assertNotNull(activationKey)
            runBlocking {
                repository.findOneByActivationKey(activationKey)
                    .run findOneByActivationKey@{
                        assertNotNull(this@findOneByActivationKey)
                        assertNotNull(this@findOneByActivationKey.id)
                        assertEquals(
                            this@findOneByLogin.id,
                            this@findOneByActivationKey.id
                        )
                    }
            }
        }
    }
}