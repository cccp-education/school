package webapp.users.signup

import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service
class SignupService(private val context: ApplicationContext){

}

/*
package webapp.accounts.signup

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import webapp.core.property.ACTIVATE_API_PATH
import webapp.core.property.BASE_URL_DEV
import webapp.core.property.ROLE_USER
import webapp.core.property.SYSTEM_USER
import webapp.accounts.models.Account
import webapp.accounts.models.AccountCredentials
import webapp.accounts.models.AccountUtils
import webapp.accounts.repository.AccountRepository
import webapp.core.logging.i
import webapp.core.mail.MailService
import java.time.Instant
import java.util.*

@Service
class SignupService(
    private val accountRepository: AccountRepository,
    private val mailService: MailService,
    private val passwordEncoder: PasswordEncoder
) {
    @Transactional
    suspend fun signup(account: AccountCredentials) = Instant.now().run {
        accountRepository.signup(
            account.copy(
                password = passwordEncoder.encode(account.password),
                activationKey = AccountUtils.generateActivationKey,
                authorities = setOf(ROLE_USER),
                langKey = when {
                    account.langKey.isNullOrBlank() -> Locale.ENGLISH.language
                    else -> account.langKey
                },
                activated = false,
                createdBy = SYSTEM_USER,
                createdDate = this,
                lastModifiedBy = SYSTEM_USER,
                lastModifiedDate = this
            ).apply { i("activation link: $BASE_URL_DEV$ACTIVATE_API_PATH$activationKey") }
        )
        mailService.sendActivationEmail(account)
    }


    @Transactional(readOnly = true)
    suspend fun accountByActivationKey(key: String) = accountRepository.findOneByActivationKey(key)

    @Transactional(readOnly = true)
    suspend fun accountById(emailOrLogin: String) = accountRepository.findOne(emailOrLogin)

    @Transactional
    suspend fun saveAccount(account: AccountCredentials) = accountRepository.save(account)

    @Transactional
    suspend fun deleteAccount(account: Account) = accountRepository.delete(account)
}

/*
package community.accounts.signup

import community.accounts.Account

interface SignupService {
    suspend fun signup(signup: Signup): Signup?
    suspend fun accountByActivationKey(key: String): Account?
    suspend fun pairAccountActivatedById(emailOrLogin: String): Pair<Account, Boolean>?
    suspend fun saveAccount(account: Account): Account?
    suspend fun deleteAccount(account: Account)
    suspend fun activate(accountActivationKey: Pair<Account, String>)
    suspend fun checkSystemAvailable()
}
 */

/*
@file:Suppress("unused")

package community.accounts.signup

import community.accounts.Account
import community.accounts.AccountRepository
import community.accounts.mail.MailService
import jakarta.annotation.PostConstruct
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant.now

@Service
class SignupServiceImpl(
    private val accountRepository: AccountRepository,
    private val mailService: MailService,
    private val passwordEncoder: PasswordEncoder
) : SignupService {
    @Transactional
    override suspend fun signup(signup: Signup): Signup? = now().run {
        accountRepository.save(signup)?.also {
            mailService.sendActivationEmail(it.account)
        }
    }

    @PostConstruct
    override suspend fun checkSystemAvailable() {
        //TODO: check if system account if available if not create it
        // if profile is not dev check if admin and user is created then create it
    }

    override suspend fun activate(accountActivationKey: Pair<Account, String>) {

        TODO("Not yet implemented")
    }

    @Transactional(readOnly = true)
    override suspend fun accountByActivationKey(key: String): Account? =
        accountRepository.findOneByActivationKey(key)

    @Transactional(readOnly = true)
    override suspend fun pairAccountActivatedById(emailOrLogin: String): Pair<Account, Boolean>? =
        accountRepository.findOne(emailOrLogin)
            ?.let {
                it.first to (it.second.activationKey?.isNotBlank() ?: false)
            }

    @Transactional
    override suspend fun saveAccount(account: Account): Account? = accountRepository.save(account)?.first

    @Transactional
    override suspend fun deleteAccount(account: Account) = accountRepository.delete(account)

}
 */
*/