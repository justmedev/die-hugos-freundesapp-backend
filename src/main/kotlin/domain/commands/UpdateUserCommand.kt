package domain.commands

import domain.models.valueobjects.IBAN
import kotlinx.datetime.LocalDateTime

data class UpdateUserCommand(
    val email: String,
    val firstName: String,
    val lastName: String,
    val accountHolderName: String? = null,
    val accountIBAN: IBAN? = null,
    val birthdate: LocalDateTime,
)