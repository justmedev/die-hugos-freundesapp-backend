package dto.cashpool_transaction

import domain.models.CashpoolTransaction
import dto.user.UserResponse
import kotlinx.serialization.Serializable

@Serializable
data class CashpoolTransactionResponse(
    val id: Int,
    val owner: UserResponse,
    val label: String,
    val attachedImageURL: String? = null,
    val excludedUsers: List<Int>,
    val amountCents: Long,
    val createdAt: String,
) {
    companion object {
        fun from(domain: CashpoolTransaction) = CashpoolTransactionResponse(
            domain.id,
            UserResponse.from(domain.owner),
            domain.label,
            if (domain.attachedImageUUID == null) null else "/uploads/${domain.attachedImageUUID}",
            domain.excludedUsers,
            domain.amountCents,
            domain.createdAt.toString(),
        )
    }
}