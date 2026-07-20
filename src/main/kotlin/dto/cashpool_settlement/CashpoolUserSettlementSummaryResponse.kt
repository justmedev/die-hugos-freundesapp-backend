package dto.cashpool_settlement

import domain.models.CashpoolUserSettlementSummary
import kotlinx.serialization.Serializable

@Serializable
data class CashpoolUserSettlementSummaryResponse(
    val netUserBalance: Long,
    val totalOpenCashpoolWorth: Long,
) {
    companion object {
        fun from(domain: CashpoolUserSettlementSummary) = CashpoolUserSettlementSummaryResponse(
            domain.netUserBalance,
            domain.totalOpenCashpoolWorth,
        )
    }
}