package service.auth

data class LoginCommand(
    val email: String,
    val password: String,
)