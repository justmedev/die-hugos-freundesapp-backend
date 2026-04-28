package domain.models

data class UserTokenPair(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)