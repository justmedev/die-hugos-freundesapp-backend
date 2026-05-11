package dto.user

import domain.models.User
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: Int,
    val email: String,
    val firstName: String,
    val lastName: String,
    val accountHolderName: String?,
    val accountIBAN: String?,
    val birthdate: String,
    val isAdmin: Boolean,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(entity: User) = UserResponse(
            id = entity.id,
            email = entity.email,
            firstName = entity.firstName,
            lastName = entity.lastName,
            accountHolderName = entity.accountHolderName,
            accountIBAN = entity.accountIBAN,
            birthdate = entity.birthdate.toString(),
            isAdmin = entity.isAdmin,
            createdAt = entity.createdAt,
        )
    }
}