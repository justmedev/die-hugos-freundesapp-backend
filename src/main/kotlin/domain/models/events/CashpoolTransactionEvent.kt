package domain.models.events

import domain.models.CashpoolTransaction

sealed class CashpoolTransactionEvent {
    abstract val cashpoolId: Int

    data class Created(
        override val cashpoolId: Int,
        val transaction: CashpoolTransaction
    ) : CashpoolTransactionEvent()

    data class Updated(
        override val cashpoolId: Int,
        val transaction: CashpoolTransaction
    ) : CashpoolTransactionEvent()

    data class Deleted(
        override val cashpoolId: Int,
        val emittingUserId: Int,
        val transactionId: Int
    ) : CashpoolTransactionEvent()
}