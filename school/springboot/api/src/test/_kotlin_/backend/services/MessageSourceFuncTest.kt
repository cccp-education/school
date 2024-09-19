package backend.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import backend.config.Constants
import backend.Server.Log
import backend.tdd.functional.AbstractBaseFunctionalTest
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import javax.inject.Inject
class MessageSourceFuncTest : AbstractBaseFunctionalTest() {

    @Inject
    lateinit var messageSource: MessageSource

    @Test
    fun `test email_activation_greeting message fr`() {
        "Oliv".apply {
            assertEquals(
                expected = "Cher $this",
                actual = messageSource.getMessage(
                    "email.activation.greeting",
                    arrayOf(this),
                    Locale.FRENCH
                )
            )
        }
    }

    @Test
    fun `test message startupLog`() {
        val msg = "You have misconfigured your application!\n" +
                "It should not run with both the ${Constants.SPRING_PROFILE_DEVELOPMENT}\n" +
                "and ${Constants.SPRING_PROFILE_PRODUCTION} profiles at the same time."
        val i18nMsg = messageSource.getMessage(
            Constants.STARTUP_LOG_MSG_KEY,
            arrayOf(
                Constants.SPRING_PROFILE_DEVELOPMENT,
                Constants.SPRING_PROFILE_PRODUCTION
            ),
            Locale.getDefault()
        )
        assertTrue(msg == i18nMsg)
        Log.i(i18nMsg)
    }
}