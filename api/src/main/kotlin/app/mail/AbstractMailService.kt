package app.mail//package school.base.mail
//
//import org.springframework.context.MessageSource
//import org.thymeleaf.context.Context
//import org.thymeleaf.spring6.SpringTemplateEngine
//import school.base.property.BASE_URL
//import school.base.property.TEMPLATE_NAME_CREATION
//import school.base.property.TEMPLATE_NAME_PASSWORD
//import school.base.property.TEMPLATE_NAME_SIGNUP
//import school.base.property.TITLE_KEY_PASSWORD
//import school.base.property.TITLE_KEY_SIGNUP
//import school.base.property.USER
//import school.base.logging.d
//import school.base.utils.Properties
////import school.accounts.models.AccountCredentials
//import java.util.Locale.forLanguageTag
//
//abstract class AbstractMailService(
//    private val properties: Properties,
//    private val messageSource: MessageSource,
//    private val templateEngine: SpringTemplateEngine
//) : MailService {
//
//    abstract override fun sendEmail(
//        to: String,
//        subject: String,
//        content: String,
//        isMultipart: Boolean,
//        isHtml: Boolean
//    )
//
//    override fun sendEmailFromTemplate(
//        account: AccountCredentials,
//        templateName: String,
//        titleKey: String
//    ) {
//        when (account.email) {
//            null -> {
//                d("Email doesn't exist for user '${account.login}'")
//                return
//            }
//
//            else -> forLanguageTag(account.langKey).apply {
//                sendEmail(
//                    account.email,
//                    messageSource.getMessage(titleKey, null, this),
//                    templateEngine.process(templateName, Context(this).apply {
//                        setVariable(USER, account)
//                        setVariable(BASE_URL, properties.mail.baseUrl)
//                    }),
//                    isMultipart = false,
//                    isHtml = true
//                )
//            }
//        }
//    }
//
//    override fun sendActivationEmail(account: AccountCredentials) = sendEmailFromTemplate(
//        account.apply {
//            d("Sending activation email to '${account.email}'")
//        }, TEMPLATE_NAME_SIGNUP, TITLE_KEY_SIGNUP
//    )
//
//    override fun sendCreationEmail(account: AccountCredentials) = sendEmailFromTemplate(
//        account.apply {
//            d("Sending creation email to '${account.email}'")
//        }, TEMPLATE_NAME_CREATION, TITLE_KEY_SIGNUP
//    )
//
//    override fun sendPasswordResetMail(account: AccountCredentials) = sendEmailFromTemplate(
//        account.apply {
//            d("Sending password reset email to '${account.email}'")
//        }, TEMPLATE_NAME_PASSWORD, TITLE_KEY_PASSWORD
//    )
//}