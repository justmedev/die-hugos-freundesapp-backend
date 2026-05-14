package dto.cashpool_settlement

import kotlinx.serialization.Serializable

@Serializable
data class CreateCashpoolSettlementRequest(val fromId: Int, val toId: Int, val amountCents: Long, val purpose: String)