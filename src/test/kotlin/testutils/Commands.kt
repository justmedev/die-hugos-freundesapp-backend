package testutils

import domain.commands.CreateUserCommand
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.time.Clock

@OptIn(ExperimentalAtomicApi::class)
object Commands {
    object User {
        private val nextId: AtomicInt = AtomicInt(0)

        fun create(
            email: String = "user${nextId.incrementAndFetch()}@b.c",
            firstName: String = "Max",
            lastName: String = "Mustermann",
            isAdmin: Boolean = false
        ) = CreateUserCommand(
            email,
            firstName,
            lastName,
            null,
            null,
            "h",
            Clock.System.todayIn(TimeZone.UTC),
            isAdmin,
        );
    }
}