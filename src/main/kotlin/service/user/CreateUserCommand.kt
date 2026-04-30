package service.user

import kotlinx.datetime.LocalDateTime

data class CreateUserCommand(
    val email: String,
    val firstName: String,
    val lastName: String,
    val passwordHash: String,
    val birthdate: LocalDateTime,
    val isAdmin: Boolean
)