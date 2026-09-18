package domain.policies

import core.exceptions.Forbidden
import core.exceptions.NotaCashpoolMember
import domain.contexts.ServiceContext

object CashpoolSuggestedSettlementPolicy {

    context(ctx: ServiceContext)
    fun canCalculateSettlement(isMember: Boolean) {
        if (ctx.calledInternallyOrByAdmin) return
        if (!isMember) throw NotaCashpoolMember()
    }

    context(ctx: ServiceContext)
    fun canCalculateUserSettlementSummary(forUserId: Int) {
        if (ctx.calledInternallyOrByAdmin) return
        if (forUserId != ctx.user.id) throw Forbidden("You are only allowed to access your own summary!");
    }
}
