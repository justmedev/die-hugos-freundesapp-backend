package service.user

import core.exceptions.UserEmailAlreadyTaken
import core.exceptions.UserNotFound
import domain.commands.CreateUserCommand
import domain.commands.UpdateUserCommand
import domain.models.User
import domain.repositories.UserRepository

class UserService(
    private val userRepo: UserRepository,
) {
    suspend fun create(cmd: CreateUserCommand): User {
        if (userRepo.findByEmail(cmd.email) != null) {
            throw UserEmailAlreadyTaken()
        }
        return userRepo.create(cmd)
    }

    suspend fun findById(id: Int) = userRepo.findById(id) ?: throw UserNotFound()

    suspend fun findByEmail(email: String) = userRepo.findByEmail(email) ?: throw UserNotFound()

    suspend fun update(id: Int, cmd: UpdateUserCommand): User = userRepo.update(id, cmd) ?: throw UserNotFound()
}