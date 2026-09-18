package testutils

import domain.contexts.ServiceContext
import domain.models.User

object Contexts {
    val default: ServiceContext get() = ServiceContext(Users.nonAdminUser)
    fun of(user: User): ServiceContext = ServiceContext(user)
    fun of(userId: Int, isAdmin: Boolean = false): ServiceContext = ServiceContext(Users.nonAdminUser.copy(id = userId, isAdmin = isAdmin))
}
