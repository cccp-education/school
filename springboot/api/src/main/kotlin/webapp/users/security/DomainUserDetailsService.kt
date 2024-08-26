package webapp.users.security
//
//import jakarta.validation.Validator
//import kotlinx.coroutines.reactor.mono
//import org.springframework.security.core.authority.SimpleGrantedAuthority
//import org.springframework.security.core.userdetails.ReactiveUserDetailsService
//import org.springframework.security.core.userdetails.User
//import org.springframework.security.core.userdetails.UserDetails
//import org.springframework.security.core.userdetails.UsernameNotFoundException
//import org.springframework.stereotype.Component
//import org.springframework.transaction.annotation.Transactional
//import reactor.core.publisher.Mono
//import webapp.core.property.EMAIL_FIELD
//import webapp.accounts.exceptions.UserNotActivatedException
//import webapp.accounts.models.AccountCredentials
//import webapp.accounts.repository.AccountRepository
//import webapp.core.logging.d
//import kotlin.jvm.Throws
//
//@Suppress("unused")
//@Component("userDetailsService")
//class DomainUserDetailsService(
//    private val accountRepository: AccountRepository,
//    private val validator: Validator,
//) : ReactiveUserDetailsService {
//
//    @Transactional
//    @Throws(UsernameNotFoundException::class)
//    override fun findByUsername(emailOrLogin: String): Mono<UserDetails> =
//        d("Authenticating $emailOrLogin").run {
//            return if (validator.validateProperty(AccountCredentials(email = emailOrLogin), EMAIL_FIELD)
//                    .isEmpty()
//            ) mono {
//                accountRepository.findOneWithAuthorities(emailOrLogin).apply {
//                    when {
//                        this == null -> {
//                            throw UsernameNotFoundException("User with email $emailOrLogin was not found in the database")
//                        }
//                    }
//                }
//            }.map { createSpringSecurityUser(emailOrLogin, it) }
//            else mono {
//                accountRepository.findOneWithAuthorities(emailOrLogin).apply {
//                    when {
//                        this == null -> throw UsernameNotFoundException("User $emailOrLogin was not found in the database")
//                    }
//                }
//            }.map { createSpringSecurityUser(emailOrLogin, it) }
//        }
//
//
//    @Throws(UserNotActivatedException::class)
//    private fun createSpringSecurityUser(
//        lowercaseLogin: String,
//        account: AccountCredentials
//    ): User = when {
//        !account.activated -> throw UserNotActivatedException("User $lowercaseLogin was not activated")
//        else -> User(
//            account.login!!,
//            account.password!!,
//            account.authorities!!.map(::SimpleGrantedAuthority)
//        )
//    }
//}
//
//
///*
//package community.accounts.security
//
//import community.accounts.Account
//import community.accounts.Account.Companion.EMAIL_FIELD
//import community.accounts.AccountExtra
//import community.accounts.AccountRepository
//import community.accounts.UserNotActivatedException
//import community.core.logging.d
//import jakarta.validation.Validator
//import kotlinx.coroutines.reactor.mono
//import org.springframework.security.core.authority.SimpleGrantedAuthority
//import org.springframework.security.core.userdetails.ReactiveUserDetailsService
//import org.springframework.security.core.userdetails.User
//import org.springframework.security.core.userdetails.UserDetails
//import org.springframework.security.core.userdetails.UsernameNotFoundException
//import org.springframework.stereotype.Component
//import org.springframework.transaction.annotation.Transactional
//import reactor.core.publisher.Mono
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