package backend.services

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import backend.config.Constants
import backend.domain.DataTest
import backend.domain.DataTest.USER_LOGIN
import backend.domain.DataTest.defaultAccount
import backend.domain.DataTest.defaultUser
import backend.domain.DataTest.userTest1
import backend.domain.DataTest.userTest2
import backend.repositories.entities.Authority
import backend.repositories.entities.User
import backend.services.exceptions.EmailAlreadyUsedException
import backend.services.exceptions.UsernameAlreadyUsedException
import backend.tdd.functional.AbstractBaseFunctionalTest
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.time.temporal.ChronoUnit.HOURS
import kotlin.test.*
import javax.inject.Inject
/**
 * Functional tests for {@link UserService}.
 */
@Suppress("NonAsciiCharacters")
class UserServiceFuncTest : AbstractBaseFunctionalTest() {
    @Inject
    private lateinit var userService: UserService

    @BeforeTest
    fun init() = runBlocking { deleteAllUsers() }


    @Test
    fun `test register account avec un login existant et activated est vrai, doit lancer l'exception UsernameAlreadyUsedException`(): Unit =
        runBlocking {
            saveUserWithAutorities(defaultUser.copy(activated = true))?.apply {
                assertNotNull(id)
                assertTrue(activated)
                assertEquals(defaultAccount.login, login)
                countUser().apply countUser@{
                    assertFailsWith<UsernameAlreadyUsedException> {
                        userService.register(defaultAccount, defaultAccount.password!!)
                    }
                    assertEquals(this@countUser, countUser())
                }
            }
        }

    @Test
    fun `test register account avec un email existant, un login inexistant et activated est vrai, doit lancer l'exception EmailAlreadyUsedException`(): Unit =
        runBlocking {
            saveUserWithAutorities(
                defaultUser.copy(
                login = defaultUser.login!!.reversed(),
                activated = true
            ).apply {
                assertNull(findOneUserByLogin(login!!))
            })?.apply {
                assertNotNull(id)
                assertTrue(activated)
                assertEquals(defaultAccount.email, email)
                assertNotEquals(defaultAccount.login, login)
                countUser().apply countUser@{
                    assertFailsWith<EmailAlreadyUsedException> {
                        userService.register(defaultAccount, defaultAccount.password!!)
                    }
                    assertEquals(this@countUser, countUser())
                }
            }
        }

    @Test
    fun `test register account avec un email inexistant, un login inexistant et activated est faux`(): Unit =
        runBlocking {
            countUser().apply countUserBeforeRegister@{
                countUserAuthority().apply countUserAuthorityBeforeRegister@{
                    assertNull(findOneUserByLogin(defaultUser.login!!))
                    assertNull(findOneUserByEmail(defaultUser.email!!))
                    assertEquals(
                        expected = defaultUser.login,
                        actual = defaultAccount.login
                    )
                    assertEquals(defaultUser.email, defaultAccount.email)
                    assertFalse(defaultAccount.activated)
                    assertFalse(
                        userService.register(
                            defaultAccount,
                            defaultAccount.password!!
                        )!!.activated
                    )
                    assertEquals(
                        expected = countUser(),
                        actual = this@countUserBeforeRegister + 1
                    )
                    assertEquals(
                        expected = countUserAuthority(),
                        actual = this@countUserAuthorityBeforeRegister + 1
                    )
                }
            }
        }

    @Test
    @WithMockUser(USER_LOGIN)
    fun `test email non inscrit ne peut reset un password`(): Unit = runBlocking {
        checkInitDatabaseWithDefaultUser()
        assertNull(userService.requestPasswordReset("invalid.login@localhost"))
    }

    @Test
    @WithMockUser(USER_LOGIN)
    fun `test email inscrit et activé peut reset un password`(): Unit = runBlocking {
        checkInitDatabaseWithDefaultUser()
        findOneUserByEmail(defaultUser.email!!)!!.apply {
            assertNull(resetDate)
            assertNull(resetKey)
            assertNotNull(id)
        }
        userService.requestPasswordReset(defaultUser.email!!).apply {
            assertNotNull(this)
            assertEquals(email, defaultUser.email)
            assertNotNull(resetDate)
            assertNotNull(resetKey)
        }
    }

    @Test
    @WithMockUser(USER_LOGIN)
    fun `test email inscrit et non activé ne peut pas reset un password`(): Unit = runBlocking {
        saveUserWithAutorities(defaultUser.copy().apply {
            activated = false
        })?.apply {
            assertFalse(activated)
            assertNull(resetKey)
        }
        assertNull(userService.requestPasswordReset(defaultUser.email!!))
        findOneUserByEmail(defaultUser.email!!)!!.apply {
            assertNull(resetKey)
            assertFalse(activated)
        }
    }

    @Test
    @WithMockUser(USER_LOGIN)
    fun `test resetKey d'un user activé ne doit pas avoir plus de 24 heures`(): Unit = runBlocking {
        RandomUtils.generateResetKey.apply {
            saveUserWithAutorities(
                defaultUser.copy(
                    activated = true,
                    resetDate = Instant.now().minus(25, HOURS),
                    resetKey = this
                )
            )
            assertNotEquals(defaultUser.password, userTest2.password)
            assertNull(userService.completePasswordReset(userTest2.password!!, this))
        }
    }

    @Test
    @WithMockUser(USER_LOGIN)
    fun `test resetKey doit être valide`(): Unit = runBlocking {
        "InvalidResetKey".apply {
            saveUserWithAutorities(
                defaultUser.copy(
                    activated = true,
                    resetDate = Instant.now().minus(25, HOURS),
                    resetKey = this
                )
            )
            assertNotEquals(defaultUser.password, userTest1.password)
            assertNull(userService.completePasswordReset(userTest1.password!!, this))
        }
    }

    @Test
    @WithMockUser(USER_LOGIN)
    fun `test un user peut reset password`(): Unit = runBlocking {
        assertEquals(countUser(), 0)
        assertEquals(countUserAuthority(), 0)
        assertTrue(defaultUser.authorities!!.contains(Authority(Constants.ROLE_USER)))
        RandomUtils.generateResetKey.apply key@{
            saveUserWithAutorities(
                defaultUser.copy(
                    resetDate = Instant.now().minus(2, HOURS),
                    resetKey = this
                )
            ).apply user@{
                assertNotNull(this@user)
                assertNotNull(resetDate)
                assertNotNull(resetKey)
                assertEquals(defaultUser.password, password)
                assertTrue(this@user.authorities!!.contains(Authority(Constants.ROLE_USER)))
                userService
                    .completePasswordReset(userTest1.password!!, this@key)
                    .apply result@{
                        assertNotNull(this@result)
                        assertNull(resetDate)
                        assertNull(resetKey)
                        assertNotEquals(defaultUser.password!!, password)
                    }
            }
        }
        assertEquals(countUser(), 1)
        assertEquals(countUserAuthority(), 1)
    }

    @Test
    @WithMockUser(USER_LOGIN)
    fun `test un user non activé avec une activationKey de plus de 3jours est détruit`(): Unit = runBlocking {
        assertEquals(countUser(), 0)
        assertEquals(countUserAuthority(), 0)
        assertTrue(defaultUser.authorities!!.contains(Authority(Constants.ROLE_USER)))
        RandomUtils.generateActivationKey.apply key@{
            Instant.now().minus(4, ChronoUnit.DAYS).apply fourDaysAgo@{
                saveUserWithAutorities(
                    defaultUser.copy(
                        activated = false,
                        createdDate = this,
                        activationKey = this@key
                    )
                ).apply user@{
                    assertEquals(countUser(), 1)
                    assertEquals(countUserAuthority(), 1)
                    assertNotNull(this)
                    assertNotNull(createdDate)
                    assertNotNull(activationKey)
                    assertEquals(defaultUser.password, password)
                    assertTrue(authorities!!.contains(Authority(Constants.ROLE_USER)))
                    LocalDateTime.ofInstant(Instant.now().minus(3, ChronoUnit.DAYS), ZoneOffset.UTC)
                        .apply threeDaysAgo@{
                            mutableListOf<User>().apply {
                                findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(
                                    this@threeDaysAgo
                                ).map { add(it) }
                                    .collect()
                                assertTrue(isNotEmpty())
                            }
                            userService.removeNotActivatedUsers()
                            mutableListOf<User>().apply {
                                findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(
                                    this@threeDaysAgo
                                ).map { add(it) }
                                    .collect()
                                assertTrue(isEmpty())
                            }
                        }
                }
            }
        }
        assertEquals(countUser(), 0)
        assertEquals(countUserAuthority(), 0)
    }

    @Test
    @WithMockUser(USER_LOGIN)
    fun `test un user créé depuis plus de 3jours et non activé avec une activationKey null n'est pas détruit`(): Unit =
        runBlocking {
            assertEquals(countUser(), 0)
            assertEquals(countUserAuthority(), 0)
            assertTrue(defaultUser.authorities!!.contains(Authority(Constants.ROLE_USER)))
            Instant.now().minus(4, ChronoUnit.DAYS).apply {
                saveUserWithAutorities(
                    defaultUser.copy(
                        activated = false,
                        createdDate = this
                    )
                ).apply {
                    assertNotNull(actual = this)
                    assertNotNull(actual = createdDate)
                    assertNull(actual = activationKey)
                    assertEquals(expected = defaultUser.password, actual = password)
                    assertTrue(actual = authorities!!.contains(Authority(Constants.ROLE_USER)))
                    LocalDateTime.ofInstant(Instant.now().minus(3, ChronoUnit.DAYS), ZoneOffset.UTC)
                        .apply threeDaysAgo@{
                            mutableListOf<User>().apply {
                                findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(
                                    dateTime = this@threeDaysAgo
                                ).map { add(element = it) }.collect()
                                assertTrue(isEmpty())
                            }
                        }
                }
            }
            userService.removeNotActivatedUsers()
            assertEquals(countUser(), actual = 1)
            assertEquals(countUserAuthority(), actual = 1)
        }
}