package testutils

import domain.commands.CreateUserCommand
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

object Commands {
    object User {
        fun create(
            email: String = "a@b.c",
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