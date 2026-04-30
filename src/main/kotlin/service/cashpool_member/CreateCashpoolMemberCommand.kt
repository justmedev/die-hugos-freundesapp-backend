package service.cashpool_member


data class CreateCashpoolMemberCommand(
    val userId: Int,
    val cashpoolId: Int
)