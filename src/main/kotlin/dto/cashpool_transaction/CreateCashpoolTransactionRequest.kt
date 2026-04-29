package dto.cashpool_transaction

import kotlinx.serialization.Serializable

@Serializable
data class CreateCashpoolTransactionRequest(val label: String, val amountCents: Long)