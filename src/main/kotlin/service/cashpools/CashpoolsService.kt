package service.cashpools

import domain.entities.CashpoolEntity
import domain.models.Cashpool
import domain.models.CashpoolWithMemberFlag
import domain.tables.CashpoolMembersTable
import domain.tables.CashpoolsTable
import domain.tables.UsersTable
import io.ktor.server.plugins.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import service.Service
import service.users.UsersService

class CashpoolsService(
    private val usersService: UsersService
) : Service {
    init {
        runBlocking { initSchema() }
    }

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

    suspend fun findById(id: Int) =
        suspendTransaction { Cashpool.from(CashpoolEntity.find { CashpoolsTable.id eq id }.firstOrNull()) }

    suspend fun findByIdWithMemberFlag(id: Int, userId: Int) = suspendTransaction {
        val cashpool = CashpoolEntity.find {
            CashpoolsTable.id eq id
        }.firstOrNull()

        val isMember = CashpoolMembersTable.select(CashpoolMembersTable.id)
            .where { (CashpoolMembersTable.cashpool eq id) and (CashpoolMembersTable.user eq userId) }
            .limit(1)
            .any()

        CashpoolWithMemberFlag.from(cashpool, isMember)
    }

    suspend fun findAll() = suspendTransaction { CashpoolEntity.all().map { Cashpool.from(it) } }
}