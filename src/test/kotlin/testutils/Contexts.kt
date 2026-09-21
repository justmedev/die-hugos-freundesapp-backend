package testutils

import domain.contexts.ServiceContext
import domain.models.User

object Contexts {
    val internal: ServiceContext get() = ServiceContext.internal()
    val default: ServiceContext get() = ServiceContext.external(Users.nonAdminUser)
    fun of(user: User): ServiceContext = ServiceContext.external(user)
    fun of(userId: Int, isAdmin: Boolean = false): ServiceContext = ServiceContext.external(Users.nonAdminUser.copy(id = userId, isAdmin = isAdmin))
}
