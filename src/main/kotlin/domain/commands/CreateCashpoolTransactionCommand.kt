package domain.commands


data class CreateCashpoolTransactionCommand(
    val ownerId: Int,
    val cashpoolId: Int,
    val label: String,
    val amountCents: Long,
) {
    init {
        require(label.isNotBlank()) { "Label cannot be blank" }
    }
}