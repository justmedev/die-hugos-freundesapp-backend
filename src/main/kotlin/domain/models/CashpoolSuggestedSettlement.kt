package domain.models

data class CashpoolSuggestedSettlement(
    val from: User,
    val to: User,
    val amountCents: Long,
)