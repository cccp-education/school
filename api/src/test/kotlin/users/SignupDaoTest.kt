package users

import app.Application
import app.database.EntityModel.Companion.MODEL_FIELD_FIELD
import app.database.EntityModel.Companion.MODEL_FIELD_MESSAGE
import app.database.EntityModel.Companion.MODEL_FIELD_OBJECTNAME
import jakarta.validation.Validator
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles
import users.TestUtils.Data.signup
import users.TestUtils.Data.user
import users.Signup.Companion.objectName
import users.dao.UserDao.Attributes.EMAIL_ATTR
import users.dao.UserDao.Attributes.LOGIN_ATTR
import users.dao.UserDao.Attributes.PASSWORD_ATTR
import users.dao.UserDao.Dao.countUsers
import users.dao.UserDao.Dao.deleteAllUsersOnly
import users.dao.UserDao.Dao.save
import users.dao.UserDao.Dao.signupAvailability
import users.signup.SignupService.Companion.SIGNUP_AVAILABLE
import users.signup.SignupService.Companion.SIGNUP_EMAIL_NOT_AVAILABLE
import users.signup.SignupService.Companion.SIGNUP_LOGIN_AND_EMAIL_NOT_AVAILABLE
import users.signup.SignupService.Companion.SIGNUP_LOGIN_NOT_AVAILABLE
import javax.inject.Inject
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ActiveProfiles("test")
@SpringBootTest(
    classes = [Application::class],properties = ["spring.main.web-application-type=reactive"])
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
            isRight().run(::assertTrue)
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

    @Test
    fun `check signup validate implementation`() {
        setOf(PASSWORD_ATTR, EMAIL_ATTR, LOGIN_ATTR)
            .map { it to context.getBean<Validator>().validateProperty(signup, it) }
            .flatMap { (first, second) ->
                second.map {
                    mapOf<String, String?>(
                        MODEL_FIELD_OBJECTNAME to objectName,
                        MODEL_FIELD_FIELD to first,
                        MODEL_FIELD_MESSAGE to it.message
                    )
                }
            }.toSet()
            .apply { run(::isEmpty).let(::assertTrue) }
    }
}
