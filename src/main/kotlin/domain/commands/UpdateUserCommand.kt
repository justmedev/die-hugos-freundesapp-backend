package domain.commands

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
        require(email.contains("@") && email.length >= 3) { "Invalid email format" }
        require(firstName.isNotBlank()) { "First name cannot be blank" }
        require(lastName.isNotBlank()) { "Last name cannot be blank" }
        require(birthdate > Clock.System.todayIn(TimeZone.UTC)) { "Birthdate cannot be in the future" }
        accountHolderName?.let { require(it.isNotBlank()) { "Account holder name cannot be blank if specified" } }
    }
}