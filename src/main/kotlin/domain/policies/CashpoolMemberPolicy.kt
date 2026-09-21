package domain.policies

import core.exceptions.Forbidden
import domain.commands.CreateCashpoolMemberCommand
import domain.contexts.ServiceContext
import domain.models.CashpoolMember

object CashpoolMemberPolicy {

    context(ctx: ServiceContext)
    fun canCreate(cmd: CreateCashpoolMemberCommand) {
        if (ctx.isCalledInternally) return
        if (cmd.userId != ctx.user.id) throw Forbidden("User ${ctx.user.id} is not allowed to create a cashpool member for another user (${cmd.userId}).")
    }

    context(ctx: ServiceContext)
    fun canView(member: CashpoolMember?) {
        if (ctx.isCalledInternally) return
        if (member == null || member.user.id != ctx.user.id) throw Forbidden("User ${ctx.user.id} is not allowed to view cashpool member (${member?.user?.id}).")
    }
}
