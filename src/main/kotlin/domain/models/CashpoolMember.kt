package domain.models

import domain.entities.CashpoolMemberEntity
import kotlinx.datetime.LocalDateTime

data class CashpoolMember(
    val id: Int,
    val user: User,
    val cashpool: Cashpool,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(entity: CashpoolMemberEntity?) = entity?.let {
            CashpoolMember(
                entity.id.value,
                User.from(entity.user)!!,
                Cashpool.from(entity.cashpool)!!,
                entity.createdAt
            )
        }
    }
}