package dto.user

import domain.models.User
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: Int,
    val email: String,
    val firstName: String,
    val lastName: String,
    val birthdate: String,
    val isAdmin: Boolean
) {
    companion object {
        fun from(entity: User) = UserResponse(
            id = entity.id,
            email = entity.email,
            firstName = entity.firstName,
            lastName = entity.lastName,
            birthdate = entity.birthdate.toString(),
            isAdmin = entity.isAdmin
        )
    }
}