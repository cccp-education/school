package backend.tdd.integration

import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory
import backend.tdd.integration.WithUnauthenticatedMockUser.Factory
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.*


@Target(
    FUNCTION,
    PROPERTY_GETTER,
    PROPERTY_SETTER,
    ANNOTATION_CLASS,
    CLASS
)
@Retention(RUNTIME)
@WithSecurityContext(factory = Factory::class)
annotation class WithUnauthenticatedMockUser {
    class Factory : WithSecurityContextFactory<WithUnauthenticatedMockUser?> {
        override fun createSecurityContext(annotation: WithUnauthenticatedMockUser?)
                : SecurityContext = SecurityContextHolder.createEmptyContext()
    }
}