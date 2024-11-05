package school.base.http

import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation.byProvider
import jakarta.validation.Validator
import org.hibernate.validator.HibernateValidator
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.server.ServerWebExchange
import school.base.utils.Constants.SPA_NEGATED_REGEX
import school.users.User
import school.users.User.UserDao.Fields.EMAIL_FIELD
import school.users.User.UserDao.Fields.LOGIN_FIELD
import school.users.User.UserDao.Fields.PASSWORD_FIELD
import school.users.security.SecurityConfiguration.Companion.spaNegated
import school.users.signup.Signup
import java.net.URI
import java.util.Locale.ENGLISH
import java.util.Locale.forLanguageTag

@Suppress("unused")
val ServerWebExchange.spaExchange: ServerWebExchange
    get() = request.uri.path.run {
        when {
            spaNegated.none { !startsWith(it) } && matches(Regex(SPA_NEGATED_REGEX)) ->
                mutate().request(request.mutate().path("/index.html").build()).build()

            else -> this@spaExchange
        }
    }


val ServerWebExchange.validator: Validator
    get() = byProvider(HibernateValidator::class.java)
        .configure()
        .localeResolver {
            try {
                forLanguageTag(
                    request
                        .headers
                        .acceptLanguage
                        .first()
                        .range
                )
            } catch (e: Exception) {
                ENGLISH
            }
        }
        .buildValidatorFactory()
        .validator


//TODO: i18n
@Suppress("unused")
fun ProblemsModel.serverErrorResponse(error: String)
        : ResponseEntity<ProblemDetail> = ResponseEntity.internalServerError()
    .body(ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR).apply {
        type = URI(this@serverErrorResponse.type)
        title = title
        status = HttpStatus.INTERNAL_SERVER_ERROR.value()
        setProperty("path", path)
        setProperty("message", message)
        setProperty("error", error)
    })


fun ProblemsModel.badResponse(
    fieldErrors: Set<Map<String, String?>>
) = ResponseEntity.badRequest().body(
    ProblemDetail.forStatus(HttpStatus.BAD_REQUEST).apply {
        type = URI(this@badResponse.type)
        title = title
        status = HttpStatus.BAD_REQUEST.value()
        setProperty("path", path)
        setProperty("message", message)
        setProperty("fieldErrors", fieldErrors)
    }
)