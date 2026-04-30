package domain.models

import domain.entities.CashpoolEntity
import kotlinx.datetime.LocalDateTime

data class Cashpool(
    val id: Int,
    val title: String,
    val description: String,
    val owner: User,
    val isOpened: Boolean,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(entity: CashpoolEntity?) = entity?.let {
            Cashpool(
                entity.id.value,
                entity.title,
                entity.description,
                User.from(entity.owner)!!,
                entity.isOpened,
                entity.createdAt
            )
        }
    }
}