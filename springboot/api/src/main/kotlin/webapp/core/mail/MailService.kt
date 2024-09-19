//package webapp.core.mail
//
//import webapp.accounts.models.AccountCredentials
//
//interface MailService {
//    fun sendEmail(
//        to: String,
//        subject: String,
//        content: String,
//        isMultipart: Boolean,
//        isHtml: Boolean
//    )
//
//    fun sendEmailFromTemplate(
//        account: AccountCredentials,
//        templateName: String,
//        titleKey: String
//    )
//
//    fun sendPasswordResetMail(account: AccountCredentials)
//    fun sendActivationEmail(account: AccountCredentials)
//    fun sendCreationEmail(account: AccountCredentials)
//}