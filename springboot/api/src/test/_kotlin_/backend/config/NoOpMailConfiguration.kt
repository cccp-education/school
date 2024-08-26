package backend.config

import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import backend.repositories.entities.User
import backend.services.MailService

@Configuration
@Suppress("unused")
class NoOpMailConfiguration {
    private val mockMailService = mock(MailService::class.java)

    @Bean
    fun mailService(): MailService = mockMailService

    init {
        doNothing()
            .`when`(mockMailService)
            .sendActivationEmail(User())
    }
}