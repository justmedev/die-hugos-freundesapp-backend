package dto.user

import domain.models.valueobjects.IBAN
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class CreateUserRequest(
    val email: String,
    val firstName: String,
    val lastName: String,
    val accountHolderName: String? = null,
    val accountIBAN: IBAN? = null,
    val password: String,
    val birthdate: LocalDate,
    val isAdmin: Boolean = false
)