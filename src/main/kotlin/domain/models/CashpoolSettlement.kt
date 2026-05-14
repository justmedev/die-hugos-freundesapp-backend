package domain.models

import domain.entities.CashpoolSettlementEntity
import kotlinx.datetime.LocalDateTime

data class CashpoolSettlement(
    val id: Int,
    val from: User,
    val to: User,
    val amountCents: Long,
    val purpose: String,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(entity: CashpoolSettlementEntity?) = entity?.let {
            CashpoolSettlement(
                entity.id.value,
                User.from(entity.from)!!,
                User.from(entity.to)!!,
                entity.amountCents,
                entity.purpose,
                entity.createdAt
            )
        }
    }
}