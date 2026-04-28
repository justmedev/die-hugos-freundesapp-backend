package dto.cashpool

import domain.models.Cashpool
import dto.user.UserResponse
import kotlinx.serialization.Serializable

@Serializable
data class CashpoolResponse(
    val id: Int,
    val title: String,
    val description: String,
    val owner: UserResponse,
    val isOpened: Boolean,
    val createdAt: String,
) {
    companion object {
        fun from(domain: Cashpool) = CashpoolResponse(
            domain.id,
            domain.title,
            domain.description,
            UserResponse.from(domain.owner),
            domain.isOpened,
            domain.createdAt.toString(),
        )
    }
}