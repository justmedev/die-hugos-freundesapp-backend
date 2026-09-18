package service.cashpool_settlement

import domain.commands.CreateCashpoolSettlementCommand
import domain.contexts.ServiceContext
import domain.models.CashpoolSettlement
import domain.policies.CashpoolSettlementPolicy
import domain.repositories.CashpoolSettlementRepository
import service.cashpool.CashpoolService

class CashpoolSettlementService(
    private val settlementRepo: CashpoolSettlementRepository,
    private val cashpoolService: CashpoolService,
) {
    context(ctx: ServiceContext)
    suspend fun create(cmd: CreateCashpoolSettlementCommand): CashpoolSettlement {
        CashpoolSettlementPolicy.canCreate(
            cmd,
            cashpoolService.isMember(cmd.cashpoolId, cmd.fromId),
            cashpoolService.isMember(cmd.cashpoolId, cmd.toId)
        )
        return settlementRepo.create(cmd)
    }

    context(ctx: ServiceContext)
    suspend fun findByCashpoolId(cashpoolId: Int): List<CashpoolSettlement> {
        CashpoolSettlementPolicy.canView(cashpoolService.isMember(cashpoolId, ctx.user.id))
        return settlementRepo.findByCashpoolId(cashpoolId)
    }
}