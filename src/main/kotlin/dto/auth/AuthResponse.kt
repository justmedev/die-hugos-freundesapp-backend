package dto.auth

import domain.entities.UserEntity
import domain.models.UserTokenPair
import dto.user.UserResponse
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val accessToken: String, val refreshToken: String, val user: UserResponse
) {
    companion object {
        fun from(tp: UserTokenPair, user: UserEntity) = AuthResponse(tp.accessToken, tp.refreshToken, UserResponse.from(user))
    }
}