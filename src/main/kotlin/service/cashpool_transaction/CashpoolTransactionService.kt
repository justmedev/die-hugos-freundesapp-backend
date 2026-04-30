package service.cashpool_transaction

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
import service.cashpool_member.CashpoolMemberService
import service.cashpool.CashpoolService
import service.user.UserService

class CashpoolTransactionService(
    private val userService: UserService,
    private val cashpoolService: CashpoolService,
    private val cashpoolMemberService: CashpoolMemberService,
) {

    suspend fun create(cmd: CreateCashpoolTransactionCommand): CashpoolTransaction {
        checkIfUserCashpoolExistsAndUserIsMember(cmd.ownerId, cmd.cashpoolId)

        return suspendTransaction {
            return@suspendTransaction CashpoolTransaction.from(CashpoolTransactionEntity.new {
                ownerId = EntityID(cmd.ownerId, UsersTable)
                cashpoolId = EntityID(cmd.cashpoolId, CashpoolsTable)
                label = cmd.label
                amountCents = cmd.amountCents
            })!!
        }
    }

    suspend fun update(cmd: UpdateCashpoolTransactionCommand): CashpoolTransaction {
        checkIfUserCashpoolExistsAndUserIsMember(cmd.ownerId, cmd.cashpoolId)
        val transaction = suspendTransaction { CashpoolTransactionEntity.findById(cmd.transactionId) } ?: throw NotFoundException("Transaction not found")

        return suspendTransaction {
            return@suspendTransaction CashpoolTransaction.from(transaction.apply {
                label = cmd.label
                amountCents = cmd.amountCents
            })!!
        }
    }

    private suspend fun checkIfUserCashpoolExistsAndUserIsMember(userId: Int, cashpoolId: Int) {
        userService.findById(userId) ?: throw NotFoundException("User not found")
        cashpoolService.findById(cashpoolId) ?: throw NotFoundException("Cashpool not found")
        cashpoolMemberService.findByCashpoolIdAndUserId(cashpoolId, userId) ?: throw NotaCashpoolMember()
    }

    suspend fun findById(id: Int) = suspendTransaction {
        CashpoolTransaction.from(CashpoolTransactionEntity.findById(id))
    }

    suspend fun findByCashpoolId(cashpoolId: Int) = suspendTransaction {
        CashpoolTransactionEntity.find { CashpoolTransactionsTable.cashpool eq cashpoolId }.orderBy(
            CashpoolTransactionsTable.createdAt to SortOrder.DESC
        ).map { CashpoolTransaction.from(it) }
    }
}