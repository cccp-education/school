package users

//@file:Suppress(
//    "NonAsciiCharacters",
//    "MemberVisibilityCanBePrivate",
//    "PropertyName"
//)
//
//package school.mail
//
//import jakarta.mail.Multipart
//import jakarta.mail.internet.MimeBodyPart
//import jakarta.mail.internet.MimeMessage
//import jakarta.mail.internet.MimeMultipart
//import org.assertj.base.api.Assertions
//import org.assertj.base.api.Assertions.assertThat
//import org.junit.jupiter.api.AfterAll
//import org.junit.jupiter.api.BeforeAll
//import org.junit.jupiter.api.BeforeEach
//import org.mockito.ArgumentCaptor
//import org.mockito.ArgumentMatchers.any
//import org.mockito.Captor
//import org.mockito.Mockito
//import org.mockito.Mockito.*
//import org.mockito.MockitoAnnotations.openMocks
//import org.mockito.Spy
//import org.springframework.beans.factory.getBean
//import org.springframework.context.ConfigurableApplicationContext
//import org.springframework.context.MessageSource
//import org.springframework.mail.MailSendException
//import org.springframework.mail.javamail.JavaMailSenderImpl
//import org.thymeleaf.spring6.SpringWebFluxTemplateEngine
//import school.accounts.models.AccountCredentials
//import school.accounts.models.AccountUtils.generateResetKey
//import school.base.mail.MailService
//import school.base.mail.MailServiceSmtp
//import school.base.property.DEFAULT_LANGUAGE
//import school.base.property.GMAIL
//import school.launcher
//import java.io.ByteArrayOutputStream
//import java.io.File
//import java.io.FileInputStream
//import java.io.InputStreamReader
//import java.net.URI
//import java.nio.charset.Charset
//import java.util.*
//import java.util.regex.Pattern
//import java.util.regex.Pattern.compile
//import kotlin.test.Ignore
//import kotlin.test.Test
//import kotlin.test.assertNotNull
//
///*=================================================================================*/
///*=================================================================================*/
//
//class MailServiceSmtpTests {
//
//    private lateinit var context: ConfigurableApplicationContext
//    private lateinit var mailService: MailService
//
//    private val properties: school.base.utils.Properties by lazy { context.getBean() }
//
//    private val messageSource: MessageSource by lazy { context.getBean() }
//
//    private val templateEngine: SpringWebFluxTemplateEngine by lazy { context.getBean() }
//
//    @Spy
//    lateinit var javaMailSender: JavaMailSenderImpl
//
//    @Captor
//    lateinit var messageCaptor: ArgumentCaptor<MimeMessage>
//
//
//    @BeforeAll
//    fun `lance le server en profile test`() {
//        context = launcher()
//    }
//
//
//    @Suppress("NonAsciiCharacters")
//    @AfterAll
//    fun `arrête le serveur`() = context.close()
//
//    @BeforeEach
//    fun setup() {
//        openMocks(this)
//        doNothing()
//            .`when`(javaMailSender)
//            .send(any(MimeMessage::class.java))
//        mailService = MailServiceSmtp(
//            properties,
//            javaMailSender,
//            messageSource,
//            templateEngine
//        )
//    }
//
//
//    @Test
//    fun `test sendEmail`() {
//        mailService.sendEmail(
//            to = "john.doe@acme.com",
//            subject = "testSubject",
//            content = "testContent",
//            isMultipart = false,
//            isHtml = false
//        )
//        verify(javaMailSender).send(messageCaptor.capture())
//        val message = messageCaptor.value
//        assertThat(message.subject).isEqualTo("testSubject")
//        assertThat(message.allRecipients[0]).hasToString("john.doe@acme.com")
//        assertThat(message.from[0]).hasToString(properties.mail.from)
//        assertThat(message.content).isInstanceOf(String::class.java)
//        assertThat(message.content).hasToString("testContent")
//        assertThat(message.dataHandler.contentType).isEqualTo("text/plain; charset=UTF-8")
//    }
//
//    @Test
//    fun `test sendMail SendHtmlEmail`() {
//        mailService.sendEmail(
//            to = "john.doe@acme.com",
//            subject = "testSubject",
//            content = "testContent",
//            isMultipart = false,
//            isHtml = true
//        )
//        verify(javaMailSender).send(messageCaptor.capture())
//        val message = messageCaptor.value
//        assertThat(message.subject).isEqualTo("testSubject")
//        assertThat("${message.allRecipients[0]}").isEqualTo("john.doe@acme.com")
//        assertThat("${message.from[0]}").isEqualTo(properties.mail.from)
//        assertThat(message.content).isInstanceOf(String::class.java)
//        assertThat(message.content.toString()).isEqualTo("testContent")
//        assertThat(message.dataHandler.contentType).isEqualTo("text/html;charset=UTF-8")
//    }
//
//    @Test
//    fun `test sendMail SendMultipartEmail`() {
//        mailService.sendEmail(
//            to = "john.doe@acme.com",
//            subject = "testSubject",
//            content = "testContent",
//            isMultipart = true,
//            isHtml = false
//        )
//        verify(javaMailSender).send(messageCaptor.capture())
//        val message = messageCaptor.value
//        val part = ((message.content as MimeMultipart)
//            .getBodyPart(0).content as MimeMultipart)
//            .getBodyPart(0) as MimeBodyPart
//        val aos = ByteArrayOutputStream()
//        part.writeTo(aos)
//        assertThat(message.subject).isEqualTo("testSubject")
//        assertThat("${message.allRecipients[0]}").isEqualTo("john.doe@acme.com")
//        assertThat("${message.from[0]}").isEqualTo(properties.mail.from)
//        assertThat(message.content).isInstanceOf(Multipart::class.java)
//        assertThat("$aos").isEqualTo("\r\ntestContent")
//        assertThat(part.dataHandler.contentType).isEqualTo("text/plain; charset=UTF-8")
//    }
//
//    @Test
//    fun `test sendMail SendMultipartHtmlEmail`() {
//        mailService.sendEmail(
//            to = "john.doe@acme.com",
//            subject = "testSubject",
//            content = "testContent",
//            isMultipart = true,
//            isHtml = true
//        )
//        verify(javaMailSender).send(messageCaptor.capture())
//        val message = messageCaptor.value
//        val part = ((message.content as MimeMultipart)
//            .getBodyPart(0).content as MimeMultipart)
//            .getBodyPart(0) as MimeBodyPart
//        val aos = ByteArrayOutputStream()
//        part.writeTo(aos)
//        assertThat(message.subject).isEqualTo("testSubject")
//        assertThat("${message.allRecipients[0]}").isEqualTo("john.doe@acme.com")
//        assertThat("${message.from[0]}").isEqualTo(properties.mail.from)
//        assertThat(message.content).isInstanceOf(Multipart::class.java)
//        assertThat("$aos").isEqualTo("\r\ntestContent")
//        assertThat(part.dataHandler.contentType).isEqualTo("text/html;charset=UTF-8")
//    }
//
//    @Test
//    fun `test SendEmailFromTemplate`() {
//        val user = AccountCredentials(
//            login = "john",
//            email = "john.doe@acme.com",
//            langKey = "en"
//        )
//        mailService.sendEmailFromTemplate(
//            user,
//            "mail/testEmail",
//            "email.test.title"
//        )
//        verify(javaMailSender).send(messageCaptor.capture())
//        val message = messageCaptor.value
//        assertThat(message.subject).isEqualTo("test title")
//        assertThat("${message.allRecipients[0]}").isEqualTo(user.email)
//        assertThat("${message.from[0]}").isEqualTo(properties.mail.from)
//        assertThat(message.content.toString()).isEqualToNormalizingNewlines(
//            "<html>test title, http://127.0.0.1:8080, john</html>"
//        )
//        assertThat(message.dataHandler.contentType).isEqualTo("text/html;charset=UTF-8")
//    }
//
//    @Test
//    fun testSendActivationEmail() {
//        val user = AccountCredentials(
//            langKey = DEFAULT_LANGUAGE,
//            login = "john",
//            email = "john.doe@acme.com"
//        )
//        mailService.sendActivationEmail(user)
//        verify(javaMailSender).send(messageCaptor.capture())
//        val message = messageCaptor.value
//        assertThat("${message.allRecipients[0]}").isEqualTo(user.email)
//        assertThat("${message.from[0]}").isEqualTo(properties.mail.from)
//        assertThat(message.content.toString()).isNotEmpty
//        assertThat(message.dataHandler.contentType).isEqualTo("text/html;charset=UTF-8")
//    }
//
//    @Test
//    fun testCreationEmail() {
//        val user = AccountCredentials(
//            langKey = DEFAULT_LANGUAGE,
//            login = "john",
//            email = "john.doe@acme.com",
//            resetKey = generateResetKey
//        )
//        mailService.sendCreationEmail(user)
//        verify(javaMailSender).send(messageCaptor.capture())
//        val message = messageCaptor.value
//        assertThat("${message.allRecipients[0]}").isEqualTo(user.email)
//        assertThat("${message.from[0]}").isEqualTo(properties.mail.from)
//        assertThat(message.content.toString()).isNotEmpty
//        assertThat(message.dataHandler.contentType).isEqualTo("text/html;charset=UTF-8")
//    }
//
//    @Test
//    fun testSendPasswordResetMail() {
//        val user = AccountCredentials(
//            langKey = DEFAULT_LANGUAGE, login = "john", email = "john.doe@acme.com"
//        )
//        mailService.sendPasswordResetMail(user)
//        verify(javaMailSender).send(messageCaptor.capture())
//        val message = messageCaptor.value
//        assertThat("${message.allRecipients[0]}").isEqualTo(user.email)
//        assertThat("${message.from[0]}").isEqualTo(properties.mail.from)
//        assertThat(message.content.toString()).isNotEmpty
//        assertThat(message.dataHandler.contentType).isEqualTo("text/html;charset=UTF-8")
//    }
//
//    @Test
//    fun testSendEmailWithException() {
//        Mockito.doThrow(MailSendException::class.java).`when`(javaMailSender)
//            .send(any(MimeMessage::class.java))
//        try {
//            mailService.sendEmail(
//                "john.doe@acme.com", "testSubject", "testContent", isMultipart = false, isHtml = false
//            )
//        } catch (e: Exception) {
//            Assertions.fail<String>("Exception shouldn't have been thrown")
//        }
//    }
//
//    val languages = arrayOf(
//        "en", "fr", "de", "it", "es"
//    )
//    val PATTERN_LOCALE_3: Pattern = compile("([a-z]{2})-([a-zA-Z]{4})-([a-z]{2})")
//    val PATTERN_LOCALE_2: Pattern = compile("([a-z]{2})-([a-z]{2})")
//
//    @Test
//    fun testSendLocalizedEmailForAllSupportedLanguages() {
//        val user = AccountCredentials(
//            login = "john", email = "john.doe@acme.com"
//        )
//        for (langKey in languages) {
//            mailService.sendEmailFromTemplate(
//                user.copy(langKey = langKey), "mail/testEmail", "email.test.title"
//            )
//            verify(javaMailSender, atLeastOnce()).send(messageCaptor.capture())
//            val message = messageCaptor.value
//
//            val resource = this::class.java.classLoader.getResource(
//                "i18n/messages_${
//                    getJavaLocale(langKey)
//                }.properties"
//            )
//            assertNotNull(resource)
//            val prop = Properties()
//            prop.load(
//                InputStreamReader(
//                    FileInputStream(File(URI(resource.file).path)), Charset.forName("UTF-8")
//                )
//            )
//
//            val emailTitle = prop["email.test.title"] as String
//            assertThat(message.subject).isEqualTo(emailTitle)
//            assertThat(message.content.toString())
//                .isEqualToNormalizingNewlines("<html>$emailTitle, http://127.0.0.1:8080, john</html>")
//        }
//    }
//
//    private fun getJavaLocale(langKey: String): String {
//        var javaLangKey = langKey
//        val matcher2 = PATTERN_LOCALE_2.matcher(langKey)
//        if (matcher2.matches()) javaLangKey = "${
//            matcher2.group(1)
//        }_${
//            matcher2.group(2).uppercase()
//        }"
//        val matcher3 = PATTERN_LOCALE_3.matcher(langKey)
//        if (matcher3.matches()) javaLangKey = "${
//            matcher3.group(1)
//        }_${
//            matcher3.group(2)
//        }_${
//            matcher3.group(3).uppercase()
//        }"
//        return javaLangKey
//    }
//}
//
///*=================================================================================*/
//@Ignore
//class GmailServiceTests {
//
//    private lateinit var context: ConfigurableApplicationContext
//
//    @BeforeAll
//    fun `lance le server en profile test`() {
//        context = launcher(GMAIL)
//    }
//
//    @AfterAll
//    fun `arrête le serveur`() = context.close()
//}
//
///*=================================================================================*/
//
