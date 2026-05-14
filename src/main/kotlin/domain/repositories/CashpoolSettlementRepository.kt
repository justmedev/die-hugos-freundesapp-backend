package domain.repositories

import domain.commands.CreateCashpoolSettlementCommand
import domain.entities.CashpoolSettlementEntity
import domain.models.CashpoolSettlement
import domain.tables.CashpoolSettlementsTable
import domain.tables.CashpoolsTable
import domain.tables.UsersTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

interface CashpoolSettlementRepository {
    suspend fun create(cmd: CreateCashpoolSettlementCommand): CashpoolSettlement
    suspend fun findByCashpoolId(cashpoolId: Int): List<CashpoolSettlement>
    // TODO: suspend fun update(cmd: UpdateCashpoolTransactionCommand): CashpoolTransaction?
    // TODO: suspend fun deleteById(id: Int)
}

class CashpoolSettlementRepositoryImpl : CashpoolSettlementRepository {
    override suspend fun create(cmd: CreateCashpoolSettlementCommand): CashpoolSettlement = suspendTransaction {
        CashpoolSettlementEntity.new {
            this.fromId = EntityID(cmd.fromId, UsersTable)
            this.toId = EntityID(cmd.toId, UsersTable)
            this.cashpoolId = EntityID(cmd.cashpoolId, CashpoolsTable)
            this.purpose = cmd.purpose
            this.amountCents = cmd.amountCents
        }.let { CashpoolSettlement.from(it)!! }
    }

    override suspend fun findByCashpoolId(cashpoolId: Int): List<CashpoolSettlement> = suspendTransaction {
        CashpoolSettlementEntity.find { CashpoolSettlementsTable.cashpool eq cashpoolId }
            .map { CashpoolSettlement.from(it)!! }
    }
}