package service.users

import domain.entities.UserEntity
import domain.models.User
import domain.tables.UsersTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class UsersService {
    suspend fun create(cmd: CreateUserCommand): User {
        return suspendTransaction {
            return@suspendTransaction User.from(UserEntity.new {
                email = cmd.email
                firstName = cmd.firstName
                lastName = cmd.lastName
                password = cmd.passwordHash
                birthdate = cmd.birthdate
                isAdmin = cmd.isAdmin
            }) as User
        }
    }

    suspend fun findById(id: Int) =
        suspendTransaction { User.from(UserEntity.find { UsersTable.id eq id }.firstOrNull()) }

    suspend fun findByEmail(email: String) =
        suspendTransaction { User.from(UserEntity.find { UsersTable.email eq email }.firstOrNull()) }
}