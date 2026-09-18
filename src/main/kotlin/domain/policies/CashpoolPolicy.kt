package domain.policies

import domain.commands.CreateCashpoolCommand
import domain.contexts.ServiceContext
import domain.models.Cashpool

object CashpoolPolicy {

    context(ctx: ServiceContext)
    fun canCreate(cmd: CreateCashpoolCommand): Boolean {
        return ctx.user.isAdmin || cmd.ownerId == ctx.user.id
    }

    context(ctx: ServiceContext)
    fun canView(isMember: Boolean): Boolean {
        return ctx.user.isAdmin || isMember
    }

    context(ctx: ServiceContext)
    fun canUpdate(cashpool: Cashpool, isMember: Boolean): Boolean {
        if (ctx.user.isAdmin) return true

        val isOwner = cashpool.owner.id == ctx.user.id
        return isMember && isOwner
    }

    context(ctx: ServiceContext)
    fun canDelete(cashpool: Cashpool, isMember: Boolean): Boolean {
        if (ctx.user.isAdmin) return true

        val isOwner = cashpool.owner.id == ctx.user.id
        return isMember && isOwner
    }
}
