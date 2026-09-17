package domain.contexts

import domain.models.User

data class ServiceContext(
    val user: User
)