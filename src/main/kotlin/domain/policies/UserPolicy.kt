package domain.policies

import core.exceptions.Forbidden
import domain.contexts.ServiceContext

object UserPolicy {

    context(ctx: ServiceContext)
    fun canCreate() {
        if (ctx.calledInternallyOrByAdmin) return
        throw Forbidden("You are not allowed to create a user!")
    }

    context(ctx: ServiceContext)
    fun canView(id: Int?) {
        if (ctx.calledInternallyOrByAdmin) return
        if (ctx.user.id != id) throw Forbidden("You are not allowed to view this user!")
    }

    context(ctx: ServiceContext)
    fun canUpdate(id: Int) {
        if (ctx.calledInternallyOrByAdmin) return
        if (ctx.user.id != id) throw Forbidden("You are not allowed to update this user!")
    }
}
