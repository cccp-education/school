package users.signup

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import app.http.HttpUtils.badResponse
import users.Signup
import users.dao.UserDao
import users.signup.SignupService.Companion.SIGNUP_EMAIL_NOT_AVAILABLE
import users.signup.SignupService.Companion.SIGNUP_LOGIN_AND_EMAIL_NOT_AVAILABLE
import users.signup.SignupService.Companion.SIGNUP_LOGIN_NOT_AVAILABLE
import users.signup.SignupService.Companion.badResponseEmailIsNotAvailable
import users.signup.SignupService.Companion.badResponseLoginAndEmailIsNotAvailable
import users.signup.SignupService.Companion.badResponseLoginIsNotAvailable
import users.signup.SignupService.Companion.signupProblems
import users.signup.SignupService.Companion.validate
import workspace.Log

@RestController
@RequestMapping(UserDao.UserRestApiRoutes.API_USERS)
class SignupController(private val signupService: SignupService) {
    internal class SignupException(message: String) : RuntimeException(message)

    /**
     * {@code POST  /signup} : Signup the user.
     *
     * @param signup the managed signup View Model.
     */
    @PostMapping(
        UserDao.UserRestApiRoutes.API_SIGNUP,
        produces = [MediaType.APPLICATION_PROBLEM_JSON_VALUE]
    )
    suspend fun signup(
        @RequestBody signup: Signup,
        exchange: ServerWebExchange
    ): ResponseEntity<ProblemDetail> = signup.validate(exchange).run {
        Log.i("signup attempt: ${this@run} ${signup.login} ${signup.email}")
        if (isNotEmpty()) return signupProblems.badResponse(this)
    }.run {
        signupService.signupAvailability(signup).map {
            return when (it) {
                SIGNUP_LOGIN_AND_EMAIL_NOT_AVAILABLE -> signupProblems.badResponseLoginAndEmailIsNotAvailable
                SIGNUP_LOGIN_NOT_AVAILABLE -> signupProblems.badResponseLoginIsNotAvailable
                SIGNUP_EMAIL_NOT_AVAILABLE -> signupProblems.badResponseEmailIsNotAvailable
                else -> {
                    signupService.signup(signup)
                    HttpStatus.CREATED.run(::ResponseEntity)
                }
            }
        }
        HttpStatus.SERVICE_UNAVAILABLE.run(::ResponseEntity)
    }
}