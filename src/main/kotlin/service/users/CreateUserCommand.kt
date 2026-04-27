package service.users

import kotlinx.datetime.LocalDateTime

data class CreateUserCommand(
    val email: String,
    val firstName: String,
    val lastName: String,
    val password: String,
    val birthdate: LocalDateTime,
    val isAdmin: Boolean
)