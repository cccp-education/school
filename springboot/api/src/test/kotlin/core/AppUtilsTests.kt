@file:Suppress("JUnitMalformedDeclaration")

package core

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles
import webapp.Application
import webapp.TestUtils.Data.user
import webapp.core.utils.AppUtils.cleanField
import webapp.core.utils.AppUtils.toJson
import webapp.core.utils.i
import kotlin.test.Test
import kotlin.test.assertEquals


@SpringBootTest(
    properties = ["spring.main.web-application-type=reactive"],
    classes = [Application::class]
)
@ActiveProfiles("test")
class AppUtilsTests {

    @Autowired
    lateinit var context: ApplicationContext
    val mapper: ObjectMapper by lazy { context.getBean() }

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
}