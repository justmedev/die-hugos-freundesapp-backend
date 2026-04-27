package dto

import domain.entities.User

data class UserDto(
    val id: Int,
    val email: String,
    val firstName: String,
    val lastName: String,
    val birthdate: String,
    val isAdmin: Boolean
) {
    companion object {
        fun from(entity: User) = UserDto(
            id = entity.id.value,
            email = entity.email,
            firstName = entity.firstName,
            lastName = entity.lastName,
            birthdate = entity.birthdate.toString(),
            isAdmin = entity.isAdmin
        )
    }
}