package service.cashpool_transactions

import core.exceptions.NotaCashpoolMember
import domain.entities.CashpoolTransactionEntity
import domain.models.CashpoolTransaction
import domain.tables.CashpoolTransactionsTable
import domain.tables.CashpoolsTable
import domain.tables.UsersTable
import io.ktor.server.plugins.*
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import service.cashpool_members.CashpoolMemberService
import service.cashpools.CashpoolsService
import service.users.UsersService

class CashpoolTransactionService(
    private val usersService: UsersService,
    private val cashpoolsService: CashpoolsService,
    private val cashpoolMemberService: CashpoolMemberService,
) {

    suspend fun create(cmd: CreateCashpoolTransactionCommand): CashpoolTransaction {
        usersService.findById(cmd.ownerId) ?: throw NotFoundException("User not found")
        cashpoolsService.findById(cmd.cashpoolId) ?: throw NotFoundException("Cashpool not found")
        cashpoolMemberService.findByCashpoolIdAndUserId(cmd.cashpoolId, cmd.ownerId) ?: throw NotaCashpoolMember()

        return suspendTransaction {
            return@suspendTransaction CashpoolTransaction.from(CashpoolTransactionEntity.new {
                ownerId = EntityID(cmd.ownerId, UsersTable)
                cashpoolId = EntityID(cmd.cashpoolId, CashpoolsTable)
                label = cmd.label
                amountCents = cmd.amountCents
            })!!
        }
    }

    suspend fun findById(id: Int) = suspendTransaction {
        CashpoolTransaction.from(CashpoolTransactionEntity.find { CashpoolTransactionsTable.id eq id }.firstOrNull())
    }

    suspend fun findByCashpoolId(cashpoolId: Int) = suspendTransaction {
        CashpoolTransactionEntity.find { CashpoolTransactionsTable.cashpool eq cashpoolId }.orderBy(
            CashpoolTransactionsTable.createdAt to SortOrder.DESC
        ).map { CashpoolTransaction.from(it) }
    }
}