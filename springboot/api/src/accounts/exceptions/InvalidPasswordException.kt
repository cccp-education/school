package webapp.accounts.exceptions

class InvalidPasswordException :
    RuntimeException("Incorrect password") {
    companion object {
        private const val serialVersionUID = 1L
    }
}