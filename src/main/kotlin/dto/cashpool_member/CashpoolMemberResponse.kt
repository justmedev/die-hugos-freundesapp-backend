package dto.cashpool_member

import domain.models.CashpoolMember
import dto.user.UserResponse
import kotlinx.serialization.Serializable

@Serializable
data class CashpoolMemberResponse(
    val id: Int,
    val user: UserResponse,
    val createdAt: String,
) {
    companion object {
        fun from(domain: CashpoolMember) = CashpoolMemberResponse(
            domain.id,
            UserResponse.from(domain.user),
            domain.createdAt.toString(),
        )
    }
}