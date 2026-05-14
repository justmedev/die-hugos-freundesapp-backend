package service.cashpool_transaction

import core.exceptions.TransactionNotFound
import core.exceptions.Unauthorized
import domain.commands.CreateCashpoolTransactionCommand
import domain.commands.UpdateCashpoolTransactionCommand
import domain.models.CashpoolTransaction
import domain.repositories.CashpoolTransactionRepository
import service.cashpool.CashpoolService
import service.user.UserService

class CashpoolTransactionService(
    private val transactionRepo: CashpoolTransactionRepository,
    private val cashpoolService: CashpoolService,
    private val userService: UserService,
) {
    private suspend fun requireOwnershipOrAdmin(transaction: CashpoolTransaction, userId: Int) {
        val user = userService.findById(userId)
        if (transaction.owner.id != userId && !user.isAdmin) {
            throw Unauthorized("You are not the owner of this transaction.")
        }
    }

    suspend fun create(cmd: CreateCashpoolTransactionCommand): CashpoolTransaction {
        cashpoolService.requireMembership(cmd.cashpoolId, cmd.ownerId)
        return transactionRepo.create(cmd)
    }

    suspend fun findByCashpoolId(cashpoolId: Int, requestingUserId: Int): List<CashpoolTransaction> {
        cashpoolService.requireMembership(cashpoolId, requestingUserId)
        return transactionRepo.findByCashpoolId(cashpoolId)
    }

    suspend fun findByCashpoolIdAndTransactionOwnerId(cashpoolId: Int, ownerId: Int): List<CashpoolTransaction> {
        cashpoolService.requireMembership(cashpoolId, ownerId)
        return transactionRepo.findByCashpoolIdAndOwnerId(cashpoolId, ownerId)
    }

    suspend fun update(cmd: UpdateCashpoolTransactionCommand): CashpoolTransaction {
        cashpoolService.requireMembership(cmd.cashpoolId, cmd.ownerId)
        val transaction = transactionRepo.findById(cmd.transactionId) ?: throw TransactionNotFound()
        requireOwnershipOrAdmin(transaction, cmd.ownerId)
        return transactionRepo.update(cmd) ?: throw TransactionNotFound()
    }

    suspend fun deleteById(cashpoolId: Int, transactionId: Int, requestingUserId: Int) {
        cashpoolService.requireMembership(cashpoolId, requestingUserId)
        val transaction = transactionRepo.findById(transactionId) ?: throw TransactionNotFound()
        requireOwnershipOrAdmin(transaction, requestingUserId)
        transactionRepo.deleteById(transactionId)
    }
}