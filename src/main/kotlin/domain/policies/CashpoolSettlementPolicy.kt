package domain.policies

import core.exceptions.Forbidden
import core.exceptions.NotaCashpoolMember
import domain.commands.CreateCashpoolSettlementCommand
import domain.contexts.ServiceContext

object CashpoolSettlementPolicy {

    context(ctx: ServiceContext)
    fun canCreate(cmd: CreateCashpoolSettlementCommand, isFromMember: Boolean, isToMember: Boolean) {
        if (ctx.calledInternallyOrByAdmin) return
        if (!isFromMember) throw NotaCashpoolMember(cmd.fromId)
        if (!isToMember) throw NotaCashpoolMember(cmd.toId)
        if (cmd.fromId != ctx.user.id) throw Forbidden("User ${cmd.fromId} is not allowed to create a settlement in this cashpool!")
    }

    context(ctx: ServiceContext)
    fun canView(isMember: Boolean) {
        if (ctx.calledInternallyOrByAdmin) return
        if (!isMember) throw NotaCashpoolMember(ctx.user.id)
    }
}
