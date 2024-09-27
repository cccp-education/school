//package webapp.users.signup
//
//import webapp.*
//import webapp.accounts.models.AccountCredentials
//import webapp.accounts.models.toAccount
//import webapp.core.http.ProblemsModel
//import webapp.core.http.badResponse
//import webapp.core.property.*
//
//@JvmField
//val signupProblems = defaultProblems.copy(path = "$ACCOUNT_API$SIGNUP_API")
//
//val ProblemsModel.badResponseLoginIsNotAvailable
//    get() = badResponse(
//        setOf(
//            mapOf(
//                "objectName" to AccountCredentials.objectName,
//                "field" to LOGIN_FIELD,
//                "message" to "Login name already used!"
//            )
//        )
//    )
//val ProblemsModel.badResponseEmailIsNotAvailable
//    get() = badResponse(
//        setOf(
//            mapOf(
//                "objectName" to AccountCredentials.objectName,
//                "field" to EMAIL_FIELD,
//                "message" to "Email is already in use!"
//            )
//        )
//    )
//
//
//suspend fun AccountCredentials.loginIsNotAvailable(signupService: SignupService) =
//    signupService.accountById(login!!).run {
//        if (this == null) return@run false
//        return when {
//            !activated -> {
//                signupService.deleteAccount(toAccount())
//                false
//            }
//
//            else -> true
//        }
//    }
//
//suspend fun AccountCredentials.emailIsNotAvailable(signupService: SignupService) =
//    signupService.accountById(email!!).run {
//        if (this == null) return@run false
//        return when {
//            !activated -> {
//                signupService.deleteAccount(toAccount())
//                false
//            }
//
//            else -> true
//        }
//    }
