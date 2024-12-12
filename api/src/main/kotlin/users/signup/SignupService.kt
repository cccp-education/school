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
import arrow.core.getOrElse
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
import users.UserController.UserRestApiRoutes.API_SIGNUP
import users.UserController.UserRestApiRoutes.API_USERS
import users.UserDao
import users.UserDao.Dao.signup
import users.UserDao.Dao.signupAvailability
import users.UserDao.Dao.signupToUser
import users.signup.UserActivationDao.Dao.activate
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
                    return signup(signup).run { CREATED.run(::ResponseEntity) }
                }
            }
        }
        SERVICE_UNAVAILABLE.run(::ResponseEntity)
    }

    suspend fun activateRequest(
        key: String,
        exchange: ServerWebExchange
    ) = key.run { activate(this) }

    suspend fun activate(key: String): Long = context.activate(key)
        .getOrElse { throw IllegalStateException("Error activating user with key: $key", it) }
        .takeIf { it == ONE_ROW_UPADTED }
        ?: throw IllegalArgumentException("Activation failed: No user was activated for key: $key")

    /*

        @GetMapping(ACTIVATE_API)
        @Throws(SignupException::class)
        suspend fun activateAccount(@RequestParam(ACTIVATE_API_KEY) key: String) {
            if (!signupService.accountByActivationKey(key).run no@{
                    return@no when {
                        this == null -> false.apply { i("no activation for key: $key") }
                        else -> signupService
                            .saveAccount(copy(activated = true, activationKey = null))
                            .run yes@{
                                return@yes when {
                                    this != null -> true.apply { i("activation: $login") }
                                    else -> false
                                }
                            }
                    }
                })
            //TODO: remplacer un ResponseEntity<ProblemDetail>
                throw SignupException(MSG_WRONG_ACTIVATION_KEY)
        }*/


    companion object {
        const val ONE_ROW_UPADTED = 1L

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