package school.base

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.context.MessageSource
import org.springframework.test.context.ActiveProfiles
import school.tdd.TestUtils.Data.OFFICIAL_SITE
import school.base.property.Properties
import school.base.property.DEVELOPMENT
import school.base.property.PRODUCTION
import school.base.property.STARTUP_LOG_MSG_KEY
import school.base.utils.i
import java.util.*
import java.util.Locale.FRENCH
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
                    FRENCH
                )
        )
    }


    @Test
    fun `ConfigurationsTests - MessageSource test message startupLog`() = context
        .getBean<MessageSource>()
        .getMessage(
            STARTUP_LOG_MSG_KEY,
            arrayOf(
                DEVELOPMENT,
                PRODUCTION
            ),
            Locale.getDefault()
        ).run {
            i(this)
            assertEquals(buildString {
                append("You have misconfigured your application!\n")
                append("It should not run with both the $DEVELOPMENT\n")
                append("and $PRODUCTION profiles at the same time.")
            }, this)
        }


    @Test
    fun `ConfigurationsTests - test go visit message`() =
        assertEquals(
            OFFICIAL_SITE,
            context.getBean<Properties>().goVisitMessage
        )
}
