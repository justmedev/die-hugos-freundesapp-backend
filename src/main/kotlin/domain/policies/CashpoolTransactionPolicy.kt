package domain.policies

import core.exceptions.CashpoolClosed
import core.exceptions.CashpoolNotFound
import core.exceptions.Forbidden
import core.exceptions.NotaCashpoolMember
import domain.commands.CreateCashpoolCommand
import domain.commands.CreateCashpoolTransactionCommand
import domain.commands.UpdateCashpoolTransactionCommand
import domain.contexts.ServiceContext
import domain.models.Cashpool
import domain.models.CashpoolTransaction

object CashpoolTransactionPolicy {

    context(ctx: ServiceContext)
    fun canCreate(cmd: CreateCashpoolTransactionCommand, cpExists: Boolean, isOpened: Boolean, isMember: Boolean) {
        if (!cpExists) throw CashpoolNotFound()
        if (!isOpened) throw CashpoolClosed()
        if (!isMember) throw NotaCashpoolMember()
        if (ctx.calledInternallyOrByAdmin) return
        if (cmd.ownerId != ctx.user.id) throw Forbidden("You cannot create a transaction in the name of another user.")
    }

    context(ctx: ServiceContext)
    fun canView(isMember: Boolean) {
        if (ctx.calledInternallyOrByAdmin) return
        if (!isMember) throw NotaCashpoolMember()
    }

    context(ctx: ServiceContext)
    fun canUpdate(cmd: UpdateCashpoolTransactionCommand, isOpened: Boolean, isMember: Boolean, transaction: CashpoolTransaction) {
        if (!isMember) throw NotaCashpoolMember()
        if (!isOpened) throw CashpoolClosed()
        if (ctx.calledInternallyOrByAdmin) return
        if (transaction.owner.id != ctx.user.id) throw Forbidden("You cannot update this transaction.")
    }

    context(ctx: ServiceContext)
    fun canAttachImage(isOpened: Boolean, isMember: Boolean, transaction: CashpoolTransaction) {
        if (!isMember) throw NotaCashpoolMember()
        if (!isOpened) throw CashpoolClosed()
        if (ctx.calledInternallyOrByAdmin) return
        if (transaction.owner.id != ctx.user.id) throw Forbidden("You cannot attach an image to this transaction.")
    }

    context(ctx: ServiceContext)
    fun canDelete(isOpened: Boolean, isMember: Boolean, transaction: CashpoolTransaction) {
        if (ctx.calledInternallyOrByAdmin) return
        if (!isMember) throw NotaCashpoolMember()
        if (!isOpened) throw CashpoolClosed()
        if (transaction.owner.id != ctx.user.id) throw Forbidden("You cannot delete this transaction.")
    }
}
