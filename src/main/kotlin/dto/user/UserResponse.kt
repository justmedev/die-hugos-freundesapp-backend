package dto.user

import domain.entities.UserEntity
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
        fun from(entity: UserEntity) = UserResponse(
            id = entity.id.value,
            email = entity.email,
            firstName = entity.firstName,
            lastName = entity.lastName,
            birthdate = entity.birthdate.toString(),
            isAdmin = entity.isAdmin
        )
    }
}