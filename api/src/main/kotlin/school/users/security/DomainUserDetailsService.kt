package school.users.security

import jakarta.validation.Validator
import kotlinx.coroutines.reactor.mono
import org.springframework.context.ApplicationContext
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.empty
import school.users.User
import school.users.User.UserDao.Dao.findOneWithAuths
import school.users.User.UserDao.Fields.EMAIL_FIELD
import school.users.User.UserDao.Fields.LOGIN_FIELD
import org.springframework.security.core.userdetails.User as UserSecurity

@Suppress("unused")
@Component("userDetailsService")
class DomainUserDetailsService(
    private val context: ApplicationContext,
    private val validator: Validator,
) : ReactiveUserDetailsService {

    @Transactional
    @Throws(UsernameNotFoundException::class)
    override fun findByUsername(emailOrLogin: String): Mono<UserDetails> {
        if (validator.validateProperty(
                User(email = emailOrLogin),
                EMAIL_FIELD
            ).isNotEmpty() && validator.validateProperty(
                User(login = emailOrLogin),
                LOGIN_FIELD
            ).isNotEmpty()
        ) throw UsernameNotFoundException("User $emailOrLogin was not found")
        mono {
            context.findOneWithAuths<User>(emailOrLogin).onRight {
                return@mono createSpringSecurityUser(emailOrLogin, it) as UserDetails
            }
        }
        return empty()
    }


    //        @Throws(UserNotActivatedException::class)
    fun createSpringSecurityUser(
        lowercaseLogin: String,
        user: User
    ): UserSecurity = /*when {
        !user.activated -> throw UserNotActivatedException("User $lowercaseLogin was not activated")
        else -> */UserSecurity(
        user.login,
        user.password,
        user.roles.map { SimpleGrantedAuthority(it.id) }
    )
//    }
}


///*
//package community.accounts.security
//
//import community.accounts.Account
//import community.accounts.Account.Companion.EMAIL_FIELD
//import community.accounts.AccountExtra
//import community.accounts.AccountRepository
//import community.accounts.UserNotActivatedException
//import community.base.logging.d
//import jakarta.validation.Validator
//import kotlinx.coroutines.reactor.mono
//import org.springframework.security.base.authority.SimpleGrantedAuthority
//import org.springframework.security.base.userdetails.ReactiveUserDetailsService
//import org.springframework.security.base.userdetails.User
//import org.springframework.security.base.userdetails.UserDetails
//import org.springframework.security.base.userdetails.UsernameNotFoundException
//import org.springframework.stereotype.Component
//import org.springframework.transaction.annotation.Transactional
//import reactor.base.publisher.Mono
//
//@Suppress("unused")
//@Component("userDetailsService")
//open class UserDetailsService(
//    private val accountRepository: AccountRepository,
//    private val validator: Validator,
//) : ReactiveUserDetailsService {
//
//    @Transactional
//    override fun findByUsername(emailOrLogin: String)
//            : Mono<UserDetails> = d("Authenticating $emailOrLogin").run {
//        return if (validator.validateProperty(Account(email = emailOrLogin), EMAIL_FIELD).isEmpty()) mono {
//            accountRepository.findOneWithAuthorities(emailOrLogin).apply {
//                if (this == null) throw UsernameNotFoundException("User with email $emailOrLogin was not found in the database")
//            }
//        }.map { createSpringSecurityUser(emailOrLogin, it) }
//        else mono {
//            accountRepository.findOneWithAuthorities(emailOrLogin).apply {
//                if (this == null) throw UsernameNotFoundException("User $emailOrLogin was not found in the database")
//            }
//        }.map { createSpringSecurityUser(emailOrLogin, it) }
//    }
//
//
//    private fun createSpringSecurityUser(
//        lowercaseLogin: String,
//        accountExtraPair: Pair<Account, AccountExtra>
//    ) = when {
//        accountExtraPair.second.activationKey != null -> throw UserNotActivatedException("User $lowercaseLogin was not activated")
//        else -> User(
//            accountExtraPair.first.login!!,
//            accountExtraPair.second.password,
//            accountExtraPair.first.authorities!!.map { SimpleGrantedAuthority(it) }
//        )
//    }
//}
// */