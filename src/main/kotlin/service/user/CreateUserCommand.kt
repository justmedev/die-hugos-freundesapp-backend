package service.user

import kotlinx.datetime.LocalDateTime

data class CreateUserCommand(
    val email: String,
    val firstName: String,
    val lastName: String,
    val accountHolderName: String? = null,
    val accountIBAN: String? = null,
    val passwordHash: String,
    val birthdate: LocalDateTime,
    val isAdmin: Boolean
)