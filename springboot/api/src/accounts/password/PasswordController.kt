package webapp.accounts.password

import jakarta.validation.Validator
import jakarta.validation.constraints.Email
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import webapp.*
import webapp.accounts.exceptions.InvalidPasswordException
import webapp.accounts.models.AccountCredentials
import webapp.accounts.models.KeyAndPassword
import webapp.accounts.models.PasswordChange
import webapp.core.logging.w
import webapp.core.mail.MailService
import webapp.core.property.*

/*=================================================================================*/
@Suppress("unused")
@RestController
@RequestMapping(ACCOUNT_API)
class PasswordController(
    private val passwordService: PasswordService,
    private val mailService: MailService,
    private val validator: Validator
) {
    internal class PasswordException(message: String) : RuntimeException(message)

    /**
     * {@code POST  /account/change-password} : changes the current user's password.
     *
     * @param passwordChange current and new password.
     * @throws InvalidPasswordProblem {@code 400 (Bad Request)} if the new password is incorrect.
     */
    @PostMapping(CHANGE_PASSWORD_API)//TODO: retourner des problemDetails
    suspend fun changePassword(@RequestBody passwordChange: PasswordChange): Unit =
        InvalidPasswordException().run {
            when {
                validator
                    .validateProperty(
                        AccountCredentials(password = passwordChange.newPassword),
                        PASSWORD_FIELD
                    ).isNotEmpty() -> throw this

                passwordChange.currentPassword != null
                        && passwordChange.newPassword != null -> passwordService.changePassword(
                    passwordChange.currentPassword,
                    passwordChange.newPassword
                )
            }

        }

    /**
     * {@code POST   /account/reset-password/init} : Send an email to reset the password of the user.
     *
     * @param mail the mail of the user.
     */
    @PostMapping(RESET_PASSWORD_API_INIT)//TODO: retourner des problemDetails
    suspend fun requestPasswordReset(@RequestBody @Email mail: String) =
        with(passwordService.requestPasswordReset(mail)) {
            when {
                this == null -> w("Password reset requested for non existing mail")
                else -> mailService.sendPasswordResetMail(this)
            }
        }

    /**
     * {@code POST   /account/reset-password/finish} : Finish to reset the password of the user.
     *
     * @param keyAndPassword the generated key and the new password.
     * @throws InvalidPasswordProblem {@code 400 (Bad Request)} if the password is incorrect.
     * @throws RuntimeException         {@code 500 (Internal Application Error)} if the password could not be reset.
     */
    @PostMapping(RESET_PASSWORD_API_FINISH)//TODO: retourner des problemDetails
    suspend fun finishPasswordReset(@RequestBody keyAndPassword: KeyAndPassword): Unit =
        InvalidPasswordException().run {
            when {
                validator
                    .validateProperty(
                        AccountCredentials(password = keyAndPassword.newPassword),
                        PASSWORD_FIELD
                    ).isNotEmpty() -> throw this
                keyAndPassword.newPassword != null
                        && keyAndPassword.key != null
                        && passwordService.completePasswordReset(
                    keyAndPassword.newPassword,
                    keyAndPassword.key
                ) == null -> throw PasswordException("No user was found for this reset key")
            }
        }

}

/**
@file:Suppress("unused")

package community.accounts.password

import community.*
import community.accounts.Account
import community.accounts.Account.Companion.PASSWORD_FIELD
import community.accounts.InvalidPasswordException
import community.accounts.mail.MailService
import community.core.http.badResponse
import community.accounts.signup.Signup
import jakarta.validation.Validator
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.OK
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange

/*=================================================================================*/
@RestController
@RequestMapping(API_ACCOUNT)
class PasswordController(
private val passwordService: PasswordService,
private val mailService: MailService,
private val validator: Validator
) {
internal class PasswordException(message: String) : RuntimeException(message)

/**
 * {@code POST   /account/reset-password/init} : Send an email to reset the password of the user.
 *
 * @param email the email of the user.
*/
@PostMapping(API_RESET_INIT)//TODO: retourner des problemDetails
// et virer la validation dans la signature
suspend fun requestPasswordReset(
@RequestBody email: String,
exchange: ServerWebExchange
): ResponseEntity<ProblemDetail> = passwordService
.reset(email, exchange)
.apply {
when (first.statusCode) {
OK -> mailService.sendPasswordResetMail(second!!)
BAD_REQUEST -> w("Password reset requested for non existing email")
else -> w("Password reset request caused unknoww error")
}
}.first


/**
 * authenticated
 *
 * {@code POST  /accounts/change-password} : changes the current user's password.
 *
 * @param passwords current and new password.
 * @return ProblemDetail
*/
@PostMapping(API_CHANGE)
suspend fun changePassword(@RequestBody passwords: PasswordChange): ResponseEntity<ProblemDetail> =
when {
validator.validateProperty(
Signup(account = Account(), password = passwords.new),
PASSWORD_FIELD
).isNotEmpty() ->
validationProblems.badResponse(setOf(mapOf("" to "")))//TODO: fulfill reasons with serverExchange and local

else -> {
val (current, new) = passwords
passwordService.change(current!!, new!!)
ResponseEntity(OK)
}
}


/**
 * {@code POST   /account/reset-password/finish} : Finish to reset the password of the user.
 *
 * @param passwordReset the generated key and the new password.
 * @throws InvalidPasswordProblem {@code 400 (Bad Request)} if the password is incorrect.
 * @throws RuntimeException         {@code 500 (Internal Application Error)} if the password could not be reset.
*/
@PostMapping(API_RESET_FINISH)//TODO: retourner des problemDetails
suspend fun finishPasswordReset(@RequestBody passwordReset: PasswordReset): Unit =
InvalidPasswordException().run {
when {
validator.validateProperty(
Signup(Account(), passwordReset.newPassword),
PASSWORD_FIELD
).isNotEmpty() -> throw this

passwordReset.newPassword != null
&& passwordReset.key != null
&& passwordService.complete(
passwordReset.newPassword,
passwordReset.key
) == null -> throw PasswordException("No user was found for this reset key")
}
}
}

 */