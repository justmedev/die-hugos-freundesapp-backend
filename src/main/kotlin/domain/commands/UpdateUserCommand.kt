package domain.commands

import kotlinx.datetime.LocalDateTime

data class UpdateUserCommand(
    val email: String,
    val firstName: String,
    val lastName: String,
    val accountHolderName: String? = null,
    val accountIBAN: String? = null,
    val birthdate: LocalDateTime,
)