package service.user

import core.exceptions.Forbidden
import core.exceptions.UserEmailAlreadyTaken
import core.exceptions.UserNotFound
import domain.commands.CreateUserCommand
import domain.commands.UpdateUserCommand
import domain.contexts.ServiceContext
import domain.models.User
import domain.policies.UserPolicy
import domain.repositories.UserRepository

class UserService(
    private val userRepo: UserRepository,
) {
    context(ctx: ServiceContext)
    suspend fun create(cmd: CreateUserCommand): User {
        if (!UserPolicy.canCreate()) {
            throw Forbidden("You are not allowed to create a user")
        }
        if (userRepo.findByEmail(cmd.email) != null) {
            throw UserEmailAlreadyTaken()
        }
        return userRepo.create(cmd)
    }

    context(ctx: ServiceContext)
    suspend fun findById(id: Int): User {
        if (UserPolicy.canView(id)) return userRepo.findById(id) ?: throw UserNotFound()
        throw Forbidden("You are not allowed to view this user")
    }

    context(ctx: ServiceContext)
    suspend fun findByEmail(email: String): User {
        val user = userRepo.findByEmail(email)
        if (UserPolicy.canView(user?.id)) return user ?: throw UserNotFound()
        throw Forbidden("You are not allowed to view this user")
    }

    context(ctx: ServiceContext)
    suspend fun findByKeycloakId(keycloakId: String): User {
        val user = userRepo.findByKeycloakId(keycloakId)
        if (UserPolicy.canView(user?.id)) return user ?: throw UserNotFound()
        throw Forbidden("You are not allowed to view this user")
    }

    /// Only use this internally when you cn be sure the user is allowed to find this user
    suspend fun findByKeycloakIdWithoutChecks(keycloakId: String): User =
        userRepo.findByKeycloakId(keycloakId) ?: throw UserNotFound()

    context(ctx: ServiceContext)
    suspend fun update(id: Int, cmd: UpdateUserCommand): User = userRepo.update(id, cmd) ?: throw UserNotFound()
}