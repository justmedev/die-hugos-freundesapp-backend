package domain.models

data class CashpoolSettlement(
    val from: User,
    val to: User,
    val amountCents: Long,
)