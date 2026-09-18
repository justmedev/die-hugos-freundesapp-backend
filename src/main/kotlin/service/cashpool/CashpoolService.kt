package service.cashpool

import core.exceptions.CashpoolNotFound
import core.exceptions.Forbidden
import core.exceptions.NotCashpoolOwner
import core.exceptions.NotaCashpoolMember
import domain.commands.CreateCashpoolCommand
import domain.commands.UpdateCashpoolCommand
import domain.contexts.ServiceContext
import domain.models.Cashpool
import domain.policies.CashpoolMemberPolicy
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

    /// Requires the cashpool to be opened (isOpened = true)
    suspend fun requireOpened(cashpoolId: Int) {
        if (!cashpoolRepo.findById(cashpoolId)!!.isOpened) {
            throw Forbidden("This cashpool is not opened.")
        }
    }

    context(ctx: ServiceContext)
    suspend fun create(cmd: CreateCashpoolCommand): Cashpool {
        userService.findById(cmd.ownerId)
        if (!CashpoolPolicy.canCreate(cmd)) {
            throw Forbidden("User ${ctx.user.id} cannot create cashpool for user ${cmd.ownerId}")
        }
        return cashpoolRepo.create(cmd)
    }

    context(ctx: ServiceContext)
    suspend fun findById(id: Int): Cashpool {
        val cp = cashpoolRepo.findById(id) ?: throw CashpoolNotFound()
        val isMember = cashpoolRepo.isMember(id, ctx.user.id)
        if (!CashpoolPolicy.canView(isMember)) throw NotaCashpoolMember()
        return cp
    }

    context(ctx: ServiceContext)
    suspend fun findAll(): List<Cashpool> {
        if (CashpoolPolicy.canView(false)) return cashpoolRepo.findAll()
        return cashpoolRepo.findByUserMembership(ctx.user.id)
    }

    context(ctx: ServiceContext)
    suspend fun update(cmd: UpdateCashpoolCommand): Cashpool {
        val cashpool = findById(cmd.cashpoolId)
        val isMember = cashpoolRepo.isMember(cmd.cashpoolId, ctx.user.id)

        if (!isMember && !ctx.user.isAdmin) throw NotaCashpoolMember()
        if (!CashpoolPolicy.canUpdate(cashpool, isMember)) throw NotCashpoolOwner()
        return cashpoolRepo.update(cmd) ?: throw CashpoolNotFound()
    }

    context(ctx: ServiceContext)
    suspend fun deleteById(id: Int, userId: Int) {
        val cashpool = findById(id)
        val isMember = cashpoolRepo.isMember(id, userId)

        if (!isMember && !ctx.user.isAdmin) throw NotaCashpoolMember()
        if (!CashpoolPolicy.canDelete(cashpool, isMember)) throw NotCashpoolOwner()
        cashpoolRepo.deleteById(id)
    }
}