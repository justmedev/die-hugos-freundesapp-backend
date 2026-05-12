package repositories

import domain.entities.UserEntity
import domain.models.User
import domain.tables.UsersTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import service.user.CreateUserCommand

interface UserRepository {
    suspend fun create(cmd: CreateUserCommand): User
    suspend fun findById(id: Int): User?
    suspend fun findByEmail(email: String): User?
}

class UserRepositoryImpl : UserRepository {
    override suspend fun create(cmd: CreateUserCommand): User = suspendTransaction {
        User.from(UserEntity.new {
            email = cmd.email
            firstName = cmd.firstName
            lastName = cmd.lastName
            accountHolderName = cmd.accountHolderName
            accountIBAN = cmd.accountIBAN
            password = cmd.passwordHash
            birthdate = cmd.birthdate
            isAdmin = cmd.isAdmin
        })!!
    }

    override suspend fun findById(id: Int): User? = suspendTransaction {
        UserEntity.findById(id)?.let { User.from(it) }
    }

    override suspend fun findByEmail(email: String): User? = suspendTransaction {
        UserEntity.find { UsersTable.email eq email }.firstOrNull()?.let { User.from(it) }
    }
}