package webapp.accounts.exceptions

class UsernameAlreadyUsedException :
    RuntimeException("Login name already used!") {
    companion object {
        private const val serialVersionUID = 1L
    }
}