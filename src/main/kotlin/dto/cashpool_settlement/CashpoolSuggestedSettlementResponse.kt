package dto.cashpool_settlement

import domain.models.CashpoolSuggestedSettlement
import dto.user.UserResponse
import kotlinx.serialization.Serializable

@Serializable
data class CashpoolSuggestedSettlementResponse(
    val from: UserResponse,
    val to: UserResponse,
    val amountCents: Long,
) {
    companion object {
        fun from(domain: CashpoolSuggestedSettlement) = CashpoolSuggestedSettlementResponse(
            UserResponse.from(domain.from),
            UserResponse.from(domain.to),
            domain.amountCents
        )
    }
}