package school.users

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.context.ActiveProfiles
import school.base.utils.Constants.EMPTY_STRING
import school.base.utils.Constants.SIGNUP_AVAILABLE
import school.base.utils.Constants.SIGNUP_EMAIL_NOT_AVAILABLE
import school.base.utils.Constants.SIGNUP_LOGIN_AND_EMAIL_NOT_AVAILABLE
import school.base.utils.Constants.SIGNUP_LOGIN_NOT_AVAILABLE
import school.tdd.TestUtils.Data.signup
import school.tdd.TestUtils.Data.user
import school.users.User.UserDao.Dao.countUsers
import school.users.User.UserDao.Dao.deleteAllUsersOnly
import school.users.User.UserDao.Dao.save
import school.users.signup.Signup
import school.users.signup.Signup.UserActivation.UserActivationDao.Dao.signupAvailability
import javax.inject.Inject
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ActiveProfiles("test")
@SpringBootTest(properties = ["spring.main.web-application-type=reactive"])
class SignupDaoTest {

    @Inject
    lateinit var context: ApplicationContext

    @AfterTest
    fun cleanUp() = runBlocking { context.deleteAllUsersOnly() }

    @Test
    fun `signupAvailability should return SIGNUP_AVAILABLE for all when login and email are available`() = runBlocking {
        (Signup(
            "testuser",
            "password",
            "password",
            "testuser@example.com"
        ) to context).signupAvailability().run {
            assertTrue(isRight())
            assertEquals(SIGNUP_AVAILABLE, getOrNull()!!)
        }
    }

    @Test
    fun `signupAvailability should return SIGNUP_NOT_AVAILABLE_AGAINST_LOGIN_AND_EMAIL for all when login and email are not available`(): Unit =
        runBlocking {
            assertEquals(0, context.countUsers())
            (user to context).save()
            assertEquals(1, context.countUsers())
            (signup to context).signupAvailability().run {
                assertEquals(
                    SIGNUP_LOGIN_AND_EMAIL_NOT_AVAILABLE,
                    getOrNull()!!
                )
            }
        }

    @Test
    fun `signupAvailability should return SIGNUP_EMAIL_NOT_AVAILABLE when only email is not available`() = runBlocking {
        assertEquals(0, context.countUsers())
        (user to context).save()
        assertEquals(1, context.countUsers())
        (Signup(
            "testuser",
            "password",
            "password",
            user.email
        ) to context).signupAvailability().run {
            assertEquals(SIGNUP_EMAIL_NOT_AVAILABLE, getOrNull()!!)
        }
    }

    @Test
    fun `signupAvailability should return SIGNUP_LOGIN_NOT_AVAILABLE when only login is not available`() = runBlocking {
        assertEquals(0, context.countUsers())
        (user to context).save()
        assertEquals(1, context.countUsers())

        (Signup(
            user.login,
            "password",
            "password",
            "testuser@example.com"
        ) to context).signupAvailability().run {
            assertEquals(SIGNUP_LOGIN_NOT_AVAILABLE, getOrNull()!!)
        }
    }
}
