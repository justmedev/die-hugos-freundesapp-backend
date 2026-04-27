package service.auth

import kotlinx.datetime.LocalDateTime

data class RegisterCommand(
    val email: String,
    val firstName: String,
    val lastName: String,
    val plaintextPassword: String,
    val birthdate: LocalDateTime,
    val isAdmin: Boolean
)