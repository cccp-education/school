package school.base.http

//import school.base.security.Security
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.hibernate.validator.HibernateValidator
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.server.ServerWebExchange
import school.base.configurations.http.ProblemsModel
import java.net.URI
import java.util.*
import java.util.Locale.ENGLISH

@Suppress("unused")
//val ServerWebExchange.spaExchange: ServerWebExchange
//    get() = request.uri.path.run {
//        when {
//            Security.spaNegated.none { !startsWith(it) } &&
//                    matches(Regex(SPA_NEGATED_REGEX)) ->
//                mutate().request(
//                    request
//                        .mutate()
//                        .path("/index.html")
//                        .build()
//                ).build()
//
//            else -> this@spaExchange
//        }
//    }


val ServerWebExchange.validator: Validator
    get() = Validation.byProvider(HibernateValidator::class.java)
        .configure()
        .localeResolver {
            try {
                Locale(
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

//fun AccountCredentials.validate(
//    exchange: ServerWebExchange
//): Set<Map<String, String?>> {
//    exchange.validator.run {
//        return setOf(
//            PASSWORD_FIELD,
//            EMAIL_FIELD,
//            LOGIN_FIELD,
//            FIRST_NAME_FIELD,
//            LAST_NAME_FIELD
//        ).map { field: String ->
//            field to validateProperty(this@validate, field)
//        }.flatMap { violatedField: Pair<String, MutableSet<ConstraintViolation<AccountCredentials>>> ->
//            violatedField.second.map {
//                mapOf<String, String?>(
//                    "objectName" to AccountCredentials.objectName,
//                    "field" to violatedField.first,
//                    "message" to it.message
//                )
//            }
//        }.toSet()
//    }
//}

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

/*
package community.base.http


import jakarta.validation.Validation.byProvider
import jakarta.validation.Validator
import org.hibernate.validator.HibernateValidator
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.ProblemDetail
import org.springframework.http.ProblemDetail.forStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.badRequest
import org.springframework.http.ResponseEntity.internalServerError
import org.springframework.web.server.ServerWebExchange
import java.net.URI
import java.util.Locale.ENGLISH
import java.util.Locale.of


//TODO: i18n
fun ProblemsModel.serverErrorResponse(error: String)
        : ResponseEntity<ProblemDetail> = internalServerError()
    .body(forStatus(INTERNAL_SERVER_ERROR).apply {
        type = URI(this@serverErrorResponse.type)
        title = title
        status = INTERNAL_SERVER_ERROR.value()
        setProperty("path", path)
        setProperty("message", message)
        setProperty("error", error)
    })

val ServerWebExchange.validator: Validator
    get() = byProvider(HibernateValidator::class.java)
        .configure()
        .localeResolver {
            try {
                of(
                    request.headers.acceptLanguage.first().range
                )
            } catch (e: Exception) {
                ENGLISH
            }
        }.buildValidatorFactory()
        .validator



fun ProblemsModel.badResponse(
    fieldErrors: Set<Map<String, String?>>
) = badRequest()
    .body(forStatus(BAD_REQUEST).apply {
        type = URI(this@badResponse.type)
        title = title
        status = BAD_REQUEST.value()
        setProperty("path", path)
        setProperty("message", message)
        setProperty("fieldErrors", fieldErrors)
    })
 */



