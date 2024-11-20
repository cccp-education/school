package app

import app.utils.Constants
import app.utils.Properties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.context.MessageSource
import org.springframework.test.context.ActiveProfiles
import users.TestUtils
import workspace.Log
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest(properties = ["spring.main.web-application-type=reactive"])
@ActiveProfiles("test")
class ApplicationTests {

    @Autowired
    lateinit var context: ApplicationContext

    @Test
    fun `ConfigurationsTests - MessageSource test email_activation_greeting message fr`() = "artisan-logiciel".run {
        assertEquals(
            expected = "Cher $this",
            actual = context
                .getBean<MessageSource>()
                .getMessage(
                    "email.activation.greeting",
                    arrayOf(this),
                    Locale.FRENCH
                )
        )
    }


    @Test
    fun `ConfigurationsTests - MessageSource test message startupLog`() = context
        .getBean<MessageSource>()
        .getMessage(
            Constants.STARTUP_LOG_MSG_KEY,
            arrayOf(
                Constants.DEVELOPMENT,
                Constants.PRODUCTION
            ),
            Locale.getDefault()
        ).run {
            Log.i(this)
            assertEquals(buildString {
                append("You have misconfigured your application!\n")
                append("It should not run with both the ${Constants.DEVELOPMENT}\n")
                append("and ${Constants.PRODUCTION} profiles at the same time.")
            }, this)
        }


    @Test
    fun `ConfigurationsTests - test go visit message`() =
        assertEquals(
            TestUtils.Data.OFFICIAL_SITE,
            context.getBean<Properties>().goVisitMessage
        )
}