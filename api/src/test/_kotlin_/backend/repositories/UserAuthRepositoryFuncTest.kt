package backend.repositories

import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import backend.config.Constants.ROLE_USER
import backend.domain.DataTest.defaultUser
import backend.repositories.entities.UserAuthority
import backend.tdd.functional.AbstractBaseFunctionalTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import javax.inject.Inject
class UserAuthRepositoryFuncTest : AbstractBaseFunctionalTest() {

    @Inject
    private lateinit var userAuthRepository: UserAuthRepository

    @BeforeTest
    fun init() = runBlocking {
        deleteAllUsers()
    }

    @Test
    fun `test saveUserAuthority`(): Unit = runBlocking {
        defaultUser.copy().apply {
            saveUser(this)!!.id.apply id@{
                assertNotNull(this@id)
                countUserAuthority().apply countBeforeSave@{
                    userAuthRepository.save(
                        UserAuthority(
                            userId = this@id,
                            role = ROLE_USER
                        )
                    )
                    assertEquals(
                        countUserAuthority(),
                        this@countBeforeSave + 1
                    )
                }
            }
        }
    }
}