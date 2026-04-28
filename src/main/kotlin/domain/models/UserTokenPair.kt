package domain.models

import domain.entities.UserEntity

data class UserTokenPair(
    val accessToken: String,
    val refreshToken: String,
    val user: UserEntity
)