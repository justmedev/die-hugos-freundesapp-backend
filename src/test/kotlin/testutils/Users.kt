package testutils

import domain.models.User
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.time.Clock

@OptIn(ExperimentalAtomicApi::class)
object Users {
    private val nextId: AtomicInt = AtomicInt(0)

    val nonAdminUser get() = createUser(false)
    val adminUser get() = createUser(true)

    private fun createUser(isAdmin: Boolean) = User(
        nextId.incrementAndFetch(),
        "test@example.com",
        "Test",
        "User",
        null,
        null,
        "hashed",
        Clock.System.todayIn(TimeZone.UTC),
        isAdmin,
        Clock.System.now().toLocalDateTime(TimeZone.UTC)
    )
}