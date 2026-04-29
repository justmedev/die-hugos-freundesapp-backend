package domain.models

import domain.entities.CashpoolEntity
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

data class CashpoolWithMemberFlag(
    val id: Int,
    val title: String,
    val description: String,
    val owner: User,
    val isOpened: Boolean,
    val isMember: Boolean,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(entity: CashpoolEntity?, isMember: Boolean) = entity?.let {
            CashpoolWithMemberFlag(
                entity.id.value,
                entity.title,
                entity.description,
                User.from(entity.owner)!!,
                entity.isOpened,
                isMember,
                entity.createdAt
            )
        }
    }
}