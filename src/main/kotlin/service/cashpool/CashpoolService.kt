package service.cashpool

import core.exceptions.CashpoolNotFound
import core.exceptions.Forbidden
import core.exceptions.NotCashpoolOwner
import core.exceptions.NotaCashpoolMember
import domain.commands.CreateCashpoolCommand
import domain.commands.UpdateCashpoolCommand
import domain.contexts.ServiceContext
import domain.models.Cashpool
import domain.repositories.CashpoolRepository
import service.user.UserService

class CashpoolService(
    private val userService: UserService,
    private val cashpoolRepo: CashpoolRepository,
) {
    private suspend fun requireOwnershipOrAdmin(cashpool: Cashpool, userId: Int) {
        val user = userService.findById(userId)
        if (cashpool.owner.id != userId && !user.isAdmin) {
            throw NotCashpoolOwner()
        }
    }

    /**
     * Validates the cashpool exists and the user is a member of it.
     */
    suspend fun requireMembership(cashpoolId: Int, userId: Int) {
        if (!cashpoolRepo.isMember(cashpoolId, userId)) {
            cashpoolRepo.findById(cashpoolId) ?: throw CashpoolNotFound()
            throw NotaCashpoolMember()
        }
    }

    /// Requires the cashpool to be opened (isOpened = true)
    suspend fun requireOpened(cashpoolId: Int) {
        if (!cashpoolRepo.findById(cashpoolId)!!.isOpened) {
            throw Forbidden("This cashpool is not opened.")
        }
    }

    context(ctx: ServiceContext)
    suspend fun create(cmd: CreateCashpoolCommand): Cashpool {
        userService.findById(cmd.ownerId)
        return cashpoolRepo.create(cmd)
    }

    context(ctx: ServiceContext)
    suspend fun findById(id: Int) = cashpoolRepo.findById(id) ?: throw CashpoolNotFound()

    context(ctx: ServiceContext)
    suspend fun findByIdOnlyIfMember(id: Int, userId: Int): Cashpool {
        val cp = findById(id)
        if (!cashpoolRepo.isMember(id, userId)) throw NotaCashpoolMember()
        return cp
    }

    context(ctx: ServiceContext)
    suspend fun findAll(): List<Cashpool> = cashpoolRepo.findAll()

    context(ctx: ServiceContext)
    suspend fun update(cmd: UpdateCashpoolCommand): Cashpool {
        val cashpool = findByIdOnlyIfMember(cmd.cashpoolId, ctx.user.id)
        requireOwnershipOrAdmin(cashpool, ctx.user.id)
        return cashpoolRepo.update(cmd) ?: throw CashpoolNotFound()
    }

    context(ctx: ServiceContext)
    suspend fun deleteById(id: Int, userId: Int) {
        val cashpool = findByIdOnlyIfMember(id, userId)
        requireOwnershipOrAdmin(cashpool, userId)
        cashpoolRepo.deleteById(id)
    }
}