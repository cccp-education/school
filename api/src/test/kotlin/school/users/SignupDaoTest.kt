package school.users

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles
import school.tdd.TestUtils.Data.user
import school.users.User.UserDao.Dao.countUsers
import school.users.User.UserDao.Dao.deleteAllUsersOnly
import school.users.User.UserDao.Dao.save
import school.users.signup.Signup
import school.users.signup.Signup.UserActivation.UserActivationDao.Dao.signupAvailability
import kotlin.test.*

@ActiveProfiles("test")
@SpringBootTest(properties = ["spring.main.web-application-type=reactive"])
class SignupDaoTest {

    @Autowired
    lateinit var context: ApplicationContext

    @AfterTest
    fun cleanUp() = runBlocking { context.deleteAllUsersOnly() }

    @Test
    fun `signupAvailability should return true for all when login and email are available`() = runBlocking {
        val signup = Signup("testuser", "password", "password", "testuser@example.com")
        val expectedAvailability = Triple(true, true, true)
        val result = (signup to context).signupAvailability()
        assertTrue(result.isRight())
        assertEquals(expectedAvailability, result.getOrNull()!!)
    }

    @Ignore
    @Test
    fun `signupAvailability should return false for all when login and email are taken`(): Unit = runBlocking {
        assertEquals(0, context.countUsers())
        (user to context).save()
        assertEquals(1, context.countUsers())

        val signup = Signup(user.login, "password", "password", "notexistinguser@example.com")
        val expectedAvailability = Triple(false, false, false)
        val result = (signup to context).signupAvailability()
        assertEquals(expectedAvailability, result.getOrNull()!!)
    }

    @Ignore
    @Test
    fun `signupAvailability should return correct availability when only email is taken`() = runBlocking {
        assertEquals(0, context.countUsers())
        (user to context).save()
        assertEquals(1, context.countUsers())

        val signup = Signup("testuser", "password", "password", user.email)
        val expectedAvailability = Triple(false, false, true)
        val result = (signup to context).signupAvailability()
        assertEquals(expectedAvailability, result.getOrNull()!!)
    }

    @Ignore
    @Test
    fun `signupAvailability should return correct availability when only login is taken`() = runBlocking {
        assertEquals(0, context.countUsers())
        (user to context).save()
        assertEquals(1, context.countUsers())

        val signup = Signup("existinguser", "password", "password", "testuser@example.com")
        val expectedAvailability = Triple(false, true, false)
        val result = (signup to context).signupAvailability()
        assertEquals(expectedAvailability, result.getOrNull()!!)
    }

    @Ignore
    @Test
    fun `signupAvailability should return left on database error`(): Unit = runBlocking {
        assertEquals(0, context.countUsers())
        (user to context).save()
        assertEquals(1, context.countUsers())

        val signup = Signup("testuser", "password", "password", "testuser@example.com")
        assertThrows<Throwable> {
            (signup to context).signupAvailability()
        }
    }
}