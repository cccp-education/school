package backend.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import backend.config.Constants.PROP_MAIL_BASE_URL
import backend.config.Constants.PROP_MAIL_FROM
import backend.config.Constants.PROP_MAIL_HOST
import backend.config.Constants.PROP_MAIL_PASSWORD
import backend.config.Constants.PROP_MAIL_PORT
import backend.config.Constants.PROP_MAIL_PROPERTY_DEBUG
import backend.config.Constants.PROP_MAIL_PROPERTY_SMTP_AUTH
import backend.config.Constants.PROP_MAIL_PROPERTY_SMTP_STARTTLS_ENABLE
import backend.config.Constants.PROP_MAIL_PROPERTY_TRANSPORT_PROTOCOL
import backend.tdd.functional.AbstractBaseFunctionalTest
import kotlin.test.Test
import javax.inject.Inject
class MailPropertiesFuncTest : AbstractBaseFunctionalTest() {

    @Inject
    lateinit var properties: ApplicationProperties

    @Value("\${${PROP_MAIL_BASE_URL}}")
    lateinit var mailBaseUrl: String

    @Value("\${${PROP_MAIL_FROM}}")
    lateinit var mailFrom: String

    @Value("\${${PROP_MAIL_HOST}}")
    lateinit var mailHost: String

    @Value("\${${PROP_MAIL_PORT}}")
    var mailPort: Int = 0

    @Value("\${${PROP_MAIL_PASSWORD}}")
    lateinit var mailPassword: String

    @Value("\${${PROP_MAIL_PROPERTY_DEBUG}}")
    var mailPropertyDebug: Boolean = false

    @Value("\${${PROP_MAIL_PROPERTY_TRANSPORT_PROTOCOL}}")
    lateinit var mailPropertyTransportProtocol: String

    @Value("\${${PROP_MAIL_PROPERTY_SMTP_AUTH}}")
    var mailPropertySmtpAuth: Boolean = false

    @Value("\${${PROP_MAIL_PROPERTY_SMTP_STARTTLS_ENABLE}}")
    var mailPropertySmtpStarttlsEnable: Boolean = false

    @Test
    fun `Check property reaktive_mail_base-url`() {
        checkProperty(
            PROP_MAIL_BASE_URL,
            properties.mail.baseUrl,
            mailBaseUrl
        )
    }

    @Test
    fun `Check property reaktive_mail_from`() {
        checkProperty(
            PROP_MAIL_FROM,
            properties.mail.from,
            mailFrom
        )
    }

    @Test
    fun `Check property reaktive_mail_host`() {
        checkProperty(
            PROP_MAIL_HOST,
            properties.mail.host,
            mailHost
        )
    }

    @Test
    fun `Check property reaktive_mail_port`() {
        checkProperty(
            PROP_MAIL_PORT,
            value = "${properties.mail.port}",
            injectedValue = "$mailPort"
        )
    }

    @Test
    fun `Check property reaktive_mail_password`() {
        checkProperty(
            PROP_MAIL_PASSWORD,
            properties.mail.password,
            mailPassword
        )
    }

    @Test
    fun `Check property reaktive_mail_property_debug`() {
        checkProperty(
            PROP_MAIL_PROPERTY_DEBUG,
            "${properties.mail.property.debug}",
            "$mailPropertyDebug"
        )
    }

    @Test
    fun `Check property reaktive_mail_property_transport_protocol`() {
        checkProperty(
            PROP_MAIL_PROPERTY_TRANSPORT_PROTOCOL,
            properties.mail.property.transport.protocol,
            mailPropertyTransportProtocol
        )
    }

    @Test
    fun `Check property reaktive_mail_property_smtp_auth`() {
        checkProperty(
            PROP_MAIL_PROPERTY_SMTP_AUTH,
            "${properties.mail.property.smtp.auth}",
            "$mailPropertySmtpAuth"
        )
    }

    @Test
    fun `Check property reaktive_mail_property_smtp_starttls_enable`() {
        checkProperty(
            PROP_MAIL_PROPERTY_SMTP_STARTTLS_ENABLE,
            "${properties.mail.property.smtp.starttls.enable}",
            "$mailPropertySmtpStarttlsEnable"
        )
    }
}