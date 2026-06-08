package domain.commands

import domain.commands.validations.UserValidations
import domain.models.valueobjects.IBAN
import kotlinx.datetime.LocalDate

data class CreateUserCommand(
    val authentikId: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val accountHolderName: String? = null,
    val accountIBAN: IBAN? = null,
    val passwordHash: String,
    val birthdate: LocalDate,
    val isAdmin: Boolean
) {
    init {
        val validation = UserValidations.validateCreateUserCommand(this)
        if (!validation.isValid) throw IllegalArgumentException(validation.errors.joinToString())
    }
}