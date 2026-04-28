package dto.auth

import domain.models.User
import domain.models.UserTokenPair
import dto.user.UserResponse
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val accessToken: String, val refreshToken: String, val user: UserResponse
) {
    companion object {
        fun from(tp: UserTokenPair, user: User) = AuthResponse(tp.accessToken, tp.refreshToken, UserResponse.from(user))
    }
}