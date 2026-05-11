package dto.user

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class CreateUserRequest(
    val email: String,
    val firstName: String,
    val lastName: String,
    val accountHolderName: String? = null,
    val accountIBAN: String? = null,
    val password: String,
    val birthDate: LocalDateTime,
    val isAdmin: Boolean = false
)