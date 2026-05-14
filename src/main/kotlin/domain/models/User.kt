package domain.models

import domain.entities.UserEntity
import domain.models.valueobjects.IBAN
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

data class User(
    val id: Int,
    val email: String,
    val firstName: String,
    val lastName: String,
    val accountHolderName: String? = null,
    val accountIBAN: IBAN? = null,
    val password: String,
    val birthdate: LocalDate,
    val isAdmin: Boolean,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(entity: UserEntity?) = entity?.let {
            User(
                entity.id.value,
                entity.email,
                entity.firstName,
                entity.lastName,
                entity.accountHolderName,
                entity.accountIBAN?.let { IBAN(entity.accountIBAN!!) },
                entity.password,
                entity.birthdate,
                entity.isAdmin,
                entity.createdAt
            )
        }
    }
}