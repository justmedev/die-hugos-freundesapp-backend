package service.cashpool_transaction

import core.exceptions.TransactionNotFound
import core.exceptions.Unauthorized
import core.utils.UpdateProperty
import domain.commands.AttachImageCashpoolTransactionCommand
import domain.commands.CreateCashpoolTransactionCommand
import domain.commands.UpdateCashpoolTransactionCommand
import domain.models.CashpoolTransaction
import domain.models.events.CashpoolTransactionEvent
import domain.repositories.CashpoolTransactionRepository
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import service.cashpool.CashpoolService
import service.user.UserService
import java.io.File
import java.util.*

class CashpoolTransactionService(
    private val transactionRepo: CashpoolTransactionRepository,
    private val cashpoolService: CashpoolService,
    private val userService: UserService,
) {
    private val _events = MutableSharedFlow<CashpoolTransactionEvent>()
    val events = _events.asSharedFlow()

    private suspend fun requireOwnershipOrAdmin(transaction: CashpoolTransaction, userId: Int) {
        val user = userService.findById(userId)
        if (transaction.owner.id != userId && !user.isAdmin) {
            throw Unauthorized("You are not the owner of this transaction.")
        }
    }

    suspend fun create(cmd: CreateCashpoolTransactionCommand): CashpoolTransaction {
        cashpoolService.requireMembership(cmd.cashpoolId, cmd.ownerId)
        cashpoolService.requireOpened(cmd.cashpoolId)

        val created = transactionRepo.create(cmd)
        _events.emit(CashpoolTransactionEvent.Created(cmd.cashpoolId, created))
        return created
    }

    suspend fun attachImage(cmd: AttachImageCashpoolTransactionCommand): CashpoolTransaction {
        cashpoolService.requireMembership(cmd.cashpoolId, cmd.requestingUserId)
        cashpoolService.requireOpened(cmd.cashpoolId)
        val transaction = transactionRepo.findById(cmd.transactionId) ?: throw TransactionNotFound()
        requireOwnershipOrAdmin(transaction, cmd.requestingUserId)

        val attachedImageUUID = UUID.randomUUID()
        val file = File("uploads/$attachedImageUUID")
        cmd.imageProvider.copyAndClose(file.writeChannel())

        return transactionRepo.update(
            UpdateCashpoolTransactionCommand(
                cmd.requestingUserId,
                cmd.cashpoolId,
                cmd.transactionId,
                attachedImageUUID = UpdateProperty(attachedImageUUID)
            )
        ) ?: throw TransactionNotFound()
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
        cashpoolService.requireOpened(cmd.cashpoolId)

        val transaction = transactionRepo.findById(cmd.transactionId) ?: throw TransactionNotFound()
        requireOwnershipOrAdmin(transaction, cmd.ownerId)

        val updated = transactionRepo.update(cmd) ?: throw TransactionNotFound()
        if (updated.attachedImageUUID != null) {
            runCatching { File("uploads/${transaction.attachedImageUUID}").delete() }
        }
        _events.emit(CashpoolTransactionEvent.Updated(cmd.cashpoolId, updated))
        return updated
    }

    suspend fun deleteById(cashpoolId: Int, transactionId: Int, requestingUserId: Int) {
        cashpoolService.requireMembership(cashpoolId, requestingUserId)
        cashpoolService.requireOpened(cashpoolId)

        val transaction = transactionRepo.findById(transactionId) ?: throw TransactionNotFound()
        requireOwnershipOrAdmin(transaction, requestingUserId)

        if (transaction.attachedImageUUID != null) {
            runCatching { File("uploads/${transaction.attachedImageUUID}").delete() }
        }
        transactionRepo.deleteById(transactionId)
        _events.emit(CashpoolTransactionEvent.Deleted(cashpoolId, requestingUserId, transactionId))
    }
}