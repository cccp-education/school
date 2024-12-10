package users.signup

import app.database.EntityModel.Companion.MODEL_FIELD_FIELD
import app.database.EntityModel.Companion.MODEL_FIELD_MESSAGE
import app.database.EntityModel.Companion.MODEL_FIELD_OBJECTNAME
import app.database.EntityModel.Members.withId
import app.http.HttpUtils.badResponse
import app.http.HttpUtils.validator
import app.http.ProblemsModel
import app.utils.Constants.defaultProblems
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import jakarta.validation.ConstraintViolation
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebExchange
import users.User
import users.UserDao
import users.UserDao.Dao.signup
import users.UserDao.Dao.signupAvailability
import users.UserDao.Dao.signupToUser
import users.User.UserRestApiRoutes.API_SIGNUP
import users.User.UserRestApiRoutes.API_USERS
import workspace.Log.i

@Service
class SignupService(private val context: ApplicationContext) {


    suspend fun signup(signup: Signup): Either<Throwable, User> = try {
        context.signupToUser(signup).run {
            (this to context).signup()
                .mapLeft { return Exception("Unable to save user with id").left() }
                .map { return withId(it.first).right() }
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

    suspend fun signupRequest(
        signup: Signup,
        exchange: ServerWebExchange
    ): ResponseEntity<ProblemDetail> = signup.validate(exchange).run {
        "signup attempt: ${this@run} ${signup.login} ${signup.email}".run(::i)
        if (isNotEmpty()) return signupProblems.badResponse(this)
    }.run {
        signupAvailability(signup).map {
            when (it) {
                SIGNUP_LOGIN_AND_EMAIL_NOT_AVAILABLE -> return signupProblems.badResponseLoginAndEmailIsNotAvailable
                SIGNUP_LOGIN_NOT_AVAILABLE -> return signupProblems.badResponseLoginIsNotAvailable
                SIGNUP_EMAIL_NOT_AVAILABLE -> return signupProblems.badResponseEmailIsNotAvailable
                else -> {
                    signup(signup)
                    return CREATED.run(::ResponseEntity)
                }
            }
        }
        SERVICE_UNAVAILABLE.run(::ResponseEntity)
    }

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
                UserDao.Attributes.PASSWORD_ATTR,
                UserDao.Attributes.EMAIL_ATTR,
                UserDao.Attributes.LOGIN_ATTR,
            ).map { it to validateProperty(this@validate, it) }
                .flatMap { violatedField: Pair<String, MutableSet<ConstraintViolation<Signup>>> ->
                    violatedField.second.map {
                        mapOf<String, String?>(
                            MODEL_FIELD_OBJECTNAME to User.objectName,
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
                        MODEL_FIELD_FIELD to UserDao.Fields.LOGIN_FIELD,
                        MODEL_FIELD_FIELD to UserDao.Fields.EMAIL_FIELD,
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
                        MODEL_FIELD_FIELD to UserDao.Fields.LOGIN_FIELD,
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
                        MODEL_FIELD_FIELD to UserDao.Fields.EMAIL_FIELD,
                        MODEL_FIELD_MESSAGE to "Email is already in use!"
                    )
                )
            )
    }
}