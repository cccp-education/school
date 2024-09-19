package webapp.users.signup

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import webapp.users.signup.AccountRestApis.API_ACCOUNT

@RestController
@RequestMapping(API_ACCOUNT)
class SignupController {
  internal class SignupException(message: String) : RuntimeException(message)

}

