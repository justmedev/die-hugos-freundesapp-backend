package service

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Test
import service.cashpool.CashpoolService
import service.cashpool.CreateCashpoolCommand
import service.cashpool_member.CashpoolMemberService
import service.cashpool_member.CreateCashpoolMemberCommand
import service.user.CreateUserCommand
import service.user.UserService
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock

class CashpoolMemberServiceTest : BaseServiceTest() {
    private val userService = UserService()
    private val cashpoolService = CashpoolService(userService)
    private val cashpoolMemberService = CashpoolMemberService(userService, cashpoolService)

    private suspend fun createTestUser(email: String = "test@example.com"): Int {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        return userService.create(CreateUserCommand(email, "F", "L", "h", now, false)).id
    }

    private suspend fun createTestCashpool(ownerId: Int): Int {
        return cashpoolService.create(CreateCashpoolCommand("Title", "Desc", ownerId)).id
    }

    @Test
    fun `create member - success`() {
        runBlocking {
            val userId = createTestUser()
            val cashpoolId = createTestCashpool(userId)
            val cmd = CreateCashpoolMemberCommand(userId, cashpoolId)

            val member = cashpoolMemberService.create(cmd)

            assertNotNull(member)
            assertEquals(userId, member.user.id)
            assertEquals(cashpoolId, member.cashpool.id)
        }
    }

    @Test
    fun `findByCashpoolIdAndUserId - exists - returns member`() {
        runBlocking {
            val userId = createTestUser()
            val cashpoolId = createTestCashpool(userId)
            cashpoolMemberService.create(CreateCashpoolMemberCommand(userId, cashpoolId))

            val found = cashpoolMemberService.findByCashpoolIdAndUserId(cashpoolId, userId)

            assertNotNull(found)
            assertEquals(userId, found.user.id)
        }
    }

    @Test
    fun `findByCashpoolIdAndUserId - not exists - returns null`() {
        runBlocking {
            val found = cashpoolMemberService.findByCashpoolIdAndUserId(999, 999)
            assertNull(found)
        }
    }

    @Test
    fun `findByCashpoolId - returns all members`() {
        runBlocking {
            val u1 = createTestUser("u1@ex.com")
            val u2 = createTestUser("u2@ex.com")
            val cp = createTestCashpool(u1)
            cashpoolMemberService.create(CreateCashpoolMemberCommand(u1, cp))
            cashpoolMemberService.create(CreateCashpoolMemberCommand(u2, cp))

            val members = cashpoolMemberService.findByCashpoolId(cp)
            assertEquals(2, members.size)
        }
    }

    @Test(expected = io.ktor.server.plugins.NotFoundException::class)
    fun `create member - user not found - throws exception`() {
        runBlocking {
            val userId = createTestUser()
            val cashpoolId = createTestCashpool(userId)
            cashpoolMemberService.create(CreateCashpoolMemberCommand(999, cashpoolId))
        }
    }

    @Test(expected = io.ktor.server.plugins.NotFoundException::class)
    fun `create member - cashpool not found - throws exception`() {
        runBlocking {
            val userId = createTestUser()
            cashpoolMemberService.create(CreateCashpoolMemberCommand(userId, 999))
        }
    }

    @Test
    fun `findById - exists - returns member`() {
        runBlocking {
            val userId = createTestUser()
            val cashpoolId = createTestCashpool(userId)
            val member = cashpoolMemberService.create(CreateCashpoolMemberCommand(userId, cashpoolId))

            val found = cashpoolMemberService.findById(member.id)

            assertNotNull(found)
            assertEquals(member.id, found.id)
        }
    }

    @Test
    fun `findAll - returns all`() {
        runBlocking {
            val userId = createTestUser()
            val cashpoolId = createTestCashpool(userId)
            cashpoolMemberService.create(CreateCashpoolMemberCommand(userId, cashpoolId))

            val all = cashpoolMemberService.findAll()
            assert(all.isNotEmpty())
        }
    }
}
