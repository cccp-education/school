package webapp.users.signup

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import webapp.users.signup.AccountRestApis.API_ACCOUNT

@RestController
@RequestMapping(API_ACCOUNT)
class SignupController {
    internal class SignupException(message: String) : RuntimeException(message)
    object Signup {
        const val API_ACCOUNT = "/api/account"
        const val SIGNUP_API_PATH = "$API_ACCOUNT/signup"
    }
}

