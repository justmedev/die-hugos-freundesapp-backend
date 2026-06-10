package testutils

import domain.models.User
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import java.util.*
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.time.Clock

@OptIn(ExperimentalAtomicApi::class)
object Users {
    private val nextId: AtomicInt = AtomicInt(0)

    val nonAdminUser get() = createUser(false)

    private fun createUser(isAdmin: Boolean) = User(
        nextId.incrementAndFetch(),
        UUID.randomUUID().toString(),
        "test@example.com",
        "Test",
        "User",
        null,
        null,
        Clock.System.todayIn(TimeZone.UTC),
        isAdmin,
        Clock.System.now().toLocalDateTime(TimeZone.UTC)
    )
}