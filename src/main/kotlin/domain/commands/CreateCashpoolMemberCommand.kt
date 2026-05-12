package domain.commands


data class CreateCashpoolMemberCommand(
    val userId: Int,
    val cashpoolId: Int
)