package school.users.security

import jakarta.validation.Validator
import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import school.users.User
import school.users.User.UserDao.Dao.findOneWithAuths
import school.users.User.UserDao.Fields.EMAIL_FIELD
import school.users.User.UserDao.Fields.LOGIN_FIELD
import org.springframework.security.core.userdetails.User as UserSecurity

@Component("userDetailsService")
class DomainUserDetailsService(private val context: ApplicationContext) : ReactiveUserDetailsService {

    @Transactional
    @Throws(UsernameNotFoundException::class)
    override fun findByUsername(emailOrLogin: String): Mono<UserDetails> = context
        .getBean<Validator>()
        .run {
            when {
                validateProperty(
                    User(email = emailOrLogin),
                    EMAIL_FIELD
                ).isNotEmpty() && validateProperty(
                    User(login = emailOrLogin),
                    LOGIN_FIELD
                ).isNotEmpty() -> throw UsernameNotFoundException("User $emailOrLogin was not found")

                else -> mono {
                    context.findOneWithAuths<User>(emailOrLogin).map {
                        return@mono createSpringSecurityUser(emailOrLogin, it)
                    }.getOrNull() ?: throw UsernameNotFoundException("User $emailOrLogin was not found")
                }
            }
        }

    //        @Throws(UserNotActivatedException::class)
    fun createSpringSecurityUser(lowercaseLogin: String, user: User): UserSecurity = /*when {
        !user.activated -> throw UserNotActivatedException("User $lowercaseLogin was not activated")
        else -> */UserSecurity(
        user.login,
        user.password,
        user.roles.map { SimpleGrantedAuthority(it.id) }
    )
}