package dto.cashpool_settlement

import domain.models.CashpoolSettlement
import domain.models.CashpoolSuggestedSettlement
import dto.user.UserResponse
import kotlinx.serialization.Serializable

@Serializable
data class CashpoolSettlementResponse(
    val from: UserResponse,
    val to: UserResponse,
    val amountCents: Long,
    val purpose: String,
    val createdAt: String,
) {
    companion object {
        fun from(domain: CashpoolSettlement) = CashpoolSettlementResponse(
            UserResponse.from(domain.from),
            UserResponse.from(domain.to),
            domain.amountCents,
            domain.purpose,
            domain.createdAt.toString(),
        )
    }
}