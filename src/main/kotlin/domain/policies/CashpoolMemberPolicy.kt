package domain.policies

import domain.commands.CreateCashpoolMemberCommand
import domain.contexts.ServiceContext
import domain.models.CashpoolMember

object CashpoolMemberPolicy {

    context(ctx: ServiceContext)
    fun canCreate(cmd: CreateCashpoolMemberCommand): Boolean {
        return ctx.calledInternallyOrByAdmin || cmd.userId == ctx.user.id
    }

    context(ctx: ServiceContext)
    fun canView(member: CashpoolMember?): Boolean {
        return ctx.calledInternallyOrByAdmin || member?.user?.id == ctx.user.id
    }
}
