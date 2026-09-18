package domain.policies

import core.exceptions.Forbidden
import core.exceptions.NotaCashpoolMember
import core.exceptions.NotCashpoolOwner
import domain.commands.CreateCashpoolCommand
import domain.contexts.ServiceContext
import domain.models.Cashpool

object CashpoolPolicy {

    context(ctx: ServiceContext)
    fun canCreate(cmd: CreateCashpoolCommand) {
        if (ctx.calledInternallyOrByAdmin) return
        if (cmd.ownerId != ctx.user.id) throw Forbidden("You are not allowed to create a cashpool for another user")
    }

    context(ctx: ServiceContext)
    fun canView(isMember: Boolean) {
        if (ctx.calledInternallyOrByAdmin) return
        if (!isMember) throw NotaCashpoolMember()
    }

    context(ctx: ServiceContext)
    fun canUpdate(cashpool: Cashpool, isMember: Boolean) {
        if (ctx.calledInternallyOrByAdmin) return

        val isOwner = cashpool.owner.id == ctx.user.id
        if (!isMember) throw NotaCashpoolMember()
        if (!isOwner) throw NotCashpoolOwner()
    }

    context(ctx: ServiceContext)
    fun canDelete(cashpool: Cashpool, isMember: Boolean) {
        if (ctx.calledInternallyOrByAdmin) return

        val isOwner = cashpool.owner.id == ctx.user.id
        if (!isMember) throw NotaCashpoolMember()
        if (!isOwner) throw NotCashpoolOwner()
    }
}
