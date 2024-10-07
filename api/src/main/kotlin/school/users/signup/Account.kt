//@file:Suppress("unused")
//
//package school.users.signup
//
//import jakarta.validation.constraints.Email
//import jakarta.validation.constraints.NotBlank
//import jakarta.validation.constraints.Pattern
//import jakarta.validation.constraints.Size
//import org.springframework.web.server.ServerWebExchange
//import school.base.http.validator
//import java.time.Instant
//import java.util.*
//
///**
// * Représente l'account domain model sans le password
// */
////TODO: add field enabled=false
//data class Account(
//    val id: UUID? = null,
//    @field:NotBlank
//    @field:Pattern(regexp = LOGIN_REGEX)
//    @field:Size(min = 1, max = 50)
//    val login: String? = null,
//    @field:Size(max = 50)
//    val firstName: String? = null,
//    @field:Size(max = 50)
//    val lastName: String? = null,
//    @field:Email
//    @field:Size(min = 5, max = 254)
//    val email: String? = null,
//    @field:Size(max = 256)
//    val imageUrl: String? = IMAGE_URL_DEFAULT,
//    val activated: Boolean = false,
//    @field:Size(min = 2, max = 10)
//    val langKey: String? = null,
//    val createdBy: String? = null,
//    val createdDate: Instant? = null,
//    val lastModifiedBy: String? = null,
//    val lastModifiedDate: Instant? = null,
//    val authorities: Set<String>? = null
//){
//    companion object {
//        const val IMAGE_URL_DEFAULT = "http://placehold.it/50x50"
//        // Regex for acceptable logins
//        const val LOGIN_REGEX =
//            "^(?>[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*)|(?>[_.@A-Za-z0-9-]+)$"
//        const val PASSWORD_MIN: Int = 4
//        const val PASSWORD_MAX: Int = 16
//        @JvmStatic
//        val objectName: String = Account::class
//            .java
//            .simpleName
//            .run {
//                replaceFirst(
//                    first(),
//                    first().lowercaseChar()
//                )
//            }
//        const val LOGIN_FIELD = "login"
//        const val PASSWORD_FIELD = "password"
//        const val EMAIL_FIELD = "email"
//        const val ACCOUNT_AUTH_USER_ID_FIELD = "userId"
//        const val ACTIVATION_KEY_FIELD = "activationKey"
//        const val RESET_KEY_FIELD = "resetKey"
//        const val FIRST_NAME_FIELD = "firstName"
//        const val LAST_NAME_FIELD = "lastName"
//    }
//}
//
//fun Account.isActivated(): Boolean = activated
//
//fun Account.validate(
//    fields: Set<String>,
//    //si le validateur est null alors injecter celui du context
//    // mais faire cela a l'appel de la methode validate
//    //faire une extension function getValidator(exchange: ServerWebExchange?=null)
//    exchange: ServerWebExchange?,   //Pair<ApplicationContext,ServerWebExchange>
//    //si les deux sont null contruire soit meme le validator par defaut
//) = exchange.apply {
//    if (this == null) return emptySet<Map<String, String>>()
//}!!.validator.run {
//    fields.map { field ->
//        field to validateProperty(this@validate, field)
//    }.flatMap { violatedField ->
//        violatedField.second.map {
//            mapOf(
//                "objectName" to Account.objectName,
//                "field" to violatedField.first,
//                "message" to it.message
//            )
//        }
//    }.toSet()
//}
//
////fun Account.validate(
////    fields: Set<String>,
////    //si le validateur est null alors injecter celui du context
////    // mais faire cela a l'appel de la methode validate
////    //faire une extension function getValidator(exchange: ServerWebExchange?=null)
////    exchange: ServerWebExchange?,   //Pair<ApplicationContext,ServerWebExchange>
////    //si les deux sont null contruire soit meme le validator par defaut
////) = exchange.apply {
////    if (this == null) return emptySet<Map<String, String>>()
////}!!.validator.run {
////    fields.map { field ->
////        field to validateProperty(this@validate, field)
////    }.flatMap { violatedField ->
////        violatedField.second.map {
////            mapOf(
////                "objectName" to Account.objectName,
////                "field" to violatedField.first,
////                "message" to it.message
////            )
////        }
////    }.toSet()
////}
//
//
////@file:Suppress("unused")
////
////package community.accounts
////
////import community.base.http.validator
////import jakarta.validation.constraints.Email
////import jakarta.validation.constraints.NotBlank
////import jakarta.validation.constraints.Pattern
////import jakarta.validation.constraints.Size
////import org.springframework.web.server.ServerWebExchange
////import java.time.Instant
////import java.util.*
////
/////**
//// * Représente l'account domain model avec le password et l'activationKey
//// * pour la vue
//// */
////data class Account(
////    val id: UUID? = null,
////    @field:NotBlank
////    @field:Pattern(regexp = LOGIN_REGEX)
////    @field:Size(min = 1, max = 50)
////    val login: String? = null,
////    @field:Email
////    @field:Size(min = 5, max = 254)
////    val email: String? = null,
////    @field:Size(max = 50)
////    val firstName: String? = null,
////    @field:Size(max = 50)
////    val lastName: String? = null,
////    @field:Size(min = 2, max = 10)
////    val langKey: String? = null,
////    @field:Size(max = 256)
////    val imageUrl: String? = IMAGE_URL_DEFAULT,
////    val createdBy: String? = null,
////    val createdDate: Instant? = null,
////    val lastModifiedBy: String? = null,
////    val lastModifiedDate: Instant? = null,
////    val authorities: Set<String>? = null
////) {
////    companion object {
////        const val IMAGE_URL_DEFAULT = "http://placehold.it/50x50"
////        // Regex for acceptable logins
////        const val LOGIN_REGEX =
////            "^(?>[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*)|(?>[_.@A-Za-z0-9-]+)$"
////        const val PASSWORD_MIN: Int = 4
////        const val PASSWORD_MAX: Int = 16
////        @JvmStatic
////        val objectName: String = Account::class
////            .java
////            .simpleName
////            .run {
////                replaceFirst(
////                    first(),
////                    first().lowercaseChar()
////                )
////            }
////        const val LOGIN_FIELD = "login"
////        const val PASSWORD_FIELD = "password"
////        const val EMAIL_FIELD = "email"
////        const val ACCOUNT_AUTH_USER_ID_FIELD = "userId"
////        const val ACTIVATION_KEY_FIELD = "activationKey"
////        const val RESET_KEY_FIELD = "resetKey"
////        const val FIRST_NAME_FIELD = "firstName"
////        const val LAST_NAME_FIELD = "lastName"
////    }
////}
////
////fun Account.validate(
////    fields: Set<String>,
////    //si le validateur est null alors injecter celui du context
////    // mais faire cela a l'appel de la methode validate
////    //faire une extension function getValidator(exchange: ServerWebExchange?=null)
////    exchange: ServerWebExchange?,   //Pair<ApplicationContext,ServerWebExchange>
////    //si les deux sont null contruire soit meme le validator par defaut
////) = exchange.apply {
////    if (this == null) return emptySet<Map<String, String>>()
////}!!.validator.run {
////    fields.map { field ->
////        field to validateProperty(this@validate, field)
////    }.flatMap { violatedField ->
////        violatedField.second.map {
////            mapOf(
////                "objectName" to Account.objectName,
////                "field" to violatedField.first,
////                "message" to it.message
////            )
////        }
////    }.toSet()
////}