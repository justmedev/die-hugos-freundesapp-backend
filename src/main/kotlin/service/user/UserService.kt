package service.user

import core.exceptions.UserNotFound
import domain.models.User
import domain.repositories.UserRepository

class UserService(
    private val userRepo: UserRepository,
) {
    suspend fun create(cmd: CreateUserCommand): User = userRepo.create(cmd)

    suspend fun findById(id: Int) = userRepo.findById(id) ?: throw UserNotFound()

    suspend fun findByEmail(email: String) = userRepo.findByEmail(email) ?: throw UserNotFound()
}