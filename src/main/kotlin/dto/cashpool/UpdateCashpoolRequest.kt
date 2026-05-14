package dto.cashpool

import kotlinx.serialization.Serializable

@Serializable
data class UpdateCashpoolRequest(val title: String, val description: String)