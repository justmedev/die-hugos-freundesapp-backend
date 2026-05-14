package domain.commands

data class LoginCommand(
    val email: String,
    val password: String,
) {
    init {
        require(email.contains("@") && email.length >= 3) { "Invalid email format" }
        require(password.isNotBlank()) { "Password must not be blank" }
    }
}