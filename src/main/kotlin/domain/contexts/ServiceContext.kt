package domain.contexts

import domain.models.User

@ConsistentCopyVisibility
data class ServiceContext private constructor(
    val nullableUser: User?,
    val isCalledInternally: Boolean = false
) {
    companion object {
        fun internal(): ServiceContext = ServiceContext(null, true)
        fun internal(user: User): ServiceContext = ServiceContext(user, true)
        fun external(user: User): ServiceContext = ServiceContext(user, false)
    }

    val user: User get() = nullableUser ?: throw IllegalStateException("User is null! Always check if a call is internal before accessing user")

    val calledInternallyOrByAdmin: Boolean get() = isCalledInternally || user.isAdmin
}