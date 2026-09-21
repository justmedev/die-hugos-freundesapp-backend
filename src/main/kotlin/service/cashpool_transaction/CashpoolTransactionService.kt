package service.cashpool_transaction

import core.exceptions.TransactionNotFound
import core.utils.UpdateProperty
import domain.commands.AttachImageCashpoolTransactionCommand
import domain.commands.CreateCashpoolTransactionCommand
import domain.commands.UpdateCashpoolTransactionCommand
import domain.contexts.ServiceContext
import domain.models.CashpoolTransaction
import domain.models.events.CashpoolTransactionEvent
import domain.policies.CashpoolTransactionPolicy
import domain.repositories.CashpoolTransactionRepository
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import service.cashpool.CashpoolService
import java.io.File
import java.util.*

class CashpoolTransactionService(
    private val transactionRepo: CashpoolTransactionRepository,
    private val cashpoolService: CashpoolService,
) {
    private val _events = MutableSharedFlow<CashpoolTransactionEvent>()
    val events = _events.asSharedFlow()

    context(ctx: ServiceContext)
    suspend fun create(cmd: CreateCashpoolTransactionCommand): CashpoolTransaction {
        CashpoolTransactionPolicy.canCreate(
            cmd = cmd,
            cpExists = context(ServiceContext.internal()) { runCatching { cashpoolService.findById(cmd.cashpoolId) }.isSuccess },
            isOpened = cashpoolService.isOpened(cmd.cashpoolId),
            isMember = cashpoolService.isMember(cmd.cashpoolId, cmd.ownerId),
        )

        val created = transactionRepo.create(cmd)
        _events.emit(CashpoolTransactionEvent.Created(cmd.cashpoolId, created))
        return created
    }

    context(ctx: ServiceContext)
    suspend fun attachImage(cmd: AttachImageCashpoolTransactionCommand): CashpoolTransaction {
        val transaction = transactionRepo.findById(cmd.transactionId) ?: throw TransactionNotFound()
        CashpoolTransactionPolicy.canAttachImage(
            cashpoolService.isOpened(cmd.cashpoolId), cashpoolService.isMember(cmd.cashpoolId, ctx.user.id), transaction
        )

        val attachedImageUUID = transaction.attachedImageUUID ?: UUID.randomUUID()
        val file = File("uploads/$attachedImageUUID")
        file.parentFile.mkdirs()
        cmd.imageProvider.copyAndClose(file.writeChannel())

        val updated = transactionRepo.update(
            UpdateCashpoolTransactionCommand(
                ctx.user.id,
                cmd.cashpoolId,
                cmd.transactionId,
                attachedImageUUID = UpdateProperty(attachedImageUUID)
            )
        ) ?: throw TransactionNotFound()
        _events.emit(CashpoolTransactionEvent.Updated(cmd.cashpoolId, updated))
        return updated
    }

    context(ctx: ServiceContext)
    suspend fun findByCashpoolId(cashpoolId: Int): List<CashpoolTransaction> {
        CashpoolTransactionPolicy.canView(
            cashpoolService.isMember(
                cashpoolId, ctx.user.id
            )
        )
        return transactionRepo.findByCashpoolId(cashpoolId)
    }

    context(ctx: ServiceContext)
    suspend fun update(cmd: UpdateCashpoolTransactionCommand): CashpoolTransaction {
        val transaction = transactionRepo.findById(cmd.transactionId) ?: throw TransactionNotFound()
        CashpoolTransactionPolicy.canUpdate(
            cmd = cmd,
            isOpened = cashpoolService.isOpened(cmd.cashpoolId),
            isMember = cashpoolService.isMember(cmd.cashpoolId, cmd.ownerId),
            transaction = transaction
        )

        val updated = transactionRepo.update(cmd) ?: throw TransactionNotFound()
        if (transaction.attachedImageUUID != null && transaction.attachedImageUUID != updated.attachedImageUUID) {
            runCatching { File("uploads/${transaction.attachedImageUUID}").delete() }
        }
        _events.emit(CashpoolTransactionEvent.Updated(cmd.cashpoolId, updated))
        return updated
    }

    context(ctx: ServiceContext)
    suspend fun deleteById(cashpoolId: Int, transactionId: Int) {
        val transaction = transactionRepo.findById(transactionId) ?: throw TransactionNotFound()
        CashpoolTransactionPolicy.canDelete(
            cashpoolService.isOpened(cashpoolId),
            cashpoolService.isMember(cashpoolId, ctx.user.id),
            transaction,
        )

        if (transaction.attachedImageUUID != null) {
            runCatching { File("uploads/${transaction.attachedImageUUID}").delete() }
        }
        transactionRepo.deleteById(transactionId)
        _events.emit(CashpoolTransactionEvent.Deleted(cashpoolId, ctx.user.id, transactionId))
    }
}