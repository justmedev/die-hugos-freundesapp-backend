package service.cashpools

import domain.entities.CashpoolEntity
import domain.models.Cashpool
import domain.tables.CashpoolsTable
import domain.tables.UsersTable
import io.ktor.server.plugins.*
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import service.Service
import service.users.UsersService

class CashpoolsService(
    private val usersService: UsersService
) : Service {
    override suspend fun initSchema() {
        suspendTransaction {
            SchemaUtils.create(CashpoolsTable)
        }
    }

    suspend fun create(cmd: CreateCashpoolCommand): Cashpool {
        usersService.findById(cmd.ownerId) ?: throw NotFoundException("User not found")

        return suspendTransaction {
            return@suspendTransaction Cashpool.from(CashpoolEntity.new {
                title = cmd.title
                description = cmd.description
                ownerId = EntityID(cmd.ownerId, UsersTable)
            })!!
        }
    }

    suspend fun findById(id: Int) = suspendTransaction { Cashpool.from(CashpoolEntity.find { CashpoolsTable.id eq id }.firstOrNull()) }
}