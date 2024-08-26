//package webapp.core.mail
//
//import jakarta.mail.MessagingException
//import org.springframework.context.MessageSource
//import org.springframework.context.annotation.Profile
//import org.springframework.mail.MailException
//import org.springframework.mail.javamail.JavaMailSender
//import org.springframework.mail.javamail.MimeMessageHelper
//import org.springframework.scheduling.annotation.Async
//import org.springframework.stereotype.Service
//import org.thymeleaf.spring6.SpringTemplateEngine
//import webapp.*
//import webapp.core.logging.d
//import webapp.core.logging.w
//import webapp.core.property.GMAIL
//import webapp.core.property.MAILSLURP
//import webapp.core.property.Properties
//import kotlin.text.Charsets.UTF_8
//
///*=================================================================================*/
//@Async
//@Service
//@Profile("!$MAILSLURP & !$GMAIL")
//class MailServiceSmtp(
//    private val properties: Properties,
//    private val mailSender: JavaMailSender,
//    private val messageSource: MessageSource,
//    private val templateEngine: SpringTemplateEngine
//) : AbstractMailService(
//    properties,
//    messageSource,
//    templateEngine
//) {
//    override fun sendEmail(
//        to: String,
//        subject: String,
//        content: String,
//        isMultipart: Boolean,
//        isHtml: Boolean
//    ) = mailSender.createMimeMessage().run {
//        try {
//            MimeMessageHelper(
//                this,
//                isMultipart,
//                UTF_8.name()
//            ).apply {
//                setTo(to)
//                setFrom(properties.mail.from)
//                setSubject(subject)
//                setText(content, isHtml)
//            }
//            mailSender.send(this)
//            d("Sent email to User '$to'")
//        } catch (e: MailException) {
//            w("Email could not be sent to user '$to'", e)
//        } catch (e: MessagingException) {
//            w("Email could not be sent to user '$to'", e)
//        }
//    }
//}