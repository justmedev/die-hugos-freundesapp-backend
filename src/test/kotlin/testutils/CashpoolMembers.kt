package testutils

import domain.models.Cashpool
import domain.models.CashpoolMember
import domain.models.User
import io.mockk.mockk
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.fetchAndIncrement
import kotlin.time.Clock

object CashpoolMembers {
    @OptIn(ExperimentalAtomicApi::class)
    private val nextId: AtomicInt = AtomicInt(0)

    @OptIn(ExperimentalAtomicApi::class)
    fun of(
        user: User = Users.nonAdminUser,
        cashpool: Cashpool = mockk<Cashpool>(),
        createdAt: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.UTC)
    ) = CashpoolMember(
        id = this.nextId.fetchAndIncrement(),
        user = user,
        cashpool = cashpool,
        createdAt = createdAt,
    )
}