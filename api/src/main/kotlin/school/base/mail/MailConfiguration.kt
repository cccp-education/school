package school.base.mail

//@Configuration
//class MailConfiguration(private val properties: Properties) {
//    @Bean
//    @Profile("!$MAILSLURP & !$GMAIL")
//    fun javaMailSender(): JavaMailSender = JavaMailSenderImpl().apply {
//        host = properties.mail.host
//        port = properties.mail.port
//        username = properties.mail.from
//        password = properties.mail.password
//        mapOf(
//            MAIL_TRANSPORT_PROTOCOL to properties.mail.property.transport.protocol,
//            MAIL_SMTP_AUTH to properties.mail.property.smtp.auth,
//            MAIL_TRANSPORT_STARTTLS_ENABLE to properties.mail.property.smtp.starttls.enable,
//            MAIL_DEBUG to properties.mail.property.debug,
//            "spring.mail.test-connection" to true,
//            "mail.smtp.ssl.trust" to true,
//            "mail.connect_timeout" to 60000,
//            "mail.auth_api_key" to "",
//        ).forEach { javaMailProperties[it.key] = it.value }
//    }
//}