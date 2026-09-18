package service.cashpool_settlement

import core.exceptions.Forbidden
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
        if (!CashpoolSettlementPolicy.canCreate(
                cmd,
                cashpoolService.isMember(cmd.cashpoolId, cmd.fromId),
                cashpoolService.isMember(cmd.cashpoolId, cmd.toId)
            )
        ) {
            throw Forbidden("User ${ctx.user.id} cannot create cashpool settlement for user ${cmd.fromId}")
        }
        return settlementRepo.create(cmd)
    }

    context(ctx: ServiceContext)
    suspend fun findByCashpoolId(cashpoolId: Int): List<CashpoolSettlement> {
        if (!CashpoolSettlementPolicy.canView(cashpoolService.isMember(cashpoolId, ctx.user.id))) {
            throw Forbidden("User ${ctx.user.id} cannot view cashpool settlements")
        }
        return settlementRepo.findByCashpoolId(cashpoolId)
    }
}