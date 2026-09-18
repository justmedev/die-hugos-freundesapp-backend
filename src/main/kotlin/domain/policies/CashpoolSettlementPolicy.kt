package domain.policies

import domain.commands.CreateCashpoolMemberCommand
import domain.commands.CreateCashpoolSettlementCommand
import domain.contexts.ServiceContext
import domain.models.CashpoolMember

object CashpoolSettlementPolicy {

    context(ctx: ServiceContext)
    fun canCreate(cmd: CreateCashpoolSettlementCommand): Boolean {
        return ctx.user.isAdmin || cmd.fromId == ctx.user.id
    }

    context(ctx: ServiceContext)
    fun canView(member: CashpoolMember?): Boolean {
        return ctx.user.isAdmin || member?.user?.id == ctx.user.id
    }
}
