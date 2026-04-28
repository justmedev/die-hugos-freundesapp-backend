package dto.cashpool

import kotlinx.serialization.Serializable

@Serializable
data class CreateCashpoolRequest(val title: String, val description: String)