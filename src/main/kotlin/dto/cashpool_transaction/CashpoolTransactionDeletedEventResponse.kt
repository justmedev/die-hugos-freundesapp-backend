package dto.cashpool_transaction

import kotlinx.serialization.Serializable

@Serializable
data class CashpoolTransactionDeletedEventResponse(
    val transactionId: Int,
    val emittingUserId: Int,
)