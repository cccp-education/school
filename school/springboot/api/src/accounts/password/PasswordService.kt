package webapp.accounts.password

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import webapp.accounts.models.AccountCredentials
import webapp.accounts.repository.AccountRepository
import webapp.core.logging.d
import java.time.Instant

@Service
@Transactional
class PasswordService(private val accountRepository: AccountRepository) {
    fun changePassword(currentPassword: String, newPassword: String) {
        TODO("Not yet implemented")
    }

    //    @Transactional
//    suspend fun changePassword(currentClearTextPassword: String, newPassword: String) {
//        SecurityUtils.getCurrentUserLogin().apply {
//            if (!isNullOrBlank()) {
//                userRepository.findOneByLogin(this).apply {
//                    if (this != null) {
//                        if (!passwordEncoder.matches(
//                                currentClearTextPassword,
//                                password
//                            )
//                        ) throw InvalidPasswordException()
//                        else saveUser(this.apply {
//                            password = passwordEncoder.encode(newPassword)
//                        }).run {
//                            d("Changed password for User: {}", this)
//                        }
//                    }
//                }
//            }
//        }
//    }
    suspend fun completePasswordReset(newPassword: String, key: String): AccountCredentials? =
        accountRepository.findOneByResetKey(key).run {
            if (this != null && resetDate?.isAfter(Instant.now().minusSeconds(86400)) == true) {
                d("Reset account password for reset key $key")
                return@completePasswordReset toCredentialsModel
                //                return saveUser(
                //                apply {
                ////                    password = passwordEncoder.encode(newPassword)
                //                    resetKey = null
                //                    resetDate = null
                //                })
            } else {
                d("$key is not a valid reset account password key")
                return@completePasswordReset null
            }
        }


    suspend fun requestPasswordReset(mail: String): AccountCredentials? = null
//        return userRepository
//            .findOneByEmail(mail)
//            .apply {
//                if (this != null && this.activated) {
//                    resetKey = generateResetKey
//                    resetDate = now()
//                    saveUser(this)
//                } else return null
//            }
//    }
}

/*
package community.accounts.password

import community.API_ACCOUNT
import community.API_RESET_INIT
import community.accounts.Account
import community.accounts.Account.Companion.EMAIL_FIELD
import community.accounts.AccountRepository
import community.accounts.signup.logResetAttempt
import community.accounts.validate
import community.core.http.badResponse
import community.core.logging.d
import community.validationProblems
import org.springframework.http.HttpStatus.OK
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ServerWebExchange
import java.time.Instant.now

@Service
@Transactional
open class PasswordService(private val accountRepository: AccountRepository) {

    suspend fun getAccountByEmail(email: String) = accountRepository.findOne(email)

    suspend fun reset(email: String, exchange: ServerWebExchange? = null)
            : Triple<ResponseEntity<ProblemDetail>, Account?, String?/*:key*/> {
//    suspend fun requestPasswordReset(mail: String): Account? =
//        accountRepository
//            .findOne(mail)
//            .apply {
//                if (this != null && this.activated) {
//                   copy(resetKey = generateResetKey,
//                    resetDate = now())
//                    saveUser(this)
//                } else return null
//            }

//        val errors = account
//            .validate(signupFields, exchange)
//            .apply { account.logSignupAttempt }


        val errors = Account(email = email)
            .apply { logResetAttempt }
            .validate(setOf(EMAIL_FIELD), exchange)

        val problems = validationProblems.copy(
            path = "$API_ACCOUNT$API_RESET_INIT"
        ).run {
            if (errors.isNotEmpty())
                return Triple(
                    badResponse(errors),
                    null,
                    null
                )
        }

//        val account = accountRepository
//            .findOne(email)
//            .apply {
//
//                if (this != null && this.activated) {
//                    copy(//TODO: repo.generateResetKey(account)
//                        resetKey = generateResetKey,
//                        resetDate = now()
//                    )
//                    saveUser(this)
//                } else return null
//
//            }

        accountRepository.generateResetKey(email).run {

        }
        val result = ResponseEntity<ProblemDetail>(OK)


        return Triple(result ,null,null)

    }


    suspend fun change(currentPassword: String, newPassword: String) {
        TODO("Not yet implemented")
    }

    //TODO: renvoi une ValidationViolationException ou une InvalidPasswordException ou OK
//    suspend fun changePassword(currentClearTextPassword: String, newPassword: String) {
//        securityUtils.getCurrentUserLogin().apply {
//            if (!isNullOrBlank()) {
//                userRepository.findOneByLogin(this).apply {
//                    if (this != null) {
//                        if (!passwordEncoder.matches(
//                                currentClearTextPassword,
//                                password
//                            )
//                        ) throw InvalidPasswordException()
//                        else saveUser(this.apply {
//                            password = passwordEncoder.encode(newPassword)
//                        }).run {
//                            d("Changed password for User: {}", this)
//                        }
//                    }
//                }
//            }
//        }
//    }
    suspend fun complete(newPassword: String, key: String): Account? =
        accountRepository.findOneByResetKey(key).run {
            if (this != null && second.resetDate?.isAfter(now().minusSeconds(86400)) == true) {
                d("Reset account password for reset key $key")
                return@complete first
                //                return saveUser(
                //                apply {
                ////                    password = passwordEncoder.encode(newPassword)
                //                    resetKey = null
                //                    resetDate = null
                //                })
            } else {
                d("$key is not a valid reset account password key")
                return@complete null
            }
        }
}
 */