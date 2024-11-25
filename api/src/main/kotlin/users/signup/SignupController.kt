package users.signup

import org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import users.Signup
import users.dao.UserDao
import users.dao.UserDao.UserRestApiRoutes.API_SIGNUP

@RestController
@RequestMapping(UserDao.UserRestApiRoutes.API_USERS)
class SignupController(private val service: SignupService) {
    internal class SignupException(message: String) : RuntimeException(message)

    /**
     * {@code POST  /signup} : Signup the user.
     *
     * @param signup the managed signup View Model.
     */
    @PostMapping(
        API_SIGNUP,
        produces = [APPLICATION_PROBLEM_JSON_VALUE]
    )
    suspend fun signup(
        @RequestBody signup: Signup,
        exchange: ServerWebExchange
    ): ResponseEntity<ProblemDetail> = service.signupRequest(signup, exchange)
}