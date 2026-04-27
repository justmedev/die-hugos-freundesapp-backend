package service.users

import domain.entities.User
import domain.tables.Users
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import service.Service

class UsersService : Service {
    override suspend fun initSchema() {
        suspendTransaction {
            SchemaUtils.create(Users)
        }
    }

    suspend fun create(cmd: CreateUserCommand): User {
        return suspendTransaction {
            return@suspendTransaction User.new {
                email = cmd.email
                firstName = cmd.firstName
                lastName = cmd.lastName
                password = cmd.passwordHash
                birthdate = cmd.birthdate
                isAdmin = cmd.isAdmin
            }
        }
    }

    fun findById(id: Int) = transaction { User.find { Users.id eq id }.firstOrNull() }
    fun findByEmail(email: String) = transaction { User.find { Users.email eq email }.firstOrNull() }
}