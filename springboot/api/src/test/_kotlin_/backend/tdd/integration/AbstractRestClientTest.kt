@file:Suppress(
    "NonAsciiCharacters",
    "SpellCheckingInspection",
    "SqlDialectInspection",
    "SqlNoDataSourceInspection"
)

package backend.tdd.integration

import backend.Server
import backend.Server.Log.log
import backend.config.Constants.DEV_HOST
import backend.domain.DataTest
import backend.domain.DataTest.defaultUser
import backend.domain.unlockUser
import backend.repositories.entities.User
import backend.repositories.entities.UserAuthority
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.getBean
import org.springframework.boot.runApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.allAndAwait
import org.springframework.data.r2dbc.core.flow
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query.query
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.await
import org.springframework.r2dbc.core.awaitSingle
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.bindToServer
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

abstract class AbstractRestClientTest {
    lateinit var context: ConfigurableApplicationContext
    private val db: DatabaseClient by lazy { context.getBean() }
    private val r2dbcEntityTemplate: R2dbcEntityTemplate by lazy { context.getBean() }
    val client: WebTestClient by lazy {
        bindToServer()
            .baseUrl("http://$DEV_HOST:8080")
            .build()
    }

    @BeforeTest
    fun init() = runBlocking { deleteAllUsers() }


    @BeforeAll
    @Suppress("unused")
    fun `lance le server en profile test`() {
        context = runApplication<Server> {
            testLoader(app = this)
        }
    }

    @AfterAll
    @Suppress("unused")
    fun `arrête le serveur`() {
        context.close()
    }


    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun deleteAllUserAuthorityByUserLogin(login: String) = db
        .sql(
            "DELETE FROM user_authority WHERE user_id = (select u.id from User u where lower(u.login)=lower(:login))"
        )
        .bind("login", login)
        .await()

    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun saveUserWithAutorities(u: User): User? = r2dbcEntityTemplate
        .insert(u)
        .awaitSingle().apply {
            authorities?.forEach {
                if (id != null)
                    r2dbcEntityTemplate
                        .insert(
                            UserAuthority(
                                userId = id!!,
                                role = it.role
                            )
                        )
                        .awaitSingle()
            }
        }

    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun deleteUserByLoginWithAuthorities(login: String) =
        deleteAllUserAuthorityByUserLogin(login).also {
            r2dbcEntityTemplate.delete(User::class.java)
                .matching(query(where("login").`is`(login)))
                .allAndAwait()
        }

    suspend fun checkInitDatabaseWithDefaultUser(): User =
        saveUserWithAutorities(
            defaultUser.copy().apply {
                unlockUser()
                activated = true
                password = context.getBean<PasswordEncoder>().encode(password)
            }.run {
                deleteUserByLoginWithAuthorities(login!!)
                return@run this
            }
        )?.apply {
            assertNotNull(id)
            assertTrue(activated)
            assertEquals(DataTest.defaultAccount.email, email)
            assertEquals(DataTest.defaultAccount.login, login)
        }!!

    @Suppress("MemberVisibilityCanBePrivate")
    fun findAllUsers(): Flow<User> = r2dbcEntityTemplate
        .select(User::class.java)
        .flow<User>()

    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun deleteAllUserAuthorities(): Unit = db
        .sql("DELETE FROM user_authority")
        .await()

    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun deleteAllUsersWithoutUserAuthorites(): Unit = db
        .sql("DELETE FROM `user`")
        .await()

    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun deleteAllUsers() {
        findAllUsers().map {
            it.unlockUser()
        }.run {
            deleteAllUserAuthorities()
            deleteAllUsersWithoutUserAuthorites()
        }
    }

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

    suspend fun logCountUser() = log
        .info("countUser: ${countUser()}")

    suspend fun logCountUserAuthority() = log
        .info("countUserAuthority: ${countUserAuthority()}")
}