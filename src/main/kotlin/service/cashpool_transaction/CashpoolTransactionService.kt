package service.cashpool_transaction

import core.exceptions.CashpoolNotFound
import core.exceptions.NotaCashpoolMember
import domain.models.CashpoolTransaction
import repositories.CashpoolRepository
import repositories.CashpoolTransactionRepository

class CashpoolTransactionService(
    private val transactionRepo: CashpoolTransactionRepository,
    private val cashpoolRepo: CashpoolRepository,
) {
    private suspend fun requireMembership(cashpoolId: Int, userId: Int) {
        cashpoolRepo.findById(cashpoolId) ?: throw CashpoolNotFound()
        if (!cashpoolRepo.isMember(cashpoolId, userId)) {
            throw NotaCashpoolMember()
        }
    }

    suspend fun create(cmd: CreateCashpoolTransactionCommand): CashpoolTransaction {
        requireMembership(cmd.cashpoolId, cmd.ownerId)
        return transactionRepo.create(cmd)
    }

    suspend fun findByCashpoolId(cashpoolId: Int, requestingUserId: Int): List<CashpoolTransaction> {
        requireMembership(cashpoolId, requestingUserId)
        return transactionRepo.findByCashpoolId(cashpoolId)
    }

    suspend fun findByCashpoolIdAndTransactionOwnerId(cashpoolId: Int, ownerId: Int): List<CashpoolTransaction> {
        requireMembership(cashpoolId, ownerId)
        return transactionRepo.findByCashpoolIdAndOwnerId(cashpoolId, ownerId)
    }

    suspend fun update(cmd: UpdateCashpoolTransactionCommand): CashpoolTransaction {
        requireMembership(cmd.cashpoolId, cmd.ownerId)
        return transactionRepo.update(cmd)
    }

    suspend fun deleteById(cashpoolId: Int, transactionId: Int, requestingUserId: Int) {
        requireMembership(cashpoolId, requestingUserId)
        // TODO: Should only transaction owners and admins be allowed to delete any transaction?
        transactionRepo.deleteById(transactionId)
    }
}