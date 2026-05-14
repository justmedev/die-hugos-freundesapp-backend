package domain.commands

import domain.commands.validations.UserValidations
import domain.models.valueobjects.IBAN
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

data class UpdateUserCommand(
    val email: String,
    val firstName: String,
    val lastName: String,
    val accountHolderName: String? = null,
    val accountIBAN: IBAN? = null,
    val birthdate: LocalDate,
) {
    init {
        val validation = UserValidations.validateUpdateUserCommand(this)
        if (!validation.isValid) throw IllegalArgumentException(validation.errors.joinToString())
    }
}