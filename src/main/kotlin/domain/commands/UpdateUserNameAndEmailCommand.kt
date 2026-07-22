package domain.commands

import domain.commands.validations.UserValidations
import domain.models.valueobjects.IBAN
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

data class UpdateUserNameAndEmailCommand(
    val email: String,
    val firstName: String,
    val lastName: String,
) {
    init {
        val validation = UserValidations.validateUpdateUserNameAndEmailCommand(this)
        if (!validation.isValid) throw IllegalArgumentException(validation.errors.joinToString())
    }
}