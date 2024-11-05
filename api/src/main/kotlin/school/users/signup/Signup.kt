package school.users.signup

import jakarta.validation.ConstraintViolation
import org.springframework.web.server.ServerWebExchange
import school.base.http.ProblemsModel
import school.base.http.badResponse
import school.base.http.validator
import school.base.model.EntityModel.Companion.MODEL_FIELD_FIELD
import school.base.model.EntityModel.Companion.MODEL_FIELD_MESSAGE
import school.base.model.EntityModel.Companion.MODEL_FIELD_OBJECTNAME
import school.base.utils.Constants.defaultProblems
import school.users.User
import school.users.User.UserDao.Fields.EMAIL_FIELD
import school.users.User.UserDao.Fields.LOGIN_FIELD
import school.users.User.UserDao.Fields.PASSWORD_FIELD
import school.users.User.UserRestApiRoutes.API_SIGNUP
import school.users.User.UserRestApiRoutes.API_USERS



@JvmRecord
data class Signup(
    val login: String,
    val password: String,
    val repassword: String,
    val email: String,
){
    companion object{
        fun Signup.validate(
            exchange: ServerWebExchange
        ): Set<Map<String, String?>> = exchange.validator.run {
            setOf(
                PASSWORD_FIELD,
                EMAIL_FIELD,
                LOGIN_FIELD,
            ).map { field: String ->
                field to validateProperty(this@validate, field)
            }.flatMap { violatedField: Pair<String, MutableSet<ConstraintViolation<Signup>>> ->
                violatedField.second.map {
                    mapOf<String, String?>(
                        MODEL_FIELD_OBJECTNAME to User.objectName,
                        MODEL_FIELD_FIELD to violatedField.first,
                        MODEL_FIELD_MESSAGE to it.message
                    )
                }
            }.toSet()
        }

        @JvmField
        val signupProblems = defaultProblems.copy(path = "$API_USERS$API_SIGNUP")

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


//suspend fun Signup.loginIsNotAvailable(signupService: UserService) =
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
//suspend fun Signup.emailIsNotAvailable(signupService: UserService) =
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

    }
}

