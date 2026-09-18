package domain.policies

import domain.commands.CreateCashpoolSettlementCommand
import domain.contexts.ServiceContext
import domain.models.CashpoolMember

object CashpoolSuggestedSettlementPolicy {

    context(ctx: ServiceContext)
    fun canCalculateSettlement(isMember: Boolean): Boolean {
        return ctx.calledInternallyOrByAdmin || isMember
    }

    context(ctx: ServiceContext)
    fun canView(isMember: Boolean): Boolean {
        return ctx.calledInternallyOrByAdmin || isMember
    }
}
