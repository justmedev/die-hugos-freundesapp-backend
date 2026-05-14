package service.cashpool_settlement

import domain.commands.CreateCashpoolSettlementCommand
import domain.models.CashpoolSettlement
import domain.repositories.CashpoolSettlementRepository
import service.cashpool.CashpoolService

class CashpoolSettlementService(
    private val settlementRepo: CashpoolSettlementRepository,
    private val cashpoolService: CashpoolService,
) {
    suspend fun create(cmd: CreateCashpoolSettlementCommand): CashpoolSettlement {
        cashpoolService.requireMembership(cmd.cashpoolId, cmd.fromId)
        cashpoolService.requireMembership(cmd.cashpoolId, cmd.toId)
        return settlementRepo.create(cmd)
    }

    suspend fun findByCashpoolId(cashpoolId: Int, requestingUserId: Int): List<CashpoolSettlement> {
        cashpoolService.requireMembership(cashpoolId, requestingUserId)
        return settlementRepo.findByCashpoolId(cashpoolId)
    }
}