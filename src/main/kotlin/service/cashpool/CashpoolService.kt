package service.cashpool

import core.exceptions.CashpoolNotFound
import core.exceptions.NotaCashpoolMember
import domain.commands.CreateCashpoolCommand
import domain.commands.UpdateCashpoolCommand
import domain.contexts.ServiceContext
import domain.models.Cashpool
import domain.policies.CashpoolPolicy
import domain.repositories.CashpoolRepository
import service.user.UserService

class CashpoolService(
    private val userService: UserService,
    private val cashpoolRepo: CashpoolRepository,
) {
    /**
     * Validates the cashpool exists and the user is a member of it.
     */
    suspend fun requireMembership(cashpoolId: Int, userId: Int) {
        if (!cashpoolRepo.isMember(cashpoolId, userId)) {
            cashpoolRepo.findById(cashpoolId) ?: throw CashpoolNotFound()
            throw NotaCashpoolMember()
        }
    }

    suspend fun isMember(cashpoolId: Int, userId: Int): Boolean = cashpoolRepo.isMember(cashpoolId, userId)

    suspend fun isOpened(cashpoolId: Int) = cashpoolRepo.findById(cashpoolId)!!.isOpened

    context(ctx: ServiceContext)
    suspend fun create(cmd: CreateCashpoolCommand): Cashpool {
        userService.findById(cmd.ownerId)
        CashpoolPolicy.canCreate(cmd)

        return cashpoolRepo.create(cmd)
    }

    context(ctx: ServiceContext)
    suspend fun findById(id: Int): Cashpool {
        val cp = cashpoolRepo.findById(id) ?: throw CashpoolNotFound()
        val isMember = cashpoolRepo.isMember(id, ctx.user.id)
        CashpoolPolicy.canView(isMember)

        return cp
    }

    context(ctx: ServiceContext)
    suspend fun findAll(): List<Cashpool> {
        CashpoolPolicy.canView(false)

        if (ctx.calledInternallyOrByAdmin) return cashpoolRepo.findAll()
        return cashpoolRepo.findByUserMembership(ctx.user.id)
    }

    context(ctx: ServiceContext)
    suspend fun update(cmd: UpdateCashpoolCommand): Cashpool {
        val cashpool = findById(cmd.cashpoolId)
        val isMember = cashpoolRepo.isMember(cmd.cashpoolId, ctx.user.id)
        CashpoolPolicy.canUpdate(cashpool, isMember)

        return cashpoolRepo.update(cmd) ?: throw CashpoolNotFound()
    }

    context(ctx: ServiceContext)
    suspend fun deleteById(id: Int, userId: Int) {
        val cashpool = findById(id)
        val isMember = cashpoolRepo.isMember(id, userId)
        CashpoolPolicy.canDelete(cashpool, isMember)

        cashpoolRepo.deleteById(id)
    }
}