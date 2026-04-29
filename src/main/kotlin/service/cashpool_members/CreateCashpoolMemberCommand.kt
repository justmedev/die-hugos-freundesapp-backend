package service.cashpool_members


data class CreateCashpoolMemberCommand(
    val userId: Int,
    val cashpoolId: Int
)