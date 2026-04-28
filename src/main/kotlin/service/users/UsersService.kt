package service.users

import domain.entities.UserEntity
import domain.tables.UsersTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import service.Service

class UsersService : Service {
    override suspend fun initSchema() {
        suspendTransaction {
            SchemaUtils.create(UsersTable)
        }
    }

    suspend fun create(cmd: CreateUserCommand): UserEntity {
        return suspendTransaction {
            return@suspendTransaction UserEntity.new {
                email = cmd.email
                firstName = cmd.firstName
                lastName = cmd.lastName
                password = cmd.passwordHash
                birthdate = cmd.birthdate
                isAdmin = cmd.isAdmin
            }
        }
    }

    fun findById(id: Int) = transaction { UserEntity.find { UsersTable.id eq id }.firstOrNull() }
    fun findByEmail(email: String) = transaction { UserEntity.find { UsersTable.email eq email }.firstOrNull() }
}