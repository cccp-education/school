@file:Suppress("JUnitMalformedDeclaration")

package users

import app.Application
import users.UserDao.Dao.deleteAllUsersOnly
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.validation.Validator
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles
import app.utils.AppUtils.cleanField
import app.utils.AppUtils.toJson
import users.TestUtils.Data.user
import workspace.Log.i
import javax.inject.Inject
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals


@SpringBootTest(
    classes = [Application::class],
    properties = ["spring.main.web-application-type=reactive"])
@ActiveProfiles("test")
class UserTests {

    @Inject
    lateinit var context: ApplicationContext
    val mapper: ObjectMapper by lazy { context.getBean() }
    val validator: Validator by lazy { context.getBean() }

    @AfterTest
    fun cleanUp() = runBlocking { context.deleteAllUsersOnly() }

    @Test
    fun `display user formatted in JSON`() = assertDoesNotThrow {
        (user to context).toJson.let(::i)
    }

    @Test
    fun `check toJson build a valid json format`(): Unit = assertDoesNotThrow {
        (user to context).toJson.let(mapper::readTree)
    }

    @Test
    fun `test cleanField extension function`() = assertEquals(
        "login",
        "`login`".cleanField(),
        "Backtick should be removed"
    )
    //TODO : Write and/or Test toMap, toUser
}