package service.cashpool_settlement

import domain.models.CashpoolMember

data class CashpoolSettlementMember(
    val member: CashpoolMember,
    val totalAmountCentsMoved: Long,
)
