package service.auth

data class RefreshCommand(
    val refreshToken: String,
)