package dto.cashpool_settlement

import domain.models.CashpoolSettlement
import domain.models.User

data class CashpoolSettlementResponse(
    val from: User,
    val to: User,
    val amountCents: Long,
) {
    companion object {
        fun from(domain: CashpoolSettlement) = CashpoolSettlementResponse(domain.from, domain.to, domain.amountCents)
    }
}