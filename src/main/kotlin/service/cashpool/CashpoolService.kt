package service.cashpool

import domain.commands.CreateCashpoolCommand
import domain.models.Cashpool
import domain.repositories.CashpoolRepository
import service.user.UserService

import core.exceptions.CashpoolNotFound
import core.exceptions.NotaCashpoolMember

class CashpoolService(
    private val userService: UserService,
    private val cashpoolRepo: CashpoolRepository,
) {
    suspend fun create(cmd: CreateCashpoolCommand): Cashpool {
        userService.findById(cmd.ownerId)
        return cashpoolRepo.create(cmd)
    }

    suspend fun findById(id: Int) = cashpoolRepo.findById(id) ?: throw CashpoolNotFound()

    suspend fun findByIdOnlyIfMember(id: Int, userId: Int): Cashpool {
        if (!cashpoolRepo.isMember(id, userId)) throw NotaCashpoolMember()
        return findById(id)
    }

    suspend fun findAll(): List<Cashpool> = cashpoolRepo.findAll()
}