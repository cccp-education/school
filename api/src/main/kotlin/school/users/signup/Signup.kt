package school.users.signup

import org.springframework.context.ApplicationContext
import school.base.property.ANONYMOUS_USER
import school.users.User
import school.users.security.Role
import java.util.*

data class Signup(
    val login: String,
    val password: String,
    val repassword:String,
    val email:String,
)

fun ApplicationContext.fromSignupToUser(signup: Signup): User {
    // Validation du mot de passe et de la confirmation
    require(signup.password == signup.repassword) { "Passwords do not match!" }

    // Création d'un utilisateur à partir des données de Signup
    return User(
        id = UUID.randomUUID(), // Génération d'un UUID
        login = signup.login,
        password = hashPassword(signup.password), // Remplacez par une fonction de hachage réelle
        email = signup.email,
        roles = mutableSetOf(Role(ANONYMOUS_USER)), // Role par défaut
        langKey = "en" // Valeur par défaut, ajustez si nécessaire
    )
}

// Fonction fictive de hachage du mot de passe
fun hashPassword(password: String): String {
    // Implémentez une fonction de hachage ici, par exemple en utilisant BCrypt
    return password // Remplacez par le mot de passe haché
}
