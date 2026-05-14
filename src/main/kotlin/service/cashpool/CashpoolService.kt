package service.cashpool

import core.exceptions.CashpoolNotFound
import core.exceptions.NotCashpoolOwner
import core.exceptions.NotaCashpoolMember
import core.exceptions.Unauthorized
import domain.commands.CreateCashpoolCommand
import domain.commands.UpdateCashpoolCommand
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

    suspend fun create(cmd: CreateCashpoolCommand): Cashpool {
        userService.findById(cmd.ownerId)
        return cashpoolRepo.create(cmd)
    }

    suspend fun findById(id: Int) = cashpoolRepo.findById(id) ?: throw CashpoolNotFound()

    suspend fun findByIdOnlyIfMember(id: Int, userId: Int): Cashpool {
        val cp = findById(id)
        if (!cashpoolRepo.isMember(id, userId)) throw NotaCashpoolMember()
        return cp
    }

    suspend fun findAll(): List<Cashpool> = cashpoolRepo.findAll()

    suspend fun update(initiatingUserId: Int, cmd: UpdateCashpoolCommand): Cashpool {
        val cashpool = findByIdOnlyIfMember(cmd.cashpoolId, initiatingUserId)
        requireOwnershipOrAdmin(cashpool, initiatingUserId)
        return cashpoolRepo.update(cmd) ?: throw CashpoolNotFound()
    }

    suspend fun deleteById(id: Int, userId: Int) {
        val cashpool = findByIdOnlyIfMember(id, userId)
        requireOwnershipOrAdmin(cashpool, userId)
        cashpoolRepo.deleteById(id)
    }
}