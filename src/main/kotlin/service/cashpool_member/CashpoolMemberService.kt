package service.cashpool_member

import core.exceptions.CashpoolMemberNotFound
import core.exceptions.CashpoolNotFound
import core.exceptions.UserNotFound
import domain.commands.CreateCashpoolMemberCommand
import domain.models.CashpoolMember
import domain.repositories.CashpoolMemberRepository
import domain.repositories.CashpoolRepository
import domain.repositories.UserRepository

class CashpoolMemberService(
    private val cashpoolMemberRepo: CashpoolMemberRepository,
    private val userRepo: UserRepository,
    private val cashpoolRepo: CashpoolRepository,
) {
    // TODO: Handle duplicates
    suspend fun create(cmd: CreateCashpoolMemberCommand): CashpoolMember {
        userRepo.findById(cmd.userId) ?: throw UserNotFound()
        cashpoolRepo.findById(cmd.cashpoolId) ?: throw CashpoolNotFound()
        return cashpoolMemberRepo.create(cmd)
    }

    suspend fun findById(id: Int) = cashpoolMemberRepo.findById(id) ?: throw CashpoolMemberNotFound()

    suspend fun findByCashpoolId(cashpoolId: Int) = cashpoolMemberRepo.findByCashpoolId(cashpoolId)

    suspend fun findAll() = cashpoolMemberRepo.findAll()
}