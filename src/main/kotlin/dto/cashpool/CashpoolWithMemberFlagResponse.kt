package dto.cashpool

import domain.models.CashpoolWithMemberFlag
import dto.user.UserResponse
import kotlinx.serialization.Serializable

@Serializable
data class CashpoolWithMemberFlagResponse(
    val id: Int,
    val title: String,
    val description: String,
    val owner: UserResponse,
    val isMember: Boolean,
    val isOpened: Boolean,
    val createdAt: String,
) {
    companion object {
        fun from(domain: CashpoolWithMemberFlag) = CashpoolWithMemberFlagResponse(
            domain.id,
            domain.title,
            domain.description,
            UserResponse.from(domain.owner),
            domain.isOpened,
            domain.isMember,
            domain.createdAt.toString(),
        )
    }
}