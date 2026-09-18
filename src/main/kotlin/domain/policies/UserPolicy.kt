package domain.policies

import domain.contexts.ServiceContext

object UserPolicy {

    context(ctx: ServiceContext)
    fun canCreate(): Boolean {
        return ctx.calledInternallyOrByAdmin
    }

    context(ctx: ServiceContext)
    fun canView(id: Int?): Boolean {
        return ctx.calledInternallyOrByAdmin || ctx.user.id == id
    }
}
