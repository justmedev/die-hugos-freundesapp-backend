package domain.models

import domain.entities.User

data class UserTokenPair(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)