package service

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Test
import service.cashpool.CashpoolService
import domain.commands.CreateCashpoolCommand
import service.cashpool_member.CashpoolMemberService
import domain.commands.CreateCashpoolMemberCommand
import domain.commands.CreateUserCommand
import service.user.UserService
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock

import core.exceptions.CashpoolMemberNotFound
import domain.repositories.CashpoolMemberRepositoryImpl
import domain.repositories.CashpoolRepositoryImpl
import domain.repositories.UserRepositoryImpl
import core.exceptions.CashpoolNotFound
import core.exceptions.UserNotFound
import kotlin.test.assertFailsWith

class CashpoolMemberServiceTest : BaseServiceTest() {
    private val userRepo = UserRepositoryImpl()
    private val userService = UserService(userRepo)
    private val cashpoolRepo = CashpoolRepositoryImpl()
    private val cashpoolService = CashpoolService(userService, cashpoolRepo)
    private val cashpoolMemberRepo = CashpoolMemberRepositoryImpl()
    private val cashpoolMemberService = CashpoolMemberService(cashpoolMemberRepo, userRepo, cashpoolRepo)

    private suspend fun createTestUser(email: String = "test@example.com"): Int {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        return userService.create(CreateUserCommand(email, "F", "L", null, null, "h", now, false)).id
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
    fun `create member - user not found - fails`() {
        runBlocking {
            val userId = createTestUser()
            val cashpoolId = createTestCashpool(userId)
            assertFailsWith<UserNotFound> {
                cashpoolMemberService.create(CreateCashpoolMemberCommand(999, cashpoolId))
            }
        }
    }

    @Test
    fun `create member - cashpool not found - fails`() {
        runBlocking {
            val userId = createTestUser()
            assertFailsWith<CashpoolNotFound> {
                cashpoolMemberService.create(CreateCashpoolMemberCommand(userId, 999))
            }
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
    fun `findById - not found - throws exception`() {
        runBlocking {
            assertFailsWith<CashpoolMemberNotFound> {
                cashpoolMemberService.findById(999)
            }
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
