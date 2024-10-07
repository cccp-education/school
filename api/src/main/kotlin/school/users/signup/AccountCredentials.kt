//@file:Suppress("unused")
//
//package school.users.signup
//
//import jakarta.validation.constraints.*
//import school.base.property.IMAGE_URL_DEFAULT
//import school.base.property.LOGIN_REGEX
//import school.base.property.PASSWORD_MAX
//import school.base.property.PASSWORD_MIN
//import school.accounts.models.AccountUtils.objectName
//import java.time.Instant
//import java.util.*
//
///**
// * Représente l'account domain model avec le password et l'activationKey
// * pour la vue
// */
//data class AccountCredentials(
//    val id: UUID? = null,
//    @field:Size(min = PASSWORD_MIN, max = PASSWORD_MAX)
//    @field:NotNull
//    val password: String? = null,
//    @field:NotBlank
//    @field:Pattern(regexp = LOGIN_REGEX)
//    @field:Size(min = 1, max = 50)
//    val login: String? = null,
//    @field:Email
//    @field:Size(min = 5, max = 254)
//    val email: String? = null,
//    @field:Size(max = 50)
//    val firstName: String? = null,
//    @field:Size(max = 50)
//    val lastName: String? = null,
//    @field:Size(min = 2, max = 10)
//    val langKey: String? = null,
//    @field:Size(max = 256)
//    val imageUrl: String? = IMAGE_URL_DEFAULT,
//    val activationKey: String? = null,
//    val resetKey: String? = null,
//    val activated: Boolean = false,
//    val createdBy: String? = null,
//    val createdDate: Instant? = null,
//    val lastModifiedBy: String? = null,
//    val lastModifiedDate: Instant? = null,
//    val authorities: Set<String>? = null
//) {
//
//    constructor(account: Account) : this(
//        id = account.id,
//        login = account.login,
//        email = account.email,
//        firstName = account.firstName,
//        lastName = account.lastName,
//        langKey = account.langKey,
//        activated = account.activated,
//        createdBy = account.createdBy,
//        createdDate = account.createdDate,
//        lastModifiedBy = account.lastModifiedBy,
//        lastModifiedDate = account.lastModifiedDate,
//        imageUrl = account.imageUrl,
//        authorities = account.authorities?.map { it }?.toMutableSet()
//    )
//
//    companion object {
//        @JvmStatic
//        val objectName = AccountCredentials::class.java.simpleName.objectName
//    }
//}
//
//
//fun AccountCredentials.toAccount(): Account = Account(
//    id = id,
//    login = login,
//    firstName = firstName,
//    lastName = lastName,
//    email = email,
//    activated = activated,
//    langKey = langKey,
//    createdBy = createdBy,
//    createdDate = createdDate,
//    lastModifiedBy = lastModifiedBy,
//    lastModifiedDate = lastModifiedDate,
//    authorities = authorities
//)
//
