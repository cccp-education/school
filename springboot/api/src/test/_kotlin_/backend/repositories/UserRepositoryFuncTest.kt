package backend.repositories

import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import backend.domain.DataTest.defaultUser
import backend.domain.DataTest.users
import backend.services.RandomUtils.generateActivationKey
import backend.services.RandomUtils.generateResetKey
import backend.tdd.functional.AbstractBaseFunctionalTest
import java.time.LocalDateTime.now
import java.time.ZoneId.systemDefault
import java.util.*
import kotlin.test.*
import javax.inject.Injectimport javax.inject.Inject
@Suppress("NonAsciiCharacters")
class UserRepositoryFuncTest : AbstractBaseFunctionalTest() {

    @Inject
    private lateinit var userRepository: UserRepository

    @BeforeTest
    fun init() = runBlocking {
        deleteAllUsers()
    }

    @Test
    fun `test count user`() = runBlocking {
        assertEquals(countUser(), userRepository.count())
    }

    @Test
    fun `test saveWithoutAuth user`(): Unit = runBlocking {
        defaultUser.copy().apply {
            countUser().apply countUserBeforeSave@{
                countUserAuthority().apply countUserAuthorityBeforeSave@{
                    assertNull(id)
                    assertNotNull(userRepository.saveWithoutAuth(this@apply).id)
                    assertNotNull(id)
                    assertEquals(
                        countUser(),
                        this@countUserBeforeSave + 1
                    )
                    assertEquals(
                        countUserAuthority(),
                        this@countUserAuthorityBeforeSave
                    )
                }
            }
        }
    }

    @Test
    fun `test save user`(): Unit = runBlocking {
        defaultUser.copy().apply {
            countUser().apply countUserBeforeSave@{
                countUserAuthority().apply countUserAuthorityBeforeSave@{
                    userRepository.save(this@apply)
                    assertEquals(
                        this@countUserBeforeSave + 1,
                        countUser()
                    )
                    assertEquals(
                        this@countUserAuthorityBeforeSave + 1,
                        countUserAuthority()
                    )
                }
            }
        }
    }

    @Test
    fun `test findOneWithAuthoritiesByLogin user login`(): Unit = runBlocking {
        defaultUser.copy().apply {
            saveUserWithAutorities(this)!!.id.apply {
                assertNotNull(this)
                assertEquals(
                    userRepository.findOneWithAuthoritiesByLogin(login!!)?.id,
                    this
                )
                assertTrue(
                    findAllAuthorites()
                        .filter {
                            it.userId == this
                        }.count() != 0
                )
            }
        }
    }

    @Test
    fun `test findOneWithAuthoritiesByEmail user email`(): Unit = runBlocking {
        defaultUser.copy().apply {
            saveUserWithAutorities(defaultUser)!!.id.apply {
                assertNotNull(this)
                assertEquals(
                    userRepository
                        .findOneWithAuthoritiesByEmail(email!!)
                        ?.id,
                    this
                )
                assertTrue(
                    findAllAuthorites()
                        .filter {
                            it.userId == this
                        }.count() != 0
                )
            }
        }
    }


    @Test
    fun `test delete user`(): Unit = runBlocking {
        defaultUser.copy().apply {
            saveUserWithAutorities(this)!!.id.apply {
                assertNotNull(this)
            }
            countUser()
                .apply countUserBeforeDelete@{
                    countUserAuthority()
                        .apply countUserAuthorityBeforeDelete@{
                            userRepository.delete(this@apply)
                            assertEquals(
                                countUser(),
                                this@countUserBeforeDelete - 1
                            )
                            assertEquals(
                                countUserAuthority(),
                                this@countUserAuthorityBeforeDelete - 1
                            )
                        }
                }
        }
    }

    @Test
    fun `test deleteAll`(): Unit = runBlocking {
        defaultUser.copy().apply {
            saveUserWithAutorities(this)!!.id.apply {
                assertNotNull(this)
            }
            assertTrue(countUser() > 0L)
            assertTrue(countUserAuthority() > 0L)
            userRepository.deleteAll()
            assertTrue(countUser() == 0L)
            assertTrue(countUserAuthority() == 0L)
        }
    }

    @Test
    fun `test findOneByActivationKey user activationKey`(): Unit = runBlocking {
        defaultUser.copy().apply {
            activationKey = generateActivationKey
            saveUserWithAutorities(user = this)!!.id.apply {
                assertNotNull(actual = this)
                assertEquals(
                    expected = userRepository
                        .findOneByActivationKey(activationKey!!)?.id,
                    actual = this
                )
            }
        }
    }

    @Test
    fun `test findOneByResetKey user resetKey`(): Unit = runBlocking {
        defaultUser.copy().apply {
            resetKey = generateResetKey
            saveUserWithAutorities(this)!!.id.apply {
                assertNotNull(this)
                assertEquals(
                    userRepository.findOneByResetKey(resetKey!!)!!.id,
                    this
                )
            }
        }
    }

    @Test
    fun `test findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore créé il y a un an`(): Unit =
        runBlocking {
            mutableListOf<UUID>().apply {
                users.forEach {
                    add(saveUserWithAutorities(it.copy().apply {
                        activationKey = generateActivationKey
                        now().apply now@{
                            createdDate = this@now.toInstant(
                                systemDefault()
                                    .rules
                                    .getOffset(this@now)
                            )
                        }
                    })!!.id!!)
                }
                assertEquals(
                    userRepository
                        .findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(
                            now().minusYears(1)
                        ).toList(mutableListOf()).size,
                    0
                )
            }
        }

    @Test
    fun `test findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore créé dans un an`(): Unit =
        runBlocking {
            mutableListOf<UUID>().apply {
                users.forEach {
                    add(saveUserWithAutorities(it.copy().apply {
                        activationKey = generateActivationKey
                        now().plusYears(1).apply now@{
                            createdDate = this@now.toInstant(
                                systemDefault()
                                    .rules
                                    .getOffset(this@now)
                            )
                        }
                    })!!.id!!)
                }
                assertEquals(
                    userRepository
                        .findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(
                            now().plusYears(1)
                        ).toList(mutableListOf())
                        .size,
                    size
                )
            }
        }

    @Test
    fun `test findAllWithAuthorities unpaged`(): Unit = runBlocking {
        mutableListOf<UUID>().apply {
            users.forEach {
                add(saveUserWithAutorities(it.copy())!!.id!!)
            }
            assertEquals(
                userRepository
                    .findAllWithAuthorities(Pageable.unpaged())
                    .toList(mutableListOf())
                    .size,
                size
            )
        }
    }
}