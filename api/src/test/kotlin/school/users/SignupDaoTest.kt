package school.users

import arrow.core.right
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles
import school.users.signup.Signup
import school.users.signup.Signup.UserActivation.UserActivationDao.Dao.signupAvailability
import kotlin.test.assertTrue

@ActiveProfiles("test")
@SpringBootTest(properties = ["spring.main.web-application-type=reactive"])
class SignupDaoTest {

    @Autowired
    lateinit var context: ApplicationContext

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
    fun `signupAvailability should return false for all when login and email are taken`() = runBlocking {
        val signup = Signup("existinguser", "password", "password", "existinguser@example.com")
        val expectedAvailability = Triple(false, false, false)
        val result = (signup to context).signupAvailability()
        assertEquals(expectedAvailability, result.right())
    }

    @Ignore
    @Test
    fun `signupAvailability should return correct availability when only email is taken`() = runBlocking {
        val signup = Signup("testuser", "password", "password", "existinguser@example.com")
        val expectedAvailability = Triple(false, false, true)
        val result = (signup to context).signupAvailability()
        assertEquals(expectedAvailability, result.right())
    }

    @Ignore
    @Test
    fun `signupAvailability should return correct availability when only login is taken`() = runBlocking {
        val signup = Signup("existinguser", "password", "password", "testuser@example.com")
        val expectedAvailability = Triple(false, true, false)
        val result = (signup to context).signupAvailability()
        assertEquals(expectedAvailability, result.right())
    }

    @Ignore
    @Test
    fun `signupAvailability should return left on database error`(): Unit = runBlocking {
        val signup = Signup("testuser", "password", "password", "testuser@example.com")
        assertThrows<Throwable> {
            (signup to context).signupAvailability()
        }
    }
}