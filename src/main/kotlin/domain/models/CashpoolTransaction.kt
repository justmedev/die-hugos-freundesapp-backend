package domain.models

import domain.entities.CashpoolTransactionEntity
import kotlinx.datetime.LocalDateTime
import java.util.UUID

data class CashpoolTransaction(
    val id: Int,
    val owner: User,
    val label: String,
    val attachedImageUUID: UUID?,
    val excludedUsers: List<Int>,
    val amountCents: Long,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(entity: CashpoolTransactionEntity?) = entity?.let {
            CashpoolTransaction(
                entity.id.value,
                User.from(entity.owner)!!,
                entity.label,
                entity.attachedImageUUID,
                entity.excludedUsers,
                entity.amountCents,
                entity.createdAt
            )
        }
    }
}