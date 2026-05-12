package domain.commands


data class CreateCashpoolTransactionCommand(
    val ownerId: Int,
    val cashpoolId: Int,
    val label: String,
    val amountCents: Long,
)