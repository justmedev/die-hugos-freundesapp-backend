package dto.cashpool_transaction

import kotlinx.serialization.Serializable

@Serializable
data class UpdateCashpoolTransactionRequest(val label: String, val amountCents: Long)