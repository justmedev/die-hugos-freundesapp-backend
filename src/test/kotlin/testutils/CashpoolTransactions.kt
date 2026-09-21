package testutils

import domain.models.CashpoolTransaction
import domain.models.User
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.*
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.fetchAndIncrement
import kotlin.time.Clock

object CashpoolTransactions {
    @OptIn(ExperimentalAtomicApi::class)
    private val nextId: AtomicInt = AtomicInt(0)

    @OptIn(ExperimentalAtomicApi::class)
    fun of(
        owner: User = Users.nonAdminUser,
        label: String = "transactionLabel",
        attachedImageUUID: UUID? = null,
        excludedUsers: List<Int> = emptyList(),
        amountCents: Long = 0L,
        createdAt: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.UTC)
    ) = CashpoolTransaction(
        id = this.nextId.fetchAndIncrement(),
        owner = owner,
        label = label,
        attachedImageUUID = attachedImageUUID,
        excludedUsers = excludedUsers,
        amountCents = amountCents,
        createdAt = createdAt
    )
}