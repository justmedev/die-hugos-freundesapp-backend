package domain.commands

import kotlinx.datetime.LocalDateTime

data class RegisterCommand(
    val email: String,
    val firstName: String,
    val lastName: String,
    val accountHolderName: String? = null,
    val accountIBAN: String? = null,
    val plaintextPassword: String,
    val birthdate: LocalDateTime,
    val isAdmin: Boolean
)