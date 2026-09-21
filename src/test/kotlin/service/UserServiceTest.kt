package service

import core.exceptions.Forbidden
import core.exceptions.UserNotFound
import core.utils.UpdateProperty
import domain.commands.UpdateUserCommand
import domain.contexts.ServiceContext
import domain.models.valueobjects.IBAN
import domain.repositories.UserRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.junit.Test
import service.user.UserService
import testutils.Commands
import testutils.Contexts
import testutils.Users
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class UserServiceTest : BaseServiceTest() {
    private val userRepo = UserRepositoryImpl()
    private val userService = UserService(userRepo)

    @Test
    fun `create user - success`() {
        runBlocking {
            val cmd = Commands.User.create()

            val user = context(Contexts.internal) { userService.create(cmd) }

            assertNotNull(user)
            assertEquals(cmd.email, user.email)
            assertEquals(cmd.firstName, user.firstName)
        }
    }

    @Test
    fun `findById - existing authorized user - returns user`() {
        runBlocking {
            val cmd = Commands.User.create()
            val created = context(Contexts.internal) { userService.create(cmd) }

            val found = context(ServiceContext.external(created)) { userService.findById(created.id) }

            assertNotNull(found)
            assertEquals(created.id, found.id)
        }
    }

    @Test
    fun `findById - existing unauthorized user - returns user`() {
        runBlocking {
            val cmd = Commands.User.create()
            val created = context(Contexts.internal) { userService.create(cmd) }

            assertFailsWith<Forbidden> {
                context(ServiceContext.external(Users.nonAdminUser)) { userService.findById(created.id) }
            }
        }
    }

    @Test
    fun `findById - non-existing user - throws UserNotFound`() {
        runBlocking {
            assertFailsWith<UserNotFound> {
                context(Contexts.internal) { userService.findById(999) }
            }
        }
    }

    @Test
    fun `findByEmail - existing user - returns user`() {
        runBlocking {
            val cmd = Commands.User.create()
            val created = context(Contexts.internal) { userService.create(cmd) }

            val found = context(ServiceContext.external(created)) { userService.findByEmail(cmd.email) }

            assertNotNull(found)
            assertEquals(cmd.email, found.email)
        }
    }

    @Test
    fun `findByEmail - non-existing user - throws UserNotFound`() {
        runBlocking {
            assertFailsWith<UserNotFound> {
                context(Contexts.internal) { userService.findByEmail("notfound@example.com") }
            }
        }
    }

    @Test
    fun `update - success`() {
        runBlocking {
            val created = context(Contexts.internal) { userService.create(Commands.User.create()) }
            val updateCmd = UpdateUserCommand(
                email = UpdateProperty("updated@example.com"),
                firstName = UpdateProperty("Updated"),
                lastName = UpdateProperty(), // Don't update this
                accountHolderName = UpdateProperty(null),
                accountIBAN = UpdateProperty(IBAN("DE36000000000000000000")),
                birthdate = UpdateProperty(created.birthdate)
            )

            val updated = context(Contexts.of(created)) { userService.update(created.id, updateCmd) }

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
                context(Contexts.internal) { userService.update(999, updateCmd) }
            }
        }
    }
}
