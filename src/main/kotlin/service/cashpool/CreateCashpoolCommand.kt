package service.cashpool


data class CreateCashpoolCommand(
    val title: String,
    val description: String,
    val ownerId: Int
)