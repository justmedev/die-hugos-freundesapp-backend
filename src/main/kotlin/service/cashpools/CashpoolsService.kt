package service.cashpools

import core.exceptions.NotaCashpoolMember
import domain.entities.CashpoolEntity
import domain.models.Cashpool
import domain.tables.CashpoolMembersTable
import domain.tables.CashpoolsTable
import domain.tables.UsersTable
import io.ktor.server.plugins.*
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import service.users.UsersService

class CashpoolsService(
    private val usersService: UsersService
) {
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
        suspendTransaction { Cashpool.from(CashpoolEntity.findById(id)) }

    suspend fun findByIdOnlyIfMember(id: Int, userId: Int) = suspendTransaction {
        val cashpool = CashpoolEntity.find {
            CashpoolsTable.id eq id
        }.firstOrNull()

        val isMember = CashpoolMembersTable.select(CashpoolMembersTable.id)
            .where { (CashpoolMembersTable.cashpool eq id) and (CashpoolMembersTable.user eq userId) }
            .limit(1)
            .any()

        if (!isMember) throw NotaCashpoolMember()
        Cashpool.from(cashpool)
    }

    suspend fun findAll() = suspendTransaction { CashpoolEntity.all().orderBy(
        CashpoolsTable.createdAt to SortOrder.DESC
    ).map { Cashpool.from(it) } }
}