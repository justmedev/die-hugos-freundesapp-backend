package service.auth

import kotlinx.datetime.LocalDateTime

data class LoginCommand(
    val email: String,
    val password: String,
)