package domain.repositories

import domain.entities.CashpoolTransactionEntity
import domain.models.CashpoolTransaction
import domain.tables.CashpoolTransactionsTable
import domain.tables.CashpoolsTable
import domain.tables.UsersTable
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import service.cashpool_transaction.CreateCashpoolTransactionCommand
import service.cashpool_transaction.UpdateCashpoolTransactionCommand

interface CashpoolTransactionRepository {
    suspend fun create(cmd: CreateCashpoolTransactionCommand): CashpoolTransaction
    suspend fun findById(id: Int): CashpoolTransaction?
    suspend fun findByCashpoolId(cashpoolId: Int): List<CashpoolTransaction>
    suspend fun findByCashpoolIdAndOwnerId(cashpoolId: Int, ownerId: Int): List<CashpoolTransaction>
    suspend fun update(cmd: UpdateCashpoolTransactionCommand): CashpoolTransaction?
    suspend fun deleteById(id: Int)
}

class CashpoolTransactionRepositoryImpl : CashpoolTransactionRepository {
    override suspend fun create(cmd: CreateCashpoolTransactionCommand): CashpoolTransaction = suspendTransaction {
        CashpoolTransactionEntity.new {
            this.ownerId = EntityID(cmd.ownerId, UsersTable)
            this.cashpoolId = EntityID(cmd.cashpoolId, CashpoolsTable)
            this.label = cmd.label
            this.amountCents = cmd.amountCents
        }.let { CashpoolTransaction.from(it)!! }
    }

    override suspend fun findById(id: Int): CashpoolTransaction? = suspendTransaction {
        CashpoolTransactionEntity.findById(id)?.let { CashpoolTransaction.from(it) }
    }

    override suspend fun findByCashpoolId(cashpoolId: Int): List<CashpoolTransaction> = suspendTransaction {
        CashpoolTransactionEntity.find { CashpoolTransactionsTable.cashpool eq cashpoolId }
            .map { CashpoolTransaction.from(it)!! }
    }

    override suspend fun findByCashpoolIdAndOwnerId(cashpoolId: Int, ownerId: Int): List<CashpoolTransaction> {
        return CashpoolTransactionEntity.find {
            (CashpoolTransactionsTable.cashpool eq cashpoolId) and (CashpoolTransactionsTable.owner eq ownerId)
        }.map { CashpoolTransaction.from(it)!! }.toList()
    }

    override suspend fun update(cmd: UpdateCashpoolTransactionCommand): CashpoolTransaction? = suspendTransaction {
        CashpoolTransactionEntity.findById(cmd.transactionId)?.let {
            it.apply {
                label = cmd.label
                amountCents = cmd.amountCents
            }.let { entity -> CashpoolTransaction.from(entity)!! }
        }
    }

    override suspend fun deleteById(id: Int): Unit = suspendTransaction {
        CashpoolTransactionEntity.findById(id)?.delete()
    }
}