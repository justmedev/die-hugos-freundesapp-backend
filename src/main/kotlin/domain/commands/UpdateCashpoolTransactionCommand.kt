package domain.commands


data class UpdateCashpoolTransactionCommand(
    val ownerId: Int,
    val cashpoolId: Int,
    val transactionId: Int,
    val label: String,
    val amountCents: Long,
)