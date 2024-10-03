package webapp.users.signup

import webapp.users.password.UserPassword

data class Signup(
    val login: String,
    val password: String,
    val repassord:String,
    val email:String,
)