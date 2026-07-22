package service

import core.exceptions.UserNotFound
import core.utils.UpdateProperty
import domain.commands.UpdateUserCommand
import domain.models.valueobjects.IBAN
import domain.repositories.UserRepositoryImpl
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Test
import service.user.UserService
import testutils.Commands
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.time.Clock

class UserServiceTest : BaseServiceTest() {
    private val userRepo = UserRepositoryImpl()
    private val userService = UserService(userRepo)

    @Test
    fun `create user - success`() {
        runBlocking {
            val cmd = Commands.User.create()

            val user = userService.create(cmd)

            assertNotNull(user)
            assertEquals(cmd.email, user.email)
            assertEquals(cmd.firstName, user.firstName)
        }
    }

    @Test
    fun `findById - existing user - returns user`() {
        runBlocking {
            val cmd = Commands.User.create()
            val created = userService.create(cmd)

            val found = userService.findById(created.id)

            assertNotNull(found)
            assertEquals(created.id, found.id)
        }
    }

    @Test
    fun `findById - non-existing user - throws UserNotFound`() {
        runBlocking {
            assertFailsWith<UserNotFound> {
                userService.findById(999)
            }
        }
    }

    @Test
    fun `findByEmail - existing user - returns user`() {
        runBlocking {
            val cmd = Commands.User.create()
            userService.create(cmd)

            val found = userService.findByEmail(cmd.email)

            assertNotNull(found)
            assertEquals(cmd.email, found.email)
        }
    }

    @Test
    fun `findByEmail - non-existing user - throws UserNotFound`() {
        runBlocking {
            assertFailsWith<UserNotFound> {
                userService.findByEmail("notfound@example.com")
            }
        }
    }

    @Test
    fun `update - success`() {
        runBlocking {
            val created = userService.create(Commands.User.create())
            val updateCmd = UpdateUserCommand(
                email = UpdateProperty("updated@example.com"),
                firstName = UpdateProperty("Updated"),
                lastName = UpdateProperty(), // Don't update this
                accountHolderName = UpdateProperty(null),
                accountIBAN = UpdateProperty(IBAN("DE36000000000000000000")),
                birthdate = UpdateProperty(created.birthdate)
            )

            val updated = userService.update(created.id, updateCmd)

            assertEquals("updated@example.com", updated.email)
            assertEquals("Updated", updated.firstName)
            assertEquals("Mustermann", updated.lastName)
            assertEquals(null, updated.accountHolderName)
        }
    }

    @Test
    fun `update - non-existing user - throws UserNotFound`() {
        runBlocking {
            val updateCmd = UpdateUserCommand(
                email = UpdateProperty("updated@example.com"),
                firstName = UpdateProperty("firstName"),
                lastName = UpdateProperty(), // Don't update this
                accountHolderName = UpdateProperty(null),
                accountIBAN = UpdateProperty(IBAN("DE36000000000000000000")),
                birthdate = UpdateProperty()
            )
            assertFailsWith<UserNotFound> {
                userService.update(999, updateCmd)
            }
        }
    }
}
