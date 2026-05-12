package domain.commands

data class LoginCommand(
    val email: String,
    val password: String,
)