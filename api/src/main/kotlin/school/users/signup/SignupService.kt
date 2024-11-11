package school.users.signup

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import jakarta.validation.ConstraintViolation
import jakarta.validation.Validator
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebExchange
import school.base.http.HttpUtils.badResponse
import school.base.http.HttpUtils.validator
import school.base.http.ProblemsModel
import school.base.model.EntityModel.Companion.MODEL_FIELD_FIELD
import school.base.model.EntityModel.Companion.MODEL_FIELD_MESSAGE
import school.base.model.EntityModel.Companion.MODEL_FIELD_OBJECTNAME
import school.base.model.EntityModel.Members.withId
import school.base.utils.Constants.defaultProblems
import school.users.User
import school.users.User.UserDao.Dao.signup
import school.users.User.UserDao.Dao.signupToUser
import school.users.User.UserDao.Fields.EMAIL_FIELD
import school.users.User.UserDao.Fields.LOGIN_FIELD
import school.users.User.UserRestApiRoutes.API_SIGNUP
import school.users.User.UserRestApiRoutes.API_USERS
import school.users.signup.Signup.Companion.objectName
import school.users.signup.Signup.UserActivation.UserActivationDao.Dao.signupAvailability


@Service
class SignupService(private val context: ApplicationContext) {
    companion object {
        @JvmStatic
        val SIGNUP_AVAILABLE = Triple(true, true, true)

        @JvmStatic
        val SIGNUP_LOGIN_NOT_AVAILABLE = Triple(false, true, false)

        @JvmStatic
        val SIGNUP_EMAIL_NOT_AVAILABLE = Triple(false, false, true)

        @JvmStatic
        val SIGNUP_LOGIN_AND_EMAIL_NOT_AVAILABLE = Triple(false, false, false)
        fun Signup.validate(
            exchange: ServerWebExchange
        ): Set<Map<String, String?>> = exchange.validator.run {
            setOf(
                User.UserDao.Attributes.PASSWORD_ATTR,
                User.UserDao.Attributes.EMAIL_ATTR,
                User.UserDao.Attributes.LOGIN_ATTR,
            ).map { it -> it to validateProperty(this@validate, it) }
                .flatMap { violatedField: Pair<String, MutableSet<ConstraintViolation<Signup>>> ->
                    violatedField.second.map {
                        mapOf<String, String?>(
                            MODEL_FIELD_OBJECTNAME to objectName,
                            MODEL_FIELD_FIELD to violatedField.first,
                            MODEL_FIELD_MESSAGE to it.message
                        )
                    }
                }.toSet()
        }

        @JvmStatic
        val signupProblems = defaultProblems.copy(path = "$API_USERS$API_SIGNUP")

        @JvmStatic
        val ProblemsModel.badResponseLoginAndEmailIsNotAvailable
            get() = badResponse(
                setOf(
                    mapOf(
                        MODEL_FIELD_OBJECTNAME to User.objectName,
                        MODEL_FIELD_FIELD to LOGIN_FIELD,
                        MODEL_FIELD_FIELD to EMAIL_FIELD,
                        MODEL_FIELD_MESSAGE to "Login name already used and email is already in use!!"
                    )
                )
            )

        @JvmStatic
        val ProblemsModel.badResponseLoginIsNotAvailable
            get() = badResponse(
                setOf(
                    mapOf(
                        MODEL_FIELD_OBJECTNAME to User.objectName,
                        MODEL_FIELD_FIELD to LOGIN_FIELD,
                        MODEL_FIELD_MESSAGE to "Login name already used!"
                    )
                )
            )

        @JvmStatic
        val ProblemsModel.badResponseEmailIsNotAvailable
            get() = badResponse(
                setOf(
                    mapOf(
                        MODEL_FIELD_OBJECTNAME to User.objectName,
                        MODEL_FIELD_FIELD to EMAIL_FIELD,
                        MODEL_FIELD_MESSAGE to "Email is already in use!"
                    )
                )
            )
    }

    suspend fun signup(signup: Signup): Either<Throwable, User> = try {
        context.signupToUser(signup).run {
            (this to context).signup()
                .mapLeft { return Exception("Unable to save user with id").left() }
                .map { return withId(it).right() }
        }
    } catch (t: Throwable) {
        t.left()
    }

    suspend fun signupAvailability(signup: Signup)
            : Either<Throwable, Triple<Boolean, Boolean, Boolean>> = try {
        (signup to context)
            .signupAvailability()
            .onRight { it.right() }
            .onLeft { it.left() }
    } catch (ex: Throwable) {
        ex.left()
    }

//    fun validate(signup: Signup): Set<Map<String, String?>> = context.getBean<Validator>().let {
//        setOf(
//            User.UserDao.Attributes.PASSWORD_ATTR,
//            User.UserDao.Attributes.EMAIL_ATTR,
//            User.UserDao.Attributes.LOGIN_ATTR,
//        ).map { field -> field to it.validateProperty(signup, field) }
//            .flatMap { violatedField: Pair<String, MutableSet<ConstraintViolation<Signup>>> ->
//                violatedField.second.map {
//                    mapOf<String, String?>(
//                        MODEL_FIELD_OBJECTNAME to objectName,
//                        MODEL_FIELD_FIELD to violatedField.first,
//                        MODEL_FIELD_MESSAGE to it.message
//                    )
//                }
//            }.toSet()
//    }

//    @Transactional(readOnly = true)
//    suspend fun accountByActivationKey(key: String) = accountRepository.findOneByActivationKey(key)
//    @Transactional
//    suspend fun deleteExpired(account: Account) = accountRepository.delete(account)
}


/*
package school.accounts.signup

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import school.base.property.ACTIVATE_API_PATH
import school.base.property.BASE_URL_DEV
import school.base.property.ROLE_USER
import school.base.property.SYSTEM_USER
import school.accounts.models.Account
import school.accounts.models.AccountCredentials
import school.accounts.models.AccountUtils
import school.accounts.repository.AccountRepository
import school.base.logging.i
import school.base.mail.MailService
import java.time.Instant
import java.util.*

@Service
class UserService(
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
*/
/*
package community.accounts.signup

import community.accounts.Account

interface UserService {
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
) : UserService {
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