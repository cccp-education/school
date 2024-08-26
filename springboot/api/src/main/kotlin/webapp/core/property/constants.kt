package webapp.core.property

import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import webapp.core.configurations.http.ProblemsModel
import java.net.URI
import java.net.URI.create

const val ROLE_FIELD = "role"
const val SPA_NEGATED_REGEX = "[^\\\\.]*"
const val EMPTY_STRING = ""
const val LINE = "\n"
const val VIRGULE = ","
const val BASE_URL_DEV = "http://localhost:8080"
const val PROBLEM_OBJECT_NAME = "objectName"
const val PROBLEM_FIELD = "field"
const val PROBLEM_MESSAGE = "message"
val detailsKeys by lazy {
    setOf(
        PROBLEM_OBJECT_NAME,
        PROBLEM_FIELD,
        PROBLEM_MESSAGE
    )
}

//SignupController
val ALLOWED_ORDERED_PROPERTIES by lazy {
    arrayOf(
        "id",
        "login",
        "firstName",
        "lastName",
        "email",
        "activated",
        "langKey"
    )
}
const val NORMAL_TERMINATION = 0
const val DOMAIN_DEV_URL = "acme.com"
private const val DOMAIN_URL = "https://cheroliv.github.io"
const val STARTUP_HOST_WARN_LOG_MSG = "The host name could not be determined, using `localhost` as fallback"
const val SPRING_APPLICATION_NAME = "spring.application.name"
const val SERVER_SSL_KEY_STORE = "server.ssl.key-store"
const val SERVER_PORT = "server.port"
const val SERVER_SERVLET_CONTEXT_PATH = "server.servlet.context-path"
const val EMPTY_CONTEXT_PATH = "/"
const val HTTPS = "https"
const val HTTP = "http"
const val PROFILE_SEPARATOR = ","
val CLI_PROPS by lazy { mapOf("spring.main.web-application-type" to "none") }
const val SPRING_PROFILE_CONF_DEFAULT_KEY = "spring.profiles.default"
const val MSG_WRONG_ACTIVATION_KEY = "No user was found for this activation key"


//Spring profiles
const val DEFAULT = "default"
const val DEVELOPMENT = "dev"
const val PRODUCTION = "prod"
const val CLOUD = "cloud"
const val TEST = "test"
const val AWS_ECS = "aws-ecs"
const val AZURE = "azure"
const val SWAGGER = "swagger"
const val NO_LIQUIBASE = "no-liquibase"
const val K8S = "k8s"
const val CLI = "utils"
const val GMAIL = "gmail"
const val MAILSLURP = "mailslurp"

//Config
const val DEV_HOST = "localhost"

//HTTP param
const val REQUEST_PARAM_LANG = "lang"
const val CONTENT_SECURITY_POLICY =
    "default-src 'self'; frame-src 'self' data:; script-src 'self' 'unsafe-inline' 'unsafe-eval' https://storage.googleapis.com; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:"
const val FEATURE_POLICY =
    "geolocation 'none'; midi 'none'; sync-xhr 'none'; microphone 'none'; camera 'none'; magnetometer 'none'; gyroscope 'none'; speaker 'none'; fullscreen 'self'; payment 'none'"

//Security
const val ROLE_ADMIN = "ADMIN"
const val ROLE_USER = "USER"
const val ROLE_ANONYMOUS = "ANONYMOUS"
const val AUTHORITIES_KEY = "auth"
const val AUTHORIZATION_HEADER = "Authorization"
const val BEARER_START_WITH = "Bearer "
const val AUTHORIZATION_ID_TOKEN = "id_token"
const val VALID_TOKEN = true
const val INVALID_TOKEN = false

//Email
const val MAIL_DEBUG = "mail.debug"
const val MAIL_TRANSPORT_STARTTLS_ENABLE = "mail.smtp.starttls.enable"
const val MAIL_SMTP_AUTH = "mail.smtp.auth"
const val MAIL_TRANSPORT_PROTOCOL = "mail.transport.protocol"




//properties
const val PROP_ITEM = "community.item"
const val PROP_MESSAGE = "community.message"
const val PROP_MAIL_BASE_URL = "community.accounts.mail.base-url"
const val PROP_MAIL_FROM = "community.accounts.mail.from"
const val PROP_MAIL_HOST = "community.accounts.mail.host"
const val PROP_MAIL_PORT = "community.accounts.mail.port"
const val PROP_MAIL_PASSWORD = "community.accounts.mail.password"
const val PROP_MAIL_PROPERTY_DEBUG = "community.accounts.mail.property.debug"
const val PROP_MAIL_PROPERTY_TRANSPORT_PROTOCOL = "community.accounts.mail.property.transport.protocol"
const val PROP_MAIL_PROPERTY_SMTP_AUTH = "community.accounts.mail.property.smtp.auth"
const val PROP_MAIL_PROPERTY_SMTP_STARTTLS_ENABLE = "community.accounts.mail.property.smtp.starttls.enable"
const val PROP_DATABASE_POPULATOR_PATH = "community.database.populator-path"
const val STARTUP_LOG_MSG_KEY = "startup.log.msg"


//Email activation
const val USER = "user"
const val ADMIN = "admin"
const val BASE_URL = "baseUrl"

const val TEMPLATE_NAME_SIGNUP = "mail/activationEmail"

const val TITLE_KEY_SIGNUP = "email.activation.title"

const val TEMPLATE_NAME_CREATION = "mail/creationEmail"

const val TEMPLATE_NAME_PASSWORD = "mail/passwordResetEmail"

const val TITLE_KEY_PASSWORD = "email.reset.title"

const val SYSTEM_USER = "system"


@Suppress("SpellCheckingInspection")
const val ANONYMOUS_USER: String = "anonymoususer"
const val DEFAULT_LANGUAGE = "en"

const val ERR_CONCURRENCY_FAILURE: String = "error.concurrencyFailure"
const val ERR_VALIDATION: String = "error.validation"
const val USER_INITIAL_ACTIVATED_VALUE = false
private const val PROBLEM_BASE_URL: String = "$DOMAIN_URL/problem"


@JvmField
val DEFAULT_TYPE: URI = create("$PROBLEM_BASE_URL/problem-with-message")

@JvmField
val CONSTRAINT_VIOLATION_TYPE: URI = create("$PROBLEM_BASE_URL/constraint-violation")

@JvmField
val INVALID_PASSWORD_TYPE: URI = create("$PROBLEM_BASE_URL/invalid-password")

@JvmField
val EMAIL_ALREADY_USED_TYPE: URI = create("$PROBLEM_BASE_URL/email-already-used")

@JvmField
val LOGIN_ALREADY_USED_TYPE: URI = create("$PROBLEM_BASE_URL/login-already-used")


val validationProblems by lazy {
    ProblemsModel(
        type = "https://cheroliv.github.io/problem/constraint-violation",
        title = "Data binding and validation failure",
        message = "error.validation",
        status = BAD_REQUEST.value(),
    )
}

val serverErrorProblems by lazy {
    ProblemsModel(
        type = "https://cheroliv.github.io/problem/internal-server-error",
        title = "Internal Server Error",
        message = "error.server",
        status = INTERNAL_SERVER_ERROR.value(),
    )
}


const val PASSWORD_MIN: Int = 4
const val PASSWORD_MAX: Int = 24
const val IMAGE_URL_DEFAULT = "http://placehold.it/50x50"


val defaultProblems = ProblemsModel(
    type = "https://cheroliv.github.io/problem/constraint-violation",
    title = "Data binding and validation failure",
    message = "error.validation",
    path = "",
    status = BAD_REQUEST.value(),
)