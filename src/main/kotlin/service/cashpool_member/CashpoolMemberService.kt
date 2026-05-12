package service.cashpool_member

import core.exceptions.CashpoolMemberNotFound
import domain.models.CashpoolMember
import domain.repositories.CashpoolMemberRepository

class CashpoolMemberService(
    private val cashpoolMemberRepo: CashpoolMemberRepository,
) {
    // TODO: Handle duplicates
    suspend fun create(cmd: CreateCashpoolMemberCommand): CashpoolMember = cashpoolMemberRepo.create(cmd)

    suspend fun findById(id: Int) = cashpoolMemberRepo.findById(id) ?: throw CashpoolMemberNotFound()

    suspend fun findByCashpoolId(cashpoolId: Int) = cashpoolMemberRepo.findByCashpoolId(cashpoolId)

    suspend fun findAll() = cashpoolMemberRepo.findAll()
}