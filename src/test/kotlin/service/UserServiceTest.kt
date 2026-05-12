package service

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Test
import domain.commands.CreateUserCommand
import service.user.UserService
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock

import domain.repositories.UserRepositoryImpl
import core.exceptions.UserNotFound
import kotlin.test.assertFailsWith

class UserServiceTest : BaseServiceTest() {
    private val userRepo = UserRepositoryImpl()
    private val userService = UserService(userRepo)

    @Test
    fun `create user - success`() {
        runBlocking {
            val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
            val cmd = CreateUserCommand(
                email = "test@example.com",
                firstName = "Test",
                lastName = "User",
                passwordHash = "hash",
                birthdate = now,
                isAdmin = false
            )

            val user = userService.create(cmd)

            assertNotNull(user)
            assertEquals(cmd.email, user.email)
            assertEquals(cmd.firstName, user.firstName)
        }
    }

    @Test
    fun `findById - existing user - returns user`() {
        runBlocking {
            val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
            val cmd = CreateUserCommand("test@example.com", "Test", "User", null, null, "hash", now, false)
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
            val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
            val cmd = CreateUserCommand("test@example.com", "Test", "User", null, null, "hash", now, false)
            userService.create(cmd)

            val found = userService.findByEmail("test@example.com")

            assertNotNull(found)
            assertEquals("test@example.com", found.email)
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
}
