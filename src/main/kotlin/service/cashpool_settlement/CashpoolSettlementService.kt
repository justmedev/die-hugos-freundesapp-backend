package service.cashpool_settlement

import core.exceptions.Forbidden
import domain.commands.CreateCashpoolSettlementCommand
import domain.contexts.ServiceContext
import domain.models.CashpoolSettlement
import domain.policies.CashpoolMemberPolicy
import domain.policies.CashpoolSettlementPolicy
import domain.repositories.CashpoolSettlementRepository
import service.cashpool.CashpoolService

class CashpoolSettlementService(
    private val settlementRepo: CashpoolSettlementRepository,
    private val cashpoolService: CashpoolService,
) {
    context(ctx: ServiceContext)
    suspend fun create(cmd: CreateCashpoolSettlementCommand): CashpoolSettlement {
        if (!CashpoolSettlementPolicy.canCreate(cmd)) {
            throw Forbidden("User ${ctx.user.id} cannot create cashpool settlement for user ${cmd.fromId}")
        }
        cashpoolService.requireMembership(cmd.cashpoolId, cmd.fromId)
        cashpoolService.requireMembership(cmd.cashpoolId, cmd.toId)
        return settlementRepo.create(cmd)
    }

    context(ctx: ServiceContext)
    suspend fun findByCashpoolId(cashpoolId: Int): List<CashpoolSettlement> {
        cashpoolService.requireMembership(cashpoolId, ctx.user.id)
        return settlementRepo.findByCashpoolId(cashpoolId)
    }
}