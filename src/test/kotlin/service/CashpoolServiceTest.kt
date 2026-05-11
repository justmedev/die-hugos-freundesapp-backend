package service

import core.exceptions.NotaCashpoolMember
import io.ktor.server.plugins.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Test
import service.cashpool.CashpoolService
import service.cashpool.CreateCashpoolCommand
import service.user.CreateUserCommand
import service.user.UserService
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.time.Clock

class CashpoolServiceTest : BaseServiceTest() {
    private val userService = UserService()
    private val cashpoolService = CashpoolService(userService)

    private suspend fun createTestUser(email: String = "test@example.com"): Int {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        return userService.create(CreateUserCommand(email, "F", "L", null, null, "h", now, false)).id
    }

    @Test
    fun `create cashpool - success`() {
        runBlocking {
            val userId = createTestUser()
            val cmd = CreateCashpoolCommand("Title", "Desc", userId)

            val cashpool = cashpoolService.create(cmd)

            assertNotNull(cashpool)
            assertEquals("Title", cashpool.title)
            assertEquals(userId, cashpool.owner.id)
        }
    }

    @Test
    fun `create cashpool - user not found - fails`() {
        runBlocking {
            val cmd = CreateCashpoolCommand("Title", "Desc", 999)
            assertFailsWith<NotFoundException> {
                cashpoolService.create(cmd)
            }
        }
    }

    @Test
    fun `findByIdOnlyIfMember - is member - success`() {
        runBlocking {
            val userId = createTestUser()
            val cashpool = cashpoolService.create(CreateCashpoolCommand("Title", "Desc", userId))

            val cashpoolMemberService = service.cashpool_member.CashpoolMemberService(userService, cashpoolService)
            cashpoolMemberService.create(service.cashpool_member.CreateCashpoolMemberCommand(userId, cashpool.id))

            val found = cashpoolService.findByIdOnlyIfMember(cashpool.id, userId)
            assertNotNull(found)
            assertEquals(cashpool.id, found.id)
        }
    }

    @Test
    fun `findByIdOnlyIfMember - not member - fails`() {
        runBlocking {
            val ownerId = createTestUser("owner@ex.com")
            val otherId = createTestUser("other@ex.com")
            val cashpool = cashpoolService.create(CreateCashpoolCommand("Title", "Desc", ownerId))

            assertFailsWith<NotaCashpoolMember> {
                cashpoolService.findByIdOnlyIfMember(cashpool.id, otherId)
            }
        }
    }

    @Test
    fun `findAll - returns all cashpools`() {
        runBlocking {
            val userId = createTestUser()
            cashpoolService.create(CreateCashpoolCommand("T1", "D1", userId))
            cashpoolService.create(CreateCashpoolCommand("T2", "D2", userId))

            val all = cashpoolService.findAll()
            assertEquals(2, all.size)
        }
    }
}
