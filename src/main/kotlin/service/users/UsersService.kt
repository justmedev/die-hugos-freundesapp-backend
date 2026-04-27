package service.users

import domain.entities.User
import domain.tables.Users
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import service.Service

class UsersService : Service {
    override suspend fun initSchema() {
        suspendTransaction {
            SchemaUtils.create(Users)
        }
    }

    suspend fun create(cmd: CreateUserCommand) {
        suspendTransaction {
            User.new {
                email = cmd.email
                firstName = cmd.firstName
                lastName = cmd.lastName
                password = cmd.password
                birthdate = cmd.birthdate
                isAdmin = cmd.isAdmin
            }
        }
    }

    fun get(id: Int) = User.findById(id)
}