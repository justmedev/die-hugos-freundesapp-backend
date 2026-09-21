package service.user

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
        UserPolicy.canCreate()
        if (userRepo.findByEmail(cmd.email) != null) {
            throw UserEmailAlreadyTaken()
        }
        return userRepo.create(cmd)
    }

    context(ctx: ServiceContext)
    suspend fun findById(id: Int): User {
        UserPolicy.canView(id)
        return userRepo.findById(id) ?: throw UserNotFound()
    }

    context(ctx: ServiceContext)
    suspend fun findByEmail(email: String): User {
        val user = userRepo.findByEmail(email)
        UserPolicy.canView(user?.id)
        return user ?: throw UserNotFound()
    }

    context(ctx: ServiceContext)
    suspend fun findByKeycloakId(keycloakId: String): User {
        val user = userRepo.findByKeycloakId(keycloakId)
        UserPolicy.canView(user?.id)
        return user ?: throw UserNotFound()
    }

    context(ctx: ServiceContext)
    suspend fun update(id: Int, cmd: UpdateUserCommand): User {
        UserPolicy.canUpdate(id)
        return userRepo.update(id, cmd) ?: throw UserNotFound()
    }
}