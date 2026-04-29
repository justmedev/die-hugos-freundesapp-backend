package domain.models

import domain.entities.CashpoolTransactionEntity
import kotlinx.datetime.LocalDateTime

data class CashpoolTransaction(
    val id: Int,
    val owner: User,
    val label: String,
    val amountCents: Long,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(entity: CashpoolTransactionEntity?) = entity?.let {
            CashpoolTransaction(
                entity.id.value,
                User.from(entity.owner)!!,
                entity.label,
                entity.amountCents,
                entity.createdAt
            )
        }
    }
}